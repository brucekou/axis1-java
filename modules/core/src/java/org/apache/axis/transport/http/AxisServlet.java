/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.axis.transport.http;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.context.SimpleSessionContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.EngineRegistryFactory;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.om.impl.llom.builder.StAXSOAPModelBuilder;


public class AxisServlet extends HttpServlet {
    private EngineRegistry engineRegistry;

    private static final String LIST_MULTIPLE_SERVICE_JSP_NAME = "listServices.jsp";
    private static final String LIST_SINGLE_SERVICE_JSP_NAME = "listSingleService.jsp";

    private final boolean allowListServices = true;
    private final boolean allowListSingleService = true;

   public void init(ServletConfig config) throws ServletException {
        try {
            ServletContext context = config.getServletContext();
            String repoDir = context.getRealPath("/WEB-INF");
            Class erClass = Class.forName("org.apache.axis.deployment.EngineRegistryFactoryImpl");
               EngineRegistryFactory erfac = (EngineRegistryFactory)erClass.newInstance();
               this.engineRegistry = erfac.createEngineRegistry(repoDir);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }


    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        String filePart = httpServletRequest.getRequestURL().toString();

        if (allowListServices && filePart != null && filePart.endsWith(Constants.LISTSERVICES)) {
            listServices(httpServletRequest,httpServletResponse);
            return;
        }else{
            if (allowListSingleService){
                listService(httpServletRequest,httpServletResponse,filePart);
                return;
            }

        }
    }

    /* (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            res.setContentType("text/xml; charset=utf-8");
            AxisEngine engine = new AxisEngine(engineRegistry);


            Object sessionContext = req.getSession().getAttribute(Constants.SESSION_CONTEXT_PROPERTY);
            if(sessionContext == null){
                sessionContext = new SimpleSessionContext();
                req.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY,sessionContext);
            }

            MessageContext msgContext = new MessageContext(engineRegistry, null,(SessionContext)sessionContext);

            msgContext.setServerSide(true);
            String filePart = req.getRequestURL().toString();
            msgContext.setTo(new EndpointReference(AddressingConstants.WSA_TO, filePart));
            String soapActionString = req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);

            if (soapActionString != null) {
                msgContext.setProperty(MessageContext.SOAP_ACTION, soapActionString);
            }

            XMLStreamReader reader =
                    XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(req.getInputStream()));
            StAXBuilder builder =
                    new StAXSOAPModelBuilder(OMFactory.newInstance(), reader);
            msgContext.setEnvelope((SOAPEnvelope) builder.getDocumentElement());

            msgContext.setProperty(MessageContext.TRANSPORT_TYPE,
                    Constants.TRANSPORT_HTTP);
            msgContext.setProperty(MessageContext.TRANSPORT_WRITER,
                    res.getWriter());

            engine.receive(msgContext);
        } catch (AxisFault e) {
            throw new ServletException(e);
        } catch (XMLStreamException e) {
            throw new ServletException(e);
        } catch (FactoryConfigurationError e) {
            throw new ServletException(e);
        }

    }

    private void listServices(HttpServletRequest req,HttpServletResponse res) throws IOException {
        HashMap services = engineRegistry.getServices();
        req.getSession().setAttribute(Constants.SERVICE_MAP,services);
        res.sendRedirect(LIST_MULTIPLE_SERVICE_JSP_NAME);
    }

    private void listService (HttpServletRequest req,HttpServletResponse res,String filePart) throws IOException {
        String serviceName =filePart.substring(filePart.lastIndexOf("/")+1,filePart.length());
        HashMap services = engineRegistry.getServices();
        if (services!=null && !services.isEmpty()){
            Object serviceObj = services.get(new QName(serviceName));
            if (serviceObj!=null){
                req.getSession().setAttribute(Constants.SINGLE_SERVICE,serviceObj);
            }
        }
        String URI =  req.getRequestURI();
        URI = URI.substring(0,URI.indexOf("services"));

        res.sendRedirect(URI + LIST_SINGLE_SERVICE_JSP_NAME);
    }



}