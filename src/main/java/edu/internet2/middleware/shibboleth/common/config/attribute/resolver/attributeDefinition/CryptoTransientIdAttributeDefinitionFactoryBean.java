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

package edu.internet2.middleware.shibboleth.common.config.attribute.resolver.attributeDefinition;

import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.attributeDefinition.CryptoTransientIdAttributeDefinition;
import edu.internet2.middleware.shibboleth.common.util.DataSealer;

/**
 * Spring factory bean producing {@link CryptoTransientIdAttributeDefinition}s.
 */
public class CryptoTransientIdAttributeDefinitionFactoryBean extends BaseAttributeDefinitionFactoryBean {

    /** Object used to protect and encrypt identifiers. */
    private DataSealer dataSealer;

    /** Length, in milliseconds, identifiers are valid. */
    private long idLifetime = 1000 * 60 * 60 * 4;
  
    /** {@inheritDoc} */
    public Class<CryptoTransientIdAttributeDefinition> getObjectType() {
        return CryptoTransientIdAttributeDefinition.class;
    }

    /**
     * Gets the object used to protect and encrypt identifiers.
     * 
     * @return object used to protect and encrypt identifiers
     */
    public DataSealer getDataSealer() {
        return dataSealer;
    }

    /**
     * Sets the object used to protect and encrypt identifiers.
     * 
     * @param sealer object used to protect and encrypt identifiers
     */
    public void setDataSealer(DataSealer sealer) {
        dataSealer = sealer;
    }

    /**
     * Gets the time, in milliseconds, ids are valid.
     * 
     * @return time, in milliseconds, ids are valid
     */
    public long getIdLifetime() {
        return idLifetime;
    }

    /**
     * Sets the time, in milliseconds, ids are valid.
     * 
     * @param lifetime time, in milliseconds, ids are valid
     */
    public void setIdLifetime(long lifetime) {
        idLifetime = lifetime;
    }
    
    /** {@inheritDoc} */
    protected Object createInstance() throws Exception {
        CryptoTransientIdAttributeDefinition definition = new CryptoTransientIdAttributeDefinition(getDataSealer());
        definition.setIdLifetime(idLifetime);
        populateAttributeDefinition(definition);

        return definition;
    }
}
