package org.apache.axis.encoding.ser;

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

import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.DeserializerImpl;
import org.apache.axis.message.MessageElement;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;
import org.xml.sax.SAXException;

import java.util.ArrayList;

/**
 * Deserializer for DOM Document
 *
 * @author Davanum Srinivas <dims@yahoo.com>
 */
public class DocumentDeserializer extends DeserializerImpl
{
    protected static Log log =
        LogFactory.getLog(DocumentDeserializer.class.getName());

   public static final String DESERIALIZE_CURRENT_ELEMENT = "DeserializeCurrentElement";

    public final void onEndElement(String namespace, String localName,
                                   DeserializationContext context)
        throws SAXException
    {
        try {
            MessageElement msgElem = context.getCurElement();
            if ( msgElem != null ) {
                MessageContext messageContext = context.getMessageContext();
                Boolean currentElement = (Boolean) messageContext.getProperty(DESERIALIZE_CURRENT_ELEMENT);
                if (currentElement != null && currentElement.booleanValue()) {
                    value = msgElem.getAsDocument();
                    messageContext.setProperty(DESERIALIZE_CURRENT_ELEMENT, Boolean.FALSE);
                    return;
                }
                ArrayList children = msgElem.getChildren();
                if ( children != null ) {
                    msgElem = (MessageElement) children.get(0);
                    if ( msgElem != null )
                        value = msgElem.getAsDocument();
                }
            }
        }
        catch( Exception exp ) {
            log.error(Messages.getMessage("exception00"), exp);
            throw new SAXException( exp );
        }
    }
}