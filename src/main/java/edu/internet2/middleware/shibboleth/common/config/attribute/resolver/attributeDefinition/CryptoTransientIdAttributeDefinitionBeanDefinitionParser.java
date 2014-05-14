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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

/**
 * Spring bean definition parser for {@link CryptoTransientIdAttributeDefinitionFactoryBean}s.
 */
public class CryptoTransientIdAttributeDefinitionBeanDefinitionParser extends BaseAttributeDefinitionBeanDefinitionParser {

    /** Schema type name. */
    public static final QName TYPE_NAME = new QName(AttributeDefinitionNamespaceHandler.NAMESPACE, "CryptoTransientId");

    /** {@inheritDoc} */
    protected Class<CryptoTransientIdAttributeDefinitionFactoryBean> getBeanClass(Element element) {
        return CryptoTransientIdAttributeDefinitionFactoryBean.class;
    }

    /** {@inheritDoc} */
    protected void doParse(String pluginId, Element pluginConfig, Map<QName, List<Element>> pluginConfigChildren,
            BeanDefinitionBuilder pluginBuilder, ParserContext parserContext) {
        super.doParse(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

        if (pluginConfig.hasAttributeNS(null, "lifetime")) {
            long lifetime  = SpringConfigurationUtils.parseDurationToMillis("'lifetime' on AttributeDefinition of type "
                    + XMLHelper.getXSIType(pluginConfig), pluginConfig.getAttributeNS(null, "lifetime"), 1000 * 60);
            pluginBuilder.addPropertyValue("idLifetime", lifetime);
        }
        
        pluginBuilder.addPropertyReference("dataSealer", DatatypeHelper.safeTrimOrNullString(pluginConfig
                .getAttributeNS(null, "dataSealerRef")));
    }
}
