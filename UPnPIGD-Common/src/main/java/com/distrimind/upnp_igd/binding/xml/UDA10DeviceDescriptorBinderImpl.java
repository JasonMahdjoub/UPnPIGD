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

package com.distrimind.upnp_igd.binding.xml;

import static com.distrimind.upnp_igd.model.XMLUtil.appendNewElementIfNotNull;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

import com.distrimind.flexilogxml.exceptions.XMLStreamException;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.flexilogxml.xml.IXmlReader;
import com.distrimind.flexilogxml.xml.IXmlWriter;
import com.distrimind.upnp_igd.Log;

import com.distrimind.upnp_igd.binding.staging.MutableDevice;
import com.distrimind.upnp_igd.binding.staging.MutableIcon;
import com.distrimind.upnp_igd.binding.staging.MutableService;
import com.distrimind.upnp_igd.binding.xml.Descriptor.Device.ELEMENT;
import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.XMLUtil;
import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.Icon;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.profile.RemoteClientInfo;
import com.distrimind.upnp_igd.model.types.DLNACaps;
import com.distrimind.upnp_igd.model.types.DLNADoc;
import com.distrimind.upnp_igd.model.types.InvalidValueException;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.model.types.UDN;
import com.distrimind.upnp_igd.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp_igd.util.Exceptions;
import com.distrimind.upnp_igd.util.MimeType;

/**
 * Implementation based on JAXP DOM.
 *
 * @author Christian Bauer
 * @author Jason Mahdjoub, use XML Parser instead of Document
 */
public class UDA10DeviceDescriptorBinderImpl implements DeviceDescriptorBinder, XMLUtil.ErrorHandler {

    final private static DMLogger log = Log.getLogger(UDA10DeviceDescriptorBinderImpl.class);
    private final NetworkAddressFactory networkAddressFactory;
    static boolean isNotValidRemoteAddress(URL u, NetworkAddressFactory networkAddressFactory)
    {
        if (u==null)
            return false;
        return isNotValidRemoteAddress(u.getHost(), networkAddressFactory);
    }
    static boolean isNotValidRemoteAddress(String host, NetworkAddressFactory networkAddressFactory)
    {
        try {
            InetAddress ia = InetAddress.getByName(host);
            ia = networkAddressFactory.getLocalAddress(
                    null,
                    ia instanceof Inet6Address,
                    ia
            );
            if (ia == null)
                return true;
        } catch (Exception ignored) {
            return true;
        }
        return false;
    }
    public UDA10DeviceDescriptorBinderImpl(NetworkAddressFactory networkAddressFactory)
    {
        this.networkAddressFactory=networkAddressFactory;
    }
    @Override
    public <D extends Device<?, D, S>, S extends Service<?, D, S>> D describe(D undescribedDevice, String descriptorXml) throws DescriptorBindingException, ValidationException {

        if (descriptorXml == null) {
            throw new DescriptorBindingException("Null descriptor");
        }
        if (descriptorXml.isEmpty()) {
            throw new DescriptorBindingException("Empty descriptor");
        }

        try {
            log.debug(() -> "Populating device from XML descriptor: " + undescribedDevice);
            // We can not validate the XML document. There is no possible XML schema (maybe RELAX NG) that would properly
            // constrain the UDA 1.0 device descriptor documents: Any unknown element or attribute must be ignored, order of elements
            // is not guaranteed. Try to write a schema for that! No combination of <xsd:any namespace="##any"> and <xsd:choice>
            // works with that... But hey, MSFT sure has great tech guys! So what we do here is just parsing out the known elements
            // and ignoring the other shit. We'll also do some very basic validation of required elements, but that's it.

            // And by the way... try this with JAXB instead of manual DOM processing! And you thought it couldn't get worse....

            return XMLUtil.readXML(xmlReader -> describe(undescribedDevice, xmlReader), this, descriptorXml.trim());// TODO: UPNP VIOLATION: Virgin Media Superhub sends trailing spaces/newlines after last XML element, need to trim()

        } catch (ValidationException | DescriptorBindingException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw DescriptorBindingException.getDescriptorBindingException("Could not parse device descriptor: " + ex, ex);
        }
    }
    @Override
    public <D extends Device<?, D, S>, S extends Service<?, D, S>> D describe(D undescribedDevice, IXmlReader xmlReader) throws DescriptorBindingException, ValidationException {
        try {
            log.debug(() -> "Populating device from DOM: " + undescribedDevice);

            // Read the XML into a mutable descriptor graph
            MutableDevice<D, S> descriptor = new MutableDevice<>();
            XMLUtil.readRootElement(xmlReader, reader -> hydrateRoot(descriptor, reader), this, Descriptor.Device.NAMESPACE_URI, ELEMENT.root.name(), log);

            // Build the immutable descriptor graph
            return buildInstance(undescribedDevice, descriptor);

        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw DescriptorBindingException.getDescriptorBindingException("Could not parse device DOM: " + ex, ex);
        }
    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> D buildInstance(D undescribedDevice, MutableDevice<D, S> descriptor) throws ValidationException {
        D res=descriptor.build(undescribedDevice);
        if (res!=null && res.getDetails()!=null && isNotValidRemoteAddress(res.getDetails().getBaseURL(), networkAddressFactory))
            return null;

        return res;
    }

    protected <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateRoot(MutableDevice<D, S> descriptor, IXmlReader xmlReader) throws DescriptorBindingException, XMLStreamException {

        class DN
        {
            String deviceNode = null;
        }
        final DN dn=new DN();

        XMLUtil.readElements(xmlReader, reader -> {
            ELEMENT e=ELEMENT.valueOrNullOf(reader.getLocalName());
            if (e!=null) {
                switch (e)
                {
                    case specVersion:
                        hydrateSpecVersion(descriptor, reader);
                        break;
                    case URLBase:
                        try {
                            String urlString = XMLUtil.getTextContent(xmlReader, this);
                            if (urlString != null && !urlString.isEmpty()) {
                                // We hope it's  RFC 2396 and RFC 2732 compliant
                                descriptor.baseURL = new URI(urlString).toURL();
                            }
                        } catch (Exception ex) {
                            throw new DescriptorBindingException("Invalid URLBase: " + ex.getMessage());
                        }
                        break;
                    case device:
                    {
                        // Just sanity check here...
                        if (dn.deviceNode != null)
                            throw new DescriptorBindingException("Found multiple <device> elements in <root>");
                        dn.deviceNode = e.name();
                        hydrateDevice(descriptor, xmlReader);
                    }
                        break;
                    default:
                    {
                        log.debug(() -> "Ignoring unknown element: " + e);
                    }
                        break;
                }
            }
        }, this);


        if (dn.deviceNode == null) {
            throw new DescriptorBindingException("No <device> element in <root>");
        }

    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateSpecVersion(MutableDevice<D,S> descriptor, IXmlReader xmlReader) throws DescriptorBindingException, XMLStreamException {


        XMLUtil.readElements(xmlReader, reader -> {
            ELEMENT e=ELEMENT.valueOrNullOf(xmlReader.getLocalName());
            if (e!=null) {
                switch (e) {
                    case major: {
                        String version = XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this).trim();
                        if (!"1".equals(version)) {
                            log.warn("Unsupported UDA major version, ignoring: " + version);
                            version = "1";
                        }
                        descriptor.udaVersion.major = Integer.parseInt(version);
                    }
                    break;
                    case minor: {
                        String version = XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this).trim();
                        if (!"0".equals(version)) {
                            log.warn(() -> "Unsupported UDA minor version, ignoring: " + version);
                        }
                        descriptor.udaVersion.minor = 0;
                    }
                    break;
                    default:
                        break;
                }
            }

        }, this);


    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateDevice(MutableDevice<D, S> descriptor, IXmlReader xmlReader) throws DescriptorBindingException, XMLStreamException {

        XMLUtil.readElements(xmlReader, reader -> {
            ELEMENT deviceNodeChild=ELEMENT.valueOrNullOf(reader.getLocalName());
            if (deviceNodeChild!=null) {
                switch (deviceNodeChild)
                {
                    case deviceType:
                        descriptor.deviceType = XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this);
                        break;
                    case friendlyName:
                        descriptor.friendlyName = XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this);
                        break;
                    case manufacturer:
                        descriptor.manufacturer = XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this);
                        break;
                    case manufacturerURL:
                        descriptor.manufacturerURI = parseURI(XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this));
                        break;
                    case modelDescription:
                        descriptor.modelDescription = XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this);
                        break;
                    case modelName:
                        descriptor.modelName = XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this);
                        break;
                    case modelNumber:
                        descriptor.modelNumber = XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this);
                        break;
                    case modelURL:
                        descriptor.modelURI = parseURI(XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this));
                        break;
                    case presentationURL:
                        descriptor.presentationURI = parseURI(XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this));
                        break;
                    case UPC:
                        descriptor.upc = XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this);
                        break;
                    case serialNumber:
                        descriptor.serialNumber = XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this);
                        break;
                    case UDN:
                        descriptor.udn = UDN.valueOf(XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this));
                        break;
                    case iconList:
                        hydrateIconList(descriptor, xmlReader);
                        break;
                    case serviceList:
                        hydrateServiceList(descriptor, xmlReader);
                        break;
                    case deviceList:
                        hydrateDeviceList(descriptor, xmlReader);
                        break;
                    case X_DLNADOC:
                        if (Descriptor.Device.DLNA_PREFIX.equals(xmlReader.getPrefix())) {
                            String txt = XMLUtil.getTextContent(xmlReader, this);
                            try {
                                descriptor.dlnaDocs.add(DLNADoc.valueOf(txt));
                            } catch (InvalidValueException ex) {
                                log.info(() -> "Invalid X_DLNADOC value, ignoring value: " + txt);
                            }
                        }
                        break;
                    case X_DLNACAP:
                        if (Descriptor.Device.DLNA_PREFIX.equals(xmlReader.getPrefix())) {
                            descriptor.dlnaCaps = DLNACaps.valueOf(XMLUtil.getTextContent(xmlReader, this));
                        }
                        break;
                    default:
                        break;
                }
            }
        }, this);
    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateIconList(MutableDevice<D, S> descriptor, IXmlReader xmlReader) throws DescriptorBindingException, XMLStreamException {


        XMLUtil.readElements(xmlReader, reader ->  {
            ELEMENT iconListNodeChild = ELEMENT.valueOrNullOf(reader.getLocalName());


            if (ELEMENT.icon.equals(iconListNodeChild)) {

                MutableIcon icon = new MutableIcon();


                XMLUtil.readElements(xmlReader, reader2 ->  {
                    ELEMENT iconChild = ELEMENT.valueOrNullOf(reader2.getLocalName());

                    if (iconChild!=null) {
                        switch (iconChild)
                        {
                            case width:
                                icon.width = (Integer.parseInt(XMLUtil.getTextContent(reader2, UDA10DeviceDescriptorBinderImpl.this)));
                                break;
                            case height:
                                icon.height = (Integer.parseInt(XMLUtil.getTextContent(reader2, UDA10DeviceDescriptorBinderImpl.this)));
                                break;
                            case depth:
                            {
                                String depth = XMLUtil.getTextContent(reader2, UDA10DeviceDescriptorBinderImpl.this);
                                try {
                                    icon.depth = (Integer.parseInt(depth));
                                } catch (NumberFormatException ex) {
                                    log.warn(() -> "Invalid icon depth '" + depth + "', using 16 as default: " + ex);
                                    icon.depth = 16;
                                }
                            }
                                break;
                            case url:
                                icon.uri = parseURI(XMLUtil.getTextContent(reader2, UDA10DeviceDescriptorBinderImpl.this));
                                break;
                            case mimetype:
                                try {
                                    icon.mimeType = XMLUtil.getTextContent(reader2, UDA10DeviceDescriptorBinderImpl.this);
                                    MimeType.valueOf(icon.mimeType);
                                } catch (IllegalArgumentException ex) {
                                    log.warn(() -> "Ignoring invalid icon mime type: " + icon.mimeType);
                                    icon.mimeType = "";
                                }
                                break;
                            default:
                                break;
                        }
                    }

                }, this);

                descriptor.icons.add(icon);
            }
        }, this);
    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateServiceList(MutableDevice<D, S> descriptor, IXmlReader xmlReader) throws XMLStreamException, DescriptorBindingException {


        XMLUtil.readElements(xmlReader, reader ->  {
            ELEMENT serviceListNodeChild = ELEMENT.valueOrNullOf(reader.getLocalName());

            if (ELEMENT.service.equals(serviceListNodeChild)) {



                try {
                    MutableService<D, S> service = new MutableService<>();

                    XMLUtil.readElements(xmlReader, reader2 ->  {
                        ELEMENT serviceChild = ELEMENT.valueOrNullOf(reader2.getLocalName());

                        if (serviceChild!=null) {
                            switch (serviceChild)
                            {
                                case serviceType:
                                    service.serviceType = (ServiceType.valueOf(XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this)));
                                    break;
                                case serviceId:
                                    service.serviceId = (ServiceId.valueOf(XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this)));
                                    break;
                                case SCPDURL:
                                    service.descriptorURI = parseURI(XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this));
                                    break;
                                case controlURL:
                                    service.controlURI = parseURI(XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this));
                                    break;
                                case eventSubURL:
                                    service.eventSubscriptionURI = parseURI(XMLUtil.getTextContent(xmlReader, UDA10DeviceDescriptorBinderImpl.this));
                                    break;
                                default:
                                    break;
                            }
                        }
                    }, this);

                    descriptor.services.add(service);
                } catch (InvalidValueException ex) {
                    log.warn(() ->
                            "UPnP specification violation, skipping invalid service declaration. " + ex.getMessage()
                    );
                }
            }
        }, this);
    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateDeviceList(MutableDevice<D, S> descriptor, IXmlReader xmlReader) throws DescriptorBindingException, XMLStreamException {


        XMLUtil.readElements(xmlReader, reader ->  {
            ELEMENT deviceListNodeChild = ELEMENT.valueOrNullOf(reader.getLocalName());

            if (ELEMENT.device.equals(deviceListNodeChild)) {
                MutableDevice<D, S> embeddedDevice = new MutableDevice<>();
                embeddedDevice.parentDevice = descriptor;
                descriptor.embeddedDevices.add(embeddedDevice);
                hydrateDevice(embeddedDevice, xmlReader);
            }
        }, this);

    }

    @Override
    public String generate(Device<?, ?, ?> deviceModel, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException {
        try {
            log.debug(() -> "Generating XML descriptor from device model: " + deviceModel);

            return buildXMLString(deviceModel, info, namespace);

        } catch (Exception ex) {
            throw DescriptorBindingException.getDescriptorBindingException("Could not build DOM: " + ex.getMessage(), ex);
        }
    }
    @Override
    public String buildXMLString(Device<?, ?, ?> deviceModel, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException {

        try {
            log.debug(() -> "Generating DOM from device model: " + deviceModel);
            return XMLUtil.generateXMLToString(xmlStreamWriter -> {
                generateRoot(namespace, deviceModel, xmlStreamWriter, info);
            });

        } catch (Exception ex) {
            throw DescriptorBindingException.getDescriptorBindingException("Could not generate device descriptor: " + ex.getMessage(), ex);
        }
    }

    protected void generateRoot(Namespace namespace, Device<?, ?, ?> deviceModel, IXmlWriter xmlStreamWriter, RemoteClientInfo info) throws XMLStreamException {
        xmlStreamWriter.writeStartElement(Descriptor.Device.NAMESPACE_URI, ELEMENT.root.toString());

        generateSpecVersion(namespace, deviceModel, xmlStreamWriter);
        generateDevice(namespace, deviceModel, xmlStreamWriter, info);
        xmlStreamWriter.writeEndElement();
        /* UDA 1.1 spec says: Don't use URLBase anymore
        if (deviceModel.getBaseURL() != null) {
            appendChildElementWithTextContent(descriptor, rootElement, "URLBase", deviceModel.getBaseURL());
        }
        */


    }

    protected void generateSpecVersion(Namespace namespace, Device<?, ?, ?> deviceModel, IXmlWriter xmlStreamWriter) throws XMLStreamException {
        //Element specVersionElement = appendNewElement(descriptor, rootElement, ELEMENT.specVersion);
        xmlStreamWriter.writeStartElement(ELEMENT.specVersion.name());
        appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.major, deviceModel.getVersion().getMajor());
        appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.minor, deviceModel.getVersion().getMinor());
        xmlStreamWriter.writeEndElement();
    }

    protected void generateDevice(Namespace namespace, Device<?, ?, ?> deviceModel, IXmlWriter xmlStreamWriter, RemoteClientInfo info) throws XMLStreamException {
        xmlStreamWriter.writeStartElement(ELEMENT.device.name());
        appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.deviceType, deviceModel.getType());

        DeviceDetails deviceModelDetails = deviceModel.getDetails(info);
        appendNewElementIfNotNull(
                xmlStreamWriter, ELEMENT.friendlyName,
                deviceModelDetails.getFriendlyName()
        );

        if (deviceModelDetails.getManufacturerDetails() != null) {
            appendNewElementIfNotNull(
                    xmlStreamWriter, ELEMENT.manufacturer,
                    deviceModelDetails.getManufacturerDetails().getManufacturer()
            );
            appendNewElementIfNotNull(
                    xmlStreamWriter, ELEMENT.manufacturerURL,
                    deviceModelDetails.getManufacturerDetails().getManufacturerURI()
            );
        }
        if (deviceModelDetails.getModelDetails() != null) {
            appendNewElementIfNotNull(
                    xmlStreamWriter, ELEMENT.modelDescription,
                    deviceModelDetails.getModelDetails().getModelDescription()
            );
            appendNewElementIfNotNull(
                    xmlStreamWriter, ELEMENT.modelName,
                    deviceModelDetails.getModelDetails().getModelName()
            );
            appendNewElementIfNotNull(
                    xmlStreamWriter, ELEMENT.modelNumber,
                    deviceModelDetails.getModelDetails().getModelNumber()
            );
            appendNewElementIfNotNull(
                    xmlStreamWriter, ELEMENT.modelURL,
                    deviceModelDetails.getModelDetails().getModelURI()
            );
        }
        appendNewElementIfNotNull(
                xmlStreamWriter, ELEMENT.serialNumber,
                deviceModelDetails.getSerialNumber()
        );
        appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.UDN, deviceModel.getIdentity().getUdn());
        appendNewElementIfNotNull(
                xmlStreamWriter, ELEMENT.presentationURL,
                deviceModelDetails.getPresentationURI()
        );
        appendNewElementIfNotNull(
                xmlStreamWriter, ELEMENT.UPC,
                deviceModelDetails.getUpc()
        );

        if (deviceModelDetails.getDlnaDocs() != null) {
            for (DLNADoc dlnaDoc : deviceModelDetails.getDlnaDocs()) {
                appendNewElementIfNotNull(
                        xmlStreamWriter, Descriptor.Device.DLNA_PREFIX, ELEMENT.X_DLNADOC.name(),
                        dlnaDoc, Descriptor.Device.DLNA_NAMESPACE_URI
                );
            }
        }
        appendNewElementIfNotNull(
                xmlStreamWriter, Descriptor.Device.DLNA_PREFIX,  ELEMENT.X_DLNACAP.name(),
                deviceModelDetails.getDlnaCaps(), Descriptor.Device.DLNA_NAMESPACE_URI
        );

        appendNewElementIfNotNull(
                xmlStreamWriter, Descriptor.Device.SEC_PREFIX, ELEMENT.ProductCap.name(),
                deviceModelDetails.getSecProductCaps(), Descriptor.Device.SEC_NAMESPACE_URI
        );

        appendNewElementIfNotNull(
                xmlStreamWriter, Descriptor.Device.SEC_PREFIX, ELEMENT.X_ProductCap.name(),
                deviceModelDetails.getSecProductCaps(), Descriptor.Device.SEC_NAMESPACE_URI
        );

        generateIconList(namespace, deviceModel, xmlStreamWriter);
        generateServiceList(namespace, deviceModel, xmlStreamWriter);
        generateDeviceList(namespace, deviceModel, xmlStreamWriter, info);
        xmlStreamWriter.writeEndElement();
    }

    protected void generateIconList(Namespace namespace, Device<?, ?, ?> deviceModel, IXmlWriter xmlStreamWriter) throws XMLStreamException {
        if (!deviceModel.hasIcons()) return;
        xmlStreamWriter.writeStartElement(ELEMENT.iconList.name());

        for (Icon icon : deviceModel.getIcons()) {
            xmlStreamWriter.writeStartElement(ELEMENT.icon.name());

            appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.mimetype, icon.getMimeType());
            appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.width, icon.getWidth());
            appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.height, icon.getHeight());
            appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.depth, icon.getDepth());
            if (deviceModel instanceof RemoteDevice) {
                appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.url,  icon.getUri());
            } else if (deviceModel instanceof LocalDevice) {
                appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.url,  namespace.getIconPath(icon));
            }
            xmlStreamWriter.writeEndElement();
        }
        xmlStreamWriter.writeEndElement();
    }

    protected void generateServiceList(Namespace namespace, Device<?, ?, ?> deviceModel, IXmlWriter xmlStreamWriter) throws XMLStreamException {
        if (!deviceModel.hasServices()) return;
        xmlStreamWriter.writeStartElement(ELEMENT.serviceList.name());

        for (Service<?, ?, ?> service : deviceModel.getServices()) {
            xmlStreamWriter.writeStartElement(ELEMENT.service.name());

            appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.serviceType, service.getServiceType());
            appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.serviceId, service.getServiceId());
            if (service instanceof RemoteService) {
                RemoteService rs = (RemoteService) service;
                appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.SCPDURL, rs.getDescriptorURI());
                appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.controlURL, rs.getControlURI());
                appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.eventSubURL, rs.getEventSubscriptionURI());
            } else if (service instanceof LocalService) {
                LocalService<?> ls = (LocalService<?>) service;
                appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.SCPDURL, namespace.getDescriptorPath(ls));
                appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.controlURL, namespace.getControlPath(ls));
                appendNewElementIfNotNull(xmlStreamWriter, ELEMENT.eventSubURL, namespace.getEventSubscriptionPath(ls));
            }
            xmlStreamWriter.writeEndElement();
        }
        xmlStreamWriter.writeEndElement();
    }

    protected void generateDeviceList(Namespace namespace, Device<?, ?, ?> deviceModel, IXmlWriter xmlStreamWriter, RemoteClientInfo info) throws XMLStreamException {
        if (!deviceModel.hasEmbeddedDevices()) return;
        xmlStreamWriter.writeStartElement(ELEMENT.deviceList.name());

        for (Device<?, ?, ?> device : deviceModel.getEmbeddedDevices()) {
            generateDevice(namespace, device, xmlStreamWriter, info);
        }
        xmlStreamWriter.writeEndElement();
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

    static protected URI parseURI(String _uri) {

        // TODO: UPNP VIOLATION: Netgear DG834 uses a non-URI: 'www.netgear.com'
        String uri;
        if (_uri.startsWith("www.")) {
            uri = "http://" + _uri;
        }
        else
            uri=_uri;

        // TODO: UPNP VIOLATION: Plutinosoft uses unencoded relative URIs
        // /var/mobile/Applications/71367E68-F30F-460B-A2D2-331509441D13/Windows Media Player Streamer.app/Icon-ps3.jpg
        if (uri.contains(" ")) {
            // We don't want to split/encode individual parts of the URI, too much work
            // TODO: But we probably should do this? Because browsers do it, everyone
            // seems to think that spaces in URLs are somehow OK...
            uri = uri.replaceAll(" ", "%20");
        }

        try {
            return URI.create(uri);
        } catch (Throwable ex) {
            /*
        	catch Throwable because on Android 2.2, parsing some invalid URI like "http://..."  gives:
        	        	java.lang.NullPointerException
        	        	 	at java.net.URI$Helper.isValidDomainName(URI.java:631)
        	        	 	at java.net.URI$Helper.isValidHost(URI.java:595)
        	        	 	at java.net.URI$Helper.parseAuthority(URI.java:544)
        	        	 	at java.net.URI$Helper.parseURI(URI.java:404)
        	        	 	at java.net.URI$Helper.access$100(URI.java:302)
        	        	 	at java.net.URI.<init>(URI.java:87)
        	        		at java.net.URI.create(URI.java:968)
            */
            log.debug(() -> "Illegal URI, trying with ./ prefix: " + Exceptions.unwrap(ex));
            // Ignore
        }
        try {
            // The java.net.URI class can't deal with "_urn:foobar" (yeah, great idea Intel UPnP tools guy), as
            // explained in RFC 3986:
            //
            // A path segment that contains a colon character (e.g., "this:that") cannot be used as the first segment
            // of a relative-path reference, as it would be mistaken for a scheme name. Such a segment must
            // be preceded by a dot-segment (e.g., "./this:that") to make a relative-path reference.
            //
            return URI.create("./" + uri);
        } catch (IllegalArgumentException ex) {
            log.warn("Illegal URI '" + uri + "', ignoring value: " + Exceptions.unwrap(ex));
            // Ignore
        }
        return null;
    }

}
