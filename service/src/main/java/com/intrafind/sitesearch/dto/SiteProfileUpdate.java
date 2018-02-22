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

package com.intrafind.sitesearch.dto;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// TODO consolidate with SiteProfile?
public class SiteProfileUpdate {
    private UUID secret;
    private Set<URI> urls;
    private List<URI> pages;
    private String email;

    private SiteProfileUpdate() {
    }

    public SiteProfileUpdate(UUID secret, Set<URI> urls, String email) {
        this.secret = secret;
        this.urls = urls;
        this.email = email;
    }

    public SiteProfileUpdate(Set<URI> urls, String email) {
        this.urls = urls;
        this.email = email;
    }

    public List<URI> getPages() {
        return pages;
    }

    public UUID getSecret() {
        return secret;
    }

    public void setSecret(UUID secret) {
        this.secret = secret;
    }

    public Set<URI> getUrls() {
        return urls;
    }

    public void setUrls(Set<URI> urls) {
        this.urls = urls;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}