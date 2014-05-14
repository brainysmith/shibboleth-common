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

package edu.internet2.middleware.shibboleth.common.config.metadata;

import java.util.List;

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

/** Base class for metadata provider configuration parser. */
public abstract class BaseMetadataProviderBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(BaseMetadataProviderBeanDefinitionParser.class);

    /** {@inheritDoc} */
    protected void doParse(Element config, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String id = getProviderId(config);
        log.debug("Parsing configuration for '{}' metadata provider with ID: {}", XMLHelper.getXSIType(config)
                .getLocalPart(), id);

        boolean requireValidMetadata = getRequireValidMetadata(config);
        log.debug("Metadata provider requires valid metadata: {}", requireValidMetadata);
        builder.addPropertyValue("requireValidMetadata", requireValidMetadata);

        List<Element> childElems = XMLHelper.getChildElementsByTagNameNS(config, MetadataNamespaceHandler.NAMESPACE,
                "MetadataFilter");
        if (childElems.size() > 0) {
            builder.addPropertyValue("metadataFilter", SpringConfigurationUtils.parseInnerCustomElement(
                    (Element) childElems.get(0), parserContext));
        }
    }

    /**
     * Gets the valid metadata requirement for the metadata provider.
     * 
     * @param config metadata provider configuration
     * 
     * @return valid metadata requirement for the metadata provider
     */
    protected boolean getRequireValidMetadata(Element config) {
        boolean requireValidMetadata = true;

        if (config.hasAttributeNS(null, "maintainExpiredMetadata")) {
            boolean maintainedExpiredMetadata = XMLHelper.getAttributeValueAsBoolean(config.getAttributeNodeNS(null,
                    "maintainExpiredMetadata"));
            requireValidMetadata = !maintainedExpiredMetadata;
            log.warn("Use of metadata provider configuration attribute 'maintainExpiredMetadata' is deprecated.  Use requireValidMetadata=\"{}\" instead.",
                            requireValidMetadata);
        }

        if (config.hasAttributeNS(null, "requireValidMetadata")) {
            requireValidMetadata = XMLHelper.getAttributeValueAsBoolean(config.getAttributeNodeNS(null,
                    "requireValidMetadata"));
        }

        return requireValidMetadata;
    }

    /**
     * Gets the ID of the metadata provider.
     * 
     * @param config metadata provider configuration element
     * 
     * @return ID of the metadata provider
     */
    protected String getProviderId(Element config) {
        return DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "id"));
    }
}