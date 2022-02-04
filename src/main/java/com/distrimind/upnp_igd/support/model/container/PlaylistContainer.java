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

package com.distrimind.upnp_igd.support.model.container;

import com.distrimind.upnp_igd.support.model.DIDLObject;
import com.distrimind.upnp_igd.support.model.Person;
import com.distrimind.upnp_igd.support.model.PersonWithRole;
import com.distrimind.upnp_igd.support.model.StorageMedium;

import java.util.List;

/**
 * @author Christian Bauer
 */
public class PlaylistContainer extends Container {

    public static final Class CLASS = new Class("object.container.playlistContainer");

    public PlaylistContainer() {
        setClazz(CLASS);
    }

    public PlaylistContainer(Container other) {
        super(other);
    }

    public PlaylistContainer(String id, Container parent, String title, String creator, Integer childCount) {
        this(id, parent.getId(), title, creator, childCount);
    }

    public PlaylistContainer(String id, String parentID, String title, String creator, Integer childCount) {
        super(id, parentID, title, creator, CLASS, childCount);
    }

    public PersonWithRole getFirstArtist() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.ARTIST.class);
    }

    public PersonWithRole[] getArtists() {
        List<PersonWithRole> list = getPropertyValues(DIDLObject.Property.UPNP.ARTIST.class);
        return list.toArray(new PersonWithRole[list.size()]);
    }

    public PlaylistContainer setArtists(PersonWithRole[] artists) {
        removeProperties(DIDLObject.Property.UPNP.ARTIST.class);
        for (PersonWithRole artist : artists) {
            addProperty(new DIDLObject.Property.UPNP.ARTIST(artist));
        }
        return this;
    }

    public String getFirstGenre() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.GENRE.class);
    }

    public String[] getGenres() {
        List<String> list = getPropertyValues(DIDLObject.Property.UPNP.GENRE.class);
        return list.toArray(new String[list.size()]);
    }

    public PlaylistContainer setGenres(String[] genres) {
        removeProperties(DIDLObject.Property.UPNP.GENRE.class);
        for (String genre : genres) {
            addProperty(new DIDLObject.Property.UPNP.GENRE(genre));
        }
        return this;
    }

    public String getDescription() {
        return getFirstPropertyValue(DIDLObject.Property.DC.DESCRIPTION.class);
    }

    public PlaylistContainer setDescription(String description) {
        replaceFirstProperty(new DIDLObject.Property.DC.DESCRIPTION(description));
        return this;
    }

    public String getLongDescription() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.LONG_DESCRIPTION.class);
    }

    public PlaylistContainer setLongDescription(String description) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.LONG_DESCRIPTION(description));
        return this;
    }

    public Person getFirstProducer() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.PRODUCER.class);
    }

    public Person[] getProducers() {
        List<Person> list = getPropertyValues(DIDLObject.Property.UPNP.PRODUCER.class);
        return list.toArray(new Person[list.size()]);
    }

    public PlaylistContainer setProducers(Person[] persons) {
        removeProperties(DIDLObject.Property.UPNP.PRODUCER.class);
        for (Person p : persons) {
            addProperty(new DIDLObject.Property.UPNP.PRODUCER(p));
        }
        return this;
    }

    public StorageMedium getStorageMedium() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_MEDIUM.class);
    }

    public PlaylistContainer setStorageMedium(StorageMedium storageMedium) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_MEDIUM(storageMedium));
        return this;
    }

    public String getDate() {
        return getFirstPropertyValue(DIDLObject.Property.DC.DATE.class);
    }

    public PlaylistContainer setDate(String date) {
        replaceFirstProperty(new DIDLObject.Property.DC.DATE(date));
        return this;
    }

    public String getFirstRights() {
        return getFirstPropertyValue(DIDLObject.Property.DC.RIGHTS.class);
    }

    public String[] getRights() {
        List<String> list = getPropertyValues(DIDLObject.Property.DC.RIGHTS.class);
        return list.toArray(new String[list.size()]);
    }

    public PlaylistContainer setRights(String[] rights) {
        removeProperties(DIDLObject.Property.DC.RIGHTS.class);
        for (String right : rights) {
            addProperty(new DIDLObject.Property.DC.RIGHTS(right));
        }
        return this;
    }

    public Person getFirstContributor() {
        return getFirstPropertyValue(DIDLObject.Property.DC.CONTRIBUTOR.class);
    }

    public Person[] getContributors() {
        List<Person> list = getPropertyValues(DIDLObject.Property.DC.CONTRIBUTOR.class);
        return list.toArray(new Person[list.size()]);
    }

    public PlaylistContainer setContributors(Person[] contributors) {
        removeProperties(DIDLObject.Property.DC.CONTRIBUTOR.class);
        for (Person p : contributors) {
            addProperty(new DIDLObject.Property.DC.CONTRIBUTOR(p));
        }
        return this;
    }

    public String getLanguage() {
        return getFirstPropertyValue(DIDLObject.Property.DC.LANGUAGE.class);
    }

    public PlaylistContainer setLanguage(String language) {
        replaceFirstProperty(new DIDLObject.Property.DC.LANGUAGE(language));
        return this;
    }

}
