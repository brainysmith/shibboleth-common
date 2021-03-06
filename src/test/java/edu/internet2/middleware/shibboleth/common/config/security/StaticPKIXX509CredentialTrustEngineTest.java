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

import java.util.Iterator;
import java.util.Set;

import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.trust.TrustEngine;
import org.opensaml.xml.security.x509.PKIXValidationInformation;
import org.opensaml.xml.security.x509.PKIXX509CredentialTrustEngine;
import org.opensaml.xml.security.x509.StaticPKIXValidationInformationResolver;
import org.springframework.context.ApplicationContext;

import edu.internet2.middleware.shibboleth.common.config.BaseConfigTestCase;

/**
 * Test that the configuration code for static PKIX X509 credential trust engine works correctly.
 */
public class StaticPKIXX509CredentialTrustEngineTest extends BaseConfigTestCase {

    /**
     * Test configuring a trust engine with Spring.
     * 
     * @throws Exception thrown if there is a problem
     */
    public void testBasicInstantiation() throws Exception {
        ApplicationContext appContext = createSpringContext(new String[] { DATA_PATH + "/config/base-config.xml",
                DATA_PATH + "/config/security/StaticPKIXX509CredentialTrustEngine1.xml", });

        TrustEngine trustEngine = (TrustEngine) appContext.getBean("StaticPKIXX509CredentialTrustEngine");
        assertNotNull(trustEngine);
        assertTrue(trustEngine instanceof PKIXX509CredentialTrustEngine);
        PKIXX509CredentialTrustEngine pkixEngine = (PKIXX509CredentialTrustEngine) trustEngine;
        
        CriteriaSet criteriaSet = new CriteriaSet();
        StaticPKIXValidationInformationResolver resolver = (StaticPKIXValidationInformationResolver)pkixEngine.getPKIXResolver();
        Set<String> trustedNames = resolver.resolveTrustedNames(criteriaSet);
        
        assertEquals("Incorrect number of trusted names", 3, trustedNames.size());
        assertTrue("Missing expected trusted name", trustedNames.contains("FOO"));
        assertTrue("Missing expected trusted name", trustedNames.contains("BAR"));
        assertTrue("Missing expected trusted name", trustedNames.contains("BAZ"));
        
        Iterator<PKIXValidationInformation> pkixIter = resolver.resolve(criteriaSet).iterator();
        assertTrue(pkixIter.hasNext());
        PKIXValidationInformation pkixInfoSet = pkixIter.next();
        assertEquals("Incorrect number of certs", 1, pkixInfoSet.getCertificates().size());
        assertEquals("Incorrect number of CRLs", 0, pkixInfoSet.getCRLs().size());
        assertEquals("Incorrect verify depth", new Integer(5), pkixInfoSet.getVerificationDepth());
        assertFalse("Too many PKIX validation info sets", pkixIter.hasNext());
    }

    /**
     * Test configuring a trust engine with an invalid Spring configuration.
     * 
     * @throws Exception thrown if there is a problem
     */
    public void testFailedInstantiation() throws Exception {
        String[] configs = { "/config/base-config.xml",
                DATA_PATH + "/config/security/StaticPKIXX509CredentialTrustEngine2.xml", };
        try {
            createSpringContext(configs);
            fail("Spring loaded invalid configuration");
        } catch (Exception e) {
            // expected
        }
    }
}