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

package com.distrimind.upnp_igd.support.lastchange;

import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.InvalidValueException;
import com.distrimind.upnp_igd.util.Exceptions;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class EventedValueURI extends EventedValue<URI> {

    final private static Logger log = Logger.getLogger(EventedValueURI.class.getName());

    public EventedValueURI(URI value) {
        super(value);
    }

    public EventedValueURI(Collection<Map.Entry<String, String>> attributes) {
        super(attributes);
    }
    
    @Override
    protected URI valueOf(String s) throws InvalidValueException {
        try {
            // These URIs are really defined as 'string' datatype in AVTransport1.0.pdf, but we can try
            // to parse whatever devices give us, like the Roku which sends "unknown url".
            return super.valueOf(s);
        } catch (InvalidValueException ex) {
            if (log.isLoggable(Level.INFO)) log.info("Ignoring invalid URI in evented value '" + s +"': " + Exceptions.unwrap(ex));
            return null;
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    protected Datatype<URI> getDatatype() {
        return (Datatype<URI>)Datatype.Builtin.URI.getDatatype();
    }
}