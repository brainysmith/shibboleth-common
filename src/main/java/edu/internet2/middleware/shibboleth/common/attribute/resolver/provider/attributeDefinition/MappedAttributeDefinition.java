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

package edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.LazySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;

/**
 * The RegexAttributeDefinition allows regular expression based replacements on attribute values, using the regex syntax
 * allowed by {@link java.util.regex.Pattern}.
 */
public class MappedAttributeDefinition extends BaseAttributeDefinition {

    /** Class logger. */
    private static Logger log = LoggerFactory.getLogger(MappedAttributeDefinition.class);

    /** Default return value. */
    private String defaultValue;

    /** Whether the definition passes thru unmatched values. */
    private boolean passThru;

    /** Value maps. */
    private Collection<ValueMap> valueMaps;

    /** Constructor. */
    public MappedAttributeDefinition() {
        valueMaps = new ArrayList<ValueMap>(5);
    }

    /** {@inheritDoc} */
    protected BaseAttribute doResolve(ShibbolethResolutionContext resolutionContext)
            throws AttributeResolutionException {
        BasicAttribute<String> attribute = new BasicAttribute<String>();
        attribute.setId(getId());

        Collection<?> values = getValuesFromAllDependencies(resolutionContext);
        if (values == null || values.isEmpty()) {
            log.debug("Attribute Definition {}: No values from dependency attribute attribute {}", getId(),
                    getDependencyIds());
            if (!DatatypeHelper.isEmpty(getDefaultValue())) {
                log.debug(
                        "Attribute Definition {}: Default value is not empty, adding it as the value for this attribute",
                        getId());
                attribute.getValues().add(getDefaultValue());
            }
            return attribute;
        }

        Set<String> mappedValues;
        for (Object o : values) {
            if (o == null) {
                log.debug("Attribute Definition {}: null attribute value, skipping it", getId());
                continue;
            }
            mappedValues = mapValue(o.toString());
            attribute.getValues().addAll(mappedValues);
        }

        return attribute;
    }

    /**
     * Maps the value from a dependency in to the value(s) for this attribute.
     * 
     * @param value the value from the dependency
     * 
     * @return the set of attribute values that the given dependency value maps in to
     */
    protected Set<String> mapValue(String value) {
        log.debug("Attribute Definition {}: mapping depdenency attribute value {}", getId(), value);
        
        LazySet<String> mappedValues = new LazySet<String>();

        boolean valueMapMatch = false;
        if (!DatatypeHelper.isEmpty(value)) {
            for (ValueMap valueMap : valueMaps) {
                mappedValues.addAll(valueMap.evaluate(value));
                if (!mappedValues.isEmpty()) {
                    valueMapMatch = true;
                }
            }

            if (!valueMapMatch) {
                if (passThru) {
                    mappedValues.add(value);
                } else if (getDefaultValue() != null) {
                    mappedValues.add(getDefaultValue());
                }
            }
        }

        log.debug("Attribute Definition {}: mapped depdenency attribute value {} to the values {}", new Object[] {
                getId(), value, mappedValues, });
        
        return mappedValues;
    }

    /** {@inheritDoc} */
    public void validate() throws AttributeResolutionException {
        if (passThru && !DatatypeHelper.isEmpty(defaultValue)) {
            log.error("MappedAttributeDefinition (" + getId()
                    + ") may not have a DefaultValue string with passThru enabled.");
            throw new AttributeResolutionException("MappedAttributeDefinition (" + getId()
                    + ") may not have a DefaultValue string with passThru enabled.");
        }
    }

    /**
     * Gets the default return value.
     * 
     * @return the default return value.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default return value.
     * 
     * @param newDefaultValue the default return value
     */
    public void setDefaultValue(String newDefaultValue) {
        defaultValue = newDefaultValue;
    }

    /**
     * Gets whether the definition passes thru unmatched values.
     * 
     * @return whether the definition passes thru unmatched values.
     */
    public boolean isPassThru() {
        return passThru;
    }

    /**
     * Sets whether the definition passes thru unmatched values.
     * 
     * @param newPassThru whether the definition passes thru unmatched values.
     */
    public void setPassThru(boolean newPassThru) {
        passThru = newPassThru;
    }

    /**
     * Get the value maps.
     * 
     * @return the value maps.
     */
    public Collection<ValueMap> getValueMaps() {
        return valueMaps;
    }

}