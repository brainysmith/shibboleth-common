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

package edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector;

import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.pool.LdapFactory;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;

/**
 * Used by the ldap data connector to interface with various pooling implementations. 
 */
public interface LdapPoolStrategy {

    /**
     * Sets the ldap factory.
     *
     * @param  factory  to create ldap objects with
     */
    void setLdapFactory(LdapFactory<Ldap> factory);

    /**
     * Sets the ldap pool configuration.
     *
     * @param  config  to manage ldap pool with
     */
    void setLdapPoolConfig(LdapPoolConfig config);

    /**
     * Sets whether to block when the pool is empty.
     *
     * @param  block  when the pool is empty
     */
    void setBlockWhenEmpty(boolean block);

    /**
     * Prepare the pool for use.
     */
    void initialize();

    /**
     * Retrieve an ldap object.
     * 
     * @return  ldap object
     * 
     * @throws Exception thrown if there is a problem checking in an {@link Ldap} object
     */
    Ldap checkOut() throws Exception;

    /**
     * Return an ldap object.
     *
     * @param l the ldap object
     * 
     * @throws Exception thrown if there is a problem checking out an {@link Ldap} object
     */
    void checkIn(Ldap l) throws Exception;
}
