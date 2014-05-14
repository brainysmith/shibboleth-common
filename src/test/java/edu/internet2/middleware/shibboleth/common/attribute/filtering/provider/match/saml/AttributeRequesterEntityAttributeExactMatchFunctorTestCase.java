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

package edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.match.saml;

import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.match.BaseTestCaseMetadata;

/** {@link AttributeRequesterEntityAttributeExactMatchFunctor} unit test. */
public class AttributeRequesterEntityAttributeExactMatchFunctorTestCase extends BaseTestCaseMetadata {

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        metadataFile = MD_PATH + "/shibboleth.net-metadata.xml";
        issuerEntityId = "https://issues.shibboleth.net/shibboleth";
        requesterEntityId = "https://idp.shibboleth.net/idp/shibboleth";

        super.setUp();
    }

    public void testEvaluatePolicyRequirement() throws Exception {
        AttributeRequesterEntityAttributeExactMatchFunctor functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValue("urn:example.org:policy:1234");
        assertTrue(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValue("urn:example.org:policy:WXYZ");
        assertFalse(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValue("urn:example.org:entitlements:ABCD");
        assertTrue(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
        functor.setValue("urn:example.org:entitlements:ABCD");
        assertTrue(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValue("urn:example.org:entitlements:WXYZ");
        assertFalse(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
        functor.setValue("urn:example.org:entitlements:ABCD");
        assertFalse(functor.evaluatePolicyRequirement(filterContext));
    }

    public void testEvaluatePermitValue() throws Exception {
        AttributeRequesterEntityAttributeExactMatchFunctor functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValue("urn:example.org:policy:1234");
        assertTrue(functor.evaluatePermitValue(filterContext, null, null));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValue("urn:example.org:policy:WXYZ");
        assertFalse(functor.evaluatePermitValue(filterContext, null, null));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValue("urn:example.org:entitlements:ABCD");
        assertTrue(functor.evaluatePermitValue(filterContext, null, null));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
        functor.setValue("urn:example.org:entitlements:ABCD");
        assertTrue(functor.evaluatePermitValue(filterContext, null, null));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValue("urn:example.org:entitlements:WXYZ");
        assertFalse(functor.evaluatePermitValue(filterContext, null, null));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
        functor.setValue("urn:example.org:entitlements:ABCD");
        assertFalse(functor.evaluatePermitValue(filterContext, null, null));
    }

    public void testEvaluateDenyRule() throws Exception {
        AttributeRequesterEntityAttributeExactMatchFunctor functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValue("urn:example.org:policy:1234");
        assertTrue(functor.evaluateDenyRule(filterContext, null, null));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValue("urn:example.org:policy:WXYZ");
        assertFalse(functor.evaluateDenyRule(filterContext, null, null));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValue("urn:example.org:entitlements:ABCD");
        assertTrue(functor.evaluateDenyRule(filterContext, null, null));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
        functor.setValue("urn:example.org:entitlements:ABCD");
        assertTrue(functor.evaluateDenyRule(filterContext, null, null));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValue("urn:example.org:entitlements:WXYZ");
        assertFalse(functor.evaluateDenyRule(filterContext, null, null));

        functor = new AttributeRequesterEntityAttributeExactMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
        functor.setValue("urn:example.org:entitlements:ABCD");
        assertFalse(functor.evaluateDenyRule(filterContext, null, null));
    }
}