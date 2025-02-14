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
import com.distrimind.upnp.model.types.BytesRange;
import com.distrimind.upnp.model.types.InvalidValueException;
import com.distrimind.upnp.support.model.dlna.types.NormalPlayTimeRange;
import com.distrimind.upnp.support.model.dlna.types.TimeSeekRangeType;

/**
 * @author Mario Franco
 */
public class TimeSeekRangeHeader extends DLNAHeader<TimeSeekRangeType> {

    public TimeSeekRangeHeader() {
    }

    public TimeSeekRangeHeader(TimeSeekRangeType timeSeekRange) {
        setValue(timeSeekRange);
    }
    @Override
    public void setString(String s) throws InvalidHeaderException {
        if (!s.isEmpty()) {
            String[] params = s.split(" ");
            if (params.length>0) {
                try {
                    TimeSeekRangeType t = new TimeSeekRangeType(NormalPlayTimeRange.valueOf(params[0]));
                    if (params.length > 1) {
                        t.setBytesRange(BytesRange.valueOf(params[1]));
                    }
                    setValue(t);
                    return;
                } catch (InvalidValueException invalidValueException) {
                    throw new InvalidHeaderException("Invalid TimeSeekRange header value: " + s + "; "+invalidValueException.getMessage());
                }
            }
        }
        throw new InvalidHeaderException("Invalid TimeSeekRange header value: " + s);
    }

    @Override
    public String getString() {
        TimeSeekRangeType t = getValue();
        String s = t.getNormalPlayTimeRange().getString();
        if (t.getBytesRange()!=null) s+= " "+t.getBytesRange().getString(true);
        return s;
    }
    
}
