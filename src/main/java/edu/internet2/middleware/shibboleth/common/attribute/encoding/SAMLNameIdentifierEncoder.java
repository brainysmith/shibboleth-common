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

package edu.internet2.middleware.shibboleth.common.attribute.encoding;

import org.opensaml.xml.XMLObject;

/**
 * Base interface for encoders that generate SAML name identifiers.
 * 
 * @param <NameIDType> the type of name identifier generated
 */
public interface SAMLNameIdentifierEncoder<NameIDType extends XMLObject> extends XMLObjectAttributeEncoder<NameIDType> {

    /**
     * Gets the name format URI for the NameID.
     * 
     * @return name format URI for the NameID
     */
    public String getNameFormat();

    /**
     * Sets the name format URI for the NameID.
     * 
     * @param format name format URI for the NameID
     */
    public void setNameFormat(String format);

    /**
     * Gets the name domain qualifier for the NameID.
     * 
     * @return name domain qualifier for the NameID
     */
    public String getNameQualifier();

    /**
     * Sets the name domain qualifier for the NameID.
     * 
     * @param qualifier name domain qualifier for the NameID
     */
    public void setNameQualifier(String qualifier);
}