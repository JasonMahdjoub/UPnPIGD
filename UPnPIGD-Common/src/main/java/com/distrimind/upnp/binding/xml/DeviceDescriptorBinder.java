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

import com.distrimind.flexilogxml.xml.IXmlReader;
import com.distrimind.upnp.model.Namespace;
import com.distrimind.upnp.model.ValidationException;
import com.distrimind.upnp.model.meta.Device;
import com.distrimind.upnp.model.meta.Service;
import com.distrimind.upnp.model.profile.RemoteClientInfo;

/**
 * Reads and generates device descriptor XML metadata.
 *
 * @author Christian Bauer
 */
public interface DeviceDescriptorBinder {

    <D extends Device<?, D, S>, S extends Service<?, D, S>> D describe(D undescribedDevice, String descriptorXml)
            throws DescriptorBindingException, ValidationException;

    <D extends Device<?, D, S>, S extends Service<?, D, S>> D describe(D undescribedDevice, IXmlReader xmlReader)
            throws DescriptorBindingException, ValidationException;

    String generate(Device<?, ?, ?> device, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException;

    String buildXMLString(Device<?, ?, ?> device, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException;

}
