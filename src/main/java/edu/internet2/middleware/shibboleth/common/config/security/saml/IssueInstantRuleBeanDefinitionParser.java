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

package edu.internet2.middleware.shibboleth.common.config.security.saml;

import javax.xml.namespace.QName;

import org.opensaml.common.binding.security.IssueInstantRule;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

/**
 * Spring bean definition parser for issue instant rules.
 */
public class IssueInstantRuleBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /** Schema type. */
    public static final QName SCHEMA_TYPE = new QName(SAMLSecurityNamespaceHandler.NAMESPACE, "IssueInstant");

    /** {@inheritDoc} */
    protected Class getBeanClass(Element element) {
        return IssueInstantRule.class;
    }

    /** {@inheritDoc} */
    protected boolean shouldGenerateId() {
        return true;
    }

    /** {@inheritDoc} */
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        long skew = 300;
        if (element.hasAttributeNS(null, "clockSkew")) {
            skew = SpringConfigurationUtils.parseDurationToMillis("'clockSkew' on security rule of type "
                    + XMLHelper.getXSIType(element), element.getAttributeNS(null, "clockSkew"), 1000) / 1000;
        }
        builder.addConstructorArgValue(skew);

        long expirationThreshold = 60;
        if (element.hasAttributeNS(null, "expirationThreshold")) {
            expirationThreshold = SpringConfigurationUtils.parseDurationToMillis(
                    "'expirationThreshold' on security rule of type " + XMLHelper.getXSIType(element), element
                            .getAttributeNS(null, "expirationThreshold"), 1000) / 1000;
        }
        builder.addConstructorArgValue(expirationThreshold);

        if (element.hasAttributeNS(null, "required")) {
            builder.addPropertyValue("requiredRule", XMLHelper.getAttributeValueAsBoolean(element.getAttributeNodeNS(
                    null, "required")));
        } else {
            builder.addPropertyValue("requiredRule", true);
        }
    }
}