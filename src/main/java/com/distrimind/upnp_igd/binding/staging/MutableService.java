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

package com.distrimind.upnp_igd.binding.staging;

import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.meta.StateVariable;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class MutableService {

    public ServiceType serviceType;
    public ServiceId serviceId;
    public URI descriptorURI;
    public URI controlURI;
    public URI eventSubscriptionURI;

    public List<MutableAction> actions = new ArrayList<>();
    public List<MutableStateVariable> stateVariables = new ArrayList<>();

    public Service build(Device prototype) throws ValidationException {
        return prototype.newInstance(
                serviceType, serviceId,
                descriptorURI, controlURI, eventSubscriptionURI,
                createActions(),
                createStateVariables()
        );
    }

    public Action[] createActions() {
        Action[] array = new Action[actions.size()];
        int i = 0;
        for (MutableAction action : actions) {
            array[i++] = action.build();
        }
        return array;
    }

    public StateVariable[] createStateVariables() {
        StateVariable[] array = new StateVariable[stateVariables.size()];
        int i = 0;
        for (MutableStateVariable stateVariable : stateVariables) {
            array[i++] = stateVariable.build();
        }
        return array;
    }

}
