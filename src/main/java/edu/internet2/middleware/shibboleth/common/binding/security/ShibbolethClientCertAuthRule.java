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

package edu.internet2.middleware.shibboleth.common.binding.security;

import java.util.Set;

import org.opensaml.common.binding.security.SAMLMDClientCertAuthRule;
import org.opensaml.ws.security.provider.CertificateNameOptions;
import org.opensaml.xml.security.trust.TrustEngine;
import org.opensaml.xml.security.x509.X500DNHandler;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.security.x509.X509Util;

/**
 * Specialization of {@link SAMLMDClientCertAuthRule} which may include Shibboleth-specific
 * method overrides for client certificate authentication processing.
 */
public class ShibbolethClientCertAuthRule extends SAMLMDClientCertAuthRule {

    /**
     * Constructor.
     *
     * @param engine Trust engine used to verify the request X509Credential
     * @param nameOptions options for deriving issuer names from an X.509 certificate
     */
    public ShibbolethClientCertAuthRule(TrustEngine<X509Credential> engine, CertificateNameOptions nameOptions) {
        super(engine, nameOptions);
    }
    
    /**
     * Constructor.  The certificate name issuer derivation options are defaulted
     * to be consistent with the Shibboleth 1.3 identity provider.
     *
     * @param engine Trust engine used to verify the request X509Credential
     */
    public ShibbolethClientCertAuthRule(TrustEngine<X509Credential> engine) {
        super(engine, new CertificateNameOptions());
        
        CertificateNameOptions nameOptions = getCertificateNameOptions();
        
        // This is the behavior used by the Shibboleth 1.3 IdP.
        nameOptions.setX500SubjectDNFormat(X500DNHandler.FORMAT_RFC2253);
        nameOptions.setEvaluateSubjectDN(true);
        nameOptions.setEvaluateSubjectCommonName(true);
        Set<Integer> altNameTypes = nameOptions.getSubjectAltNames();
        altNameTypes.add(X509Util.DNS_ALT_NAME);
        altNameTypes.add(X509Util.URI_ALT_NAME);
    }
    
}