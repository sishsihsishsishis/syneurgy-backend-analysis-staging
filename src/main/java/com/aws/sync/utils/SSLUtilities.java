package com.aws.sync.utils;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class SSLUtilities {
    public static RestTemplate createRestTemplateWithDisabledSSL() {
        try {
            // 创建一个不验证证书链的TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // 安装全信任的TrustManager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // 创建一个忽略SSL验证的RestTemplate
            org.apache.http.conn.ssl.NoopHostnameVerifier noopHostnameVerifier = new org.apache.http.conn.ssl.NoopHostnameVerifier();
            org.apache.http.impl.client.CloseableHttpClient httpClient = org.apache.http.impl.client.HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(noopHostnameVerifier)
                    .build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(5000); // 连接超时，单位为毫秒
            factory.setReadTimeout(120000); // 读取超时，单位为毫秒
            return new RestTemplate(factory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RestTemplate with disabled SSL verification", e);
        }
    }
}
