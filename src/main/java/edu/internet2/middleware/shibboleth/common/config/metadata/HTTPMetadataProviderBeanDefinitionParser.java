/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.common.config.metadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.QName;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.ws.soap.client.http.TLSProtocolSocketFactory;
import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

/**
 * Spring bean definition parser for Shibboleth file backed url metadata provider definition.
 */
public class HTTPMetadataProviderBeanDefinitionParser extends AbstractReloadingMetadataProviderBeanDefinitionParser {

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(MetadataNamespaceHandler.NAMESPACE, "HTTPMetadataProvider");

    /** Class logger. */
    private Logger log = LoggerFactory.getLogger(HTTPMetadataProviderBeanDefinitionParser.class);

    /** {@inheritDoc} */
    protected Class getBeanClass(Element element) {
        return HTTPMetadataProvider.class;
    }

    /** {@inheritDoc} */
    protected void doParse(Element config, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String providerId = getProviderId(config);

        super.doParse(config, parserContext, builder);

        String metadataURL = DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "metadataURL"));
        URL metadataURI = null;
        try {
            metadataURI = new URL(metadataURL);
        } catch (MalformedURLException e) {
            throw new BeanCreationException("metadataURL attribute for metadata provider " + providerId
                    + " must be present and must contain a valid URL");
        }

        HttpClient httpClient = buildHttpClient(config, providerId, metadataURI);
        builder.addConstructorArgValue(httpClient);

        log.debug("Metadata provider '{}' metadata URL: {}", providerId, metadataURL);
        builder.addConstructorArgValue(metadataURL);
    }

    /**
     * Builds the HTTP client used to fetch metadata.
     * 
     * @param config the metadata provider configuration element
     * @param providerId the ID of the metadata provider
     * @param metadataURL the URL from which metadata will be fetched
     * 
     * @return the constructed HTTP client
     */
    protected HttpClient buildHttpClient(Element config, String providerId, URL metadataURL) {
        HttpClientBuilder builder = new HttpClientBuilder();

        int requestTimeout = 5000;
        if (config.hasAttributeNS(null, "requestTimeout")) {
            requestTimeout = (int) SpringConfigurationUtils.parseDurationToMillis(
                    "'requestTimeout' on metadata provider " + providerId,
                    config.getAttributeNS(null, "requestTimeout"), 0);
        }
        log.debug("Metadata provider '{}' HTTP request timeout: {}ms", providerId, requestTimeout);
        builder.setConnectionTimeout(requestTimeout);

        if (metadataURL.getProtocol().equalsIgnoreCase("https")) {
            boolean disregardSslCertificate = false;
            if (config.hasAttributeNS(null, "disregardSslCertificate")) {
                disregardSslCertificate = XMLHelper.getAttributeValueAsBoolean(config.getAttributeNodeNS(null,
                        "disregardSslCertificate"));
            }

            log.debug("Metadata provider '{}' disregards server SSL certificate: {}", providerId,
                    disregardSslCertificate);
            if (disregardSslCertificate) {
                builder.setHttpsProtocolSocketFactory(new TLSProtocolSocketFactory(null, buildNoTrustTrustManager()));
            }
        }

        setHttpProxySettings(builder, config, providerId);

        HttpClient httpClient = builder.buildClient();
        setHttpBasicAuthSettings(httpClient, config, providerId, metadataURL);

        return httpClient;
    }

    /**
     * Builds a {@link javax.net.ssl.X509TrustManager} which bypasses all X.509 validation steps.
     * 
     * @return the trustless trust manager
     */
    protected X509TrustManager buildNoTrustTrustManager() {
        X509TrustManager noTrustManager = new X509TrustManager() {

            /** {@inheritDoc} */
            public void checkClientTrusted(X509Certificate[] certs, String auth) {
            }

            /** {@inheritDoc} */
            public void checkServerTrusted(X509Certificate[] certs, String auth) {
            }

            /** {@inheritDoc} */
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }
        };

        return noTrustManager;
    }

    /**
     * Sets the HTTP proxy properties, if any, for the HTTP client used to fetch metadata.
     * 
     * @param builder the HTTP client builder
     * @param config the metadata provider configuration
     * @param providerId the ID of the metadata provider
     */
    protected void setHttpProxySettings(HttpClientBuilder builder, Element config, String providerId) {
        String proxyHost = DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "proxyHost"));
        if (proxyHost == null) {
            return;
        }
        log.debug("Metadata provider '{}' HTTP proxy host: {}", providerId, proxyHost);
        builder.setProxyHost(proxyHost);

        if (config.hasAttributeNS(null, "proxyPort")) {
            int proxyPort = Integer.parseInt(config.getAttributeNS(null, "proxyPort"));
            log.debug("Metadata provider '{}' HTTP proxy port: ", providerId, proxyPort);
            builder.setProxyPort(proxyPort);
        }

        String proxyUser = DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "proxyUser"));
        if (proxyUser != null) {
            log.debug("Metadata provider '{}' HTTP proxy username: ", providerId, proxyUser);
            builder.setProxyUsername(proxyUser);
            log.debug("Metadata provider '{}' HTTP proxy password not shown", providerId);
            builder.setProxyPassword(DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "proxyPassword")));
        }
    }

    /**
     * Sets the basic authentication properties, if any, for the HTTP client used to fetch metadata.
     * 
     * @param httpClient the HTTP client
     * @param config the metadata provider configuration
     * @param providerId the ID of the metadata provider
     * @param metadataURL the URL from which metadata will be fetched
     */
    protected void setHttpBasicAuthSettings(HttpClient httpClient, Element config, String providerId, URL metadataURL) {
        String authUser = DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "basicAuthUser"));
        if (authUser == null) {
            return;
        }
        log.debug("Metadata provider '{}' HTTP Basic Auth username: {}", providerId, authUser);

        String authPassword = DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "basicAuthPassword"));
        log.debug("Metadata provider '{}' HTTP Basic Auth password not show", providerId);

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(authUser, authPassword);
        AuthScope authScope = new AuthScope(metadataURL.getHost(), metadataURL.getPort());
        httpClient.getState().setCredentials(authScope, credentials);
    }
}