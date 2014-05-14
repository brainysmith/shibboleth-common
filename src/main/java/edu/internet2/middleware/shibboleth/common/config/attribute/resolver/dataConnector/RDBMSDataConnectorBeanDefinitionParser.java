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

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.RDBMSColumnDescriptor;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.RDBMSDataConnector.DATA_TYPES;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

/** Spring bean definition parser for reading relational database data connector. */
public class RDBMSDataConnectorBeanDefinitionParser extends BaseDataConnectorBeanDefinitionParser {

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(DataConnectorNamespaceHandler.NAMESPACE, "RelationalDatabase");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(RDBMSDataConnectorBeanDefinitionParser.class);

    /** {@inheritDoc} */
    protected Class getBeanClass(Element element) {
        return RDBMSDataConnectorFactoryBean.class;
    }

    /** {@inheritDoc} */
    protected void doParse(String pluginId, Element pluginConfig, Map<QName, List<Element>> pluginConfigChildren,
            BeanDefinitionBuilder pluginBuilder, ParserContext parserContext) {
        super.doParse(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

        processConnectionManagement(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

        processQueryHandlingConfig(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

        processResultHandlingConfig(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

        processCacheConfig(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);
    }

    /**
     * Processes the connection management configuration.
     * 
     * @param pluginId ID of the data connector
     * @param pluginConfig configuration element for the data connector
     * @param pluginConfigChildren child config elements for the data connect
     * @param pluginBuilder builder of the data connector
     * @param parserContext current configuration parsing context
     */
    protected void processConnectionManagement(String pluginId, Element pluginConfig,
            Map<QName, List<Element>> pluginConfigChildren, BeanDefinitionBuilder pluginBuilder,
            ParserContext parserContext) {
        DataSource datasource;
        List<Element> cmc = pluginConfigChildren.get(new QName(DataConnectorNamespaceHandler.NAMESPACE,
                "ContainerManagedConnection"));
        if (cmc != null && cmc.get(0) != null) {
            datasource = buildContainerManagedConnection(pluginId, cmc.get(0));
        } else {
            datasource = buildApplicationManagedConnection(pluginId, pluginConfigChildren.get(
                    new QName(DataConnectorNamespaceHandler.NAMESPACE, "ApplicationManagedConnection")).get(0));
        }

        pluginBuilder.addPropertyValue("connectionDataSource", datasource);
    }

    /**
     * Builds a JDBC {@link javax.sql.DataSource} from a ContainerManagedConnection configuration element.
     * 
     * @param pluginId ID of this data connector
     * @param cmc the container managed configuration element
     * 
     * @return the built data source
     */
    protected DataSource buildContainerManagedConnection(String pluginId, Element cmc) {
        String jndiResource = cmc.getAttributeNS(null, "resourceName");
        jndiResource = DatatypeHelper.safeTrim(jndiResource);

        Hashtable<String, String> initCtxProps = buildProperties(XMLHelper.getChildElementsByTagNameNS(cmc,
                DataConnectorNamespaceHandler.NAMESPACE, "JNDIConnectionProperty"));
        try {
            InitialContext initCtx = new InitialContext(initCtxProps);
            DataSource dataSource = (DataSource) initCtx.lookup(jndiResource);
            if (dataSource == null) {
                log.error("DataSource " + jndiResource + " did not exist in JNDI directory");
                throw new BeanCreationException("DataSource " + jndiResource + " did not exist in JNDI directory");
            }
            if (log.isDebugEnabled()) {
                log.debug("Retrieved data source for data connector {} from JNDI location {} using properties ",
                        pluginId, initCtxProps);
            }
            return dataSource;
        } catch (NamingException e) {
            log.error("Unable to retrieve data source for data connector " + pluginId + " from JNDI location "
                    + jndiResource + " using properties " + initCtxProps, e);
            return null;
        }
    }

    /**
     * Builds a JDBC {@link javax.sql.DataSource} from an ApplicationManagedConnection configuration element.
     * 
     * @param pluginId ID of this data connector
     * @param amc the application managed configuration element
     * 
     * @return the built data source
     */
    protected DataSource buildApplicationManagedConnection(String pluginId, Element amc) {
        ComboPooledDataSource datasource = new ComboPooledDataSource();

        String driverClass = DatatypeHelper.safeTrim(amc.getAttributeNS(null, "jdbcDriver"));
        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            classLoader.loadClass(driverClass);
        } catch (ClassNotFoundException e) {
            log.error("Unable to create relational database connector, JDBC driver can not be found on the classpath");
            throw new BeanCreationException(
                    "Unable to create relational database connector, JDBC driver can not be found on the classpath");
        }

        try {
            datasource.setDriverClass(driverClass);
            datasource.setJdbcUrl(DatatypeHelper.safeTrim(amc.getAttributeNS(null, "jdbcURL")));
            datasource.setUser(DatatypeHelper.safeTrim(amc.getAttributeNS(null, "jdbcUserName")));
            datasource.setPassword(DatatypeHelper.safeTrim(amc.getAttributeNS(null, "jdbcPassword")));

            if (amc.hasAttributeNS(null, "poolAcquireIncrement")) {
                datasource.setAcquireIncrement(Integer.parseInt(DatatypeHelper.safeTrim(amc.getAttributeNS(null,
                        "poolAcquireIncrement"))));
            } else {
                datasource.setAcquireIncrement(3);
            }

            if (amc.hasAttributeNS(null, "poolAcquireRetryAttempts")) {
                datasource.setAcquireRetryAttempts(Integer.parseInt(DatatypeHelper.safeTrim(amc.getAttributeNS(null,
                        "poolAcquireRetryAttempts"))));
            } else {
                datasource.setAcquireRetryAttempts(36);
            }

            if (amc.hasAttributeNS(null, "poolAcquireRetryDelay")) {
                datasource.setAcquireRetryDelay(Integer.parseInt(DatatypeHelper.safeTrim(amc.getAttributeNS(null,
                        "poolAcquireRetryDelay"))));
            } else {
                datasource.setAcquireRetryDelay(5000);
            }

            if (amc.hasAttributeNS(null, "poolBreakAfterAcquireFailure")) {
                datasource.setBreakAfterAcquireFailure(XMLHelper.getAttributeValueAsBoolean(amc.getAttributeNodeNS(
                        null, "poolBreakAfterAcquireFailure")));
            } else {
                datasource.setBreakAfterAcquireFailure(true);
            }

            if (amc.hasAttributeNS(null, "poolMinSize")) {
                datasource.setMinPoolSize(Integer.parseInt(DatatypeHelper.safeTrim(amc.getAttributeNS(null,
                        "poolMinSize"))));
            } else {
                datasource.setMinPoolSize(2);
            }

            if (amc.hasAttributeNS(null, "poolMaxSize")) {
                datasource.setMaxPoolSize(Integer.parseInt(DatatypeHelper.safeTrim(amc.getAttributeNS(null,
                        "poolMaxSize"))));
            } else {
                datasource.setMaxPoolSize(50);
            }

            if (amc.hasAttributeNS(null, "poolMaxIdleTime")) {
                datasource.setMaxIdleTime(Integer.parseInt(DatatypeHelper.safeTrim(amc.getAttributeNS(null,
                        "poolMaxIdleTime"))));
            } else {
                datasource.setMaxIdleTime(600);
            }

            if (amc.hasAttributeNS(null, "poolIdleTestPeriod")) {
                datasource.setIdleConnectionTestPeriod(Integer.parseInt(DatatypeHelper.safeTrim(amc.getAttributeNS(
                        null, "poolIdleTestPeriod"))));
            } else {
                datasource.setIdleConnectionTestPeriod(180);
            }

            log.debug("Created application managed data source for data connector {}", pluginId);
            return datasource;
        } catch (PropertyVetoException e) {
            log.error("Unable to create data source for data connector {} with JDBC driver class {}", pluginId,
                    driverClass);
            return null;
        }
    }

    /**
     * Processes query handling related configuration options.
     * 
     * @param pluginId ID of the data connector
     * @param pluginConfig configuration element for the data connector
     * @param pluginConfigChildren child config elements for the data connect
     * @param pluginBuilder builder of the data connector
     * @param parserContext current configuration parsing context
     */
    protected void processQueryHandlingConfig(String pluginId, Element pluginConfig,
            Map<QName, List<Element>> pluginConfigChildren, BeanDefinitionBuilder pluginBuilder,
            ParserContext parserContext) {
        String templateEngineRef = pluginConfig.getAttributeNS(null, "templateEngine");
        pluginBuilder.addPropertyReference("templateEngine", templateEngineRef);

        List<Element> queryTemplateElems = pluginConfigChildren.get(new QName(DataConnectorNamespaceHandler.NAMESPACE,
                "QueryTemplate"));
        String queryTemplate = queryTemplateElems.get(0).getTextContent();
        log.debug("Data connector {} query template: {}", pluginId, queryTemplate);
        pluginBuilder.addPropertyValue("queryTemplate", queryTemplate);

        long queryTimeout = 5 * 1000;
        if (pluginConfig.hasAttributeNS(null, "queryTimeout")) {
            queryTimeout = SpringConfigurationUtils.parseDurationToMillis(
                    "queryTimeout on relational database connector " + pluginId, pluginConfig.getAttributeNS(null,
                            "queryTimeout"), 0);
        }
        log.debug("Data connector {} SQL query timeout: {}ms", pluginId, queryTimeout);
        pluginBuilder.addPropertyValue("queryTimeout", queryTimeout);

        boolean useSP = false;
        if (pluginConfig.hasAttributeNS(null, "queryUsesStoredProcedure")) {
            useSP = XMLHelper.getAttributeValueAsBoolean(pluginConfig.getAttributeNodeNS(null,
                    "queryUsesStoredProcedure"));
        }
        log.debug("Data connector {} query uses stored procedures: {}", pluginId, useSP);
        pluginBuilder.addPropertyValue("queryUsesStoredProcedures", useSP);

        boolean readOnlyCtx = true;
        if (pluginConfig.hasAttributeNS(null, "readOnlyConnection")) {
            readOnlyCtx = XMLHelper.getAttributeValueAsBoolean(pluginConfig.getAttributeNodeNS(null,
                    "readOnlyConnection"));
        }
        log.debug("Data connector {} connections are read only: {}", pluginId, readOnlyCtx);
        pluginBuilder.addPropertyValue("readOnlyConnections", readOnlyCtx);

    }

    /**
     * Processes the result handling configuration options.
     * 
     * @param pluginId ID of the data connector
     * @param pluginConfig configuration element for the data connector
     * @param pluginConfigChildren child config elements for the data connect
     * @param pluginBuilder builder of the data connector
     * @param parserContext current configuration parsing context
     */
    protected void processResultHandlingConfig(String pluginId, Element pluginConfig,
            Map<QName, List<Element>> pluginConfigChildren, BeanDefinitionBuilder pluginBuilder,
            ParserContext parserContext) {

        List<RDBMSColumnDescriptor> descriptors = processColumnDescriptors(pluginId, pluginConfigChildren,
                pluginBuilder);
        pluginBuilder.addPropertyValue("columnDescriptors", descriptors);

        boolean noResultsIsError = false;
        if (pluginConfig.hasAttributeNS(null, "noResultIsError")) {
            noResultsIsError = XMLHelper.getAttributeValueAsBoolean(pluginConfig.getAttributeNodeNS(null,
                    "noResultIsError"));
        }
        log.debug("Data connector {} no results is error: {}", pluginId, noResultsIsError);
        pluginBuilder.addPropertyValue("noResultIsError", noResultsIsError);
    }

    /**
     * Processes the cache configuration options.
     * 
     * @param pluginId ID of the data connector
     * @param pluginConfig configuration element for the data connector
     * @param pluginConfigChildren child config elements for the data connect
     * @param pluginBuilder builder of the data connector
     * @param parserContext current configuration parsing context
     */
    protected void processCacheConfig(String pluginId, Element pluginConfig,
            Map<QName, List<Element>> pluginConfigChildren, BeanDefinitionBuilder pluginBuilder,
            ParserContext parserContext) {
        boolean cacheResults = false;
        String cacheManagerId = "shibboleth.CacheManager";
        long cacheElementTtl = 4 * 60 * 60 * 1000;
        int maximumCachedElements = 500;

        List<Element> cacheConfigs = XMLHelper.getChildElementsByTagNameNS(pluginConfig,
                DataConnectorNamespaceHandler.NAMESPACE, "ResultCache");
        if (cacheConfigs != null && !cacheConfigs.isEmpty()) {
            Element cacheConfig = cacheConfigs.get(0);

            cacheResults = true;

            if (cacheConfig.hasAttributeNS(null, "cacheManagerRef")) {
                cacheManagerId = DatatypeHelper.safeTrim(cacheConfig.getAttributeNS(null, "cacheManagerRef"));
            }

            if (cacheConfig.hasAttributeNS(null, "elementTimeToLive")) {
                cacheElementTtl = SpringConfigurationUtils.parseDurationToMillis("elementTimeToLive on data connector "
                        + pluginId, cacheConfig.getAttributeNS(null, "elementTimeToLive"), 0);
            }

            if (cacheConfig.hasAttributeNS(null, "maximumCachedElements")) {
                maximumCachedElements = Integer.parseInt(DatatypeHelper.safeTrim(cacheConfig.getAttributeNS(null,
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
     * Processes the Column descriptor configuration elements.
     * 
     * @param pluginId ID of this data connector
     * @param pluginConfigChildren configuration elements
     * @param pluginBuilder the bean definition parser
     * 
     * @return result set column descriptors
     */
    protected List<RDBMSColumnDescriptor> processColumnDescriptors(String pluginId,
            Map<QName, List<Element>> pluginConfigChildren, BeanDefinitionBuilder pluginBuilder) {
        List<RDBMSColumnDescriptor> columnDescriptors = new ArrayList<RDBMSColumnDescriptor>();

        QName columnElementName = new QName(DataConnectorNamespaceHandler.NAMESPACE, "Column");

        RDBMSColumnDescriptor columnDescriptor;
        String columnName;
        String attributeId;
        String dataType;
        if (pluginConfigChildren.containsKey(columnElementName)) {
            for (Element columnElem : pluginConfigChildren.get(columnElementName)) {
                columnName = columnElem.getAttributeNS(null, "columnName");
                attributeId = columnElem.getAttributeNS(null, "attributeID");

                if (columnElem.hasAttributeNS(null, "type")) {
                    dataType = columnElem.getAttributeNS(null, "type");
                } else {
                    dataType = DATA_TYPES.String.toString();
                }

                columnDescriptor = new RDBMSColumnDescriptor(columnName, attributeId, DATA_TYPES.valueOf(dataType));
                columnDescriptors.add(columnDescriptor);
            }
            log.debug("Data connector {} column descriptors: {}", pluginId, columnDescriptors);
        }

        return columnDescriptors;
    }

    /**
     * Builds a hash from PropertyType elements.
     * 
     * @param propertyElements properties elements
     * 
     * @return properties extracted from elements, key is the property name.
     */
    protected Hashtable<String, String> buildProperties(List<Element> propertyElements) {
        if (propertyElements == null || propertyElements.size() < 1) {
            return null;
        }

        Hashtable<String, String> properties = new Hashtable<String, String>();

        String propName;
        String propValue;
        for (Element propertyElement : propertyElements) {
            propName = DatatypeHelper.safeTrim(propertyElement.getAttributeNS(null, "name"));
            propValue = DatatypeHelper.safeTrim(propertyElement.getAttributeNS(null, "value"));
            properties.put(propName, propValue);
        }

        return properties;
    }
}