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

package com.distrimind.upnp.model;

import com.distrimind.upnp.model.types.UDADeviceType;
import com.distrimind.upnp.model.types.UDAServiceId;
import com.distrimind.upnp.model.types.UDAServiceType;

import java.util.regex.Pattern;

/**
 * Shared and immutable settings.
 *
 * @author Christian Bauer
 */
public class Constants {
    public static final String DATE_FORMAT="yyyy-MM-dd";
    public static final String URN_p = "urn:(";
    public static final String URN = "urn:";
    private static final String END_PATTERN = "):([0-9]+).*";
    public static String SYSTEM_PROPERTY_ANNOUNCE_MAC_ADDRESS = "com.distrimind.upnp.network.announceMACAddress";

    public static int UPNP_MULTICAST_PORT = 1900;

    public static String IPV4_UPNP_MULTICAST_GROUP = "239.255.255.250";

    public static String IPV6_UPNP_LINK_LOCAL_ADDRESS = "FF02::C";
    public static String IPV6_UPNP_SUBNET_ADDRESS = "FF03::C";
    public static String IPV6_UPNP_ADMINISTRATIVE_ADDRESS = "FF04::C";
    public static String IPV6_UPNP_SITE_LOCAL_ADDRESS = "FF05::C";
    public static String IPV6_UPNP_GLOBAL_ADDRESS = "FF0E::C";

    public static int MIN_ADVERTISEMENT_AGE_SECONDS = 1800;
    
    // Parsing rules for: deviceType, serviceType, serviceId (UDA 1.0, section 2.5)

    // TODO: UPNP VIOLATION: Microsoft Windows Media Player Sharing 4.0, X_MS_MediaReceiverRegistrar service has type with periods instead of hyphens in the namespace!
    // UDA 1.0 spec: "Period characters in the vendor domain name MUST be replaced with hyphens in accordance with RFC 2141"
    // TODO: UPNP VIOLATION: Azureus/Vuze 4.2.0.2 sends a URN as a service identifier, so we need to match colons!
    // TODO: UPNP VIOLATION: Intel UPnP Tools send dots in the service identifier suffix, match that...

    private static String REGEX_NAMESPACE="[a-zA-Z0-9\\-\\.]+";
    private static String REGEX_TYPE="[a-zA-Z_0-9\\-]{1,64}";
    private static String REGEX_ID="[a-zA-Z_0-9\\-:\\.]{1,64}";
    private static Pattern patternNamespace;
    private static Pattern patternType;
    private static Pattern patternService;
    private static Pattern patternBrokenService;
    private static Pattern patternServiceId;
    private static Pattern patternBrokenServiceId;
    private static Pattern patternServiceEyeTV;
    private static Pattern patternServiceIniniTV;
    private static Pattern patternServiceIdKodakMediaServer;
    private static Pattern patternDeviceType;
    private static Pattern patternId;
    private static Pattern patternUDAServiceID;
    private static Pattern patternBrokenUDAServiceID;
    private static Pattern patternEyeconAndroidApp;
    private static Pattern patternUDAServiceType;
    private static Pattern patternUDADeviceType;
    private static Pattern patternDeviceEyeTV;
    private static Pattern patternDeviceEscient;


    private static void updateDeducedId()
    {
        patternId=Pattern.compile(REGEX_ID);
        patternBrokenUDAServiceID = Pattern.compile(URN + UDAServiceId.BROKEN_DEFAULT_NAMESPACE + ":service:(" + Constants.REGEX_ID+ ")");
        patternEyeconAndroidApp=Pattern.compile("urn:upnp-orgerviceId:urnchemas-upnp-orgervice:(" + Constants.REGEX_ID + ")");
        patternUDAServiceID = Pattern.compile(URN + UDAServiceId.DEFAULT_NAMESPACE + ":serviceId:(" + Constants.REGEX_ID + ")");
    }
    private static void updateDeducedType()
    {
        patternType=Pattern.compile(REGEX_TYPE);
        patternUDAServiceType=Pattern.compile(URN + UDAServiceType.DEFAULT_NAMESPACE + ":service:(" + Constants.REGEX_TYPE + END_PATTERN);
        // This pattern also accepts decimal versions, not only integers (as would be required by UDA), but cuts off fractions
        patternUDADeviceType=Pattern.compile(URN + UDADeviceType.DEFAULT_NAMESPACE + ":device:(" + Constants.REGEX_TYPE + END_PATTERN);
    }
    private static void updateDeducedNamespace()
    {
        patternNamespace=Pattern.compile(REGEX_NAMESPACE);
        patternServiceEyeTV=Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):service:(.+?):([0-9]+).*");
        patternDeviceEyeTV=Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):device:(.+?):([0-9]+).*");
        patternServiceIniniTV=Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):serviceId:(.+?):([0-9]+).*");
        patternDeviceEscient=Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):device::([0-9]+).*");

        patternServiceIdKodakMediaServer=Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):serviceId:");
    }
    private static void updateDeducedNamespaceType()
    {
        updateDeducedNamespaceType(true);
    }

    private static void updateDeducedNamespaceType(boolean cascade)
    {
        patternService =Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):service:(" + Constants.REGEX_TYPE + END_PATTERN);
        // Note: 'serviceId' vs. 'service'
        patternBrokenService =Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):serviceId:(" + Constants.REGEX_TYPE + END_PATTERN);
        patternDeviceType=Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):device:(" + Constants.REGEX_TYPE + END_PATTERN);
        if (cascade) {
            updateDeducedNamespace();
            updateDeducedType();
        }

    }
    private static void updateDeducedNamespaceId()
    {
        updateDeducedNamespaceId(true);
    }
    private static void updateDeducedNamespaceId(boolean cascade)
    {
        patternServiceId=Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):serviceId:(" + REGEX_ID + ")");
        // Note: 'service' vs. 'serviceId'
        patternBrokenServiceId=Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):service:(" + Constants.REGEX_ID+ ")");


        if (cascade) {
            updateDeducedNamespace();
            updateDeducedId();
        }
    }


    public static void setRegexNamespace(String regexNamespace)
    {
        if (regexNamespace==null)
            throw new NullPointerException();
        REGEX_NAMESPACE=regexNamespace;
        updateDeducedNamespace();
        updateDeducedNamespaceType(false);
        updateDeducedNamespaceId(false);
        updateDeducedPatternsFromUDANameAndNamespace();
    }
    public static void setRegexType(String regexType)
    {
        if (regexType==null)
            throw new NullPointerException();
        REGEX_TYPE=regexType;
        updateDeducedNamespaceType();
    }
    public static void setRegexId(String regexId)
    {
        if (regexId==null)
            throw new NullPointerException();
        REGEX_ID=regexId;

        updateDeducedNamespaceId();
    }

    public static Pattern getPatternDeviceEyeTV() {
        return patternDeviceEyeTV;
    }

    public static Pattern getPatternUDADeviceType() {
        return patternUDADeviceType;
    }

    public static Pattern getPatternUDAServiceType() {
        return patternUDAServiceType;
    }

    public static Pattern getPatternEyeconAndroidApp() {
        return patternEyeconAndroidApp;
    }

    public static Pattern getPatternUDAServiceID() {
        return patternUDAServiceID;
    }

    public static Pattern getPatternBrokenUDAServiceID() {
        return patternBrokenUDAServiceID;
    }

    public static Pattern getPatternDeviceEscient() {
        return patternDeviceEscient;
    }

    public static Pattern getPatternNamespace() {
        return patternNamespace;
    }

    public static Pattern getPatternType() {
        return patternType;
    }

    public static Pattern getPatternId() {
        return patternId;
    }

    public static Pattern getPatternService() {
        return patternService;
    }

    public static Pattern getPatternBrokenService() {
        return patternBrokenService;
    }


    public static Pattern getPatternServiceEyeTV() {
        return patternServiceEyeTV;
    }

    public static Pattern getPatternServiceIniniTV() {
        return patternServiceIniniTV;
    }

    public static Pattern getPatternServiceId() {
        return patternServiceId;
    }

    public static Pattern getPatternBrokenServiceId() {
        return patternBrokenServiceId;
    }

    public static Pattern getPatternServiceIdKodakMediaServer() {
        return patternServiceIdKodakMediaServer;
    }

    public static Pattern getPatternDeviceType() {
        return patternDeviceType;
    }

    /*
								Must not contain a hyphen character (-, 2D Hex in UTF- 8). First character must be a USASCII letter (A-Z, a-z),
								USASCII digit (0-9), an underscore ("_"), or a non-experimental Unicode letter or digit greater than U+007F.
								Succeeding characters must be a USASCII letter (A-Z, a-z), USASCII digit (0-9), an underscore ("_"), a
								period ("."), a Unicode combiningchar, an extender, or a non-experimental Unicode letter or digit greater
								than U+007F. The first three letters must not be "XML" in any combination of case. Case sensitive.
								 */
    // TODO: I have no idea how to match or what even is a "unicode extender character", neither does the Unicode book
    private static String REGEX_UDA_NAME="[a-zA-Z0-9^-_\\p{L}\\p{N}]{1}[a-zA-Z0-9^-_\\.\\\\p{L}\\\\p{N}\\p{Mc}\\p{Sk}]*";
    private static Pattern patternUDAName=Pattern.compile(REGEX_UDA_NAME);
    private static Pattern patternSOAPActionTypeMagicControl;
    private static Pattern patternSOAPActionType;
    private static void updateDeducedPatternsFromUDAName()
    {
        patternSOAPActionTypeMagicControl=Pattern.compile(Constants.NS_UPNP_CONTROL_10 +"#("+Constants.REGEX_UDA_NAME+")");

    }
    private static void updateDeducedPatternsFromUDANameAndNamespace()
    {
        patternSOAPActionType=Pattern.compile(URN_p + Constants.REGEX_NAMESPACE + "):service:(" + Constants.REGEX_TYPE + "):([0-9]+)#("+Constants.REGEX_UDA_NAME+")");
    }
    public static void setUDAName(String regexUDAName)
    {
        if (regexUDAName==null)
            throw new NullPointerException();
        REGEX_UDA_NAME=regexUDAName;
        patternUDAName=Pattern.compile(regexUDAName);
        updateDeducedPatternsFromUDAName();
        updateDeducedPatternsFromUDANameAndNamespace();
    }

    public static Pattern getPatternSOAPActionType() {
        return patternSOAPActionType;
    }

    public static Pattern getPatternSOAPActionTypeMagicControl() {
        return patternSOAPActionTypeMagicControl;
    }

    public static Pattern getPatternUDAName() {
        return patternUDAName;
    }

    // Random patentable "inventions" by MSFT
    public static String SOAP_NS_ENVELOPE = "https://schemas.xmlsoap.org/soap/envelope/";
    public static String SOAP_URI_ENCODING_STYLE = "https://schemas.xmlsoap.org/soap/encoding/";
    public static String NS_UPNP_CONTROL_10 = "urn:schemas-upnp-org:control-1-0";
    public static String NS_UPNP_EVENT_10 = "urn:schemas-upnp-org:event-1-0";

    // State variable prefixes
    public static String ARG_TYPE_PREFIX = "A_ARG_TYPE_";
    public static final int MAX_DESCRIPTOR_LENGTH=65536;
    public static final int MAX_BODY_LENGTH=MAX_DESCRIPTOR_LENGTH*2;
    //Defaults to maximum datagram size of 640 bytes (512 per UDA 1.0, 128 byte header).
    public static final int MAX_HEADER_LENGTH_IN_BYTES=640;
    public static final int MAX_INPUT_STREAM_SIZE_IN_BYTES=MAX_BODY_LENGTH+MAX_HEADER_LENGTH_IN_BYTES+100;
    static
    {
        updateDeducedId();
        updateDeducedType();
        updateDeducedNamespace();
        updateDeducedNamespaceType(false);
        updateDeducedNamespaceId(false);
        updateDeducedPatternsFromUDAName();
        updateDeducedPatternsFromUDANameAndNamespace();
    }
}
