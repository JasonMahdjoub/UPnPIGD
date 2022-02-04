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

package com.distrimind.upnp_igd.support.model.item;

import com.distrimind.upnp_igd.support.model.*;
import com.distrimind.upnp_igd.support.model.container.Container;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class TextItem extends Item {

    public static final Class CLASS = new Class("object.item.textItem");

    public TextItem() {
        setClazz(CLASS);
    }

    public TextItem(Item other) {
        super(other);
    }

    public TextItem(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public TextItem(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, CLASS);
        if (resource != null) {
            getResources().addAll(Arrays.asList(resource));
        }
    }

    public PersonWithRole getFirstAuthor() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.AUTHOR.class);
    }

    public PersonWithRole[] getAuthors() {
        List<PersonWithRole> list = getPropertyValues(DIDLObject.Property.UPNP.AUTHOR.class);
        return list.toArray(new PersonWithRole[list.size()]);
    }

    public TextItem setAuthors(PersonWithRole[] persons) {
        removeProperties(DIDLObject.Property.UPNP.AUTHOR.class);
        for (PersonWithRole p: persons) {
            addProperty(new DIDLObject.Property.UPNP.AUTHOR(p));
        }
        return this;
    }

    public String getDescription() {
        return getFirstPropertyValue(DIDLObject.Property.DC.DESCRIPTION.class);
    }

    public TextItem setDescription(String description) {
        replaceFirstProperty(new DIDLObject.Property.DC.DESCRIPTION(description));
        return this;
    }

    public String getLongDescription() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.LONG_DESCRIPTION.class);
    }

    public TextItem setLongDescription(String description) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.LONG_DESCRIPTION(description));
        return this;
    }

    public String getLanguage() {
        return getFirstPropertyValue(DIDLObject.Property.DC.LANGUAGE.class);
    }

    public TextItem setLanguage(String language) {
        replaceFirstProperty(new DIDLObject.Property.DC.LANGUAGE(language));
        return this;
    }

    public StorageMedium getStorageMedium() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_MEDIUM.class);
    }

    public TextItem setStorageMedium(StorageMedium storageMedium) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_MEDIUM(storageMedium));
        return this;
    }

    public String getDate() {
        return getFirstPropertyValue(DIDLObject.Property.DC.DATE.class);
    }

    public TextItem setDate(String date) {
        replaceFirstProperty(new DIDLObject.Property.DC.DATE(date));
        return this;
    }

    public URI getFirstRelation() {
        return getFirstPropertyValue(DIDLObject.Property.DC.RELATION.class);
    }

    public URI[] getRelations() {
        List<URI> list = getPropertyValues(DIDLObject.Property.DC.RELATION.class);
        return list.toArray(new URI[list.size()]);
    }

    public TextItem setRelations(URI[] relations) {
        removeProperties(DIDLObject.Property.DC.RELATION.class);
        for (URI relation : relations) {
            addProperty(new DIDLObject.Property.DC.RELATION(relation));
        }
        return this;
    }

    public String getFirstRights() {
        return getFirstPropertyValue(DIDLObject.Property.DC.RIGHTS.class);
    }

    public String[] getRights() {
        List<String> list = getPropertyValues(DIDLObject.Property.DC.RIGHTS.class);
        return list.toArray(new String[list.size()]);
    }

    public TextItem setRights(String[] rights) {
        removeProperties(DIDLObject.Property.DC.RIGHTS.class);
        for (String right : rights) {
            addProperty(new DIDLObject.Property.DC.RIGHTS(right));
        }
        return this;
    }

    public String getRating() {
        return getFirstPropertyValue(DIDLObject.Property.UPNP.RATING.class);
    }

    public TextItem setRating(String rating) {
        replaceFirstProperty(new DIDLObject.Property.UPNP.RATING(rating));
        return this;
    }

    public Person getFirstContributor() {
        return getFirstPropertyValue(DIDLObject.Property.DC.CONTRIBUTOR.class);
    }

    public Person[] getContributors() {
        List<Person> list = getPropertyValues(DIDLObject.Property.DC.CONTRIBUTOR.class);
        return list.toArray(new Person[list.size()]);
    }

    public TextItem setContributors(Person[] contributors) {
        removeProperties(DIDLObject.Property.DC.CONTRIBUTOR.class);
        for (Person p : contributors) {
            addProperty(new DIDLObject.Property.DC.CONTRIBUTOR(p));
        }
        return this;
    }

    public Person getFirstPublisher() {
        return getFirstPropertyValue(DIDLObject.Property.DC.PUBLISHER.class);
    }

    public Person[] getPublishers() {
        List<Person> list = getPropertyValues(DIDLObject.Property.DC.PUBLISHER.class);
        return list.toArray(new Person[list.size()]);
    }

    public TextItem setPublishers(Person[] publishers) {
        removeProperties(DIDLObject.Property.DC.PUBLISHER.class);
        for (Person publisher : publishers) {
            addProperty(new DIDLObject.Property.DC.PUBLISHER(publisher));
        }
        return this;
    }

}
