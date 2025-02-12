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

package com.distrimind.upnp.model.types;

import com.distrimind.upnp.model.Constants;

import java.util.regex.Matcher;

/**
 * Device type with a fixed <code>schemas-upnp-org</code> namespace.
 *
 * @author Christian Bauer
 */
public class UDADeviceType extends DeviceType {

    public static final String DEFAULT_NAMESPACE = "schemas-upnp-org";




    public UDADeviceType(String type) {
        super(DEFAULT_NAMESPACE, type, 1);
    }

    public UDADeviceType(String type, int version) {
        super(DEFAULT_NAMESPACE, type, version);
    }

    public static UDADeviceType valueOf(String s) throws InvalidValueException {
        Matcher matcher = Constants.getPatternUDADeviceType().matcher(s);
        
        try {
        	if (matcher.matches())
        		return new UDADeviceType(matcher.group(1), Integer.parseInt(matcher.group(2)));
        } catch(RuntimeException e) {
        	throw new InvalidValueException(String.format(
                "Can't parse UDA device type string (namespace/type/version) '%s': %s", s, e
            ));
        }
        throw new InvalidValueException("Can't parse UDA device type string (namespace/type/version): " + s);
    }

}
