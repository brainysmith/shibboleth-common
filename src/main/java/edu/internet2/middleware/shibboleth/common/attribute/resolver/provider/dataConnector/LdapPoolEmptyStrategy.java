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

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.pool.LdapFactory;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;

/**
 * Ldap pool strategy that does no pooling.
 */
public class LdapPoolEmptyStrategy implements LdapPoolStrategy {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(LdapPoolEmptyStrategy.class);

    /** Factory for making ldap objects. */
    private LdapFactory<Ldap> ldapFactory;

    /**
     * Default constructor.
     */
    public LdapPoolEmptyStrategy() {
    }

    /** {@inheritDoc} */
    public void setLdapPoolConfig(LdapPoolConfig config) {
    }

    /** {@inheritDoc} */
    public void setLdapFactory(LdapFactory<Ldap> factory) {
        ldapFactory = factory;
    }

    /** {@inheritDoc} */
    public void setBlockWhenEmpty(boolean block) {
    }

    /** {@inheritDoc} */
    public void initialize() {
        Ldap ldap = null;
        try {
            ldap = checkOut();
            if (ldap == null) {
                log.error("Unable to retrieve an LDAP connection");
                throw new AttributeResolutionException("Unable to retrieve LDAP connection");
            }
            if (!ldap.connect()) {
                throw new RuntimeException("Unable to connect to LDAP server");
            }
        } catch (NamingException e) {
            log.error("An error occured when attempting to search the LDAP: " + ldap.getLdapConfig().getEnvironment(),
                    e);
            throw new RuntimeException("Unable to connect to LDAP server", e);
        } catch (Exception e) {
            log.error("Could not retrieve Ldap object from pool", e);
            throw new RuntimeException("Could not retrieve Ldap object from pool", e);
        } finally {
            if (ldap != null) {
                try {
                    checkIn(ldap);
                } catch (Exception e) {
                    log.error("Could not return Ldap object back to pool", e);
                }
            }
        }
    }

    /** {@inheritDoc} */
    public Ldap checkOut() throws Exception {
        return ldapFactory.create();
    }

    /** {@inheritDoc} */
    public void checkIn(Ldap l) throws Exception {
        ldapFactory.destroy(l);
    }
}
