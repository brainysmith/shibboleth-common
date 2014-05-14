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

import java.util.regex.Pattern;

import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.match.BaseTestCaseMetadata;

/** {@link AttributeIssuerEntityAttributeRegexMatchFunctor} unit test. */
public class AttributeIssuerEntityAttributeRegexMatchFunctorTestCase extends BaseTestCaseMetadata {

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        metadataFile = MD_PATH + "/shibboleth.net-metadata.xml";
        issuerEntityId = "https://idp.shibboleth.net/idp/shibboleth";
        requesterEntityId = "https://issues.shibboleth.net/shibboleth";

        super.setUp();
    }

    public void testEvaluatePolicyRequirement() throws Exception {
        AttributeIssuerEntityAttributeRegexMatchFunctor functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:policy\\:12.*"));
        assertTrue(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:policy\\:WX.*"));
        assertFalse(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:12.*"));
        assertTrue(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:12.*"));
        assertTrue(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:WX.*"));
        assertFalse(functor.evaluatePolicyRequirement(filterContext));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:12.*"));
        assertFalse(functor.evaluatePolicyRequirement(filterContext));
    }

    public void testEvaluatePermitValue() throws Exception {
        AttributeIssuerEntityAttributeRegexMatchFunctor functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:policy\\:12.*"));
        assertTrue(functor.evaluatePermitValue(filterContext, null, null));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:policy\\:WX.*"));
        assertFalse(functor.evaluatePermitValue(filterContext, null, null));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:12.*"));
        assertTrue(functor.evaluatePermitValue(filterContext, null, null));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:12.*"));
        assertTrue(functor.evaluatePermitValue(filterContext, null, null));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:WX.*"));
        assertFalse(functor.evaluatePermitValue(filterContext, null, null));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:12.*"));
        assertFalse(functor.evaluatePermitValue(filterContext, null, null));
    }

    public void testEvaluateDenyRule() throws Exception {
        AttributeIssuerEntityAttributeRegexMatchFunctor functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:policy\\:12.*"));
        assertTrue(functor.evaluateDenyRule(filterContext, null, null));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:policies");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:policy\\:WX.*"));
        assertFalse(functor.evaluateDenyRule(filterContext, null, null));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:12.*"));
        assertTrue(functor.evaluateDenyRule(filterContext, null, null));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:12.*"));
        assertTrue(functor.evaluateDenyRule(filterContext, null, null));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:WX.*"));
        assertFalse(functor.evaluateDenyRule(filterContext, null, null));

        functor = new AttributeIssuerEntityAttributeRegexMatchFunctor();
        functor.setName("urn:example.org:entitlements");
        functor.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
        functor.setValueRegex(Pattern.compile("urn\\:example.org\\:entitlements\\:12.*"));
        assertFalse(functor.evaluateDenyRule(filterContext, null, null));
    }
}