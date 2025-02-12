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
 package com.distrimind.upnp.swing.logging;

import com.distrimind.flexilogxml.log.LogRecord;
import com.distrimind.flexilogxml.log.Level;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

/**
 * @author Christian Bauer
 */
public class LogMessage {

    private final LogRecord logRecord;
    private final String thread = Thread.currentThread().getName();
    private final String source;

    public LogMessage(String message) {
        this(Level.INFO, message);
    }

    public LogMessage(String source, String message) {
        this(Level.INFO, source, message);
    }

    public LogMessage(Level level, String message) {
        this(level, null, message);
    }

    public LogMessage(Level level, String source, String message) {
        this(new LogRecord(level, message), source);
    }
    public LogMessage(LogRecord logRecord) {
        this(logRecord, null);
    }
    public LogMessage(LogRecord logRecord, String source) {
        if (logRecord==null)
            throw new NullPointerException();
        this.logRecord=logRecord;
        this.source=source;
    }

    public Level getLevel() {
        return logRecord.getLevel();
    }

    public Long getCreatedOn() {
        return logRecord.getCreatedOnUTC();
    }

    public String getThread() {
        return thread;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return logRecord.getMessage();
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS", Locale.ROOT);
        return getLevel() + " - " +
                dateFormat.format(new Date(getCreatedOn())) + " - " +
                getThread() + " : " +
                getSource() + " : " +
                getMessage();
    }
}