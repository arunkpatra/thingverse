/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.thingverse.security.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class TlsUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TlsUtils.class);

    public static SSLContext getSslContext(String keyStoreFileName, String password, boolean trustAll) {
        //HttpsConnectionContext https = null;
        SSLContext sslContext = null;
        try {

            final KeyStore ks = KeyStore.getInstance("PKCS12");
            final InputStream keystore = TlsUtils.class.getClassLoader().getResourceAsStream(keyStoreFileName);
            if (keystore == null) {
                throw new RuntimeException("Keystore required!");
            }
            ks.load(keystore, password.toCharArray());
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(ks, password.toCharArray());
            tmf.init(ks);
            sslContext = SSLContext.getInstance("TLS");

            if (trustAll) {
                sslContext.init(null, trustAllCerts(), new SecureRandom());
            } else {
                sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            }
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.error("Exception while configuring HTTPS.", e);
        } catch (CertificateException | KeyStoreException | UnrecoverableKeyException | IOException e) {
            LOGGER.error("Exception while ", e);
        }

        return sslContext;
    }

    private static TrustManager[] trustAllCerts() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(
                            X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            X509Certificate[] certs, String authType) {
                    }
                }
        };
    }
}
