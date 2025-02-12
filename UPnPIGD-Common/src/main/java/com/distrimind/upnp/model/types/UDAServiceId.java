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

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;
import java.util.regex.Matcher;

/**
 * Service identifier with a fixed <code>upnp-org</code> namespace.
 * <p>
 * Also accepts the namespace sometimes used by broken devices, <code>schemas-upnp-org</code>.
 * </p>
 *
 * @author Christian Bauer
 */
public class UDAServiceId extends ServiceId {
	
	final private static DMLogger log = Log.getLogger(UDAServiceId.class);

    public static final String DEFAULT_NAMESPACE = "upnp-org";
    public static final String BROKEN_DEFAULT_NAMESPACE = "schemas-upnp-org"; // TODO: UPNP VIOLATION: Intel UPnP tools!



    public UDAServiceId(String id) {
        super(DEFAULT_NAMESPACE, id);
    }

    public static UDAServiceId valueOf(String s) throws InvalidValueException {
        Matcher matcher = Constants.getPatternUDAServiceID().matcher(s);
        if (matcher.matches() && matcher.groupCount() >= 1) {
            return new UDAServiceId(matcher.group(1));
        }

        matcher = Constants.getPatternBrokenUDAServiceID().matcher(s);
        if (matcher.matches() && matcher.groupCount() >= 1) {
            return new UDAServiceId(matcher.group(1));
        }

        // TODO: UPNP VIOLATION: Handle garbage sent by Eyecon Android app
        matcher = Constants.getPatternEyeconAndroidApp().matcher(s);
        if (matcher.matches()) {
            if (log.isWarnEnabled()) log.warn("UPnP specification violation, recovering from Eyecon garbage: " + s);
            return new UDAServiceId(matcher.group(1));
        }

        // Some devices just set the last token of the Service ID, e.g. 'ContentDirectory'
        if("ContentDirectory".equals(s) ||
           "ConnectionManager".equals(s) ||
           "RenderingControl".equals(s) ||
           "AVTransport".equals(s)) {
            if (log.isWarnEnabled()) log.warn("UPnP specification violation, fixing broken Service ID: " + s);
            return new UDAServiceId(s);
        }

        throw new InvalidValueException("Can't parse UDA service ID string (upnp-org/id): " + s);
    }

}
