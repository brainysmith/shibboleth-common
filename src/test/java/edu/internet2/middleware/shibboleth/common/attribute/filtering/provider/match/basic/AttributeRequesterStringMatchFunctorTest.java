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

package edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.match.basic;

import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.match.BaseTestCaseMetadata;

/** {@link AttributeRequesterStringMatchFunctor} unit test. */
public class AttributeRequesterStringMatchFunctorTest extends BaseTestCaseMetadata {

    /** Test against the requester name ("urn:exmaple.org:requester") in the metadata. */
    public void testCaseSensitive() throws Exception {
        AttributeRequesterStringMatchFunctor functor = new AttributeRequesterStringMatchFunctor();
        functor.setMatchString("urn:exmaple.org:requester");
        functor.setCaseSensitive(true);
        assertTrue(functor.evaluatePolicyRequirement(filterContext));
        assertTrue(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeRequesterStringMatchFunctor();
        functor.setMatchString("urn:exmaple.org:Requester");
        functor.setCaseSensitive(true);
        assertFalse(functor.evaluatePermitValue(filterContext, null, null));
        assertFalse(functor.evaluatePolicyRequirement(filterContext));
    }

    /** Test against almost the requester name in the metadata. */
    public void testCaseInsensitive() throws Exception {
        AttributeRequesterStringMatchFunctor functor = new AttributeRequesterStringMatchFunctor();
        functor.setMatchString("urn:exmaple.org:requester");
        functor.setCaseSensitive(false);
        assertTrue(functor.evaluatePolicyRequirement(filterContext));
        assertTrue(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeRequesterStringMatchFunctor();
        functor.setMatchString("urn:exmaple.org:Requester");
        functor.setCaseSensitive(false);
        assertTrue(functor.evaluatePermitValue(filterContext, null, null));
        assertTrue(functor.evaluatePolicyRequirement(filterContext));
    }

    /** Test against nothing like the requester name in the metadata. */
    public void testMismatch() throws Exception {
        AttributeRequesterStringMatchFunctor functor = new AttributeRequesterStringMatchFunctor();
        functor.setMatchString("foo");
        functor.setCaseSensitive(true);

        assertFalse(functor.evaluatePermitValue(filterContext, null, null));
        assertFalse(functor.evaluatePolicyRequirement(filterContext));
    }
}