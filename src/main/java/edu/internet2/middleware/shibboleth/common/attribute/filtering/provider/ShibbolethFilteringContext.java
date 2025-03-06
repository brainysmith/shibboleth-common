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

package edu.internet2.middleware.shibboleth.common.attribute.filtering.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opensaml.xml.util.LazyList;
import org.opensaml.xml.util.LazySet;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.profile.provider.SAMLProfileRequestContext;

/**
 * Contextual information for performing attribute filtering.
 */
public class ShibbolethFilteringContext {

    /** The attribute request. */
    private SAMLProfileRequestContext attributeRequestContext;

    /** Attributes being filtered. */
    private Map<String, BaseAttribute> unfilteredAttributes;

    /** Retained values for a given attribute. */
    private Map<String, Collection> retainedValues;

    /** Deny value rules that apply to the attribute identified by the map key. */
    private Map<String, List<MatchFunctor>> denyValueRules;

    /**
     * Constructor.
     * 
     * @param attributes unfiltered attribute set
     * @param context attribute request context
     */
    public ShibbolethFilteringContext(Map<String, BaseAttribute> attributes, SAMLProfileRequestContext context) {
        attributeRequestContext = context;
        unfilteredAttributes = attributes;
        retainedValues = new HashMap<String, Collection>();
        denyValueRules = new HashMap<String, List<MatchFunctor>>();
    }

    /**
     * Gets the context for the attribute request.
     * 
     * @return context for the attribute request
     */
    public SAMLProfileRequestContext getAttributeRequestContext() {
        return attributeRequestContext;
    }

    /**
     * Gets the attributes being filtered.
     * 
     * @return attributes being filtered
     */
    public Map<String, BaseAttribute> getUnfilteredAttributes() {
        return unfilteredAttributes;
    }

    /**
     * Gets the values, for the given attribute, that have no yet been filtered out.
     * 
     * @param attributeId attribute to retrieve the values for
     * @param prepopulate whether to pre-populate the retained value list from the unfiltered value list if there is
     *            currently no set of values retained for the given attribute
     * 
     * @return attribute values not yet filtered out, never null
     */
    public Collection getRetainedValues(String attributeId, boolean prepopulate) {        
        Collection attributeValues;
        if (!retainedValues.containsKey(attributeId)) {
            attributeValues = new LazySet();
            retainedValues.put(attributeId, attributeValues);
            
            if (prepopulate) {
                BaseAttribute attribute = unfilteredAttributes.get(attributeId);
                if (attribute != null && attribute.getValues() != null) {
                    for(Object value : attribute.getValues()){
                        if(value != null){
                            attributeValues.add(value);
                        }
                    }
                }
            }
        } else {
            attributeValues = retainedValues.get(attributeId);
        }

        return attributeValues;
    }

    /**
     * Gets the deny value rules that apply to the attribute. The map key is the ID of the attribute, the value is a
     * list of deny rules that apply to that attribute.
     * 
     * @return deny value rules that apply to the attribute
     */
    public Map<String, List<MatchFunctor>> getDenyValueRules() {
        return denyValueRules;
    }
}