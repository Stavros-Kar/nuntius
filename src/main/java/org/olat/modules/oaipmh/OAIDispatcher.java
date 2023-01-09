/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.oaipmh;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OAIDispatcher implements Dispatcher {

    private static final Logger log = Tracing.createLoggerFor(OAIDispatcher.class);

    @Autowired
    private OAIService oaiService;

    @Autowired
    private OAIPmhModule oaiPmhModule;


    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserRequest ureq;
        final String pathInfo = request.getPathInfo();
        String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);

        try {
            ureq = new UserRequestImpl(uriPrefix, request, response);
        } catch (NumberFormatException nfe) {
            log.debug("Bad Request {}", pathInfo);
            DispatcherModule.sendBadRequest(pathInfo, response);
            return;
        }

        if(pathInfo.contains("close-window")) {
            DispatcherModule.setNotContent(request.getPathInfo(), response);
            return;
        }

        // Controller has been created already
        if (ureq.isValidDispatchURI()) {
            dispatch(ureq);
            return;
        }

        if (!oaiPmhModule.isEnabled()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            String requestVerbParam = ureq.getParameter("verb");
            String requestIdentifierParam = ureq.getParameter("identifier");
            String requestMetadataPrefixParameter = ureq.getParameter("metadataprefix");
            String requestResumptionTokenParameter = ureq.getParameter("resumptiontoken");
            String requestFromParameter = ureq.getParameter("from");
            String requestUntilParameter = ureq.getParameter("until");
            String requestSetParameter = ureq.getParameter("set");

            MediaResource mr =
                    oaiService.handleOAIRequest(requestVerbParam, requestIdentifierParam,
                            requestMetadataPrefixParameter, requestResumptionTokenParameter,
                            requestFromParameter, requestUntilParameter,
                            requestSetParameter);

            response.setCharacterEncoding("UTF-8");
            response.setContentType(mr.getContentType());

            ServletUtil.serveResource(request, response, mr);
        } catch (Exception e) {
            //
        }
    }

    private void dispatch(UserRequest ureq) {
        UserSession usess = ureq.getUserSession();
        Windows windows = Windows.getWindows(usess);

        ChiefController chiefController = windows.getChiefController(ureq);
        try {
            Window w = chiefController.getWindow().getWindowBackOffice().getWindow();
            w.dispatchRequest(ureq, false); // renderOnly
        } catch (Exception e) {
            log.error("", e);
        }
    }

}