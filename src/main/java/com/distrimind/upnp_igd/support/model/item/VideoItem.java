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

import com.distrimind.upnp_igd.support.model.Person;
import com.distrimind.upnp_igd.support.model.PersonWithRole;
import com.distrimind.upnp_igd.support.model.Res;
import com.distrimind.upnp_igd.support.model.container.Container;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class VideoItem extends Item {

    public static final Class CLASS = new Class("object.item.videoItem");

    public VideoItem() {
        setClazz(CLASS);
    }

    public VideoItem(Item other) {
        super(other);
    }

    public VideoItem(String id, Container parent, String title, String creator, Res... resource) {
        this(id, parent.getId(), title, creator, resource);
    }

    public VideoItem(String id, String parentID, String title, String creator, Res... resource) {
        super(id, parentID, title, creator, CLASS);
        if (resource != null) {
            getResources().addAll(Arrays.asList(resource));
        }
    }

    public String getFirstGenre() {
        return getFirstPropertyValue(Property.UPNP.GENRE.class);
    }

    public String[] getGenres() {
        List<String> list = getPropertyValues(Property.UPNP.GENRE.class);
        return list.toArray(new String[list.size()]);
    }

    public VideoItem setGenres(String[] genres) {
        removeProperties(Property.UPNP.GENRE.class);
        for (String genre : genres) {
            addProperty(new Property.UPNP.GENRE(genre));
        }
        return this;
    }

    public String getDescription() {
        return getFirstPropertyValue(Property.DC.DESCRIPTION.class);
    }

    public VideoItem setDescription(String description) {
        replaceFirstProperty(new Property.DC.DESCRIPTION(description));
        return this;
    }

    public String getLongDescription() {
        return getFirstPropertyValue(Property.UPNP.LONG_DESCRIPTION.class);
    }

    public VideoItem setLongDescription(String description) {
        replaceFirstProperty(new Property.UPNP.LONG_DESCRIPTION(description));
        return this;
    }

    public Person getFirstProducer() {
        return getFirstPropertyValue(Property.UPNP.PRODUCER.class);
    }

    public Person[] getProducers() {
        List<Person> list = getPropertyValues(Property.UPNP.PRODUCER.class);
        return list.toArray(new Person[list.size()]);
    }

    public VideoItem setProducers(Person[] persons) {
        removeProperties(Property.UPNP.PRODUCER.class);
        for (Person p : persons) {
            addProperty(new Property.UPNP.PRODUCER(p));
        }
        return this;
    }

    public String getRating() {
        return getFirstPropertyValue(Property.UPNP.RATING.class);
    }

    public VideoItem setRating(String rating) {
        replaceFirstProperty(new Property.UPNP.RATING(rating));
        return this;
    }

    public PersonWithRole getFirstActor() {
        return getFirstPropertyValue(Property.UPNP.ACTOR.class);
    }

    public PersonWithRole[] getActors() {
        List<PersonWithRole> list = getPropertyValues(Property.UPNP.ACTOR.class);
        return list.toArray(new PersonWithRole[list.size()]);
    }

    public VideoItem setActors(PersonWithRole[] persons) {
        removeProperties(Property.UPNP.ACTOR.class);
        for (PersonWithRole p : persons) {
            addProperty(new Property.UPNP.ACTOR(p));
        }
        return this;
    }

    public Person getFirstDirector() {
        return getFirstPropertyValue(Property.UPNP.DIRECTOR.class);
    }

    public Person[] getDirectors() {
        List<Person> list = getPropertyValues(Property.UPNP.DIRECTOR.class);
        return list.toArray(new Person[list.size()]);
    }

    public VideoItem setDirectors(Person[] persons) {
        removeProperties(Property.UPNP.DIRECTOR.class);
        for (Person p : persons) {
            addProperty(new Property.UPNP.DIRECTOR(p));
        }
        return this;
    }

    public Person getFirstPublisher() {
        return getFirstPropertyValue(Property.DC.PUBLISHER.class);
    }

    public Person[] getPublishers() {
        List<Person> list = getPropertyValues(Property.DC.PUBLISHER.class);
        return list.toArray(new Person[list.size()]);
    }

    public VideoItem setPublishers(Person[] publishers) {
        removeProperties(Property.DC.PUBLISHER.class);
        for (Person publisher : publishers) {
            addProperty(new Property.DC.PUBLISHER(publisher));
        }
        return this;
    }

    public String getLanguage() {
        return getFirstPropertyValue(Property.DC.LANGUAGE.class);
    }

    public VideoItem setLanguage(String language) {
        replaceFirstProperty(new Property.DC.LANGUAGE(language));
        return this;
    }

    public URI getFirstRelation() {
        return getFirstPropertyValue(Property.DC.RELATION.class);
    }

    public URI[] getRelations() {
        List<URI> list = getPropertyValues(Property.DC.RELATION.class);
        return list.toArray(new URI[list.size()]);
    }

    public VideoItem setRelations(URI[] relations) {
        removeProperties(Property.DC.RELATION.class);
        for (URI relation : relations) {
            addProperty(new Property.DC.RELATION(relation));
        }
        return this;
    }

}
