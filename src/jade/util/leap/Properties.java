/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.util.leap;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

/**
 * J2SE implementation of the Properties.
 */
public class Properties extends java.util.Properties 
    implements Serializable {

    /**
     * The default resource file path.
     */
    private static final String DEFAULT_RESOURCE_PATH = "resources" 
            + File.separator + "jade.properties";
    private String              RESOURCE_PATH;

    /**
     * A text header for the properties file.
     */
    private static final String HEADER = "LEAP configuration file.";

    /**
     * Constructor declaration
     *
     */
    public Properties() {
        super();

        RESOURCE_PATH = DEFAULT_RESOURCE_PATH;
    }

    /**
     * Creates an empty property list.
     */
    public Properties(String fileName) {
        super();

        RESOURCE_PATH = fileName;
    }

    /**
     * Calls the hashtable method <code>put</code>.
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to <code>key</code>.
     * @see #getProperty
     */
    public synchronized Object setProperty(String key, String value) {
        return super.setProperty(key, value);
    } 

    /**
     * Searches for the property with the specified key in this
     * property list. The method returns <code>null</code> if the
     * property is not found.
     * @param   key   the property key.
     * @return  the value in this property list with the specified key value.
     * @see     #setProperty
     */
    public String getProperty(String key) {
        return super.getProperty(key);
    } 

    /**
     * Reads a property list (key and element pairs) from
     * the default persistent storage.
     */
    public void load() throws IOException {
        InputStream in = new FileInputStream(RESOURCE_PATH);

        super.load(in);
        in.close();
    } 

    /**
     * Stores this property list (key and element pairs) to
     * the default persistent storage.
     */
    public void store() throws IOException {
        OutputStream out = new FileOutputStream(RESOURCE_PATH);

        super.store(out, HEADER);
        out.close();
    } 

    /**
     * Unitary test
     */

    /*
     * public static void main(String[] args) {
     * try{
     * Properties props = new Properties();
     * props.setProperty( "screen", "120x60" );
     * props.setProperty( "memory", "1MB" );
     * props.setProperty( "other.property", "Can be a string..." );
     * props.store();
     * Properties others = new Properties();
     * others.load();
     * System.out.println( others.get( "other.property" ) );
     * } catch( IOException ioe ){
     * System.err.println( ioe );
     * }
     * }
     */
}

