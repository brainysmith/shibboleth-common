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

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/** Base class for metadata providers that reload their metadata. */
public abstract class AbstractMetadataProviderBeanDefinitionParser extends BaseMetadataProviderBeanDefinitionParser {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractMetadataProviderBeanDefinitionParser.class);

    /** {@inheritDoc} */
    protected void doParse(Element config, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);
        
        builder.setInitMethodName("initialize");

        String parserPoolRef = getParserPoolRef(config);
        log.debug("Metadata provider using parser pool: {}", parserPoolRef);
        builder.addPropertyReference("parserPool", parserPoolRef);

        boolean failFastInit = getFailFastInitialization(config);
        log.debug("Metadata provider fail fast initialization enabled: {}", failFastInit);
        builder.addPropertyValue("failFastInitialization", failFastInit);
    }

    /**
     * Gets the default parser pool reference for the metadata provider.
     * 
     * @param config metadata provider configuration element
     * 
     * @return parser pool reference
     */
    protected String getParserPoolRef(Element config) {
        String parserPoolRef = null;
        if (config.hasAttributeNS(null, "parerPoolRef")) {
            parserPoolRef = DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "parserPoolRef"));
        }

        if (parserPoolRef == null) {
            parserPoolRef = "shibboleth.ParserPool";
        }

        return parserPoolRef;
    }
    
    /**
     * Gets the fail fast initialization requirement for the metadata provider.
     * 
     * @param config metadata provider config
     * 
     * @return fail fast initialization requirement for the metadata provider
     */
    protected boolean getFailFastInitialization(Element config) {
        boolean failFastInit = true;
        if (config.hasAttributeNS(null, "failFastInitialization")) {
            failFastInit = XMLHelper.getAttributeValueAsBoolean(config.getAttributeNodeNS(null,
                    "failFastInitialization"));
        }

        return failFastInit;
    }
}