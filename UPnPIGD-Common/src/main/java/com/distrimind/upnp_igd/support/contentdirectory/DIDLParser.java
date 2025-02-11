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

package com.distrimind.upnp_igd.support.contentdirectory;

import com.distrimind.upnp_igd.model.XMLUtil;
import com.distrimind.upnp_igd.Log;
import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.InvalidValueException;
import com.distrimind.upnp_igd.support.model.DIDLAttribute;
import com.distrimind.upnp_igd.support.model.DIDLContent;
import com.distrimind.upnp_igd.support.model.DIDLObject;
import com.distrimind.upnp_igd.support.model.DescMeta;
import com.distrimind.upnp_igd.support.model.Person;
import com.distrimind.upnp_igd.support.model.PersonWithRole;
import com.distrimind.upnp_igd.support.model.ProtocolInfo;
import com.distrimind.upnp_igd.support.model.Res;
import com.distrimind.upnp_igd.support.model.StorageMedium;
import com.distrimind.upnp_igd.support.model.WriteStatus;
import com.distrimind.upnp_igd.support.model.container.Container;
import com.distrimind.upnp_igd.support.model.item.Item;
import com.distrimind.upnp_igd.util.io.IO;
import com.distrimind.upnp_igd.util.Exceptions;
import com.distrimind.upnp_igd.xml.SAXParser;
import com.distrimind.flexilogxml.exceptions.XMLStreamException;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.flexilogxml.xml.IXmlWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;


import static com.distrimind.upnp_igd.model.XMLUtil.appendNewElement;
import static com.distrimind.upnp_igd.model.XMLUtil.appendNewElementIfNotNull;

/**
 * DIDL parser based on SAX for reading and DOM for writing.
 * <p>
 * This parser requires Android platform level 8 (2.2).
 * </p>
 * <p>
 * Override the {@link #createDescMetaHandler(com.distrimind.upnp_igd.support.model.DescMeta, Handler)}
 * method to read vendor extension content of {@code <desc>} elements. You then should also override the
 * {@link #populateDescMetadata(IXmlWriter, com.distrimind.upnp_igd.support.model.DescMeta)} method for writing.
 * </p>
 * <p>
 * Override the {@link #createItemHandler(com.distrimind.upnp_igd.support.model.item.Item, Handler)}
 * etc. methods to register custom handlers for vendor-specific elements and attributes within items, containers,
 * and so on.
 * </p>
 *
 * @author Christian Bauer
 * @author Mario Franco
 * @author Jason Mahdjoub, use XML Parser instead of Document
 */
public class DIDLParser extends SAXParser {

    final private static DMLogger log = Log.getLogger(DIDLParser.class);

    public static final String UNKNOWN_TITLE = "Unknown Title";
    public static final String RESTRICTED = "restricted";
    public static final String ITEM = "item";
    public static final String RES = "res";
    public static final String DESC = "desc";
    public static final String NAME = "name";
    public static final String PARENT_ID = "parentID";

    /**
     * Uses the current thread's context classloader to read and unmarshall the given resource.
     *
     * @param resource The resource on the classpath.
     * @return The unmarshalled DIDL content model.
     *
     */
    public DIDLContent parseResource(String resource) throws Exception {
		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
			return parse(IO.readLines(is));
		}
    }

    /**
     * Reads and unmarshalls an XML representation into a DIDL content model.
     *
     * @param xml The XML representation.
     * @return A DIDL content model.
	 */
    public DIDLContent parse(String xml) throws Exception {

        if (xml == null || xml.isEmpty()) {
            throw new RuntimeException("Null or empty XML");
        }

        DIDLContent content = new DIDLContent();
        createRootHandler(content, this);

        log.debug("Parsing DIDL XML content");
        parse(new InputSource(new StringReader(xml)));
        return content;
    }

    protected RootHandler createRootHandler(DIDLContent instance, SAXParser parser) {
        return new RootHandler(instance, parser);
    }

    protected ContainerHandler createContainerHandler(Container instance, Handler<?> parent) {
        return new ContainerHandler(instance, parent);
    }

    protected ItemHandler createItemHandler(Item instance, Handler<?> parent) {
        return new ItemHandler(instance, parent);
    }

    protected ResHandler createResHandler(Res instance, Handler<?> parent) {
        return new ResHandler(instance, parent);
    }

    protected DescMetaHandler createDescMetaHandler(DescMeta<XMLUtil.XMLCompleters> instance, Handler<?> parent) {
        return new DescMetaHandler(instance, parent);
    }


    protected Container createContainer(Attributes attributes) {
        Container container = new Container();

        container.setId(attributes.getValue("id"));
        container.setParentID(attributes.getValue(PARENT_ID));

        if ((attributes.getValue("childCount") != null))
            container.setChildCount(Integer.valueOf(attributes.getValue("childCount")));

        try {
            Boolean value = (Boolean) Datatype.Builtin.BOOLEAN.getDatatype().valueOf(
                attributes.getValue(RESTRICTED)
            );
            if (value != null)
                container.setRestricted(value);

            value = (Boolean) Datatype.Builtin.BOOLEAN.getDatatype().valueOf(
                attributes.getValue("searchable")
            );
            if (value != null)
                container.setSearchable(value);
        } catch (Exception ignored) {
            // Ignore
        }

        return container;
    }

    protected Item createItem(Attributes attributes) {
        Item item = new Item();

        item.setId(attributes.getValue("id"));
        item.setParentID(attributes.getValue(PARENT_ID));

        try {
            Boolean value = (Boolean)Datatype.Builtin.BOOLEAN.getDatatype().valueOf(
                    attributes.getValue(RESTRICTED)
            );
            if (value != null)
                item.setRestricted(value);

        } catch (Exception ignored) {
            // Ignore
        }

        if ((attributes.getValue("refID") != null))
            item.setRefID(attributes.getValue("refID"));

        return item;
    }

    protected Res createResource(Attributes attributes) {
        Res res = new Res();

        if (attributes.getValue("importUri") != null)
            res.setImportUri(URI.create(attributes.getValue("importUri")));

        try {
            res.setProtocolInfo(
                    new ProtocolInfo(attributes.getValue("protocolInfo"))
            );
        } catch (InvalidValueException ex) {
            if (log.isWarnEnabled()) log.warn("In DIDL content, invalid resource protocol info: ", Exceptions.unwrap(ex));
            return null;
        }

        if (attributes.getValue("size") != null)
            res.setSize(toLongOrNull(attributes.getValue("size")));

        if (attributes.getValue("duration") != null)
            res.setDuration(attributes.getValue("duration"));

        if (attributes.getValue("bitrate") != null)
            res.setBitrate(toLongOrNull(attributes.getValue("bitrate")));

        if (attributes.getValue("sampleFrequency") != null)
            res.setSampleFrequency(toLongOrNull(attributes.getValue("sampleFrequency")));

        if (attributes.getValue("bitsPerSample") != null)
            res.setBitsPerSample(toLongOrNull(attributes.getValue("bitsPerSample")));

        if (attributes.getValue("nrAudioChannels") != null)
            res.setNrAudioChannels(toLongOrNull(attributes.getValue("nrAudioChannels")));

        if (attributes.getValue("colorDepth") != null)
            res.setColorDepth(toLongOrNull(attributes.getValue("colorDepth")));

        if (attributes.getValue("protection") != null)
            res.setProtection(attributes.getValue("protection"));

        if (attributes.getValue("resolution") != null)
            res.setResolution(attributes.getValue("resolution"));

        return res;
    }

    private Long toLongOrNull(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException x) {
            return null;
        }
    }

    protected DescMeta<XMLUtil.XMLCompleters> createDescMeta(Attributes attributes) {
        DescMeta<XMLUtil.XMLCompleters> desc = new DescMeta<>();

        desc.setId(attributes.getValue("id"));

        if ((attributes.getValue("type") != null))
            desc.setType(attributes.getValue("type"));

        if ((attributes.getValue("nameSpace") != null))
            desc.setNameSpace(URI.create(attributes.getValue("nameSpace")));

        return desc;
    }


    /* ############################################################################################# */


    /**
     * Generates an XML representation of the content model.
     * <p>
     * Items inside a container will <em>not</em> be represented in the XML, the containers
     * will be rendered flat without children.
     *
     * @param content The content model.
     * @return An XML representation.
     *
     */
    public String generate(DIDLContent content) throws Exception {
        return generate(content, false);
    }

    /**
     * Generates an XML representation of the content model.
     * <p>
     * Optionally, items inside a container will be represented in the XML,
     * the container elements then have nested item elements. Although this
     * parser can read such a structure, it is unclear whether other DIDL
     * parsers should and actually do support this XML.
     *
     * @param content     The content model.
     * @param nestedItems <code>true</code> if nested item elements should be rendered for containers.
     * @return An XML representation.
     *
     */
    public String generate(DIDLContent content, boolean nestedItems) throws Exception {
        return buildXMLString(content, nestedItems);
    }

    protected String buildXMLString(DIDLContent content, boolean nestedItems) throws Exception {

        return XMLUtil.generateXMLToString(xmlStreamWriter -> generateRoot(content, xmlStreamWriter, nestedItems));
    }

    protected void generateRoot(DIDLContent content, IXmlWriter xmlWriter, boolean nestedItems) throws XMLStreamException {
        xmlWriter.writeStartElement(DIDLContent.NAMESPACE_URI, "DIDL-Lite");

        // rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:didl", DIDLContent.NAMESPACE_URI);
        xmlWriter.writeAttribute("http://www.w3.org/2000/xmlns/", "xmlns:upnp", DIDLObject.Property.UPNP.NAMESPACE.URI);
        xmlWriter.writeAttribute("http://www.w3.org/2000/xmlns/", "xmlns:dc", DIDLObject.Property.DC.NAMESPACE.URI);
        xmlWriter.writeAttribute("http://www.w3.org/2000/xmlns/", "xmlns:sec", DIDLObject.Property.SEC.NAMESPACE.URI);

        for (Container container : content.getContainers()) {
            if (container == null) continue;
            generateContainer(container, xmlWriter, nestedItems);
        }

        for (Item item : content.getItems()) {
            if (item == null) continue;
            generateItem(item, xmlWriter);
        }

        for (DescMeta<?> descMeta : content.getDescMetadata()) {
            if (descMeta == null) continue;
            generateDescMetadata(descMeta, xmlWriter);
        }
        xmlWriter.writeEndElement();
    }

    protected void generateContainer(Container container, IXmlWriter xmlWriter, boolean nestedItems) throws XMLStreamException {

        if (container.getClazz() == null) {
            throw new RuntimeException("Missing 'upnp:class' element for container: " + container.getId());
        }
        xmlWriter.writeStartElement("container");

        if (container.getId() == null)
            throw new NullPointerException("Missing id on container: " + container);
        xmlWriter.writeAttribute("id", container.getId());

        if (container.getParentID() == null)
            throw new NullPointerException("Missing parent id on container: " + container);
        xmlWriter.writeAttribute(PARENT_ID, container.getParentID());

        if (container.getChildCount() != null) {
            xmlWriter.writeAttribute("childCount", Integer.toString(container.getChildCount()));
        }

        xmlWriter.writeAttribute(RESTRICTED, booleanToInt(container.isRestricted()));
        xmlWriter.writeAttribute("searchable", booleanToInt(container.isSearchable()));

        String title = container.getTitle();
        if (title == null) {
            log.warn(() -> "Missing 'dc:title' element for container: " + container.getId());
            title = UNKNOWN_TITLE;
        }

        appendNewElementIfNotNull(
                xmlWriter,
                "dc:title",
                title,
                DIDLObject.Property.DC.NAMESPACE.URI
        );

        appendNewElementIfNotNull(
                xmlWriter,
                "dc:creator",
                container.getCreator(),
                DIDLObject.Property.DC.NAMESPACE.URI
        );

        appendNewElementIfNotNull(
                xmlWriter,
                "upnp:writeStatus",
                container.getWriteStatus(),
                DIDLObject.Property.UPNP.NAMESPACE.URI
        );

        appendClass(xmlWriter, container.getClazz(), "upnp:class", false);

        for (DIDLObject.Class searchClass : container.getSearchClasses()) {
            appendClass(xmlWriter, searchClass, "upnp:searchClass", true);
        }

        for (DIDLObject.Class createClass : container.getCreateClasses()) {
            appendClass(xmlWriter, createClass, "upnp:createClass", true);
        }

        appendProperties(xmlWriter, container, "upnp", DIDLObject.Property.UPNP.NAMESPACE.class, DIDLObject.Property.UPNP.NAMESPACE.URI);
        appendProperties(xmlWriter, container, "dc", DIDLObject.Property.DC.NAMESPACE.class, DIDLObject.Property.DC.NAMESPACE.URI);

        if (nestedItems) {
            for (Item item : container.getItems()) {
                if (item == null) continue;
                generateItem(item, xmlWriter);
            }
        }

        for (Res resource : container.getResources()) {
            if (resource == null) continue;
            generateResource(resource, xmlWriter);
        }

        for (DescMeta<?> descMeta : container.getDescMetadata()) {
            if (descMeta == null) continue;
            generateDescMetadata(descMeta, xmlWriter);
        }
        xmlWriter.writeEndElement();
    }

    protected void generateItem(Item item, IXmlWriter xmlWriter) throws XMLStreamException {

        if (item.getClazz() == null) {
            throw new RuntimeException("Missing 'upnp:class' element for item: " + item.getId());
        }
        xmlWriter.writeStartElement(ITEM);


        if (item.getId() == null)
            throw new NullPointerException("Missing id on item: " + item);
        xmlWriter.writeAttribute("id", item.getId());

        if (item.getParentID() == null)
            throw new NullPointerException("Missing parent id on item: " + item);
        xmlWriter.writeAttribute(PARENT_ID, item.getParentID());

        if (item.getRefID() != null)
            xmlWriter.writeAttribute("refID", item.getRefID());
        xmlWriter.writeAttribute(RESTRICTED, booleanToInt(item.isRestricted()));

        String title = item.getTitle();
        if (title == null) {
            log.warn(() -> "Missing 'dc:title' element for item: " + item.getId());
            title = UNKNOWN_TITLE;
        }

        appendNewElementIfNotNull(
                xmlWriter,
                "dc:title",
                title,
                DIDLObject.Property.DC.NAMESPACE.URI
        );

        appendNewElementIfNotNull(
                xmlWriter,
                "dc:creator",
                item.getCreator(),
                DIDLObject.Property.DC.NAMESPACE.URI
        );

        appendNewElementIfNotNull(
                xmlWriter,
                "upnp:writeStatus",
                item.getWriteStatus(),
                DIDLObject.Property.UPNP.NAMESPACE.URI
        );

        appendClass(xmlWriter, item.getClazz(), "upnp:class", false);

        appendProperties(xmlWriter, item, "upnp", DIDLObject.Property.UPNP.NAMESPACE.class, DIDLObject.Property.UPNP.NAMESPACE.URI);
        appendProperties(xmlWriter, item, "dc", DIDLObject.Property.DC.NAMESPACE.class, DIDLObject.Property.DC.NAMESPACE.URI);
        appendProperties(xmlWriter, item, "sec", DIDLObject.Property.SEC.NAMESPACE.class, DIDLObject.Property.SEC.NAMESPACE.URI);

        for (Res resource : item.getResources()) {
            if (resource == null) continue;
            generateResource(resource, xmlWriter);
        }

        for (DescMeta<?> descMeta : item.getDescMetadata()) {
            if (descMeta == null) continue;
            generateDescMetadata(descMeta, xmlWriter);
        }
        xmlWriter.writeEndElement();
    }

    protected void generateResource(Res resource, IXmlWriter xmlWriter) throws XMLStreamException {

        if (resource.getValue() == null) {
            throw new RuntimeException("Missing resource URI value" + resource);
        }
        if (resource.getProtocolInfo() == null) {
            throw new RuntimeException("Missing resource protocol info: " + resource);
        }

        appendNewElement(xmlWriter, RES, resource.getValue());
        xmlWriter.writeAttribute("protocolInfo", resource.getProtocolInfo().toString());
        if (resource.getImportUri() != null)
            xmlWriter.writeAttribute("importUri", resource.getImportUri().toString());
        if (resource.getSize() != null)
            xmlWriter.writeAttribute("size", resource.getSize().toString());
        if (resource.getDuration() != null)
            xmlWriter.writeAttribute("duration", resource.getDuration());
        if (resource.getBitrate() != null)
            xmlWriter.writeAttribute("bitrate", resource.getBitrate().toString());
        if (resource.getSampleFrequency() != null)
            xmlWriter.writeAttribute("sampleFrequency", resource.getSampleFrequency().toString());
        if (resource.getBitsPerSample() != null)
            xmlWriter.writeAttribute("bitsPerSample", resource.getBitsPerSample().toString());
        if (resource.getNrAudioChannels() != null)
            xmlWriter.writeAttribute("nrAudioChannels", resource.getNrAudioChannels().toString());
        if (resource.getColorDepth() != null)
            xmlWriter.writeAttribute("colorDepth", resource.getColorDepth().toString());
        if (resource.getProtection() != null)
            xmlWriter.writeAttribute("protection", resource.getProtection());
        if (resource.getResolution() != null)
            xmlWriter.writeAttribute("resolution", resource.getResolution());
    }

    protected void generateDescMetadata(DescMeta<?> descMeta, IXmlWriter xmlWriter) throws XMLStreamException {

        if (descMeta.getId() == null) {
            throw new RuntimeException("Missing id of description metadata: " + descMeta);
        }
        if (descMeta.getNameSpace() == null) {
            throw new RuntimeException("Missing namespace of description metadata: " + descMeta);
        }
        xmlWriter.writeStartElement(DESC);

        xmlWriter.writeAttribute("id", descMeta.getId());
        xmlWriter.writeAttribute("nameSpace", descMeta.getNameSpace().toString());
        if (descMeta.getType() != null)
            xmlWriter.writeAttribute("type", descMeta.getType());
        populateDescMetadata(xmlWriter, descMeta);
        xmlWriter.writeEndElement();
    }

    /**
     * Expects an <code>org.w3c.Document</code> as metadata, copies nodes of the document into the DIDL content.
     * <p>
     * This method will ignore the content and log a warning if it's of the wrong type. If you override
     * {@link #createDescMetaHandler(com.distrimind.upnp_igd.support.model.DescMeta, Handler)},
     * you most likely also want to override this method.

     *
     * @param xmlWriter the xml writer
     * @param descMeta    The metadata with a <code>org.w3c.Document</code> payload.
     */
    protected void populateDescMetadata(IXmlWriter xmlWriter, DescMeta<?> descMeta) throws XMLStreamException {

        if (descMeta.getMetadata() instanceof XMLUtil.XMLCompleters) {
            XMLUtil.XMLCompleters c = (XMLUtil.XMLCompleters) descMeta.getMetadata();
            c.write(xmlWriter);

        } else {
            log.warn(() -> "Unknown desc metadata content, please override populateDescMetadata(): " + descMeta.getMetadata());
        }
    }

    protected void appendProperties(IXmlWriter xmlWriter, DIDLObject object, String prefix,
                                    Class<? extends DIDLObject.Property.NAMESPACE> namespace,
                                    String namespaceURI) throws XMLStreamException {
        for (DIDLObject.Property<Object> property : object.getPropertiesByNamespace(namespace)) {
            xmlWriter.writeStartElement(prefix, property.getDescriptorName(), namespaceURI);
            property.setOnElement(xmlWriter);
            xmlWriter.writeEndElement();
        }
    }

    protected void appendClass(IXmlWriter xmlWriter, DIDLObject.Class clazz, String element, boolean appendDerivation) throws XMLStreamException {
        appendNewElementIfNotNull(
                xmlWriter,
                element,
                clazz.getValue(),
                DIDLObject.Property.UPNP.NAMESPACE.URI
        );
        if (clazz.getFriendlyName() != null && !clazz.getFriendlyName().isEmpty())
            xmlWriter.writeAttribute(NAME, clazz.getFriendlyName());
        if (appendDerivation)
            xmlWriter.writeAttribute("includeDerived", Boolean.toString(clazz.isIncludeDerived()));
    }

    protected String booleanToInt(boolean b) {
        return b ? "1" : "0";
    }

    /**
     * Sends the given string to the log with <code>Level.FINE</code>, if that log level is enabled.
     *
     * @param s The string to send to the log.
     */
    public void debugXML(String s) {
        if (log.isDebugEnabled()) {
            log.debug("-------------------------------------------------------------------------------------");
            log.debug("\n" + s);
            log.debug("-------------------------------------------------------------------------------------");
        }
    }


    /* ############################################################################################# */


    public abstract static class DIDLObjectHandler<I extends DIDLObject> extends Handler<I> {

        protected DIDLObjectHandler(I instance, Handler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName==null)
                return;
            if (uri==null)
                return;
            switch (uri)
            {
                case DIDLObject.Property.DC.NAMESPACE.URI:
                    switch (localName) {
                        case "title":
                            getInstance().setTitle(getCharacters());
                            break;
                        case "creator":
                            getInstance().setCreator(getCharacters());
                            break;
                        case "description":
                            getInstance().addProperty(new DIDLObject.Property.DC.DESCRIPTION(getCharacters()));
                            break;
                        case "publisher":
                            getInstance().addProperty(new DIDLObject.Property.DC.PUBLISHER(new Person(getCharacters())));
                            break;
                        case "contributor":
                            getInstance().addProperty(new DIDLObject.Property.DC.CONTRIBUTOR(new Person(getCharacters())));
                            break;
                        case "date":
                            getInstance().addProperty(new DIDLObject.Property.DC.DATE(getCharacters()));
                            break;
                        case "language":
                            getInstance().addProperty(new DIDLObject.Property.DC.LANGUAGE(getCharacters()));
                            break;
                        case "rights":
                            getInstance().addProperty(new DIDLObject.Property.DC.RIGHTS(getCharacters()));
                            break;
                        case "relation":
                            getInstance().addProperty(new DIDLObject.Property.DC.RELATION(URI.create(getCharacters())));
                            break;
                        default:
                            break;

                    }
                    break;
                case DIDLObject.Property.UPNP.NAMESPACE.URI:
                    switch (localName)
                    {
                        case "writeStatus":
                            try {
                                getInstance().setWriteStatus(
                                        WriteStatus.valueOf(getCharacters())
                                );
                            } catch (Exception ex) {
                                if (log.isInfoEnabled()) log.info("Ignoring invalid writeStatus value: " + getCharacters());
                            }
                            break;
                        case "class":
                            getInstance().setClazz(
                                    new DIDLObject.Class(
                                            getCharacters(),
                                            getAttributes().getValue(NAME)
                                    )
                            );
                            break;
                        case "artist":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.ARTIST(
                                            new PersonWithRole(getCharacters(), getAttributes().getValue("role"))
                                    )
                            );
                            break;
                        case "actor":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.ACTOR(
                                            new PersonWithRole(getCharacters(), getAttributes().getValue("role"))
                                    )
                            );
                            break;
                        case "author":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.AUTHOR(
                                            new PersonWithRole(getCharacters(), getAttributes().getValue("role"))
                                    )
                            );
                            break;
                        case "producer":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.PRODUCER(new Person(getCharacters()))
                            );
                            break;
                        case "director":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.DIRECTOR(new Person(getCharacters()))
                            );
                            break;
                        case "longDescription":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.LONG_DESCRIPTION(getCharacters())
                            );
                            break;
                        case "storageUsed":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.STORAGE_USED(Long.valueOf(getCharacters()))
                            );
                            break;
                        case "storageTotal":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.STORAGE_TOTAL(Long.valueOf(getCharacters()))
                            );
                            break;
                        case "storageFree":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.STORAGE_FREE(Long.valueOf(getCharacters()))
                            );
                            break;
                        case "storageMaxPartition":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.STORAGE_MAX_PARTITION(Long.valueOf(getCharacters()))
                            );
                            break;
                        case "storageMedium":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.STORAGE_MEDIUM(StorageMedium.valueOrVendorSpecificOf(getCharacters()))
                            );
                            break;
                        case "genre":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.GENRE(getCharacters())
                            );
                            break;
                        case "album":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.ALBUM(getCharacters())
                            );
                            break;
                        case "playlist":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.PLAYLIST(getCharacters())
                            );
                            break;
                        case "region":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.REGION(getCharacters())
                            );
                            break;
                        case "rating":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.RATING(getCharacters())
                            );
                            break;
                        case "toc":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.TOC(getCharacters())
                            );
                            break;
                        case "albumArtURI":
                        {
                            DIDLObject.Property<URI> albumArtURI = new DIDLObject.Property.UPNP.ALBUM_ART_URI(URI.create(getCharacters()));

                            Attributes albumArtURIAttributes = getAttributes();
                            for (int i = 0; i < albumArtURIAttributes.getLength(); i++) {
                                if ("profileID".equals(albumArtURIAttributes.getLocalName(i))) {
                                    albumArtURI.addAttribute(
                                            new DIDLObject.Property.DLNA.PROFILE_ID(
                                                    new DIDLAttribute(
                                                            DIDLObject.Property.DLNA.NAMESPACE.URI,
                                                            "dlna",
                                                            albumArtURIAttributes.getValue(i))
                                            ));
                                }
                            }

                            getInstance().addProperty(albumArtURI);
                        }
                        break;
                        case "artistDiscographyURI":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.ARTIST_DISCO_URI(URI.create(getCharacters()))
                            );
                            break;
                        case "lyricsURI":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.LYRICS_URI(URI.create(getCharacters()))
                            );
                            break;
                        case "icon":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.ICON(URI.create(getCharacters()))
                            );
                            break;
                        case "radioCallSign":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.RADIO_CALL_SIGN(getCharacters())
                            );
                            break;
                        case "radioStationID":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.RADIO_STATION_ID(getCharacters())
                            );
                            break;
                        case "radioBand":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.RADIO_BAND(getCharacters())
                            );
                            break;
                        case "channelNr":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.CHANNEL_NR(Integer.valueOf(getCharacters()))
                            );
                            break;
                        case "channelName":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.CHANNEL_NAME(getCharacters())
                            );
                            break;
                        case "scheduledStartTime":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.SCHEDULED_START_TIME(getCharacters())
                            );
                            break;
                        case "scheduledEndTime":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.SCHEDULED_END_TIME(getCharacters())
                            );
                            break;
                        case "DVDRegionCode":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.DVD_REGION_CODE(Integer.valueOf(getCharacters()))
                            );
                            break;
                        case "originalTrackNumber":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.ORIGINAL_TRACK_NUMBER(Integer.valueOf(getCharacters()))
                            );
                            break;
                        case "userAnnotation":
                            getInstance().addProperty(
                                    new DIDLObject.Property.UPNP.USER_ANNOTATION(getCharacters())
                            );
                            break;
                        default:
                            break;


                    }
                    break;
                default:
                    break;
            }

        }
    }

    public class RootHandler extends Handler<DIDLContent> {

        RootHandler(DIDLContent instance, SAXParser parser) {
            super(instance, parser);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (!DIDLContent.NAMESPACE_URI.equals(uri)) return;

			switch (localName) {
				case "container":

					Container container = createContainer(attributes);
					getInstance().addContainer(container);
					createContainerHandler(container, this);

					break;
				case ITEM:

					Item item = createItem(attributes);
					getInstance().addItem(item);
					createItemHandler(item, this);

					break;
				case DESC:

					DescMeta<XMLUtil.XMLCompleters> desc = createDescMeta(attributes);
					getInstance().addDescMetadata(desc);
					createDescMetaHandler(desc, this);

					break;
			}
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            if (DIDLContent.NAMESPACE_URI.equals(uri) && "DIDL-Lite".equals(localName)) {

                // Now transform all the generically typed Container and Item instances into
                // more specific Album, MusicTrack, etc. instances
                getInstance().replaceGenericContainerAndItems();

                return true;
            }
            return false;
        }
    }

    public class ContainerHandler extends DIDLObjectHandler<Container> {
        public ContainerHandler(Container instance, Handler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (!DIDLContent.NAMESPACE_URI.equals(uri)) return;

			switch (localName) {
				case ITEM:

					Item item = createItem(attributes);
					getInstance().addItem(item);
					createItemHandler(item, this);

					break;
				case DESC:

					DescMeta<XMLUtil.XMLCompleters> desc = createDescMeta(attributes);
					getInstance().addDescMetadata(desc);
					createDescMetaHandler(desc, this);

					break;
				case RES:

					Res res = createResource(attributes);
					if (res != null) {
						getInstance().addResource(res);
						createResHandler(res, this);
					}

					break;
			}

            // We do NOT support recursive container embedded in container! The schema allows it
            // but the spec doesn't:
            //
            // Section 2.8.3: Incremental navigation i.e. the full hierarchy is never returned
            // in one call since this is likely to flood the resources available to the control
            // point (memory, network bandwidth, etc.).
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName==null)
                return;
            if (DIDLObject.Property.UPNP.NAMESPACE.URI.equals(uri)) {
                switch (localName)
                {
                    case "searchClass":
                        getInstance().getSearchClasses().add(
                                new DIDLObject.Class(
                                        getCharacters(),
                                        getAttributes().getValue(NAME),
                                        "true".equals(getAttributes().getValue("includeDerived"))
                                )
                        );
                        break;
                    case "createClass":
                        getInstance().getCreateClasses().add(
                                new DIDLObject.Class(
                                        getCharacters(),
                                        getAttributes().getValue(NAME),
                                        "true".equals(getAttributes().getValue("includeDerived"))
                                )
                        );
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            if (DIDLContent.NAMESPACE_URI.equals(uri) && "container".equals(localName)) {
                if (getInstance().getTitle() == null) {
                    if (log.isWarnEnabled()) log.warn("In DIDL content, missing 'dc:title' element for container: " + getInstance().getId());
                }
                if (getInstance().getClazz() == null) {
                    if (log.isWarnEnabled()) log.warn("In DIDL content, missing 'upnp:class' element for container: " + getInstance().getId());
                }
                return true;
            }
            return false;
        }
    }

    public class ItemHandler extends DIDLObjectHandler<Item> {
        public ItemHandler(Item instance, Handler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (!DIDLContent.NAMESPACE_URI.equals(uri)) return;
            if (localName==null)
                return;
            switch (localName)
            {
                case RES:
                {
                    Res res = createResource(attributes);
                    if (res != null) {
                        getInstance().addResource(res);
                        createResHandler(res, this);
                    }
                }
                    break;
                case DESC:
                {
                    DescMeta<XMLUtil.XMLCompleters> desc = createDescMeta(attributes);
                    getInstance().addDescMetadata(desc);
                    createDescMetaHandler(desc, this);
                }
                    break;
                default:
                    break;
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            if (DIDLContent.NAMESPACE_URI.equals(uri) && ITEM.equals(localName)) {
                if (getInstance().getTitle() == null) {
                    if (log.isWarnEnabled()) log.warn("In DIDL content, missing 'dc:title' element for item: " + getInstance().getId());
                }
                if (getInstance().getClazz() == null) {
                    if (log.isWarnEnabled()) log.warn("In DIDL content, missing 'upnp:class' element for item: " + getInstance().getId());
                }
                return true;
            }
            return false;
        }
    }

    protected static class ResHandler extends Handler<Res> {
        public ResHandler(Res instance, Handler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            getInstance().setValue(getCharacters());
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return DIDLContent.NAMESPACE_URI.equals(uri) && RES.equals(localName);
        }
    }

    /**
     * Extracts an <code>org.w3c.Document</code> from the nested elements in the {@code <desc>} element.
     * <p>
     * The root element of this document is a wrapper in the namespace
     * {@link com.distrimind.upnp_igd.support.model.DIDLContent#DESC_WRAPPER_NAMESPACE_URI}.
     */
    public static class DescMetaHandler extends Handler<DescMeta<XMLUtil.XMLCompleters>> {


        public DescMetaHandler(DescMeta<XMLUtil.XMLCompleters> instance, Handler<?> parent) {
            super(instance, parent);
            instance.setMetadata(instance.createXMLCompleters());
        }

        @Override
        public DescMeta<XMLUtil.XMLCompleters> getInstance() {
            return super.getInstance();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            getInstance().getMetadata().add(xmlWriter -> {
                xmlWriter.writeStartElement(uri, qName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    xmlWriter.writeAttribute(
                            attributes.getURI(i),
                            attributes.getQName(i),
                            attributes.getValue(i)
                    );
                }

            });

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            getInstance().getMetadata().add(xmlWriter -> {
                if (!isLastElement(uri, localName, qName)) {

                    // Ignore whitespace
                    if (!getCharacters().isEmpty() && !getCharacters().matches("[\\t\\n\\x0B\\f\\r\\s]+")) {
                        xmlWriter.writeCharacters(getCharacters());
                    }
                    // Reset this so we can continue parsing child nodes with this handler
                    chars = new StringBuilder();

                    attributes = null;
                }
                xmlWriter.writeEndElement();
            });
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return DIDLContent.NAMESPACE_URI.equals(uri) && DESC.equals(localName);
        }
    }
}
