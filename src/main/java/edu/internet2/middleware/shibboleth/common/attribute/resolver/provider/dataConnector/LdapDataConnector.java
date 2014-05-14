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

package edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.TemplateEngine.CharacterEscapingStrategy;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapBeanProvider;

/**
 * <code>LdapDataConnector</code> provides a plugin to retrieve attributes from an LDAP.
 */
public class LdapDataConnector extends BaseDataConnector {

    /** Authentication type values. */
    public static enum AUTHENTICATION_TYPE {
        /** Anonymous authentication type. */
        ANONYMOUS("none"),
        /** Simple authentication type. */
        SIMPLE("simple"),
        /** Strong authentication type. */
        STRONG("strong"),
        /** External authentication type. */
        EXTERNAL("EXTERNAL"),
        /** Digest MD5 authentication type. */
        DIGEST_MD5("DIGEST-MD5"),
        /** Cram MD5 authentication type. */
        CRAM_MD5("CRAM-MD5"),
        /** Kerberos authentication type. */
        GSSAPI("GSSAPI");

        /** auth type name passed to LdapConfig. */
        private String authTypeName;

        /**
         * Default constructor.
         * 
         * @param s auth type name
         */
        private AUTHENTICATION_TYPE(String s) {
            authTypeName = s;
        }

        /**
         * This returns the auth type name needed by the LdapConfig.
         * 
         * @return auth type name
         */
        public String getAuthTypeName() {
            return authTypeName;
        }

        /**
         * Returns the corresponding AUTHENTICATION_TYPE for the supplied auth type name.
         * 
         * @param s auth type name to lookup
         * @return AUTHENTICATION_TYPE
         */
        public static AUTHENTICATION_TYPE getAuthenticationTypeByName(String s) {
            AUTHENTICATION_TYPE type = null;
            if (AUTHENTICATION_TYPE.ANONYMOUS.getAuthTypeName().equals(s)) {
                type = AUTHENTICATION_TYPE.ANONYMOUS;
            } else if (AUTHENTICATION_TYPE.SIMPLE.getAuthTypeName().equals(s)) {
                type = AUTHENTICATION_TYPE.SIMPLE;
            } else if (AUTHENTICATION_TYPE.STRONG.getAuthTypeName().equals(s)) {
                type = AUTHENTICATION_TYPE.STRONG;
            } else if (AUTHENTICATION_TYPE.EXTERNAL.getAuthTypeName().equals(s)) {
                type = AUTHENTICATION_TYPE.EXTERNAL;
            } else if (AUTHENTICATION_TYPE.DIGEST_MD5.getAuthTypeName().equals(s)) {
                type = AUTHENTICATION_TYPE.DIGEST_MD5;
            } else if (AUTHENTICATION_TYPE.CRAM_MD5.getAuthTypeName().equals(s)) {
                type = AUTHENTICATION_TYPE.CRAM_MD5;
            } else if (AUTHENTICATION_TYPE.GSSAPI.getAuthTypeName().equals(s)) {
                type = AUTHENTICATION_TYPE.GSSAPI;
            }
            return type;
        }
    };

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(LdapDataConnector.class);

    /** Ldap pool strategy. */
    private LdapPoolStrategy ldapPool;

    /** Template engine used to change filter template into actual filter. */
    private TemplateEngine filterCreator;

    /** Name the filter template is registered under within the template engine. */
    private String filterTemplateName;

    /** Template that produces the query to use. */
    private String filterTemplate;

    /** Attributes to return from ldap searches. */
    private String[] returnAttributes;

    /** Whether an empty result set is an error. */
    private boolean noResultsIsError;
    
    /** Cache of past search results. */
    private Cache resultsCache;

    /** Filter value escaping strategy. */
    private final LDAPValueEscapingStrategy escapingStrategy;

    /**
     * This creates a new LDAP data connector with the supplied properties.
     * 
     * @param pool LDAP connection pooling strategy
     * @param cache cached used to cache search results, or null if results should not be cached
     */
    public LdapDataConnector(LdapPoolStrategy pool, Cache cache) {
        super();
        ldapPool = pool;

        resultsCache = cache;

        escapingStrategy = new LDAPValueEscapingStrategy();
    }

    /**
     * This sets the underlying template engine and registers the supplied template.
     * 
     * @param engine engine used to fill in search filter templates
     * @param template search filter template
     */
    public void registerTemplate(TemplateEngine engine, String template) {
        if (getId() == null) {
            throw new IllegalStateException("Template cannot be registered until plugin id has been set");
        }
        filterCreator = engine;
        filterTemplate = template;
        filterTemplateName = "shibboleth.resolver.dc." + getId();
        filterCreator.registerTemplate(filterTemplateName, filterTemplate);        
    }

    /** Removes all entries from the cache if results are being cached. */
    protected void clearCache() {
        if (isCacheResults()) {
            resultsCache.removeAll();
        }
    }

    /**
     * This returns whether this connector will cache search results.
     * 
     * @return true if results are being cached
     */
    public boolean isCacheResults() {
        return resultsCache != null;
    }

    /**
     * This returns whether this connector will throw an exception if no search results are found.
     * 
     * @return true if searches which return no results are considered an error
     */
    public boolean isNoResultsIsError() {
        return noResultsIsError;
    }

    /**
     * This sets whether this connector will throw an exception if no search results are found.
     * 
     * @param isError true if searches which return no results are considered an error, false otherwise
     */
    public void setNoResultsIsError(boolean isError) {
        noResultsIsError = isError;
    }
    
    /**
     * Gets the engine used to evaluate the query template.
     * 
     * @return engine used to evaluate the query template
     */
    public TemplateEngine getTemplateEngine() {
        return filterCreator;
    }

    /**
     * Gets the template used to create queries.
     * 
     * @return template used to create queries
     */
    public String getFilterTemplate() {
        return filterTemplate;
    }

    /**
     * This returns the ldap pool strategy this connector is using.
     * 
     * @return ldap pool strategy
     */
    public LdapPoolStrategy getLdapPool() {
        return ldapPool;
    }

    /**
     * This returns the attributes that all searches will request from the ldap.
     * 
     * @return <code>String[]</code>
     */
    public String[] getReturnAttributes() {
        return returnAttributes;
    }

    /**
     * This sets the attributes that all searches will request from the ldap. This method will remove any cached
     * results.
     * 
     * @see #clearCache()
     * 
     * @param attributes <code>String[]</code>
     */
    public void setReturnAttributes(String[] attributes) {
        returnAttributes = attributes;
    }

    /**
     * This sets the attributes that all searches will request from the ldap. s should be a comma delimited string.
     * 
     * @param s <code>String[]</code> comma delimited returnAttributes
     */
    public void setReturnAttributes(String s) {
        StringTokenizer st = new StringTokenizer(s, ",");
        String[] ra = new String[st.countTokens()];
        for (int count = 0; count < st.countTokens(); count++) {
            ra[count] = st.nextToken();
        }
        setReturnAttributes(ra);
    }

    /** {@inheritDoc} */
    public void validate() throws AttributeResolutionException {
        Ldap ldap = null;
        try {
            ldap = ldapPool.checkOut();
            if(ldap == null){
                log.error("Unable to retrieve an LDAP connection");
                throw new AttributeResolutionException("Unable to retrieve LDAP connection");
            }
            if (!ldap.connect()) {
                throw new NamingException();
            }
        } catch (NamingException e) {
            log.error("An error occured when attempting to search the LDAP: " + ldap.getLdapConfig().getEnvironment(),
                    e);
            throw new AttributeResolutionException("An error occurred when attempting to search the LDAP", e);
        } catch (Exception e) {
            log.error("Could not retrieve Ldap object from pool", e);
            throw new AttributeResolutionException(
                    "An error occurred when attempting to retrieve a LDAP connection from the pool", e);
        } finally {
            if (ldap != null) {
                try {
                    ldapPool.checkIn(ldap);
                } catch (Exception e) {
                    log.error("Could not return Ldap object back to pool", e);
                }
            }
        }
    }

    /** {@inheritDoc} */
    public Map<String, BaseAttribute> resolve(ShibbolethResolutionContext resolutionContext)
            throws AttributeResolutionException {
        String searchFilter = filterCreator.createStatement(filterTemplateName, resolutionContext, getDependencyIds(),
                escapingStrategy);
        searchFilter = searchFilter.trim();
        log.debug("Search filter: {}", searchFilter);

        // attempt to get attributes from the cache
        Map<String, BaseAttribute> attributes = retrieveAttributesFromCache(searchFilter);

        // results not found in the cache
        if (attributes == null) {
            Iterator<SearchResult> results = searchLdap(searchFilter);

            if (noResultsIsError && !results.hasNext()) {
                log.debug("LDAP data connector " + getId()
                        + " - No result returned and connector configured to treat this as an error.");
                throw new AttributeResolutionException("No LDAP entry found for "
                        + resolutionContext.getAttributeRequestContext().getPrincipalName());
            }

            // build resolved attributes from LDAP attributes and cache the result
            attributes = buildBaseAttributes(results);
            cacheResult(searchFilter, attributes);
        }

        return attributes;
    }

    /**
     * This retrieves any cached attributes for the supplied resolution context. Returns null if nothing is cached.
     * 
     * @param searchFilter the search filter the produced the attributes
     * 
     * @return <code>Map</code> of attributes IDs to attributes
     */
    protected Map<String, BaseAttribute> retrieveAttributesFromCache(String searchFilter) {
        if (!isCacheResults()) {
            return null;
        }

        log.debug("LDAP data connector {} - Checking cache for search results", getId());
        Element cachedResult = resultsCache.get(searchFilter);
        if (cachedResult != null && !cachedResult.isExpired()) {
            log.debug("LDAP data connector {} - Returning attributes from cache", getId());
            return (Map<String, BaseAttribute>) cachedResult.getObjectValue();
        }

        log.debug("LDAP data connector {} - No results cached for search filter '{}'", getId(), searchFilter);
        return null;
    }

    /**
     * This searches the LDAP with the supplied filter.
     * 
     * @param searchFilter <code>String</code> the searchFilter that produced the attributes
     * @return <code>Iterator</code> of search results
     * @throws AttributeResolutionException if an error occurs performing the search
     */
    protected Iterator<SearchResult> searchLdap(String searchFilter) throws AttributeResolutionException {
        log.debug("LDAP data connector {} - Retrieving attributes from LDAP", getId());

        Ldap ldap = null;
        try {
            ldap = ldapPool.checkOut();
            return ldap.search(new SearchFilter(searchFilter), returnAttributes);
        } catch (NamingException e) {
            log.debug("LDAP data connector " + getId() + " - An error occured when attempting to search the LDAP: "
                    + ldap.getLdapConfig().getEnvironment(), e);
            throw new AttributeResolutionException("An error occurred when attempting to search the LDAP");
        } catch (Exception e) {
            log.debug("LDAP data connector " + getId() + " - Could not perform ldap search", e);
            throw new AttributeResolutionException("An error occurred when attempting to perform a LDAP search");
        } finally {
            if (ldap != null) {
                try {
                    ldapPool.checkIn(ldap);
                } catch (Exception e) {
                    log.error("LDAP data connector " + getId() + " - Could not return Ldap object back to pool", e);
                }
            }
        }
    }

    /**
     * This returns a map of attribute ids to attributes from the supplied search results.
     * 
     * @param results <code>Iterator</code> of LDAP search results
     * @return <code>Map</code> of attribute ids to attributes
     * @throws AttributeResolutionException if an error occurs parsing attribute results
     */
    protected Map<String, BaseAttribute> buildBaseAttributes(Iterator<SearchResult> results)
            throws AttributeResolutionException {

        Map<String, BaseAttribute> attributes = new HashMap<String, BaseAttribute>();

        if (!results.hasNext()) {
            return attributes;
        }

        SearchResult sr = results.next();
        LdapAttributes ldapAttrs = null;
        try {
            ldapAttrs = LdapBeanProvider.getLdapBeanFactory().newLdapAttributes();
            ldapAttrs.addAttributes(sr.getAttributes());
        } catch (NamingException e) {
            log.debug("LDAP data connector " + getId() + " - Error parsing LDAP attributes", e);
            throw new AttributeResolutionException("Error parsing LDAP attributes", e);
        }

        for (LdapAttribute ldapAttr : ldapAttrs.getAttributes()) {
            log.debug("LDAP data connector {} - Found the following attribute: {}", getId(), ldapAttr);
            BaseAttribute attribute = attributes.get(ldapAttr.getName());
            if (attribute == null) {
                attribute = new BasicAttribute<String>(ldapAttr.getName());
                attributes.put(ldapAttr.getName(), attribute);
            }

            Set<Object> values = ldapAttr.getValues();
            if (values != null && !values.isEmpty()) {
                for (Object value : values) {
                    if (value instanceof String) {
                        String s = (String) value;
                        if (!DatatypeHelper.isEmpty(s)) {
                            attribute.getValues().add(DatatypeHelper.safeTrimOrNullString(s));
                        }
                    } else {
                        log.debug("LDAP data connector {} - Attribute {} contained a value that is not of type String",
                                getId(), ldapAttr.getName());
                        attribute.getValues().add(value);
                    }
                }
            }
        }

        return attributes;
    }

    /**
     * This stores the supplied attributes in the cache.
     * 
     * @param searchFilter the searchFilter that produced the attributes
     * @param attributes <code>Map</code> of attribute IDs to attributes
     */
    protected void cacheResult(String searchFilter, Map<String, BaseAttribute> attributes) {
        if (!isCacheResults()) {
            return;
        }

        log.debug("LDAP data connector {} - Caching attributes from search '{}'", getId(), searchFilter);
        resultsCache.put(new Element(searchFilter, attributes));
    }

    /**
     * Escapes values that will be included within an LDAP filter.
     */
    protected class LDAPValueEscapingStrategy implements CharacterEscapingStrategy {

        /** {@inheritDoc} */
        public String escape(String value) {
            return value.replace("*", "\\*").replace("(", "\\(").replace(")", "\\)").replace("\\", "\\");
        }
    }
}
