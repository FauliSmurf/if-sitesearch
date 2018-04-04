/*
 * Copyright 2018 IntraFind Software AG. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intrafind.sitesearch.gadget

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass

suspend fun main(args: Array<String>) {
    window.addEventListener("DOMContentLoaded", {
        showInitCode()
        js("    IFS.eventbus.addEventListener(IFS.constants.events.SEARCHBAR_RENDERED_INITIALLY, function () {" +
                "        document.getElementById('ifs-sb-searchfield').setAttribute('placeholder', 'Search...');" +
                "    });"
        )
    })
}

private var siteId: String = ""
private var siteSecret: String = ""
private var websiteUrl: String = ""
private val serviceUrl: String = window.location.origin

fun triggerFirstUsageOwnership() {
    val xhr = XMLHttpRequest()
    xhr.open("POST", "$serviceUrl/sites")
    xhr.onload = {
        siteId = JSON.parse<dynamic>(xhr.responseText).siteId as String
        siteSecret = JSON.parse<dynamic>(xhr.responseText).siteSecret as String
        siteIdContainer.textContent = siteId
        siteSecretContainer.textContent = siteSecret
        overrideSite(siteId)
        document.dispatchEvent(Event("sis.triggerFirstUsageOwnershipEvent"))
    }
    xhr.setRequestHeader("content-type", "application/json")
//    xhr.send(JSON.stringify(SiteProfileCreation(setOf(url.value), email.value, !sitemapsIgnore.checked)))
    xhr.send(JSON.stringify(SiteProfileCreation(setOf(SiteProfileConfig(url.value, "", !sitemapsIgnore.checked)), email.value)))
}

@JsName("overrideSite")
fun overrideSite(siteId: String) {
    document.cookie = "override-site = $siteId; domain = .sitesearch.cloud; path = /"
}

@JsName("captchaResult")
lateinit var captchaResult: String

private lateinit var integrationCode: HTMLTextAreaElement
private lateinit var siteIdContainer: HTMLDivElement
private lateinit var siteIdBox: HTMLDivElement
private lateinit var siteSecretContainer: HTMLDivElement
private lateinit var siteSecretBox: HTMLDivElement
private lateinit var captcha: HTMLDivElement
private lateinit var emailContainer: HTMLDivElement
private lateinit var websiteUrlContainer: HTMLDivElement
private lateinit var siteSearchSetupUrl: HTMLDivElement
private lateinit var triggerButton: HTMLButtonElement
private lateinit var url: HTMLInputElement
private lateinit var email: HTMLInputElement
private lateinit var sitemapsIgnore: HTMLInputElement

fun showInitCode() {
    sitemapsIgnore = document.getElementById("sitemapsIgnore") as HTMLInputElement
    email = document.getElementById("email") as HTMLInputElement
    url = document.getElementById("url") as HTMLInputElement
    integrationCode = document.getElementById("integration-code") as HTMLTextAreaElement
    siteIdContainer = document.getElementById("siteId") as HTMLDivElement
    siteSecretContainer = document.getElementById("siteSecret") as HTMLDivElement
    siteIdBox = document.getElementById("siteId-container") as HTMLDivElement
    captcha = document.getElementById("captcha") as HTMLDivElement
    siteSecretBox = document.getElementById("siteSecret-container") as HTMLDivElement
    emailContainer = document.getElementById("email-container") as HTMLDivElement
    websiteUrlContainer = document.getElementById("websiteUrl-container") as HTMLDivElement
    siteSearchSetupUrl = document.getElementById("siteSearchSetupUrl") as HTMLDivElement
    triggerButton = document.getElementById("index") as HTMLButtonElement
    val enterpriseSearchbar = document.getElementById("sitesearch-searchbar") as HTMLDivElement
    val searchbarVersion = "2018-01-15" // when updating, update the value in the corresponding HTML container too
    val siteSearchConfig = "https://cdn.sitesearch.cloud/searchbar/$searchbarVersion/config/sitesearch.json"
    val enterpriseSearchbarCode = enterpriseSearchbar.outerHTML
            .replace("/searchbar/$searchbarVersion/config/sitesearch.json", siteSearchConfig)
    integrationCode.value = enterpriseSearchbarCode

    document.addEventListener("sis.crawlerFinishedEvent", {
        triggerButton.textContent = "Enable Search"
        triggerButton.disabled = false
        (document.getElementById("ifs-sb-searchfield") as HTMLInputElement).placeholder = "$crawlerPageCount pages from ${url.value} have been crawled. Consider that it takes around a minute before you can find here everything we have found."
    })

    enableProactiveValidation()

    val waitWhileCrawlerIsRunningMsg = "Crawler is running... please give us just a minute or two."
    document.addEventListener("sis.triggerFirstUsageOwnershipEvent", {
        startCrawler()
        triggerButton.textContent = waitWhileCrawlerIsRunningMsg
        triggerButton.disabled = true
        siteSearchSetupUrl.innerHTML = "<strong>Copy this search setup URL to resume evaluation later:</strong> " +
                "<a href='https://sitesearch.cloud/getting-started?siteId=$siteId&siteSecret=$siteSecret&url=${url.value}'>" +
                "https://sitesearch.cloud/getting-started?siteId=$siteId&siteSecret=$siteSecret&url=${url.value}" +
                "</a>"
        (document.getElementById("ifs-sb-searchfield") as HTMLInputElement).placeholder = waitWhileCrawlerIsRunningMsg
        insertSiteIdIntoIntegrationCode()
        //TODO make crawl requests visible in logs.sis that do not have any email provided
    })

    applyQueryOverrides()
}

private fun enableProactiveValidation() {
    url.addEventListener("blur", {
        if (!(url.value.startsWith("http") || url.value.startsWith("https"))) {
            url.value = "https://${url.value}"
        }
        validateDomain()
    })

    url.addEventListener("keyup", {
        validateDomain()
    })
}

private fun validateDomain() {
    val xhr = XMLHttpRequest()
    xhr.open("GET", "https://api.muctool.de/curl?url=${url.value}")
    xhr.send()
    xhr.onload = {
        if (allowedToCrawl(xhr)) {
            classifyUrlAsValid(true)
        } else {
            classifyUrlAsValid(false)
        }
    }
}

private fun allowedToCrawl(xhr: XMLHttpRequest) =
        xhr.status.equals(200) && ((JSON.parse<dynamic>(xhr.responseText).code as Short).equals(200) || (JSON.parse<dynamic>(xhr.responseText).code as Short).equals(302))

private var isValidSetup: Boolean = false
private fun classifyUrlAsValid(isValid: Boolean) {
    if (isValid) {
        websiteUrlContainer.removeClass("invalidUrl")
        isValidSetup = true
    } else {
        websiteUrlContainer.addClass("invalidUrl")
        isValidSetup = false
    }
}

@JsName("verifyCallback")
private fun verifyCallback(token: String) {
    captchaResult = token
    triggerButton.disabled = false
}

@JsName("preserveSearchSetup")
private fun preserveSearchSetup() {
    document.execCommand("copy")
}

@JsName("applyQueryOverrides")
private fun applyQueryOverrides() {
    siteId = when {
        window.location.search.indexOf("siteId=") != -1 -> window.location.search.substring(window.location.search.indexOf("siteId=") + 7, 44)
        document.cookie.indexOf("override-site") != -1 ->
            document.cookie.substring(document.cookie.indexOf("override-site") + 14, document.cookie.indexOf("override-site") + 14 + 36) // relies on cookie-setting code in embedding iframe container
        else -> ""
    }
    websiteUrl = when {
        window.location.search.indexOf("url=") != -1 -> window.location.search.substring(window.location.search.indexOf("url=") + 4)
        document.cookie.indexOf("sis.websiteUrl") != -1 -> document.cookie.substring(document.cookie.indexOf("sis.websiteUrl") + 15)
                .substring(0, document.cookie.substring(document.cookie.indexOf("sis.websiteUrl") + 15).indexOf(";")) // relies on cookie-setting code in embedding iframe container
        else -> ""
    }
    if (siteId.isNotEmpty()) {
        url.value = websiteUrl
        url.readOnly = true
        overrideSite(siteId)
        insertSiteIdIntoIntegrationCode()
        triggerButton.style.display = "none"
        captcha.style.display = "none"
        siteSecretBox.style.display = "none"
        emailContainer.style.display = "none"
        siteIdBox.style.display = "none"
    }
}

private fun insertSiteIdIntoIntegrationCode() {
    integrationCode.value = integrationCode.value.replace("siteId: \".+".toRegex(), "siteId: \"$siteId\"")
}

external fun encodeURIComponent(str: String): String
private var crawlerPageCount: Int = 0
fun startCrawler() {
    val xhr = XMLHttpRequest()
    xhr.open("POST", "$serviceUrl/sites/$siteId/crawl?siteSecret=$siteSecret&url=${encodeURIComponent(url.value)}&token=$captchaResult&email=${email.value}&sitemapsOnly=${!sitemapsIgnore.checked}")
    xhr.onload = {
        console.warn(xhr.responseText)
        if (xhr.status.equals(200)) {
            crawlerPageCount = JSON.parse<dynamic>(xhr.responseText).pageCount as Int
            document.dispatchEvent(Event("sis.crawlerFinishedEvent"))
        } else {
            console.error("startCrawler failed")
        }
    }
    xhr.send()
}

class SiteSearch {
    companion object {
        val captchaSiteKey = "6LflVEQUAAAAANVEkwc63uQX96feH1H_6jDU-Bn5"
    }
}

//data class SiteProfileCreation(val urls: Set<String>, val email: String, val sitemapsOnly: Boolean)
data class SiteProfileConfig(val url: String, val pageBodyCssSelector: String = "body", val sitemapsOnly: Boolean = false)

data class SiteProfileCreation(val configs: Set<SiteProfileConfig> = emptySet(), val email: String = "")
