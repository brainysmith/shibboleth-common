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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;

/**
 * A data connector that can retrieve information from a relational database through JDBC, version 3.
 */
public class RDBMSDataConnector extends BaseDataConnector {

    /** Data types understood by this connector. */
    public static enum DATA_TYPES {
        BigDecimal, Boolean, Byte, ByteArray, Date, Double, Float, Integer, Long, Object, Short, String, Time, Timestamp, URL
    };

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(RDBMSDataConnector.class);

    /** JDBC data source for retrieving connections. */
    private DataSource dataSource;

    /** Template engine used to change query template into actual query. */
    private TemplateEngine queryCreator;

    /** Name the query template is registered under with the statement creator. */
    private String queryTemplateName;

    /** Template that produces the query to use. */
    private String queryTemplate;
    
    /** SQL query timeout in seconds. */
    private int queryTimeout;

    /** Whether the JDBC connection is read-only. */
    private boolean readOnlyConnection;

    /** Whether queries might use stored procedures. */
    private boolean usesStoredProcedure;

    /** Whether an empty result set is an error. */
    private boolean noResultIsError;

    /** Set of column descriptors for managing returned data. [columnName => colmentDescriptr] */
    private Map<String, RDBMSColumnDescriptor> columnDescriptors;
    
    /** Query result cache. */
    private Cache resultsCache;
    
    /**
     * Constructor.
     * 
     * @param source data source used to retrieve connections
     * @param cache cache used to cache results
     */
    public RDBMSDataConnector(DataSource source, Cache cache) {
        super();

        dataSource = source;

        resultsCache = cache;

        readOnlyConnection = true;
        usesStoredProcedure = false;
        noResultIsError = false;

        columnDescriptors = new HashMap<String, RDBMSColumnDescriptor>();
    }

    /**
     * This sets the underlying template engine and registers the supplied template.
     * 
     * @param engine template engine used to generate the query
     * @param template template used to generate the query
     */
    public void registerTemplate(TemplateEngine engine, String template) {
        if (getId() == null) {
            throw new IllegalStateException("Template cannot be registered until plugin id has been set");
        }
        queryCreator = engine;
        queryTemplate = template;
        queryTemplateName = "shibboleth.resolver.dc." + getId();
        queryCreator.registerTemplate(queryTemplateName, queryTemplate);
    }

    /**
     * Gets whether this data connector is caching results.
     * 
     * @return true if this data connector is caching results, false if not
     */
    public boolean isCachingResuts() {
        return resultsCache != null;
    }

    /**
     * Gets the timeout, in seconds, of the SQL query.
     * 
     * @return timeout, in seconds, of the SQL query
     */
    public int getQueryTimeout() {
        return queryTimeout;
    }
    
    /**
     * Sets the timeout, in seconds, of the SQL query.
     * 
     * @param timeout timeout, in seconds, of the SQL query
     */
    public void setQueryTimeout(int timeout) {
        queryTimeout = timeout;
    }
    
    /**
     * Gets whether this data connector uses read-only connections.
     * 
     * @return whether this data connector uses read-only connections
     */
    public boolean isConnectionReadOnly() {
        return readOnlyConnection;
    }

    /**
     * Sets whether this data connector uses read-only connections.
     * 
     * @param isReadOnly whether this data connector uses read-only connections
     */
    public void setConnectionReadOnly(boolean isReadOnly) {
        readOnlyConnection = isReadOnly;
    }

    /**
     * Gets whether queries made use stored procedures.
     * 
     * @return whether queries made use stored procedures
     */
    public boolean getUsesStoredProcedure() {
        return usesStoredProcedure;
    }

    /**
     * Sets whether queries made use stored procedures.
     * 
     * @param storedProcedure whether queries made use stored procedures
     */
    public void setUsesStoredProcedure(boolean storedProcedure) {
        usesStoredProcedure = storedProcedure;
    }

    /**
     * This returns whether this connector will throw an exception if no search results are found. The default is false.
     * 
     * @return <code>boolean</code>
     */
    public boolean isNoResultIsError() {
        return noResultIsError;
    }

    /**
     * This sets whether this connector will throw an exception if no search results are found.
     * 
     * @param isError <code>boolean</code>
     */
    public void setNoResultIsError(boolean isError) {
        noResultIsError = isError;
    }

    /**
     * Gets the set of column descriptors used to deal with result set data. The name of the database column is the
     * map's key. This list is unmodifiable.
     * 
     * @return column descriptors used to deal with result set data
     */
    public Map<String, RDBMSColumnDescriptor> getColumnDescriptor() {
        return columnDescriptors;
    }

    /** {@inheritDoc} */
    public void validate() throws AttributeResolutionException {
        log.debug("RDBMS data connector {} - Validating configuration.", getId());

        if (dataSource == null) {
            log.error("RDBMS data connector {} - Datasource is null", getId());
            throw new AttributeResolutionException("Datasource is null");
        }

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            if (connection == null) {
                log.error("RDBMS data connector {} - Unable to create connections", getId());
                throw new AttributeResolutionException("Unable to create connections for RDBMS data connector "
                        + getId());
            }

            DatabaseMetaData dbmd = connection.getMetaData();
            if (!dbmd.supportsStoredProcedures() && usesStoredProcedure) {
                log.error("RDBMS data connector {} - Database does not support stored procedures.", getId());
                throw new AttributeResolutionException("Database does not support stored procedures.");
            }

            log.debug("RDBMS data connector {} - Connector configuration is valid.", getId());
        } catch (SQLException e) {
            if (e.getSQLState() != null) {
                log.error("RDBMS data connector {} - Invalid connector configuration; SQL state: {}, SQL Code: {}",
                        new Object[] { getId(), e.getSQLState(), e.getErrorCode() }, e);
            } else {
                log.error("RDBMS data connector {} - Invalid connector configuration", new Object[] { getId() }, e);
            }
            throw new AttributeResolutionException("Invalid connector configuration", e);
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("RDBMS data connector {} - Error closing database connection; SQL State: {}, SQL Code: {}",
                        new Object[] { getId(), e.getSQLState(), e.getErrorCode() }, e);
            }
        }
    }

    /** {@inheritDoc} */
    public Map<String, BaseAttribute> resolve(ShibbolethResolutionContext resolutionContext)
            throws AttributeResolutionException {
        String query = queryCreator.createStatement(queryTemplateName, resolutionContext, getDependencyIds(), null);
        log.debug("RDBMS data connector {} - Search Query: {}", getId(), query);

        Map<String, BaseAttribute> resolvedAttributes = null;
        resolvedAttributes = retrieveAttributesFromCache(resolutionContext.getAttributeRequestContext()
                .getPrincipalName(), query);

        if (resolvedAttributes == null) {
            resolvedAttributes = retrieveAttributesFromDatabase(query);
        }

        cacheResult(resolutionContext.getAttributeRequestContext().getPrincipalName(), query, resolvedAttributes);

        return resolvedAttributes;
    }

    /**
     * Attempts to retrieve the attributes from the cache.
     * 
     * @param principal the principal name of the user the attributes are for
     * @param query query used to generate the attributes
     * 
     * @return cached attributes
     * 
     * @throws AttributeResolutionException thrown if there is a problem retrieving data from the cache
     */
    protected Map<String, BaseAttribute> retrieveAttributesFromCache(String principal, String query)
            throws AttributeResolutionException {
        if (resultsCache == null) {
            return null;
        }

        Element cacheElement = resultsCache.get(query);
        if (cacheElement != null && !cacheElement.isExpired()) {
            log.debug("RDBMS data connector {} - Fetched attributes from cache for principal {}", getId(), principal);
            return (Map<String, BaseAttribute>) cacheElement.getObjectValue();
        }

        return null;
    }

    /**
     * Attempts to retrieve the attribute from the database.
     * 
     * @param query query used to get the attributes
     * 
     * @return attributes gotten from the database
     * 
     * @throws AttributeResolutionException thrown if there is a problem retrieving data from the database or
     *             transforming that data into {@link BaseAttribute}s
     */
    protected Map<String, BaseAttribute> retrieveAttributesFromDatabase(String query)
            throws AttributeResolutionException {
        Map<String, BaseAttribute> resolvedAttributes;
        Connection connection = null;
        ResultSet queryResult = null;

        try {
            connection = dataSource.getConnection();
            if (readOnlyConnection) {
                connection.setReadOnly(true);
            }
            log.debug("RDBMS data connector {} - Querying database for attributes with query {}", getId(), query);
            Statement stmt = connection.createStatement();
            stmt.setQueryTimeout(queryTimeout);
            queryResult = stmt.executeQuery(query);
            resolvedAttributes = processResultSet(queryResult);
            if (resolvedAttributes.isEmpty() && noResultIsError) {
                log.debug("RDBMS data connector {} - No attributes from query", getId());
                throw new AttributeResolutionException("No attributes returned from query");
            }
            log.debug("RDBMS data connector {} - Retrieved attributes: {}", getId(), resolvedAttributes.keySet());
            return resolvedAttributes;
        } catch (SQLException e) {
            log.debug("RDBMS data connector {} - Unable to execute SQL query {}; SQL State: {}, SQL Code: {}",
                    new Object[] { getId(), query, e.getSQLState(), e.getErrorCode(), }, e);
            throw new AttributeResolutionException("Unable to execute SQL query", e);
        } finally {
            try {
                if (queryResult != null) {
                    queryResult.close();
                }

                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.debug("RDBMS data connector {} - Unable to close database connection; SQL State: {}, SQL Code: {}",
                        new Object[] { getId(), e.getSQLState(), e.getErrorCode() }, e);
            }
        }
    }

    /**
     * Converts a SQL query results set into a set of {@link BaseAttribute}s.
     * 
     * @param resultSet the result set to convert
     * 
     * @return the resultant set of attributes
     * 
     * @throws AttributeResolutionException thrown if there is a problem converting the result set into attributes
     */
    protected Map<String, BaseAttribute> processResultSet(ResultSet resultSet) throws AttributeResolutionException {
        Map<String, BaseAttribute> attributes = new HashMap<String, BaseAttribute>();

        try {
            if (!resultSet.next()) {
                return attributes;
            }

            ResultSetMetaData resultMD = resultSet.getMetaData();
            int numOfCols = resultMD.getColumnCount();
            String columnName;
            RDBMSColumnDescriptor columnDescriptor;
            String attributeId;
            BaseAttribute attribute;
            Collection attributeValues;
            
            do {
                for (int i = 1; i <= numOfCols; i++) {
                    columnName = resultMD.getColumnName(i);
                    columnDescriptor = columnDescriptors.get(columnName);

                    if (columnDescriptor == null || columnDescriptor.getAttributeID() == null) {
                        attributeId = columnName;
                    } else {
                        attributeId = columnDescriptor.getAttributeID();
                    }
                    
                    attribute = attributes.get(attributeId);
                    if (attribute == null) {
                        attribute = new BasicAttribute(attributeId);
                    }

                    attributes.put(attribute.getId(), attribute);
                    attributeValues = attribute.getValues();
                    if (columnDescriptor == null || columnDescriptor.getDataType() == null) {
                        attributeValues.add(resultSet.getObject(i));
                    } else {
                        addValueByType(attributeValues, columnDescriptor.getDataType(), resultSet, i);
                    }
                }
            } while (resultSet.next());
        } catch (SQLException e) {
            log.debug("RDBMS data connector {} - Unable to read data from query result; SQL State: {}, SQL Code: {}",
                    new Object[] { getId(), e.getSQLState(), e.getErrorCode() }, e);
        }

        return attributes;
    }

    /**
     * Adds a value extracted from the result set as a specific type into the value set.
     * 
     * @param values set to add values into
     * @param type type the value should be extracted as
     * @param resultSet result set, on the current row, to extract the value from
     * @param columnIndex index of the column from which to extract the attribute
     * 
     * @throws java.sql.SQLException thrown if value can not retrieved from the result set
     */
    protected void addValueByType(Collection values, DATA_TYPES type, ResultSet resultSet, int columnIndex)
            throws SQLException {
        switch (type) {
            case BigDecimal:
                values.add(resultSet.getBigDecimal(columnIndex));
                break;
            case Boolean:
                values.add(resultSet.getBoolean(columnIndex));
                break;
            case Byte:
                values.add(resultSet.getByte(columnIndex));
                break;
            case ByteArray:
                values.add(resultSet.getBytes(columnIndex));
                break;
            case Date:
                values.add(resultSet.getDate(columnIndex));
                break;
            case Double:
                values.add(resultSet.getDouble(columnIndex));
                break;
            case Float:
                values.add(resultSet.getFloat(columnIndex));
                break;
            case Integer:
                values.add(resultSet.getInt(columnIndex));
                break;
            case Long:
                values.add(resultSet.getLong(columnIndex));
                break;
            case Object:
                values.add(resultSet.getObject(columnIndex));
                break;
            case Short:
                values.add(resultSet.getShort(columnIndex));
                break;
            case Time:
                values.add(resultSet.getTime(columnIndex));
                break;
            case Timestamp:
                values.add(resultSet.getTimestamp(columnIndex));
                break;
            case URL:
                values.add(resultSet.getURL(columnIndex));
                break;
            default:
                values.add(resultSet.getString(columnIndex));
        }
    }

    /**
     * Caches the attributes resulting from a query.
     * 
     * @param principal the principal name of the user the attributes are for
     * @param query the query that generated the attributes
     * @param attributes the results of the query
     */
    protected void cacheResult(String principal, String query, Map<String, BaseAttribute> attributes) {
        if (resultsCache == null) {
            return;
        }

        log.debug("RDBMS data connector {} - Caching attributes for principal {}", getId(), principal);
        Element cacheElement = new Element(query, attributes);
        resultsCache.put(cacheElement);
    }
}