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

package com.intrafind.sitesearch.jmh;

import com.intrafind.sitesearch.controller.AutocompleteController;
import com.intrafind.sitesearch.controller.SearchController;
import com.intrafind.sitesearch.dto.Autocomplete;
import com.intrafind.sitesearch.dto.Hits;
import com.intrafind.sitesearch.dto.Site;
import com.intrafind.sitesearch.integration.SearchTest;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Threads(10)
@BenchmarkMode(Mode.Throughput)
public class Load {
    private static final String[] loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras viverra enim vitae malesuada placerat. Nam auctor pellentesque libero, et venenatis enim molestie vel. Duis est metus, congue quis orci id, tincidunt mattis turpis. In fringilla ultricies sapien ultrices accumsan. Sed mattis tellus lacus, quis scelerisque turpis hendrerit et. In iaculis malesuada ipsum, ac rhoncus mauris auctor quis. Proin varius, ex vestibulum condimentum lacinia, ligula est finibus ligula, id consectetur nisi enim ut velit. Sed aliquet gravida justo ac condimentum. In malesuada sed elit vitae vestibulum. Mauris vitae congue lacus. Quisque vitae tincidunt orci. Donec viverra enim a lacinia pulvinar. Sed vel ullamcorper est. Vestibulum vel urna at nisl tincidunt blandit. Donec purus leo, interdum in diam in, posuere varius tellus. Quisque eleifend nulla at nulla vestibulum ullamcorper. Praesent interdum vehicula cursus. Morbi vitae nunc et urna rhoncus semper aliquam nec velit. Quisque aliquet et velit ut mollis. Sed mattis eleifend tristique. Praesent pharetra, eros eget viverra tempus, nisi turpis molestie metus, nec tristique nulla dolor a mauris. Nullam cursus finibus erat, in pretium urna fermentum ac. In hac habitasse platea dictumst. Cras id velit id nisi euismod eleifend. Duis vehicula gravida bibendum. Cras rhoncus, massa et accumsan euismod, metus arcu rutrum orci, eu porttitor lacus tellus sed quam. Morbi tincidunt est sit amet sem convallis porta in nec nisi. Sed ex enim, fringilla nec diam in, luctus pulvinar enim. Suspendisse potenti. Quisque ut pellentesque erat. In tincidunt metus id sem fringilla sagittis. Interdum et malesuada fames ac ante ipsum primis in faucibus. Proin erat nunc, pharetra sit amet iaculis nec, malesuada eu dui. Nullam sagittis ut arcu vitae convallis. Mauris molestie gravida lectus, eu commodo quam bibendum aliquam. Donec laoreet sed dolor eu consectetur."
            .split("\\s");

    private final static Logger LOG = LoggerFactory.getLogger(Load.class);
    private static final String LOAD_TARGET = "https://dev.sitesearch.cloud";
    //    private static final String LOAD_TARGET = "http://localhost:8001";
    private static final TestRestTemplate CALLER = new TestRestTemplate();
    private static Random PSEUDO_ENTROPY = new Random();

    private static final List<UUID> tenants = Arrays.asList(
            SearchTest.SEARCH_TENANT_ID,
            UUID.fromString("363d50f3-17cb-4756-aeca-7d3768092ae1"),
            UUID.fromString("1a6715d9-119f-48d1-9329-e8763273bbea")
    );
    private static final Map<String, Long> queries = new HashMap<>();

    static {
        queries.put("knowledge", 1L);
        queries.put("ifinder", 7L);
        queries.put("\uD83E\uDD84", 25L);
    }

    private static final Map<String, Long> autocompleteQueries = new HashMap<>();
    private static final List<String> queryListSearch = new ArrayList<>(queries.keySet());

    @Benchmark
    public void staticFiles() throws Exception {
        final ResponseEntity<String> staticFile = CALLER.getForEntity(LOAD_TARGET, String.class);

        assertEquals(HttpStatus.OK, staticFile.getStatusCode());
        assertFalse(staticFile.getBody().isEmpty());
    }

    private static final List<String> queryListAutocomplete = new ArrayList<>(autocompleteQueries.keySet());

    static {
        autocompleteQueries.put("kno", 1L);
        autocompleteQueries.put("know", 1L);
        autocompleteQueries.put("knowl", 1L);
        autocompleteQueries.put("knowle", 1L);
        autocompleteQueries.put("ifi", 6L);
        autocompleteQueries.put("ifin", 6L);
        autocompleteQueries.put("ifind", 6L);
        autocompleteQueries.put("ifinde", 6L);
        autocompleteQueries.put("ifinder", 6L);
    }

    @Benchmark
    public void searchComplex() throws Exception {
        final int queryIndex = PSEUDO_ENTROPY.nextInt(queries.size());
        final String query = queryListSearch.get(queryIndex);

        final ResponseEntity<Hits> actual = CALLER.getForEntity(
                LOAD_TARGET + SearchController.ENDPOINT
                        + "?query=" + query + "&tenantId=" + SearchTest.SEARCH_TENANT_ID,
                Hits.class
        );

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        final long queryResultCount = queries.get(query);
        assertEquals(queryResultCount, actual.getBody().getResults().size());
    }

    @Benchmark
    public void autocomplete() throws Exception {
        final int queryIndex = PSEUDO_ENTROPY.nextInt(autocompleteQueries.size());
        final String query = queryListAutocomplete.get(queryIndex);

        final ResponseEntity<Autocomplete> actual = CALLER.getForEntity(
                LOAD_TARGET + AutocompleteController.ENDPOINT
                        + "?query=" + query + "&tenantId=" + SearchTest.SEARCH_TENANT_ID,
                Autocomplete.class
        );

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        final long queryResultCount = autocompleteQueries.get(query);
        assertEquals(queryResultCount, actual.getBody().getResults().size());
    }

    private String generateLoremIpsum() {
        final StringBuilder loremIpsumText = new StringBuilder();
        for (String word : loremIpsum) {
            final int wordIndex = PSEUDO_ENTROPY.nextInt(loremIpsum.length);
            loremIpsumText.append(loremIpsum[wordIndex]).append(" ");
        }
        return loremIpsumText.toString();
    }

    @Threads(1)
    @Benchmark
    public void indexNew() throws Exception {
        final String loremIpsumText = generateLoremIpsum();
        final Site siteToIndex = new Site(
                null, null, null,
                loremIpsumText.substring(0, 42),
                loremIpsumText,
                "https://example.com/" + UUID.randomUUID()
        );

//        final ResponseEntity<Site> actual = CALLER.postForEntity(
//                LOAD_TARGET + SiteController.ENDPOINT
//                        + "?tenantId=" + SearchTest.SEARCH_TENANT_ID,
//                siteToIndex,
//                Site.class
//        );
//
//        assertEquals(HttpStatus.CREATED, actual.getStatusCode());
//        assertNotNull(actual.getHeaders().getLocation());
    }

    @Threads(1)
    @Benchmark
    public void indexUpdate() throws Exception {
        final String loremIpsumText = generateLoremIpsum();
    }
}
