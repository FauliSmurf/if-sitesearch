/*
 * Copyright 2017 IntraFind Software AG. All rights reserved.
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

import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLScriptElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.browser.window

fun main(args: Array<String>) {
    window.addEventListener("DOMContentLoaded", {
    })
}

fun triggerFirstUsageOwnership() {
    val serviceUrl: String
    if (window.location.hostname.equals("localhost")) {
        serviceUrl = "http://localhost:8001"
    } else {
        serviceUrl = "https://api.sitesearch.cloud"
    }

    val xhr = XMLHttpRequest()
    val feedUrl = (document.getElementById("feedUrl") as HTMLInputElement).value
    xhr.open("POST", "$serviceUrl/sites/rss?feedUrl=$feedUrl&stripHtmlTags=true")
    xhr.onload = {
        val siteId = JSON.parse<dynamic>(xhr.responseText).siteId as String
        val siteSecret = JSON.parse<dynamic>(xhr.responseText).siteSecret as String
        (document.getElementById("siteId") as HTMLInputElement).value = siteId
        (document.getElementById("siteSecret") as HTMLInputElement).value = siteSecret
        document.cookie = "override-site = $siteId"
        document.dispatchEvent(Event("triggerFirstUsageOwnershipEvent"))
    }
    xhr.send()
}

fun showInitCode() {
    val siteIdContainer = document.getElementById("siteId") as HTMLInputElement
    val enterpriseSearchbar = document.getElementById("sitesearch-searchbar") as HTMLDivElement
    val finderInit = document.getElementById("sitesearch-page-finder-init") as HTMLScriptElement
    val finderContainer = document.getElementById("page-finder") as HTMLDivElement
    val finderVariant = document.getElementById("finder-variant") as HTMLInputElement
    val searchbarInputField = document.getElementById("ifs-sb-searchfield") as HTMLInputElement
    val searchbarVariant = document.getElementById("searchbar-variant") as HTMLInputElement
    val integrationCode = document.getElementById("integration-code") as HTMLTextAreaElement
    searchbarInputField.placeholder = "Search for \"knowledge\""
    val enterpriseSearchbarCode = enterpriseSearchbar.outerHTML
            .replace("searchbar-config/sitesearch-config.json", "https://api.sitesearch.cloud/searchbar-config/sitesearch-config.json")
    integrationCode.value = enterpriseSearchbarCode
    finderContainer.style.display = "none"
    val finderInitCode = "<script src=\"https://api.sitesearch.cloud/app/runtime/kotlin.js\"></script>\n" +
            finderInit.outerHTML
                    .replace("/app/finder/finder.js", "https://api.sitesearch.cloud/app/finder/finder.js")

    searchbarVariant.addEventListener("click", {
        enterpriseSearchbar.style.display = "block"
        finderContainer.style.display = "none"
        if (siteIdContainer.value.isBlank()) {
            integrationCode.value = enterpriseSearchbarCode
        } else {
            integrationCode.value = enterpriseSearchbarCode.replace("siteId\\:\\ \".+".toRegex(), "siteId: \"${siteIdContainer.value}\"")
        }
    })

    finderVariant.addEventListener("click", {
        enterpriseSearchbar.style.display = "none"
        finderContainer.style.display = "block"
        if (siteIdContainer.value.isBlank()) {
            integrationCode.value = finderInitCode
        } else {
            integrationCode.value = finderInitCode.replace("data-siteId=\".+\"".toRegex(RegexOption.IGNORE_CASE), "data-siteId=\"${siteIdContainer.value}\"")
        }
    })

    document.addEventListener("triggerFirstUsageOwnershipEvent", {
        if (!siteIdContainer.value.isBlank()) {
            if (searchbarVariant.checked) {
                integrationCode.value = integrationCode.value.replace("siteId\\:\\ \".+".toRegex(), "siteId: \"${siteIdContainer.value}\"")
            } else {
                integrationCode.value = integrationCode.value.replace("data-siteId=\".+\"".toRegex(RegexOption.IGNORE_CASE), "data-siteId=\"${siteIdContainer.value}\"")
            }
        }
    })
}
