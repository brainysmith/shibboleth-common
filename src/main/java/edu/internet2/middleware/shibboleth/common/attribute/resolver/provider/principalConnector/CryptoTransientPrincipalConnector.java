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

package edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.principalConnector;

import org.opensaml.common.SAMLObject;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.NameID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.profile.provider.SAMLProfileRequestContext;
import edu.internet2.middleware.shibboleth.common.util.DataExpiredException;
import edu.internet2.middleware.shibboleth.common.util.DataSealer;
import edu.internet2.middleware.shibboleth.common.util.DataSealerException;

/**
 * A principal connector that attempts to look up a name identifier within a store.
 */
public class CryptoTransientPrincipalConnector extends BasePrincipalConnector {

    /** Class logger. */
    private static Logger log = LoggerFactory.getLogger(CryptoTransientPrincipalConnector.class);

    /** Object used to protect and encrypt the data. */
    private DataSealer dataSealer;
    
    /**
     * Constructor.
     * 
     * @param sealer object used to protect and encrypt the data
     */
    public CryptoTransientPrincipalConnector(DataSealer sealer) {
        if (sealer == null) {
            throw new IllegalArgumentException("DataSealer may not be null.");
        }
        dataSealer = sealer;
    }

    /** {@inheritDoc} */
    public String resolve(ShibbolethResolutionContext resolutionContext) throws AttributeResolutionException {
        SAMLProfileRequestContext<?, ?, ?, ?> requestContext = resolutionContext.getAttributeRequestContext();

        String transientId = null;
        String nameQualifier = null;
        String spNameQualifier = null;
        SAMLObject subjectId = requestContext.getSubjectNameIdentifier();
        if (subjectId instanceof NameIdentifier) {
            NameIdentifier nameId = (NameIdentifier) requestContext.getSubjectNameIdentifier();
            if (nameId != null) {
                transientId = nameId.getNameIdentifier();
                nameQualifier = nameId.getNameQualifier();
            }
        } else if (requestContext.getSubjectNameIdentifier() instanceof NameID) {
            NameID nameId = (NameID) requestContext.getSubjectNameIdentifier();
            if (nameId != null) {
                transientId = nameId.getValue();
                nameQualifier = nameId.getNameQualifier();
                spNameQualifier = nameId.getSPNameQualifier();
            }
        } else {
            throw new AttributeResolutionException("Subject name identifier is not of a supported type");
        }

        if (transientId == null) {
            throw new AttributeResolutionException("Invalid subject name identifier");
        }

        String decodedId;
        try {
            decodedId = dataSealer.unwrap(transientId);
        } catch (DataExpiredException e) {
            throw new AttributeResolutionException("Principal identifier has expired.");
        } catch (DataSealerException e) {
            throw new AttributeResolutionException("Caught exception unwrapping principal identifier.", e);
        }
        
        if (decodedId == null) {
            throw new AttributeResolutionException("Unable to recover principal from transient identifier: "
                    + transientId);
        }
        
        // Split the identifier.
        String[] parts = decodedId.split("!");
        if (parts.length != 3) {
            throw new AttributeResolutionException("Decoded principal information was invalid: "
                    + decodedId);
        }
        
        if (nameQualifier != null && !nameQualifier.equals(parts[0])) {
            throw new AttributeResolutionException("Decoded NameQualifier (" + nameQualifier +
                    ") does not match supplied value (" + parts[0] + ").");
        } else if (spNameQualifier != null && !spNameQualifier.equals(parts[1])) {
            throw new AttributeResolutionException("Decoded SPNameQualifier (" + spNameQualifier +
                    ") does not match supplied value (" + parts[1] + ").");
        } else if (!parts[0].equals(requestContext.getOutboundMessageIssuer())) {
            throw new AttributeResolutionException("Decoded NameQualifier (" + parts[0] +
                    ") does not match issuer (" + requestContext.getOutboundMessageIssuer() + ").");
        } else if (!parts[1].equals(requestContext.getInboundMessageIssuer())) {
            throw new AttributeResolutionException("Decoded SPNameQualifier (" + parts[0] +
                    ") does not match requester (" + requestContext.getInboundMessageIssuer() + ").");
        }

        return parts[2];
    }

    /** {@inheritDoc} */
    public void validate() throws AttributeResolutionException {
        if (dataSealer == null) {
            log.error("CryptoTransientPrincipalConnector (" + getId()
                    + ") must have a DataSealer object set.");
            throw new AttributeResolutionException("CryptoTransientPrincipalConnector (" + getId()
                    + ") must have a DataSealer object set.");
        }
    }
}