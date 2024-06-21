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

import java.util.*;
import java.util.logging.Logger;

import com.distrimind.upnp_igd.model.action.ActionArgumentValue;
import com.distrimind.upnp_igd.model.action.ActionException;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.message.control.ActionRequestMessage;
import com.distrimind.upnp_igd.model.message.control.ActionResponseMessage;
import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.types.ErrorCode;
import com.distrimind.upnp_igd.transport.spi.SOAPActionProcessor;
import com.distrimind.upnp_igd.model.UnsupportedDataException;
import com.distrimind.upnp_igd.xml.XmlPullParserUtils;

import jakarta.enterprise.inject.Alternative;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation based on the <em>Xml Pull Parser</em> XML processing API.
 * <p>
 * This processor is more lenient with parsing, looking only for the required XML tags.
 * </p>
 * <p>
 * To use this parser you need to install an implementation of the
 * <a href="http://www.xmlpull.org/impls.shtml">XMLPull API</a>.
 * </p>
 *
 * @author Michael Pujos
 */
@Alternative
public class PullSOAPActionProcessorImpl extends SOAPActionProcessorImpl {

    protected static Logger log = Logger.getLogger(SOAPActionProcessor.class.getName());

    public <S extends Service<?, ?, ?>> void readBody(ActionRequestMessage requestMessage, ActionInvocation<S> actionInvocation) throws UnsupportedDataException {
        String body = getMessageBody(requestMessage);
        try {
            Document doc=createDocumentBuilderFactory().newDocumentBuilder().parse(body);
            readBodyRequest(doc, requestMessage, actionInvocation);
        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex, ex, body);
        }
    }

    public <S extends Service<?, ?, ?>> void readBody(ActionResponseMessage responseMsg, ActionInvocation<S> actionInvocation) throws UnsupportedDataException {
        String body = getMessageBody(responseMsg);
        try {
            Document doc=createDocumentBuilderFactory().newDocumentBuilder().parse(body);
            readBodyElement(doc);
            readBodyResponse(doc, actionInvocation);
        } catch (Exception ex) {
            throw new UnsupportedDataException("Can't transform message payload: " + ex, ex, body);
        }
    }



    protected <S extends Service<?, ?, ?>> void readBodyRequest(Document doc, ActionRequestMessage requestMessage, ActionInvocation<S> actionInvocation) throws Exception {
        XmlPullParserUtils.searchTag(doc, actionInvocation.getAction().getName());
        readActionInputArguments(doc, actionInvocation);
    }

    protected <S extends Service<?, ?, ?>>  boolean readBodyResponse(Node node, ActionInvocation<S> actionInvocation) throws Exception {
        NodeList nl=node.getChildNodes();
        for (int i=0;i<nl.getLength();i++)
        {
            Node n=nl.item(i);
            if (n.getNodeName().equalsIgnoreCase("Fault")) {
                ActionException e = readFaultElement(n);
                actionInvocation.setFailure(e);
                return true;
            } else if (n.getNodeName().equalsIgnoreCase(actionInvocation.getAction().getName() + "Response")) {
                readActionOutputArguments(n, actionInvocation);
                return true;
            }
            else if (readBodyResponse(n, actionInvocation))
                return true;
        }

        throw new ActionException(
            ErrorCode.ACTION_FAILED,
            String.format("Action SOAP response do not contain %s element",
                actionInvocation.getAction().getName() + "Response"
            )
        );
    }

    protected <S extends Service<?, ?, ?>> void readActionInputArguments(Document doc, ActionInvocation<S> actionInvocation) throws Exception {
        actionInvocation.setInput(readArgumentValues(doc, actionInvocation.getAction().getInputArguments()));
    }
    protected <S extends Service<?, ?, ?>> void readActionOutputArguments(Node node, ActionInvocation<S> actionInvocation) throws Exception {
        if (node instanceof Element)
            readActionOutputArguments((Element) node, actionInvocation);
        else
            actionInvocation.setOutput(readArgumentValues(node, actionInvocation.getAction().getOutputArguments()));
    }
    protected <S extends Service<?, ?, ?>> void readActionOutputArguments(Document doc, ActionInvocation<S> actionInvocation) throws Exception {
        actionInvocation.setOutput(readArgumentValues(doc, actionInvocation.getAction().getOutputArguments()));
    }
    private void getMatchingNodes(Node node, List<String> names, Map<String, String> matches) {
        NodeList nl=node.getChildNodes();
        for (int i=0;i<nl.getLength();i++)
        {
            Node n=nl.item(i);
            String name=n.getNodeName().toLowerCase(Locale.ROOT);
            if (names.contains(name))
                matches.put(name, n.getTextContent());
            getMatchingNodes(n, names, matches);
        }

    }
    protected <S extends Service<?, ?, ?>> Map<String, String> getMatchingNodes(Node node, List<ActionArgument<S>> args) throws Exception {

        List<String> names = getNames(args);

        Map<String, String> matches = new HashMap<>();
        getMatchingNodes(node, names, matches);

        if (matches.size() < args.size()) {
            throw new ActionException(
                ErrorCode.ARGUMENT_VALUE_INVALID,
                "Invalid number of input or output arguments in XML message, expected "
                    + args.size() + " but found " + matches.size()
            );
        }
        return matches;
    }

    protected <S extends Service<?, ?, ?>> List<ActionArgumentValue<S>> readArgumentValues(Node node, List<ActionArgument<S>> args) throws Exception {
        // We're in the <ActionName>Response tag
        Map<String, String> matches = getMatchingNodes(node, args);
        List<ActionArgumentValue<S>> values=new ArrayList<>(args.size());


        for (ActionArgument<S> arg : args) {

            String value = findActionArgumentValue(matches, arg);
            if (value == null) {
                throw new ActionException(
                    ErrorCode.ARGUMENT_VALUE_INVALID,
                    "Could not find argument '" + arg.getName() + "' node");
            }

            log.fine("Reading action argument: " + arg.getName());
            values.add(createValue(arg, value));
        }
        return values;
    }

    protected <S extends Service<?, ?, ?>>  String findActionArgumentValue(Map<String, String> entries, ActionArgument<S> arg) {
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (arg.isNameOrAlias(entry.getKey())) return entry.getValue();
        }
        return null;
    }
    private boolean readFaultElement(Node node, StringBuilder errorCode, StringBuilder errorDescription) throws Exception {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            switch (n.getNodeName()) {
                case "errorCode":
                    if (errorCode.length()==0)
                        errorCode.append(n.getTextContent());
                    break;
                case "errorDescription":
                    if (errorDescription.length()==0)
                        errorDescription.append(n.getTextContent());
                    break;
                case "UPnPError":
                    return false;
            }
            if (errorCode.length()>0 && errorDescription.length()>0)
                return false;
            if (!readFaultElement(n, errorCode, errorDescription))
                return false;
        }
        return true;
    }
    protected ActionException readFaultElement(Node node) throws Exception {
        if (node instanceof Element)
            return readFaultElement((Element) node);
        else {
            // We're in the "Fault" tag



            XmlPullParserUtils.searchTag(node, "UPnPError");
            final StringBuilder errorCode = new StringBuilder();
            final StringBuilder errorDescription = new StringBuilder();
            readFaultElement(node, errorCode, errorDescription);



            if (errorCode.length()>0) {
                try {
                    int numericCode = Integer.parseInt(errorCode.toString());
                    ErrorCode standardErrorCode = ErrorCode.getByCode(numericCode);
                    if (standardErrorCode != null) {
                        log.fine("Reading fault element: " + standardErrorCode.getCode() + " - " + errorDescription);
                        return new ActionException(standardErrorCode, errorDescription.toString(), false);
                    } else {
                        log.fine("Reading fault element: " + numericCode + " - " + errorDescription);
                        return new ActionException(numericCode, errorDescription.toString());
                    }
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("Error code was not a number");
                }
            }

            throw new RuntimeException("Received fault element but no error code");
        }
    }
}
