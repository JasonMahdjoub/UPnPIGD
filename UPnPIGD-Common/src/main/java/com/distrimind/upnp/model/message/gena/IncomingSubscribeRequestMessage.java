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

package com.distrimind.upnp.model.message.gena;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;
import com.distrimind.upnp.model.ModelUtil;
import com.distrimind.upnp.model.message.IUpnpHeaders;
import com.distrimind.upnp.model.message.StreamRequestMessage;
import com.distrimind.upnp.model.message.header.CallbackHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.model.message.header.NTEventHeader;
import com.distrimind.upnp.model.message.header.TimeoutHeader;
import com.distrimind.upnp.model.message.header.SubscriptionIdHeader;
import com.distrimind.upnp.model.meta.LocalService;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class IncomingSubscribeRequestMessage extends StreamRequestMessage {
    final private static DMLogger log = Log.getLogger(IncomingSubscribeRequestMessage.class);

    final private LocalService<?> service;
    private List<URL> callbackURLs=null;

    public IncomingSubscribeRequestMessage(StreamRequestMessage source, LocalService<?>  service) {
        super(source);
        this.service = service;
    }

    public LocalService<?> getService() {
        return service;
    }

    static List<URL> generateCallbackURLs(CallbackHeader header)
    {
        List<URL> callbackURLs=new ArrayList<>();
        if (header != null)
        {
            for (URL url : header.getValue()) {
                try {
                    InetAddress ia = InetAddress.getByName(url.getHost());
                    if (ia != null) {
                        if (!ModelUtil.isLocalAddressReachableFromThisMachine(ia)) {
                            log.debug("Host not accepted in IncomingSubscribeRequestMessage class");
                        } else {
                            callbackURLs.add(url);
                        }
                    }
                } catch (UnknownHostException ignored) {
                    log.debug("URL not found in IncomingSubscribeRequestMessage class");
                } catch (SocketException e) {
                    log.debug("Cannot parse network interfaces", e);
                }
            }
        }
        return callbackURLs;
    }

    public List<URL> getCallbackURLs() {
        if (callbackURLs==null) {
            callbackURLs=generateCallbackURLs(getHeaders().getFirstHeader(UpnpHeader.Type.CALLBACK, CallbackHeader.class));
        }
        return callbackURLs;
    }


    public boolean hasNotificationHeader() {
        return getHeaders().getFirstHeader(UpnpHeader.Type.NT, NTEventHeader.class) != null;
    }

    public Integer getRequestedTimeoutSeconds() {
        TimeoutHeader timeoutHeader = getHeaders().getFirstHeader(UpnpHeader.Type.TIMEOUT, TimeoutHeader.class);
        return timeoutHeader != null ? timeoutHeader.getValue() : null;
    }

    public String getSubscriptionId() {
        SubscriptionIdHeader header = getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class);
        return header != null ? header.getValue() : null;
    }

    @Override
    public void setHeaders(IUpnpHeaders headers) {
        super.setHeaders(headers);
        callbackURLs=null;
    }
}
