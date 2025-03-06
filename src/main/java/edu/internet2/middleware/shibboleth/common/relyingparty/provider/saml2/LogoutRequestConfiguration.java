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

package edu.internet2.middleware.shibboleth.common.relyingparty.provider.saml2;

/**
 * SAML 2 Logout Request configuration settings.
 */
public class LogoutRequestConfiguration extends AbstractSAML2ProfileConfiguration {

    /** ID for this profile configuration. */
    public static final String PROFILE_ID = "urn:mace:shibboleth:2.0:profiles:saml2:logout";
    
    /** {@inheritDoc} */
    public String getProfileId() {
        return PROFILE_ID;
    }

    /* SLO patch (added) */
    private int frontChannelResponseTimeout;
    private int backChannelConnectionTimeout;
    private int backChannelConnectionPoolTimeout;
    private int backChannelResponseTimeout;

    /**
     * Returns the back-channel timeout (in milliseconds) for connection
     * acquirement from the pool.
     *
     * @return
     */
    public int getBackChannelConnectionPoolTimeout() {
        return backChannelConnectionPoolTimeout;
    }

    public void setBackChannelConnectionPoolTimeout(int backChannelConnectionPoolTimeout) {
        this.backChannelConnectionPoolTimeout = backChannelConnectionPoolTimeout;
    }

    /**
     * Returns the back-channel timeout (in milliseconds) for connection establishment.
     *
     * @return
     */
    public int getBackChannelConnectionTimeout() {
        return backChannelConnectionTimeout;
    }

    public void setBackChannelConnectionTimeout(int backChannelConnectionTimeout) {
        this.backChannelConnectionTimeout = backChannelConnectionTimeout;
    }

    /**
     * Returns the back-channel timeout (in milliseconds) for soap response.
     *
     * @return
     */
    public int getBackChannelResponseTimeout() {
        return backChannelResponseTimeout;
    }

    public void setBackChannelResponseTimeout(int backChannelResponseTimeout) {
        this.backChannelResponseTimeout = backChannelResponseTimeout;
    }

    /**
     * Returns the front-channel response timeout in milliseconds.
     *
     * @return
     */
    public int getFrontChannelResponseTimeout() {
        return frontChannelResponseTimeout;
    }

    public void setFrontChannelResponseTimeout(int frontChannelResponseTimeout) {
        this.frontChannelResponseTimeout = frontChannelResponseTimeout;
    }
}