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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.pool.BlockingLdapPool;
import edu.vt.middleware.ldap.pool.LdapFactory;
import edu.vt.middleware.ldap.pool.LdapPool;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;
import edu.vt.middleware.ldap.pool.SoftLimitLdapPool;

/** Ldap pool strategy backed by the vt-ldap library. */
public class LdapPoolVTStrategy implements LdapPoolStrategy {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(LdapPoolVTStrategy.class);

    /** Underlying pool. */
    private LdapPool<Ldap> ldapPool;

    /** Ldap pool configuration. */
    private LdapPoolConfig ldapPoolConfig;

    /** Factory for making ldap objects. */
    private LdapFactory<Ldap> ldapFactory;

    /** Whether to block when empty. */
    private boolean blockWhenEmpty;

    /** Amount of time to wait, in milliseconds, if blocking when the pool is empty. Default value: 0 */
    private int blockWaitTime;

    /**
     * Default constructor.
     */
    public LdapPoolVTStrategy() {
    }

    /** {@inheritDoc} */
    public void setLdapPoolConfig(LdapPoolConfig config) {
        ldapPoolConfig = config;
    }

    /** {@inheritDoc} */
    public void setLdapFactory(LdapFactory<Ldap> factory) {
        ldapFactory = factory;
    }

    /** {@inheritDoc} */
    public void setBlockWhenEmpty(boolean block) {
        blockWhenEmpty = block;
    }

    /**
     * Sets the amount of time to wait, in milliseconds, if blocking when the pool is empty. A value of 0 means to wait
     * indefinitely.
     * 
     * @param waitTime amount of time to wait, in milliseconds, if blocking when the pool is empty
     */
    public void setBlockWaitTime(int waitTime) {
        blockWaitTime = waitTime;
    }

    /** {@inheritDoc} */
    public void initialize() {
        Ldap ldap = null;

        try {
            ldapPoolConfig.setPruneTimerPeriod(ldapPoolConfig.getValidateTimerPeriod());
            if (blockWhenEmpty) {
                ldapPool = new BlockingLdapPool(ldapPoolConfig, ldapFactory);
                ((BlockingLdapPool) ldapPool).setBlockWaitTime(blockWaitTime);
            } else {
                ldapPool = new SoftLimitLdapPool(ldapPoolConfig, ldapFactory);
            }
            ldapPool.initialize();
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
        return ldapPool.checkOut();
    }

    /** {@inheritDoc} */
    public void checkIn(Ldap l) throws Exception {
        ldapPool.checkIn(l);
    }
}
