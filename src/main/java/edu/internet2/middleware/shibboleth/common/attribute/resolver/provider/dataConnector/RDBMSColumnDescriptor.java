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

import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.RDBMSDataConnector.DATA_TYPES;

/**
 * Describes how to express a given result set column as an attribute and value.
 */
public class RDBMSColumnDescriptor {

    /** Name of the database column. */
    private String columnName;

    /** Name of the attribute to map the column to. */
    private String attributeName;

    /** Java data type to express the database value as. */
    private DATA_TYPES dataType;

    /**
     * Constructor.
     * 
     * @param column name of the database column
     * @param attribute name of the attribute to map the column to
     * @param type Java data type to express the database value as
     */
    public RDBMSColumnDescriptor(String column, String attribute, DATA_TYPES type) {
        columnName = column;
        attributeName = attribute;
        dataType = type;
    }

    /**
     * Gets the name of the database column.
     * 
     * @return name of the database column
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Gets the name of the attribute to map the column to.
     * 
     * @return name of the attribute to map the column to
     */
    public String getAttributeID() {
        return attributeName;
    }

    /**
     * Gets the Java data type to express the database value as.
     * 
     * @return Java data type to express the database value as
     */
    public DATA_TYPES getDataType() {
        return dataType;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "RBDMSColumnDescriptor{columnName=" + columnName + ", attributeId=" + attributeName + ", dataType="
                + dataType + "}";
    }
}