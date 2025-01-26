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

package com.distrimind.upnp_igd.model.types;

import com.distrimind.upnp_igd.model.Constants;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp_igd.Log;
import java.util.regex.Matcher;

/**
 * Represents a service type, for example <code>urn:my-domain-namespace:service:MyService:1</code>.
 * <p>
 * Although decimal versions are accepted and parsed, the version used for
 * comparison is only the integer withou the fraction.
 * </p>
 *
 * @author Christian Bauer
 */
public class ServiceType {

    final private static DMLogger log = Log.getLogger(ServiceType.class);

    private final String namespace;
    private final String type;
    private final int version;

    public ServiceType(String namespace, String type) {
        this(namespace, type, 1);
    }

    public ServiceType(String namespace, String type, int version) {

        if (namespace != null && !Constants.getPatternNamespace().matcher(namespace).matches()) {
            throw new IllegalArgumentException("Service type namespace contains illegal characters");
        }
        this.namespace = namespace;

        if (type != null && !Constants.getPatternType().matcher(type).matches()) {
            throw new IllegalArgumentException("Service type suffix too long (64) or contains illegal characters");
        }
        this.type = type;

        this.version = version;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getType() {
        return type;
    }

    public int getVersion() {
        return version;
    }

    /**
     * @return Either a {@link UDAServiceType} or a more generic {@link ServiceType}.
     */
    public static ServiceType valueOf(String _s) throws InvalidValueException {

        if (_s == null)
            throw new InvalidValueException("Can't parse null string");
        ServiceType serviceType = null;

        // Sometimes crazy UPnP devices deliver spaces in a URN, don't ask...
        String s = _s.replaceAll("\\s", "");

        // First try UDAServiceType parse
        try {
            serviceType = UDAServiceType.valueOf(s);
        } catch (Exception ignored) {
            // Ignore
        }

        if (serviceType != null)
            return serviceType;

        // Now try a generic ServiceType parse
        try {
            Matcher matcher = Constants.getPatternService().matcher(s);
            if (matcher.matches() && matcher.groupCount() >= 3) {
                return new ServiceType(matcher.group(1), matcher.group(2), Integer.parseInt(matcher.group(3)));
            }

            matcher = Constants.getPatternBrokenService().matcher(s);
            if (matcher.matches() && matcher.groupCount() >= 3) {
                return new ServiceType(matcher.group(1), matcher.group(2), Integer.parseInt(matcher.group(3)));
            }

            // TODO: UPNP VIOLATION: EyeTV Netstream uses colons in service type token
            // urn:schemas-microsoft-com:service:pbda:tuner:1
            matcher = Constants.getPatternServiceEyeTV().matcher(s);
            if (matcher.matches() && matcher.groupCount() >= 3) {
                String cleanToken = matcher.group(2).replaceAll("[^a-zA-Z_0-9\\-]", "-");
                if (log.isWarnEnabled()) log.warn(
                    "UPnP specification violation, replacing invalid service type token '"
                        + matcher.group(2)
                        + "' with: "
                        + cleanToken
                    );
                return new ServiceType(matcher.group(1), cleanToken, Integer.parseInt(matcher.group(3)));
            }

            // TODO: UPNP VIOLATION: Ceyton InfiniTV uses colons in service type token and 'serviceId' instead of 'service'
            // urn:schemas-opencable-com:serviceId:dri2:debug:1
            matcher = Constants.getPatternServiceIniniTV().matcher(s);
            if (matcher.matches() && matcher.groupCount() >= 3) {
                String cleanToken = matcher.group(2).replaceAll("[^a-zA-Z_0-9\\-]", "-");
                if (log.isWarnEnabled()) log.warn(
                    "UPnP specification violation, replacing invalid service type token '"
                    + matcher.group(2)
                    + "' with: "
                    + cleanToken
                );
                return new ServiceType(matcher.group(1), cleanToken, Integer.parseInt(matcher.group(3)));
            }
        } catch (RuntimeException e) {
            throw new InvalidValueException(String.format(
                "Can't parse service type string (namespace/type/version) '%s': %s", s, e
            ));
        }

        throw new InvalidValueException("Can't parse service type string (namespace/type/version): " + s);
    }

    /**
     * @return <code>true</code> if this type's namespace/name matches the other type's namespace/name and
     *         this type's version is equal or higher than the given types version.
     */
    public boolean implementsVersion(ServiceType that) {
        if (that == null) return false;
        if (!namespace.equals(that.namespace)) return false;
        if (!type.equals(that.type)) return false;
		return version >= that.version;
	}

    public String toFriendlyString() {
        return getNamespace() + ":" + getType() + ":" + getVersion();
    }

    @Override
    public String toString() {
        return "urn:" + getNamespace() + ":service:" + getType() + ":" + getVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceType)) return false;

        ServiceType that = (ServiceType) o;

        if (version != that.version) return false;
        if (!namespace.equals(that.namespace)) return false;
		return type.equals(that.type);
	}

    @Override
    public int hashCode() {
        int result = namespace.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + version;
        return result;
    }
}
