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

package edu.internet2.middleware.shibboleth.common.config.attribute.resolver.principalConnector;

import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.principalConnector.CryptoTransientPrincipalConnector;
import edu.internet2.middleware.shibboleth.common.util.DataSealer;

/**
 * Spring factory bean for {@link CryptoTransientPrincipalConnector}s.
 */
public class CryptoTransientPrincipalConnectorFactoryBean extends BasePrincipalConnectorFactoryBean {

    /** Object used to decrypt identifiers. */
    private DataSealer dataSealer;

    /** {@inheritDoc} */
    public Class<CryptoTransientPrincipalConnector> getObjectType() {
        return CryptoTransientPrincipalConnector.class;
    }

    /**
     * Gets the object used to decrypt identifiers.
     * 
     * @return object used to decrypt identifiers
     */
    public DataSealer getDataSealer() {
        return dataSealer;
    }

    /**
     * Sets the object used to decrypt identifiers.
     * 
     * @param sealer object used to decrypt identifiers
     */
    public void setDataSealer(DataSealer sealer) {
        dataSealer = sealer;
    }

    /** {@inheritDoc} */
    protected Object createInstance() throws Exception {
        CryptoTransientPrincipalConnector connector = new CryptoTransientPrincipalConnector(getDataSealer());
        populatePrincipalConnector(connector);

        return connector;
    }
}