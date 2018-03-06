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
import com.intrafind.sitesearch.dto.FetchedPage;
import com.intrafind.sitesearch.dto.SitesCrawlStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        assertEquals(2, request.getBody().getUrls().size());
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
        assertEquals(16, request.getBody().getUrls().size());
    }

    @Test
    public void crawlSiteSearchViaHttps() throws Exception {
        Instant beforeOperation = Instant.now();
        final ResponseEntity<CrawlerJobResult> request = caller
                .postForEntity(SiteController.ENDPOINT + "/" + CRAWL_SITE_ID + "/crawl?siteSecret=" + CRAWL_SITE_SECRET
                                + "&url=" + "https://sitesearch.cloud&token=" + UUID.randomUUID()
                                + "&email=" + TEST_EMAIL_ADDRESS,
                        RequestEntity.EMPTY, CrawlerJobResult.class);

        assertEquals(HttpStatus.OK, request.getStatusCode());
        assertNotNull(request.getBody());
        assertEquals(15, request.getBody().getPageCount());
        assertEquals(16, request.getBody().getUrls().size());

        // assert correct timestamp after crawling & indexing
        final Set<URI> crawledPage = request.getBody().getUrls();
        assertNotNull(crawledPage);
        final URI crawledPageUrl = new ArrayList<>(request.getBody().getUrls()).get(1);

        TimeUnit.MILLISECONDS.sleep(18_000);
        final ResponseEntity<FetchedPage> fetchedCrawledPage = caller.exchange(SiteController.ENDPOINT
                        + "/" + CRAWL_SITE_ID + "/pages?url=" + crawledPageUrl.toURL(),
                HttpMethod.GET, HttpEntity.EMPTY, FetchedPage.class);
        assertEquals(HttpStatus.OK, fetchedCrawledPage.getStatusCode());
        LOG.warn(fetchedCrawledPage.getBody() + "1<<<<<<<<<<<<<<<<<<");
        LOG.warn(fetchedCrawledPage.getBody().getUrl() + "2<<<<<<<<<<<<<<<<<<");
        LOG.warn(URLEncoder.encode(fetchedCrawledPage.getBody().getUrl()) + "3<<<<<<<<<<<<<<<<<<");
        LOG.warn(URLEncoder.encode(crawledPageUrl.toString()) + "4<<<<<<<<<<<<<<<<<<");
        LOG.warn(crawledPageUrl.toURL() + "5<<<<<<<<<<<<<<<<<<");
        final Instant crawledAndIndexedPage = Instant.parse(fetchedCrawledPage.getBody().getTimestamp());
        assertTrue(crawledAndIndexedPage.isAfter(beforeOperation));
        assertTrue(crawledAndIndexedPage.isBefore(Instant.now()));
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
        assertEquals(8, request.getBody().getUrls().size());
    }

    @Test
    public void recrawl() {
        final SitesCrawlStatus freshlyCrawledSiteStatus = new SitesCrawlStatus(Collections.singletonList(new CrawlStatus(CRAWL_SITE_SECRET, Instant.now())));

        // not authenticated crawl
        final ResponseEntity<SitesCrawlStatus> recrawlNotAuthenticated = caller
                .postForEntity(SiteController.ENDPOINT + "/crawl?serviceSecret=" + UUID.randomUUID(),
                        new HttpEntity<>(freshlyCrawledSiteStatus), SitesCrawlStatus.class);
        assertEquals(HttpStatus.BAD_REQUEST, recrawlNotAuthenticated.getStatusCode());

        // crawl freshly crawled site
        final ResponseEntity<SitesCrawlStatus> recrawlFreshSite = caller
                .postForEntity(SiteController.ENDPOINT + "/crawl?serviceSecret=" + SiteTest.ADMIN_SITE_SECRET,
                        new HttpEntity<>(freshlyCrawledSiteStatus), SitesCrawlStatus.class);
        assertEquals(HttpStatus.OK, recrawlFreshSite.getStatusCode());
        final SitesCrawlStatus freshCrawlStatus = recrawlFreshSite.getBody();
        assertEquals(2, freshCrawlStatus.getSites().size());
        final int crawlSiteIdIndex = 1;
        assertEquals(CRAWL_SITE_ID, freshCrawlStatus.getSites().get(crawlSiteIdIndex).getSiteId());
        // TODO use findSearchSiteCrawlStatus where appropriate to validate only the test site ID
        assertTrue(Instant.parse(
                freshlyCrawledSiteStatus.getSites().get(0).getCrawled())
                .isAfter(Instant.parse(
                        freshCrawlStatus.getSites().get(crawlSiteIdIndex).getCrawled())));

        // crawl all sites
        final ResponseEntity<SitesCrawlStatus> recrawl = caller
                .postForEntity(SiteController.ENDPOINT + "/crawl?serviceSecret=" + SiteTest.ADMIN_SITE_SECRET + "&allSitesCrawl=true",
                        new HttpEntity<>(freshlyCrawledSiteStatus), SitesCrawlStatus.class);

        assertEquals(HttpStatus.OK, recrawl.getStatusCode());
        final SitesCrawlStatus sitesCrawlStatus = recrawl.getBody();
        assertEquals(2, sitesCrawlStatus.getSites().size());
        assertEquals(CRAWL_SITE_ID, sitesCrawlStatus.getSites().get(crawlSiteIdIndex).getSiteId());
        assertTrue(Instant.now().isAfter(Instant.parse(sitesCrawlStatus.getSites().get(crawlSiteIdIndex).getCrawled())));

        // crawl stale site
        final SitesCrawlStatus staleSiteStatus = new SitesCrawlStatus(Collections.singletonList(new CrawlStatus(CRAWL_SITE_SECRET, Instant.now().minus(1, ChronoUnit.DAYS))));
        final ResponseEntity<SitesCrawlStatus> recrawlStaleSite = caller
                .postForEntity(SiteController.ENDPOINT + "/crawl?serviceSecret=" + SiteTest.ADMIN_SITE_SECRET,
                        new HttpEntity<>(staleSiteStatus), SitesCrawlStatus.class);
        assertEquals(HttpStatus.OK, recrawlStaleSite.getStatusCode());
        final SitesCrawlStatus staleCrawlStatus = recrawlStaleSite.getBody();
        assertEquals(2, staleCrawlStatus.getSites().size());
        assertEquals(CRAWL_SITE_ID, staleCrawlStatus.getSites().get(crawlSiteIdIndex).getSiteId());
        assertTrue(Instant.parse(
                staleSiteStatus.getSites().get(0).getCrawled())
                .isBefore(Instant.parse(
                        staleCrawlStatus.getSites().get(crawlSiteIdIndex).getCrawled())));
    }
}


