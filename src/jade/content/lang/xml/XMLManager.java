/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content.lang.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import jade.content.ContentException;
import jade.content.onto.BasicOntology;
import jade.content.onto.BeanOntology;
import jade.content.onto.Introspector;
import jade.content.onto.Ontology;

/**
 * Utility class to transform Java objects (beans) to/from XML. 
 */
public class XMLManager {

	private BeanOntology myOntology;
	private Ontology[] superOntologies;
	private XMLCodec codec = new XMLCodec();
	
	public XMLManager() {
		superOntologies = new Ontology[2];
		superOntologies[0] = new Ontology("placeholder", (Introspector) null);
		superOntologies[1] = BasicOntology.getInstance();
		myOntology = new BeanOntology("dummy", superOntologies);
	}
	
	public XMLManager(String packageName) throws ContentException {
		this();
		myOntology.add(packageName);
	}
	
	public XMLManager(Ontology onto) {
		this();
		superOntologies[0] = onto;
	}
	
	public void add(String packageName) throws ContentException {
		myOntology.add(packageName);
	}
	
	public void add(Class<?> c) throws ContentException {
		myOntology.add(c);
	}
	
	public Ontology getOntology() {
		return myOntology;
	}
	
	public Object decode(String xml) throws ContentException {
		return codec.decodeObject(myOntology, xml);
	}
	
	public Object decode(File xmlFile) throws ContentException, IOException {
		return codec.decodeObject(myOntology, getFileContent(xmlFile));
	}
	
	public Object decode(InputStream xmlStream) throws ContentException, IOException {
		return codec.decodeObject(myOntology, getStreamContent(xmlStream));
	}
	
	public String encode(Object obj) throws ContentException {
		return codec.encodeObject(myOntology, obj, true);
	}
	
	public void encodeToFile(Object obj, File output) throws ContentException, IOException {
		String xml = codec.encodeObject(myOntology, obj, true);
		setFileContents(output, xml);
	}
	
	private static String getFileContent(File file) throws FileNotFoundException,IOException {
		return getReaderContent(new FileReader(file));
	}

	private static String getStreamContent(InputStream str) throws FileNotFoundException,IOException {
		return getReaderContent(new InputStreamReader(str));
	}

	private static String getReaderContent(Reader r) throws IOException {
		StringBuffer contents = new StringBuffer();
		String lineSeparator = System.getProperty("line.separator");

		BufferedReader input = null;
		input = new BufferedReader(r);
		String line = null;
		while (( line = input.readLine()) != null){
			contents.append(line);
			contents.append(lineSeparator);
		}
		if (input!= null) {
			//flush and close both "input" and its underlying Reader
			input.close();
		}
		return contents.toString();
	}
	
	private static void setFileContents(File file, String contents) throws FileNotFoundException, IOException {
		if (file == null) {
			throw new IllegalArgumentException("File should not be null.");
		}
		if (!file.exists()) {
			file.createNewFile();
		}
		if (!file.isFile()) {
			throw new IllegalArgumentException("Should not be a directory: " + file);
		}
		if (!file.canWrite()) {
			throw new IllegalArgumentException("File cannot be written: " + file);
		}

		//declared here only to make visible to finally clause; generic reference
		Writer output = null;
		try {
			//use buffering
			output = new BufferedWriter( new FileWriter(file) );
			output.write( contents );
		}
		finally {
			//flush and close both "output" and its underlying FileWriter
			if (output != null) output.close();
		}
	}
}
