/* Copyright (c) 2004 Christopher M Butler

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use,
 copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 Software, and to permit persons to whom the Software is furnished to do so, subject
 to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.flexdock.util;

import org.w3c.dom.Document;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * This class provides {@code static} convenience methods for resource
 * management, including resource lookups and image, icon, and cursor creation.
 *
 * @author Chris Butler
 */
public class ResourceManager {

	private ResourceManager() {
	}

	/**
	 * Performs resource lookups using the {@code ClassLoader} and classpath.
	 * This method attemps to consolidate several techniques used for resource
	 * lookup in different situations, providing a common API that works the
	 * same from standalone applications to applets to multiple-classloader
	 * container-managed applications. Returns {@code null} if specified
	 * resource cannot be found.
	 *
	 * @param uri the String describing the resource to be looked up
	 * @return a {@code URL} representing the resource that has been looked up.
	 */
	public static URL getResource(String uri) {
		if (uri == null) {
			return null;
		}

		URL url = ResourceManager.class.getResource(uri);
		if (url == null) {
			url = ClassLoader.getSystemResource(uri);
		}

		// if we still couldn't find the resource, then slash it and try again
		if (url == null && !uri.startsWith("/")) {
			url = getResource("/" + uri);
		}

		// if resource is still null, then check to see if it's a filesystem
		// path
		if (url == null) {
			try {
				File file = new File(uri);
				if (file.exists()) {
					url = file.toURI().toURL();
				}
			}
			catch (MalformedURLException e) {
				System.err.println("Exception: " + e.getMessage());
				url = null;
			}
		}
		return url;
	}

	/**
	 * Returns an {@code Image} object based on the specified resource URL. Does
	 * not perform any caching on the {@code Image} object, so a new object will
	 * be created with each call to this method.
	 *
	 * @param url the {@code String} describing the resource to be looked up
	 * @return an {@code Image} created from the specified resource URL
	 * @throws NullPointerException if specified resource cannot be found.
	 */
	public static Image createImage(String url) {
		try {
			URL location = getResource(url);
			return Toolkit.getDefaultToolkit().createImage(location);
		}
		catch (NullPointerException e) {
			throw new NullPointerException("Unable to locate image: " + url);
		}
	}

	/**
	 * Returns an {@code ImageIcon} object based on the specified resource URL.
	 * Uses the {@code ImageIcon} constructor internally instead of dispatching
	 * to {@code createImage(String url)}, so {@code Image} objects are cached
	 * via the {@code MediaTracker}.
	 *
	 * @param url the {@code String} describing the resource to be looked up
	 * @return an {@code ImageIcon} created from the specified resource URL
	 * @throws NullPointerException if specified resource cannot be found.
	 */
	public static ImageIcon createIcon(String url) {
		try {
			URL location = getResource(url);
			return new ImageIcon(location);
		}
		catch (NullPointerException e) {
			throw new NullPointerException("Unable to locate image: " + url);
		}
	}

	/**
	 * Returns a {@code Document} object based on the specified resource
	 * {@code uri}. This method resolves a {@code URL} from the specified
	 * {@code String} via {@code getResource(String uri)} and dispatches to
	 * {@code getDocument(URL url)}. If the specified {@code uri} is
	 * {@code null}, then this method returns {@code null}.
	 *
	 * @param uri the {@code String} describing the resource to be looked up
	 * @return a {@code Document} object based on the specified resource
	 * {@code uri}
	 * @see #getResource(String)
	 * @see #getDocument(URL)
	 */
	public static Document getDocument(String uri) {
		URL resource = getResource(uri);
		return getDocument(resource);
	}

	/**
	 * Returns a {@code Document} object based on the specified resource
	 * {@code URL}. This method will open an {@code InputStream} to the
	 * specified {@code URL} and construct a {@code Document} instance. If any
	 * {@code Exceptions} are encountered in the process, this method returns
	 * {@code null}. If the specified {@code URL} is {@code null}, then this
	 * method returns {@code null}.
	 *
	 * @param url the {@code URL} describing the resource to be looked up
	 * @return a {@code Document} object based on the specified resource
	 * {@code URL}
	 */
	private static Document getDocument(URL url) {
		if (url == null) {
			return null;
		}

		InputStream inStream = null;
		try {
			inStream = url.openStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(inStream);
		}
		catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
		finally {
			close(inStream);
		}
		return null;
	}

	/**
	 * Returns a {@code Properties} object based on the specified resource
	 * {@code uri}. This method resolves a {@code URL} from the specified
	 * {@code String} via {@code getResource(String uri)} and dispatches to
	 * {@code getProperties(URL url, boolean failSilent)}, passing the
	 * specified {@code failSilent} parameter. If the specified {@code uri} is
	 * {@code null}, this method will return {@code null}. If
	 * {@code failSilent} is {@code false}, then the ensuing
	 * {@code NullPointerException's} stacktrace will be printed to the
	 * {@code System.err} before returning.
	 *
	 * @param uri        the {@code String} describing the resource to be looked up
	 * @param failSilent {@code true} if no errors are to be reported to the
	 *                   {@code System.err} before returning; {@code false} otherwise.
	 * @return a {@code Properties} object based on the specified resource
	 * {@code uri}.
	 * @see #getResource(String)
	 * @see #getProperties(URL, boolean)
	 */
	public static Properties getProperties(String uri, boolean failSilent) {
		URL url = getResource(uri);
		return getProperties(url, failSilent);
	}

	/**
	 * Returns a {@code Properties} object based on the specified resource
	 * {@code url}. If the specified {@code uri} is {@code null}, this method
	 * will return {@code null}. If any errors are encountered during the
	 * properties-load process, this method will return {@code null}. If
	 * {@code failSilent} is {@code false}, then the any encoutered error
	 * stacktraces will be printed to the {@code System.err} before returning.
	 *
	 * @param url        the {@code URL} describing the resource to be looked up
	 * @param failSilent {@code true} if no errors are to be reported to the
	 *                   {@code System.err} before returning; {@code false} otherwise.
	 * @return a {@code Properties} object based on the specified resource
	 * {@code url}.
	 */
	private static Properties getProperties(URL url, boolean failSilent) {
		if (failSilent && url == null) {
			return null;
		}

		InputStream in = null;
		try {
			in = url.openStream();
			Properties p = new Properties();
			p.load(in);
			return p;
		}
		catch (Exception e) {
			if (!failSilent) {
				System.err.println("Exception: " + e.getMessage());
			}
			return null;
		}
		finally {
			close(in);
		}
	}

	/**
	 * Calls {@code close()} on the specified {@code InputStream}. Any
	 * {@code Exceptions} encountered will be printed to the {@code System.err}.
	 * If {@code in} is {@code null}, then no {@code Exception} is thrown and
	 * no action is taken.
	 *
	 * @param in the {@code InputStream} to close
	 * @see InputStream#close()
	 */
	public static void close(InputStream in) {
		try {
			if (in != null) {
				in.close();
			}
		}
		catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}
}
