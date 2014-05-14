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

/** {@link AttributeIssuerRegexMatchFunctor} unit test. */
public class AttributeIssuerRegexMatchFunctorTest extends BaseTestCaseMetadata {

    /** Test against the issuer name ("urn:exmaple.org:issuer") in the metadata. */
    public void testIssuerRegexp() throws Exception {
        AttributeIssuerRegexMatchFunctor functor = new AttributeIssuerRegexMatchFunctor();
        functor.setRegularExpression("[uU].*[iI]ssuer");
        assertTrue(functor.evaluatePermitValue(filterContext, null, null));
        assertTrue(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeIssuerRegexMatchFunctor();
        functor.setRegularExpression(".*a");
        assertFalse(functor.evaluatePolicyRequirement(filterContext));
    }
}