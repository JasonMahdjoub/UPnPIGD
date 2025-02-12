package com.distrimind.upnp.support.igd.callback;

import com.distrimind.upnp.controlpoint.ActionCallback;
import com.distrimind.upnp.controlpoint.ControlPoint;
import com.distrimind.upnp.model.action.ActionArgumentValue;
import com.distrimind.upnp.model.action.ActionException;
import com.distrimind.upnp.model.action.ActionInvocation;
import com.distrimind.upnp.model.meta.Service;
import com.distrimind.upnp.model.types.ErrorCode;
import com.distrimind.upnp.model.types.UnsignedIntegerTwoBytes;
import com.distrimind.upnp.support.model.PortMapping;

import java.util.Map;

public abstract class PortMappingEntryGet extends ActionCallback {

    public PortMappingEntryGet(Service<?, ?, ?> service, long index) {
        this(service, null, index);
    }

    protected PortMappingEntryGet(Service<?, ?, ?> service, ControlPoint controlPoint, long index) {
        super(new ActionInvocation<>(service.getAction("GetGenericPortMappingEntry")), controlPoint);

        getActionInvocation().setInput("NewPortMappingIndex", new UnsignedIntegerTwoBytes(index));
    }

    @Override
    public void success(ActionInvocation<?> invocation) {
        try {
            Map<String, ? extends ActionArgumentValue<? extends Service<?, ?, ?>>> outputMap = invocation.getOutputMap();
            success(new PortMapping(outputMap));
        }
        catch (Exception ex) {
            invocation.setFailure(
                    new ActionException(
                            ErrorCode.ARGUMENT_VALUE_INVALID,
                            "Invalid status or last error string: " + ex,
                            ex
                    )
            );
            failure(invocation, null);
        }
    }

    protected abstract void success(PortMapping portMapping);
}