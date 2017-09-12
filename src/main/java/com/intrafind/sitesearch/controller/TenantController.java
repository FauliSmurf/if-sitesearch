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

package com.intrafind.sitesearch.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intrafind.sitesearch.dto.TenantOverview;
import com.intrafind.sitesearch.dto.TenantSiteAssignment;
import jetbrains.exodus.entitystore.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(TenantController.ENDPOINT)
public class TenantController {
    public static final String ENDPOINT = "/tenants";
    private static final Logger LOG = LoggerFactory.getLogger(TenantController.class);
    public static final PersistentEntityStore ACID_PERSISTENCE_ENTITY = PersistentEntityStores.newInstance("data/entity");
    RestTemplate caller = new RestTemplate();

    @RequestMapping(path = "{tenantId}/sites/{siteId}/assign", method = RequestMethod.POST)
    ResponseEntity<TenantSiteAssignment> assignSite(
            @PathVariable(value = "tenantId") UUID tenantId,
            @PathVariable(value = "siteId") UUID siteId,
            @RequestParam(value = "siteSecret") UUID siteSecret,
            @RequestBody TenantSiteAssignment tenantSiteAssignment
    ) {
        // TODO introduce tenantSecret check
        // TODO prevent duplicate Assignments 204, or better NO_MODIFICATION
        final StoreTransaction entityTxn = ACID_PERSISTENCE_ENTITY.beginTransaction();
        Entity tenant = entityTxn.find("Tenant", "id", tenantId.toString()).getFirst();
        if (tenant == null) {
            tenant = entityTxn.newEntity("Tenant");
        }
        // TODO create tenant when it does not exist
        final EntityId id = tenant.getId();
        tenant.setProperty("id", tenantId.toString());
        tenant.setProperty("company", tenantSiteAssignment.getCompany());
        tenant.setProperty("contactEmail", tenantSiteAssignment.getContactEmail());

        LOG.info(entityTxn.find("AuthProvider", "id", tenantSiteAssignment.getAuthProviderId()).size() + " AUTH");
        if (entityTxn.find("AuthProvider", "id", tenantSiteAssignment.getAuthProviderId()).isEmpty()) {
            LOG.info("DONE1");
            final Entity authProvider = entityTxn.newEntity("AuthProvider");
            authProvider.setProperty("id", tenantSiteAssignment.getAuthProviderId());
            tenant.addLink("authProvider", authProvider);   // TODO avoid duplicates // TODO add tests
            authProvider.addLink("tenant", tenant);
        }

        LOG.info(entityTxn.find("Site", "id", siteId.toString()).size() + " SITE");
        if (entityTxn.find("Site", "id", siteId.toString()).isEmpty()) {
            LOG.info("DONE");
            final Entity site = entityTxn.newEntity("Site");
            site.setProperty("id", siteId.toString());
            site.setProperty("secret", siteSecret.toString());
            tenant.addLink("site", site);   // TODO avoid duplicates // TODO add tests
            site.addLink("tenant", tenant);
        }

        entityTxn.commit();

        final Entity assignedTenant = getTenant(id);
        LOG.info("tenantId: " + id.getLocalId());

        return ResponseEntity
                .created(URI.create("https://sitesearch.cloud/").resolve(String.valueOf(id.getLocalId())))
                .build();
    }

    private Entity getTenant(@NotNull EntityId id) {
        final StoreTransaction readTxn = ACID_PERSISTENCE_ENTITY.beginReadonlyTransaction();
        final Entity clientFetched = readTxn.getEntity(id);
        return clientFetched;
    }

    @RequestMapping(path = "{tenantId}/auth-providers/{provider}/{id}", method = RequestMethod.GET)
    ResponseEntity<TenantOverview> obtainTenantOverview(
            @PathVariable(value = "tenantId") UUID tenantId,
            @PathVariable(value = "provider") String provider,
            @PathVariable(value = "id") String providerId,
            @RequestParam(value = "accessToken") String accessToken
    ) {
        // TODO introduce check against oAuth endpoint
        TenantOverview tenantOverview = new TenantOverview(
                Maps.newHashMap(),
                Maps.newHashMap(),
                Lists.newArrayList()
        );
        LOG.info("tenantId: " + tenantId);
        LOG.info("provider: " + provider);
        LOG.info("providerId: " + providerId);
        LOG.info("accessToken: " + accessToken);

        final StoreTransaction findTxn = ACID_PERSISTENCE_ENTITY.beginReadonlyTransaction();
        final EntityIterable authProviders = findTxn.find("AuthProvider", "id", provider + "." + providerId);
        authProviders.forEach(authProvider -> {
            tenantOverview.getAuthProviders().add(authProvider.getProperty("id").toString());
            authProvider.getLinks("tenant").forEach(tenant -> {
                TenantOverview.TenantInfo tenantInfo = new TenantOverview.TenantInfo(tenant.getProperty("company").toString(), tenant.getProperty("contactEmail").toString());
                tenantOverview.getTenants().put(UUID.fromString(tenant.getProperty("id").toString()), tenantInfo);
                tenant.getLinks("site").forEach(site -> {
                    tenantOverview.getSites().put(UUID.fromString(site.getProperty("id").toString()), UUID.fromString(site.getProperty("secret").toString()));
                });
            });
        });
        findTxn.commit();

        return ResponseEntity.ok(tenantOverview);
    }

}
