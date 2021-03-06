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

package edu.internet2.middleware.shibboleth.common.config.attribute.filtering;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

/**
 * Spring bean definition parser for Shibboleth attribute filtering engine attribute filter policy.
 */
public class AttributeFilterPolicyGroupBeanDefinitionParser implements BeanDefinitionParser {

    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(AttributeFilterNamespaceHandler.NAMESPACE,
            "AttributeFilterPolicyGroup");

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(AttributeFilterNamespaceHandler.NAMESPACE,
            "AttributeFilterPolicyGroupType");

    /** Local name of the policy requirement element. */
    public static final QName POLICY_REQUIREMENT_ELEMENT_NAME = new QName(AttributeFilterNamespaceHandler.NAMESPACE,
            "PolicyRequirement");

    /** Local name of the value filter element. */
    public static final QName PERMIT_VALUE_ELEMENT_NAME = new QName(AttributeFilterNamespaceHandler.NAMESPACE,
            "PermitValue");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AttributeFilterPolicyGroupBeanDefinitionParser.class);

    /** {@inheritDoc} */
    public BeanDefinition parse(Element config, ParserContext context) {
        String policyId = DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "id"));

        log.debug("Parsing attribute filter policy group {}", policyId);

        List<Element> children;
        Map<QName, List<Element>> childrenMap = XMLHelper.getChildElements(config);

        children = childrenMap.get(new QName(AttributeFilterNamespaceHandler.NAMESPACE, "PolicyRequirementRule"));
        SpringConfigurationUtils.parseInnerCustomElements(children, context);

        children = childrenMap.get(new QName(AttributeFilterNamespaceHandler.NAMESPACE, "AttributeRule"));
        SpringConfigurationUtils.parseInnerCustomElements(children, context);

        children = childrenMap.get(new QName(AttributeFilterNamespaceHandler.NAMESPACE, "PermitValueRule"));
        SpringConfigurationUtils.parseInnerCustomElements(children, context);

        children = childrenMap.get(new QName(AttributeFilterNamespaceHandler.NAMESPACE, "AttributeFilterPolicy"));
        SpringConfigurationUtils.parseInnerCustomElements(children, context);

        return null;
    }
}