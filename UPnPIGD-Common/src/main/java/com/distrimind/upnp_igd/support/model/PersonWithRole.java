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

package com.distrimind.upnp_igd.support.model;

import com.distrimind.flexilogxml.exceptions.XMLStreamException;
import com.distrimind.flexilogxml.xml.IXmlWriter;

/**
 * @author Christian Bauer
 * @author Jason Mahdjoub, use XML Parser instead of Document
 */
public class PersonWithRole extends Person {

    private String role;

    public PersonWithRole(String name) {
        super(name);
    }

    public PersonWithRole(String name, String role) {
        super(name);
        this.role = role;
    }

    public String getRole() {
        return role;
    }
    public void setOnElement(IXmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeCharacters(toString());
        if(getRole() != null) {
            xmlWriter.writeAttribute("role", getRole());
        }
    }
}
