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

package edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.match;

import java.io.File;
import java.net.URL;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.parse.BasicParserPool;

import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.MatchFunctor;
import edu.internet2.middleware.shibboleth.common.attribute.filtering.provider.ShibbolethFilteringContext;
import edu.internet2.middleware.shibboleth.common.profile.provider.BaseSAMLProfileRequestContext;
import edu.internet2.middleware.shibboleth.common.relyingparty.RelyingPartyConfiguration;

/**
 * Base class for JUnit test cases.
 */
public class BaseTestCaseMetadata extends TestCase {

    /** Base path for data files. */
    public static final String DATA_PATH = "/data/edu/internet2/middleware/shibboleth/common/attribute/filtering/provider/match/basic";

    /** Base path to metadata files. */
    public static final String MD_PATH = "/data/edu/internet2/middleware/shibboleth/common/config/metadata";

    /** The metadata file that will be used during this test. */
    protected String metadataFile = MD_PATH + "/Metadata1.xml";

    /** EntityID of the message issuer. */
    protected String issuerEntityId = "urn:exmaple.org:issuer";

    /** EntityID of the message requester. */
    protected String requesterEntityId = "urn:exmaple.org:requester";

    /** Parser for metadata. */
    protected BasicParserPool parser;

    /** Provider loaded with metadata. */
    protected FilesystemMetadataProvider metadataProvider;

    /** Request Context included in filter context. */
    protected BaseSAMLProfileRequestContext requestContext;

    /** Simple filtering context for use by tests. */
    protected ShibbolethFilteringContext filterContext;

    /** A simple attribute included in filterContext. */
    protected BaseAttribute<Integer> iAttribute;

    /** A simple attribute included in filterContext. */
    protected BaseAttribute<String> sAttribute;

    /** The Functor under test. */
    protected MatchFunctor matchFunctor;

    /**
     * Constructor.
     */
    public BaseTestCaseMetadata() {
        //
        // initialize SAML in here - it's a bit costly to do it for every test.
        //

        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            fail(e.getMessage());
        }

        parser = new BasicParserPool();
        parser.setNamespaceAware(true);
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        //
        // We load the test data from the class path
        //

        URL mdURL = BaseTestCase.class.getResource(metadataFile);
        File mdFile = new File(mdURL.toURI());

        //
        // Summon up something to read the metadata
        //

        metadataProvider = new FilesystemMetadataProvider(mdFile);
        metadataProvider.setParserPool(parser);
        metadataProvider.initialize();

        //
        // Create a simple configuration
        //

        RelyingPartyConfiguration rpConfig = new RelyingPartyConfiguration(requesterEntityId, issuerEntityId);

        //
        // Build the request context from the metadata (with attached parser) and config
        //

        requestContext = new BaseSAMLProfileRequestContext();
        requestContext.setMetadataProvider(metadataProvider);
        requestContext.setRelyingPartyConfiguration(rpConfig);
        requestContext.setOutboundMessageIssuer(issuerEntityId);
        requestContext.setLocalEntityMetadata(metadataProvider.getEntityDescriptor(issuerEntityId));
        requestContext.setInboundMessageIssuer(requesterEntityId);
        requestContext.setPeerEntityMetadata(metadataProvider.getEntityDescriptor(requesterEntityId));

        //
        // And the filter context from the request context (with no attributes)
        //

        filterContext = new ShibbolethFilteringContext(new TreeMap<String, BaseAttribute>(), requestContext);

    }

    /** Placeholder to allow us to test an entire folder. */
    public void testBase() {
    }
}