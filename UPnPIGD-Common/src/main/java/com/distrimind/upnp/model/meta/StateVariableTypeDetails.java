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

package com.distrimind.upnp.model.meta;



import com.distrimind.upnp.model.Validatable;
import com.distrimind.upnp.model.ValidationError;
import com.distrimind.upnp.model.types.Datatype;

import java.util.*;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * Type of state variable, its default value, and integrity rules for allowed values and ranges.
 *
 * @author Christian Bauer
 */
public class StateVariableTypeDetails implements Validatable {

    final private static DMLogger log = Log.getLogger(StateVariableTypeDetails.class);

    final private Datatype<?> datatype;
    final private String defaultValue;
    final private List<String> allowedValues;
    final private StateVariableAllowedValueRange allowedValueRange;

    public StateVariableTypeDetails(Datatype<?> datatype) {
        this(datatype, null, null, null);
    }

    public StateVariableTypeDetails(Datatype<?> datatype, String defaultValue) {
        this(datatype, defaultValue, null, null);
    }

    public StateVariableTypeDetails(Datatype<?> datatype, String defaultValue, Collection<String> allowedValues, StateVariableAllowedValueRange allowedValueRange) {
        this.datatype = datatype;
        this.defaultValue = defaultValue;
        this.allowedValues = allowedValues==null?null:List.copyOf(allowedValues);
        this.allowedValueRange = allowedValueRange;
    }

    public Datatype<?> getDatatype() {
        return datatype;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<String> getAllowedValues() {
        // TODO: UPNP VIOLATION: DirecTV HR23/700 High Definition DVR Receiver has invalid default value
        if (!foundDefaultInAllowedValues(defaultValue, allowedValues)) {
            List<String> list = new ArrayList<>(allowedValues);
            list.add(getDefaultValue());
            return list;
        }
        return allowedValues;
    }

    public StateVariableAllowedValueRange getAllowedValueRange() {
        return allowedValueRange;
    }

    protected boolean foundDefaultInAllowedValues(String defaultValue, Collection<String> allowedValues) {
        if (defaultValue == null || allowedValues == null) return true;
        for (String s : allowedValues) {
            if (s.equals(defaultValue)) return true;
        }
        return false;
    }

    @Override
	public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getDatatype() == null) {
            errors.add(new ValidationError(
                    getClass(),
                    "datatype",
                    "Service state variable has no datatype"
            ));
        }

        if (getAllowedValues() != null) {

            if (getAllowedValueRange() != null) {
                errors.add(new ValidationError(
                        getClass(),
                        "allowedValues",
                        "Allowed value list of state variable can not also be restricted with allowed value range"
                ));
            }

            if (!Datatype.Builtin.STRING.equals(getDatatype().getBuiltin())) {
                errors.add(new ValidationError(
                        getClass(),
                        "allowedValues",
                        "Allowed value list of state variable only available for string datatype, not: " + getDatatype()
                ));
            }

            for (String s : getAllowedValues()) {
                if (s.length() > 31) {
                    if (log.isWarnEnabled()) log.warn("UPnP specification violation, allowed value string must be less than 32 chars: " + s);
                }
            }

            if(!foundDefaultInAllowedValues(defaultValue, allowedValues)) {
                if (log.isWarnEnabled()) log.warn("UPnP specification violation, allowed string values " +
                                    "don't contain default value: " + defaultValue
                    );
            }
        }

        if (getAllowedValueRange() != null) {
            errors.addAll(getAllowedValueRange().validate());
        }

        return errors;
    }
}
