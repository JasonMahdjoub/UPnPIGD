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
package com.distrimind.upnp.support.model.dlna.message.header;

import com.distrimind.upnp.model.message.header.InvalidHeaderException;

/**
 * @author Mario Franco
 */
public class SupportedHeader extends DLNAHeader<String[]> {
    
    public SupportedHeader() {
        setValue(new String[]{});
    }

    @Override
    public void setString(String _s) throws InvalidHeaderException {
        String s=_s;
        if (!s.isEmpty()) {
            if (s.endsWith(";"))
                s = s.substring(0, s.length()-1);
            setValue(s.split("\\s*,\\s*"));
            return;
        }
        throw new InvalidHeaderException("Invalid Supported header value: " + s);
    }

    @Override
    public String getString() {
        String[] v = getValue();
        StringBuilder r = new StringBuilder(v.length > 0 ? v[0] : "");
        for (int i = 1; i < v.length; i++) {
            r.append(",").append(v[i]);
        }
        return r.toString();
    }
}
