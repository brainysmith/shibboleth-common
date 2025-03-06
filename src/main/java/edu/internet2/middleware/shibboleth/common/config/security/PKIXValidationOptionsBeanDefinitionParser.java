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

package edu.internet2.middleware.shibboleth.common.config.security;

import javax.xml.namespace.QName;

import org.opensaml.xml.security.x509.PKIXValidationOptions;
import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/** Spring bean definition parser for {urn:mace:shibboleth:2.0:security}PKIXValidationOptions elements. */
public class PKIXValidationOptionsBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    
    /** Element name. */
    public static final QName ELEMENT_NAME = new QName(SecurityNamespaceHandler.NAMESPACE, "ValidationOptions");
    
    /** Schema type. */
    public static final QName SCHEMA_TYPE = new QName(SecurityNamespaceHandler.NAMESPACE, "ValidationOptionsType");
    
    /** {@inheritDoc} */
    protected Class getBeanClass(Element element) {
        return PKIXValidationOptions.class;
    }
    
    /** {@inheritDoc} */
    protected boolean shouldGenerateId() {
        return true;
    }
    
    /** {@inheritDoc} */
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        if (element.hasAttributeNS(null, "processEmptyCRLs")) {
            Attr attr = element.getAttributeNodeNS(null, "processEmptyCRLs");
            builder.addPropertyValue("processEmptyCRLs", XMLHelper.getAttributeValueAsBoolean(attr));
        }
        
        if (element.hasAttributeNS(null, "processExpiredCRLs")) {
            Attr attr = element.getAttributeNodeNS(null, "processExpiredCRLs");
            builder.addPropertyValue("processExpiredCRLs", XMLHelper.getAttributeValueAsBoolean(attr));
        }
        
        if (element.hasAttributeNS(null, "processCredentialCRLs")) {
            Attr attr = element.getAttributeNodeNS(null, "processCredentialCRLs");
            builder.addPropertyValue("processCredentialCRLs", XMLHelper.getAttributeValueAsBoolean(attr));
        }
        
        if (element.hasAttributeNS(null, "defaultVerificationDepth")) {
            Integer depth = new Integer(DatatypeHelper.safeTrim(element.getAttributeNS(null, "defaultVerificationDepth")));
            builder.addPropertyValue("defaultVerificationDepth", depth);
        }
        
    }

}
