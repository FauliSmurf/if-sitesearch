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

package com.intrafind.sitesearch.integration;

import com.intrafind.sitesearch.controller.SiteController;
import com.intrafind.sitesearch.dto.CrawlStatus;
import com.intrafind.sitesearch.dto.CrawlerJobResult;
import com.intrafind.sitesearch.dto.SitesCrawlStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CrawlerTest {
    static final UUID CRAWL_SITE_ID = UUID.fromString("a2e8d60b-0696-47ea-bc48-982598ee35bd");
    private static final UUID CRAWL_SITE_SECRET = UUID.fromString("04a0afc6-d89a-45c9-8ba8-41d393d8d2f8");
    private static final Logger LOG = LoggerFactory.getLogger(CrawlerTest.class);
    static final String TEST_EMAIL_ADDRESS = "DevOps - Site Search <6752dd9c.intrafind.de@emea.teams.ms>";

    @Autowired
    private TestRestTemplate caller;

    @Test
    public void crawlExampleViaHttp() {
        final ResponseEntity<CrawlerJobResult> request = caller
                .postForEntity(SiteController.ENDPOINT + "/" + CRAWL_SITE_ID + "/crawl?siteSecret=" + CRAWL_SITE_SECRET
                                + "&url=http://example.com&token=" + UUID.randomUUID()
                                + "&email=" + TEST_EMAIL_ADDRESS,
                        RequestEntity.EMPTY, CrawlerJobResult.class);

        assertEquals(HttpStatus.OK, request.getStatusCode());
        assertNotNull(request.getBody());
        assertEquals(1, request.getBody().getPageCount());
    }

    @Test
    public void crawlHttp() {
        final ResponseEntity<CrawlerJobResult> request = caller
                .postForEntity(SiteController.ENDPOINT + "/" + CRAWL_SITE_ID + "/crawl?siteSecret=" + CRAWL_SITE_SECRET
                                + "&url=" + "http://example.de&token=" + UUID.randomUUID()
                                + "&email=" + TEST_EMAIL_ADDRESS,
                        RequestEntity.EMPTY, CrawlerJobResult.class);

        assertEquals(HttpStatus.OK, request.getStatusCode());
        assertNotNull(request.getBody());
        assertEquals(15, request.getBody().getPageCount());
    }

    @Test
    public void crawlSiteSearchViaHttps() {
        final ResponseEntity<CrawlerJobResult> request = caller
                .postForEntity(SiteController.ENDPOINT + "/" + CRAWL_SITE_ID + "/crawl?siteSecret=" + CRAWL_SITE_SECRET
                                + "&url=" + "https://sitesearch.cloud&token=" + UUID.randomUUID()
                                + "&email=" + TEST_EMAIL_ADDRESS,
                        RequestEntity.EMPTY, CrawlerJobResult.class);

        assertEquals(HttpStatus.OK, request.getStatusCode());
        assertNotNull(request.getBody());
        assertEquals(18, request.getBody().getPageCount());
    }

    @Test
    public void crawlHttps() {
        final ResponseEntity<CrawlerJobResult> request = caller
                .postForEntity(SiteController.ENDPOINT + "/" + CRAWL_SITE_ID + "/crawl?siteSecret=" + CRAWL_SITE_SECRET
                                + "&url=" + "https://api.sitesearch.cloud&token=" + UUID.randomUUID()
                                + "&email=" + TEST_EMAIL_ADDRESS,
                        RequestEntity.EMPTY, CrawlerJobResult.class);

        assertEquals(HttpStatus.OK, request.getStatusCode());
        assertNotNull(request.getBody());
        assertEquals(7, request.getBody().getPageCount());
    }

    @Test
    public void considerNoindexWhileCrawlingWww() {
        final SitesCrawlStatus siteToCrawl = new SitesCrawlStatus(Arrays.asList(new CrawlStatus(UUID.fromString("8e0af062-cb74-4529-9b7b-47ca1c101ae8"), Instant.now())));
        final ResponseEntity<CrawlerJobResult> request = caller
                .postForEntity(SiteController.ENDPOINT + "/crawl?siteSecret=" + SiteTest.ADMIN_SITE_SECRET
                                + "&url=" + "https://www.migrosbank.ch/de&clearIndex=true&isThrottled=true",
                        new HttpEntity<>(siteToCrawl), CrawlerJobResult.class);

        assertEquals(HttpStatus.OK, request.getStatusCode());
        assertEquals(154, request.getBody().getPageCount());
    }

    @Test
    public void considerNoindexWhileCrawlingBlog() {
        final SitesCrawlStatus siteToCrawl = new SitesCrawlStatus(Arrays.asList(new CrawlStatus(UUID.fromString("f771eb6b-80d6-4e9f-a660-22c9972a8e06"), Instant.now())));
        final ResponseEntity<CrawlerJobResult> request = caller
                .postForEntity(SiteController.ENDPOINT + "/crawl?siteSecret=" + SiteTest.ADMIN_SITE_SECRET
                                + "&url=" + "https://blog.migrosbank.ch/de&clearIndex=true&isThrottled=true",
                        new HttpEntity<>(siteToCrawl), CrawlerJobResult.class);

        assertEquals(HttpStatus.OK, request.getStatusCode());
        assertEquals(374, request.getBody().getPageCount());
    }

    @Test
    public void recrawl() {
        final SitesCrawlStatus freshlyCrawledSiteStatus = new SitesCrawlStatus(Arrays.asList(new CrawlStatus(CRAWL_SITE_SECRET, Instant.now())));

        // not authenticated crawl
        final ResponseEntity<SitesCrawlStatus> recrawlNotAuthenticated = caller
                .postForEntity(SiteController.ENDPOINT + "/crawl?serviceSecret=" + UUID.randomUUID(),
                        new HttpEntity<>(freshlyCrawledSiteStatus), SitesCrawlStatus.class);
        assertEquals(HttpStatus.BAD_REQUEST, recrawlNotAuthenticated.getStatusCode());

        // fetch recrawled sites set
        final ResponseEntity<SitesCrawlStatus> currentlyRecrawled = caller
                .getForEntity(SiteController.ENDPOINT + "/crawl/status?serviceSecret=" + SiteTest.ADMIN_SITE_SECRET,
                        SitesCrawlStatus.class);
        assertEquals(HttpStatus.OK, currentlyRecrawled.getStatusCode());
        assertTrue(1 < currentlyRecrawled.getBody().getSites().size());

        // crawl freshly crawled site
        final ResponseEntity<SitesCrawlStatus> recrawlFreshSite = caller
                .postForEntity(SiteController.ENDPOINT + "/crawl?serviceSecret=" + SiteTest.ADMIN_SITE_SECRET,
                        new HttpEntity<>(freshlyCrawledSiteStatus), SitesCrawlStatus.class);
        assertEquals(HttpStatus.OK, recrawlFreshSite.getStatusCode());
        final SitesCrawlStatus freshCrawlStatus = recrawlFreshSite.getBody();
        assertTrue(1 < freshCrawlStatus.getSites().size());
        assertTrue(containsUpdatedSiteId(freshCrawlStatus));
        // TODO use findSearchSiteCrawlStatus where appropriate to validate only the test site ID
        assertTrue(Instant.parse(
                freshlyCrawledSiteStatus.getSites().get(0).getCrawled())
                .isAfter(Instant.parse(
                        getCrawlStatusWithUpdatedSiteId(freshCrawlStatus).getCrawled())));

        // crawl all sites
        final ResponseEntity<SitesCrawlStatus> recrawl = caller
                .postForEntity(SiteController.ENDPOINT + "/crawl?serviceSecret=" + SiteTest.ADMIN_SITE_SECRET + "&allSitesCrawl=true",
                        new HttpEntity<>(freshlyCrawledSiteStatus), SitesCrawlStatus.class);

        assertEquals(HttpStatus.OK, recrawl.getStatusCode());
        final SitesCrawlStatus sitesCrawlStatus = recrawl.getBody();
        assertTrue(1 < sitesCrawlStatus.getSites().size());
        assertTrue(containsUpdatedSiteId(sitesCrawlStatus));
        assertTrue(Instant.now().isAfter(Instant.parse(getCrawlStatusWithUpdatedSiteId(sitesCrawlStatus).getCrawled())));

        // crawl stale site
        final SitesCrawlStatus staleSiteStatus = new SitesCrawlStatus(Collections.singletonList(new CrawlStatus(CRAWL_SITE_SECRET, Instant.now().minus(1, ChronoUnit.DAYS))));
        final ResponseEntity<SitesCrawlStatus> recrawlStaleSite = caller
                .postForEntity(SiteController.ENDPOINT + "/crawl?serviceSecret=" + SiteTest.ADMIN_SITE_SECRET,
                        new HttpEntity<>(staleSiteStatus), SitesCrawlStatus.class);
        assertEquals(HttpStatus.OK, recrawlStaleSite.getStatusCode());
        final SitesCrawlStatus staleCrawlStatus = recrawlStaleSite.getBody();
        assertTrue(1 < staleCrawlStatus.getSites().size());
        assertTrue(containsUpdatedSiteId(staleCrawlStatus));
        assertTrue(Instant.parse(
                staleSiteStatus.getSites().get(0).getCrawled())
                .isBefore(Instant.parse(
                        getCrawlStatusWithUpdatedSiteId(staleCrawlStatus).getCrawled())));
    }

    private CrawlStatus getCrawlStatusWithUpdatedSiteId(SitesCrawlStatus freshCrawlStatus) {
        for (CrawlStatus crawlStatus : freshCrawlStatus.getSites()) {
            if (CRAWL_SITE_ID.equals(crawlStatus.getSiteId())) {
                return crawlStatus;
            }
        }
        return null;
    }

    private boolean containsUpdatedSiteId(SitesCrawlStatus freshCrawlStatus) {
        boolean freshCrawlStatusCheck = false;
        for (CrawlStatus crawlStatus : freshCrawlStatus.getSites()) {
            if (CRAWL_SITE_ID.equals(crawlStatus.getSiteId())) {
                freshCrawlStatusCheck = true;
            }
        }
        return freshCrawlStatusCheck;
    }
}


