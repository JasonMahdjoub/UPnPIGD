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

module UPnPIGD_Common {

	requires org.jsoup;
	requires static jakarta.cdi;
	requires static java.desktop;

	requires FlexiLogXML_Common;


	exports com.distrimind.upnp;
	exports com.distrimind.upnp.binding;
	exports com.distrimind.upnp.binding.xml;
	exports com.distrimind.upnp.binding.annotations;
	exports com.distrimind.upnp.binding.staging;
	exports com.distrimind.upnp.controlpoint;
	exports com.distrimind.upnp.controlpoint.event;
	exports com.distrimind.upnp.http;
	exports com.distrimind.upnp.mock;
	exports com.distrimind.upnp.model;
	exports com.distrimind.upnp.model.action;
	exports com.distrimind.upnp.model.gena;
	exports com.distrimind.upnp.model.message;
	exports com.distrimind.upnp.model.message.gena;
	exports com.distrimind.upnp.model.message.header;
	exports com.distrimind.upnp.model.message.control;
	exports com.distrimind.upnp.model.message.discovery;
	exports com.distrimind.upnp.model.meta;
	exports com.distrimind.upnp.model.profile;
	exports com.distrimind.upnp.model.resource;
	exports com.distrimind.upnp.model.state;
	exports com.distrimind.upnp.model.types;
	exports com.distrimind.upnp.model.types.csv;
	exports com.distrimind.upnp.protocol;
	exports com.distrimind.upnp.protocol.async;
	exports com.distrimind.upnp.protocol.sync;
	exports com.distrimind.upnp.registry;
	exports com.distrimind.upnp.registry.event;
	exports com.distrimind.upnp.statemachine;
	exports com.distrimind.upnp.support.model;
	exports com.distrimind.upnp.support.avtransport;
	exports com.distrimind.upnp.support.avtransport.lastchange;
	exports com.distrimind.upnp.support.avtransport.impl;
	exports com.distrimind.upnp.support.avtransport.impl.state;
	exports com.distrimind.upnp.support.avtransport.callback;
	exports com.distrimind.upnp.support.contentdirectory;
	exports com.distrimind.upnp.support.contentdirectory.callback;
	exports com.distrimind.upnp.support.contentdirectory.ui;
	exports com.distrimind.upnp.support.igd;
	exports com.distrimind.upnp.support.igd.callback;
	exports com.distrimind.upnp.support.lastchange;
	exports com.distrimind.upnp.support.model.container;
	exports com.distrimind.upnp.support.model.dlna;
	exports com.distrimind.upnp.support.model.item;
	exports com.distrimind.upnp.support.shared;
	exports com.distrimind.upnp.support.shared.log;
	exports com.distrimind.upnp.swing;
	exports com.distrimind.upnp.swing.logging;
	exports com.distrimind.upnp.transport;
	exports com.distrimind.upnp.transport.impl;
	exports com.distrimind.upnp.transport.spi;
	exports com.distrimind.upnp.util;
	exports com.distrimind.upnp.util.io;
	exports com.distrimind.upnp.xml;
	exports com.distrimind.upnp.platform;
}

