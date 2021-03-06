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

package edu.internet2.middleware.shibboleth.common.profile;

import javax.servlet.ServletRequest;

/**
 * This manager is responsible for determining the correct {@link ProfileHandler} for a given request. The
 * manner in which this is done is completely implementation specific but implementers should make the selection process
 * as quick and simple as absolutely possible as this process will be run on every request.
 */
public interface ProfileHandlerManager {

    /**
     * Gets the profile handler to service this request.
     * 
     * @param request request that will be serviced by the profile handler
     * 
     * @return the profile handler that should be used to service the given request
     */
    public ProfileHandler getProfileHandler(ServletRequest request);
    
    /**
     * Gets the error handler for this manager.
     * 
     * @return handler to use if an error is encountered when processing a request
     */
    public AbstractErrorHandler getErrorHandler();
}