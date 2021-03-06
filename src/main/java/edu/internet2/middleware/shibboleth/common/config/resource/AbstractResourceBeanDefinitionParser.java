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

package edu.internet2.middleware.shibboleth.common.config.resource;

import java.util.List;

import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

/** Base class {@link org.opensaml.util.resource.Resource} for bean definition parsers. */
public abstract class AbstractResourceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected BeanDefinition addResourceFilter(Element element, ParserContext parserContext,
            BeanDefinitionBuilder builder) {
        List<Element> resFilter = XMLHelper.getChildElementsByTagNameNS(element, ResourceNamespaceHandler.NAMESPACE,
                "ResourceFilter");
        if (!resFilter.isEmpty()) {
            builder.addPropertyValue("resourceFilter", SpringConfigurationUtils.parseInnerCustomElement(resFilter.get(0), parserContext));
        }
        return null;
    }
}