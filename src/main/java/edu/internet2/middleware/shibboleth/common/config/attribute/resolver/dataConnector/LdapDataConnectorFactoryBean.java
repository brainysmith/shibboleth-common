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

package edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.util.DatatypeHelper;

import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.LdapDataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.LdapPoolStrategy;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.TemplateEngine;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.LdapDataConnector.AUTHENTICATION_TYPE;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.LdapConfig.SearchScope;
import edu.vt.middleware.ldap.handler.BinarySearchResultHandler;
import edu.vt.middleware.ldap.handler.CaseChangeSearchResultHandler;
import edu.vt.middleware.ldap.handler.CaseChangeSearchResultHandler.CaseChange;
import edu.vt.middleware.ldap.handler.ConnectionHandler.ConnectionStrategy;
import edu.vt.middleware.ldap.handler.EntryDnSearchResultHandler;
import edu.vt.middleware.ldap.handler.FqdnSearchResultHandler;
import edu.vt.middleware.ldap.handler.MergeSearchResultHandler;
import edu.vt.middleware.ldap.handler.SearchResultHandler;
import edu.vt.middleware.ldap.pool.DefaultLdapFactory;
import edu.vt.middleware.ldap.pool.LdapValidator;

/**
 * Spring factory for creating {@link LdapDataConnector} beans.
 */
public class LdapDataConnectorFactoryBean extends BaseDataConnectorFactoryBean {

    /** Ldap pool strategy. */
    private LdapPoolStrategy ldapPoolStrategy;

    /** Ldap configuration. */
    private LdapConfig ldapConfig = new LdapConfig();

    /** Ldap connection strategy. */
    private ConnectionStrategy connStrategy;

    /** Ldap connection provider specific properties. */
    private Map<String, String> ldapProperties;

    /** Connection validator that performs compares. */
    private LdapValidator ldapValidator;

    /** Template engine used to construct filter queries. */
    private TemplateEngine templateEngine;

    /** LDAP query filter template. */
    private String filterTemplate;

    /** Name of the LDAP attributes to return. */
    private List<String> returnAttributes;

    /** Trust material used when connecting to the LDAP over SSL/TLS. */
    private X509Credential trustCredential;

    /** Client authentication material used when connecting to the LDAP over SSL/TLS. */
    private X509Credential connectionCredential;

    /** Whether to merge multiple results into a single set of attributes. */
    private boolean mergeResults;

    /** Whether a search returning no results should be considered an error. */
    private boolean noResultsIsError;
    
    /** Whether LDAP attribute names used as Shibboleth attribute IDs will be lowercased. */
    private boolean lowercaseAttributeNames;

    /** Whether results should be cached. */
    private CacheManager cacheManager;

    /** Maximum number of queries to keep in the cache. */
    private int maximumCachedElements;

    /** Length of time, in milliseconds, elements are cached. */
    private long cacheElementTtl;

    /** {@inheritDoc} */
    protected Object createInstance() throws Exception {
        List<SearchResultHandler> resultHandlers = new ArrayList<SearchResultHandler>();
        resultHandlers.add(new FqdnSearchResultHandler());
        resultHandlers.add(new EntryDnSearchResultHandler());
        if (mergeResults) {
            resultHandlers.add(new MergeSearchResultHandler());
        }
        if (lowercaseAttributeNames) {
            final CaseChangeSearchResultHandler srh = new CaseChangeSearchResultHandler();
            srh.setAttributeNameCaseChange(CaseChange.LOWER);
            resultHandlers.add(srh);
        }
        resultHandlers.add(new BinarySearchResultHandler());
        ldapConfig.setSearchResultHandlers(resultHandlers.toArray(new SearchResultHandler[resultHandlers.size()]));
        ldapConfig.getConnectionHandler().setConnectionStrategy(connStrategy);

        // set extra properties on the ldap config
        if (ldapProperties != null) {
            for (Map.Entry<String, String> entry : ldapProperties.entrySet()) {
                ldapConfig.setEnvironmentProperties(entry.getKey(), entry.getValue());
            }
        }

        SSLContext ctx = createSSLContext();
        if (ctx != null) {
            ldapConfig.setSslSocketFactory(ctx.getSocketFactory());
        }

        Cache resultsCache = null;
        if (cacheManager != null) {
            resultsCache = cacheManager.getCache(getPluginId());
            if (resultsCache == null) {
                long ttlInSeconds = cacheElementTtl / 1000;
                resultsCache = new Cache(
                    getPluginId(), maximumCachedElements, false, false, ttlInSeconds, ttlInSeconds);
                cacheManager.addCache(resultsCache);
            }
        }

        setupPoolStrategy();
        
        LdapDataConnector connector = new LdapDataConnector(ldapPoolStrategy, resultsCache);
        populateDataConnector(connector);
        connector.setNoResultsIsError(noResultsIsError);
        if (returnAttributes != null) {
            connector.setReturnAttributes(returnAttributes.toArray(new String[returnAttributes.size()]));
        }
        connector.registerTemplate(templateEngine, filterTemplate);

        return connector;
    }

    /**
     * Initializes the LDAP factory and validator for the selected pool strategy,
     * and initializes the pool.
     * 
     * @throws Exception    thrown if the pool cannot be initialized
     */
    protected void setupPoolStrategy() throws Exception {
        
        // initialize the pool
        DefaultLdapFactory ldapFactory = new DefaultLdapFactory(ldapConfig);
        if (ldapValidator != null) {
            ldapFactory.setLdapValidator(ldapValidator);
        }
        ldapPoolStrategy.setLdapFactory(ldapFactory);
        ldapPoolStrategy.initialize();
    }
    
    /**
     * Creates an SSLContext if either trust or key material was set.
     * 
     * @return the created SSL context or null if no trust or key material was provided
     * 
     * @throws Exception thrown if the SSLContext can not be created and initialized
     */
    protected SSLContext createSSLContext() throws Exception {
        // setup trust and key managers
        TrustManager[] sslTrustManagers = null;
        if (trustCredential != null) {
            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(null, null);
                for (X509Certificate c : trustCredential.getEntityCertificateChain()) {
                    keystore.setCertificateEntry("ldap_tls_trust_" + c.getSerialNumber(), c);
                }
                tmf.init(keystore);
                sslTrustManagers = tmf.getTrustManagers();
            } catch (GeneralSecurityException e) {
                logger.error("Error initializing trust managers", e);
            } catch (IOException e) {
                logger.error("Error initializing trust managers", e);
            }
        }

        KeyManager[] sslKeyManagers = null;
        if (connectionCredential != null) {
            try {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(null, null);
                keystore.setKeyEntry("ldap_tls_client_auth", connectionCredential.getPrivateKey(), "changeit"
                        .toCharArray(), connectionCredential.getEntityCertificateChain()
                        .toArray(new X509Certificate[0]));
                kmf.init(keystore, "changeit".toCharArray());
                sslKeyManagers = kmf.getKeyManagers();
            } catch (GeneralSecurityException e) {
                logger.error("Error initializing key managers", e);
            } catch (IOException e) {
                logger.error("Error initializing key managers", e);
            }
        }

        SSLContext ctx = null;
        if (sslTrustManagers != null || sslKeyManagers != null) {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(sslKeyManagers, sslTrustManagers, null);
        }
        return ctx;
    }

    /**
     * Gets the authentication type used when connecting to the directory.
     * 
     * @return authentication type used when connecting to the directory
     */
    public AUTHENTICATION_TYPE getAuthenticationType() {
        return AUTHENTICATION_TYPE.getAuthenticationTypeByName(ldapConfig.getAuthtype());
    }

    /**
     * Gets the base search DN.
     * 
     * @return the base search DN
     */
    public String getBaseDN() {
        return this.ldapConfig.getBaseDn();
    }

    /**
     * Gets the time to live, in milliseconds, for cache elements.
     * 
     * @return time to live, in milliseconds, for cache elements
     */
    public long getCacheElementTimeToLive() {
        return cacheElementTtl;
    }

    /**
     * Gets the manager for the results cache.
     * 
     * @return manager for the results cache
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * Gets the client authentication material used when connecting to the LDAP via SSL or TLS.
     * 
     * @return client authentication material used when connecting to the LDAP via SSL or TLS
     */
    public X509Credential getConnectionCredential() {
        return connectionCredential;
    }

    /**
     * Gets the LDAP query filter template.
     * 
     * @return LDAP query filter template
     */
    public String getFilterTemplate() {
        return filterTemplate;
    }

    /**
     * Gets the LDAP connection provider specific properties.
     * 
     * @return LDAP connection provider specific properties
     */
    public Map<String, String> getLdapProperties() {
        return ldapProperties;
    }

    /**
     * Gets the LDAP server's URL.
     * 
     * @return LDAP server's URL
     */
    public String getLdapUrl() {
        return ldapConfig.getLdapUrl();
    }

    /**
     * Gets the LDAP connection strategy.
     * 
     * @return connection strategy
     */
    public ConnectionStrategy getConnectionStrategy() {
        return connStrategy;
    }

    /**
     * Gets the maximum number of elements that will be cached.
     * 
     * @return maximum number of elements that will be cached
     */
    public int getMaximumCachedElements() {
        return maximumCachedElements;
    }

    /**
     * Gets the maximum number of results to return from a query.
     * 
     * @return maximum number of results to return from a query
     */
    public int getMaxResultSize() {
        return (int) ldapConfig.getCountLimit();
    }

    /** {@inheritDoc} */
    public Class<?> getObjectType() {
        return LdapDataConnector.class;
    }

    /**
     * Gets the ldap pool strategy.
     * 
     * @return ldap pool strategy
     */
    public LdapPoolStrategy getPoolStrategy() {
        return ldapPoolStrategy;
    }

    /**
     * Gets the pool validator.
     * 
     * @return pool validator
     */
    public LdapValidator getPoolValidator() {
        return ldapValidator;
    }

    /**
     * Gets the principal DN used to bind to the directory.
     * 
     * @return principal DN used to bind to the directory
     */
    public String getPrincipal() {
        return ldapConfig.getBindDn();
    }

    /**
     * Gets the credential of the principal DN used to bind to the directory.
     * 
     * @return credential of the principal DN used to bind to the directory
     */
    public String getPrincipalCredential() {
        return (String) ldapConfig.getBindCredential();
    }

    /**
     * Gets the attributes to return from a query.
     * 
     * @return attributes to return from a query
     */
    public List<String> getReturnAttributes() {
        return returnAttributes;
    }

    /**
     * Gets the search scope of a query.
     * 
     * @return search scope of a query
     */
    public SearchScope getSearchScope() {
        return ldapConfig.getSearchScope();
    }

    /**
     * Gets the maximum amount of time, in milliseconds, to wait for a search to complete.
     * 
     * @return maximum amount of time, in milliseconds, to wait for a search to complete
     */
    public int getSearchTimeLimit() {
        return ldapConfig.getTimeLimit();
    }

    /**
     * Gets the template engine used to construct query filters.
     * 
     * @return template engine used to construct query filters
     */
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    /**
     * Gets the trust material used when connecting to the LDAP via SSL or TLS.
     * 
     * @return trust material used when connecting to the LDAP via SSL or TLS
     */
    public X509Credential getTrustCredential() {
        return trustCredential;
    }

    /**
     * Gets whether to use StartTLS when connecting to the LDAP.
     * 
     * @return whether to use StartTLS when connecting to the LDAP
     */
    public boolean getUseStartTLS() {
        return ldapConfig.isTlsEnabled();
    }

    /**
     * Gets whether LDAP attribute names used as Shibboleth attribute IDs will be lowercased.
     * 
     * @return whether LDAP attribute names used as Shibboleth attribute IDs will be lowercased
     */
    public boolean isLowercaseAttributeNames() {
        return lowercaseAttributeNames;
    }

    /**
     * Gets whether to merge multiple results into a single result.
     * 
     * @return whether to merge multiple results into a single result
     */
    public boolean isMergeResults() {
        return mergeResults;
    }

    /**
     * Gets whether a query that returns no results is an error condition.
     * 
     * @return whether a query that returns no results is an error condition
     */
    public boolean isNoResultsIsError() {
        return noResultsIsError;
    }

    /**
     * Sets the authentication type used when connecting to the directory.
     * 
     * @param type authentication type used when connecting to the directory
     */
    public void setAuthenticationType(AUTHENTICATION_TYPE type) {
        ldapConfig.setAuthtype(type.getAuthTypeName());
    }
    
    /**
     * Sets the base search DN.
     * 
     * @param dn the base search DN
     */
    public void setBaseDN(String dn) {
        String trimmedDN = DatatypeHelper.safeTrimOrNullString(dn);
        if(trimmedDN != null){
            ldapConfig.setBaseDn(trimmedDN);
        }else{
            ldapConfig.setBaseDn("");
        }
    }
    
    /**
     * Sets the time to live, in milliseconds, for cache elements.
     * 
     * @param ttl time to live, in milliseconds, for cache elements
     */
    public void setCacheElementTimeToLive(long ttl) {
        cacheElementTtl = ttl;
    }

    /**
     * Sets the manager for the results cache.
     * 
     * @param manager manager for the results cache
     */
    public void setCacheManager(CacheManager manager) {
        cacheManager = manager;
    }

    /**
     * Sets the client authentication material used when connecting to the LDAP via SSL or TLS.
     * 
     * @param credential client authentication material used when connecting to the LDAP via SSL or TLS
     */
    public void setConnectionCredential(X509Credential credential) {
        connectionCredential = credential;
    }

    /**
     * Sets the LDAP query filter template.
     * 
     * @param template LDAP query filter template
     */
    public void setFilterTemplate(String template) {
        filterTemplate = DatatypeHelper.safeTrimOrNullString(template);
    }

    /**
     * Sets the LDAP connection provider specific properties.
     * 
     * @param properties LDAP connection provider specific properties
     */
    public void setLdapProperties(Map<String, String> properties) {
        ldapProperties = properties;
    }

    /**
     * Sets the LDAP server's URL.
     * 
     * @param url LDAP server's URL
     */
    public void setLdapUrl(String url) {
        ldapConfig.setLdapUrl(DatatypeHelper.safeTrimOrNullString(url));
    }

    /**
     * Sets the LDAP connection strategy.
     * 
     * @param strategy connection strategy
     */
    public void setConnectionStrategy(ConnectionStrategy strategy) {
        connStrategy = strategy;
    }

    /**
     * Sets whether LDAP attribute names used as Shibboleth attribute IDs will be lowercased.
     * 
     * @param lowercase whether LDAP attribute names used as Shibboleth attribute IDs will be lowercased
     */
    public void setLowercaseAttributeNames(boolean lowercase) {
        lowercaseAttributeNames = lowercase;
    }

    /**
     * Sets the maximum number of elements that will be cached.
     * 
     * @param max maximum number of elements that will be cached
     */
    public void setMaximumCachedElements(int max) {
        maximumCachedElements = max;
    }

    /**
     * Sets the maximum number of results to return from a query.
     * 
     * @param max maximum number of results to return from a query
     */
    public void setMaxResultSize(int max) {
        ldapConfig.setCountLimit(max);
    }

    /**
     * Sets whether to merge multiple results into a single result.
     * 
     * @param merge whether to merge multiple results into a single result
     */
    public void setMergeResults(boolean merge) {
        mergeResults = merge;
    }

    /**
     * Sets whether a query that returns no results is an error condition.
     * 
     * @param isError whether a query that returns no results is an error condition
     */
    public void setNoResultsIsError(boolean isError) {
        noResultsIsError = isError;
    }

    /**
     * Sets the ldap pool strategy.
     * 
     * @param strategy to use for pooling
     */
    public void setPoolStrategy(LdapPoolStrategy strategy) {
        ldapPoolStrategy = strategy;
    }

    /**
     * Sets the validator used to validate pool connections.
     * 
     * @param validator validator used to validate pool connections
     */
    public void setPoolValidator(LdapValidator validator) {
        ldapValidator = validator;
    }

    /**
     * Sets the principal DN used to bind to the directory.
     * 
     * @param principalName principal DN used to bind to the directory
     */
    public void setPrincipal(String principalName) {
        ldapConfig.setBindDn(DatatypeHelper.safeTrimOrNullString(principalName));
    }

    /**
     * Sets the credential of the principal DN used to bind to the directory.
     * 
     * @param credential credential of the principal DN used to bind to the directory
     */
    public void setPrincipalCredential(String credential) {
        ldapConfig.setBindCredential(DatatypeHelper.safeTrimOrNullString(credential));
    }

    /**
     * Sets the attributes to return from a query.
     * 
     * @param attributes attributes to return from a query
     */
    public void setReturnAttributes(List<String> attributes) {
        returnAttributes = attributes;
    }

    /**
     * Sets the search scope of a query.
     * 
     * @param scope search scope of a query
     */
    public void setSearchScope(SearchScope scope) {
        ldapConfig.setSearchScope(scope);
    }

    /**
     * Sets the maximum amount of time, in milliseconds, to wait for a search to complete.
     * 
     * @param timeLimit maximum amount of time, in milliseconds, to wait for a search to complete
     */
    public void setSearchTimeLimit(int timeLimit) {
        ldapConfig.setTimeLimit(timeLimit);
    }

    /**
     * Sets the template engine used to construct query filters.
     * 
     * @param engine template engine used to construct query filters
     */
    public void setTemplateEngine(TemplateEngine engine) {
        templateEngine = engine;
    }

    /**
     * Sets the trust material used when connecting to the LDAP via SSL or TLS.
     * 
     * @param credential trust material used when connecting to the LDAP via SSL or TLS
     */
    public void setTrustCredential(X509Credential credential) {
        trustCredential = credential;
    }

    /**
     * Sets whether to use StartTLS when connecting to the LDAP.
     * 
     * @param startTLS whether to use StartTLS when connecting to the LDAP
     */
    public void setUseStartTLS(boolean startTLS) {
        ldapConfig.setTls(startTLS);
    }
}
