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

package edu.internet2.middleware.shibboleth.common.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

/** Unit test for {@link SVNResource }. */
public class SVNResourceTest extends TestCase {

    private SVNClientManager clientManager;

    private File repoDir;

    private File workingCopyDirectory;

    private File altWorkingCopyDirectory;

    private String schemaFileName;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        FSRepositoryFactory.setup();
        clientManager = SVNClientManager.newInstance();

        repoDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "svnrepo");
        SVNURL repoUrl = SVNRepositoryFactory.createLocalRepository(repoDir, false, true);
        File schemaDir = new File(SVNResourceTest.class.getResource("/schema").toURI());
        clientManager.getCommitClient().doImport(schemaDir, repoUrl, null, null, false, false, SVNDepth.INFINITY);

        altWorkingCopyDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + "svnaltwc");
        clientManager.getUpdateClient().doCheckout(repoUrl, altWorkingCopyDirectory, SVNRevision.HEAD,
                SVNRevision.HEAD, SVNDepth.INFINITY, true);

        workingCopyDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + "svnwc");

        schemaFileName = "shibboleth-2.0-resource.xsd";
    }

    /** {@inheritDoc} */
    protected void tearDown() throws Exception {
        deleteDirectory(workingCopyDirectory);
        deleteDirectory(altWorkingCopyDirectory);
        deleteDirectory(repoDir);
    }

    public void test() throws Exception {
        SVNURL svnurl = SVNURL.parseURIDecoded("file://" + repoDir.getAbsolutePath());
        File schemaFile = new File(workingCopyDirectory, schemaFileName);

        SVNResource resource = new SVNResource(clientManager, svnurl, workingCopyDirectory, -1, schemaFileName);

        // directory should contain the 19 schema files plus the .svn directory
        assertEquals(20, workingCopyDirectory.list().length);
        assertTrue(schemaFile.exists());
        assertEquals(5148, schemaFile.length());
        assertTrue(resource.exists());
        assertEquals("file://" + repoDir.getAbsolutePath() + "/" + schemaFileName, resource.getLocation());

        DateTime lastModified = resource.getLastModifiedTime();
        assertTrue(lastModified.isBeforeNow());

        modifySchemaFile(16);

        // exist() and getLocation() should not force an update
        assertTrue(resource.exists());
        assertEquals("file://" + repoDir.getAbsolutePath() + "/" + schemaFileName, resource.getLocation());
        assertEquals(20, workingCopyDirectory.list().length);
        assertTrue(schemaFile.exists());
        assertEquals(5148, schemaFile.length());

        // getLastModified() should force an update
        assertTrue(lastModified.isBefore(resource.getLastModifiedTime()));
        assertEquals(20, workingCopyDirectory.list().length);
        assertTrue(schemaFile.exists());
        assertEquals(16, schemaFile.length());

        modifySchemaFile(32);

        // getInputStream() should force an update
        assertTrue(lastModified.isBefore(resource.getLastModifiedTime()));
        assertEquals(20, workingCopyDirectory.list().length);
        assertTrue(schemaFile.exists());
        assertEquals(32, schemaFile.length());
    }

    private void modifySchemaFile(int numOfRandomBytes) throws Exception {
        Random randomGen = new Random();
        byte[] randomBytes = new byte[numOfRandomBytes];
        randomGen.nextBytes(randomBytes);

        File schemaFile = new File(altWorkingCopyDirectory, schemaFileName);
        FileOutputStream out = new FileOutputStream(schemaFile);
        out.write(randomBytes);
        out.flush();
        out.close();

        clientManager.getCommitClient().doCommit(new File[] { schemaFile }, false, null, null, null, false, false,
                SVNDepth.FILES);
    }

    /**
     * Used to recursively delete a directory.
     * 
     * @param directory directory to be deleted
     */
    private void deleteDirectory(File directory) throws Exception {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }

        directory.delete();
    }
}