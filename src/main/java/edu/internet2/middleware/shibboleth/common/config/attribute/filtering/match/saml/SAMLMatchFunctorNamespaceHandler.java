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

package edu.internet2.middleware.shibboleth.common.config.attribute.filtering.match.saml;

import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;

/**
 * Namespace handler for the attribute filter engine SAML match functions.
 */
public class SAMLMatchFunctorNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Basic match function namespace. */
    public static final String NAMESPACE = "urn:mace:shibboleth:2.0:afp:mf:saml";

    /** {@inheritDoc} */
    public void init() {
        registerBeanDefinitionParser(AttributeRequesterInEntityGroupMatchFunctorBeanDefinitionParser.SCHEMA_TYPE,
                new AttributeRequesterInEntityGroupMatchFunctorBeanDefinitionParser());

        registerBeanDefinitionParser(AttributeIssuerInEntityGroupMatchFunctorBeanDefinitionParser.SCHEMA_TYPE,
                new AttributeIssuerInEntityGroupMatchFunctorBeanDefinitionParser());

        registerBeanDefinitionParser(AttributeIssuerEntityAttributeExactMatchFunctorBeanDefinitionParser.SCHEMA_TYPE,
                new AttributeIssuerEntityAttributeExactMatchFunctorBeanDefinitionParser());

        registerBeanDefinitionParser(
                AttributeRequesterEntityAttributeExactMatchFunctorBeanDefinitionParser.SCHEMA_TYPE,
                new AttributeRequesterEntityAttributeExactMatchFunctorBeanDefinitionParser());

        registerBeanDefinitionParser(AttributeIssuerEntityAttributeRegexMatchFunctorBeanDefinitionParser.SCHEMA_TYPE,
                new AttributeIssuerEntityAttributeRegexMatchFunctorBeanDefinitionParser());

        registerBeanDefinitionParser(
                AttributeRequesterEntityAttributeRegexMatchFunctorBeanDefinitionParser.SCHEMA_TYPE,
                new AttributeRequesterEntityAttributeRegexMatchFunctorBeanDefinitionParser());

        registerBeanDefinitionParser(AttributeIssuerNameIDFormatExactMatchFunctorBeanDefinitionParser.SCHEMA_TYPE,
                new AttributeIssuerNameIDFormatExactMatchFunctorBeanDefinitionParser());

        registerBeanDefinitionParser(AttributeRequesterNameIDFormatExactMatchFunctorBeanDefinitionParser.SCHEMA_TYPE,
                new AttributeRequesterNameIDFormatExactMatchFunctorBeanDefinitionParser());
    }
}