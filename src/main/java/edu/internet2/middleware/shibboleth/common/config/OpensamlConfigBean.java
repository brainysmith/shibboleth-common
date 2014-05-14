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

package edu.internet2.middleware.shibboleth.common.config;

import java.util.List;

import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.util.resource.Resource;
import org.opensaml.xml.XMLConfigurator;
import org.opensaml.xml.parse.ParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * A simple bean that may be used with Spring to initialize the OpenSAML library.
 */
public class OpensamlConfigBean implements InitializingBean {
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(OpensamlConfigBean.class);
    
    /** OpenSAML configuration resources. */
    private List<Resource> configResources;
    
    /** Optional ParserPool to configure. */
    private ParserPool parserPool;
    
    /**
     * Get the global ParserPool to configure.
     * 
     * @return Returns the parserPool.
     */
    public ParserPool getParserPool() {
        return parserPool;
    }

    /**
     * Set the global ParserPool to configure.
     * 
     * @param newParserPool The parserPool to set.
     */
    public void setParserPool(ParserPool newParserPool) {
        parserPool = newParserPool;
    }

    /**
     * Constructor.
     *
     * @param configs OpenSAML configuration resources
     */
    public OpensamlConfigBean(List<Resource> configs){
        configResources = configs;
    }

    /** {@inheritDoc} */
    public void afterPropertiesSet() throws Exception {
        DefaultBootstrap.bootstrap();
        
        if(configResources != null && !configResources.isEmpty()){
            XMLConfigurator configurator = new XMLConfigurator();
            for(Resource config : configResources){
                try{
                    log.debug("Loading OpenSAML configuration file: {}", config.getLocation());
                    configurator.load(config.getInputStream());
                }catch(Exception e){
                    log.error("Unable to load OpenSAML configuration file: " + config.getLocation());
                }
            }
        }
        
        if (getParserPool() != null) {
            Configuration.setParserPool(getParserPool());
        }
        
    }
}