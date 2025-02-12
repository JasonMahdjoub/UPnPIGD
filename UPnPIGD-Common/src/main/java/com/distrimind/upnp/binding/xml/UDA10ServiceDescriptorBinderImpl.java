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

package com.distrimind.upnp.binding.xml;

import com.distrimind.flexilogxml.exceptions.XMLStreamException;
import com.distrimind.flexilogxml.xml.IXmlReader;
import com.distrimind.flexilogxml.xml.IXmlWriter;
import com.distrimind.upnp.binding.staging.MutableAction;
import com.distrimind.upnp.binding.staging.MutableActionArgument;
import com.distrimind.upnp.binding.staging.MutableAllowedValueRange;
import com.distrimind.upnp.binding.staging.MutableService;
import com.distrimind.upnp.binding.staging.MutableStateVariable;
import com.distrimind.upnp.model.ValidationException;
import com.distrimind.upnp.model.XMLUtil;
import com.distrimind.upnp.model.meta.*;
import com.distrimind.upnp.model.types.CustomDatatype;
import com.distrimind.upnp.model.types.Datatype;
import com.distrimind.upnp.transport.spi.NetworkAddressFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

import static com.distrimind.upnp.binding.xml.Descriptor.Service.ATTRIBUTE;
import static com.distrimind.upnp.binding.xml.Descriptor.Service.ELEMENT;
import static com.distrimind.upnp.model.XMLUtil.appendNewElementIfNotNull;

/**
 * Implementation based on JAXP DOM.
 *
 * @author Christian Bauer
 * @author Jason Mahdjoub, use XML Parser instead of Document
 */
public class UDA10ServiceDescriptorBinderImpl implements ServiceDescriptorBinder, XMLUtil.ErrorHandler {

    private static final DMLogger log = Log.getLogger(ServiceDescriptorBinder.class);
    private final NetworkAddressFactory networkAddressFactory;
    public UDA10ServiceDescriptorBinderImpl(NetworkAddressFactory networkAddressFactory)
    {
        this.networkAddressFactory=networkAddressFactory;
    }
    @Override
    public <D extends Device<?, D, S>, S extends Service<?, D, S>> S describe(S undescribedService, String descriptorXml) throws DescriptorBindingException, ValidationException {
        if (descriptorXml == null || descriptorXml.isEmpty()) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Populating service from XML descriptor: " + undescribedService);
            }

            return XMLUtil.readXML(xmlReader -> describe(undescribedService, xmlReader), this, descriptorXml.trim());

        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw DescriptorBindingException.getDescriptorBindingException("Could not parse service descriptor: " + ex, ex);
        }
    }

    @Override
    public <D extends Device<?, D, S>, S extends Service<?, D, S>> S describe(S undescribedService, IXmlReader xmlReader) throws DescriptorBindingException, ValidationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Populating service from DOM: " + undescribedService);
            }

            // Read the XML into a mutable descriptor graph
            MutableService<D, S> descriptor = new MutableService<>();

            hydrateBasic(descriptor, undescribedService);
            XMLUtil.readRootElement(xmlReader, reader -> hydrateRoot(descriptor, reader), this, Descriptor.Service.NAMESPACE_URI, ELEMENT.scpd.name(), log);

            // Build the immutable descriptor graph
            return buildInstance(undescribedService, descriptor);

        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw DescriptorBindingException.getDescriptorBindingException("Could not parse service DOM: " + ex, ex);
        }
    }

    protected <D extends Device<?, D, S>, S extends Service<?, D, S>> S buildInstance(S undescribedService, MutableService<D, S> descriptor) throws ValidationException {
        S res= descriptor.build(undescribedService.getDevice());
        if (res.getDevice()!=null && res.getDevice().getDetails()!=null && UDA10DeviceDescriptorBinderImpl.isNotValidRemoteAddress(res.getDevice().getDetails().getBaseURL(), networkAddressFactory))
            return null;
        return res;
    }

    protected void hydrateBasic(MutableService<?, ?> descriptor, Service<?, ?, ?> undescribedService) {
        descriptor.serviceId = undescribedService.getServiceId();
        descriptor.serviceType = undescribedService.getServiceType();
        if (undescribedService instanceof RemoteService) {
            RemoteService rs = (RemoteService) undescribedService;
            descriptor.controlURI = rs.getControlURI();
            descriptor.eventSubscriptionURI = rs.getEventSubscriptionURI();
            descriptor.descriptorURI = rs.getDescriptorURI();
        }
    }

    protected void hydrateRoot(MutableService<?, ?> descriptor, IXmlReader xmlReader)
            throws DescriptorBindingException, XMLStreamException {

        // We don't check the XMLNS, nobody bothers anyway...



        XMLUtil.readElements(xmlReader, reader -> {
            ELEMENT rootChild = ELEMENT.valueOrNullOf(reader.getLocalName());
            if (rootChild!=null) {
                switch (rootChild)
                {
                    case specVersion:
                        // We don't care about UDA major/minor specVersion anymore - whoever had the brilliant idea that
                        // the spec versions can be declared on devices _AND_ on their services should have their fingers
                        // broken, so they never touch a keyboard again.
                        // hydrateSpecVersion(descriptor, rootChild);
                        break;
                    case actionList:
                        hydrateActionList(descriptor, reader);
                        break;
                    case serviceStateTable:
                        hydrateServiceStateTableList(descriptor, reader);
                        break;
                    default:
                        log.trace(() -> "Ignoring unknown element: " + rootChild);
                        break;
                }
            }
        }, this);

    }

    /*
    public void hydrateSpecVersion(MutableService descriptor, Node specVersionNode)
            throws DescriptorBindingException {

        NodeList specVersionChildren = specVersionNode.getChildNodes();
        for (int i = 0; i < specVersionChildren.getLength(); i++) {
            Node specVersionChild = specVersionChildren.item(i);

            if (specVersionChild.getNodeType() != Node.ELEMENT_NODE)
                continue;

            MutableUDAVersion version = new MutableUDAVersion();
            if (ELEMENT.major.equals(specVersionChild)) {
                version.major = Integer.valueOf(XMLUtil.getTextContent(specVersionChild));
            } else if (ELEMENT.minor.equals(specVersionChild)) {
                version.minor = Integer.valueOf(XMLUtil.getTextContent(specVersionChild));
            }
        }
    }
    */

    public <S extends Service<?, ?, S>> void hydrateActionList(MutableService<?, S> descriptor, IXmlReader xmlReader) throws DescriptorBindingException, XMLStreamException {


        XMLUtil.readElements(xmlReader, reader -> {
            ELEMENT actionListChild = ELEMENT.valueOrNullOf(reader.getLocalName());

            if (ELEMENT.action.equals(actionListChild)) {
                MutableAction<S> action = new MutableAction<>();
                hydrateAction(action, reader);
                descriptor.actions.add(action);
            }
        }, this);
    }

    public <S extends Service<?, ?, S>> void hydrateAction(MutableAction<S> action, IXmlReader xmlReader) throws XMLStreamException, DescriptorBindingException {


        XMLUtil.readElements(xmlReader, reader -> {
            ELEMENT actionNodeChild = ELEMENT.valueOrNullOf(reader.getLocalName());
            if (actionNodeChild!=null) {
                switch (actionNodeChild)
                {
                    case name:
                        action.name = XMLUtil.getTextContent(reader, UDA10ServiceDescriptorBinderImpl.this);
                        break;
                    case argumentList:
                        XMLUtil.readElements(reader, reader2 -> {
                            MutableActionArgument<S> actionArgument = new MutableActionArgument<>();
                            hydrateActionArgument(actionArgument, reader2);
                            action.arguments.add(actionArgument);
                        }, this);
                        break;
                    default:
                        break;
                }
            }
        }, this);

    }

    public void hydrateActionArgument(MutableActionArgument<?> actionArgument, IXmlReader xmlReader) throws XMLStreamException, DescriptorBindingException {


        XMLUtil.readElements(xmlReader, reader -> {
            ELEMENT argumentNodeChild = ELEMENT.valueOrNullOf(reader.getLocalName());
            if (argumentNodeChild!=null) {
                switch (argumentNodeChild)
                {
                    case name:
                        actionArgument.name = XMLUtil.getTextContent(xmlReader, UDA10ServiceDescriptorBinderImpl.this);
                        break;
                    case direction:
                    {
                        String directionString = XMLUtil.getTextContent(xmlReader, UDA10ServiceDescriptorBinderImpl.this);
                        try {
                            actionArgument.direction = ActionArgument.Direction.valueOf(directionString.toUpperCase(Locale.ROOT));
                        } catch (IllegalArgumentException ex) {
                            // TODO: UPNP VIOLATION: Pelco SpectraIV-IP uses illegal value INOUT
                            log.warn(() -> "UPnP specification violation: Invalid action argument direction, assuming 'IN': " + directionString);
                            actionArgument.direction = ActionArgument.Direction.IN;
                        }
                    }
                        break;
                    case relatedStateVariable:
                        actionArgument.relatedStateVariable = XMLUtil.getTextContent(xmlReader, UDA10ServiceDescriptorBinderImpl.this);
                        break;
                    case retval:
                        actionArgument.retval = true;
                        break;
                    default:
                        break;
                }
            }
        }, this);
    }

    public <S extends Service<?, ?, S>> void hydrateServiceStateTableList(MutableService<?, S> descriptor, IXmlReader xmlReader) throws XMLStreamException, DescriptorBindingException {


        XMLUtil.readElements(xmlReader, reader -> {
            ELEMENT serviceStateTableChild = ELEMENT.valueOrNullOf(reader.getLocalName());

            if (ELEMENT.stateVariable.equals(serviceStateTableChild)) {
                MutableStateVariable<S> stateVariable = new MutableStateVariable<>();
                hydrateStateVariable(stateVariable, reader);
                descriptor.stateVariables.add(stateVariable);
            }
        }, this);
    }

    public void hydrateStateVariable(MutableStateVariable<?> stateVariable, IXmlReader xmlReader) throws XMLStreamException, DescriptorBindingException {
        String att=xmlReader.getAttributeValue(ATTRIBUTE.sendEvents.toString());
        stateVariable.eventDetails = new StateVariableEventDetails(
                att != null &&
                        "YES".equals(att.toUpperCase(Locale.ROOT))
        );


        XMLUtil.readElements(xmlReader, reader -> {
            ELEMENT stateVariableChild = ELEMENT.valueOrNullOf(reader.getLocalName());
            if (stateVariableChild!=null) {
                switch (stateVariableChild)
                {
                    case name:
                        stateVariable.name = XMLUtil.getTextContent(xmlReader, UDA10ServiceDescriptorBinderImpl.this);
                        break;
                    case dataType:
                    {
                        String dtName = XMLUtil.getTextContent(xmlReader, UDA10ServiceDescriptorBinderImpl.this);
                        Datatype.Builtin builtin = Datatype.Builtin.getByDescriptorName(dtName);
                        stateVariable.dataType = builtin != null ? builtin.getDatatype() : new CustomDatatype(dtName);
                    }
                        break;
                    case defaultValue:
                        stateVariable.defaultValue = XMLUtil.getTextContent(xmlReader, UDA10ServiceDescriptorBinderImpl.this);
                        break;
                    case allowedValueList:
                    {
                        List<String> allowedValues = new ArrayList<>();

                        XMLUtil.readElements(xmlReader, reader2 -> {
                            ELEMENT allowedValueListChild = ELEMENT.valueOrNullOf(reader2.getLocalName());

                            if (ELEMENT.allowedValue.equals(allowedValueListChild))
                                allowedValues.add(XMLUtil.getTextContent(xmlReader, UDA10ServiceDescriptorBinderImpl.this));
                        }, this);

                        stateVariable.allowedValues = allowedValues;
                    }
                        break;
                    case allowedValueRange:
                    {
                        MutableAllowedValueRange range = new MutableAllowedValueRange();


                        XMLUtil.readElements(xmlReader, reader2 -> {
                            ELEMENT allowedValueRangeChild = ELEMENT.valueOrNullOf(reader2.getLocalName());
                            if (allowedValueRangeChild!=null) {
                                switch (allowedValueRangeChild)
                                {
                                    case minimum:
                                        try {
                                            range.minimum = Long.valueOf(XMLUtil.getTextContent(xmlReader, UDA10ServiceDescriptorBinderImpl.this));
                                        } catch (Exception ignored) {
                                        }
                                        break;
                                    case maximum:
                                        try {
                                            range.maximum = Long.valueOf(XMLUtil.getTextContent(xmlReader, UDA10ServiceDescriptorBinderImpl.this));
                                        } catch (Exception ignored) {
                                        }
                                        break;
                                    case step:
                                        try {
                                            range.step = Long.valueOf(XMLUtil.getTextContent(xmlReader, UDA10ServiceDescriptorBinderImpl.this));
                                        } catch (Exception ignored) {
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }, this);

                        stateVariable.allowedValueRange = range;
                    }
                        break;
                    default:
                        break;
                }
            }
        }, this);
    }

    @Override
    public String generate(Service<?, ?, ?> service) throws DescriptorBindingException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Generating XML descriptor from service model: " + service);
            }

            return buildXMLString(service);

        } catch (Exception ex) {
            throw DescriptorBindingException.getDescriptorBindingException("Could not build DOM: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String buildXMLString(Service<?, ?, ?> service) throws DescriptorBindingException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Generating XML descriptor from service model: " + service);
            }
            return XMLUtil.generateXMLToString(xmlStreamWriter -> generateScpd(service, xmlStreamWriter));

        } catch (Exception ex) {
            throw DescriptorBindingException.getDescriptorBindingException("Could not generate service descriptor: " + ex.getMessage(), ex);
        }
    }

    private void generateScpd(Service<?, ?, ?> serviceModel, IXmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement(Descriptor.Service.NAMESPACE_URI, ELEMENT.scpd.toString());

        generateSpecVersion(serviceModel, xmlWriter);
        if (serviceModel.hasActions()) {
            generateActionList(serviceModel, xmlWriter);
        }
        generateServiceStateTable(serviceModel, xmlWriter);
        xmlWriter.writeEndElement();
    }

    private void generateSpecVersion(Service<?, ?, ?> serviceModel, IXmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement(ELEMENT.specVersion.name());
        appendNewElementIfNotNull(xmlWriter, ELEMENT.major, serviceModel.getDevice().getVersion().getMajor());
        appendNewElementIfNotNull(xmlWriter, ELEMENT.minor, serviceModel.getDevice().getVersion().getMinor());
        xmlWriter.writeEndElement();
    }

    private void generateActionList(Service<?, ?, ?> serviceModel, IXmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement(ELEMENT.actionList.name());


        for (Action<?> action : serviceModel.getActions()) {
            if (!QueryStateVariableAction.ACTION_NAME.equals(action.getName()))
                generateAction(action, xmlWriter);
        }
        xmlWriter.writeEndElement();
    }

    private void generateAction(Action<?> action, IXmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement(ELEMENT.action.name());

        appendNewElementIfNotNull(xmlWriter, ELEMENT.name, action.getName());

        if (action.hasArguments()) {
            xmlWriter.writeStartElement(ELEMENT.argumentList.name());
            for (ActionArgument<?> actionArgument : action.getArguments()) {
                generateActionArgument(actionArgument, xmlWriter);
            }
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
    }

    private void generateActionArgument(ActionArgument<?> actionArgument, IXmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement(ELEMENT.argument.name());

        appendNewElementIfNotNull(xmlWriter, ELEMENT.name, actionArgument.getName());
        appendNewElementIfNotNull(xmlWriter, ELEMENT.direction, actionArgument.getDirection().toString().toLowerCase(Locale.ROOT));
        if (actionArgument.isReturnValue()) {
            // TODO: UPNP VIOLATION: WMP12 will discard RenderingControl service if it contains <retval> tags
            log.warn(() -> "UPnP specification violation: Not producing <retval> element to be compatible with WMP12: " + actionArgument);
            // appendNewElement(descriptor, actionArgumentElement, ELEMENT.retval);
        }
        appendNewElementIfNotNull(xmlWriter, ELEMENT.relatedStateVariable, actionArgument.getRelatedStateVariableName());
        xmlWriter.writeEndElement();
    }

    private void generateServiceStateTable(Service<?, ?, ?> serviceModel, IXmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement(ELEMENT.serviceStateTable.name());

        for (StateVariable<?> stateVariable : serviceModel.getStateVariables()) {
            generateStateVariable(stateVariable, xmlWriter);
        }
        xmlWriter.writeEndElement();
    }

    private void generateStateVariable(StateVariable<?> stateVariable, IXmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement(ELEMENT.stateVariable.name());
        if (stateVariable.getEventDetails().isSendEvents()) {
            xmlWriter.writeAttribute(ATTRIBUTE.sendEvents.toString(), "yes");
        } else {
            xmlWriter.writeAttribute(ATTRIBUTE.sendEvents.toString(), "no");
        }


        appendNewElementIfNotNull(xmlWriter, ELEMENT.name, stateVariable.getName());

        if (stateVariable.getTypeDetails().getDatatype() instanceof CustomDatatype) {
            appendNewElementIfNotNull(xmlWriter, ELEMENT.dataType,
                    ((CustomDatatype)stateVariable.getTypeDetails().getDatatype()).getName());
        } else {
            appendNewElementIfNotNull(xmlWriter, ELEMENT.dataType,
                    stateVariable.getTypeDetails().getDatatype().getBuiltin().getDescriptorName());
        }

        appendNewElementIfNotNull(xmlWriter, ELEMENT.defaultValue,
                stateVariable.getTypeDetails().getDefaultValue());


        if (stateVariable.getTypeDetails().getAllowedValues() != null) {
            xmlWriter.writeStartElement(ELEMENT.allowedValueList.name());
            for (String allowedValue : stateVariable.getTypeDetails().getAllowedValues()) {
                appendNewElementIfNotNull(xmlWriter, ELEMENT.allowedValue, allowedValue);
            }
            xmlWriter.writeEndElement();
        }

        if (stateVariable.getTypeDetails().getAllowedValueRange() != null) {
            xmlWriter.writeStartElement(ELEMENT.allowedValueRange.name());
            appendNewElementIfNotNull(
                    xmlWriter, ELEMENT.minimum, stateVariable.getTypeDetails().getAllowedValueRange().getMinimum()
            );
            appendNewElementIfNotNull(
                    xmlWriter, ELEMENT.maximum, stateVariable.getTypeDetails().getAllowedValueRange().getMaximum()
            );
            if (stateVariable.getTypeDetails().getAllowedValueRange().getStep() >= 1L) {
                appendNewElementIfNotNull(
                        xmlWriter, ELEMENT.step, stateVariable.getTypeDetails().getAllowedValueRange().getStep()
                );
            }
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
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

