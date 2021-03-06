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

package edu.internet2.middleware.shibboleth.common.config.attribute.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base class for Spring bean definition parser for Shibboleth resolver plug-ins.
 */
public abstract class AbstractResolutionPlugInBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /** Name of resolution plug-in dependency. */
    public static final QName DEPENDENCY_ELEMENT_NAME = new QName(AttributeResolverNamespaceHandler.NAMESPACE,
            "Dependency");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractResolutionPlugInBeanDefinitionParser.class);

    /**
     * Parses the plugins ID and attribute definition and data connector dependencies.
     * 
     * {@inheritDoc}
     */
    protected final void doParse(Element config, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String pluginId = DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "id"));
        log.info("Parsing configuration for {} plugin with ID: {}", config.getLocalName(), pluginId);
        builder.addPropertyValue("pluginId", pluginId);

        Map<QName, List<Element>> children = XMLHelper.getChildElements(config);

        List<String> dependencyIds = parseDependencies(children.get(DEPENDENCY_ELEMENT_NAME));
        if(dependencyIds != null && !dependencyIds.isEmpty()){
            log.debug("Dependencies for plugin {}: {}", pluginId, dependencyIds);
            builder.addPropertyValue("dependencyIds", dependencyIds);
        }else{
            log.debug("Dependencies for plugin {}: none", pluginId);
        }

        doParse(pluginId, config, children, builder, parserContext);
    }

    /**
     * Parses the plugin configuration.
     * 
     * @param pluginId unique ID of the plugin
     * @param pluginConfig root plugin configuration element
     * @param pluginConfigChildren immediate children of the root configuration element (provided to save from having to
     *            reparse them)
     * @param pluginBuilder bean definition builder for the plugin
     * @param parserContext current parsing context
     */
    protected abstract void doParse(String pluginId, Element pluginConfig,
            Map<QName, List<Element>> pluginConfigChildren, BeanDefinitionBuilder pluginBuilder,
            ParserContext parserContext);

    /** {@inheritDoc} */
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        return element.getAttributeNS(null, "id");
    }

    /**
     * Parse dependency elements.
     * 
     * @param elements DOM elements of type <code>resolver:PluginDependencyType</code>
     * 
     * @return the dependency IDs
     */
    protected List<String> parseDependencies(List<Element> elements) {
        if (elements == null || elements.size() == 0) {
            return null;
        }

        List<String> dependencyIds = new ArrayList<String>();
        for (Element dependency : elements) {
            dependencyIds.add(DatatypeHelper.safeTrimOrNullString(dependency.getAttributeNS(null, "ref")));
        }

        return dependencyIds;
    }
}