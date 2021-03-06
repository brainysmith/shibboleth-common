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

package edu.internet2.middleware.shibboleth.common.xmlobject.impl;

import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.common.ShibbolethConstants;
import edu.internet2.middleware.shibboleth.common.xmlobject.ShibbolethMetadataScope;

/**
 * A thread-safe Marshaller for {@link ShibbolethMetadataScope}.
 */
public class ShibbolethMetadataScopeMarshaller extends AbstractXMLObjectMarshaller {
    
    /** Constructor. */
    public ShibbolethMetadataScopeMarshaller() {
        super(ShibbolethConstants.SHIB_MDEXT10_NS, ShibbolethMetadataScope.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI
     * @param elementLocalName the element local name
     */
    protected ShibbolethMetadataScopeMarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
        ShibbolethMetadataScope scope = (ShibbolethMetadataScope) xmlObject;
        
        if (scope.getRegexpXSBoolean() != null) {
            domElement.setAttributeNS(null, ShibbolethMetadataScope.REGEXP_ATTRIB_NAME,
                    scope.getRegexpXSBoolean().toString());
        }

    }

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject xmlObject, Element domElement) throws MarshallingException {
        ShibbolethMetadataScope shibMDScope = (ShibbolethMetadataScope) xmlObject;
        
        XMLHelper.appendTextContent(domElement, shibMDScope.getValue());
    }

}
