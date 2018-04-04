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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

// TODO consolidate with SiteProfileUpdate?
public class SiteProfile {
    private UUID id;
    private UUID secret;
    /**
     * @deprecated use Set<Config> instead
     */
    private Set<URI> urls;
    private Set<Config> configs;
    private String email;

    private SiteProfile() {
    }

    public SiteProfile(UUID id, UUID secret, Set<URI> urls, String email, Set<Config> configs) {
        this.id = id;
        this.secret = secret;
        this.urls = urls;
        this.email = email;
        this.configs = configs;
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSecret() {
        return secret;
    }

    public void setSecret(UUID secret) {
        this.secret = secret;
    }

    public Set<Config> getConfigs() {
        return configs;
    }

    public static class Config {
        private URI url;
        /**
         * Defaults to "body", overridable with any other CSS selector.
         */
        private String pageBodyCssSelector = "body";
        private boolean sitemapsOnly = false;

        private Config() {
        }

        public Config(URI url, String pageBodyCssSelector, boolean sitemapsOnly) {
            this.url = url;
            this.pageBodyCssSelector = pageBodyCssSelector;
            this.sitemapsOnly = sitemapsOnly;
        }

        public String getPageBodyCssSelector() {
            return pageBodyCssSelector;
        }

        public URI getUrl() {
            return url;
        }

        public boolean isSitemapsOnly() {
            return sitemapsOnly;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Config config = (Config) o;
            return sitemapsOnly == config.sitemapsOnly &&
                    Objects.equals(url, config.url) &&
                    Objects.equals(pageBodyCssSelector, config.pageBodyCssSelector);
        }

        @Override
        public int hashCode() {

            return Objects.hash(url, pageBodyCssSelector, sitemapsOnly);
        }
    }
}
