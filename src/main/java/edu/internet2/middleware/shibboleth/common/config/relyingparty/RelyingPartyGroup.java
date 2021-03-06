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

package edu.internet2.middleware.shibboleth.common.config.relyingparty;

import java.util.List;

import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.ws.security.SecurityPolicy;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.trust.TrustEngine;

import edu.internet2.middleware.shibboleth.common.relyingparty.RelyingPartyConfiguration;

/**
 * Container for relying party group information.
 */
public class RelyingPartyGroup {

    /** Anonymous relying party config for the group. */
    private RelyingPartyConfiguration anonymousRP;

    /** Default relying party config for the group. */
    private RelyingPartyConfiguration defaultRP;

    /** Relying party config for the group. */
    private List<RelyingPartyConfiguration> relyingParties;

    /** Metadata provider for the group. */
    private MetadataProvider metadataProvider;
    
    /** Security policies for the group. */
    private List<SecurityPolicy> securityPolicies;
    
    /** Trust engines for the group. */
    private List<TrustEngine> trustEngines;

    /** Credentials for the group. */
    private List<Credential> groupCredentials;
    
    /**
     * Gets the anonymous relying party config for the group.
     * 
     * @return anonymous relying party config for the group
     */
    public RelyingPartyConfiguration getAnonymousRP() {
        return anonymousRP;
    }
    
    /**
     * Gets the credentials for the group.
     * 
     * @return credentials for the group
     */
    public List<Credential> getCredentials() {
        return groupCredentials;
    }
    
    /**
     * Gets the default relying party for the group.
     * 
     * @return default relying party for the group
     */
    public RelyingPartyConfiguration getDefaultRP() {
        return defaultRP;
    }
    
    /**
     * Gets the metadata provider for the group.
     * 
     * @return metadata provider for the group
     */
    public MetadataProvider getMetadataProvider() {
        return metadataProvider;
    }

    /**
     * Gets the relying party configurations for the group.
     * 
     * @return relying party configurations for the group
     */
    public List<RelyingPartyConfiguration> getRelyingParties() {
        return relyingParties;
    }

    /**
     * Gets the security policies for the group.
     * 
     * @return security policies for the group
     */
    public List<SecurityPolicy> getSecurityPolicies() {
        return securityPolicies;
    }

    /**
     * Gets the trust engines for the group.
     * 
     * @return trust engines for the group
     */
    public List<TrustEngine> getTrustEngines() {
        return trustEngines;
    }

    /**
     * Sets the anonymous relying party config for the group.
     * 
     * @param config anonymous relying party config for the group
     */
    public void setAnonymousRP(RelyingPartyConfiguration config) {
        anonymousRP = config;
    }

    /**
     * Sets the credentials for the group.
     * 
     * @param credentials credentials for the group
     */
    public void setCredentials(List<Credential> credentials) {
        groupCredentials = credentials;
    }

    /**
     * Sets the default relying party for the group.
     * 
     * @param config default relying party for the group
     */
    public void setDefaultRP(RelyingPartyConfiguration config) {
        defaultRP = config;
    }

    /**
     * Sets the metadata provider for the group.
     * 
     * @param provider metadata provider for the group
     */
    public void setMetadataProvider(MetadataProvider provider) {
        metadataProvider = provider;
    }

    /**
     * Sets the relying party configurations for the group.
     * 
     * @param configurations relying party configurations for the group
     */
    public void setRelyingParties(List<RelyingPartyConfiguration> configurations) {
        relyingParties = configurations;
    }

    /**
     * Sets the security policies for the group.
     * 
     * @param policies security policies for the group
     */
    public void setSecurityPolicies(List<SecurityPolicy> policies) {
        securityPolicies = policies;
    }

    /**
     * Sets the trust engines for the group.
     * 
     * @param engines trust engines for the group
     */
    public void setTrustEngines(List<TrustEngine> engines) {
        trustEngines = engines;
    }
}