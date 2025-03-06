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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.joda.time.DateTime;
import org.opensaml.util.resource.AbstractFilteredResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;

/**
 * A resource representing a file fetch from a Subversion server.
 * 
 * This resource will fetch the given resource as follows:
 * <ul>
 * <li>If the revision is a positive number the resource will fetch the resource once during construction time and will
 * never attempt to fetch it again.</li>
 * <li>If the revision number is zero or less, signaling the HEAD revision, every call this resource will cause the
 * resource to check to see if the current working copy is the same as the revision in the remote repository. If it is
 * not the new revision will be retrieved.</li>
 * </ul>
 * 
 * The behavior of multiple {@link edu.internet2.middleware.shibboleth.common.resource.SVNResource} operating on the same local copy are undefined.
 * 
 * @since 1.1
 */
public class SVNResource extends AbstractFilteredResource {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(SVNResource.class);

    /** SVN Client manager. */
    private final SVNClientManager clientManager;

    /** URL to the remote repository. */
    private SVNURL remoteRepository;

    /** Directory where the working copy will be kept. */
    private File workingCopyDirectory;

    /** Revision of the working copy. */
    private SVNRevision retrievalRevision;

    /** File, within the working copy, represented by this resource. */
    private String resourceFileName;

    /** Time the resource file was last modified. */
    private DateTime lastModified;

    /**
     * Constructor.
     * 
     * @param svnClientMgr manager used to create SVN clients
     * @param repositoryUrl URL of the remote repository
     * @param workingCopy directory that will serve as the root of the local working copy
     * @param workingRevision revision of the resource to retrieve or -1 for HEAD revision
     * @param resourceFile file, within the working copy, represented by this resource
     * 
     * @throws ResourceException thrown if there is a problem initializing the SVN resource
     */
    public SVNResource(SVNClientManager svnClientMgr, SVNURL repositoryUrl, File workingCopy, long workingRevision,
            String resourceFile) throws ResourceException {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
        if (svnClientMgr == null) {
            log.error("SVN client manager may not be null");
            throw new IllegalArgumentException("SVN client manager may not be null");
        }
        clientManager = svnClientMgr;

        if (repositoryUrl == null) {
            throw new IllegalArgumentException("SVN repository URL may not be null");
        }
        remoteRepository = repositoryUrl;

        try {
            checkWorkingCopyDirectory(workingCopy);
            workingCopyDirectory = workingCopy;
        } catch (ResourceException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        if (workingRevision < 0) {
            this.retrievalRevision = SVNRevision.HEAD;
        } else {
            this.retrievalRevision = SVNRevision.create(workingRevision);
        }

        resourceFileName = DatatypeHelper.safeTrimOrNullString(resourceFile);
        if (resourceFileName == null) {
            log.error("SVN working copy resource file name may not be null or empty");
            throw new IllegalArgumentException("SVN working copy resource file name may not be null or empty");
        }

        checkoutOrUpdateResource();
        if (!getResourceFile().exists()) {
            log.error("Resource file " + resourceFile + " does not exist in SVN working copy directory "
                    + workingCopy.getAbsolutePath());
            throw new ResourceException("Resource file " + resourceFile
                    + " does not exist in SVN working copy directory " + workingCopy.getAbsolutePath());
        }
    }

    /** {@inheritDoc} */
    public boolean exists() throws ResourceException {
        return getResourceFile().exists();
    }

    /** {@inheritDoc} */
    public InputStream getInputStream() throws ResourceException {
        checkoutOrUpdateResource();
        try {
            return applyFilter(new FileInputStream(getResourceFile()));
        } catch (IOException e) {
            String erroMsg = "Unable to read resource file " + resourceFileName + " from local working copy "
                    + workingCopyDirectory.getAbsolutePath();
            log.error(erroMsg, e);
            throw new ResourceException(erroMsg, e);
        }
    }

    /** {@inheritDoc} */
    public DateTime getLastModifiedTime() throws ResourceException {
        checkoutOrUpdateResource();
        return lastModified;
    }

    /** {@inheritDoc} */
    public String getLocation() {
        return remoteRepository.toDecodedString() + "/" + resourceFileName;
    }

    /**
     * Gets {@link java.io.File} for the resource.
     * 
     * @return file for the resource
     * 
     * @throws ResourceException thrown if there is a problem fetching the resource or checking on its status
     */
    protected File getResourceFile() throws ResourceException {
        return new File(workingCopyDirectory, resourceFileName);
    }

    /**
     * Checks that the given file exists, or can be created, is a directory, and is read/writable by this process.
     * 
     * @param directory the directory to check
     * 
     * @throws ResourceException thrown if the file is invalid
     */
    protected void checkWorkingCopyDirectory(File directory) throws ResourceException {
        if (directory == null) {
            log.error("SVN working copy directory may not be null");
            throw new ResourceException("SVN working copy directory may not be null");
        }

        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                log.error("SVN working copy direction " + directory.getAbsolutePath()
                        + " does not exist and could not be created");
                throw new ResourceException("SVN working copy direction " + directory.getAbsolutePath()
                        + " does not exist and could not be created");
            }
        }

        if (!directory.isDirectory()) {
            log.error("SVN working copy location " + directory.getAbsolutePath() + " is not a directory");
            throw new ResourceException("SVN working copy location " + directory.getAbsolutePath()
                    + " is not a directory");
        }

        if (!directory.canRead()) {
            log.error("SVN working copy directory " + directory.getAbsolutePath() + " can not be read by this process");
            throw new ResourceException("SVN working copy directory " + directory.getAbsolutePath()
                    + " can not be read by this process");
        }

        if (!directory.canWrite()) {
            log.error("SVN working copy directory " + directory.getAbsolutePath()
                    + " can not be written to by this process");
            throw new ResourceException("SVN working copy directory " + directory.getAbsolutePath()
                    + " can not be written to by this process");
        }
    }

    /**
     * Checks out the resource specified by the {@link #remoteRepository} in to the working copy
     * {@link #workingCopyDirectory}. If the working copy is empty than an SVN checkout is performed if the working copy
     * already exists then an SVN update is performed.
     * 
     * @throws ResourceException thrown if there is a problem communicating with the remote repository, the revision
     *             does not exist, or the working copy is unusable
     */
    protected void checkoutOrUpdateResource() throws ResourceException {
        log.debug("checking out or updating working copy");
        SVNRevision newRevision;

        if (!workingCopyDirectoryExists()) {
            log.debug("working copy does not yet exist, checking it out");
            newRevision = checkoutResourceDirectory();
        } else {
            if (retrievalRevision != SVNRevision.HEAD) {
                log.debug("Working copy exists and version is pegged at {}, no need to update",
                        retrievalRevision.toString());
                return;
            }
            log.debug("Working copy exists, updating to latest version.");
            newRevision = updateResourceDirectory();
        }

        log.debug("Determing last modification date of revision {}", newRevision.getNumber());
        lastModified = getLastModificationForRevision(newRevision);
    }

    /**
     * Checks to see if the working copy directory exists.
     * 
     * @return true if the working copy directory exists, false otherwise
     */
    private boolean workingCopyDirectoryExists() {
        File svnMetadataDir = new File(workingCopyDirectory, ".svn");
        return svnMetadataDir.exists();
    }

    /**
     * Fetches the content from the SVN repository and creates the local working copy.
     * 
     * @return the revision of the fetched content
     * 
     * @throws ResourceException thrown if there is a problem checking out the content from the repository
     */
    private SVNRevision checkoutResourceDirectory() throws ResourceException {
        try {
            long newRevision = clientManager.getUpdateClient().doCheckout(remoteRepository, workingCopyDirectory,
                    retrievalRevision, retrievalRevision, SVNDepth.INFINITY, true);
            log.debug(
                    "Checked out revision {} from remote repository {} and stored it in local working directory {}",
                    new Object[] { newRevision, remoteRepository.toDecodedString(),
                            workingCopyDirectory.getAbsolutePath(), });
            return SVNRevision.create(newRevision);
        } catch (SVNException e) {
            String errMsg = "Unable to check out revsion " + retrievalRevision.toString() + " from remote repository "
                    + remoteRepository.toDecodedString() + " to local working directory "
                    + workingCopyDirectory.getAbsolutePath();
            log.error(errMsg, e);
            throw new ResourceException(errMsg, e);
        }
    }

    /**
     * Updates an existing local working copy from the repository.
     * 
     * @return the revision of the fetched content
     * 
     * @throws ResourceException thrown if there is a problem updating the working copy
     */
    private SVNRevision updateResourceDirectory() throws ResourceException {
        try {
            long newRevision = clientManager.getUpdateClient().doUpdate(workingCopyDirectory, retrievalRevision,
                    SVNDepth.INFINITY, true, true);
            log.debug("Updated local working directory {} to revision {} from remote repository {}", new Object[] {
                    workingCopyDirectory.getAbsolutePath(), newRevision, remoteRepository.toDecodedString(), });
            return SVNRevision.create(newRevision);
        } catch (SVNException e) {
            String errMsg = "Unable to update working copy of resoure " + remoteRepository.toDecodedString()
                    + " in working copy " + workingCopyDirectory.getAbsolutePath() + " to revsion "
                    + retrievalRevision.toString();
            log.error(errMsg, e);
            throw new ResourceException(errMsg, e);
        }
    }

    /**
     * Gets the last modified time for the given revision.
     * 
     * @param revision revision to get the last modified date for
     * 
     * @return the last modified time
     * 
     * @throws ResourceException thrown if there is a problem getting the last modified time
     */
    private DateTime getLastModificationForRevision(SVNRevision revision) throws ResourceException {
        try {
            SVNStatusHandler handler = new SVNStatusHandler();
            clientManager.getStatusClient().doStatus(getResourceFile(), revision, SVNDepth.INFINITY, true, true, false,
                    false, handler, null);
            SVNStatus status = handler.getStatus();

            // remote revision is null when using a pegged version or when using HEAD and the version has not changed
            if (status.getRemoteRevision() == null) {
                return new DateTime(status.getCommittedDate());
            } else {
                return new DateTime(status.getRemoteDate());
            }
        } catch (SVNException e) {
            String errMsg = "Unable to check status of resource " + resourceFileName + " within working directory "
                    + workingCopyDirectory.getAbsolutePath();
            log.error(errMsg, e);
            throw new ResourceException(errMsg, e);
        }
    }

    /** Simple {@link ISVNStatusHandler} implementation that just stores and returns the status. */
    private class SVNStatusHandler implements ISVNStatusHandler {

        /** Current status of the resource. */
        private SVNStatus status;

        /**
         * Gets the current status of the resource.
         * 
         * @return current status of the resource
         */
        public SVNStatus getStatus() {
            return status;
        }

        /** {@inheritDoc} */
        public void handleStatus(SVNStatus currentStatus) throws SVNException {
            status = currentStatus;
        }
    }
}