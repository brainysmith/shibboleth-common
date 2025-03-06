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

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.opensaml.xml.util.DatatypeHelper;

import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.RDBMSColumnDescriptor;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.RDBMSDataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.TemplateEngine;

/**
 * Spring factory bean that produces {@link RDBMSDataConnector}s.
 */
public class RDBMSDataConnectorFactoryBean extends BaseDataConnectorFactoryBean {

    /** Source of connections to the database. */
    private DataSource connectionDataSource;

    /** Template engine used to transform query templates into queries. */
    private TemplateEngine templateEngine;

    /** SQL query template. */
    private String queryTemplate;
    
    /** SQL query timeout in milliseconds. */
    private long queryTimeout;

    /** Whether the database connections should be read-only. */
    private boolean readOnlyConnections;

    /** Whether the SQL query uses stored procedures. */
    private boolean queryUsesStoredProcedures;

    /** Whether an empty result set is an error. */
    private boolean noResultsIsError;

    /** Result set column descriptors. */
    private List<RDBMSColumnDescriptor> columnDescriptors;
    
    /** Whether results should be cached. */
    private CacheManager cacheManager;

    /** Maximum number of queries to keep in the cache. */
    private int maximumCachedElements;

    /** Length of time, in milliseconds, elements are cached. */
    private long cacheElementTtl;


    /** {@inheritDoc} */
    public Class getObjectType() {
        return RDBMSDataConnector.class;
    }

    /**
     * This returns whether this connector will throw an exception if no search results are found. The default is false.
     * 
     * @return <code>boolean</code>
     */
    public boolean isNoResultIsError() {
        return noResultsIsError;
    }

    /**
     * This sets whether this connector will throw an exception if no search results are found.
     * 
     * @param b <code>boolean</code>
     */
    public void setNoResultIsError(boolean b) {
        noResultsIsError = b;
    }

    /**
     * Gets the result set column descriptors.
     * 
     * @return result set column descriptors
     */
    public List<RDBMSColumnDescriptor> getColumnDescriptors() {
        return columnDescriptors;
    }

    /**
     * Sets the result set column descriptors.
     * 
     * @param descriptors result set column descriptors
     */
    public void setColumnDescriptors(List<RDBMSColumnDescriptor> descriptors) {
        columnDescriptors = descriptors;
    }

    /**
     * Gets the database connection source.
     * 
     * @return database connection source.
     */
    public DataSource getConnectionDataSource() {
        return connectionDataSource;
    }

    /**
     * Sets the database connection source.
     * 
     * @param source database connection source
     */
    public void setConnectionDataSource(DataSource source) {
        connectionDataSource = source;
    }

    /**
     * Gets the template engine used to construct the SQL query from the query template.
     * 
     * @return template engine used to construct the SQL query from the query template
     */
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    /**
     * Sets the template engine used to construct the SQL query from the query template.
     * 
     * @param engine template engine used to construct the SQL query from the query template
     */
    public void setTemplateEngine(TemplateEngine engine) {
        templateEngine = engine;
    }

    /**
     * Gets the SQL query template.
     * 
     * @return SQL query template
     */
    public String getQueryTemplate() {
        return queryTemplate;
    }

    /**
     * Sets the SQL query template.
     * 
     * @param template SQL query template
     */
    public void setQueryTemplate(String template) {
        queryTemplate = DatatypeHelper.safeTrimOrNullString(template);
    }
    
    /**
     * Gets the timeout, in milliseconds, of the SQL query.
     * 
     * @return timeout, in milliseconds, of the SQL query.
     */
    public long getQueryTimeout() {
        return queryTimeout;
    }
    
    /**
     * Sets the timeout, in milliseconds, of the SQL query.
     * 
     * @param timeout timeout, in milliseconds, of the SQL query.
     */
    public void setQueryTimeout(long timeout) {
        queryTimeout = timeout;
    }

    /**
     * Gets whether the SQL query uses stored procedures.
     * 
     * @return whether the SQL query uses stored procedures
     */
    public boolean getQueryUsesStoredProcedures() {
        return queryUsesStoredProcedures;
    }

    /**
     * Sets whether the SQL query uses stored procedures.
     * 
     * @param storedProcedures whether the SQL query uses stored procedures
     */
    public void setQueryUsesStoredProcedures(boolean storedProcedures) {
        queryUsesStoredProcedures = storedProcedures;
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
     * Sets the manager for the results cache.
     * 
     * @param manager manager for the results cache
     */
    public void setCacheManager(CacheManager manager) {
        cacheManager = manager;
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
     * Sets the time to live, in milliseconds, for cache elements.
     * 
     * @param ttl time to live, in milliseconds, for cache elements
     */
    public void setCacheElementTimeToLive(long ttl) {
        cacheElementTtl = ttl;
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
     * Sets the maximum number of elements that will be cached.
     * 
     * @param max maximum number of elements that will be cached
     */
    public void setMaximumCachedElements(int max) {
        maximumCachedElements = max;
    }

    /**
     * Gets whether the database connection is read-only.
     * 
     * @return whether the database connection is read-only
     */
    public boolean isReadOnlyConnections() {
        return readOnlyConnections;
    }

    /**
     * Sets whether the database connection is read-only.
     * 
     * @param readOnly whether the database connection is read-only
     */
    public void setReadOnlyConnections(boolean readOnly) {
        readOnlyConnections = readOnly;
    }

    /** {@inheritDoc} */
    protected Object createInstance() throws Exception {
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

        RDBMSDataConnector connector = new RDBMSDataConnector(getConnectionDataSource(), resultsCache);
        populateDataConnector(connector);
        connector.registerTemplate(templateEngine, queryTemplate);

        connector.setQueryTimeout((int) (queryTimeout/1000));
        connector.setUsesStoredProcedure(getQueryUsesStoredProcedures());
        connector.setConnectionReadOnly(isReadOnlyConnections());
        connector.setNoResultIsError(isNoResultIsError());

        if (getColumnDescriptors() != null) {
            Map<String, RDBMSColumnDescriptor> columnDecriptors = connector.getColumnDescriptor();
            for (RDBMSColumnDescriptor descriptor : getColumnDescriptors()) {
                columnDecriptors.put(descriptor.getColumnName(), descriptor);
            }
        }

        return connector;
    }
}