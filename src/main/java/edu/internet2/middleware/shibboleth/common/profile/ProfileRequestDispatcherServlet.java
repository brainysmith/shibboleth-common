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

package edu.internet2.middleware.shibboleth.common.profile;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.ws.transport.http.HTTPInTransport;
import org.opensaml.ws.transport.http.HTTPOutTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.shibboleth.common.log.AccessLogEntry;

/**
 * Servlet responsible for dispatching incoming requests to the appropriate {@link ProfileHandler}.
 */
public class ProfileRequestDispatcherServlet extends HttpServlet {

    /** Serial version UID. */
    private static final long serialVersionUID = 3750548606378986211L;

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ProfileRequestDispatcherServlet.class);

    /** Access logger. */
    private final Logger accessLog = LoggerFactory.getLogger(AccessLogEntry.ACCESS_LOGGER_NAME);

    /** Profile handler manager. */
    private ProfileHandlerManager handlerManager;

    /** {@inheritDoc} */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String handlerManagerId = config.getInitParameter("handlerManagerId");
        if (DatatypeHelper.isEmpty(handlerManagerId)) {
            handlerManagerId = "shibboleth.HandlerManager";
        }

        handlerManager = (ProfileHandlerManager) getServletContext().getAttribute(handlerManagerId);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    protected void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException,
            IOException {
        if (accessLog.isInfoEnabled()) {
            AccessLogEntry accessEntry = new AccessLogEntry(httpRequest);
            accessLog.info(accessEntry.toString());
        }

        HTTPInTransport profileReq = new HttpServletRequestAdapter(httpRequest);
        HTTPOutTransport profileResp = new HttpServletResponseAdapter(httpResponse, httpRequest.isSecure());

        AbstractErrorHandler errorHandler = handlerManager.getErrorHandler();
        ProfileHandler handler = handlerManager.getProfileHandler(httpRequest);
        if (handler != null) {
            try {
                handler.processRequest(profileReq, profileResp);
                return;
            }catch(ProfileException e){
                httpRequest.setAttribute(AbstractErrorHandler.ERROR_KEY, e);
            } catch (Throwable t) {
                log.error("Error occurred while processing request", t);
            }
        } else {
            log.warn("No profile handler configured for request at path: {}", httpRequest.getPathInfo());
            httpRequest.setAttribute(AbstractErrorHandler.ERROR_KEY, new NoProfileHandlerException(
                    "No profile handler configured for request at path: " + httpRequest.getPathInfo()));
        }

        errorHandler.processRequest(profileReq, profileResp);
        return;
    }
}