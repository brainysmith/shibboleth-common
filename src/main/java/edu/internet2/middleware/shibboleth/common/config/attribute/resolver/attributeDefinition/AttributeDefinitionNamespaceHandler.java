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

package edu.internet2.middleware.shibboleth.common.config.attribute.resolver.attributeDefinition;

import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;

/**
 * Spring namespace handler for the Shibboleth simple attribute definition namespace.
 */
public class AttributeDefinitionNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Namespace for this handler. */
    public static final String NAMESPACE = "urn:mace:shibboleth:2.0:resolver:ad";

    /** {@inheritDoc} */
    public void init() {
        registerBeanDefinitionParser(SimpleAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new SimpleAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(ScopedAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new ScopedAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(PrescopedAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new PrescopedAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(RegexSplitAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new RegexSplitAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(SAML1NameIdentifierAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new SAML1NameIdentifierAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(SAML2NameIDAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new SAML2NameIDAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(ScriptedAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new ScriptedAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(PrincipalNameAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new PrincipalNameAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(PrincipalAuthenticationMethodAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new PrincipalAuthenticationMethodAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(MappedAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new MappedAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(TransientIdAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new TransientIdAttributeDefinitionBeanDefinitionParser());

        registerBeanDefinitionParser(CryptoTransientIdAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new CryptoTransientIdAttributeDefinitionBeanDefinitionParser());
        
        registerBeanDefinitionParser(TemplateAttributeDefinitionBeanDefinitionParser.TYPE_NAME,
                new TemplateAttributeDefinitionBeanDefinitionParser());
    }
}