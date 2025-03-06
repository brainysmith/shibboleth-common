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

import org.opensaml.xml.security.x509.PKIXValidationOptions;
import org.springframework.context.ApplicationContext;

import edu.internet2.middleware.shibboleth.common.config.BaseConfigTestCase;

/**
 * Test that the configuration code for PKIXValidationOptions works correctly.
 */
public class PKIXValidationOptionsTest extends BaseConfigTestCase {

    /**
     * Test configuring basic options with default values.
     * 
     * @throws Exception thrown if there is a problem
     */
    public void testDefaultValues() throws Exception {
        ApplicationContext appContext = createSpringContext(new String[] { DATA_PATH + "/config/base-config.xml",
                DATA_PATH + "/config/security/PKIXValidationOptions1.xml", });

        PKIXValidationOptions pkixOptions = 
            (PKIXValidationOptions) appContext.getBeansOfType(PKIXValidationOptions.class).values().iterator().next();
        assertNotNull(pkixOptions);
        
        assertEquals(true, pkixOptions.isProcessEmptyCRLs());
        assertEquals(true, pkixOptions.isProcessExpiredCRLs());
        assertEquals(true, pkixOptions.isProcessCredentialCRLs());
        assertEquals(new Integer(1), pkixOptions.getDefaultVerificationDepth());
    }
    
    /**
     * Test configuring basic options with default values and xsi:type.
     * 
     * @throws Exception thrown if there is a problem
     */
    public void testDefaultValuesWithType() throws Exception {
        ApplicationContext appContext = createSpringContext(new String[] { DATA_PATH + "/config/base-config.xml",
                DATA_PATH + "/config/security/PKIXValidationOptions2.xml", });

        PKIXValidationOptions pkixOptions = 
            (PKIXValidationOptions) appContext.getBeansOfType(PKIXValidationOptions.class).values().iterator().next();
        assertNotNull(pkixOptions);
        
        assertEquals(true, pkixOptions.isProcessEmptyCRLs());
        assertEquals(true, pkixOptions.isProcessExpiredCRLs());
        assertEquals(true, pkixOptions.isProcessCredentialCRLs());
        assertEquals(new Integer(1), pkixOptions.getDefaultVerificationDepth());
    }
    
    /**
     * Test configuring basic options with non-default values.
     * 
     * @throws Exception thrown if there is a problem
     */
    public void testNonDefaultValues() throws Exception {
        ApplicationContext appContext = createSpringContext(new String[] { DATA_PATH + "/config/base-config.xml",
                DATA_PATH + "/config/security/PKIXValidationOptions3.xml", });

        PKIXValidationOptions pkixOptions = 
            (PKIXValidationOptions) appContext.getBeansOfType(PKIXValidationOptions.class).values().iterator().next();
        assertNotNull(pkixOptions);
        
        assertEquals(false, pkixOptions.isProcessEmptyCRLs());
        assertEquals(false, pkixOptions.isProcessExpiredCRLs());
        assertEquals(false, pkixOptions.isProcessCredentialCRLs());
        assertEquals(new Integer(3), pkixOptions.getDefaultVerificationDepth());
    }

    /**
     * Test configuring an element with an invalid Spring configuration.
     * 
     * @throws Exception thrown if there is a problem
     */
    public void testFailedInstantiation() throws Exception {
        String[] configs = { "/config/base-config.xml",
                DATA_PATH + "/config/security/PKIXValidationOptions4.xml", };
        try {
            createSpringContext(configs);
            fail("Spring loaded invalid configuration");
        } catch (Exception e) {
            // expected
        }
    }
}