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

package de.intrafind.sitesearch.integration;

import de.intrafind.sitesearch.dto.Site;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;

import static de.intrafind.sitesearch.controller.SiteController.ENDPOINT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SiteTest {
    private final static Logger LOG = LoggerFactory.getLogger(SiteTest.class);
    @Autowired
    private TestRestTemplate caller;

    private static Site buildSite(String id) {
        Site simple = new Site();
        simple.setUrl(URI.create("https://www.intrafind.de/cloud"));
        simple.setTenant("1a6715d9-119f-48d1-9329-e8763273bbea");
        simple.setContent("Sitesearch is IntraFind's new SaaS solution.");
        simple.setTitle("Cloud Solution");
        simple.setId(id); // is ignored when persisted
        return simple;
    }

    @Test
    public void simpleIndexWithCloudInside() throws Exception {
        Site simple = buildSite("123");

        final ResponseEntity<Site> actual = caller.postForEntity(ENDPOINT + "/123", simple, Site.class);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(simple, actual.getBody());
    }

    @Test
    public void assureIrrelevancyOfSiteIdInBody() throws Exception {
        String relevantSiteId = "124";
        String irrelevantSiteId = "random";
        
        Site simple = new Site();
        simple.setUrl(URI.create("https://www.intrafind.de/saas"));
        simple.setTenant("1a6715d9-119f-48d1-9329-e8763273bbea");
        simple.setContent("Sitesearch is IntraFind's new SaaS solution.");
        simple.setTitle("SaaS Solution");
        simple.setId(irrelevantSiteId);

        ResponseEntity<Site> actual = caller.postForEntity(ENDPOINT + "/" + relevantSiteId, simple, Site.class);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertNotEquals(simple, actual.getBody());
        assertNotEquals(irrelevantSiteId, actual.getBody().getId());
        assertEquals(relevantSiteId, actual.getBody().getId());
    }

    @Test
    public void fetchById() throws Exception {
        Site ying = buildSite("200");
        Site yang = buildSite("300");

        final ResponseEntity<Site> actualYing = caller.postForEntity(ENDPOINT + "/200", ying, Site.class);
        assertEquals(HttpStatus.OK, actualYing.getStatusCode());
        assertEquals(ying, actualYing.getBody());
        final ResponseEntity<Site> actualYang = caller.postForEntity(ENDPOINT + "/300", yang, Site.class);
        assertEquals(HttpStatus.OK, actualYang.getStatusCode());
        assertEquals(yang, actualYang.getBody());

        final ResponseEntity<Site> actualYingFetched = caller.getForEntity(ENDPOINT + "/200", Site.class);
        assertEquals(HttpStatus.OK, actualYingFetched.getStatusCode());
        assertEquals(ying, actualYingFetched.getBody());

        final ResponseEntity<Site> actualYangFetched = caller.getForEntity(ENDPOINT + "/300", Site.class);
        assertEquals(HttpStatus.OK, actualYangFetched.getStatusCode());
        assertEquals(yang, actualYangFetched.getBody());

        assertNotEquals(actualYingFetched, actualYangFetched);
    }

    // TODO provoke 400 responses

    // TODO overwrite Site with same ID but other content, aka update
}