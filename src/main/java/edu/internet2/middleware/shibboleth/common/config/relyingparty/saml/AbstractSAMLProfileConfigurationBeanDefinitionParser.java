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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.LazyList;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.config.SpringConfigurationUtils;
import edu.internet2.middleware.shibboleth.common.relyingparty.provider.CryptoOperationRequirementLevel;

/**
 * Base Spring configuration parser for SAML profile configurations.
 */
public abstract class AbstractSAMLProfileConfigurationBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /** {@inheritDoc} */
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        builder.setLazyInit(true);
        Map<QName, List<Element>> children = XMLHelper.getChildElements(element);

        List<Element> audienceElems = children.get(new QName(SAMLRelyingPartyNamespaceHandler.NAMESPACE, "Audience"));
        if (audienceElems != null && audienceElems.size() > 0) {
            LazyList<String> audiences = new LazyList<String>();
            for (Element audienceElem : audienceElems) {
                audiences.add(DatatypeHelper.safeTrimOrNullString(audienceElem.getTextContent()));
            }
            builder.addPropertyValue("audiences", audiences);
        }

        String secCredRef = DatatypeHelper.safeTrimOrNullString(element.getAttributeNS(null, "signingCredentialRef"));
        if (secCredRef != null) {
            builder.addDependsOn(secCredRef);
            builder.addPropertyReference("signingCredential", secCredRef);
        }

        long lifetime = 300000L;
        if (element.hasAttributeNS(null, "assertionLifetime")) {
            lifetime = SpringConfigurationUtils.parseDurationToMillis(
                    "'assertionLifetime' on profile configuration of type " + XMLHelper.getXSIType(element),
                    element.getAttributeNS(null, "assertionLifetime"), 0);
        }
        builder.addPropertyValue("assertionLifetime", lifetime);

        String artifactType = DatatypeHelper.safeTrimOrNullString(element.getAttributeNS(null, "outboundArtifactType"));
        if (artifactType != null) {
            byte[] artifactTypeBytes = DatatypeHelper.intToByteArray(Integer.parseInt(artifactType));
            byte[] trimmedArtifactTypeBytes = { artifactTypeBytes[2], artifactTypeBytes[3] };
            builder.addPropertyValue("outboundArtifactType", trimmedArtifactTypeBytes);
        }

        CryptoOperationRequirementLevel signRequests = CryptoOperationRequirementLevel.conditional;
        if (element.hasAttributeNS(null, "signRequests")) {
            signRequests = CryptoOperationRequirementLevel.valueOf(element.getAttributeNS(null, "signRequests"));
        }
        builder.addPropertyValue("signRequests", signRequests);

        CryptoOperationRequirementLevel signResponses = getSignResponsesDefault();
        if (element.hasAttributeNS(null, "signResponses")) {
            signResponses = CryptoOperationRequirementLevel.valueOf(element.getAttributeNS(null, "signResponses"));
        }
        builder.addPropertyValue("signResponses", signResponses);

        CryptoOperationRequirementLevel signAssertions = getSignAssertionsDefault();
        if (element.hasAttributeNS(null, "signAssertions")) {
            signAssertions = CryptoOperationRequirementLevel.valueOf(element.getAttributeNS(null, "signAssertions"));
        }
        builder.addPropertyValue("signAssertions", signAssertions);

        String secPolRef = DatatypeHelper.safeTrimOrNullString(element.getAttributeNS(null, "securityPolicyRef"));
        if (secPolRef != null) {
            builder.addDependsOn(secPolRef);
            builder.addPropertyReference("profileSecurityPolicy", secPolRef);
        }
    }

    /** {@inheritDoc} */
    protected boolean shouldGenerateId() {
        return true;
    }

    /**
     * Gets the default value for the signResponses property.
     * 
     * @return default value for the signResponses property
     */
    protected abstract CryptoOperationRequirementLevel getSignResponsesDefault();

    /**
     * Gets the default value for the signAssertions property.
     * 
     * @return default value for the signAssertions property
     */
    protected abstract CryptoOperationRequirementLevel getSignAssertionsDefault();
}