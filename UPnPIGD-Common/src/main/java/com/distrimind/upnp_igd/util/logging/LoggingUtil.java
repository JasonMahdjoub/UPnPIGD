 /*
 * Copyright (C) 2012 4th Line GmbH, Switzerland
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
 package com.distrimind.upnp_igd.util.logging;

import com.distrimind.flexilogxml.log.Handler;
import com.distrimind.flexilogxml.log.LogManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Simplifies dealing with JUL oddities.
 * <p>
 * This utility class helps you deal with the two most common problems you'll have with
 * java.util.logging (JUL) and friends. The first is loading a default logging configuration without
 * specifying any additional system properties. Put a file called {@code default-logging.properties}
 * into the root of your classpath and call the static loader methods here.
 * </p>
 * <p>
 * Secondly, this class will automatically instantiate all logging handlers you specified
 * in your logging properties configuration. By default, JUL will search and instantiate handlers
 * using the system classloader. This regularly fails in more complex deployment scenarios, e.g.
 * when building "uber" JARs with delegating classloaders. The correct way to load user-configured
 * handlers is of course with the current thread's context classloader, which is what this utility
 * enables.
 * </p>
 *
 * @author Christian Bauer
 */
public class LoggingUtil {

    public static final String DEFAULT_CONFIG = "default-logging.properties";


    /**
     * Loads a handler class with the current context classloader.
     * <p>
     * The JUL manager will load handler classes with the system classloader, causing deployment problems. This
     * method will load a handler class through the curren thread's context classloader and instantiate it with
     * the default constructor.

     *
     * @param handlerNames A list of handler class names.
     * @return An array of instantiated handlers.
     * @throws Exception If there was a problem loading or instantiating the handler class.
     */
    public static Handler[] instantiateHandlers(List<String> handlerNames) throws Exception {
        List<Handler> list = new ArrayList<>();
        for (String handlerName : handlerNames) {
            list.add(
                    (Handler) Thread.currentThread().getContextClassLoader().loadClass(handlerName).getConstructor().newInstance()
            );
        }
        return list.toArray(new Handler[0]);
    }

    /**
     * Reads a standard JUL properties stream and removes the 'handlers' property.
     * <p>
     * All handler (class) names are added to the supplied list and an input stream with the same
     * properties, except for the 'handler' properties, is returned.

     *
     * @param is       The properties input stream
     * @param handlers All handler (class) names are added to this list.
     * @return A properties input stream without the 'handlers' property.
     * @throws IOException When building properties from the input stream failed.
     */
    public static InputStream spliceHandlers(InputStream is, List<String> handlers)
            throws IOException {

        // Load the properties from the original inputstream
        Properties props = new Properties();
        props.load(is);

        // Build new properties
        StringBuilder sb = new StringBuilder();
        List<String> handlersProperties = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {

            // Hold any 'handlers' property
            if ("handlers".equals(entry.getKey())) {
                handlersProperties.add(entry.getValue().toString());
            } else {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
        }

        // Add all handler classnames (separated by whitespace) to given list
        for (String handlersProperty : handlersProperties) {
            String[] handlerClasses = handlersProperty.trim().split(" ");
            for (String handlerClass : handlerClasses) {
                handlers.add(handlerClass.trim());
            }
        }

        // Return the modified properties without 'handlers' property (note the encoding)
        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * Clears root loggers and adds the given loggers.
     * <p>
     * This method removes all loggers which might have been configured on the JUL root logger
     * handlers (such as the broken default two-line-system-err handler) and adds the given handlers.

     *
     * @param h An array of handlers to use with the root logger.
     */
    public static void resetRootHandler(Handler... h) {
        LogManager.resetHandlers(h);
    }
}