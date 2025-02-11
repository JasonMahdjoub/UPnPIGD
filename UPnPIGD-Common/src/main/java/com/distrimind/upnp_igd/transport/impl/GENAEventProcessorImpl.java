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

package com.distrimind.upnp_igd.transport.impl;

import com.distrimind.upnp_igd.Log;
import com.distrimind.upnp_igd.binding.xml.DescriptorBindingException;
import com.distrimind.upnp_igd.model.Constants;
import com.distrimind.upnp_igd.model.XMLUtil;
import com.distrimind.upnp_igd.model.message.UpnpMessage;
import com.distrimind.upnp_igd.model.message.gena.IncomingEventRequestMessage;
import com.distrimind.upnp_igd.model.message.gena.OutgoingEventRequestMessage;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.meta.StateVariable;
import com.distrimind.upnp_igd.model.state.StateVariableValue;
import com.distrimind.upnp_igd.transport.spi.GENAEventProcessor;
import com.distrimind.upnp_igd.model.UnsupportedDataException;
import com.distrimind.flexilogxml.exceptions.XMLStreamException;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.flexilogxml.xml.IXmlReader;
import com.distrimind.flexilogxml.xml.IXmlWriter;

import java.util.Collection;

/**
 * Default implementation based on the <em>W3C DOM</em> XML processing API.
 *
 * @author Christian Bauer
 * @author Jason Mahdjoub, use XML Parser instead of Document
 */
public class GENAEventProcessorImpl implements GENAEventProcessor, XMLUtil.ErrorHandler {

    final private static DMLogger log = Log.getLogger(GENAEventProcessorImpl.class);


    @Override
	public void writeBody(OutgoingEventRequestMessage requestMessage) throws UnsupportedDataException {
		if (log.isDebugEnabled()) {
            log.debug("Writing body of: " + requestMessage);
		}

		try {



            String d= XMLUtil.generateXMLToString(xmlStreamWriter -> {
                xmlStreamWriter.writeStartElement("e", "propertyset", Constants.NS_UPNP_EVENT_10);
                writeProperties(xmlStreamWriter, requestMessage);
                xmlStreamWriter.writeEndElement();
            });

            requestMessage.setBody(UpnpMessage.BodyType.STRING, d);

            if (log.isTraceEnabled()) {
				log.trace("===================================== GENA BODY BEGIN ============================================");
                log.trace(requestMessage.getBody().toString());
                log.trace("====================================== GENA BODY END =============================================");
            }

        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void readBody(IncomingEventRequestMessage requestMessage) throws UnsupportedDataException {

		if (log.isDebugEnabled()) {
            log.debug("Reading body of: " + requestMessage);
		}
		if (log.isTraceEnabled()) {
            log.trace("===================================== GENA BODY BEGIN ============================================");
            log.trace(requestMessage.getBody() != null ? requestMessage.getBody().toString() : "null");
            log.trace("-===================================== GENA BODY END ============================================");
        }

        String body = getMessageBody(requestMessage);
        try {

            XMLUtil.readXML(xmlReader -> {
                XMLUtil.readRootElement(xmlReader, xmlReader2 -> readProperties(xmlReader2, requestMessage), this, Constants.NS_UPNP_EVENT_10, "propertyset", log);

                return null;
            }, this, body);


        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex.getMessage(), ex, body);
        }
    }

    /* ##################################################################################################### */


    protected void readPropertysetElement(IXmlReader xmlReader) {

        if (xmlReader.hasName() || !"propertyset".equals(getUnprefixedNodeName(xmlReader))) {
            throw new RuntimeException("Root element was not 'propertyset'");
        }
    }

    /* ##################################################################################################### */

    protected void writeProperties(IXmlWriter xmlWriter, OutgoingEventRequestMessage message) throws XMLStreamException {
        for (StateVariableValue<?> stateVariableValue : message.getStateVariableValues()) {

            xmlWriter.writeStartElement( "e:property");
            XMLUtil.appendNewElement(
                    xmlWriter,
                    stateVariableValue.getStateVariable().getName(),
                    stateVariableValue.toString()
            );
            xmlWriter.writeEndElement();
        }
    }

    protected void readProperties(IXmlReader xmlReader, IncomingEventRequestMessage message) throws XMLStreamException, DescriptorBindingException {


        Collection<StateVariable<RemoteService>> stateVariables = message.getService().getStateVariables();

        XMLUtil.readElements(xmlReader, reader -> {
            String propertysetChild = getUnprefixedNodeName(reader);


            if ("property".equals(propertysetChild)) {


                XMLUtil.readElements(xmlReader, reader2 -> {
                    String stateVariableName = getUnprefixedNodeName(reader2);

                    for (StateVariable<RemoteService> stateVariable : stateVariables) {
                        if (stateVariable.getName().equals(stateVariableName)) {
                            log.debug(() -> "Reading state variable value: " + stateVariableName);
                            String value = XMLUtil.getTextContent(xmlReader, GENAEventProcessorImpl.this);
                            message.getStateVariableValues().add(
                                    new StateVariableValue<>(stateVariable, value)
                            );
                            break;
                        }
                    }

                }, this);
            }
        }, this);
    }

    /* ##################################################################################################### */

    protected String getMessageBody(UpnpMessage<?> message) throws UnsupportedDataException {
        if (!message.isBodyNonEmptyString())
            throw new UnsupportedDataException(
                "Can't transform null or non-string/zero-length body of: " + message
            );
        return message.getBodyString().trim();
    }


    protected String getUnprefixedNodeName(IXmlReader xmlReader) {
        return xmlReader.getLocalName();
    }

    @Override
	public void warning(XMLStreamException e) throws XMLStreamException {
        log.warn(e::toString);
    }

    @Override
	public void error(XMLStreamException e) throws XMLStreamException {
        throw e;
    }

    @Override
	public void fatalError(XMLStreamException e) throws XMLStreamException {
        throw e;
    }
}

