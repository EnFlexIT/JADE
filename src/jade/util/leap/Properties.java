/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
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
package jade.util.leap;

import java.io.*;
import java.util.*;
/*#MIDP_INCLUDE_BEGIN
import javax.microedition.io.*;
import javax.microedition.rms.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
#MIDP_INCLUDE_END*/

/**
 * Environment dependent implementation of the Properties class.
 * The J2SE and PJAVA implementation simply extend 
 * java.util.Properties.
 * The MIDP implementation allows getting properties from
 * - the .jad file of the midlet
 * - the persistent storage mechanism of MIDP (RecordStore)
 * - an http server
 * - manually entered
 * 
 * load and store operations identify which source to use by
 * value of the "MIDlet-LEAP-Properties" key in the .jad file.
 * if this does not exist, or it can not be accessed,
 * the default recordstore with the name "LEAP" will be used.
 * 
 * @author Steffen Rusitschka - Siemens AG
 * @author Marc Schlichte - Siemens AG
 * @author Nicolas Lhuillier - Motorola
 */
//#MIDP_EXCLUDE_BEGIN
public class Properties extends java.util.Properties implements Serializable {
	/**
 	   This is required to ensure compatibility with 
 	   the J2ME version of this class in serialization/deserialization 
 	   operations.
 	 */
  private static final long     serialVersionUID = 3487495895819396L;
  
  private static final String HEADER = "LEAP-Properties";
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
public class Properties extends Hashtable {
  private static final String SEPARATOR = "=";
  private static final String JAD = "jad";
  private static final String JAD_PREFIX = "MIDlet-LEAP-";
  private boolean             fromJad = false;
#MIDP_INCLUDE_END*/
  
  /**
     Load a set of key-value pairs from a given storage element.
     All key-value pairs previously included in this Properties object
     will be lost.
     The storage element is environment-dependent:
     In a J2SE or PJAVA environment it is a file named 
     <code>storage</code>.
     In a MIDP environment it can be the JAD (if storage = "jad") or 
     a RecordStore called <code>storage</code>.
   */
  public void load(String storage) throws IOException {
  	clear();
  	//#MIDP_EXCLUDE_BEGIN
    try {
      // Search the file system
      InputStream in = new FileInputStream(storage); 
      super.load(in);
      in.close();
    }
    catch(IOException ioe) {
      super.load(ClassLoader.getSystemResource(storage).openStream()); // Search the classpath
    }
    //#MIDP_EXCLUDE_END
    /*#MIDP_INCLUDE_BEGIN
    if (JAD.equals(storage)) {
    	fromJad = true;
  	}
  	else {
    	fromJad = false;
      recordstoreLoad(storage);
    }
    #MIDP_INCLUDE_END*/
  } 

  /**
     Store the set of key-value pairs held by this Properties object
     into a given storage element.
     The storage element is environment-dependent:
     In a J2SE or PJAVA environment it is a file named 
     <code>storage</code>.
     In a MIDP environment it is a RecordStore called 
     <code>storage</code>.
   */
  public void store(String storage) throws IOException {
  	//#J2ME_EXCLUDE_BEGIN
    OutputStream out = new FileOutputStream(storage);
    super.store(out, HEADER);
    out.close();
  	//#J2ME_EXCLUDE_END
  	/*#PJAVA_INCLUDE_BEGIN
    OutputStream out = new FileOutputStream(storage);
    super.save(out, HEADER);
    out.close();
  	#PJAVA_INCLUDE_END*/
  	/*#MIDP_INCLUDE_BEGIN
    recordstoreStore(storage);
  	#MIDP_INCLUDE_END*/
  } 

  public Object clone() {
    Properties  p = new Properties();
    Enumeration enum = propertyNames();

    while (enum.hasMoreElements()) {
      String key = (String) enum.nextElement();
      p.setProperty(key, getProperty(key));
    }
		/*#MIDP_INCLUDE_BEGIN
    p.fromJad = fromJad;
    #MIDP_INCLUDE_END*/
    return p;
  } 

  /*#J2ME_INCLUDE_BEGIN
  public synchronized Object setProperty(String key, String value) {
    return super.put(key, value);
  } 
  #J2ME_INCLUDE_END*/

  /*#MIDP_INCLUDE_BEGIN
  public String getProperty(String key) {
    String prop = (String) super.get(key);
    if (prop == null && fromJad) {
      prop = jade.core.Agent.midlet.getAppProperty("MIDlet-LEAP-" + key);
    } 
    return prop;
  } 

  public Enumeration propertyNames() {
    return keys();
  } 
  
  private void recordstoreLoad(String name) throws IOException {
      RecordStore recordstore = null;

      try {
          if (recordstoreExists(name)) {
              recordstore = RecordStore.openRecordStore(name, false);
          } 
          else {
              throw new IOException("Persistent storage not existing.");
          } 
      } 
      catch (Exception e) {
          throw new IOException("Exception opening persistent storage.");
      } 

      // Get an Enumeration of the Records
      RecordEnumeration re = null;

      try {
          re = recordstore.enumerateRecords((RecordFilter) null, 
                                            (RecordComparator) null, false);
      } 
      catch (RecordStoreNotOpenException rsnoe) {
          throw new IOException("Exception reading persistent storage.");
      } 

      // Parse content
      while (re.hasNextElement()) {
          try {
              String record = 
                  new String(recordstore.getRecord(re.nextRecordId()));

              parseLine(record);
          } 
          catch (Exception e) {
              throw new IOException("Exception reading persistent storage.");
          } 
      } 

      // Close RecordStore
      try {
          recordstore.closeRecordStore();
      } 
      catch (Exception e) {
          System.err.println("Exception closing persistent storage.");
      } 
  } 

  // Utility method to determine if a RecordStore exists, based
  // on its name.
  private boolean recordstoreExists(String rsName) {
      String[] stores = RecordStore.listRecordStores();

      for (int s = 0; s < stores.length; s++) {
          if (stores[s].equals(rsName)) {
              return true;
          } 
      } 

      return false;
  } 

  private void recordstoreStore(String name) throws IOException {
      RecordStore recordstore = null;

      // Reset RecordStore
      try {
          recordstoreReset(name);

          recordstore = RecordStore.openRecordStore(name, true);
      } 
      catch (Exception e) {
          throw new IOException("Exception accessing persistent storage.");
      } 

      // Store the data
      Enumeration enum = keys();
      boolean     exceptions = false;
      int         recordID = 0;

      try {
          while (enum.hasMoreElements()) {
              String key = (String) enum.nextElement();
              String line = key + SEPARATOR + getProperty(key);
              byte[] data = line.getBytes();

              try {
                  recordID = recordstore.addRecord(data, 0, data.length);
              } 
              catch (RecordStoreException rse) {
                  exceptions = true;

                  try {
                      recordstore.deleteRecord(recordID);
                  } 
                  catch (Exception ex) {}
              } 
          } 
      } 
      catch (ClassCastException cce) {
          throw new IOException("Non String Properties have been inserted. Aborting storage!");
      } 

      // Close the RecordStore
      try {
          recordstore.closeRecordStore();
      } 
      catch (Exception e) {
          throw new IOException("Exception closing persistent storage.");
      } 

      if (exceptions) {
          throw new IOException("Exception(s): some properties not saved.");
      } 
  } 

  // Clear and reset the persistent storage.
  private void recordstoreReset(String name) {
      try {
          if (recordstoreExists(name)) {
              RecordStore.deleteRecordStore(name);
          } 
      } 
      catch (Exception e) {}

      return;
  } 

  // Parse a property file line, and add it in the Properties
  // hashtable. The property files should
  // be strings storing <code>key=value</code> pairs.
  // This method does nothing if the string is null or begins
  // with '#' (comment line) or if it does not contain the SEPARATOR.
  // @param line The property string to parse.
  private void parseLine(String line) {
      int separator;

      if ((line == null) || (line.startsWith("#")) 
              || ((separator = line.indexOf(SEPARATOR)) == -1)) {
          return;
      } 

      super.put(line.substring(0, separator), 
                     line.substring(separator + 1));
  } 
  #MIDP_INCLUDE_END*/
}

