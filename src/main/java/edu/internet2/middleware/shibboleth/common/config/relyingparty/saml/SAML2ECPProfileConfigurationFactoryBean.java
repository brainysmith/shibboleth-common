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

package edu.internet2.middleware.shibboleth.common.config.relyingparty.saml;

import edu.internet2.middleware.shibboleth.common.relyingparty.provider.CryptoOperationRequirementLevel;
import edu.internet2.middleware.shibboleth.common.relyingparty.provider.saml2.ECPConfiguration;

/** Spring factory for ECP SAML 2 SSO profile configurations. */
public class SAML2ECPProfileConfigurationFactoryBean extends SAML2SSOProfileConfigurationFactoryBean {

    // We convert CryptoOperationRequirementLevel.conditional to CryptoOperationRequirementLevel.always because
    // in front-channel ECP only always/never make sense

    /** {@inheritDoc} */
    public void setEncryptAssertions(CryptoOperationRequirementLevel encrypt) {
        if (encrypt == CryptoOperationRequirementLevel.conditional) {
            super.setEncryptAssertions(CryptoOperationRequirementLevel.always);
        } else {
            super.setEncryptAssertions(encrypt);
        }
    }

    /** {@inheritDoc} */
    public void setEncryptNameIds(CryptoOperationRequirementLevel encrypt) {
        if (encrypt == CryptoOperationRequirementLevel.conditional) {
            super.setEncryptNameIds(CryptoOperationRequirementLevel.always);
        } else {
            super.setEncryptNameIds(encrypt);
        }
    }

    /** {@inheritDoc} */
    public void setSignAssertions(CryptoOperationRequirementLevel sign) {
        if (sign == CryptoOperationRequirementLevel.conditional) {
            super.setSignAssertions(CryptoOperationRequirementLevel.always);
        } else {
            super.setSignAssertions(sign);
        }
    }

    /** {@inheritDoc} */
    public void setSignResponses(CryptoOperationRequirementLevel sign) {
        if (sign == CryptoOperationRequirementLevel.conditional) {
            super.setSignResponses(CryptoOperationRequirementLevel.always);
        } else {
            super.setSignResponses(sign);
        }
    }

    /** {@inheritDoc} */
    public Class getObjectType() {
        return ECPConfiguration.class;
    }
}
