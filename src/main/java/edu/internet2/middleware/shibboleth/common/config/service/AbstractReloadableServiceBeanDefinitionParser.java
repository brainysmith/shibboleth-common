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

package edu.internet2.middleware.shibboleth.common.config.service;

import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

/**
 * Base bean definition parser for reloadable services.
 */
public abstract class AbstractReloadableServiceBeanDefinitionParser extends AbstractServiceBeanDefinitionParser {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractReloadableServiceBeanDefinitionParser.class);

    /** {@inheritDoc} */
    protected void doParse(Element configElement, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(configElement, parserContext, builder);

        if (configElement.hasAttributeNS(null, "configurationResourcePollingFrequency")) {
            builder.addPropertyReference("pollingTimer", configElement.getAttributeNS(null, "timerId"));

            long frequency = SpringConfigurationUtils.parseDurationToMillis(
                    "'configurationResourcePollingFrequency' on service " + configElement.getAttributeNS(null, "id"),
                    configElement.getAttributeNS(null, "configurationResourcePollingFrequency"), 1);
            builder.addPropertyValue("pollingFrequency", frequency);
            log.debug("{} service configuration polling frequency: {}ms", getServiceId(configElement), frequency);

            int retryAttempts = 0;
            if (configElement.hasAttributeNS(null, "configurationResourcePollingRetryAttempts")) {
                retryAttempts = Integer.parseInt(DatatypeHelper.safeTrimOrNullString(configElement.getAttributeNS(null,
                        "configurationResourcePollingRetryAttempts")));
            }
            if (retryAttempts < 1) {
                retryAttempts = 3;
            }
            builder.addPropertyValue("pollingRetryAttempts", retryAttempts);
            log.debug("{} service configuration polling retry attempts: {}", getServiceId(configElement),
                            retryAttempts);
        }
    }
}