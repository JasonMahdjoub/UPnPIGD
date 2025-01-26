/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.distrimind.upnp_igd.model;

import com.distrimind.flexilogxml.exceptions.XMLStreamException;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.flexilogxml.xml.*;
import com.distrimind.flexilogxml.xml.Location;
import com.distrimind.upnp_igd.binding.xml.DescriptorBindingException;
import com.distrimind.upnp_igd.model.action.ActionException;
import com.distrimind.upnp_igd.model.types.InvalidValueException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML handling and printing shortcuts.
 * <p>
 * This class exists because Android 2.1 does not offer any way to print an <code>org.w3c.dom.Document</code>,
 * and it also doesn't implement the most trivial methods to build a DOM (although the API is provided, they
 * fail at runtime). We might be able to remove this class once compatibility for Android 2.1 can be
 * dropped.
 * </p>
 *
 * @author Christian Bauer
 * @author Jason Mahdjoub, use XML Parser instead of Document
 */
public class XMLUtil {


    // TODO: Evil methods to print XML on Android 2.1 (there is no TransformerFactory)



    public static String encodeText(String s) {
        return encodeText(s, true);
    }

    public static String encodeText(String _s, boolean encodeQuotes) {

        String s = _s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        if(encodeQuotes) {
            s = s.replaceAll("'", "&apos;");
            s = s.replaceAll("\"", "&quot;");
        }
        return s;
    }
    public static void appendNewElementIfNotNull(IXmlWriter xmlStreamWriter, Enum<?> el, Object content) throws XMLStreamException {
        appendNewElementIfNotNull(xmlStreamWriter, el, content, (XMLCompleter) null);
    }

    public static void appendNewElementIfNotNull(IXmlWriter xmlStreamWriter, Enum<?> el, Object content, String namespace) throws XMLStreamException {
        appendNewElementIfNotNull(xmlStreamWriter, el.toString(), content, namespace, null);
    }

    public static void appendNewElementIfNotNull(IXmlWriter xmlStreamWriter, String element, Object content) throws XMLStreamException {
        appendNewElementIfNotNull(xmlStreamWriter, element, content, (XMLCompleter) null);
    }
    public static void appendNewElementIfNotNull(IXmlWriter xmlStreamWriter, String prefix, String element, Object content, String namespace) throws XMLStreamException {
        appendNewElementIfNotNull(xmlStreamWriter, prefix, element, content, namespace, null);
    }
    public static void appendNewElementIfNotNull(IXmlWriter xmlStreamWriter, String element, Object content, String namespace) throws XMLStreamException {
        appendNewElementIfNotNull(xmlStreamWriter, element, content, namespace, null);
    }

    public static void appendNewElement(IXmlWriter xmlStreamWriter, String element, Object content) throws XMLStreamException {
        appendNewElement(xmlStreamWriter, element, content, (XMLCompleter) null);
    }

    public static void appendNewElement(IXmlWriter xmlStreamWriter, String element, Object content, String namespace) throws XMLStreamException {
        appendNewElement(xmlStreamWriter, element, content, namespace, null);
    }
    public static void appendNewElementIfNotNull(IXmlWriter xmlStreamWriter, Enum<?> el, Object content, XMLCompleter consumer) throws XMLStreamException {
        appendNewElementIfNotNull(xmlStreamWriter, el, content, null, consumer);
    }

    public static void appendNewElementIfNotNull(IXmlWriter xmlStreamWriter, Enum<?> el, Object content, String namespace, XMLCompleter consumer) throws XMLStreamException {
        appendNewElementIfNotNull(xmlStreamWriter, el.toString(), content, namespace, consumer);
    }

    public static void appendNewElementIfNotNull(IXmlWriter xmlStreamWriter, String element, Object content, XMLCompleter consumer) throws XMLStreamException {
        appendNewElementIfNotNull(xmlStreamWriter, element, content, null, consumer);
    }
    public static void appendNewElementIfNotNull(IXmlWriter xmlStreamWriter, String prefix, String element, Object content, String namespace, XMLCompleter consumer) throws XMLStreamException {
        if (content != null)
            appendNewElement(xmlStreamWriter, prefix, element, content, namespace, consumer);
    }
    public static void appendNewElementIfNotNull(IXmlWriter xmlStreamWriter, String element, Object content, String namespace, XMLCompleter consumer) throws XMLStreamException {
        appendNewElementIfNotNull(xmlStreamWriter, null, element, content, namespace, consumer);
    }

    public static void appendNewElement(IXmlWriter xmlStreamWriter, String element, Object content, XMLCompleter consumer) throws XMLStreamException {
        appendNewElement(xmlStreamWriter, element, content, null, consumer);
    }
    public static void appendNewElement(IXmlWriter xmlStreamWriter, String element, Object content, String namespace, XMLCompleter consumer) throws XMLStreamException {
        appendNewElement(xmlStreamWriter, null, element, content, namespace, consumer);
    }
    public static void appendNewElement(IXmlWriter xmlStreamWriter, String prefix, String element, Object content, String namespace, XMLCompleter consumer) throws XMLStreamException {
        if (content==null && consumer==null) {
            if (prefix!=null && !prefix.isEmpty())
                xmlStreamWriter.writeEmptyElement(prefix, element, namespace);
            else {
                if (namespace == null)
                    xmlStreamWriter.writeEmptyElement(element);
                else
                    xmlStreamWriter.writeEmptyElement(namespace, element);
            }
        }
        else {
            if (namespace==null)
                xmlStreamWriter.writeStartElement(element);
            else {
                if (prefix==null || prefix.isEmpty())
                    xmlStreamWriter.writeStartElement(namespace, element);
                else
                    xmlStreamWriter.writeStartElement(prefix, element, namespace);
            }
            if (consumer!=null)
                consumer.write(xmlStreamWriter);
            if (content!=null)
                xmlStreamWriter.writeCharacters(content.toString());

            xmlStreamWriter.writeEndElement();
        }

    }

    // TODO: Of course, there is no Element.getTextContent() either...
    public static String getTextContent(IXmlReader xmlReader, ErrorHandler errorHandler) throws XMLStreamException {
        try {
            return xmlReader.getElementText();
        } catch (XMLStreamException e) {
            errorHandler.error(e);
            return "";
        }
    }
    public static void getTextContent(IXmlReader xmlReader, ErrorHandler errorHandler, Consumer<String> consumer) throws XMLStreamException {
        String r=getTextContent(xmlReader,errorHandler);
        if (r!=null)
            consumer.accept(r);
    }

    @FunctionalInterface
    public interface XMLWriteConsumer {

        void accept(IXmlWriter t) throws Exception;

    }
    public static IXmlWriter getXMLWriter(boolean enableIndent, OutputStream out) throws XMLStreamException {

        IXmlWriter xmlWriter= XmlParserFactory.getXmlOutputFactory().getXMLWriter(enableIndent, out, StandardCharsets.UTF_8);
        xmlWriter.writeDTD("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        return xmlWriter;
    }
    public static String generateXMLToString(XMLWriteConsumer c) throws XMLStreamException {
        try(ByteArrayOutputStream out=new ByteArrayOutputStream()) {

            IXmlWriter xmlStreamWriter = getXMLWriter(false, out);

            c.accept(xmlStreamWriter);
            xmlStreamWriter.writeEndDocument();
            xmlStreamWriter.close();
            out.flush();
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw XMLStreamException.getXmlStreamException(e);
        }
    }
    @FunctionalInterface
    public interface XMLReadFunction<R> {

        R accept(IXmlReader t) throws XMLStreamException, DescriptorBindingException, ValidationException, ActionException;

    }
    @FunctionalInterface
    public interface XMLReadConsumer {

        void accept(IXmlReader t) throws Exception;

    }

    public static void readRootElement(IXmlReader reader, XMLReadConsumer c, ErrorHandler errorHandler, String nameSpaceURI, String localName, DMLogger log) throws XMLStreamException, DescriptorBindingException {
        boolean rootFound=false;
        try {
            final int level = reader.getCurrentLevel();

            while (reader.hasNext()) {
                XMLType event = reader.next();
                if (event == XMLType.START_ELEMENT) {
                    if (level + 1 == reader.getCurrentLevel()) {
                        String nodeName = reader.getLocalName();
                        boolean nodeOk=nodeName.equals(localName);
                        rootFound|=nodeOk;
                        if (nodeOk && ((nameSpaceURI!=null && reader.getNamespaceURI() != null && nameSpaceURI.equals(reader.getNamespaceURI()))) || nameSpaceURI==null) {
                            c.accept(reader);
                            while (reader.getCurrentLevel() > level && reader.hasNext()) {
                                if (reader.next() == XMLType.END_DOCUMENT)
                                    break;
                            }
                            return;
                        }
                    }
                }
            }
        }
        catch (DescriptorBindingException e) {
            throw e;
        }
        catch (XMLStreamException e) {
            errorHandler.error(e);
        }
        catch (Exception e) {

            XMLStreamException e2=XMLStreamException.getXmlStreamException(e);
            errorHandler.error(e2);
            throw e2;
        }
        if (!rootFound)
            throw new DescriptorBindingException("Root element " + localName + " with name space " + nameSpaceURI + " was not found !");
        else
            log.warn(() -> "Wrong XML namespace declared on root element: " + nameSpaceURI);
    }
    public static void readElements(IXmlReader reader, XMLReadConsumer c, ErrorHandler errorHandler) throws DescriptorBindingException, XMLStreamException {
        final int level=reader.getCurrentLevel();
        try {
            boolean forceNext=false;
            for (;; )
            {
                XMLType event=reader.getEventType();
                while (event!=XMLType.START_ELEMENT || level+1!=reader.getCurrentLevel() || forceNext)
                {
                    forceNext=false;
                    if (reader.hasNext()) {
                        event = reader.next();
                        if (level>reader.getCurrentLevel())
                            return;
                    }
                    else
                        return;
                }
                Location oldLocation=reader.getLocation();
                c.accept(reader);
                if (oldLocation.isPositionEqual(reader.getLocation()))
                {
                    forceNext=true;
                }

            }

        }
        catch (InvalidValueException | DescriptorBindingException e) {
            throw e;
        }
        catch (XMLStreamException e) {
            errorHandler.error(e);
        } catch (Exception e) {
            XMLStreamException e2=XMLStreamException.getXmlStreamException(e);
            errorHandler.error(e2);
            throw e2;
        }
    }
    public static IXmlReader getXMLReader(String xmlString) throws XMLStreamException {
        if (ModelUtil.checkDescriptionXMLNotValid(xmlString))
            throw new XMLStreamException("XML not valid");
        XmlInputFactory factory=XmlParserFactory.getXmlInputFactory();
        factory.setNameSpaceAware(true);
        return factory.getXMLReader(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static <R> R readXML(XMLReadFunction<R> c, ErrorHandler errorHandler, String xmlString) throws XMLStreamException, ValidationException, DescriptorBindingException {
        try {
            IXmlReader xmlReader=getXMLReader(xmlString);
            R r=c.accept(xmlReader);
            while (xmlReader.hasNext())
            {
                xmlReader.next();
            }
            return r;

        }
        catch (XMLStreamException e) {
            errorHandler.error(e);
        }
        catch (ValidationException | DescriptorBindingException e) {
            throw e;
        }
        catch (Exception e) {
            XMLStreamException e2=XMLStreamException.getXmlStreamException(e);
            errorHandler.error(e2);
            throw e2;
        }
        return null;

    }

    public interface ErrorHandler {

        void warning (XMLStreamException exception)
                throws XMLStreamException;


        void error (XMLStreamException exception)
                throws XMLStreamException;

        void fatalError (XMLStreamException exception)
                throws XMLStreamException;

    }
    public static class XMLCompleters implements XMLCompleter
    {
        private final List<XMLCompleter> completers=new ArrayList<>();
        @Override
        public void write(IXmlWriter xmlWriter) throws XMLStreamException {
            for (XMLCompleter c : completers)
                c.write(xmlWriter);
        }

        public boolean add(XMLCompleter xmlCompleter) {
            return completers.add(xmlCompleter);
        }
    }
    public interface XMLCompleter
    {
        void write(IXmlWriter xmlWriter) throws XMLStreamException;
    }
    public static String escape(String string) {
        return escape(string, false, false);
    }

    public static String escape(String string, boolean convertNewlines, boolean convertSpaces) {
        if (string == null) return null;
        StringBuilder sb = new StringBuilder();
        String entity;
        char c;
        for (int i = 0; i < string.length(); ++i) {
            entity = null;
            c = string.charAt(i);
            switch (c) {
                case '<':
                    entity = "&#60;";
                    break;
                case '>':
                    entity = "&#62;";
                    break;
                case '&':
                    entity = "&#38;";
                    break;
                case '"':
                    entity = "&#34;";
                    break;
            }
            if (entity != null) {
                sb.append(entity);
            } else {
                sb.append(c);
            }
        }
        String result = sb.toString();
        if (convertSpaces) {
            // Converts the _beginning_ of line whitespaces into non-breaking spaces
            Matcher matcher = Pattern.compile("(\\n+)(\\s*)(.*)").matcher(result);
            StringBuilder temp = new StringBuilder();
            while (matcher.find()) {
                String group = matcher.group(2);
                matcher.appendReplacement(temp, "$1" + "&#160;".repeat(group.length()) + "$3");
            }
            matcher.appendTail(temp);
            result = temp.toString();
        }
        if (convertNewlines) {
            result = result.replaceAll("\n", "<br/>");
        }
        return result;
    }
}