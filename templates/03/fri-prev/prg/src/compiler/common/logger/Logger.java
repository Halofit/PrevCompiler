package compiler.common.logger;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

import compiler.common.report.*;

/**
 * A logger used to generate a log file for a compiler phase.
 * 
 * @author sliva
 */
public class Logger implements AutoCloseable {

	/** The name of the generated log file. */
	private final String xmlFileName;

	/** The name of the style file. */
	private final String xslFileName;

	/** The document representing the entire log. */
	private final Document doc;

	/** The path from the root of the log document to the current node. */
	private final Stack<Element> elements = new Stack<Element>();

	/** The document transformer. */
	private Transformer transformer;

	/**
	 * Constructs a new logger.
	 * 
	 * @param xmlFileName
	 *            The file name of the log file (without phase name and
	 *            <code>.xml</code> extension).
	 * @param xslFileName
	 *            The name of the phase being logged.
	 */
	public Logger(String xmlFileName, String xslFileName) {
		this.xmlFileName = xmlFileName;
		this.xslFileName = xslFileName;
		this.transformer = null;

		// Prepare a new log document.
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new InternalCompilerError();
		}

		// Create the root element representing the entire phase.
		Element phase = doc.createElement("report");
		doc.appendChild(phase);
		elements.push(phase);

		// Add XSL declaration.
		ProcessingInstruction xsl = doc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/xsl\" href=\"" + this.xslFileName + "\"");
		doc.insertBefore(xsl, phase);
	}

	/**
	 * Dumps the entire log document to the log file.
	 */
	@Override
	public void close() {
		try {
			elements.pop();
		} catch (EmptyStackException ex) {
			throw new InternalCompilerError();
		}

		Document transformedDoc = transformer == null ? doc : transformer.transform(doc);

		// Dump the log document out.
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(transformedDoc);
			StreamResult result = new StreamResult(new File(xmlFileName));
			transformer.transform(source, result);
		} catch (TransformerException ex) {
			Report.warning("Cannot generate log file '" + xmlFileName + "'.");
		}
	}
	
	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	/**
	 * Starts constructing a new log element with a specified tag name as a
	 * child of the current log element, and makes the new log element the
	 * current log element.
	 * 
	 * @param tagName
	 *            The tag of the new log element.
	 */
	public void begElement(String tagName) {
		try {
			Element element = doc.createElement(tagName);
			elements.peek().appendChild(element);
			elements.push(element);
		} catch (EmptyStackException ex) {
			throw new InternalCompilerError();
		}
	}

	/**
	 * Adds a new attribute to the current log element.
	 * 
	 * @param attrName
	 *            The attribute name.
	 * @param attrValue
	 *            The attribute value.
	 */
	public void addAttribute(String attrName, String attrValue) {
		try {
			elements.peek().setAttribute(attrName, attrValue);
		} catch (EmptyStackException ex) {
			throw new InternalCompilerError();
		}
	}

	/**
	 * Ends constructing the current log element and makes its parent the
	 * current log element again.
	 */
	public void endElement() {
		try {
			elements.pop();
		} catch (EmptyStackException ex) {
			throw new InternalCompilerError();
		}
	}

}
