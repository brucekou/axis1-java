/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.axis.deployment;

import org.apache.axis.Constants;
import org.apache.axis.Handler;
import org.apache.axis.deployment.wsdd.WSDDGlobalConfiguration;
import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.axis.deployment.wsdd.WSDDHandler;
import org.apache.axis.encoding.SOAPTypeMappingRegistry;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.w3c.dom.Document;

import javax.xml.rpc.namespace.QName;
import java.util.Hashtable;


/**
 * This is a simple implementation of the DeploymentRegistry class
 *
 * @author James Snell
 * @author Glen Daniels (gdaniels@macromedia.com)
 */
public class SimpleDeploymentManager
    extends DeploymentRegistry
{
    /** Our global configuration */
    WSDDGlobalConfiguration globalConfig;

    /** Storage for our handlers */
    Hashtable handlers = new Hashtable();
    Hashtable services = new Hashtable();
    Hashtable transports = new Hashtable();

    /** Storage for our TypeMappingRegistries */
    Hashtable mappings;
    
    /** The deployment document we're rooted off of.
     * This will be updated as new items are deployed into the registry.
     */
    DeploymentDocument doc = null;

    /**
     * Constructor - sets up a default encoding style
     */
    public SimpleDeploymentManager()
    {
        mappings = new Hashtable();

        mappings.put(Constants.URI_SOAP_ENC, new SOAPTypeMappingRegistry());
    }

    /**
     * Deploy a deployment document into this registry.
     * 
     * @param deployment the DeploymentDocument we'll operate on
     * @throws DeploymentException if there was a problem
     */
    public void deploy(DeploymentDocument deployment)
        throws DeploymentException
    {
        if (doc == null)
            doc = deployment;
        deployment.deploy(this);
    }

    /**
     * Obtain our "root" deployment document.
     */ 
    public DeploymentDocument getConfigDocument()
            throws DeploymentException {
        return doc;
    }

    /**
     * Return the global configuration
     * 
     * @return our global configuration
     * @throws DeploymentException XXX
     */
    public WSDDGlobalConfiguration getGlobalConfiguration()
        throws DeploymentException
    {
        return globalConfig;
    }

    /**
     * Set the global configuration
     * @param global XXX
     */
    public void setGlobalConfiguration(WSDDGlobalConfiguration global)
    {
        globalConfig = global;
    }

    /**
     * Deploy the given WSDD Deployable Item
     * @param item XXX
     * @throws DeploymentException XXX
     */
    public void deployItem(DeployableItem item)
        throws DeploymentException
    {
        QName qn = item.getQName();
        handlers.put(qn, item);
    }

    /**
     * Deploy the given WSDD Deployable Item
     * @param item XXX
     * @throws DeploymentException XXX
     */
    public void deployHandler(DeployableItem item)
        throws DeploymentException
    {
        if (doc == null) doc = new WSDDDocument();
        handlers.put(item.getQName(), item);
        doc.importItem(item);
    }

    /**
     * Deploy the given WSDD Deployable Item
     * @param item XXX
     * @throws DeploymentException XXX
     */
    public void deployService(DeployableItem item)
        throws DeploymentException
    {
        if (doc == null) doc = new WSDDDocument();
        services.put(item.getQName(), item);
        doc.importItem(item);
    }

    /**
     * Deploy the given WSDD Transport
     * @param item XXX
     * @throws DeploymentException XXX
     */
    public void deployTransport(DeployableItem item)
        throws DeploymentException
    {
        if (doc == null) doc = new WSDDDocument();
        transports.put(item.getQName(), item);
        doc.importItem(item);
    }

    /**
     * Return an instance of the deployed item
     * @param qname XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public Handler getHandler(QName qname)
        throws DeploymentException
    {
        try {
            DeployableItem item = (DeployableItem)handlers.get(qname);
            
            if (item == null)
                return null;

            return item.getInstance(this);
        }
        catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
    }

    /**
     * Return an instance of the deployed service
     * @param qname XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public Handler getService(QName qname)
        throws DeploymentException
    {
        try {
            DeployableItem item = (DeployableItem)services.get(qname);
            
            if (item == null)
                return null;

            return item.getInstance(this);
        }
        catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
    }

    /**
     * Return an instance of the deployed transport
     * @param qname XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public Handler getTransport(QName qname)
        throws DeploymentException
    {
        try {
            DeployableItem item = (DeployableItem)transports.get(qname);
            
            if (item == null)
                return null;

            return item.getInstance(this);
        }
        catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
    }

    /**
     * remove the given item
     * @param name XXX
     * @throws DeploymentException XXX
     */
    public void removeDeployedItem(QName qname)
        throws DeploymentException
    {
        handlers.remove(qname);
    }


    /**
     * return the named mapping registry
     * @param encodingStyle XXX
     * @return XXX
     * @throws DeploymentException XXX
     */
    public TypeMappingRegistry getTypeMappingRegistry(String encodingStyle)
        throws DeploymentException
    {

        TypeMappingRegistry tmr =
            (TypeMappingRegistry) mappings.get(encodingStyle);

        return tmr;
    }

    /**
     * adds a new mapping registry
     * @param encodingStyle XXX
     * @param tmr XXX
     */
    public void addTypeMappingRegistry(String encodingStyle,
                                       TypeMappingRegistry tmr)
    {
        mappings.put(encodingStyle, tmr);
    }

    /**
     * remove the named mapping registry
     * @param encodingStyle XXX
     */
    public void removeTypeMappingRegistry(String encodingStyle)
    {
        mappings.remove(encodingStyle);
    }
}
