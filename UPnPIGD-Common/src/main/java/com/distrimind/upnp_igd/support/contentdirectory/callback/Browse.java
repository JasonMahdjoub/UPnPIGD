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

package com.distrimind.upnp_igd.support.contentdirectory.callback;

import com.distrimind.upnp_igd.controlpoint.ActionCallback;
import com.distrimind.upnp_igd.model.action.ActionException;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.types.ErrorCode;
import com.distrimind.upnp_igd.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp_igd.support.contentdirectory.DIDLParser;
import com.distrimind.upnp_igd.support.model.BrowseFlag;
import com.distrimind.upnp_igd.support.model.BrowseResult;
import com.distrimind.upnp_igd.support.model.DIDLContent;
import com.distrimind.upnp_igd.support.model.SortCriterion;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp_igd.Log;

/**
 * Invokes a "Browse" action, parses the result.
 *
 * @author Christian Bauer
 */
public abstract class Browse extends ActionCallback {

    public static final String CAPS_WILDCARD = "*";

    public enum Status {
        NO_CONTENT("No Content"),
        LOADING("Loading..."),
        OK("OK");

        private final String defaultMessage;

        Status(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    final private static DMLogger log = Log.getLogger(Browse.class);

    /**
     * Browse with first result 0 and {@link #getDefaultMaxResults()}, filters with {@link #CAPS_WILDCARD}.
     */
    public Browse(Service<?, ?, ?> service, String containerId, BrowseFlag flag) {
        this(service, containerId, flag, CAPS_WILDCARD, 0, null);
    }

    /**
     * @param maxResults Can be <code>null</code>, then {@link #getDefaultMaxResults()} is used.
     */
    public Browse(Service<?, ?, ?> service, String objectID, BrowseFlag flag,
                                String filter, long firstResult, Long maxResults, SortCriterion... orderBy) {

        super(new ActionInvocation<>(service.getAction("Browse")));

		if (log.isDebugEnabled()) {
            log.debug("Creating browse action for object ID: " + objectID);
		}

		getActionInvocation().setInput("ObjectID", objectID);
        getActionInvocation().setInput("BrowseFlag", flag.toString());
        getActionInvocation().setInput("Filter", filter);
        getActionInvocation().setInput("StartingIndex", new UnsignedIntegerFourBytes(firstResult));
        getActionInvocation().setInput("RequestedCount",
                new UnsignedIntegerFourBytes(maxResults == null ? getDefaultMaxResults() : maxResults)
        );
        getActionInvocation().setInput("SortCriteria", SortCriterion.toString(orderBy));
    }

    @Override
    public void run() {
        updateStatus(Status.LOADING);
        super.run();
    }

    @Override
	public void success(ActionInvocation<?> invocation) {
        log.debug("Successful browse action, reading output argument values");

        BrowseResult result = new BrowseResult(
                invocation.getOutput("Result").getValue().toString(),
                (UnsignedIntegerFourBytes) invocation.getOutput("NumberReturned").getValue(),
                (UnsignedIntegerFourBytes) invocation.getOutput("TotalMatches").getValue(),
                (UnsignedIntegerFourBytes) invocation.getOutput("UpdateID").getValue()
        );

        boolean proceed = receivedRaw(invocation, result);

        if (proceed && result.getCountLong() > 0 && !result.getResult().isEmpty()) {

            try {

                DIDLParser didlParser = new DIDLParser();
                DIDLContent didl = didlParser.parse(result.getResult());
                received(invocation, didl);
                updateStatus(Status.OK);

            } catch (Exception ex) {
                invocation.setFailure(
                        new ActionException(ErrorCode.ACTION_FAILED, "Can't parse DIDL XML response: " + ex, ex)
                );
                failure(invocation, null);
            }

        } else {
            received(invocation, new DIDLContent());
            updateStatus(Status.NO_CONTENT);
        }
    }

    /**
     * Some media servers will crash if there is no limit on the maximum number of results.
     *
     * @return The default limit, 999.
     */
    public long getDefaultMaxResults() {
        return 999;
    }

    public boolean receivedRaw(ActionInvocation<?> actionInvocation, BrowseResult browseResult) {
        /*
        if (log.isTraceEnabled()) {
            log.trace("-------------------------------------------------------------------------------------");
            log.trace("\n" + XML.pretty(browseResult.getDidl()));
            log.trace("-------------------------------------------------------------------------------------");
        }
        */
        return true;
    }

    public abstract void received(ActionInvocation<?> actionInvocation, DIDLContent didl);
    public abstract void updateStatus(Status status);

}
