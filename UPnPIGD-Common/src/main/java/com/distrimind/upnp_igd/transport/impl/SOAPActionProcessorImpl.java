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

import com.distrimind.flexilogxml.exceptions.XMLStreamException;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.flexilogxml.xml.IXmlReader;
import com.distrimind.flexilogxml.xml.IXmlWriter;
import com.distrimind.upnp_igd.binding.xml.DescriptorBindingException;
import com.distrimind.upnp_igd.Log;
import com.distrimind.upnp_igd.model.Constants;
import com.distrimind.upnp_igd.model.XMLUtil;
import com.distrimind.upnp_igd.model.action.ActionArgumentValue;
import com.distrimind.upnp_igd.model.action.ActionException;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.message.control.ActionMessage;
import com.distrimind.upnp_igd.model.message.control.ActionRequestMessage;
import com.distrimind.upnp_igd.model.message.control.ActionResponseMessage;
import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.types.ErrorCode;
import com.distrimind.upnp_igd.model.types.InvalidValueException;
import com.distrimind.upnp_igd.transport.spi.SOAPActionProcessor;
import com.distrimind.upnp_igd.model.UnsupportedDataException;


import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation based on the <em>W3C DOM</em> XML processing API.
 *
 * @author Christian Bauer
 * @author Jason Mahdjoub, use XML Parser instead of Document
 */
public class SOAPActionProcessorImpl implements SOAPActionProcessor, XMLUtil.ErrorHandler {

    private static final DMLogger log = Log.getLogger(SOAPActionProcessor.class.getName());
    public static final String FOR = " for: ";
    public static final String SOAP_BODY_BEGIN = "===================================== SOAP BODY BEGIN ============================================";
    public static final String SOAP_BODY_END = "-===================================== SOAP BODY END ============================================";
    public static final String CAN_T_TRANSFORM_MESSAGE_PAYLOAD = "Can't transform message payload: ";

    @Override
    public <S extends Service<?, ?, ?>> void writeBody(ActionRequestMessage requestMessage, ActionInvocation<S> actionInvocation) throws UnsupportedDataException {

		if (log.isDebugEnabled()) {
            log.debug("Writing body of " + requestMessage + FOR + actionInvocation);
		}

        try {
            String d= XMLUtil.generateXMLToString(xmlStreamWriter -> {
                writeStartBodyElement(xmlStreamWriter);

                writeBodyRequest(xmlStreamWriter, requestMessage, actionInvocation);

                xmlStreamWriter.writeEndElement();

                writeEndBodyElement(xmlStreamWriter);
            });
            requestMessage.setBody(d);

            if (log.isTraceEnabled()) {
				log.trace(SOAP_BODY_BEGIN);
                log.trace(requestMessage.getBodyString());
                log.trace(SOAP_BODY_END);
            }

        } catch (Exception ex) {
            throw new UnsupportedDataException(CAN_T_TRANSFORM_MESSAGE_PAYLOAD + ex, ex);
        }
    }

    @Override
    public <S extends Service<?, ?, ?>> void writeBody(ActionResponseMessage responseMessage, ActionInvocation<S> actionInvocation) throws UnsupportedDataException {

		if (log.isDebugEnabled()) {
            log.debug("Writing body of " + responseMessage + FOR + actionInvocation);
		}

        try {
            String d= XMLUtil.generateXMLToString(xmlStreamWriter -> {
                writeStartBodyElement(xmlStreamWriter);

                if (actionInvocation.getFailure() != null) {
                    writeBodyFailure(xmlStreamWriter, responseMessage, actionInvocation);
                } else {
                    writeBodyResponse(xmlStreamWriter, responseMessage, actionInvocation);
                }

                writeEndBodyElement(xmlStreamWriter);
            });
            responseMessage.setBody(d);



            if (log.isTraceEnabled()) {
				log.trace(SOAP_BODY_BEGIN);
                log.trace(responseMessage.getBodyString());
                log.trace(SOAP_BODY_END);
            }

        } catch (Exception ex) {
            throw new UnsupportedDataException(CAN_T_TRANSFORM_MESSAGE_PAYLOAD + ex, ex);
        }
    }

    @Override
    public <S extends Service<?, ?, ?>> void readBody(ActionRequestMessage requestMessage, ActionInvocation<S> actionInvocation) throws UnsupportedDataException {

		if (log.isDebugEnabled()) {
            log.debug("Reading body of " + requestMessage + FOR + actionInvocation);
		}
		if (log.isTraceEnabled()) {
            log.trace(SOAP_BODY_BEGIN);
            log.trace(requestMessage.getBodyString());
            log.trace(SOAP_BODY_END);
        }

        String body = getMessageBody(requestMessage);
        try {
            XMLUtil.readXML(xmlReader -> {
                readXML(xmlReader, xmlReader2 -> {
                    readBodyRequest(xmlReader, requestMessage, actionInvocation);
                    return null;
                });
                return null;
            }, this, body);

        } catch (Exception ex) {
            throw new UnsupportedDataException(CAN_T_TRANSFORM_MESSAGE_PAYLOAD + ex, ex, body);
        }
    }

    @Override
    public <S extends Service<?, ?, ?>> void readBody(ActionResponseMessage responseMsg, ActionInvocation<S> actionInvocation) throws UnsupportedDataException {

		if (log.isDebugEnabled()) {
            log.debug("Reading body of " + responseMsg + FOR + actionInvocation);
		}
		if (log.isTraceEnabled()) {
            log.trace(SOAP_BODY_BEGIN);
            log.trace(responseMsg.getBodyString());
            log.trace(SOAP_BODY_END);
        }

        String body = getMessageBody(responseMsg);
        try {
            try {
                class C{
                    ActionException failure=null;
                }
                C c=new C();
                XMLUtil.readXML(xmlReader -> {
                    readXML(xmlReader, xmlReader2 -> {
                        c.failure = readBodyFailure(xmlReader2);
                        return null;
                    });
                    return null;
                }, this, body);
                if (c.failure==null)
                {
                    XMLUtil.readXML(xmlReader -> {
                        readXML(xmlReader, xmlReader2 -> {
                            readBodyResponse(xmlReader2, actionInvocation);
                            return null;
                        });
                        return null;
                    }, this, body);
                }
                else
                    actionInvocation.setFailure(c.failure);


            } catch (Exception ex) {
                throw new UnsupportedDataException(CAN_T_TRANSFORM_MESSAGE_PAYLOAD + ex, ex, body);
            }



        } catch (Exception ex) {
            throw new UnsupportedDataException(CAN_T_TRANSFORM_MESSAGE_PAYLOAD + ex, ex, body);
        }
    }

    /* ##################################################################################################### */

    protected <S extends Service<?, ?, ?>> void writeBodyFailure(IXmlWriter xmlWriter,
                                                                 ActionResponseMessage message,
                                                                 ActionInvocation<S> actionInvocation) throws Exception {

        writeFaultElement(xmlWriter, actionInvocation);
    }

    protected <S extends Service<?, ?, ?>> void writeBodyRequest(IXmlWriter xmlWriter,
                                                                 ActionRequestMessage message,
                                                                 ActionInvocation<S> actionInvocation) throws Exception {

        writeActionRequestElement(xmlWriter, message, actionInvocation);
        writeActionInputArguments(xmlWriter, actionInvocation);

    }

    protected <S extends Service<?, ?, ?>> void writeBodyResponse(IXmlWriter xmlWriter,
                                                                  ActionResponseMessage message,
                                                                  ActionInvocation<S> actionInvocation) throws Exception {

        writeActionResponseElement(xmlWriter, message, actionInvocation);
        writeActionOutputArguments(xmlWriter, actionInvocation);
    }

    protected ActionException readBodyFailure(IXmlReader xmlReader) throws XMLStreamException, DescriptorBindingException {
        return readFaultElement(xmlReader);
    }

    protected <S extends Service<?, ?, ?>> void readBodyRequest(IXmlReader xmlReader,
                                                                ActionRequestMessage message,
                                                                ActionInvocation<S> actionInvocation) throws XMLStreamException, DescriptorBindingException, ActionException {

        readActionRequestElement(xmlReader, message, actionInvocation);

    }

    protected <S extends Service<?, ?, ?>> void readBodyResponse(IXmlReader xmlReader,
                                                                 ActionInvocation<S> actionInvocation) throws XMLStreamException, ActionException, DescriptorBindingException {

        readActionResponseElement(xmlReader, actionInvocation);

    }

    /* ##################################################################################################### */

    protected void writeStartBodyElement(IXmlWriter xmlWriter) throws XMLStreamException {

        xmlWriter.writeStartElement("s", "Envelope", Constants.SOAP_NS_ENVELOPE);
        xmlWriter.writeAttribute("s", Constants.SOAP_NS_ENVELOPE, "encodingStyle", Constants.SOAP_URI_ENCODING_STYLE);

        xmlWriter.writeStartElement("s", "Body", Constants.SOAP_NS_ENVELOPE);
    }
    protected void writeEndBodyElement(IXmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
    }

    protected <S extends Service<?, ?, ?>> void readXML(IXmlReader xmlReader, XMLUtil.XMLReadFunction<Void> nextRead) throws XMLStreamException, DescriptorBindingException {
        XMLUtil.readRootElement(xmlReader, reader -> {
            class B
            {
                boolean b=true;
            }
            B b=new B();
            XMLUtil.readElements(reader, reader2 -> {
                String envelopeChild = reader2.getLocalName();


                if ("Body".equals(envelopeChild)) {
                    nextRead.accept(reader2);
                    b.b=false;
                }


            }, SOAPActionProcessorImpl.this);
            if (b.b)
                throw new XMLStreamException("Response envelope did not contain 'Body' child element");

        }, this, null, "Envelope", log);

    }

    /* ##################################################################################################### */

    protected <S extends Service<?, ?, ?>> void writeActionRequestElement(IXmlWriter xmlWriter,
                                                                          ActionRequestMessage message,
                                                                          ActionInvocation<S> actionInvocation) throws XMLStreamException {

		if (log.isDebugEnabled()) {
            log.debug("Writing action request element: " + actionInvocation.getAction().getName());
		}

        xmlWriter.writeStartElement("s",
                actionInvocation.getAction().getName(), message.getActionNamespace()
        );

    }

    protected <S extends Service<?, ?, ?>> void readActionRequestElement(IXmlReader xmlReader,
                                                                         ActionRequestMessage message,
                                                                         ActionInvocation<S> actionInvocation) throws XMLStreamException, DescriptorBindingException {

		if (log.isDebugEnabled()) {
            log.debug("Looking for action request element matching namespace:" + message.getActionNamespace());
		}
        class B
        {
            boolean b=true;
        }
        B b=new B();
        XMLUtil.readElements(xmlReader, reader -> {
            String unprefixedName = getUnprefixedNodeName(reader);
            if (unprefixedName.equals(actionInvocation.getAction().getName())) {
                if (xmlReader.getNamespaceURI() == null
                        || !xmlReader.getNamespaceURI().equals(message.getActionNamespace()))
                    throw new UnsupportedDataException(
                            "Illegal or missing namespace on action request element: " + unprefixedName
                    );
				if (log.isDebugEnabled()) {
					log.debug("Reading action request element: " + unprefixedName);
				}
                b.b=false;
                readActionInputArguments(xmlReader, actionInvocation);
            }
        }, this);
        if (b.b)
            throw new UnsupportedDataException(
                    "Could not read action request element matching namespace: " + message.getActionNamespace()
            );
    }

    /* ##################################################################################################### */

    protected <S extends Service<?, ?, ?>> void writeActionResponseElement(IXmlWriter xmlWriter,
                                                                           ActionResponseMessage message,
                                                                           ActionInvocation<S> actionInvocation) throws XMLStreamException {

		if (log.isDebugEnabled()) {
            log.debug("Writing action response element: " + actionInvocation.getAction().getName());
		}
        xmlWriter.writeStartElement("s",
                actionInvocation.getAction().getName() + "Response", message.getActionNamespace()
        );



    }

    protected <S extends Service<?, ?, ?>> void readActionResponseElement(IXmlReader xmlReader, ActionInvocation<S> actionInvocation) throws XMLStreamException, DescriptorBindingException {
        class B
        {
            boolean b=true;
        }
        B b=new B();
        XMLUtil.readElements(xmlReader, reader -> {
            String bodyChild = getUnprefixedNodeName(xmlReader);

            if (bodyChild.equals(actionInvocation.getAction().getName() + "Response")) {
                log.debug(() -> "Reading action response element: " + bodyChild);
                b.b=false;
                readActionOutputArguments(xmlReader, actionInvocation);
            }
        }, this);
        if (b.b)
            log.debug("Could not read action response element");
    }

    /* ##################################################################################################### */

    protected <S extends Service<?, ?, ?>> void writeActionInputArguments(IXmlWriter xmlWriter,
                                                                          ActionInvocation<S> actionInvocation) throws XMLStreamException {

        for (ActionArgument<S> argument : actionInvocation.getAction().getInputArguments()) {
			if (log.isDebugEnabled()) {
				log.debug("Writing action input argument: " + argument.getName());
			}
			String value = actionInvocation.getInput(argument) != null ? actionInvocation.getInput(argument).toString() : "";
            XMLUtil.appendNewElement(xmlWriter, argument.getName(), value);
        }
    }

    public <S extends Service<?, ?, ?>> void readActionInputArguments(IXmlReader xmlReader,
                                                                      ActionInvocation<S> actionInvocation) throws ActionException, XMLStreamException, DescriptorBindingException {
        actionInvocation.setInput(
                readArgumentValues(
                        xmlReader,
                        actionInvocation.getAction().getInputArguments()
                )
        );
    }

    /* ##################################################################################################### */

    protected <S extends Service<?, ?, ?>> void writeActionOutputArguments(IXmlWriter xmlWriter,
                                                                           ActionInvocation<S> actionInvocation) throws XMLStreamException {

        for (ActionArgument<S> argument : actionInvocation.getAction().getOutputArguments()) {
			if (log.isDebugEnabled()) {
				log.debug("Writing action output argument: " + argument.getName());
			}
			String value = actionInvocation.getOutput(argument) != null ? actionInvocation.getOutput(argument).toString() : "";
            XMLUtil.appendNewElement(xmlWriter, argument.getName(), value);
        }
    }

    protected <S extends Service<?, ?, ?>> void readActionOutputArguments(IXmlReader xmlReader,
                                                                          ActionInvocation<S> actionInvocation) throws ActionException, XMLStreamException, DescriptorBindingException {

        actionInvocation.setOutput(
                readArgumentValues(
                        xmlReader,
                        actionInvocation.getAction().getOutputArguments()
                )
        );
    }

    /* ##################################################################################################### */

    protected <S extends Service<?, ?, ?>> void writeFaultElement(IXmlWriter xmlWriter, ActionInvocation<S> actionInvocation) throws XMLStreamException {

        xmlWriter.writeStartElement("s", "Fault", Constants.SOAP_NS_ENVELOPE);


        // This stuff is really completely arbitrary nonsense... let's hope they fired the guy who decided this
        XMLUtil.appendNewElement(xmlWriter, "faultcode", "s:Client");
        XMLUtil.appendNewElement(xmlWriter, "faultstring", "UPnPError");

        xmlWriter.writeStartElement("detail");

        xmlWriter.writeStartElement(Constants.NS_UPNP_CONTROL_10, "UPnPError");


        int errorCode = actionInvocation.getFailure().getErrorCode();
        String errorDescription = actionInvocation.getFailure().getMessage();

		if (log.isDebugEnabled()) {
            log.debug("Writing fault element: " + errorCode + " - " + errorDescription);
		}

		XMLUtil.appendNewElement(xmlWriter, "errorCode", Integer.toString(errorCode));
        XMLUtil.appendNewElement(xmlWriter, "errorDescription", errorDescription);

        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement();
    }

    protected ActionException readFaultElement(IXmlReader xmlReader) throws XMLStreamException, DescriptorBindingException {
        class C {
            boolean receivedFaultElement = false;
            String errorCode = null;
            String errorDescription = null;
        }
        C c=new C();


        XMLUtil.readElements(xmlReader, reader -> {
            String bodyChild = getUnprefixedNodeName(reader);


            if ("Fault".equals(bodyChild)) {

                c.receivedFaultElement = true;

                XMLUtil.readElements(xmlReader, reader2 -> {
                    String faultChild = getUnprefixedNodeName(reader2);

                    if ("detail".equals(faultChild)) {

                        XMLUtil.readElements(xmlReader, reader3 -> {
                            String detailChild = getUnprefixedNodeName(reader3);

                            if ("UPnPError".equals(detailChild)) {

                                XMLUtil.readElements(xmlReader, reader4 -> {
                                    String errorChild = getUnprefixedNodeName(reader4);

                                    if ("errorCode".equals(errorChild))
                                        c.errorCode = XMLUtil.getTextContent(xmlReader, SOAPActionProcessorImpl.this);

                                    if ("errorDescription".equals(errorChild))
                                        c.errorDescription = XMLUtil.getTextContent(xmlReader, SOAPActionProcessorImpl.this);
                                }, this);
                            }
                        }, this);
                    }
                }, this);
            }
        }, this);

        if (c.errorCode != null) {
            try {
                int numericCode = Integer.parseInt(c.errorCode);
                ErrorCode standardErrorCode = ErrorCode.getByCode(numericCode);
                String ed=c.errorDescription;
                if (standardErrorCode != null) {
                    log.debug(() -> "Reading fault element: " + standardErrorCode.getCode() + " - " + ed);
                    return new ActionException(standardErrorCode, c.errorDescription, false);
                } else {
                    log.debug(() -> "Reading fault element: " + numericCode + " - " + ed);
                    return new ActionException(numericCode, c.errorDescription);
                }
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Error code was not a number");
            }
        } else if (c.receivedFaultElement) {
            throw new RuntimeException("Received fault element but no error code");
        }
        return null;
    }


    /* ##################################################################################################### */

    protected String getMessageBody(ActionMessage message) throws UnsupportedDataException {
        if (!message.isBodyNonEmptyString())
            throw new UnsupportedDataException(
                    "Can't transform null or non-string/zero-length body of: " + message
            );
        return message.getBodyString().trim();
    }

    protected static String getUnprefixedNodeName(IXmlReader xmlReader) {
        return xmlReader.getLocalName();
    }

    /**
     * The UPnP spec says that action arguments must be in the order as declared
     * by the service. This method however is lenient, the action argument nodes
     * in the XML can be in any order, as long as they are all there everything
     * is OK.
     */
    protected <S extends Service<?, ?, ?>> List<ActionArgumentValue<S>> readArgumentValues(IXmlReader xmlReader, List<ActionArgument<S>> args)
            throws ActionException, XMLStreamException, DescriptorBindingException {

        List<N> nodes = getMatchingNodes(xmlReader, args);

        List<ActionArgumentValue<S>> values = new ArrayList<>(args.size());

        for (ActionArgument<S> arg : args) {
            N node = findActionArgumentNode(nodes, arg);
            if(node == null) {
                throw new ActionException(
                        ErrorCode.ARGUMENT_VALUE_INVALID,
                        "Could not find argument '" + arg.getName() + "' node");
            }
            log.debug(() -> "Reading action argument: " + arg.getName());
            String value = node.value;
            values.add(createValue(arg, value));
        }
        return values;
    }
    protected <S extends Service<?, ?, ?>> List<String> getNames(List<ActionArgument<S>> args)
    {
        List<String> names = new ArrayList<>();
        for (ActionArgument<?> argument : args) {
            names.add(argument.getName());
            names.addAll(argument.getAliases());
        }
        return names;
    }
    /**
     * Finds all element nodes in the list that match any argument name or argument
     * alias, throws {@link ActionException} if not all arguments were found.
     */
    protected static class N
    {
        String localName;
        String value;

        public N(IXmlReader xmlReader) throws XMLStreamException {
            this.localName = getUnprefixedNodeName(xmlReader);
            this.value = xmlReader.getElementText();
        }
    }
    protected <S extends Service<?, ?, ?>> List<N> getMatchingNodes(IXmlReader xmlReader, List<ActionArgument<S>> args) throws ActionException, XMLStreamException, DescriptorBindingException {
        //TODO check if must be a case-insensitive search!
        List<String> names = getNames(args);

        List<N> matches = new ArrayList<>();
        XMLUtil.readElements(xmlReader, reader -> {
            N node=new N(xmlReader);

            if (names.stream().anyMatch(s -> s.equals(node.localName)))
                matches.add(node);
        }, this);

        if (matches.size() < args.size()) {
            throw new ActionException(
                    ErrorCode.ARGUMENT_VALUE_INVALID,
                    "Invalid number of input or output arguments in XML message, expected " + args.size() + " but found " + matches.size()
            );
        }
        return matches;
    }

    /**
     * Creates an instance of {@link ActionArgumentValue} and wraps an
     * {@link InvalidValueException} as an {@link ActionException} with the
     * appropriate {@link ErrorCode}.
     */
    protected <S extends Service<?, ?, ?>> ActionArgumentValue<S> createValue(ActionArgument<S> arg, String value) throws ActionException {
        try {
            return new ActionArgumentValue<>(arg, value);
        } catch (InvalidValueException ex) {
            throw new ActionException(
                    ErrorCode.ARGUMENT_VALUE_INVALID,
                    "Wrong type or invalid value for '" + arg.getName() + "': " + ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * Returns the node with the same unprefixed name as the action argument
     * name/alias or <code>null</code>.
     */
    protected <S extends Service<?, ?, ?>> N findActionArgumentNode(List<N> nodes, ActionArgument<S> arg) {
        for(N node : nodes) {
            if(arg.isNameOrAlias(node.localName)) return node;
        }
        return null;
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
