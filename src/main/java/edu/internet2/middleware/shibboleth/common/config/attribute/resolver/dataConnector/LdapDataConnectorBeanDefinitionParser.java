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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.LdapPoolEmptyStrategy;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.LdapPoolVTStrategy;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.LdapDataConnector.AUTHENTICATION_TYPE;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.LdapConfig.SearchScope;
import edu.vt.middleware.ldap.handler.ConnectionHandler.ConnectionStrategy;
import edu.vt.middleware.ldap.pool.CompareLdapValidator;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;
import edu.vt.middleware.ldap.pool.LdapValidator;

/** Spring bean definition parser for configuring an LDAP data connector. */
public class LdapDataConnectorBeanDefinitionParser extends BaseDataConnectorBeanDefinitionParser {

    /** LDAP data connector type name. */
    public static final QName TYPE_NAME = new QName(DataConnectorNamespaceHandler.NAMESPACE, "LDAPDirectory");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(LdapDataConnectorBeanDefinitionParser.class);

    /** {@inheritDoc} */
    protected Class<?> getBeanClass(Element element) {
        return LdapDataConnectorFactoryBean.class;
    }

    /** {@inheritDoc} */
    protected void doParse(String pluginId, Element pluginConfig, Map<QName, List<Element>> pluginConfigChildren,
            BeanDefinitionBuilder pluginBuilder, ParserContext parserContext) {
        super.doParse(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

        processBasicConnectionConfig(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);
        processSecurityConfig(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);
        processResultHandlingConfig(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

        Map<String, String> ldapProperties =
                processLDAPProperties(pluginConfigChildren.get(new QName(DataConnectorNamespaceHandler.NAMESPACE,
                        "LDAPProperty")));
        if (ldapProperties != null) {
            log.debug("Data connector {} LDAP properties: {}", pluginId, ldapProperties);
            pluginBuilder.addPropertyValue("ldapProperties", ldapProperties);
        }

        processPoolingConfig(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

        processCacheConfig(pluginId, pluginConfig, pluginBuilder);
    }

    /**
     * Process the basic LDAP connection configuration for the LDAP data connector.
     * 
     * @param pluginId ID of the LDAP plugin
     * @param pluginConfig LDAP plugin configuration element
     * @param pluginConfigChildren child elements of the plugin
     * @param pluginBuilder plugin builder
     * @param parserContext current parsing context
     */
    protected void processBasicConnectionConfig(String pluginId, Element pluginConfig,
            Map<QName, List<Element>> pluginConfigChildren, BeanDefinitionBuilder pluginBuilder,
            ParserContext parserContext) {

        String ldapURL = pluginConfig.getAttributeNS(null, "ldapURL");
        log.debug("Data connector {} LDAP URL: {}", pluginId, ldapURL);
        pluginBuilder.addPropertyValue("ldapUrl", ldapURL);

        ConnectionStrategy connStrategy = ConnectionStrategy.ACTIVE_PASSIVE;
        if (pluginConfig.hasAttributeNS(null, "connectionStrategy")) {
            connStrategy = ConnectionStrategy.valueOf(pluginConfig.getAttributeNS(null, "connectionStrategy"));
        }
        log.debug("Data connector {} connection strategy: {}", pluginId, connStrategy);
        pluginBuilder.addPropertyValue("connectionStrategy", connStrategy);

        if (pluginConfig.hasAttributeNS(null, "baseDN")) {
            String baseDN = pluginConfig.getAttributeNS(null, "baseDN");
            log.debug("Data connector {} base DN: {}", pluginId, baseDN);
            pluginBuilder.addPropertyValue("baseDN", baseDN);
        }

        AUTHENTICATION_TYPE authnType = AUTHENTICATION_TYPE.SIMPLE;
        if (pluginConfig.hasAttributeNS(null, "authenticationType")) {
            authnType = AUTHENTICATION_TYPE.valueOf(pluginConfig.getAttributeNS(null, "authenticationType"));
        }
        log.debug("Data connector {} authentication type: {}", pluginId, authnType);
        pluginBuilder.addPropertyValue("authenticationType", authnType);

        String principal = pluginConfig.getAttributeNS(null, "principal");
        log.debug("Data connector {} principal: {}", pluginId, principal);
        pluginBuilder.addPropertyValue("principal", principal);

        String credential = pluginConfig.getAttributeNS(null, "principalCredential");
        pluginBuilder.addPropertyValue("principalCredential", credential);

        String templateEngineRef = pluginConfig.getAttributeNS(null, "templateEngine");
        pluginBuilder.addPropertyReference("templateEngine", templateEngineRef);

        String filterTemplate =
                pluginConfigChildren.get(new QName(DataConnectorNamespaceHandler.NAMESPACE, "FilterTemplate")).get(0)
                        .getTextContent();
        filterTemplate = DatatypeHelper.safeTrimOrNullString(filterTemplate);
        log.debug("Data connector {} LDAP filter template: {}", pluginId, filterTemplate);
        pluginBuilder.addPropertyValue("filterTemplate", filterTemplate);

        SearchScope searchScope = SearchScope.SUBTREE;
        if (pluginConfig.hasAttributeNS(null, "searchScope")) {
            searchScope = SearchScope.valueOf(pluginConfig.getAttributeNS(null, "searchScope"));
        }
        log.debug("Data connector {} search scope: {}", pluginId, searchScope);
        pluginBuilder.addPropertyValue("searchScope", searchScope);

        QName returnAttributesName = new QName(DataConnectorNamespaceHandler.NAMESPACE, "ReturnAttributes");
        if (pluginConfigChildren.containsKey(returnAttributesName)) {
            List<String> returnAttributes =
                    XMLHelper.getElementContentAsList(pluginConfigChildren.get(returnAttributesName).get(0));
            log.debug("Data connector {} return attributes: {}", pluginId, returnAttributes);
            pluginBuilder.addPropertyValue("returnAttributes", returnAttributes);
        }
    }

    /**
     * Process the LDAP connection security configuration for the LDAP data connector.
     * 
     * @param pluginId ID of the LDAP plugin
     * @param pluginConfig LDAP plugin configuration element
     * @param pluginConfigChildren child elements of the plugin
     * @param pluginBuilder plugin builder
     * @param parserContext current parsing context
     */
    protected void processSecurityConfig(String pluginId, Element pluginConfig,
            Map<QName, List<Element>> pluginConfigChildren, BeanDefinitionBuilder pluginBuilder,
            ParserContext parserContext) {
        RuntimeBeanReference trustCredential =
                processCredential(pluginConfigChildren.get(new QName(DataConnectorNamespaceHandler.NAMESPACE,
                        "StartTLSTrustCredential")), parserContext);
        if (trustCredential != null) {
            log.debug("Data connector {} using provided SSL/TLS trust material", pluginId);
            pluginBuilder.addPropertyValue("trustCredential", trustCredential);
        }

        RuntimeBeanReference connectionCredential =
                processCredential(pluginConfigChildren.get(new QName(DataConnectorNamespaceHandler.NAMESPACE,
                        "StartTLSAuthenticationCredential")), parserContext);
        if (connectionCredential != null) {
            log.debug("Data connector {} using provided SSL/TLS client authentication material", pluginId);
            pluginBuilder.addPropertyValue("connectionCredential", connectionCredential);
        }

        boolean useStartTLS = false;
        if (pluginConfig.hasAttributeNS(null, "useStartTLS")) {
            useStartTLS = XMLHelper.getAttributeValueAsBoolean(pluginConfig.getAttributeNodeNS(null, "useStartTLS"));
        }
        log.debug("Data connector {} use startTLS: {}", pluginId, useStartTLS);
        pluginBuilder.addPropertyValue("useStartTLS", useStartTLS);
    }

    /**
     * Process the LDAP result handling configuration for the LDAP data connector.
     * 
     * @param pluginId ID of the LDAP plugin
     * @param pluginConfig LDAP plugin configuration element
     * @param pluginConfigChildren child elements of the plugin
     * @param pluginBuilder plugin builder
     * @param parserContext current parsing context
     */
    protected void processResultHandlingConfig(String pluginId, Element pluginConfig,
            Map<QName, List<Element>> pluginConfigChildren, BeanDefinitionBuilder pluginBuilder,
            ParserContext parserContext) {
        int searchTimeLimit = 3000;
        if (pluginConfig.hasAttributeNS(null, "searchTimeLimit")) {
            searchTimeLimit =
                    (int) SpringConfigurationUtils.parseDurationToMillis("'searchTimeLimit' on data connector "
                            + pluginId, pluginConfig.getAttributeNS(null, "searchTimeLimit"), 0);
        }
        log.debug("Data connector {} search timeout: {}ms", pluginId, searchTimeLimit);
        pluginBuilder.addPropertyValue("searchTimeLimit", searchTimeLimit);

        int maxResultSize = 1;
        if (pluginConfig.hasAttributeNS(null, "maxResultSize")) {
            maxResultSize = Integer.parseInt(pluginConfig.getAttributeNS(null, "maxResultSize"));
        }
        log.debug("Data connector {} max search result size: {}", pluginId, maxResultSize);
        pluginBuilder.addPropertyValue("maxResultSize", maxResultSize);

        boolean mergeResults = false;
        if (pluginConfig.hasAttributeNS(null, "mergeResults")) {
            mergeResults = XMLHelper.getAttributeValueAsBoolean(pluginConfig.getAttributeNodeNS(null, "mergeResults"));
        }
        log.debug("Data connector {} merge results: {}", pluginId, mergeResults);
        pluginBuilder.addPropertyValue("mergeResults", mergeResults);

        boolean noResultsIsError = false;
        if (pluginConfig.hasAttributeNS(null, "noResultIsError")) {
            noResultsIsError =
                    XMLHelper.getAttributeValueAsBoolean(pluginConfig.getAttributeNodeNS(null, "noResultIsError"));
        }
        log.debug("Data connector {} no results is error: {}", pluginId, noResultsIsError);
        pluginBuilder.addPropertyValue("noResultsIsError", noResultsIsError);

        boolean lowercaseAttributeNames = false;
        if (pluginConfig.hasAttributeNS(null, "lowercaseAttributeNames")) {
            lowercaseAttributeNames =
                    XMLHelper.getAttributeValueAsBoolean(pluginConfig.getAttributeNodeNS(null,
                            "lowercaseAttributeNames"));
        }
        log.debug("Data connector {} will lower case attribute IDs: {}", pluginId, lowercaseAttributeNames);
        pluginBuilder.addPropertyValue("lowercaseAttributeNames", lowercaseAttributeNames);
    }

    /**
     * Process the pooling configuration for the LDAP data connector.
     * 
     * @param pluginId ID of the LDAP plugin
     * @param pluginConfig LDAP plugin configuration element
     * @param pluginConfigChildren child elements of the plugin
     * @param pluginBuilder plugin builder
     * @param parserContext current parsing context
     */
    protected void processPoolingConfig(String pluginId, Element pluginConfig,
            Map<QName, List<Element>> pluginConfigChildren, BeanDefinitionBuilder pluginBuilder,
            ParserContext parserContext) {

        List<Element> poolConfigElems =
                pluginConfigChildren.get(new QName(DataConnectorNamespaceHandler.NAMESPACE, "ConnectionPool"));
        if (poolConfigElems == null || poolConfigElems.size() == 0) {
            log.debug("Data connector {} is pooling connections: {}", pluginId, false);
            pluginBuilder.addPropertyValue("poolStrategy", new LdapPoolEmptyStrategy());
            return;
        }

        Element poolConfigElem = poolConfigElems.get(0);

        LdapPoolConfig ldapPoolConfig = new LdapPoolConfig();
        LdapPoolVTStrategy ldapPoolStrategy = new LdapPoolVTStrategy();
        ldapPoolStrategy.setLdapPoolConfig(ldapPoolConfig);
        log.debug("Data connector {} is pooling connections: {}", pluginId, true);
        pluginBuilder.addPropertyValue("poolStrategy", ldapPoolStrategy);

        int poolMinSize = 0;
        if (pluginConfig.hasAttributeNS(null, "poolInitialSize")) {
            poolMinSize = Integer.parseInt(pluginConfig.getAttributeNS(null, "poolInitialSize"));
            log.warn("Data connector {} using deprecated attribute poolInitialSize on <DataConnector> use minPoolSize on child <PoolConfig> instead");
        } else if (poolConfigElem != null && poolConfigElem.hasAttributeNS(null, "minPoolSize")) {
            poolMinSize = Integer.parseInt(poolConfigElem.getAttributeNS(null, "minPoolSize"));
        }
        log.debug("Data connector {} pool minimum connections: {}", pluginId, poolMinSize);
        ldapPoolConfig.setMinPoolSize(poolMinSize);

        int poolMaxSize = 3;
        if (pluginConfig.hasAttributeNS(null, "poolMaxIdleSize")) {
            poolMaxSize = Integer.parseInt(pluginConfig.getAttributeNS(null, "poolMaxIdleSize"));
            log.warn("Data connector {} using deprecated attribute poolMaxIdleSize on <DataConnector> use maxPoolSize on child <PoolConfig> instead");
        } else if (poolConfigElem != null && poolConfigElem.hasAttributeNS(null, "maxPoolSize")) {
            poolMaxSize = Integer.parseInt(poolConfigElem.getAttributeNS(null, "maxPoolSize"));
        }
        log.debug("Data connector {} pool maximum connections: {}", pluginId, poolMaxSize);
        ldapPoolConfig.setMaxPoolSize(poolMaxSize);

        boolean blockWhenEmpty = true;
        if (poolConfigElem != null && poolConfigElem.hasAttributeNS(null, "blockWhenEmpty")) {
            blockWhenEmpty =
                    XMLHelper.getAttributeValueAsBoolean(poolConfigElem.getAttributeNodeNS(null, "blockWhenEmpty"));
        }
        log.debug("Data connector {} pool block when empty: {}", pluginId, blockWhenEmpty);
        ldapPoolStrategy.setBlockWhenEmpty(blockWhenEmpty);

        int blockWaitTime = 0;
        if (poolConfigElem != null && poolConfigElem.hasAttributeNS(null, "blockWaitTime")) {
            blockWaitTime =
                    (int) SpringConfigurationUtils.parseDurationToMillis("blockWaitTime",
                            poolConfigElem.getAttributeNS(null, "blockWaitTime"), 0);
        }
        if(blockWaitTime == 0){
        log.debug("Data connector {} pool block wait time: indefintely", pluginId);
        }else{
            log.debug("Data connector {} pool block wait time: {}ms", pluginId, blockWaitTime);
        }
        ldapPoolStrategy.setBlockWaitTime(blockWaitTime);

        boolean poolValidatePeriodically = false;
        if (poolConfigElem != null && poolConfigElem.hasAttributeNS(null, "validatePeriodically")) {
            poolValidatePeriodically =
                    XMLHelper.getAttributeValueAsBoolean(poolConfigElem
                            .getAttributeNodeNS(null, "validatePeriodically"));
        }
        log.debug("Data connector {} pool validate periodically: {}", pluginId, poolValidatePeriodically);
        ldapPoolConfig.setValidatePeriodically(poolValidatePeriodically);

        int poolValidateTimerPeriod = 1800000;
        if (poolConfigElem != null && poolConfigElem.hasAttributeNS(null, "validateTimerPeriod")) {
            poolValidateTimerPeriod =
                    (int) SpringConfigurationUtils.parseDurationToMillis("validateTimerPeriod",
                            poolConfigElem.getAttributeNS(null, "validateTimerPeriod"), 0);
        }
        log.debug("Data connector {} pool validate timer period: {}ms", pluginId, poolValidateTimerPeriod);
        ldapPoolConfig.setValidateTimerPeriod(poolValidateTimerPeriod);

        String poolValidateDn = "";
        if (poolConfigElem != null && poolConfigElem.hasAttributeNS(null, "validateDN")) {
            poolValidateDn = poolConfigElem.getAttributeNS(null, "validateDN");
        }
        String poolValidateFilter = "(objectClass=*)";
        if (poolConfigElem != null && poolConfigElem.hasAttributeNS(null, "validateFilter")) {
            poolValidateFilter = poolConfigElem.getAttributeNS(null, "validateFilter");
        }
        LdapValidator poolValidator = new CompareLdapValidator(poolValidateDn, new SearchFilter(poolValidateFilter));
        log.debug("Data connector {} pool validation filter: {}", pluginId, poolValidateFilter);
        pluginBuilder.addPropertyValue("poolValidator", poolValidator);

        int poolExpirationTime = 600000;
        if (poolConfigElem != null && poolConfigElem.hasAttributeNS(null, "expirationTime")) {
            poolExpirationTime =
                    (int) SpringConfigurationUtils.parseDurationToMillis("expirationTime",
                            poolConfigElem.getAttributeNS(null, "expirationTime"), 0);
        }
        log.debug("Data connector {} pool expiration time: {}ms", pluginId, poolExpirationTime);
        ldapPoolConfig.setExpirationTime(poolExpirationTime);
    }

    /**
     * Processes the cache configuration directives.
     * 
     * @param pluginId ID of the plugin
     * @param pluginConfig configuration element for the plugin
     * @param pluginBuilder builder for the plugin
     */
    protected void processCacheConfig(String pluginId, Element pluginConfig, BeanDefinitionBuilder pluginBuilder) {
        boolean cacheResults = false;
        String cacheManagerId = "shibboleth.CacheManager";
        long cacheElementTtl = 4 * 60 * 60 * 1000;
        int maximumCachedElements = 500;

        List<Element> cacheConfigs =
                XMLHelper.getChildElementsByTagNameNS(pluginConfig, DataConnectorNamespaceHandler.NAMESPACE,
                        "ResultCache");
        if (cacheConfigs != null && !cacheConfigs.isEmpty()) {
            Element cacheConfig = cacheConfigs.get(0);

            cacheResults = true;

            if (cacheConfig.hasAttributeNS(null, "cacheManagerRef")) {
                cacheManagerId = DatatypeHelper.safeTrim(cacheConfig.getAttributeNS(null, "cacheManagerRef"));
            }

            if (cacheConfig.hasAttributeNS(null, "elementTimeToLive")) {
                cacheElementTtl =
                        SpringConfigurationUtils.parseDurationToMillis("elementTimeToLive on data connector "
                                + pluginId, cacheConfig.getAttributeNS(null, "elementTimeToLive"), 0);
            }

            if (cacheConfig.hasAttributeNS(null, "maximumCachedElements")) {
                maximumCachedElements =
                        Integer.parseInt(DatatypeHelper.safeTrim(cacheConfig.getAttributeNS(null,
                                "maximumCachedElements")));
            }
        }

        if (pluginConfig.hasAttributeNS(null, "cacheResults")) {
            log.warn("Data connection {}: use of 'cacheResults' attribute is deprecated.  Use <ResultCache> instead.",
                    pluginId);
            cacheResults = XMLHelper.getAttributeValueAsBoolean(pluginConfig.getAttributeNodeNS(null, "cacheResults"));
        }

        if (cacheResults) {
            log.debug("Data connector {} is caching results: {}", pluginId, cacheResults);

            pluginBuilder.addPropertyReference("cacheManager", cacheManagerId);

            log.debug("Data connector {} cache element time to live: {}ms", pluginId, cacheElementTtl);
            pluginBuilder.addPropertyValue("cacheElementTimeToLive", cacheElementTtl);

            log.debug("Data connector {} maximum number of caches elements: {}", pluginId, maximumCachedElements);
            pluginBuilder.addPropertyValue("maximumCachedElements", maximumCachedElements);
        }

    }

    /**
     * Processes the LDAP properties provided in the configuration.
     * 
     * @param propertyElems LDAP properties provided in the configuration
     * 
     * @return LDAP properties provided in the configuration
     */
    protected Map<String, String> processLDAPProperties(List<Element> propertyElems) {
        if (propertyElems == null || propertyElems.size() == 0) {
            return null;
        }

        HashMap<String, String> properties = new HashMap<String, String>(5);

        String propName;
        String propValue;
        for (Element propertyElem : propertyElems) {
            propName = DatatypeHelper.safeTrimOrNullString(propertyElem.getAttributeNS(null, "name"));
            propValue = DatatypeHelper.safeTrimOrNullString(propertyElem.getAttributeNS(null, "value"));
            properties.put(propName, propValue);
        }

        return properties;
    }

    /**
     * Processes a credential element.
     * 
     * @param credentials list containing the element to process.
     * @param parserContext current parser context
     * 
     * @return the bean definition for the credential
     */
    protected RuntimeBeanReference processCredential(List<Element> credentials, ParserContext parserContext) {
        if (credentials == null) {
            return null;
        }

        Element credentialElem = credentials.get(0);
        return SpringConfigurationUtils.parseCustomElement(credentialElem, parserContext);
    }
}
