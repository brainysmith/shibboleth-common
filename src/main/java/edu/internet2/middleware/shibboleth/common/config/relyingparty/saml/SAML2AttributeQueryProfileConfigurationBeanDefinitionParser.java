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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.relyingparty.provider.CryptoOperationRequirementLevel;

/**
 * Spring configuration parser for SAML 2 attribute query configurations.
 */
public class SAML2AttributeQueryProfileConfigurationBeanDefinitionParser extends
        AbstractSAML2ProfileConfigurationBeanDefinitionParser {

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(SAMLRelyingPartyNamespaceHandler.NAMESPACE,
            "SAML2AttributeQueryProfile");

    /** {@inheritDoc} */
    protected Class getBeanClass(Element element) {
        return SAML2AttributeQueryProfileConfigurationFactoryBean.class;
    }

    /** {@inheritDoc} */
    protected CryptoOperationRequirementLevel getSignAssertionsDefault() {
        return CryptoOperationRequirementLevel.never;
    }

    /** {@inheritDoc} */
    protected CryptoOperationRequirementLevel getSignResponsesDefault() {
        return CryptoOperationRequirementLevel.conditional;
    }
}