/*
 * The Apache Software License, Version 1.1
 * 
 * 
 * Copyright (c) 2001-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The end-user documentation
 * included with the redistribution, if any, must include the following
 * acknowledgment: "This product includes software developed by the Apache
 * Software Foundation (http://www.apache.org/)." Alternately, this
 * acknowledgment may appear in the software itself, if and wherever such
 * third-party acknowledgments normally appear. 4. The names "Axis" and "Apache
 * Software Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written permission,
 * please contact apache@apache.org. 5. Products derived from this software may
 * not be called "Apache", nor may "Apache" appear in their name, without prior
 * written permission of the Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation. For more information on the
 * Apache Software Foundation, please see <http://www.apache.org/> .
 */

package org.apache.axis.message;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.*;

import java.util.Vector;
import java.util.Iterator;

/**
 * A W3C simple DOM NameNodeMap implementation
 *
 * @author Heejune Ahn (cityboy@tmax.co.kr)
 */
public class NamedNodeMapImpl implements  NamedNodeMap {


  /** Nodes. */
  protected Vector nodes;

  static private Document doc = null;
  static{
    try {
      org.w3c.dom.Document doc = org.apache.axis.utils.XMLUtils.newDocument();
    } catch (javax.xml.parsers.ParserConfigurationException e) {
      throw new org.apache.axis.InternalException(e);
    }
  }

  public NamedNodeMapImpl()
  {
    nodes = new Vector();
  }
  /**
   * Retrieves a node specified by name.
   * @param name The <code>nodeName</code> of a node to retrieve.
   * @return A <code>Node</code> (of any type) with the specified
   *   <code>nodeName</code>, or <code>null</code> if it does not identify
   *   any node in this map.
   */
  public Node getNamedItem(String name){
    if(name == null   ){
      Thread.dumpStack();
      throw new java.lang.IllegalArgumentException("local name = null");
    }

   for(Iterator iter = nodes.iterator(); iter.hasNext();){
     Attr attr = (Attr)iter.next();
     if(name.equals(attr.getName())){
       return attr;
     }
   }
   return null;
  }

    /**
     * Adds a node using its <code>nodeName</code> attribute. If a node with
     * that name is already present in this map, it is replaced by the new
     * one.
     * <br>As the <code>nodeName</code> attribute is used to derive the name
     * which the node must be stored under, multiple nodes of certain types
     * (those that have a "special" string value) cannot be stored as the
     * names would clash. This is seen as preferable to allowing nodes to be
     * aliased.
     * @param arg A node to store in this map. The node will later be
     *   accessible using the value of its <code>nodeName</code> attribute.
     * @return If the new <code>Node</code> replaces an existing node the
     *   replaced <code>Node</code> is returned, otherwise <code>null</code>
     *   is returned.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if <code>arg</code> was created from a
     *   different document than the one that created this map.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this map is readonly.
     *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>arg</code> is an
     *   <code>Attr</code> that is already an attribute of another
     *   <code>Element</code> object. The DOM user must explicitly clone
     *   <code>Attr</code> nodes to re-use them in other elements.
     *   <br>HIERARCHY_REQUEST_ERR: Raised if an attempt is made to add a node
     *   doesn't belong in this NamedNodeMap. Examples would include trying
     *   to insert something other than an Attr node into an Element's map
     *   of attributes, or a non-Entity node into the DocumentType's map of
     *   Entities.
     */
    public Node setNamedItem(Node arg) throws DOMException
    {

         String name    = arg.getNodeName();

         if(name == null   ){
           Thread.dumpStack();
           throw new java.lang.IllegalArgumentException("local name = null");
         }

         for(int i = 0; i <  nodes.size(); i++){
           Attr attr = (Attr)nodes.get(i);
           // search if we have already
           if(name.equals(attr.getName())){
             nodes.remove(i);
             nodes.add(i, arg);
             return attr; // return old one
           }
         }

         // if cannot found
         nodes.add(arg);
         return null;
    }

    /**
     * Removes a node specified by name. When this map contains the attributes
     * attached to an element, if the removed attribute is known to have a
     * default value, an attribute immediately appears containing the
     * default value as well as the corresponding namespace URI, local name,
     * and prefix when applicable.
     * @param name The <code>nodeName</code> of the node to remove.
     * @return The node removed from this map if a node with such a name
     *   exists.
     * @exception DOMException
     *   NOT_FOUND_ERR: Raised if there is no node named <code>name</code> in
     *   this map.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this map is readonly.
     */
    public Node removeNamedItem(String name) throws DOMException
    {
       if(name == null ){
         Thread.dumpStack();
         throw new java.lang.IllegalArgumentException("local name = null");
       }
       for(int i = 0; i <  nodes.size(); i++){
         Attr attr = (Attr)nodes.get(i);
         // search if we have already
         if(name.equals(attr.getLocalName())){
           nodes.remove(i);
           return attr; // return old one
         }
      }
      return null;
    }

    /**
     * Returns the <code>index</code>th item in the map. If <code>index</code>
     * is greater than or equal to the number of nodes in this map, this
     * returns <code>null</code>.
     * @param index Index into this map.
     * @return The node at the <code>index</code>th position in the map, or
     *   <code>null</code> if that is not a valid index.
     */
    public Node item(int index){
      return (nodes != null && index < nodes.size()) ?
                    (Node)(nodes.elementAt(index)) : null;
    }

    /**
     * The number of nodes in this map. The range of valid child node indices
     * is <code>0</code> to <code>length-1</code> inclusive.
     */
    public int getLength(){
      return (nodes != null) ? nodes.size() : 0;
    }

    /**
     * Retrieves a node specified by local name and namespace URI.
     * <br>Documents which do not support the "XML" feature will permit only
     * the DOM Level 1 calls for creating/setting elements and attributes.
     * Hence, if you specify a non-null namespace URI, these DOMs will never
     * find a matching node.
     * @param namespaceURI The namespace URI of the node to retrieve.
     * @param localName The local name of the node to retrieve.
     * @return A <code>Node</code> (of any type) with the specified local
     *   name and namespace URI, or <code>null</code> if they do not
     *   identify any node in this map.
     * @since DOM Level 2
     */
    public Node getNamedItemNS(String namespaceURI, String localName){

      if(namespaceURI == null) namespaceURI = "";
      if(localName == null   ){
        Thread.dumpStack();
        throw new java.lang.IllegalArgumentException("local name = null");
      }

     for(Iterator iter = nodes.iterator(); iter.hasNext();){
       Attr attr = (Attr)iter.next();
       if(namespaceURI.equals(attr.getNamespaceURI()) &&
          namespaceURI.equals(attr.getLocalName())){
         return attr;
       }
     }
     return null;
    }

    /**
     * Adds a node using its <code>namespaceURI</code> and
     * <code>localName</code>. If a node with that namespace URI and that
     * local name is already present in this map, it is replaced by the new
     * one.
     * @param arg A node to store in this map. The node will later be
     *   accessible using the value of its <code>namespaceURI</code> and
     *   <code>localName</code> attributes.
     * @return If the new <code>Node</code> replaces an existing node the
     *   replaced <code>Node</code> is returned, otherwise <code>null</code>
     *   is returned.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if <code>arg</code> was created from a
     *   different document than the one that created this map.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this map is readonly.
     *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>arg</code> is an
     *   <code>Attr</code> that is already an attribute of another
     *   <code>Element</code> object. The DOM user must explicitly clone
     *   <code>Attr</code> nodes to re-use them in other elements.
     *   <br>HIERARCHY_REQUEST_ERR: Raised if an attempt is made to add a node
     *   doesn't belong in this NamedNodeMap. Examples would include trying
     *   to insert something other than an Attr node into an Element's map
     *   of attributes, or a non-Entity node into the DocumentType's map of
     *   Entities.
     *   <br>NOT_SUPPORTED_ERR: Always thrown if the current document does not
     *   support the <code>"XML"</code> feature, since namespaces were
     *   defined by XML.
     * @since DOM Level 2
     */
    public Node setNamedItemNS(Node arg) throws DOMException
    {
      String namespaceURI = arg.getNamespaceURI();
      String localName    = arg.getLocalName();

      if(namespaceURI == null) namespaceURI = "";
      if(localName == null   ){
        Thread.dumpStack();
        throw new java.lang.IllegalArgumentException("local name = null");
      }

      for(int i = 0; i <  nodes.size(); i++){
        Attr attr = (Attr)nodes.get(i);
        // search if we have already
        if(namespaceURI.equals(attr.getNamespaceURI()) &&
           namespaceURI.equals(attr.getLocalName())){
          nodes.remove(i);
          nodes.add(i, arg);
          return attr; // return old one
        }
      }

      // if cannot found
      nodes.add(arg);
      return null;
    }

    /**
     * Removes a node specified by local name and namespace URI. A removed
     * attribute may be known to have a default value when this map contains
     * the attributes attached to an element, as returned by the attributes
     * attribute of the <code>Node</code> interface. If so, an attribute
     * immediately appears containing the default value as well as the
     * corresponding namespace URI, local name, and prefix when applicable.
     * <br>Documents which do not support the "XML" feature will permit only
     * the DOM Level 1 calls for creating/setting elements and attributes.
     * Hence, if you specify a non-null namespace URI, these DOMs will never
     * find a matching node.
     * @param namespaceURI The namespace URI of the node to remove.
     * @param localName The local name of the node to remove.
     * @return The node removed from this map if a node with such a local
     *   name and namespace URI exists.
     * @exception DOMException
     *   NOT_FOUND_ERR: Raised if there is no node with the specified
     *   <code>namespaceURI</code> and <code>localName</code> in this map.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this map is readonly.
     * @since DOM Level 2
     */
    public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException{

      if(namespaceURI == null) namespaceURI = "";
      if(localName == null   ){
        Thread.dumpStack();
        throw new java.lang.IllegalArgumentException("local name = null");
      }

      for(int i = 0; i <  nodes.size(); i++){
        Attr attr = (Attr)nodes.get(i);
        // search if we have already
        if(namespaceURI.equals(attr.getNamespaceURI()) &&
           localName.equals(attr.getLocalName())){
          nodes.remove(i);
          return attr; // return old one
        }
      }
      return null;

    }


}