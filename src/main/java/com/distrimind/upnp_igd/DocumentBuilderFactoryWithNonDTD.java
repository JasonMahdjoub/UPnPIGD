package com.distrimind.upnp_igd;
/*
Copyright or © or Copr. Jason Mahdjoub (01/04/2013)

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

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since UPNPIGD 1.0.0
 */

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MaDKitLanEdition 5.19.0
 */
public class DocumentBuilderFactoryWithNonDTD extends DocumentBuilderFactory {

	public static DocumentBuilderFactoryWithNonDTD newDocumentBuilderFactoryWithNonDTDInstance()
	{
		return newDocumentBuilderFactoryWithNonDTDInstance(false);
	}
	public static DocumentBuilderFactoryWithNonDTD newDocumentBuilderFactoryWithNonDTDInstance(boolean enableDocType)
	{
		if (enableDocType) {
			try {
				return new DocumentBuilderFactoryWithNonDTD(true);
			} catch (ParserConfigurationException e) {
				return null;
			}
		}
		return (DocumentBuilderFactoryWithNonDTD)DocumentBuilderFactory.newInstance(DocumentBuilderFactoryWithNonDTD.class.getName(), ClassLoader.getSystemClassLoader());
	}
	private final DocumentBuilderFactory base;
	public DocumentBuilderFactoryWithNonDTD() throws ParserConfigurationException {
		this(false);
	}
	public DocumentBuilderFactoryWithNonDTD(boolean enableDocType) throws ParserConfigurationException {
		base=DocumentBuilderFactory.newInstance();
		base.setValidating(false);
		base.setNamespaceAware(false);
		base.setCoalescing(false);
		base.setFeature("http://xml.org/sax/features/validation", false);
		if (!enableDocType)
			base.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		base.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		base.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		base.setFeature("http://xml.org/sax/features/external-general-entities", false);
		base.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		base.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	}

	@Override
	public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return base.newDocumentBuilder();
	}

	@Override
	public void setAttribute(String name, Object value) throws IllegalArgumentException {
		base.setAttribute(name, value);
	}

	@Override
	public Object getAttribute(String name) throws IllegalArgumentException {
		return base.getAttribute(name);
	}

	@Override
	public void setFeature(String name, boolean value) throws ParserConfigurationException {
		base.setFeature(name, value);
	}

	@Override
	public boolean getFeature(String name) throws ParserConfigurationException {
		return base.getFeature(name);
	}

	public static DocumentBuilderFactory newInstance() {
		return DocumentBuilderFactory.newInstance();
	}

	public static DocumentBuilderFactory newInstance(String factoryClassName, ClassLoader classLoader) {
		return DocumentBuilderFactory.newInstance(factoryClassName, classLoader);
	}

	@Override
	public void setNamespaceAware(boolean awareness) {
		base.setNamespaceAware(awareness);
	}

	@Override
	public void setValidating(boolean validating) {
		base.setValidating(validating);
	}

	@Override
	public void setIgnoringElementContentWhitespace(boolean whitespace) {
		base.setIgnoringElementContentWhitespace(whitespace);
	}

	@Override
	public void setExpandEntityReferences(boolean expandEntityRef) {
		base.setExpandEntityReferences(expandEntityRef);
	}

	@Override
	public void setIgnoringComments(boolean ignoreComments) {
		base.setIgnoringComments(ignoreComments);
	}

	@Override
	public void setCoalescing(boolean coalescing) {
		base.setCoalescing(coalescing);
	}

	@Override
	public boolean isNamespaceAware() {
		return base.isNamespaceAware();
	}

	@Override
	public boolean isValidating() {
		return base.isValidating();
	}

	@Override
	public boolean isIgnoringElementContentWhitespace() {
		return base.isIgnoringElementContentWhitespace();
	}

	@Override
	public boolean isExpandEntityReferences() {
		return base.isExpandEntityReferences();
	}

	@Override
	public boolean isIgnoringComments() {
		return base.isIgnoringComments();
	}

	@Override
	public boolean isCoalescing() {
		return base.isCoalescing();
	}

	@Override
	public Schema getSchema() {
		return base.getSchema();
	}

	@Override
	public void setSchema(Schema schema) {
		base.setSchema(schema);
	}

	@Override
	public void setXIncludeAware(boolean state) {
		base.setXIncludeAware(state);
	}

	@Override
	public boolean isXIncludeAware() {
		return base.isXIncludeAware();
	}
}
