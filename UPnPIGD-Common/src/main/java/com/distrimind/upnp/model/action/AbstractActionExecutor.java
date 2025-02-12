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

package com.distrimind.upnp.model.action;

import com.distrimind.upnp.model.Command;
import com.distrimind.upnp.model.ServiceManager;
import com.distrimind.upnp.model.meta.Action;
import com.distrimind.upnp.model.meta.ActionArgument;
import com.distrimind.upnp.model.meta.LocalService;
import com.distrimind.upnp.model.state.StateVariableAccessor;
import com.distrimind.upnp.model.types.ErrorCode;
import com.distrimind.upnp.model.types.InvalidValueException;
import com.distrimind.upnp.util.Exceptions;

import java.util.*;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * Shared procedures for action executors based on an actual service implementation instance.
 *
 * @author Christian Bauer
 */
public abstract class AbstractActionExecutor implements ActionExecutor {

    final private static DMLogger log = Log.getLogger(AbstractActionExecutor.class);

    protected Map<? extends ActionArgument<? extends LocalService<?>>, StateVariableAccessor> outputArgumentAccessors =
        new HashMap<>();

    protected AbstractActionExecutor() {
    }

    protected AbstractActionExecutor(Map<? extends ActionArgument<? extends LocalService<?>>, StateVariableAccessor> outputArgumentAccessors) {
        this.outputArgumentAccessors = outputArgumentAccessors;
    }

    public Map<? extends ActionArgument<? extends LocalService<?>>, StateVariableAccessor> getOutputArgumentAccessors() {
        return outputArgumentAccessors;
    }

    /**
     * Obtains the service implementation instance from the {@link ServiceManager}, handles exceptions.
     */
    @Override
	public <T> void execute(final ActionInvocation<LocalService<T>> actionInvocation) {

		if (log.isDebugEnabled()) {
            log.debug("Invoking on local service: " + actionInvocation);
		}

		final LocalService<T> service = actionInvocation.getAction().getService();

        try {

            if (service.getManager() == null) {
                throw new IllegalStateException("Service has no implementation factory, can't get service instance");
            }

            service.getManager().execute(new Command<>() {
                @Override
				public void execute(ServiceManager<T> serviceManager) throws Exception {
                    AbstractActionExecutor.this.execute(
                            actionInvocation,
                            serviceManager.getImplementation()
                    );
                }

                @Override
                public String toString() {
                    return "Action invocation: " + actionInvocation.getAction();
                }
            });

        } catch (ActionException ex) {
            if (log.isDebugEnabled()) {
                log.debug("ActionException thrown by service, wrapping in invocation and returning: ", ex);
                log.debug("Exception root cause: ", Exceptions.unwrap(ex));
            }
            actionInvocation.setFailure(ex);
        } catch (InterruptedException ex) {
            if (log.isDebugEnabled()) {
                log.debug("InterruptedException thrown by service, wrapping in invocation and returning: ", ex);
                log.debug("Exception root cause: ", Exceptions.unwrap(ex));
            }
            actionInvocation.setFailure(new ActionCancelledException(ex));
        } catch (Throwable t) {
            Throwable rootCause = Exceptions.unwrap(t);
            if (log.isDebugEnabled()) {
                log.debug("Execution has thrown, wrapping root cause in ActionException and returning: " + t);
                log.debug("Exception root cause: ", rootCause);
            }
            actionInvocation.setFailure(
                new ActionException(
                    ErrorCode.ACTION_FAILED,
                    (rootCause.getMessage() != null ? rootCause.getMessage() : rootCause.toString()),
                    rootCause
                )
            );
        }
    }

    protected abstract <T> void execute(ActionInvocation<LocalService<T>> actionInvocation, Object serviceImpl) throws Exception;

    /**
     * Reads the output arguments after an action execution using accessors.
     *
     * @param action The action of which the output arguments are read.
     * @param instance The instance on which the accessors will be invoked.
     * @return <code>null</code> if the action has no output arguments, a single instance if it has one, an
     *         <code>Object[]</code> otherwise.
     * @throws Exception if a problem occurs
     */
    protected <T> Object readOutputArgumentValues(Action<LocalService<T>> action, Object instance) throws Exception {
        List<Object> results = new ArrayList<>(action.getOutputArguments().size());
		if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve output argument values using accessor: " + action.getOutputArguments().size());
		}

		for (ActionArgument<LocalService<T>> outputArgument : action.getOutputArguments()) {
			if (log.isTraceEnabled()) {
				log.trace("Calling accessor method for: " + outputArgument);
			}

			StateVariableAccessor accessor = getOutputArgumentAccessors().get(outputArgument);
            if (accessor != null) {
				if (log.isDebugEnabled()) {
					log.debug("Calling accessor to read output argument value: " + accessor);
				}
				results.add(accessor.read(instance));
            } else {
                throw new IllegalStateException("No accessor bound for: " + outputArgument);
            }
        }

        if (results.size() == 1) {
            return results.iterator().next();
        }
        return !results.isEmpty() ? results : null;
    }

    /**
     * Sets the output argument value on the {@link ActionInvocation}, considers string conversion.
     */
    protected <T> void setOutputArgumentValue(ActionInvocation<LocalService<T>> actionInvocation, ActionArgument<LocalService<T>> argument, Object result)
            throws ActionException {

        LocalService<?> service = actionInvocation.getAction().getService();

        if (result != null) {
            try {
                if (service.isStringConvertibleType(result)) {
                    log.debug("Result of invocation matches convertible type, setting toString() single output argument value");
                    actionInvocation.setOutput(new ActionArgumentValue<>(argument, result.toString()));
                } else {
                    log.debug("Result of invocation is Object, setting single output argument value");
                    actionInvocation.setOutput(new ActionArgumentValue<>(argument, result));
                }
            } catch (InvalidValueException ex) {
                throw new ActionException(
                        ErrorCode.ARGUMENT_VALUE_INVALID,
                        "Wrong type or invalid value for '" + argument.getName() + "': " + ex.getMessage(),
                        ex
                );
            }
        } else {

            log.debug("Result of invocation is null, not setting any output argument value(s)");
        }

    }

}
