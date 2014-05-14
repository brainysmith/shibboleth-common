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

package edu.internet2.middleware.shibboleth.common.config.security;

import org.opensaml.xml.security.trust.TrustEngine;
import org.opensaml.xml.security.x509.CertPathPKIXValidationOptions;
import org.opensaml.xml.security.x509.PKIXX509CredentialTrustEngine;
import org.opensaml.xml.signature.impl.PKIXSignatureTrustEngine;
import org.springframework.context.ApplicationContext;

import edu.internet2.middleware.shibboleth.common.config.BaseConfigTestCase;

/**
 * Test that the configuration code for wiring PKIXValidationOptions into relevant trust engines is correct.
 */
public class PKIXValidationOptionsTrustEngineTest extends BaseConfigTestCase {

    /**
     * Test the StaticPKIXSignature TrustEngine element.
     * 
     * @throws Exception thrown if there is a problem
     */
    public void testStaticSignatureEngine() throws Exception {
        ApplicationContext appContext = createSpringContext(new String[] { DATA_PATH + "/config/base-config.xml",
                DATA_PATH + "/config/security/StaticPKIXSignatureTrustEngine-Options.xml", });

        TrustEngine trustEngine = (TrustEngine) appContext.getBean("StaticPKIXSignatureTrustEngine");
        assertNotNull(trustEngine);
        assertTrue(trustEngine instanceof PKIXSignatureTrustEngine);
        PKIXSignatureTrustEngine pkixEngine = (PKIXSignatureTrustEngine) trustEngine;
        
        CertPathPKIXValidationOptions pkixOptions = 
            (CertPathPKIXValidationOptions) pkixEngine.getPKIXTrustEvaluator().getPKIXValidationOptions();
        assertNotNull(pkixOptions);
        
        assertEquals(false, pkixOptions.isProcessEmptyCRLs());
        assertEquals(false, pkixOptions.isProcessExpiredCRLs());
        assertEquals(false, pkixOptions.isProcessCredentialCRLs());
        assertEquals(new Integer(3), pkixOptions.getDefaultVerificationDepth());
        assertEquals(true, pkixOptions.isForceRevocationEnabled());
        assertEquals(false, pkixOptions.isRevocationEnabled());
    }
    
    /**
     * Test the StaticPKIXX509Credential TrustEngine element.
     * 
     * @throws Exception thrown if there is a problem
     */
    public void testStaticX509CredentialEngine() throws Exception {
        ApplicationContext appContext = createSpringContext(new String[] { DATA_PATH + "/config/base-config.xml",
                DATA_PATH + "/config/security/StaticPKIXX509CredentialTrustEngine-Options.xml", });

        TrustEngine trustEngine = (TrustEngine) appContext.getBean("StaticPKIXX509CredentialTrustEngine");
        assertNotNull(trustEngine);
        assertTrue(trustEngine instanceof PKIXX509CredentialTrustEngine);
        PKIXX509CredentialTrustEngine pkixEngine = (PKIXX509CredentialTrustEngine) trustEngine;
        
        CertPathPKIXValidationOptions pkixOptions = 
            (CertPathPKIXValidationOptions) pkixEngine.getPKIXTrustEvaluator().getPKIXValidationOptions();
        assertNotNull(pkixOptions);
        
        assertEquals(false, pkixOptions.isProcessEmptyCRLs());
        assertEquals(false, pkixOptions.isProcessExpiredCRLs());
        assertEquals(false, pkixOptions.isProcessCredentialCRLs());
        assertEquals(new Integer(3), pkixOptions.getDefaultVerificationDepth());
        assertEquals(true, pkixOptions.isForceRevocationEnabled());
        assertEquals(false, pkixOptions.isRevocationEnabled());
    }
    
    /**
     * Test the MetadataPKIXX509Credential TrustEngine element.
     * 
     * @throws Exception thrown if there is a problem
     */
    public void testMetadataX509CredentialEngine() throws Exception {
        ApplicationContext appContext = createSpringContext(new String[] { DATA_PATH + "/config/base-config.xml",
                DATA_PATH + "/config/security/MetadataPKIXTrustEngines-Options.xml", });

        TrustEngine trustEngine = (TrustEngine) appContext.getBean("MetadataPKIXX509CredentialTrustEngine");
        assertNotNull(trustEngine);
        assertTrue(trustEngine instanceof PKIXX509CredentialTrustEngine);
        PKIXX509CredentialTrustEngine pkixEngine = (PKIXX509CredentialTrustEngine) trustEngine;
        
        CertPathPKIXValidationOptions pkixOptions = 
            (CertPathPKIXValidationOptions) pkixEngine.getPKIXTrustEvaluator().getPKIXValidationOptions();
        assertNotNull(pkixOptions);
        
        assertEquals(false, pkixOptions.isProcessEmptyCRLs());
        assertEquals(false, pkixOptions.isProcessExpiredCRLs());
        assertEquals(false, pkixOptions.isProcessCredentialCRLs());
        assertEquals(new Integer(3), pkixOptions.getDefaultVerificationDepth());
        assertEquals(true, pkixOptions.isForceRevocationEnabled());
        assertEquals(false, pkixOptions.isRevocationEnabled());
    }
    
    /**
     * Test the MetadataPKIXSignature TrustEngine element.
     * 
     * @throws Exception thrown if there is a problem
     */
    public void testMetadataSignatureEngine() throws Exception {
        ApplicationContext appContext = createSpringContext(new String[] { DATA_PATH + "/config/base-config.xml",
                DATA_PATH + "/config/security/MetadataPKIXTrustEngines-Options.xml", });

        TrustEngine trustEngine = (TrustEngine) appContext.getBean("MetadataPKIXSignatureTrustEngine");
        assertNotNull(trustEngine);
        assertTrue(trustEngine instanceof PKIXSignatureTrustEngine);
        PKIXSignatureTrustEngine pkixEngine = (PKIXSignatureTrustEngine) trustEngine;
        
        CertPathPKIXValidationOptions pkixOptions = 
            (CertPathPKIXValidationOptions) pkixEngine.getPKIXTrustEvaluator().getPKIXValidationOptions();
        assertNotNull(pkixOptions);
        
        assertEquals(false, pkixOptions.isProcessEmptyCRLs());
        assertEquals(false, pkixOptions.isProcessExpiredCRLs());
        assertEquals(false, pkixOptions.isProcessCredentialCRLs());
        assertEquals(new Integer(3), pkixOptions.getDefaultVerificationDepth());
        assertEquals(true, pkixOptions.isForceRevocationEnabled());
        assertEquals(false, pkixOptions.isRevocationEnabled());
    }
}