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

import static com.distrimind.upnp_igd.model.XMLUtil.appendNewElement;
import static com.distrimind.upnp_igd.model.XMLUtil.appendNewElementIfNotNull;

import java.io.StringReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.distrimind.upnp_igd.DocumentBuilderFactoryWithNonDTD;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Implementation based on JAXP DOM.
 *
 * @author Christian Bauer
 */
public class UDA10DeviceDescriptorBinderImpl implements DeviceDescriptorBinder, ErrorHandler {

    private static final Logger log = Logger.getLogger(DeviceDescriptorBinder.class.getName());
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

        if (descriptorXml == null || descriptorXml.isEmpty()) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }

        try {
            if (log.isLoggable(Level.FINE))
                log.fine("Populating device from XML descriptor: " + undescribedDevice);
            // We can not validate the XML document. There is no possible XML schema (maybe RELAX NG) that would properly
            // constrain the UDA 1.0 device descriptor documents: Any unknown element or attribute must be ignored, order of elements
            // is not guaranteed. Try to write a schema for that! No combination of <xsd:any namespace="##any"> and <xsd:choice>
            // works with that... But hey, MSFT sure has great tech guys! So what we do here is just parsing out the known elements
            // and ignoring the other shit. We'll also do some very basic validation of required elements, but that's it.

            // And by the way... try this with JAXB instead of manual DOM processing! And you thought it couldn't get worse....

            DocumentBuilderFactory factory = DocumentBuilderFactoryWithNonDTD.newDocumentBuilderFactoryWithNonDTDInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            documentBuilder.setErrorHandler(this);

            Document d = documentBuilder.parse(
                    new InputSource(
                            // TODO: UPNP VIOLATION: Virgin Media Superhub sends trailing spaces/newlines after last XML element, need to trim()
                            new StringReader(descriptorXml.trim())
                    )
            );

            return describe(undescribedDevice, d);

        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not parse device descriptor: " + ex, ex);
        }
    }
    @Override
    public <D extends Device<?, D, S>, S extends Service<?, D, S>> D describe(D undescribedDevice, Document dom) throws DescriptorBindingException, ValidationException {
        try {
            if (log.isLoggable(Level.FINE))
                log.fine("Populating device from DOM: " + undescribedDevice);

            // Read the XML into a mutable descriptor graph
            MutableDevice<D, S> descriptor = new MutableDevice<>();
            Element rootElement = dom.getDocumentElement();
            hydrateRoot(descriptor, rootElement);

            // Build the immutable descriptor graph
            return buildInstance(undescribedDevice, descriptor);

        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not parse device DOM: " + ex, ex);
        }
    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> D buildInstance(D undescribedDevice, MutableDevice<D, S> descriptor) throws ValidationException {
        D res=descriptor.build(undescribedDevice);
        if (res.getDetails()!=null && isNotValidRemoteAddress(res.getDetails().getBaseURL(), networkAddressFactory))
            return null;

        return res;
    }

    protected <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateRoot(MutableDevice<D, S> descriptor, Element rootElement) throws DescriptorBindingException {

        if (rootElement.getNamespaceURI() == null || !Descriptor.Device.NAMESPACE_URI.equals(rootElement.getNamespaceURI())) {
            if (log.isLoggable(Level.WARNING))
                log.warning("Wrong XML namespace declared on root element: " + rootElement.getNamespaceURI());
            return;
        }

        if (!rootElement.getNodeName().equals(ELEMENT.root.name())) {
            throw new DescriptorBindingException("Root element name is not <root>: " + rootElement.getNodeName());
        }

        NodeList rootChildren = rootElement.getChildNodes();

        Node deviceNode = null;

        for (int i = 0; i < rootChildren.getLength(); i++) {
            Node rootChild = rootChildren.item(i);

            if (rootChild.getNodeType() != Node.ELEMENT_NODE)
                continue;

            if (ELEMENT.specVersion.equals(rootChild)) {
                hydrateSpecVersion(descriptor, rootChild);
            } else if (ELEMENT.URLBase.equals(rootChild)) {
                try {
                    String urlString = XMLUtil.getTextContent(rootChild);
                    if (urlString != null && !urlString.isEmpty()) {
                        // We hope it's  RFC 2396 and RFC 2732 compliant
                        descriptor.baseURL = new URL(urlString);
                    }
                } catch (Exception ex) {
                    throw new DescriptorBindingException("Invalid URLBase: " + ex.getMessage());
                }
            } else if (ELEMENT.device.equals(rootChild)) {
                // Just sanity check here...
                if (deviceNode != null)
                    throw new DescriptorBindingException("Found multiple <device> elements in <root>");
                deviceNode = rootChild;
            } else {
                if (log.isLoggable(Level.FINER))
                    log.finer("Ignoring unknown element: " + rootChild.getNodeName());
            }
        }

        if (deviceNode == null) {
            throw new DescriptorBindingException("No <device> element in <root>");
        }
        hydrateDevice(descriptor, deviceNode);
    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateSpecVersion(MutableDevice<D,S> descriptor, Node specVersionNode) throws DescriptorBindingException {

        NodeList specVersionChildren = specVersionNode.getChildNodes();
        for (int i = 0; i < specVersionChildren.getLength(); i++) {
            Node specVersionChild = specVersionChildren.item(i);

            if (specVersionChild.getNodeType() != Node.ELEMENT_NODE)
                continue;

            if (ELEMENT.major.equals(specVersionChild)) {
                String version = XMLUtil.getTextContent(specVersionChild).trim();
                if (!"1".equals(version)) {
                    if (log.isLoggable(Level.WARNING))
                        log.warning("Unsupported UDA major version, ignoring: " + version);
                    version = "1";
                }
                descriptor.udaVersion.major = Integer.parseInt(version);
            } else if (ELEMENT.minor.equals(specVersionChild)) {
                String version = XMLUtil.getTextContent(specVersionChild).trim();
                if (!"0".equals(version)) {
                    if (log.isLoggable(Level.WARNING))
                        log.warning("Unsupported UDA minor version, ignoring: " + version);
                }
                descriptor.udaVersion.minor = 0;
            }

        }

    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateDevice(MutableDevice<D, S> descriptor, Node deviceNode) throws DescriptorBindingException {

        NodeList deviceNodeChildren = deviceNode.getChildNodes();
        for (int i = 0; i < deviceNodeChildren.getLength(); i++) {
            Node deviceNodeChild = deviceNodeChildren.item(i);

            if (deviceNodeChild.getNodeType() != Node.ELEMENT_NODE)
                continue;

            if (ELEMENT.deviceType.equals(deviceNodeChild)) {
                descriptor.deviceType = XMLUtil.getTextContent(deviceNodeChild);
            } else if (ELEMENT.friendlyName.equals(deviceNodeChild)) {
                descriptor.friendlyName = XMLUtil.getTextContent(deviceNodeChild);
            } else if (ELEMENT.manufacturer.equals(deviceNodeChild)) {
                descriptor.manufacturer = XMLUtil.getTextContent(deviceNodeChild);
            } else if (ELEMENT.manufacturerURL.equals(deviceNodeChild)) {
                descriptor.manufacturerURI = parseURI(XMLUtil.getTextContent(deviceNodeChild));
            } else if (ELEMENT.modelDescription.equals(deviceNodeChild)) {
                descriptor.modelDescription = XMLUtil.getTextContent(deviceNodeChild);
            } else if (ELEMENT.modelName.equals(deviceNodeChild)) {
                descriptor.modelName = XMLUtil.getTextContent(deviceNodeChild);
            } else if (ELEMENT.modelNumber.equals(deviceNodeChild)) {
                descriptor.modelNumber = XMLUtil.getTextContent(deviceNodeChild);
            } else if (ELEMENT.modelURL.equals(deviceNodeChild)) {
                descriptor.modelURI = parseURI(XMLUtil.getTextContent(deviceNodeChild));
            } else if (ELEMENT.presentationURL.equals(deviceNodeChild)) {
                descriptor.presentationURI = parseURI(XMLUtil.getTextContent(deviceNodeChild));
            } else if (ELEMENT.UPC.equals(deviceNodeChild)) {
                descriptor.upc = XMLUtil.getTextContent(deviceNodeChild);
            } else if (ELEMENT.serialNumber.equals(deviceNodeChild)) {
                descriptor.serialNumber = XMLUtil.getTextContent(deviceNodeChild);
            } else if (ELEMENT.UDN.equals(deviceNodeChild)) {
                descriptor.udn = UDN.valueOf(XMLUtil.getTextContent(deviceNodeChild));
            } else if (ELEMENT.iconList.equals(deviceNodeChild)) {
                hydrateIconList(descriptor, deviceNodeChild);
            } else if (ELEMENT.serviceList.equals(deviceNodeChild)) {
                hydrateServiceList(descriptor, deviceNodeChild);
            } else if (ELEMENT.deviceList.equals(deviceNodeChild)) {
                hydrateDeviceList(descriptor, deviceNodeChild);
            } else if (ELEMENT.X_DLNADOC.equals(deviceNodeChild) &&
                Descriptor.Device.DLNA_PREFIX.equals(deviceNodeChild.getPrefix())) {
                String txt = XMLUtil.getTextContent(deviceNodeChild);
                try {
                    descriptor.dlnaDocs.add(DLNADoc.valueOf(txt));
                } catch (InvalidValueException ex) {
                    if (log.isLoggable(Level.INFO))
                        log.info("Invalid X_DLNADOC value, ignoring value: " + txt);
                }
            } else if (ELEMENT.X_DLNACAP.equals(deviceNodeChild) &&
                Descriptor.Device.DLNA_PREFIX.equals(deviceNodeChild.getPrefix())) {
                descriptor.dlnaCaps = DLNACaps.valueOf(XMLUtil.getTextContent(deviceNodeChild));
            }
        }
    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateIconList(MutableDevice<D, S> descriptor, Node iconListNode) throws DescriptorBindingException {

        NodeList iconListNodeChildren = iconListNode.getChildNodes();
        for (int i = 0; i < iconListNodeChildren.getLength(); i++) {
            Node iconListNodeChild = iconListNodeChildren.item(i);

            if (iconListNodeChild.getNodeType() != Node.ELEMENT_NODE)
                continue;

            if (ELEMENT.icon.equals(iconListNodeChild)) {

                MutableIcon icon = new MutableIcon();

                NodeList iconChildren = iconListNodeChild.getChildNodes();

                for (int x = 0; x < iconChildren.getLength(); x++) {
                    Node iconChild = iconChildren.item(x);

                    if (iconChild.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    if (ELEMENT.width.equals(iconChild)) {
                        icon.width = (Integer.parseInt(XMLUtil.getTextContent(iconChild)));
                    } else if (ELEMENT.height.equals(iconChild)) {
                        icon.height = (Integer.parseInt(XMLUtil.getTextContent(iconChild)));
                    } else if (ELEMENT.depth.equals(iconChild)) {
                        String depth = XMLUtil.getTextContent(iconChild);
                        try {
                            icon.depth = (Integer.parseInt(depth));
                       	} catch(NumberFormatException ex) {
                            if (log.isLoggable(Level.WARNING))
                       		    log.warning("Invalid icon depth '" + depth + "', using 16 as default: " + ex);
                       		icon.depth = 16;
                       	}
                    } else if (ELEMENT.url.equals(iconChild)) {
                        icon.uri = parseURI(XMLUtil.getTextContent(iconChild));
                    } else if (ELEMENT.mimetype.equals(iconChild)) {
                        try {
                            icon.mimeType = XMLUtil.getTextContent(iconChild);
                            MimeType.valueOf(icon.mimeType);
                        } catch(IllegalArgumentException ex) {
                            if (log.isLoggable(Level.WARNING))
                                log.warning("Ignoring invalid icon mime type: " + icon.mimeType);
                            icon.mimeType = "";
                        }
                    }

                }

                descriptor.icons.add(icon);
            }
        }
    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateServiceList(MutableDevice<D, S> descriptor, Node serviceListNode) {

        NodeList serviceListNodeChildren = serviceListNode.getChildNodes();
        for (int i = 0; i < serviceListNodeChildren.getLength(); i++) {
            Node serviceListNodeChild = serviceListNodeChildren.item(i);

            if (serviceListNodeChild.getNodeType() != Node.ELEMENT_NODE)
                continue;

            if (ELEMENT.service.equals(serviceListNodeChild)) {

                NodeList serviceChildren = serviceListNodeChild.getChildNodes();

                try {
                    MutableService<D, S> service = new MutableService<>();

                    for (int x = 0; x < serviceChildren.getLength(); x++) {
                        Node serviceChild = serviceChildren.item(x);

                        if (serviceChild.getNodeType() != Node.ELEMENT_NODE)
                            continue;

                        if (ELEMENT.serviceType.equals(serviceChild)) {
                            service.serviceType = (ServiceType.valueOf(XMLUtil.getTextContent(serviceChild)));
                        } else if (ELEMENT.serviceId.equals(serviceChild)) {
                            service.serviceId = (ServiceId.valueOf(XMLUtil.getTextContent(serviceChild)));
                        } else if (ELEMENT.SCPDURL.equals(serviceChild)) {
                            service.descriptorURI = parseURI(XMLUtil.getTextContent(serviceChild));
                        } else if (ELEMENT.controlURL.equals(serviceChild)) {
                            service.controlURI = parseURI(XMLUtil.getTextContent(serviceChild));
                        } else if (ELEMENT.eventSubURL.equals(serviceChild)) {
                            service.eventSubscriptionURI = parseURI(XMLUtil.getTextContent(serviceChild));
                        }

                    }

                    descriptor.services.add(service);
                } catch (InvalidValueException ex) {
                    if (log.isLoggable(Level.WARNING))
                        log.warning(
                            "UPnP specification violation, skipping invalid service declaration. " + ex.getMessage()
                        );
                }
            }
        }
    }

    public <D extends Device<?, D, S>, S extends Service<?, D, S>> void hydrateDeviceList(MutableDevice<D, S> descriptor, Node deviceListNode) throws DescriptorBindingException {

        NodeList deviceListNodeChildren = deviceListNode.getChildNodes();
        for (int i = 0; i < deviceListNodeChildren.getLength(); i++) {
            Node deviceListNodeChild = deviceListNodeChildren.item(i);

            if (deviceListNodeChild.getNodeType() != Node.ELEMENT_NODE)
                continue;

            if (ELEMENT.device.equals(deviceListNodeChild)) {
                MutableDevice<D, S> embeddedDevice = new MutableDevice<>();
                embeddedDevice.parentDevice = descriptor;
                descriptor.embeddedDevices.add(embeddedDevice);
                hydrateDevice(embeddedDevice, deviceListNodeChild);
            }
        }

    }

    @Override
    public String generate(Device<?, ?, ?> deviceModel, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException {
        try {
            if (log.isLoggable(Level.FINE))
                log.fine("Generating XML descriptor from device model: " + deviceModel);

            return XMLUtil.documentToString(buildDOM(deviceModel, info, namespace));

        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not build DOM: " + ex.getMessage(), ex);
        }
    }
    @Override
    public Document buildDOM(Device<?, ?, ?> deviceModel, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException {

        try {
            if (log.isLoggable(Level.FINE))
                log.fine("Generating DOM from device model: " + deviceModel);

            DocumentBuilderFactory factory = DocumentBuilderFactoryWithNonDTD.newDocumentBuilderFactoryWithNonDTDInstance();
            factory.setNamespaceAware(true);

            Document d = factory.newDocumentBuilder().newDocument();
            generateRoot(namespace, deviceModel, d, info);

            return d;

        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not generate device descriptor: " + ex.getMessage(), ex);
        }
    }

    protected void generateRoot(Namespace namespace, Device<?, ?, ?> deviceModel, Document descriptor, RemoteClientInfo info) {

        Element rootElement = descriptor.createElementNS(Descriptor.Device.NAMESPACE_URI, ELEMENT.root.toString());
        descriptor.appendChild(rootElement);

        generateSpecVersion(namespace, deviceModel, descriptor, rootElement);

        /* UDA 1.1 spec says: Don't use URLBase anymore
        if (deviceModel.getBaseURL() != null) {
            appendChildElementWithTextContent(descriptor, rootElement, "URLBase", deviceModel.getBaseURL());
        }
        */

        generateDevice(namespace, deviceModel, descriptor, rootElement, info);
    }

    protected void generateSpecVersion(Namespace namespace, Device<?, ?, ?> deviceModel, Document descriptor, Element rootElement) {
        Element specVersionElement = appendNewElement(descriptor, rootElement, ELEMENT.specVersion);
        appendNewElementIfNotNull(descriptor, specVersionElement, ELEMENT.major, deviceModel.getVersion().getMajor());
        appendNewElementIfNotNull(descriptor, specVersionElement, ELEMENT.minor, deviceModel.getVersion().getMinor());
    }

    protected void generateDevice(Namespace namespace, Device<?, ?, ?> deviceModel, Document descriptor, Element rootElement, RemoteClientInfo info) {

        Element deviceElement = appendNewElement(descriptor, rootElement, ELEMENT.device);

        appendNewElementIfNotNull(descriptor, deviceElement, ELEMENT.deviceType, deviceModel.getType());

        DeviceDetails deviceModelDetails = deviceModel.getDetails(info);
        appendNewElementIfNotNull(
                descriptor, deviceElement, ELEMENT.friendlyName,
                deviceModelDetails.getFriendlyName()
        );
        if (deviceModelDetails.getManufacturerDetails() != null) {
            appendNewElementIfNotNull(
                    descriptor, deviceElement, ELEMENT.manufacturer,
                    deviceModelDetails.getManufacturerDetails().getManufacturer()
            );
            appendNewElementIfNotNull(
                    descriptor, deviceElement, ELEMENT.manufacturerURL,
                    deviceModelDetails.getManufacturerDetails().getManufacturerURI()
            );
        }
        if (deviceModelDetails.getModelDetails() != null) {
            appendNewElementIfNotNull(
                    descriptor, deviceElement, ELEMENT.modelDescription,
                    deviceModelDetails.getModelDetails().getModelDescription()
            );
            appendNewElementIfNotNull(
                    descriptor, deviceElement, ELEMENT.modelName,
                    deviceModelDetails.getModelDetails().getModelName()
            );
            appendNewElementIfNotNull(
                    descriptor, deviceElement, ELEMENT.modelNumber,
                    deviceModelDetails.getModelDetails().getModelNumber()
            );
            appendNewElementIfNotNull(
                    descriptor, deviceElement, ELEMENT.modelURL,
                    deviceModelDetails.getModelDetails().getModelURI()
            );
        }
        appendNewElementIfNotNull(
                descriptor, deviceElement, ELEMENT.serialNumber,
                deviceModelDetails.getSerialNumber()
        );
        appendNewElementIfNotNull(descriptor, deviceElement, ELEMENT.UDN, deviceModel.getIdentity().getUdn());
        appendNewElementIfNotNull(
                descriptor, deviceElement, ELEMENT.presentationURL,
                deviceModelDetails.getPresentationURI()
        );
        appendNewElementIfNotNull(
                descriptor, deviceElement, ELEMENT.UPC,
                deviceModelDetails.getUpc()
        );

        if (deviceModelDetails.getDlnaDocs() != null) {
            for (DLNADoc dlnaDoc : deviceModelDetails.getDlnaDocs()) {
                appendNewElementIfNotNull(
                        descriptor, deviceElement, Descriptor.Device.DLNA_PREFIX + ":" + ELEMENT.X_DLNADOC,
                        dlnaDoc, Descriptor.Device.DLNA_NAMESPACE_URI
                );
            }
        }
        appendNewElementIfNotNull(
                descriptor, deviceElement, Descriptor.Device.DLNA_PREFIX + ":" + ELEMENT.X_DLNACAP,
                deviceModelDetails.getDlnaCaps(), Descriptor.Device.DLNA_NAMESPACE_URI
        );
        
        appendNewElementIfNotNull(
                descriptor, deviceElement, Descriptor.Device.SEC_PREFIX + ":" + ELEMENT.ProductCap,
                deviceModelDetails.getSecProductCaps(), Descriptor.Device.SEC_NAMESPACE_URI
        );
        
        appendNewElementIfNotNull(
                descriptor, deviceElement, Descriptor.Device.SEC_PREFIX + ":" + ELEMENT.X_ProductCap,
                deviceModelDetails.getSecProductCaps(), Descriptor.Device.SEC_NAMESPACE_URI
        );

        generateIconList(namespace, deviceModel, descriptor, deviceElement);
        generateServiceList(namespace, deviceModel, descriptor, deviceElement);
        generateDeviceList(namespace, deviceModel, descriptor, deviceElement, info);
    }

    protected void generateIconList(Namespace namespace, Device<?, ?, ?> deviceModel, Document descriptor, Element deviceElement) {
        if (!deviceModel.hasIcons()) return;

        Element iconListElement = appendNewElement(descriptor, deviceElement, ELEMENT.iconList);

        for (Icon icon : deviceModel.getIcons()) {
            Element iconElement = appendNewElement(descriptor, iconListElement, ELEMENT.icon);

            appendNewElementIfNotNull(descriptor, iconElement, ELEMENT.mimetype, icon.getMimeType());
            appendNewElementIfNotNull(descriptor, iconElement, ELEMENT.width, icon.getWidth());
            appendNewElementIfNotNull(descriptor, iconElement, ELEMENT.height, icon.getHeight());
            appendNewElementIfNotNull(descriptor, iconElement, ELEMENT.depth, icon.getDepth());
            if (deviceModel instanceof RemoteDevice) {
            	appendNewElementIfNotNull(descriptor, iconElement, ELEMENT.url,  icon.getUri());
            } else if (deviceModel instanceof LocalDevice) {
            	appendNewElementIfNotNull(descriptor, iconElement, ELEMENT.url,  namespace.getIconPath(icon));
            }
        }
    }

    protected void generateServiceList(Namespace namespace, Device<?, ?, ?> deviceModel, Document descriptor, Element deviceElement) {
        if (!deviceModel.hasServices()) return;

        Element serviceListElement = appendNewElement(descriptor, deviceElement, ELEMENT.serviceList);

        for (Service<?, ?, ?> service : deviceModel.getServices()) {
            Element serviceElement = appendNewElement(descriptor, serviceListElement, ELEMENT.service);

            appendNewElementIfNotNull(descriptor, serviceElement, ELEMENT.serviceType, service.getServiceType());
            appendNewElementIfNotNull(descriptor, serviceElement, ELEMENT.serviceId, service.getServiceId());
            if (service instanceof RemoteService) {
                RemoteService rs = (RemoteService) service;
                appendNewElementIfNotNull(descriptor, serviceElement, ELEMENT.SCPDURL, rs.getDescriptorURI());
                appendNewElementIfNotNull(descriptor, serviceElement, ELEMENT.controlURL, rs.getControlURI());
                appendNewElementIfNotNull(descriptor, serviceElement, ELEMENT.eventSubURL, rs.getEventSubscriptionURI());
            } else if (service instanceof LocalService) {
                LocalService<?> ls = (LocalService<?>) service;
                appendNewElementIfNotNull(descriptor, serviceElement, ELEMENT.SCPDURL, namespace.getDescriptorPath(ls));
                appendNewElementIfNotNull(descriptor, serviceElement, ELEMENT.controlURL, namespace.getControlPath(ls));
                appendNewElementIfNotNull(descriptor, serviceElement, ELEMENT.eventSubURL, namespace.getEventSubscriptionPath(ls));
            }
        }
    }

    protected void generateDeviceList(Namespace namespace, Device<?, ?, ?> deviceModel, Document descriptor, Element deviceElement, RemoteClientInfo info) {
        if (!deviceModel.hasEmbeddedDevices()) return;

        Element deviceListElement = appendNewElement(descriptor, deviceElement, ELEMENT.deviceList);

        for (Device<?, ?, ?> device : deviceModel.getEmbeddedDevices()) {
            generateDevice(namespace, device, descriptor, deviceListElement, info);
        }
    }
    @Override
    public void warning(SAXParseException e) throws SAXException {
        if (log.isLoggable(Level.WARNING))
            log.warning(e.toString());
    }
    @Override
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
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
            if (log.isLoggable(Level.FINE))
                log.fine("Illegal URI, trying with ./ prefix: " + Exceptions.unwrap(ex));
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
            if (log.isLoggable(Level.WARNING))
                log.warning("Illegal URI '" + uri + "', ignoring value: " + Exceptions.unwrap(ex));
            // Ignore
        }
        return null;
    }

}
