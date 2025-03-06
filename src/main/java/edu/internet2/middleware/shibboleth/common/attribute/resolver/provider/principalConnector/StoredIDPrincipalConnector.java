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

import java.sql.SQLException;

import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.SubjectQuery;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.StoredIDDataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.StoredIDStore;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.StoredIDStore.PersistentIdEntry;
import edu.internet2.middleware.shibboleth.common.profile.provider.SAMLProfileRequestContext;

/**
 * A principal connector that resolved ID created by {@link edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.principalConnector.StoredIDPrincipalConnector}s into principals.
 */
public class StoredIDPrincipalConnector extends BasePrincipalConnector {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(StoredIDPrincipalConnector.class);

    /** ID store that manages the stored IDs. */
    private StoredIDStore pidStore;

    /** Whether an empty result set is an error. */
    private boolean noResultIsError;

    /**
     * Constructor.
     * 
     * @param idProducer data connector that produced the stored ID.
     */
    public StoredIDPrincipalConnector(StoredIDDataConnector idProducer) {
        if (idProducer == null) {
            throw new IllegalArgumentException("ID producing data connector may not be null");
        }
        pidStore = idProducer.getStoredIDStore();
        noResultIsError = false;
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

    /** {@inheritDoc} */
    public String resolve(ShibbolethResolutionContext resolutionContext) throws AttributeResolutionException {
        SAMLProfileRequestContext requestContext = resolutionContext.getAttributeRequestContext();

        String persistentId;
        if (requestContext.getSubjectNameIdentifier() instanceof NameIdentifier) {
            persistentId = ((NameIdentifier) requestContext.getSubjectNameIdentifier()).getNameIdentifier();
        } else if (requestContext.getSubjectNameIdentifier() instanceof NameID) {
            persistentId = ((NameID) requestContext.getSubjectNameIdentifier()).getValue();
        } else {
            throw new AttributeResolutionException("Subject name identifier is not of a supported type");
        }

        try {
            PersistentIdEntry pidEntry = pidStore.getActivePersistentIdEntry(persistentId);
            if (pidEntry == null) {
                if (noResultIsError) {
                    log.warn("PersistentId '{}' not found", persistentId);
                    throw new AttributeResolutionException("No identifier found");
                }
                return null;
            }

            if (!DatatypeHelper.safeEquals(pidEntry.getPeerEntityId(), getPeerEntityId(resolutionContext))) {
                log.warn(
                        "Requester '{}' attempted to use identifier '{}' which was issued to the entity '{}'",
                        new Object[] {requestContext.getInboundMessageIssuer(), pidEntry.getPersistentId(),
                                pidEntry.getPeerEntityId(),});
                if (noResultIsError) {
                    throw new AttributeResolutionException("identifier mismatch");
                }
                return null;
            }

            return pidEntry.getPrincipalName();
        } catch (SQLException e) {
            log.error("Error retrieving persistent ID from database", e);
            throw new AttributeResolutionException("Error retrieving persistent ID from database", e);
        }

    }

    /**
     * Gets the entity ID used for the peer. If the inbound request is a SAML 2 authentication context and contains a
     * NameIDPolicy than the SPNameQualifier is used if present, otherwise the inbound message issuer is used.
     * 
     * @param resolutionContext current attribute resolution context
     * 
     * @return the entity ID to use for the peer
     */
    protected String getPeerEntityId(ShibbolethResolutionContext resolutionContext) {
        SAMLProfileRequestContext requestContext = resolutionContext.getAttributeRequestContext();
        
        String peerEntityId = null;

        log.debug("Determining if peer entity ID will be the SPNameQualifier from a SAML 2 authentication statement");
        XMLObject inboundMessage = requestContext.getInboundSAMLMessage();
        if (inboundMessage instanceof AuthnRequest) {
            AuthnRequest authnRequest = (AuthnRequest) inboundMessage;
            if (authnRequest.getNameIDPolicy() != null) {
                peerEntityId = DatatypeHelper.safeTrimOrNullString(authnRequest.getNameIDPolicy().getSPNameQualifier());
                if (peerEntityId == null) {
                    log.debug("SAML 2 authentication request did not contain an SPNameQualifier within its NameIDPolicy");
                } else {
                    log.debug("SAML 2 authentication request contained an SPNameQualifier, within its NameIDPolicy.  Using that as peer entity ID");
                }
            } else {
                log.debug("SAML 2 authentication request did not contain a NameIDPolicy");
            }
        } else if (inboundMessage instanceof SubjectQuery) {
            SubjectQuery query = (SubjectQuery) inboundMessage;
            if (query.getSubject().getNameID().getSPNameQualifier() != null) {
                peerEntityId =
                        DatatypeHelper.safeTrimOrNullString(query.getSubject().getNameID().getSPNameQualifier());
                if (peerEntityId == null) {
                    log.debug("SAML 2 subject query did not contain an SPNameQualifier within its NameID");
                } else {
                    log.debug("SAML 2 subject query contained an SPNameQualifier, within its NameID.  Using that as peer entity ID");
                }
            } else {
                log.debug("SAML 2 attribute query did not contain a SPNameQualifier");
            }
        } else {
            peerEntityId = requestContext.getInboundMessageIssuer(); 
        }

        if (peerEntityId == null) {
            log.debug("Determining if inbound message issuer is available for use as peer entity ID");
            peerEntityId = resolutionContext.getAttributeRequestContext().getInboundMessageIssuer();
        }

        return peerEntityId;
    }

    /** {@inheritDoc} */
    public void validate() throws AttributeResolutionException {
        if (pidStore == null) {
            throw new AttributeResolutionException("Persistent ID store was null");
        }

        try {
            pidStore.getPersistentIdEntry("test", false);
        } catch (SQLException e) {
            throw new AttributeResolutionException("Persistent ID store can not perform persistent ID search", e);
        }
    }
}