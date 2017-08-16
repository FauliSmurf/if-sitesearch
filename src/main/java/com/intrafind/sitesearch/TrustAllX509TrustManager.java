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

package com.intrafind.sitesearch;

import com.sun.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;

public class TrustAllX509TrustManager implements X509TrustManager {
    private static final Logger LOG = LoggerFactory.getLogger(TrustAllX509TrustManager.class);

//    static {
//        LOG.info(">>>>>>>>>>>>>>>>>>>>>SERVER");
//        try {
//            SSLContext sc = SSLContext.getInstance("TLS");
//            sc.init(null, new TrustManager[]{new TrustAllX509TrustManager()}, new SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier((string, sslSession) -> true);
//        } catch (NoSuchAlgorithmException | KeyManagementException e) {
//            LOG.info(">>>>>>>>>>>>>>>>>>>>>SERVER1");
//            LOG.warn(e.getStackTrace().toString());
//            LOG.warn(e.getMessage());
//        }
//    }

//    public static void configureUnsecureSSLConnections() {
//        try {
//            SSLContext sc = SSLContext.getInstance("TLS");
//            sc.init(null, new TrustManager[]{new TrustAllX509TrustManager()}, new java.security.SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
//                public boolean verify(String string, SSLSession ssls) {
//                    return true;
//                }
//            });
//        } catch (NoSuchAlgorithmException | KeyManagementException e) {
//            LOG.warn(e.getMessage());
//        }
//    }

    @Override
    public boolean isClientTrusted(X509Certificate[] chain) {
        return true;
    }

    @Override
    public boolean isServerTrusted(X509Certificate[] chain) {
        return true;
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
    }
}