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

package com.intrafind.sitesearch.integration;

import com.intrafind.sitesearch.controller.PageController;
import com.intrafind.sitesearch.controller.SiteController;
import com.intrafind.sitesearch.dto.Page;
import com.intrafind.sitesearch.dto.Tenant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PageTest {
    private final static Logger LOG = LoggerFactory.getLogger(PageTest.class);
    @Autowired
    private TestRestTemplate caller;

    private static String testPageId;
    private static UUID testSiteSiteSecret;
    private static UUID testSiteId;

    static Page buildSite(UUID siteSecret) {
        final UUID testSiteId = UUID.fromString("1a6715d9-119f-48d1-9329-e8763273bbea");
        final String url = "https://api.sitesearch.cloud";
        return new Page(
                Page.hashPageId(testSiteId, url),
                testSiteId, siteSecret,
                "Cloud Solution",
                "Site Search is IntraFind's on-demand solution for site search.",
                url
        );
    }

    @Before
    public void init() throws Exception {
        Page testPage = createNewSiteViaPageCreation();
        testPageId = testPage.getId();
        testSiteId = testPage.getSiteId();
        testSiteSiteSecret = testPage.getSiteSecret();
    }

    private Page createNewSiteViaPageCreation() throws Exception {
        UUID irrelevantPageId = UUID.fromString("f55d093a-7911-11e7-8fc8-025041000001");
        Page simple = buildSite(UUID.randomUUID());

        ResponseEntity<Page> actual = caller.exchange(PageController.ENDPOINT, HttpMethod.POST, new HttpEntity<>(simple), Page.class);

        assertEquals(HttpStatus.CREATED, actual.getStatusCode());
        assertEquals(simple, actual.getBody());
        assertNotEquals("assure irrelevancy of siteId during creation", irrelevantPageId, actual.getBody().getId());
        assertEquals("https://api.sitesearch.cloud/sites/" + actual.getBody().getId(), actual.getHeaders().get(HttpHeaders.LOCATION).get(0));

        ResponseEntity<Page> newlyCreatedSite = caller.exchange(PageController.ENDPOINT + "/" + actual.getBody().getId(), HttpMethod.GET, HttpEntity.EMPTY, Page.class);
        assertEquals(HttpStatus.OK, newlyCreatedSite.getStatusCode());
        assertEquals(actual.getBody().getId(), newlyCreatedSite.getBody().getId());

        return actual.getBody();
    }

    @Test
    public void updateSiteViaUrl() throws Exception {
        final Page newPage = createNewSiteViaPageCreation();
        final String updatedBodyContent = "Updated via Hash(siteId, URL)";
        newPage.setBody(updatedBodyContent);

        TimeUnit.MILLISECONDS.sleep(8_000);

        // update
        final ResponseEntity<Page> updatedSite = caller.exchange(SiteController.ENDPOINT
                        + "/" + newPage.getSiteId() + "/pages?siteSecret=" + newPage.getSiteSecret(),
                HttpMethod.PUT, new HttpEntity<>(newPage), Page.class);
        assertEquals(HttpStatus.OK, updatedSite.getStatusCode());
        assertEquals(newPage.getId(), updatedSite.getBody().getId());
        assertEquals(updatedBodyContent, updatedSite.getBody().getBody());

        // fetch & check updated site
        assertEquals(Page.hashPageId(newPage.getSiteId(), newPage.getUrl()), newPage.getId());
        final ResponseEntity<Page> fetchedUpdatedSite = caller.exchange(PageController.ENDPOINT
                        + "/" + Page.hashPageId(newPage.getSiteId(), newPage.getUrl()),
                HttpMethod.GET, HttpEntity.EMPTY, Page.class);

        assertEquals(HttpStatus.OK, fetchedUpdatedSite.getStatusCode());
        assertEquals(newPage.getId(), fetchedUpdatedSite.getBody().getId());
        assertEquals(updatedBodyContent, fetchedUpdatedSite.getBody().getBody());

        // fetch via URL
        final ResponseEntity<Page> fetchViaUrl = caller.exchange(SiteController.ENDPOINT
                        + "/" + newPage.getSiteId() + "/pages?url=" + newPage.getUrl(),
                HttpMethod.GET, HttpEntity.EMPTY, Page.class);
        assertEquals(HttpStatus.OK, fetchViaUrl.getStatusCode());
        assertEquals(newPage.getId(), fetchViaUrl.getBody().getId());
        assertEquals(updatedBodyContent, fetchViaUrl.getBody().getBody());
        assertEquals(newPage.getUrl(), fetchViaUrl.getBody().getUrl());
    }

    @Test
    public void fetchUpdatedById() throws Exception {
        Page ying = createNewSiteViaPageCreation();
        Page yang = createNewSiteViaPageCreation();
        TimeUnit.MILLISECONDS.sleep(8_000);

        final ResponseEntity<Page> actualYing = caller.exchange(SiteController.ENDPOINT + "/"
                + ying.getSiteId() + "/pages/" + ying.getId() + "?siteSecret=" + ying.getSiteSecret(), HttpMethod.PUT, new HttpEntity<>(ying), Page.class);
        assertEquals(HttpStatus.OK, actualYing.getStatusCode());
        assertEquals(ying, actualYing.getBody());
        final ResponseEntity<Page> actualYang = caller.exchange(SiteController.ENDPOINT + "/"
                + yang.getSiteId() + "/pages/" + yang.getId() + "?siteSecret=" + yang.getSiteSecret(), HttpMethod.PUT, new HttpEntity<>(yang), Page.class);
        assertEquals(HttpStatus.OK, actualYang.getStatusCode());
        assertEquals(yang, actualYang.getBody());

        final ResponseEntity<Page> actualYingFetched = caller.getForEntity(PageController.ENDPOINT + "/" + ying.getId(), Page.class);
        assertEquals(HttpStatus.OK, actualYingFetched.getStatusCode());
        assertEquals(ying, actualYingFetched.getBody());

        final ResponseEntity<Page> actualYangFetched = caller.getForEntity(PageController.ENDPOINT + "/" + yang.getId(), Page.class);
        assertEquals(HttpStatus.OK, actualYangFetched.getStatusCode());
        assertEquals(yang, actualYangFetched.getBody());

        Page fetchedYing = actualYingFetched.getBody();
        Page fetchedYang = actualYangFetched.getBody();
        assertEquals(fetchedYing, fetchedYang);
        assertNotEquals(fetchedYing.getId(), fetchedYang.getId());
        assertNotEquals(fetchedYing.getSiteId(), fetchedYang.getSiteId());
        assertNull(fetchedYing.getSiteSecret());
        assertNull(fetchedYang.getSiteSecret());
    }

    @Test
    public void updatedSite() throws Exception {
        Page createdPage = createNewSiteViaPageCreation();

        TimeUnit.MILLISECONDS.sleep(8_000);

        final ResponseEntity<Page> updateWithSiteIdOnly = caller.exchange(SiteController.ENDPOINT + "/" + createdPage.getSiteId()
                + "/pages/" + createdPage.getId(), HttpMethod.PUT, new HttpEntity<>(createdPage), Page.class);
        assertEquals("only valid siteId is provided", HttpStatus.BAD_REQUEST, updateWithSiteIdOnly.getStatusCode());
        assertEquals(29791, updateWithSiteIdOnly.getBody().hashCode());

        final ResponseEntity<Page> updateWithSiteSecretOnly = caller.exchange(SiteController.ENDPOINT + "/" + createdPage.getSiteId()
                + "/pages/" + createdPage.getId(), HttpMethod.PUT, new HttpEntity<>(createdPage), Page.class);
        assertEquals("only valid siteSecret is provided", HttpStatus.BAD_REQUEST, updateWithSiteSecretOnly.getStatusCode());
        assertEquals(29791, updateWithSiteSecretOnly.getBody().hashCode());

        final ResponseEntity<Page> updateWithWrongSiteSecret = caller.exchange(SiteController.ENDPOINT + "/" + createdPage.getSiteId()
                        + "/pages/" + createdPage.getId() + "?siteSecret=" + UUID.randomUUID(),
                HttpMethod.PUT, new HttpEntity<>(createdPage), Page.class);
        assertEquals("siteSecret is invalid", HttpStatus.NOT_FOUND, updateWithWrongSiteSecret.getStatusCode());
        assertNull(updateWithWrongSiteSecret.getBody());

        createdPage.setTitle("updated title");
        createdPage.setBody("updated body");
        createdPage.setUrl("https://example.com/updated");
        final ResponseEntity<Page> updated = caller.exchange(SiteController.ENDPOINT + "/" + createdPage.getSiteId()
                        + "/pages/" + createdPage.getId() + "?siteSecret=" + createdPage.getSiteSecret(),
                HttpMethod.PUT, new HttpEntity<>(createdPage), Page.class);
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertEquals(createdPage, updated.getBody());
        assertEquals("updated body", updated.getBody().getBody());
    }

    @Test
    public void importFeed() throws Exception {
        final ResponseEntity<Tenant> exchange = caller.exchange(
                SiteController.ENDPOINT + "/rss?feedUrl=http://www.mvv-muenchen.de/de/aktuelles/fahrplanaenderungen/detail/rss.xml",
                HttpMethod.POST, HttpEntity.EMPTY, Tenant.class);
        final Tenant creation = validateTenantSummary(exchange, 10);

        TimeUnit.MILLISECONDS.sleep(8_000);
        validateUpdatedSites(creation);
    }

    @Test
    public void importFeedAndReadSingleSiteWithSSL() throws Exception {  // TODO actually use SSL below >> httpS instead of http-sans-S
        final ResponseEntity<Tenant> exchange = caller.exchange(SiteController.ENDPOINT + "/rss?feedUrl=http://intrafind.de/share/enterprise-search-blog.xml",
                HttpMethod.POST, HttpEntity.EMPTY, Tenant.class);
        final Tenant creation = validateTenantSummary(exchange, 25);

        TimeUnit.MILLISECONDS.sleep(8_000);
        LOG.info("siteId: " + creation.getSiteId());
        validateUpdatedSites(creation);
    }

    private void validateUpdatedSites(Tenant tenant) {
        tenant.getDocuments().forEach(documentId -> {
            final ResponseEntity<Page> fetchedById = caller.exchange(
                    PageController.ENDPOINT + "/" + documentId, HttpMethod.GET, HttpEntity.EMPTY, Page.class);
            assertTrue(HttpStatus.OK.equals(fetchedById.getStatusCode()));
            assertTrue(tenant.getSiteId().equals(fetchedById.getBody().getSiteId()));
            assertTrue(!fetchedById.getBody().getBody().isEmpty());
            assertNotNull(fetchedById.getBody().getUrl());
            assertNull(fetchedById.getBody().getSiteSecret());
        });
    }

    @Test
    public void importFeedStrippingHtml() throws Exception {
        // create index with stripped HTML tags
        final ResponseEntity<Tenant> initialIndexCreation = caller.exchange(
                SiteController.ENDPOINT + "/rss?feedUrl=http://intrafind.de/share/enterprise-search-blog.xml&stripHtmlTags=true",
                HttpMethod.POST, HttpEntity.EMPTY, Tenant.class);
        TimeUnit.MILLISECONDS.sleep(8_000);
        final Tenant tenantCreation = validateTenantSummary(initialIndexCreation, 25);

        UUID siteIdFromCreation = tenantCreation.getSiteId();
        UUID siteSecretFromCreation = tenantCreation.getSiteSecret();

        LOG.info("siteIdFromCreation: " + siteIdFromCreation);
        LOG.info("siteSecretFromCreation: " + siteSecretFromCreation);
    }

    @Test
    public void importFeedAndUpdate() throws Exception {
        // create index
        final ResponseEntity<Tenant> initialIndexCreation = caller.exchange(
                SiteController.ENDPOINT + "/rss?feedUrl=http://www.mvv-muenchen.de/de/aktuelles/meldungen/detail/rss.xml",
                HttpMethod.POST, HttpEntity.EMPTY, Tenant.class);
        TimeUnit.MILLISECONDS.sleep(8_000);
        final Tenant tenantCreation = validateTenantSummary(initialIndexCreation, 10);

        UUID siteIdFromCreation = tenantCreation.getSiteId();
        UUID siteSecretFromCreation = tenantCreation.getSiteSecret();

        LOG.info("siteIdFromCreation: " + siteIdFromCreation);
        LOG.info("siteSecretFromCreation: " + siteSecretFromCreation);

        final ResponseEntity<Tenant> updateWithoutSecret = caller.exchange(
                SiteController.ENDPOINT + "/" + siteIdFromCreation + "/rss?feedUrl=http://intrafind.de/share/enterprise-search-blog.xml",
                HttpMethod.PUT, HttpEntity.EMPTY, Tenant.class);
        assertEquals(HttpStatus.BAD_REQUEST, updateWithoutSecret.getStatusCode());

        final ResponseEntity<Tenant> updateWithInvalidSecret = caller.exchange(
                SiteController.ENDPOINT + "/" + siteIdFromCreation + "/rss?feedUrl=http://intrafind.de/share/enterprise-search-blog.xml"
                        + "&siteSecret=" + UUID.randomUUID(),
                HttpMethod.PUT, HttpEntity.EMPTY, Tenant.class);
        assertEquals(HttpStatus.BAD_REQUEST, updateWithInvalidSecret.getStatusCode());


        // update index
        final ResponseEntity<Tenant> anotherFeedReplacement = caller.exchange(
                SiteController.ENDPOINT + "/" + siteIdFromCreation + "/rss?feedUrl=http://intrafind.de/share/enterprise-search-blog.xml"
                        + "&siteSecret=" + siteSecretFromCreation,
                HttpMethod.PUT, HttpEntity.EMPTY, Tenant.class);
        final Tenant tenantUpdate = validateTenantSummary(anotherFeedReplacement, 25);

        validateUpdatedSites(tenantUpdate);

        tryDeletionOfSites(siteIdFromCreation, siteSecretFromCreation);
    }

    private void tryDeletionOfSites(UUID siteIdFromCreation, UUID siteSecretFromCreation) {
        final ResponseEntity<List> fetchAll = caller.exchange(SiteController.ENDPOINT + "/" + siteIdFromCreation, HttpMethod.GET, HttpEntity.EMPTY, List.class);
        assertTrue(HttpStatus.OK.equals(fetchAll.getStatusCode()));
        @SuppressWarnings("unchecked")
        List<String> pages = fetchAll.getBody();
        assertTrue(1 < pages.size());
        int siteCountBeforeDeletion = pages.size();

        for (String pageId : pages) {
            LOG.info("pageId: " + pageId);

            // delete using an invalid siteSecret
            UUID invalidSiteSecret = UUID.randomUUID();
            final ResponseEntity<ResponseEntity> deletionWithInvalidSiteSecret = caller.exchange(SiteController.ENDPOINT + "/" + siteIdFromCreation + "/pages/" + pageId + "?siteSecret=" + invalidSiteSecret, HttpMethod.DELETE, HttpEntity.EMPTY, ResponseEntity.class);
            assertEquals(HttpStatus.NOT_FOUND, deletionWithInvalidSiteSecret.getStatusCode());
            assertNull(deletionWithInvalidSiteSecret.getBody());

            // delete using a valid siteSecret
            final ResponseEntity<ResponseEntity> deletion = caller.exchange(SiteController.ENDPOINT + "/" + siteIdFromCreation + "/pages/" + pageId + "?siteSecret=" + siteSecretFromCreation, HttpMethod.DELETE, HttpEntity.EMPTY, ResponseEntity.class);
            assertEquals(HttpStatus.NO_CONTENT, deletion.getStatusCode());
            assertNull(deletion.getBody());
        }

//        pages.stream().forEach(pageId -> {
//            LOG.info("pageId: " + pageId);
//            final ResponseEntity<ResponseEntity> deletion = caller.exchange(SiteController.ENDPOINT + "/" + siteIdFromCreation+"/pages/"+pageId +"?siteSecret="+siteSecretFromCreation, HttpMethod.DELETE, HttpEntity.EMPTY, ResponseEntity.class);
//            assertEquals(HttpStatus.NO_CONTENT, deletion.getStatusCode());
//            assertNull(deletion.getBody());
//        });
        LOG.info("siteCountBeforeDeletion: " + siteCountBeforeDeletion);
    }

    private Tenant validateTenantSummary(ResponseEntity<Tenant> anotherFeedReplacement, int indexEntriesCount) {
        assertEquals(HttpStatus.OK, anotherFeedReplacement.getStatusCode());
        final Tenant tenantUpdate = anotherFeedReplacement.getBody();
        assertTrue(tenantUpdate.getSiteId() != null);
        assertTrue(tenantUpdate.getSiteSecret() != null);
        assertEquals(indexEntriesCount, tenantUpdate.getSuccessCount());
        assertEquals(indexEntriesCount, tenantUpdate.getDocuments().size());
        assertTrue(tenantUpdate.getFailed().isEmpty());
        return tenantUpdate;
    }
}