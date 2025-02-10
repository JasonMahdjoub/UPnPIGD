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

package com.distrimind.upnp_igd.support.shared;

import com.distrimind.flexilogxml.concurrent.ThreadType;
import com.distrimind.flexilogxml.log.Handler;
import com.distrimind.flexilogxml.log.LogManager;
import com.distrimind.upnp_igd.Log;
import com.distrimind.upnp_igd.UpnpService;
import com.distrimind.upnp_igd.swing.AbstractController;
import com.distrimind.upnp_igd.swing.Application;
import com.distrimind.upnp_igd.swing.logging.LogCategory;
import com.distrimind.upnp_igd.swing.logging.LogController;
import com.distrimind.upnp_igd.swing.logging.LogMessage;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.flexilogxml.log.Level;

/**
 * @author Christian Bauer
 */
public abstract class MainController extends AbstractController<JFrame> {
    final DMLogger logger = Log.getLogger(MainController.class);
    // Dependencies
    final private LogController logController;

    // View
    final private JPanel logPanel;

    public MainController(JFrame view, List<LogCategory> logCategories) {
        super(view);

        // Some UI stuff (of course, why would the OS L&F be the default -- too easy?!)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.error(() -> "Unable to load native look and feel: ", ex);
        }

        // Exception handler
        System.setProperty("sun.awt.exception.handler", AWTExceptionHandler.class.getName());
        ThreadFactory threadFactory= ThreadType.VIRTUAL_THREAD_IF_AVAILABLE.newThreadFactoryInstance();
        // Shutdown behavior
        Runtime.getRuntime().addShutdownHook(threadFactory.newThread(() -> {
            if (getUpnpService() != null)
                getUpnpService().shutdown();
        }));

        // Logging UI
        logController = new LogController(this, logCategories) {
            @Override
            protected void expand(LogMessage logMessage) {
                fireEventGlobal(
                        new TextExpandEvent(logMessage.getMessage())
                );
            }

            @Override
            protected Frame getParentWindow() {
                return MainController.this.getView();
            }
        };
        logPanel = logController.getView();
        logPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Wire UI into JUL
        // Don't reset JUL root logger but add if there is a JUL config file
        Handler handler = logRecord -> logController.pushMessage(new LogMessage(logRecord));
        if (System.getProperty("java.util.logging.config.file") == null) {
            LogManager.resetHandlers(handler);
        } else {
            LogManager.addHandler(handler);
        }
    }

    public LogController getLogController() {
        return logController;
    }

    public JPanel getLogPanel() {
        return logPanel;
    }

    public void log(Level level, String msg) {
        log(new LogMessage(level, msg));
    }

    public void log(LogMessage message) {
        getLogController().pushMessage(message);
    }

    @Override
    public void dispose() {
        super.dispose();
        ShutdownWindow.INSTANCE.setVisible(true);
    }

    public static class ShutdownWindow extends JWindow {
        private static final long serialVersionUID = 1L;
        final public static JWindow INSTANCE = new ShutdownWindow();

        protected ShutdownWindow() {
            JLabel shutdownLabel = new JLabel("Shutting down, please wait...");
            shutdownLabel.setHorizontalAlignment(JLabel.CENTER);
            getContentPane().add(shutdownLabel);
            setPreferredSize(new Dimension(300, 30));
            pack();
            Application.center(this);
        }
    }

    public abstract UpnpService getUpnpService();

}
