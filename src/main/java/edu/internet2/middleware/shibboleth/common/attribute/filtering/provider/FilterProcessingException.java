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

package edu.internet2.middleware.shibboleth.common.attribute.filtering.provider;

import edu.internet2.middleware.shibboleth.common.attribute.filtering.AttributeFilteringException;

/**
 * An exception resulting from the application of an attribute acceptance policy to a collection of attributes.
 */
public class FilterProcessingException extends AttributeFilteringException {

    /** Serial version UID. */
    private static final long serialVersionUID = -3674729593708202825L;

    /**
     * Constructor.
     */
    public FilterProcessingException() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     */
    public FilterProcessingException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param wrappedException exception to be wrapped by this one
     */
    public FilterProcessingException(Throwable wrappedException) {
        super(wrappedException);
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     * @param wrappedException exception to be wrapped by this one
     */
    public FilterProcessingException(String message, Throwable wrappedException) {
        super(message, wrappedException);
    }
}