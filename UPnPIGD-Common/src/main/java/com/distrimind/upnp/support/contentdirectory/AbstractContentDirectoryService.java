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
package com.distrimind.upnp.support.contentdirectory;

import com.distrimind.upnp.binding.annotations.UpnpAction;
import com.distrimind.upnp.binding.annotations.UpnpInputArgument;
import com.distrimind.upnp.binding.annotations.UpnpOutputArgument;
import com.distrimind.upnp.binding.annotations.UpnpService;
import com.distrimind.upnp.binding.annotations.UpnpServiceId;
import com.distrimind.upnp.binding.annotations.UpnpServiceType;
import com.distrimind.upnp.binding.annotations.UpnpStateVariable;
import com.distrimind.upnp.binding.annotations.UpnpStateVariables;
import com.distrimind.upnp.model.types.ErrorCode;
import com.distrimind.upnp.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp.model.types.csv.CSV;
import com.distrimind.upnp.model.types.csv.CSVString;
import com.distrimind.upnp.support.avtransport.AbstractAVTransportService;
import com.distrimind.upnp.support.model.BrowseFlag;
import com.distrimind.upnp.support.model.BrowseResult;
import com.distrimind.upnp.support.model.DIDLContent;
import com.distrimind.upnp.support.model.SortCriterion;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple ContentDirectory service skeleton.
 * <p>
 * Only state variables and actions required by <em>ContentDirectory:1</em>
 * (not the optional ones) are implemented.
 * </p>
 *
 * @author Alessio Gaeta
 * @author Christian Bauer
 */

@UpnpService(
        serviceId = @UpnpServiceId("ContentDirectory"),
        serviceType = @UpnpServiceType(value = "ContentDirectory", version = 1)
)

@UpnpStateVariables({
                            @UpnpStateVariable(
                                    name = "A_ARG_TYPE_ObjectID",
                                    sendEvents = false,
                                    datatype = AbstractAVTransportService.STRING),
                            @UpnpStateVariable(
                                    name = "A_ARG_TYPE_Result",
                                    sendEvents = false,
                                    datatype = AbstractAVTransportService.STRING),
                            @UpnpStateVariable(
                                    name = "A_ARG_TYPE_BrowseFlag",
                                    sendEvents = false,
                                    datatype = AbstractAVTransportService.STRING,
                                    allowedValuesEnum = BrowseFlag.class),
                            @UpnpStateVariable(
                                    name = "A_ARG_TYPE_Filter",
                                    sendEvents = false,
                                    datatype = AbstractAVTransportService.STRING),
                            @UpnpStateVariable(
                                    name = "A_ARG_TYPE_SortCriteria",
                                    sendEvents = false,
                                    datatype = AbstractAVTransportService.STRING),
                            @UpnpStateVariable(
                                    name = "A_ARG_TYPE_Index",
                                    sendEvents = false,
                                    datatype = "ui4"),
                            @UpnpStateVariable(
                                    name = AbstractContentDirectoryService.A_ARG_TYPE_COUNT,
                                    sendEvents = false,
                                    datatype = "ui4"),
                            @UpnpStateVariable(
                                    name = "A_ARG_TYPE_UpdateID",
                                    sendEvents = false,
                                    datatype = "ui4"),
                            @UpnpStateVariable(
                                    name = "A_ARG_TYPE_URI",
                                    sendEvents = false,
                                    datatype = "uri"),
                            @UpnpStateVariable(
                                    name = "A_ARG_TYPE_SearchCriteria",
                                    sendEvents = false,
                                    datatype = AbstractAVTransportService.STRING)
                    })
@SuppressWarnings("PMD.LooseCoupling")
public abstract class AbstractContentDirectoryService {

    public static final String CAPS_WILDCARD = "*";
    public static final String A_ARG_TYPE_COUNT = "A_ARG_TYPE_Count";

    @UpnpStateVariable(sendEvents = false)
    final private CSV<String> searchCapabilities;

    @UpnpStateVariable(sendEvents = false)
    final private CSV<String> sortCapabilities;

    @UpnpStateVariable(
            sendEvents = true,
            defaultValue = "0",
            eventMaximumRateMilliseconds = 200
    )
    private final UnsignedIntegerFourBytes systemUpdateID = new UnsignedIntegerFourBytes(0);

    final protected PropertyChangeSupport propertyChangeSupport;

    protected AbstractContentDirectoryService() {
        this(new ArrayList<>(), new ArrayList<>(), null);
    }

    protected AbstractContentDirectoryService(List<String> searchCapabilities, List<String> sortCapabilities) {
        this(searchCapabilities, sortCapabilities, null);
    }

    protected AbstractContentDirectoryService(List<String> searchCapabilities, List<String> sortCapabilities,
                                              PropertyChangeSupport propertyChangeSupport) {
        this.propertyChangeSupport = propertyChangeSupport != null ? propertyChangeSupport : new PropertyChangeSupport(this);
        this.searchCapabilities = new CSVString();
        this.searchCapabilities.addAll(searchCapabilities);
        this.sortCapabilities = new CSVString();
        this.sortCapabilities.addAll(sortCapabilities);
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "SearchCaps"))
    public CSV<String> getSearchCapabilities() {
        return searchCapabilities;
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "SortCaps"))
    public CSV<String> getSortCapabilities() {
        return sortCapabilities;
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "Id"))
    synchronized public UnsignedIntegerFourBytes getSystemUpdateID() {
        return systemUpdateID;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    /**
     * Call this method after making changes to your content directory.
     * <p>
     * This will notify clients that their view of the content directory is potentially
     * outdated and has to be refreshed.
   
     */
    synchronized protected void changeSystemUpdateID() {
        Long oldUpdateID = getSystemUpdateID().getValue();
        systemUpdateID.increment(true);
        getPropertyChangeSupport().firePropertyChange(
                "SystemUpdateID",
                oldUpdateID,
                getSystemUpdateID().getValue()
        );
    }

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "Result",
                                stateVariable = "A_ARG_TYPE_Result",
                                getterName = "getResult"),
            @UpnpOutputArgument(name = "NumberReturned",
                                stateVariable = A_ARG_TYPE_COUNT,
                                getterName = "getCount"),
            @UpnpOutputArgument(name = "TotalMatches",
                                stateVariable = A_ARG_TYPE_COUNT,
                                getterName = "getTotalMatches"),
            @UpnpOutputArgument(name = "UpdateID",
                                stateVariable = "A_ARG_TYPE_UpdateID",
                                getterName = "getContainerUpdateID")
    })
    public BrowseResult browse(
            @UpnpInputArgument(name = "ObjectID", aliases = "ContainerID") String objectId,
            @UpnpInputArgument(name = "BrowseFlag") String browseFlag,
            @UpnpInputArgument(name = "Filter") String filter,
            @UpnpInputArgument(name = "StartingIndex", stateVariable = "A_ARG_TYPE_Index") UnsignedIntegerFourBytes firstResult,
            @UpnpInputArgument(name = "RequestedCount", stateVariable = A_ARG_TYPE_COUNT) UnsignedIntegerFourBytes maxResults,
            @UpnpInputArgument(name = "SortCriteria") String orderBy)
            throws ContentDirectoryException {

        List<SortCriterion> orderByCriteria;
        try {
            orderByCriteria = SortCriterion.valueOf(orderBy);
        } catch (Exception ex) {
            throw new ContentDirectoryException(ContentDirectoryErrorCode.UNSUPPORTED_SORT_CRITERIA, ex.toString());
        }

        try {
            return browse(
                    objectId,
                    BrowseFlag.valueOrNullOf(browseFlag),
                    filter,
                    firstResult.getValue(), maxResults.getValue(),
                    orderByCriteria
            );
        } catch (ContentDirectoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, ex.toString());
        }
    }

    /**
     * Implement this method to implement browsing of your content.
     * <p>
     * This is a required action defined by <em>ContentDirectory:1</em>.
   
     * <p>
     * You should wrap any exception into a {@link ContentDirectoryException}, so a property
     * error message can be returned to control points.
   
     */
    public abstract BrowseResult browse(String objectID, BrowseFlag browseFlag,
                                        String filter,
                                        long firstResult, long maxResults,
                                        List<SortCriterion> orderBy) throws ContentDirectoryException;


    @UpnpAction(out = {
            @UpnpOutputArgument(name = "Result",
                                stateVariable = "A_ARG_TYPE_Result",
                                getterName = "getResult"),
            @UpnpOutputArgument(name = "NumberReturned",
                                stateVariable = A_ARG_TYPE_COUNT,
                                getterName = "getCount"),
            @UpnpOutputArgument(name = "TotalMatches",
                                stateVariable = A_ARG_TYPE_COUNT,
                                getterName = "getTotalMatches"),
            @UpnpOutputArgument(name = "UpdateID",
                                stateVariable = "A_ARG_TYPE_UpdateID",
                                getterName = "getContainerUpdateID")
    })
    public BrowseResult search(
            @UpnpInputArgument(name = "ContainerID", stateVariable = "A_ARG_TYPE_ObjectID") String containerId,
            @UpnpInputArgument(name = "SearchCriteria") String searchCriteria,
            @UpnpInputArgument(name = "Filter") String filter,
            @UpnpInputArgument(name = "StartingIndex", stateVariable = "A_ARG_TYPE_Index") UnsignedIntegerFourBytes firstResult,
            @UpnpInputArgument(name = "RequestedCount", stateVariable = A_ARG_TYPE_COUNT) UnsignedIntegerFourBytes maxResults,
            @UpnpInputArgument(name = "SortCriteria") String orderBy)
            throws ContentDirectoryException {

        List<SortCriterion> orderByCriteria;
        try {
            orderByCriteria = SortCriterion.valueOf(orderBy);
        } catch (Exception ex) {
            throw new ContentDirectoryException(ContentDirectoryErrorCode.UNSUPPORTED_SORT_CRITERIA, ex.toString());
        }

        try {
            return search(
                    containerId,
                    searchCriteria,
                    filter,
                    firstResult.getValue(), maxResults.getValue(),
                    orderByCriteria
            );
        } catch (ContentDirectoryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, ex.toString());
        }
    }

    /**
     * Override this method to implement searching of your content.
     * <p>
     * The default implementation returns an empty result.
   
     */
    public BrowseResult search(String containerId, String searchCriteria, String filter,
                               long firstResult, long maxResults, List<SortCriterion> orderBy) throws ContentDirectoryException {

        try {
            return new BrowseResult(new DIDLParser().generate(new DIDLContent()), 0, 0);
        } catch (Exception ex) {
            throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, ex.toString());
        }
    }
}
