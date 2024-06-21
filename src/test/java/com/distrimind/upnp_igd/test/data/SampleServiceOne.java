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

package com.distrimind.upnp_igd.test.data;

import com.distrimind.upnp_igd.model.meta.Action;
import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.meta.StateVariable;
import com.distrimind.upnp_igd.model.meta.StateVariableAllowedValueRange;
import com.distrimind.upnp_igd.model.meta.StateVariableEventDetails;
import com.distrimind.upnp_igd.model.meta.StateVariableTypeDetails;
import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.ServiceId;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.model.types.UDAServiceId;
import com.distrimind.upnp_igd.model.types.UDAServiceType;
import com.distrimind.upnp_igd.util.URIUtil;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Christian Bauer
 */
public class SampleServiceOne extends SampleService {

    public static URL getDescriptorURL() {
        return URIUtil.createAbsoluteURL(SampleDeviceRoot.getDeviceDescriptorURL(), getThisDescriptorURI());
    }

    public static URI getThisDescriptorURI() {
        return URI.create("service/upnp-org/MY-SERVICE-123/desc");
    }

    public static URI getThisControlURI() {
        return URI.create("service/upnp-org/MY-SERVICE-123/control");
    }

    public static URI getThisEventSubscriptionURI() {
        return URI.create("service/upnp-org/MY-SERVICE-123/events");
    }

    public static ServiceId getThisServiceId() {
        return new UDAServiceId("MY-SERVICE-123");
    }

    public static ServiceType getThisServiceType() {
        return new UDAServiceType("MY-SERVICE-TYPE-ONE", 1);
    }

    @Override
    public ServiceType getServiceType() {
        return getThisServiceType();
    }

    @Override
    public ServiceId getServiceId() {
        return getThisServiceId();
    }

    @Override
    public URI getDescriptorURI() {
        return getThisDescriptorURI();
    }

    @Override
    public URI getControlURI() {
        return getThisControlURI();
    }

    @Override
    public URI getEventSubscriptionURI() {
        return getThisEventSubscriptionURI();
    }

    @Override
    public Collection<Action> getActions() {
        return List.of(
                new Action(
                        "SetTarget",
						List.of(new ActionArgument("NewTargetValue", "Target", ActionArgument.Direction.IN))
                ),
                new Action(
                        "GetTarget",
                        List.of(new ActionArgument("RetTargetValue", "Target", ActionArgument.Direction.OUT, true))
                ),
                new Action(
                        "GetStatus",
                        List.of(new ActionArgument("ResultStatus", "Status", ActionArgument.Direction.OUT))
                )
        );
    }

    @Override
    public Collection<StateVariable> getStateVariables() {
        return List.of(
                new StateVariable(
                        "Target",
                        new StateVariableTypeDetails(Datatype.Builtin.BOOLEAN.getDatatype(), "0"),
                        new StateVariableEventDetails(false)
                ),
                new StateVariable(
                        "Status",
                        new StateVariableTypeDetails(Datatype.Builtin.BOOLEAN.getDatatype(), "0")
                ),
                new StateVariable(
                        "SomeVar",
                        new StateVariableTypeDetails(Datatype.Builtin.STRING.getDatatype(), "foo", List.of("foo", "bar"), null)
                ),
                new StateVariable(
                        "AnotherVar",
                        new StateVariableTypeDetails(Datatype.Builtin.UI4.getDatatype(), null, null, new StateVariableAllowedValueRange(0, 10, 2)),
                        new StateVariableEventDetails(false)
                ),
                new StateVariable(
                        "ModeratedMaxRateVar",
                        new StateVariableTypeDetails(Datatype.Builtin.STRING.getDatatype()),
                        new StateVariableEventDetails(true, 500, 0)
                ),
                new StateVariable(
                        "ModeratedMinDeltaVar",
                        new StateVariableTypeDetails(Datatype.Builtin.I4.getDatatype()),
                        new StateVariableEventDetails(true, 0, 3)
                )
                );
    }

    public static void assertMatch(Service<?, ?, ?> a, Service<?, ?, ?> b) {

        assertEquals(a.getActions().size(), b.getActions().size());

        assertEquals(a.getAction("SetTarget").getName(), b.getAction("SetTarget").getName());
        assertEquals(a.getAction("SetTarget").getArguments().size(), b.getAction("SetTarget").getArguments().size());
        assertEquals(a.getAction("SetTarget").getArguments().iterator().next().getName(), a.getAction("SetTarget").getArguments().iterator().next().getName());
        assertEquals(a.getAction("SetTarget").getArguments().iterator().next().getDirection(), b.getAction("SetTarget").getArguments().iterator().next().getDirection());
        assertEquals(a.getAction("SetTarget").getArguments().iterator().next().getRelatedStateVariableName(), b.getAction("SetTarget").getArguments().iterator().next().getRelatedStateVariableName());

        assertEquals(a.getAction("GetTarget").getArguments().iterator().next().getName(), b.getAction("GetTarget").getArguments().iterator().next().getName());
        // TODO: UPNP VIOLATION: WMP12 will discard RenderingControl service if it contains <retval> tags
        // assertEquals(a.getAction("GetTarget").getArguments()[0].isReturnValue(), b.getAction("GetTarget").getArguments()[0].isReturnValue());

        assertEquals(a.getStateVariables().size(), b.getStateVariables().size());
		assertNotNull(a.getStateVariable("Target"));
		assertNotNull(b.getStateVariable("Target"));
		assertNotNull(a.getStateVariable("Status"));
		assertNotNull(b.getStateVariable("Status"));
		assertNotNull(a.getStateVariable("SomeVar"));
		assertNotNull(b.getStateVariable("SomeVar"));

        assertEquals(a.getStateVariable("Target").getName(), "Target");
        assertEquals(a.getStateVariable("Target").getEventDetails().isSendEvents(), b.getStateVariable("Target").getEventDetails().isSendEvents());

        assertEquals(a.getStateVariable("Status").getName(), "Status");
        assertEquals(a.getStateVariable("Status").getEventDetails().isSendEvents(), b.getStateVariable("Status").getEventDetails().isSendEvents());
        assertEquals(a.getStateVariable("Status").getTypeDetails().getDatatype(), Datatype.Builtin.BOOLEAN.getDatatype());

        assertEquals(a.getStateVariable("SomeVar").getTypeDetails().getAllowedValues().size(), b.getStateVariable("SomeVar").getTypeDetails().getAllowedValues().size());
        assertEquals(a.getStateVariable("SomeVar").getTypeDetails().getDefaultValue(), b.getStateVariable("SomeVar").getTypeDetails().getDefaultValue());
        assertEquals(a.getStateVariable("SomeVar").getTypeDetails().getAllowedValues().iterator().next(), b.getStateVariable("SomeVar").getTypeDetails().getAllowedValues().iterator().next());
        assertEquals(a.getStateVariable("SomeVar").getTypeDetails().getAllowedValues().iterator().next(), b.getStateVariable("SomeVar").getTypeDetails().getAllowedValues().iterator().next());
        assertEquals(a.getStateVariable("SomeVar").getEventDetails().isSendEvents(), b.getStateVariable("SomeVar").getEventDetails().isSendEvents());

        assertEquals(a.getStateVariable("AnotherVar").getTypeDetails().getAllowedValueRange().getMinimum(), b.getStateVariable("AnotherVar").getTypeDetails().getAllowedValueRange().getMinimum());
        assertEquals(a.getStateVariable("AnotherVar").getTypeDetails().getAllowedValueRange().getMaximum(), b.getStateVariable("AnotherVar").getTypeDetails().getAllowedValueRange().getMaximum());
        assertEquals(a.getStateVariable("AnotherVar").getTypeDetails().getAllowedValueRange().getStep(), b.getStateVariable("AnotherVar").getTypeDetails().getAllowedValueRange().getStep());
        assertEquals(a.getStateVariable("AnotherVar").getEventDetails().isSendEvents(), b.getStateVariable("AnotherVar").getEventDetails().isSendEvents());
    }

}
