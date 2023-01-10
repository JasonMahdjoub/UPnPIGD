/*
Copyright or © or Corp. Jason Mahdjoub (01/04/2013)

jason.mahdjoub@distri-mind.fr

This software (Object Oriented Database (OOD)) is a computer program 
whose purpose is to manage a local database with the object paradigm 
and the java language 

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
module com.distrimind.upnp_igd {
	requires java.logging;

	requires cdi.api;
	requires seamless.util;
	exports com.distrimind.upnp_igd;
	exports com.distrimind.upnp_igd.model.state;
	exports com.distrimind.upnp_igd.model.message.control;
	exports com.distrimind.upnp_igd.model.message.discovery;
	exports com.distrimind.upnp_igd.model.message.gena;
	exports com.distrimind.upnp_igd.model.message.header;
	exports com.distrimind.upnp_igd.model.message;
	exports com.distrimind.upnp_igd.model.gena;
	exports com.distrimind.upnp_igd.model.action;
	exports com.distrimind.upnp_igd.model.meta;
	exports com.distrimind.upnp_igd.model.profile;
	exports com.distrimind.upnp_igd.model.resource;
	exports com.distrimind.upnp_igd.model.types;
	exports com.distrimind.upnp_igd.model.types.csv;
	exports com.distrimind.upnp_igd.model;
	exports com.distrimind.upnp_igd.android;
	exports com.distrimind.upnp_igd.binding.annotations;
	exports com.distrimind.upnp_igd.binding.xml;
	exports com.distrimind.upnp_igd.binding.staging;
	exports com.distrimind.upnp_igd.binding;
	exports com.distrimind.upnp_igd.mock;
	exports com.distrimind.upnp_igd.controlpoint.event;
	exports com.distrimind.upnp_igd.controlpoint;
	exports com.distrimind.upnp_igd.protocol.async;
	exports com.distrimind.upnp_igd.protocol.sync;
	exports com.distrimind.upnp_igd.protocol;
	exports com.distrimind.upnp_igd.registry.event;
	exports com.distrimind.upnp_igd.registry;
	exports com.distrimind.upnp_igd.support.model.container;
	exports com.distrimind.upnp_igd.support.model.dlna.types;
	exports com.distrimind.upnp_igd.support.model.dlna.message.header;
	exports com.distrimind.upnp_igd.support.model.dlna.message;
	exports com.distrimind.upnp_igd.support.model.dlna;
	exports com.distrimind.upnp_igd.support.model.item;
	exports com.distrimind.upnp_igd.support.model;
	exports com.distrimind.upnp_igd.support.igd.callback;
	exports com.distrimind.upnp_igd.support.igd;
	exports com.distrimind.upnp_igd.support.avtransport.callback;
	exports com.distrimind.upnp_igd.support.avtransport.impl.state;
	exports com.distrimind.upnp_igd.support.avtransport.impl;
	exports com.distrimind.upnp_igd.support.avtransport.lastchange;
	exports com.distrimind.upnp_igd.support.avtransport;
	exports com.distrimind.upnp_igd.support.lastchange;
	exports com.distrimind.upnp_igd.support.contentdirectory.callback;
	exports com.distrimind.upnp_igd.support.contentdirectory.ui;
	exports com.distrimind.upnp_igd.support.contentdirectory;
	exports com.distrimind.upnp_igd.support.shared.log.impl;
	exports com.distrimind.upnp_igd.support.shared.log;
	exports com.distrimind.upnp_igd.support.shared;
	exports com.distrimind.upnp_igd.transport.impl.jetty;
	exports com.distrimind.upnp_igd.transport.impl;
	exports com.distrimind.upnp_igd.transport.spi;
	exports com.distrimind.upnp_igd.transport;



}