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

package edu.internet2.middleware.shibboleth.common.config.relyingparty.saml;

import edu.internet2.middleware.shibboleth.common.relyingparty.provider.saml2.SSOConfiguration;

/** Spring factory for SAML 2 SSO profile configurations. */
public class SAML2SSOProfileConfigurationFactoryBean extends AbstractSAML2ProfileConfigurationFactoryBean {

    /** Whether responses to the authentication request should include an attribute statement. */
    private boolean includeAttributeStatement;

    /** The maximum amount of time, in milliseconds, the service provider should maintain a session for the user. */
    private long maximumSPSessionLifetime;

    /** {@inheritDoc} */
    public Class getObjectType() {
        return SSOConfiguration.class;
    }

    /**
     * Gets whether responses to the authentication request should include an attribute statement.
     * 
     * @return whether responses to the authentication request should include an attribute statement
     */
    public boolean includeAttributeStatement() {
        return includeAttributeStatement;
    }

    /**
     * Sets whether responses to the authentication request should include an attribute statement.
     * 
     * @param include whether responses to the authentication request should include an attribute statement
     */
    public void setIncludeAttributeStatement(boolean include) {
        includeAttributeStatement = include;
    }

    /**
     * Gets the maximum amount of time, in milliseconds, the service provider should maintain a session for the user
     * based on the authentication assertion.
     * 
     * @return max lifetime of service provider should maintain a session
     */
    public long getMaximumSPSessionLifetime() {
        return maximumSPSessionLifetime;
    }

    /**
     * Sets the maximum amount of time, in milliseconds, the service provider should maintain a session for the user
     * based on the authentication assertion.
     * 
     * @param lifetime max lifetime of service provider should maintain a session
     */
    public void setMaximumSPSessionLifetime(long lifetime) {
        maximumSPSessionLifetime = lifetime;
    }

    /** {@inheritDoc} */
    protected Object createInstance() throws Exception {
        SSOConfiguration configuration = (SSOConfiguration) getObjectType().newInstance();
        populateBean(configuration);
        configuration.setIncludeAttributeStatement(includeAttributeStatement());
        configuration.setMaximumSPSessionLifetime(getMaximumSPSessionLifetime());

        return configuration;
    }
}