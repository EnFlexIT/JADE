/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * $header: $
 * <TODO>Put the LEAP HEADER and footer</TODO>
 */
package jade.util.leap;

import java.io.*;
import java.util.Properties;

// import jade.util.leap.*;

/**
 * J2SE implementation of the LEAPProperties.
 */
public class LEAPProperties extends Properties 
    implements LEAPSerializable {

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
    public LEAPProperties() {
        super();

        RESOURCE_PATH = DEFAULT_RESOURCE_PATH;
    }

    /**
     * Creates an empty property list.
     */
    public LEAPProperties(String fileName) {
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
     * LEAPProperties props = new LEAPProperties();
     * props.setProperty( "screen", "120x60" );
     * props.setProperty( "memory", "1MB" );
     * props.setProperty( "other.property", "Can be a string..." );
     * props.store();
     * LEAPProperties others = new LEAPProperties();
     * others.load();
     * System.out.println( others.get( "other.property" ) );
     * } catch( IOException ioe ){
     * System.err.println( ioe );
     * }
     * }
     */
}

