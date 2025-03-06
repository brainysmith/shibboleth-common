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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.profile.provider.SAMLProfileRequestContext;
import edu.internet2.middleware.shibboleth.common.util.DataSealer;
import edu.internet2.middleware.shibboleth.common.util.DataSealerException;

/**
 * An attribute definition that generates integrity protected,
 * encrypted identifiers useful for stateless transient subject IDs.
 */
public class CryptoTransientIdAttributeDefinition extends BaseAttributeDefinition {

    /** Class logger. */
    private static Logger log = LoggerFactory.getLogger(CryptoTransientIdAttributeDefinition.class);
    
    /** Object used to protect and encrypt the data. */
    private DataSealer dataSealer;

    /** Length, in milliseconds, tokens are valid. */
    private long idLifetime;

    /**
     * Constructor.
     * 
     * @param sealer object used to protect and encrypt the data
     */
    public CryptoTransientIdAttributeDefinition(DataSealer sealer) {
        if (sealer == null) {
            throw new IllegalArgumentException("DataSealer may not be null.");
        }
        dataSealer = sealer;
        idLifetime = 1000 * 60 * 60 * 4;
    }

    /** {@inheritDoc} */
    protected BaseAttribute<String> doResolve(ShibbolethResolutionContext resolutionContext)
            throws AttributeResolutionException {

        SAMLProfileRequestContext<?, ?, ?, ?> requestContext = resolutionContext.getAttributeRequestContext();

        StringBuilder principalTokenIdBuilder = new StringBuilder();
        principalTokenIdBuilder.append(requestContext.getOutboundMessageIssuer()).append("!").append(
                requestContext.getInboundMessageIssuer()).append("!").append(requestContext.getPrincipalName());
        String transientId;
        try {
            transientId = dataSealer.wrap(principalTokenIdBuilder.toString(), System.currentTimeMillis() + idLifetime);
        } catch (DataSealerException e) {
            throw new AttributeResolutionException("Caught exception wrapping principal identifier.", e);
        }

        BasicAttribute<String> attribute = new BasicAttribute<String>();
        attribute.setId(getId());
        attribute.getValues().add(transientId);

        return attribute;
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
    public void validate() throws AttributeResolutionException {
        if (dataSealer == null) {
            log.error("CryptoTransientIdAttributeDefinition (" + getId()
                    + ") must have a DataSealer object set.");
            throw new AttributeResolutionException("CryptoTransientIdAttributeDefinition (" + getId()
                    + ") must have a DataSealer object set.");
        }
    }
}