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

package edu.internet2.middleware.shibboleth.common.config.attribute.filtering.match.basic;

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.match.basic.OrMatchFunctor;
import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;
import edu.internet2.middleware.shibboleth.common.config.attribute.filtering.BaseFilterBeanDefinitionParser;

/**
 * Bean definition parser for {@link OrMatchFunctor} objects.
 */
public class OrMatchFunctorBeanDefinitionParser extends BaseFilterBeanDefinitionParser {

    /** Schema type. */
    public static final QName SCHEMA_TYPE = new QName(BasicMatchFunctorNamespaceHandler.NAMESPACE, "OR");

    /** {@inheritDoc} */
    protected Class getBeanClass(Element arg0) {
        return OrMatchFunctor.class;
    }

    /** {@inheritDoc} */
    protected void doParse(Element configElement, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(configElement, parserContext, builder);

        List<Element> ruleElements = XMLHelper.getChildElementsByTagNameNS(configElement,
                BasicMatchFunctorNamespaceHandler.NAMESPACE, "Rule");

        builder.addConstructorArgValue(SpringConfigurationUtils.parseInnerCustomElements(ruleElements, parserContext));
        
        ruleElements = XMLHelper.getChildElementsByTagNameNS(configElement,
                BasicMatchFunctorNamespaceHandler.NAMESPACE, "RuleReference");
        if(!ruleElements.isEmpty()){
            throw new BeanCreationException("RuleReference elements within an OR rule are not supported");
        }
    }
}