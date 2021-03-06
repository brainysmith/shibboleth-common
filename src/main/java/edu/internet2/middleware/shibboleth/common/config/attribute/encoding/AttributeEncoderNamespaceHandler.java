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

package edu.internet2.middleware.shibboleth.common.config.attribute.encoding;

import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;

/**
 * Spring namespace handler for the Shibboleth encoder namespace.
 */
public class AttributeEncoderNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Namespace for this handler. */
    public static final String NAMESPACE = "urn:mace:shibboleth:2.0:attribute:encoder";

    /** {@inheritDoc} */
    public void init() {
        registerBeanDefinitionParser(SAML1StringAttributeEncoderBeanDefinitionParser.TYPE_NAME,
                new SAML1StringAttributeEncoderBeanDefinitionParser());

        registerBeanDefinitionParser(SAML1Base64AttributeEncoderBeanDefinitionParser.TYPE_NAME,
                new SAML1Base64AttributeEncoderBeanDefinitionParser());

        registerBeanDefinitionParser(SAML1ScopedStringAttributeEncoderBeanDefinitionParser.TYPE_NAME,
                new SAML1ScopedStringAttributeEncoderBeanDefinitionParser());

        registerBeanDefinitionParser(SAML1XMLObjectAttributeEncoderBeanDefinitionParser.TYPE_NAME,
                new SAML1XMLObjectAttributeEncoderBeanDefinitionParser());

        registerBeanDefinitionParser(SAML1StringNameIdentifierEncoderBeanDefinitionParser.SCHEMA_TYPE,
                new SAML1StringNameIdentifierEncoderBeanDefinitionParser());

        registerBeanDefinitionParser(SAML2StringAttributeEncoderBeanDefinitionParser.TYPE_NAME,
                new SAML2StringAttributeEncoderBeanDefinitionParser());

        registerBeanDefinitionParser(SAML2ScopedStringAttributeEncoderBeanDefinitionParser.TYPE_NAME,
                new SAML2ScopedStringAttributeEncoderBeanDefinitionParser());

        registerBeanDefinitionParser(SAML2Base64AttributeEncoderBeanDefinitionParser.TYPE_NAME,
                new SAML2Base64AttributeEncoderBeanDefinitionParser());

        registerBeanDefinitionParser(SAML2XMLObjectAttributeEncoderBeanDefinitionParser.TYPE_NAME,
                new SAML2XMLObjectAttributeEncoderBeanDefinitionParser());

        registerBeanDefinitionParser(SAML2StringNameIDEncoderBeanDefinitionParser.SCHEMA_TYPE,
                new SAML2StringNameIDEncoderBeanDefinitionParser());

    }
}