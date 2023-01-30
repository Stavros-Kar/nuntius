/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="client://java.sun.com/xml/jaxb">client://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.27 at 01:16:00 PM WEST 
//

package org.olat.modules.oaipmh.common.oaidc;


import static com.lyncode.xml.matchers.AttributeMatchers.attributeName;
import static com.lyncode.xml.matchers.QNameMatchers.localPart;
import static com.lyncode.xml.matchers.XmlEventMatchers.aStartElement;
import static com.lyncode.xml.matchers.XmlEventMatchers.anElement;
import static com.lyncode.xml.matchers.XmlEventMatchers.anEndElement;
import static com.lyncode.xml.matchers.XmlEventMatchers.elementName;
import static com.lyncode.xml.matchers.XmlEventMatchers.text;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.lyncode.xml.XmlReader;
import com.lyncode.xml.exceptions.XmlReaderException;
import org.olat.core.CoreSpringFactory;
import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;
import org.olat.repository.RepositoryService;

public class Element implements XmlWritable {
	protected List<Field> fields = new ArrayList<>();
	protected String name;
	protected String value;
	protected List<Element> elements = new ArrayList<>();

	RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);

	public Element(String name) {
		this.name = name;
	}

	public static Element parse(XmlReader reader) throws XmlReaderException {
		if (!reader.current(allOf(aStartElement(), elementName(localPart(equalTo("element"))))))
			throw new XmlReaderException("Invalid XML. Expecting entity 'element'");

		if (!reader.hasAttribute(attributeName(localPart(equalTo("name")))))
			throw new XmlReaderException("Invalid XML. Element entities must have a name");

		Element element = new Element(reader.getAttributeValue(localPart(equalTo("name"))));


		while (reader.next(anElement()).current(aStartElement())) {
			if (reader.current(elementName(localPart(equalTo("element"))))) { // Nested element
				element.withElement(parse(reader));
			} else if (reader.next(anElement(), text()).current(text()))
				element.withValue("testValue");
				//element.withField(Field.parse(reader));
			else throw new XmlReaderException("Unexpected element");
		}

		if (!reader.current(allOf(anEndElement(), elementName(localPart(equalTo("element"))))))
			throw new XmlReaderException("Invalid XML. Expecting end of entity 'element'");

		return element;
	}

	public List<Field> getFields() {
		return fields;
	}

	public String getName() {
		return name;
	}

	public Element withName(String value) {
		this.name = value;
		return this;
	}

	public Element withField(Field field) {
		this.fields.add(field);
		return this;
	}

	public Element withValue(String value) {
		this.value = value;
		return this;
	}

	public Element withField(String name, String value) {
		this.value = value;
		//this.fields.add(new Field(value, name));
		return this;
	}

	public List<Element> getElements() {
		return this.elements;
	}

	public Element withElement(Element element) {
		this.elements.add(element);
		return this;
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		try {
			// if (this.name != null)
			//writer.writeAttribute("name", this.getName());

            /*for (RepositoryEntry repositoryEntry : repositoryService.loadRepositoryForMetadata("published")) {

            }*/

            /*for (Element element : this.getElements()) {
                writer.writeStartElement(getName());
                element.write(writer);
                writer.writeCharacters(value);
                writer.writeEndElement();
            }*/

			writer.writeCharacters(value);

            /*for (Field field : this.getFields()) {
                writer.writeStartElement("field");
                field.withValue(repositoryService.loadRepositoryForMetadata("published").get(0).getDisplayname());
                field.write(writer);
                writer.writeEndElement();
            }*/

		} catch (XMLStreamException e) {
			throw new XmlWriteException(e);
		}
	}
}
