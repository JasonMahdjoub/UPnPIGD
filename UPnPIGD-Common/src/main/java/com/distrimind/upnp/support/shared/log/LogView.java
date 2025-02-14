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

package com.distrimind.upnp.support.shared.log;

import com.distrimind.upnp.swing.logging.LogCategory;
import com.distrimind.upnp.swing.logging.LogMessage;
import com.distrimind.upnp.support.shared.View;

import java.util.List;

/**
 * @author Christian Bauer
 */
public interface LogView extends View<LogView.Presenter> {

    interface Presenter {

        void init();

        void onExpand(LogMessage logMessage);

        void pushMessage(LogMessage logMessage);
    }

    interface LogCategories extends List<LogCategory> {
    }

    void pushMessage(LogMessage logMessage);

    void dispose();
}
