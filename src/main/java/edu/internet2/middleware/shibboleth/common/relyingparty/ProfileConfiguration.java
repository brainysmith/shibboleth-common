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

package edu.internet2.middleware.shibboleth.common.relyingparty;

import org.opensaml.ws.security.SecurityPolicy;

/**
 * Configuration options for a specific communication profile.
 */
public interface ProfileConfiguration {

    /**
     * Gets the unique identifier of the profile.
     * 
     * @return unique identifier of the profile
     */
    public String getProfileId();
    
    /**
     * Gets the security policy for this profile.
     * 
     * @return security policy for this profile
     */
    public SecurityPolicy getSecurityPolicy();
}