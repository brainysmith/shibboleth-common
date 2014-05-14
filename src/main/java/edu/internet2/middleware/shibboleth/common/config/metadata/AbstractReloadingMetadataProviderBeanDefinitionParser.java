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

import javax.xml.datatype.Duration;

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;

/** Base class for metadata providers that reload their metadata. */
public abstract class AbstractReloadingMetadataProviderBeanDefinitionParser extends
        AbstractMetadataProviderBeanDefinitionParser {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractReloadingMetadataProviderBeanDefinitionParser.class);

    /** {@inheritDoc} */
    protected void doParse(Element config, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(config, parserContext, builder);

        String taskTimerRef = getTaskTimerRef(config);
        log.debug("Metadata provider using task timer: {}", taskTimerRef);
        builder.addConstructorArgReference(taskTimerRef);

        float refreshDelayFactor = getRefreshDelayFactor(config);
        log.debug("Metadata provider refresh delay factor: {}", refreshDelayFactor);
        builder.addPropertyValue("refreshDelayFactor", refreshDelayFactor);

        long minRefreshDelay = getMinRefreshDelay(config);
        log.debug("Metadata provider min refresh delay: {}ms", minRefreshDelay);
        builder.addPropertyValue("minRefreshDelay", minRefreshDelay);
        
        long maxRefreshDelay = getMaxRefreshDelay(config);
        log.debug("Metadata provider max refresh delay: {}ms", maxRefreshDelay);
        builder.addPropertyValue("maxRefreshDelay", maxRefreshDelay);
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
     * Gets the default task timer reference for the metadata provider.
     * 
     * @param config metadata provider configuration element
     * 
     * @return task timer reference
     */
    protected String getTaskTimerRef(Element config) {
        String taskTimerRef = null;
        if (config.hasAttributeNS(null, "taskTimerRef")) {
            taskTimerRef = DatatypeHelper.safeTrimOrNullString(config.getAttributeNS(null, "taskTimerRef"));
        }

        if (taskTimerRef == null) {
            taskTimerRef = "shibboleth.TaskTimer";
        }

        return taskTimerRef;
    }

    /**
     * Gets the refresh delay factor for the metadata provider.
     * 
     * @param config provider configuration element
     * 
     * @return refresh delay factor
     */
    protected float getRefreshDelayFactor(Element config) {
        float delayFactor = 0.75f;

        if (config.hasAttributeNS(null, "refreshDelayFactor")) {
            String factorString = config.getAttributeNS(null, "refreshDelayFactor");
            try {
                delayFactor = Float.parseFloat(factorString);
            } catch (NumberFormatException e) {
                log.error("Metadata provider had invalid refreshDelayFactor value '{}', using default value",
                        factorString);
            }
        }

        if (delayFactor <= 0.0 || delayFactor >= 1.0) {
            log.error("Metadata provider had invalid refreshDelayFactor value '{}', using default value", delayFactor);
            delayFactor = 0.75f;
        }

        return delayFactor;
    }

    /**
     * Gets the maximum refresh delay for the metadata provider.
     * 
     * @param config provider configuration element
     * 
     * @return the maximum refresh delay, in milliseconds
     */
    protected long getMaxRefreshDelay(Element config) {
        long maxRefreshDelay = 14400000L;

        if (config.hasAttributeNS(null, "cacheDuration")) {
            int cacheDuration = Integer.parseInt(config.getAttributeNS(null, "cacheDuration"));
            maxRefreshDelay = cacheDuration * 1000;
            log.warn("Metadata provider cacheDuration attribute is deprecated, use maxRefreshDelay=\"{}\" instead.",
                    XMLHelper.getDataTypeFactory().newDuration(maxRefreshDelay).toString());
        }

        if (config.hasAttributeNS(null, "maxCacheDuration")) {
            int cacheDuration = Integer.parseInt(config.getAttributeNS(null, "maxCacheDuration"));
            Duration duration = XMLHelper.getDataTypeFactory().newDuration(cacheDuration * 1000);
            log.warn("Metadata provider maxCacheDuration attribute is deprecated, use maxRefreshDelay=\"{}\" instead.",
                    duration.toString());
        }

        if (config.hasAttributeNS(null, "maxRefreshDelay")) {
            String delayString = config.getAttributeNS(null, "maxRefreshDelay");
            try {
                maxRefreshDelay = SpringConfigurationUtils.parseDurationToMillis("maxRefreshDelay", delayString, 1);
            } catch (NumberFormatException e) {
                log.error("Metadata provider had invalid maxRefreshDelay value '{}', using default value", delayString);
            }
        }

        if (maxRefreshDelay <= 0) {
            log.error("Metadata provider had invalid maxRefreshDelay value '{}', using default value", maxRefreshDelay);
            maxRefreshDelay = 14400000L;
        }

        return maxRefreshDelay;
    }

    /**
     * Gets the minimum refresh delay for the metadata provider.
     * 
     * @param config provider configuration element
     * 
     * @return the minimum refresh delay, in milliseconds
     */
    protected long getMinRefreshDelay(Element config) {
        long minRefreshDelay = 300000L;

        if (config.hasAttributeNS(null, "minRefreshDelay")) {
            String delayString = config.getAttributeNS(null, "minRefreshDelay");
            try {
                minRefreshDelay = SpringConfigurationUtils.parseDurationToMillis("minRefreshDelay", delayString, 1);
            } catch (NumberFormatException e) {
                log.error("Metadata provider had invalid minRefreshDelay value '{}', using default value", delayString);
            }
        }

        if (minRefreshDelay <= 0) {
            log.error("Metadata provider had invalid minRefreshDelay value '{}', using default value", minRefreshDelay);
            minRefreshDelay = 300000L;
        }

        return minRefreshDelay;
    }
}