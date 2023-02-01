/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package fiji.plugin.trackmate.helper.spt.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML utilities class (parse, read, create and write XML documents).
 * 
 * @author Stephane
 */
public class XMLUtil
{
    public static final String FILE_EXTENSION = "xml";
    public static final String FILE_DOT_EXTENSION = "." + FILE_EXTENSION;

    public static final String NODE_ROOT_NAME = "root";

    private static final String ATTR_NAME_NAME = "name";
    private static final String ATTR_VALUE_NAME = "value";

    // static document builder factory
    private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    // static transformer factory
    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();

    static
    {
        try
        {
            docBuilderFactory.setNamespaceAware(false);
            docBuilderFactory.setValidating(false);
            docBuilderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            docBuilderFactory.setFeature("http://xml.org/sax/features/validation", false);
            docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        }
        catch (final Exception e)
        {
            // ignore this
        }
    }

    // private static synchronized void init()
    // {
    // // initialize static builder
    // if (docBuilder == null)
    // docBuilder = createDocumentBuilder();
    // // initialize static transformer
    // if (transformer == null)
    // transformer = createTransformer();
    // }
    // docBuilder = createDocumentBuilder();
    // transformer = createTransformer();

    /**
     * Create and returns a new DocumentBuilder.
     */
    public static DocumentBuilder createDocumentBuilder()
    {
        try
        {
            return docBuilderFactory.newDocumentBuilder();
        }
        catch (final ParserConfigurationException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create and returns a new Transformer.
     */
    public static Transformer createTransformerSafe() throws TransformerConfigurationException
    {
        final Transformer result = transformerFactory.newTransformer();

        result.setOutputProperty(OutputKeys.METHOD, "xml");
        result.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        result.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        result.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        result.setOutputProperty(OutputKeys.INDENT, "yes");

        return result;
    }

    /**
     * Create and return an empty XML Document.
     */
    public static Document createDocument(final boolean createRoot)
    {
        final DocumentBuilder docBuilder = createDocumentBuilder();

        // an error occurred
        if (docBuilder == null)
            return null;

        // create document
        final Document result = docBuilder.newDocument();

        // add default "root" element if wanted
        if (createRoot)
            createRootElement(result);

        return result;
    }

    /**
     * Parse the specified string and convert it to XML Document (throw an exception if an error occurred).
     * 
     * @throws IOException
     * @throws SAXException
     */
    public static Document createDocument(final String xmlString) throws SAXException, IOException
    {
        final DocumentBuilder docBuilder = createDocumentBuilder();

        // an error occurred
        if (docBuilder == null)
            return null;

        return docBuilder.parse(new InputSource(new StringReader(filterString(xmlString))));
    }

    /**
     * @deprecated Use {@link #createDocument(String)} instead.
     */
    @Deprecated
    public static Document getDocumentSafe(final String xmlString) throws SAXException, IOException
    {
        return createDocument(xmlString);
    }

    /**
     * Load XML Document from specified file.<br>
     * Return null if no document can be loaded.
     */
    public static Document loadDocument(final File f)
    {
        return loadDocument(f, false);
    }

    /**
	 * Load XML Document from specified file.<br>
	 * Return null if no document can be loaded.
	 */
	public static Document loadDocument( final File f, final boolean showError )
	{
		if ( ( f == null ) || !f.exists() )
		{
			if ( showError )
				System.err.println( "XMLUtil.loadDocument('" + f + "') error: file not found !" );

			return null;
		}

		try
		{
			final FileInputStream is = new FileInputStream( f );

			try
			{
				return loadDocument( is );
			}
			finally
			{
				try
				{
					is.close();
				}
				catch ( final Exception e )
				{
					// ignore
				}
			}
		}
		catch ( final Exception e )
		{
			if ( showError )
			{
				System.err.println( "XMLUtil.loadDocument('" + f.getPath() + "') error:" );
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Load XML Document from specified InputStream.<br>
	 * Return null if no document can be loaded.
	 */
    public static Document loadDocument(final InputStream is)
    {
        final DocumentBuilder builder = createDocumentBuilder();

        if (builder != null)
        {
            try
            {
                return builder.parse(is);
            }
            catch (final Exception e)
            {
                System.err.println("XMLUtil.loadDocument('" + is.toString() + "') error :");
				e.printStackTrace();
            }
        }

        return null;
    }

	/**
	 * Create and returns a new Transformer.
	 */
	public static Transformer createTransformer()
	{
		final Transformer result;

		try
		{
			result = transformerFactory.newTransformer();
		}
		catch ( final TransformerConfigurationException e )
		{
			e.printStackTrace();
			return null;
		}

		result.setOutputProperty( OutputKeys.METHOD, "xml" );
		result.setOutputProperty( OutputKeys.ENCODING, "ISO-8859-1" );
		result.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
		result.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
		result.setOutputProperty( OutputKeys.INDENT, "yes" );

		return result;
	}

	public static boolean saveDocument( final Document doc, final File f )
	{
		if ( ( doc == null ) || ( f == null ) )
		{
			System.err.println( "XMLUtil.saveDocument(...) error: specified document or file is null !" );

			return false;
		}

		final Transformer transformer = createTransformer();

		// an error occurred
		if ( transformer == null )
			return false;

		doc.normalizeDocument();

		final DocumentType doctype = doc.getDoctype();
		final DOMSource domSource = new DOMSource( doc );
		final StreamResult streamResult = new StreamResult( f.getAbsolutePath() );

		try
		{
			if ( doctype != null )
			{
				transformer.setOutputProperty( OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId() );
				transformer.setOutputProperty( OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId() );
			}

			transformer.transform( domSource, streamResult );

			return true;
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}

		return false;
	}

    /**
     * Return the XML String from the specified document.
     * 
     * @throws TransformerException
     */
    public static String getXMLString(final Document document) throws TransformerException
    {
        if (document == null)
            return "";

        final Transformer transformer = createTransformerSafe();
        final StringWriter writer = new StringWriter();

        transformer.transform(new DOMSource(document), new StreamResult(writer));

        return writer.toString();
    }

    /**
     * Create root element for specified document if it does not already exist and return it
     */
    public static Element createRootElement(final Document doc)
    {
        return createRootElement(doc, NODE_ROOT_NAME);
    }

    /**
     * Create root element for specified document if it does not already exist and return it
     */
    public static Element createRootElement(final Document doc, final String name)
    {
        return getRootElement(doc, true, name);
    }

    /**
     * Return the root element for specified document<br>
     * Create if it does not already exist with the specified name
     */
    private static Element getRootElement(final Document doc, final boolean create, final String name)
    {
        if (doc != null)
        {
            Element result = doc.getDocumentElement();

            if ((result == null) && create)
            {
                result = doc.createElement(name);
                doc.appendChild(result);
            }

            return result;
        }

        return null;
    }

    /**
     * Return the root element for specified document<br>
     * Create if it does not already exist with the default {@link #NODE_ROOT_NAME}
     */
    public static Element getRootElement(final Document doc, final boolean create)
    {
        return getRootElement(doc, create, NODE_ROOT_NAME);
    }

    /**
     * Return the root element for specified document (null if not found)<br>
     */
    public static Element getRootElement(final Document doc)
    {
        return getRootElement(doc, false);
    }

    /**
     * Get parent element of specified element
     */
    public static Element getParentElement(final Element element)
    {
        Node parent = element.getParentNode();

        while (parent != null)
        {
            if (parent instanceof Element)
                return (Element) parent;

            parent = parent.getParentNode();
        }

        return null;
    }

    /**
     * Get all child node of specified node.
     */
    @SuppressWarnings("null")
    public static ArrayList<Node> getChildren(final Node node)
    {
        final ArrayList<Node> result = new ArrayList<Node>();
        int tries = 3;
        RuntimeException exception = null;

        // sometime the XML library fails so we make several attempts
        while (tries > 0)
        {
            try
            {
                final NodeList nodeList = node.getChildNodes();

                if (nodeList != null)
                {
                    for (int i = 0; i < nodeList.getLength(); i++)
                    {
                        final Node n = nodeList.item(i);

                        if (n != null)
                            result.add(n);
                    }
                }

                return result;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * Get the first child node with specified name from node.<br>
     * Return null if not found.
     */
    @SuppressWarnings("null")
    public static Node getChild(final Node node, final String name)
    {
        int tries = 3;
        RuntimeException exception = null;

        // have to make several attempts as sometime XML library fails to correctly retrieve XML data
        while (tries > 0)
        {
            final NodeList nodeList = node.getChildNodes();

            try
            {
                if (nodeList != null)
                {
                    for (int i = 0; i < nodeList.getLength(); i++)
                    {
                        final Node n = nodeList.item(i);

                        if ((n != null) && n.getNodeName().equals(name))
                            return n;
                    }
                }

                return null;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * Get all child nodes with specified name from node.
     */
    @SuppressWarnings("null")
    public static ArrayList<Node> getChildren(final Node node, final String name)
    {
        final ArrayList<Node> result = new ArrayList<Node>();
        int tries = 3;
        RuntimeException exception = null;

        // have to make several attempts as sometime XML library fails to correctly retrieve XML data
        while (tries > 0)
        {
            final NodeList nodeList = node.getChildNodes();

            try
            {
                if (nodeList != null)
                {
                    for (int i = 0; i < nodeList.getLength(); i++)
                    {
                        final Node n = nodeList.item(i);

                        if ((n != null) && n.getNodeName().equals(name))
                            result.add(n);
                    }
                }

                return result;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * @deprecated Use {@link #getChildren(Node)} instead.
     */
    @Deprecated
    public static ArrayList<Node> getSubNodes(final Node node)
    {
        return getChildren(node);
    }

    /**
     * @deprecated Use {@link #getChild(Node, String)} instead.
     */
    @Deprecated
    public static Node getSubNode(final Node node, final String name)
    {
        return getChild(node, name);
    }

    /**
     * @deprecated Use {@link #getChildren(Node, String)} instead.
     */
    @Deprecated
    public static ArrayList<Node> getSubNodes(final Node node, final String name)
    {
        return getChildren(node, name);
    }

    /**
     * Get all child element of specified node.
     */
    @SuppressWarnings("null")
    public static ArrayList<Element> getElements(final Node node)
    {
        final ArrayList<Element> result = new ArrayList<Element>();
        int tries = 3;
        RuntimeException exception = null;

        // have to make several attempts as sometime XML library fails to correctly retrieve XML data
        while (tries > 0)
        {
            final NodeList nodeList = node.getChildNodes();

            try
            {
                if (nodeList != null)
                {
                    for (int i = 0; i < nodeList.getLength(); i++)
                    {
                        final Node n = nodeList.item(i);

                        if (n instanceof Element)
                            result.add((Element) n);
                    }
                }

                return result;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * Get the first child element with specified name from node.<br>
     * Return null if not found.
     */
    @SuppressWarnings("null")
    public static Element getElement(final Node node, final String name)
    {
        if (node == null)
            return null;

        final String filteredName = filterString(name);
        int tries = 3;
        RuntimeException exception = null;

        // have to make several attempts as sometime XML library fails to correctly retrieve XML data
        while (tries > 0)
        {
            try
            {
                final NodeList nodeList = node.getChildNodes();

                if (nodeList != null)
                {
                    for (int i = 0; i < nodeList.getLength(); i++)
                    {
                        final Node n = nodeList.item(i);

                        if ((n instanceof Element) && n.getNodeName().equals(filteredName))
                            return (Element) n;
                    }
                }

                return null;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * Get all child element with specified name of specified node.
     */
    @SuppressWarnings("null")
    public static ArrayList<Element> getElements(final Node node, final String name)
    {
        final ArrayList<Element> result = new ArrayList<Element>();
        final String filteredName = filterString(name);
        int tries = 3;
        RuntimeException exception = null;

        // have to make several attempts as sometime XML library fails to correctly retrieve XML data
        while (tries > 0)
        {
            final NodeList nodeList = node.getChildNodes();

            try
            {
                if (nodeList != null)
                {
                    for (int i = 0; i < nodeList.getLength(); i++)
                    {
                        final Node n = nodeList.item(i);

                        if ((n instanceof Element) && n.getNodeName().equals(filteredName))
                            result.add((Element) n);
                    }
                }
                return result;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * @deprecated Use {@link #getElements(Node)} instead.
     */
    @Deprecated
    public static ArrayList<Element> getSubElements(final Node node)
    {
        return getElements(node);
    }

    /**
     * @deprecated Use {@link #getElement(Node, String)} instead.
     */
    @Deprecated
    public static Element getSubElement(final Node node, final String name)
    {
        return getElement(node, name);
    }

    /**
     * @deprecated Use {@link #getElements(Node, String)} instead.
     */
    @Deprecated
    public static ArrayList<Element> getSubElements(final Node node, final String name)
    {
        return getElements(node, name);
    }

    /**
     * Get all child element with specified type (name) from specified node.
     */
    @SuppressWarnings("null")
    public static ArrayList<Element> getGenericElements(final Node node, final String type)
    {
        final ArrayList<Element> result = new ArrayList<Element>();
        final String filteredType = filterString(type);
        int tries = 3;
        RuntimeException exception = null;

        // have to make several attempts as sometime XML library fails to correctly retrieve XML data
        while (tries > 0)
        {
            final NodeList nodeList = node.getChildNodes();

            try
            {
                if (nodeList != null)
                {
                    for (int i = 0; i < nodeList.getLength(); i++)
                    {
                        final Node n = nodeList.item(i);

                        if ((n instanceof Element) && n.getNodeName().equals(filteredType))
                            result.add((Element) n);
                    }
                }

                return result;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * Get all child element with specified type (name) and name ('name attribute value')
     * from specified node.
     */
    @SuppressWarnings("null")
    public static ArrayList<Element> getGenericElements(final Node node, final String type, final String name)
    {
        final ArrayList<Element> result = new ArrayList<Element>();
        final String filteredName = filterString(name);
        final String filteredType = filterString(type);
        int tries = 3;
        RuntimeException exception = null;

        // have to make several attempts as sometime XML library fails to correctly retrieve XML data
        while (tries > 0)
        {
            final NodeList nodeList = node.getChildNodes();

            try
            {
                if (nodeList != null)
                {
                    for (int i = 0; i < nodeList.getLength(); i++)
                    {
                        final Node n = nodeList.item(i);

                        if ((n instanceof Element) && n.getNodeName().equals(filteredType))
                        {
                            final Element element = (Element) n;

                            if (element.getAttribute(ATTR_NAME_NAME).equals(filteredName))
                                result.add(element);
                        }
                    }
                }

                return result;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * @deprecated Use {@link #getGenericElements(Node, String)} instead.
     */
    @Deprecated
    public static ArrayList<Element> getSubGenericElements(final Node node, final String type)
    {
        return getGenericElements(node, type);
    }

    /**
     * @deprecated Use {@link #getGenericElements(Node, String, String)} instead.
     */
    @Deprecated
    public static ArrayList<Element> getSubGenericElements(final Node node, final String type, final String name)
    {
        return getGenericElements(node, type, name);
    }

    /**
     * Get child element with specified type (name) and name ('name attribute value')
     * from specified node.
     */
    @SuppressWarnings("null")
    public static Element getGenericElement(final Node node, final String type, final String name)
    {
        final String filteredName = filterString(name);
        final String filteredType = filterString(type);
        int tries = 3;
        RuntimeException exception = null;

        // have to make several attempts as sometime XML library fails to correctly retrieve XML data
        while (tries > 0)
        {
            final NodeList nodeList = node.getChildNodes();

            try
            {
                if (nodeList != null)
                {
                    for (int i = 0; i < nodeList.getLength(); i++)
                    {
                        final Node n = nodeList.item(i);

                        if ((n instanceof Element) && n.getNodeName().equals(filteredType))
                        {
                            final Element element = (Element) n;

                            if (element.getAttribute(ATTR_NAME_NAME).equals(filteredName))
                                return element;
                        }
                    }
                }

                return null;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * Get name of specified generic element
     */
    public static String getGenericElementName(final Element element)
    {
        if (element != null)
            return element.getAttribute(ATTR_NAME_NAME);

        return "";
    }

    /**
     * Get value of specified generic element
     */
    public static String getGenericElementValue(final Element element, final String def)
    {
        return getAttributeValue(element, ATTR_VALUE_NAME, def);
    }

    /**
     * Get all attributes of the specified element
     */
    public static ArrayList<Attr> getAllAttributes(final Element element)
    {
        final NamedNodeMap nodeMap = element.getAttributes();
        final ArrayList<Attr> result = new ArrayList<Attr>();

        for (int i = 0; i < nodeMap.getLength(); i++)
            result.add((Attr) nodeMap.item(i));

        return result;
    }

	private static boolean getBoolean( final String value, final boolean def )
	{
		return StringUtil.parseBoolean( value, def );
	}

	private static int getInt( final String value, final int def )
	{
		return StringUtil.parseInt( value, def );
	}

	private static long getLong( final String value, final long def )
	{
		return StringUtil.parseLong( value, def );
	}

	private static float getFloat( final String value, final float def )
	{
		return StringUtil.parseFloat( value, def );
	}

	private static double getDouble( final String value, final double def )
	{
		return StringUtil.parseDouble( value, def );
	}

	private static String toString( final boolean value )
	{
		return StringUtil.toString( value );
	}

	private static String toString( final int value )
	{
		return StringUtil.toString( value );
	}

	private static String toString( final long value )
	{
		return StringUtil.toString( value );
	}

	private static String toString( final float value )
	{
		return StringUtil.toString( value );
	}

	private static String toString( final double value )
	{
		return StringUtil.toString( value );
	}

    /**
     * Get an attribute from the specified Element
     */
    public static Attr getAttribute(final Element element, final String attribute)
    {
        if (element != null)
            return element.getAttributeNode(attribute);

        return null;
    }

    /**
     * Get attribute value from the specified Element.<br>
     * If no attribute found 'def' value is returned.
     */
    @SuppressWarnings("null")
    public static String getAttributeValue(final Element element, final String attribute, final String def)
    {
        if (element == null)
            return def;

        final String filteredAttr = filterString(attribute);
        int tries = 3;
        RuntimeException exception = null;

        // sometime the XML library fails so we make several attempts
        while (tries > 0)
        {
            try
            {
                final Attr attr = element.getAttributeNode(filteredAttr);

                if (attr != null)
                    return attr.getValue();

                return def;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * Get attribute value as Boolean from the specified Element.<br>
     * If no attribute found 'def' value is returned.
     */
    public static boolean getAttributeBooleanValue(final Element element, final String attribute, final boolean def)
    {
        return getBoolean(getAttributeValue(element, attribute, ""), def);
    }

    /**
     * Get attribute value as integer from the specified Element.<br>
     * If no attribute found 'def' value is returned.
     */
    public static int getAttributeIntValue(final Element element, final String attribute, final int def)
    {
        return getInt(getAttributeValue(element, attribute, ""), def);
    }

    /**
     * Get attribute value as long from the specified Element.<br>
     * If no attribute found 'def' value is returned.
     */
    public static long getAttributeLongValue(final Element element, final String attribute, final long def)
    {
        return getLong(getAttributeValue(element, attribute, ""), def);
    }

    /**
     * Get attribute value as float from the specified Element.<br>
     * If no attribute found 'def' value is returned.
     */
    public static float getAttributeFloatValue(final Element element, final String attribute, final float def)
    {
        return getFloat(getAttributeValue(element, attribute, ""), def);
    }

    /**
     * Get attribute value as double from the specified Element.<br>
     * If no attribute found 'def' value is returned.
     */
    public static double getAttributeDoubleValue(final Element element, final String attribute, final double def)
    {
        return getDouble(getAttributeValue(element, attribute, ""), def);
    }

    /**
     * Get first value (value of first child) from the specified Element.<br>
     * If no value found 'def' value is returned.
     */
    @SuppressWarnings("null")
    public static String getFirstValue(final Element element, final String def)
    {
        if (element == null)
            return def;

        int tries = 3;
        RuntimeException exception = null;

        // sometime the XML library fails so we make several attempts
        while (tries > 0)
        {
            try
            {
                final Node child = element.getFirstChild();

                if (child != null)
                    return child.getNodeValue();

                return def;
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * Get all values (value of all child) from the specified Element.<br>
     * If no value found 'def' value is returned.
     */
    @SuppressWarnings("null")
    public static String getAllValues(final Element element, final String def)
    {
        if (element == null)
            return def;

        int tries = 3;
        RuntimeException exception = null;

        // sometime the XML library fails so we make several attempts
        while (tries > 0)
        {
            try
            {
                final StringBuilder str = new StringBuilder();

                Node child = element.getFirstChild();
                while (child != null)
                {
                    str.append(child.getNodeValue());
                    child = child.getNextSibling();
                }

                return str.toString();
            }
            catch (final RuntimeException e)
            {
                // try again
                exception = e;
                tries--;
            }
        }

        throw exception;
    }

    /**
     * Get all values (value of all child) as String from the specified Element.<br>
     * If no value found 'def' value is returned.
     */
    public static String getValue(final Element element, final String def)
    {
        return getAllValues(element, def);
    }

    /**
     * Get all values (value of all child) as Boolean from the specified Element.
     */
    public static boolean getBooleanValue(final Element element, final boolean def)
    {
        return getBoolean(getFirstValue(element, ""), def);
    }

    /**
     * Get value as integer from the specified Element.<br>
     * If no integer value found 'def' value is returned.
     */
    public static int getIntValue(final Element element, final int def)
    {
        return getInt(getFirstValue(element, ""), def);
    }

    /**
     * Get value as long from the specified Element.<br>
     * If no integer value found 'def' value is returned.
     */
    public static long getLongValue(final Element element, final long def)
    {
        return getLong(getFirstValue(element, ""), def);
    }

    /**
     * Get value as float from the specified Element.<br>
     * If no float value found 'def' value is returned.
     */
    public static float getFloatValue(final Element element, final float def)
    {
        return getFloat(getFirstValue(element, ""), def);
    }

    /**
     * Get value as double from the specified Element.<br>
     * If no double value found 'def' value is returned.
     */
    public static double getDoubleValue(final Element element, final double def)
    {
        return getDouble(getFirstValue(element, ""), def);
    }

    /**
     * Get first element value from the specified node.<br>
     * If no value found 'def' value is returned.
     */
    public static String getElementFirstValue(final Node node, final String name, final String def)
    {
        return getFirstValue(getElement(node, name), def);
    }

    /**
     * Get all element values from the specified node.<br>
     * If no value found 'def' value is returned.
     */
    public static String getElementAllValues(final Node node, final String name, final String def)
    {
        return getAllValues(getElement(node, name), def);
    }

    /**
     * Get element value as string from the specified node.<br>
     * If no value found 'def' value is returned.
     */
    public static String getElementValue(final Node node, final String name, final String def)
    {
        return getValue(getElement(node, name), def);
    }

    /**
     * Get element value as boolean from the specified node.
     */
    public static boolean getElementBooleanValue(final Node node, final String name, final boolean def)
    {
        return getBoolean(getElementValue(node, name, ""), def);
    }

    /**
     * Get element value as integer from the specified node.<br>
     * If no integer value found 'def' value is returned.
     */
    public static int getElementIntValue(final Node node, final String name, final int def)
    {
        return getInt(getElementValue(node, name, ""), def);
    }

    /**
     * Get element value as long from the specified node.<br>
     * If no integer value found 'def' value is returned.
     */
    public static long getElementLongValue(final Node node, final String name, final long def)
    {
        return getLong(getElementValue(node, name, ""), def);
    }

    /**
     * Get element value as float from the specified node.<br>
     * If no float value found 'def' value is returned.
     */
    public static float getElementFloatValue(final Node node, final String name, final float def)
    {
        return getFloat(getElementValue(node, name, ""), def);
    }

    /**
     * Get element value as double from the specified node.<br>
     * If no double value found 'def' value is returned.
     */
    public static double getElementDoubleValue(final Node node, final String name, final double def)
    {
        return getDouble(getElementValue(node, name, ""), def);
    }

    /**
     * Get value ('value' attribute value) from element with specified type
     * and name ('name' attribute value).<br>
     * If no value found 'def' value is returned.
     */
    public static String getGenericElementValue(final Node node, final String type, final String name, final String def)
    {
        return getGenericElementValue(getGenericElement(node, type, name), def);
    }

    /**
     * Get value ('value' attribute value) as boolean from element with specified type
     * and name ('name' attribute value).<br>
     * If no byte array value found 'def' value is returned.
     */
    public static boolean getGenericElementBooleanValue(final Node node, final String type, final String name, final boolean def)
    {
        return getBoolean(getGenericElementValue(node, type, name, ""), def);
    }

    /**
     * Get value ('value' attribute value) as integer from element with specified type
     * and name ('name' attribute value).<br>
     * If no integer value found 'def' value is returned.
     */
    public static int getGenericElementIntValue(final Node node, final String type, final String name, final int def)
    {
        return getInt(getGenericElementValue(node, type, name, ""), def);
    }

    /**
     * Get value ('value' attribute value) as long from element with specified type
     * and name ('name' attribute value).<br>
     * If no integer value found 'def' value is returned.
     */
    public static long getGenericElementLongValue(final Node node, final String type, final String name, final long def)
    {
        return getLong(getGenericElementValue(node, type, name, ""), def);
    }

    /**
     * Get value ('value' attribute value) as float from element with specified type
     * and name ('name' attribute value).<br>
     * If no float value found 'def' value is returned.
     */
    public static float getGenericElementFloatValue(final Node node, final String type, final String name, final float def)
    {
        return getFloat(getGenericElementValue(node, type, name, ""), def);
    }

    /**
     * Get value ('value' attribute value) as double from element with specified type
     * and name ('name' attribute value).<br>
     * If no double value found 'def' value is returned.
     */
    public static double getGenericElementDoubleValue(final Node node, final String type, final String name, final double def)
    {
        return getDouble(getGenericElementValue(node, type, name, ""), def);
    }

    /**
     * Add the specified node to specified parent node
     */
    public static Node addNode(final Node parent, final Node node)
    {
        return parent.appendChild(node);
    }

    /**
     * Add a value to the specified node
     */
    public static Node addValue(final Node node, final String value)
    {
        final Node newNode;
        final String filteredValue = filterString(value);

        if (node instanceof Document)
            newNode = ((Document) node).createTextNode(filteredValue);
        else
            newNode = node.getOwnerDocument().createTextNode(filteredValue);

        if (newNode != null)
            node.appendChild(newNode);

        return newNode;
    }

    /**
     * Add a named element to the specified node
     */
    public static Element addElement(final Node node, final String name)
    {
        final Element element;
        final String filteredName = filterString(name);

        if (node instanceof Document)
            element = ((Document) node).createElement(filteredName);
        else
            element = node.getOwnerDocument().createElement(filteredName);

        node.appendChild(element);

        return element;
    }

    /**
     * Add a named element with a value to the specified node
     */
    public static Element addElement(final Node node, final String name, final String value)
    {
        final Element element = addElement(node, name);

        if (!StringUtil.isEmpty(value))
            addValue(element, value);

        return element;
    }

    /**
     * Add a generic element with specified type and name to the specified node
     */
    public static Element addGenericElement(final Node node, final String type, final String name)
    {
        final Element element = addElement(node, type);

        setGenericElementName(element, name);

        return element;
    }

    /**
     * Add a generic element with specified type, name and value to the specified node
     */
    public static Element addGenericElement(final Node node, final String type, final String name, final String value)
    {
        final Element element = addElement(node, type);

        setGenericElementName(element, name);
        setGenericElementValue(element, value);

        return element;
    }

    /**
     * Set name of specified generic element
     */
    public static void setGenericElementName(final Element element, final String name)
    {
        if (element != null)
            element.setAttribute(ATTR_NAME_NAME, filterString(name));
    }

    /**
     * Set value of specified generic element
     */
    public static void setGenericElementValue(final Element element, final String value)
    {
        if (element != null)
            element.setAttribute(ATTR_VALUE_NAME, filterString(value));
    }

    /**
     * Set the specified node to the specified parent node.<br>
     * The new node replace the previous existing node with the same name.
     */
    public static Node setNode(final Node parent, final Node node)
    {
        final String name = node.getNodeName();

        XMLUtil.removeNode(parent, name);

        return XMLUtil.addNode(parent, node);
    }

    /**
     * Set a element with specified name to specified node.<br>
     * If the Element was already existing then it's just returned.
     */
    public static Element setElement(final Node node, final String name)
    {
        // get element
        final Element element = getElement(node, name);
        if (element != null)
            return element;

        return addElement(node, name);
    }

    /**
     * Set a generic element with specified type and name to specified node.<br>
     * If the generic element was already existing then it's just returned.
     */
    public static Element setGenericElement(final Node node, final String type, final String name)
    {
        // get generic element
        final Element element = getGenericElement(node, type, name);
        if (element != null)
            return element;

        return addGenericElement(node, type, name);
    }

    /**
     * Set an attribute and his value to the specified node
     */
    public static void setAttributeValue(final Element element, final String attribute, final String value)
    {
        element.setAttribute(attribute, filterString(value));
    }

    /**
     * Set an attribute and his value as boolean to the specified node
     */
    public static void setAttributeBooleanValue(final Element element, final String attribute, final boolean value)
    {
        setAttributeValue(element, attribute, toString(value));
    }

    /**
     * Set an attribute and his value as integer to the specified node
     */
    public static void setAttributeIntValue(final Element element, final String attribute, final int value)
    {
        setAttributeValue(element, attribute, toString(value));
    }

    /**
     * Set an attribute and his value as integer to the specified node
     */
    public static void setAttributeLongValue(final Element element, final String attribute, final long value)
    {
        setAttributeValue(element, attribute, toString(value));
    }

    /**
     * Set an attribute and his value as float to the specified node
     */
    public static void setAttributeFloatValue(final Element element, final String attribute, final float value)
    {
        setAttributeValue(element, attribute, toString(value));
    }

    /**
     * Set an attribute and his value as double to the specified node
     */
    public static void setAttributeDoubleValue(final Element element, final String attribute, final double value)
    {
        setAttributeValue(element, attribute, toString(value));
    }

    /**
     * Set value to the specified element
     */
    public static void setValue(final Element element, final String value)
    {
        // remove child nodes
        removeAllChildren(element);
        // add value
        addValue(element, value);
    }

    /**
     * Remove all characters that are valid XML markups.
     */
    public static String removeXMLMarkups(final String s)
    {
        final StringBuffer out = new StringBuffer();

        for (final char c : s.toCharArray())
        {
            if ((c == '\'') || (c == '<') || (c == '>') || (c == '&') || (c == '\"'))
                continue;

            out.append(c);
        }

        return out.toString();
    }

    /**
     * Remove any invalid XML character from the specified string.
     */
    public static String removeInvalidXMLCharacters(final String text)
    {
        if (text == null)
            return "";

        final String xml10pattern = "[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD"
                + "\ud800\udc00-\udbff\udfff" + "]";
        // final String xml11pattern = "[^" + "\u0001-\uD7FF" + "\uE000-\uFFFD" +
        // "\ud800\udc00-\udbff\udfff" + "]+";

        // some OME generate incorrect "&#" sequence so we just replace them with "#"
        return text.replaceAll(xml10pattern, "").replaceAll("&#", "#");
    }

    /**
     * Same as {@link #removeInvalidXMLCharacters(String)}
     */
    public static String filterString(final String text)
    {
        return removeInvalidXMLCharacters(text);
    }

    /**
     * Set value as boolean to the specified element
     */
    public static void setBooleanValue(final Element element, final boolean value)
    {
        setValue(element, toString(value));
    }

    /**
     * Set value as integer to the specified element
     */
    public static void setIntValue(final Element element, final int value)
    {
        setValue(element, toString(value));
    }

    /**
     * Set value as long to the specified element
     */
    public static void setLongValue(final Element element, final long value)
    {
        setValue(element, toString(value));
    }

    /**
     * Set value as float to the specified element
     */
    public static void setFloatValue(final Element element, final float value)
    {
        setValue(element, toString(value));
    }

    /**
     * Set value as double to the specified element
     */
    public static void setDoubleValue(final Element element, final double value)
    {
        setValue(element, toString(value));
    }

    /**
     * Set an element with specified name and his value to the specified node
     */
    public static void setElementValue(final Node node, final String name, final String value)
    {
        // get element (create it if needed)
        final Element element = setElement(node, name);
        // set value
        setValue(element, value);
    }

    /**
     * Set an element with specified name and his value as boolean to the specified node
     */
    public static void setElementBooleanValue(final Node node, final String name, final boolean value)
    {
        setElementValue(node, name, toString(value));
    }

    /**
     * Set an element with specified name and his value as integer to the specified node
     */
    public static void setElementIntValue(final Node node, final String name, final int value)
    {
        setElementValue(node, name, toString(value));
    }

    /**
     * Set an element with specified name and his value as long to the specified node
     */
    public static void setElementLongValue(final Node node, final String name, final long value)
    {
        setElementValue(node, name, toString(value));
    }

    /**
     * Set an element with specified name and his value as float to the specified node
     */
    public static void setElementFloatValue(final Node node, final String name, final float value)
    {
        setElementValue(node, name, toString(value));
    }

    /**
     * Set an element with specified name and his value as double to the specified node
     */
    public static void setElementDoubleValue(final Node node, final String name, final double value)
    {
        setElementValue(node, name, toString(value));
    }

    /**
     * Set a generic element with specified type and name and his value to the specified node
     */
    public static void setGenericElementValue(final Node node, final String type, final String name, final String value)
    {
        // get generic element (create it if needed)
        final Element element = setGenericElement(node, type, name);

        if (element != null)
            element.setAttribute(ATTR_VALUE_NAME, value);
    }

    /**
     * Set an element with specified type and name and his value as boolean to the specified node
     */
    public static void setGenericElementBooleanValue(final Node node, final String type, final String name, final boolean value)
    {
        setGenericElementValue(node, type, name, toString(value));
    }

    /**
     * Set an element with specified type and name and his value as integer to the specified node
     */
    public static void setGenericElementIntValue(final Node node, final String type, final String name, final int value)
    {
        setGenericElementValue(node, type, name, toString(value));
    }

    /**
     * Set an element with specified type and name and his value as long to the specified node
     */
    public static void setGenericElementLongValue(final Node node, final String type, final String name, final long value)
    {
        setGenericElementValue(node, type, name, toString(value));
    }

    /**
     * Set an element with specified type and name and his value as float to the specified node
     */
    public static void setGenericElementFloatValue(final Node node, final String type, final String name, final float value)
    {
        setGenericElementValue(node, type, name, toString(value));
    }

    /**
     * Set an element with specified type and name and his value as double to the specified node
     */
    public static void setGenericElementDoubleValue(final Node node, final String type, final String name, final double value)
    {
        setGenericElementValue(node, type, name, toString(value));
    }

    /**
     * Remove a node with specified name from the specified node
     */
    public static boolean removeNode(final Node node, final String name)
    {
        final Node subNode = getSubNode(node, name);

        if (subNode != null)
            return removeNode(node, subNode);

        return false;
    }

    /**
     * Remove the specified node from the specified parent node
     */
    public static boolean removeNode(final Node parent, final Node child)
    {
        int tries = 3;
        // RuntimeException exception = null;

        // have to make several attempts as sometime XML library fails to correctly retrieve XML data
        while (tries > 0)
        {
            try
            {
                parent.removeChild(child);
                return true;
            }
            catch (final RuntimeException e)
            {
                // exception = e;
                tries--;
            }
        }

        // we just ignore here
        return false;
    }

    /**
     * @deprecated Use {@link #removeAllChildren(Node)} instead
     */
    @Deprecated
    public static void removeAllChilds(final Node node)
    {
        removeAllChildren(node);
    }

    /**
     * Remove all children from the specified node
     */
    public static void removeAllChildren(final Node node)
    {
        while (node.hasChildNodes())
            node.removeChild(node.getLastChild());
    }

    /**
     * @deprecated Use {@link #removeChildren(Node, String)} instead
     */
    @Deprecated
    public static void removeChilds(final Node node, final String name)
    {
        removeChildren(node, name);
    }

    /**
     * Remove all children with specified name from the specified node
     */
    public static void removeChildren(final Node node, final String name)
    {
        Node currentChild = node.getFirstChild();

        while (currentChild != null)
        {
            final Node nextChild = currentChild.getNextSibling();

            if (currentChild.getNodeName().equals(name))
                node.removeChild(currentChild);

            currentChild = nextChild;
        }
    }

    /**
     * Remove an attribute from the specified element
     */
    public static void removeAttribute(final Element element, final String name)
    {
        element.removeAttribute(name);
    }

    /**
     * Remove all attribute from the specified element
     */
    public static void removeAllAttributes(final Element element)
    {
        final NamedNodeMap nodeMap = element.getAttributes();

        for (int i = 0; i < nodeMap.getLength(); i++)
            element.removeAttribute(nodeMap.item(i).getNodeName());
    }
}
