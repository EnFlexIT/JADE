/*
 * (c) Copyright Hewlett-Packard Company 2001
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE and no warranty
 * that the program does not infringe the Intellectual Property rights of
 * a third party.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 */

package jade.util;

//#J2ME_EXCLUDE_FILE

import java.util.Hashtable;

import java.io.Reader;
import java.io.IOException;

/**
 * This class serves as a basis for supporting the ability to import properties
 * from files. Those files may also contain further import dirrectives. It is
 * also usable in the restrictive J2ME CLDC environment. Since file support
 * will be handled differently in different environments, it contains one
 * abstract method <b><tt>fileReader</tt></b> which given the name of a file
 * (its URL) must return a Reader object. Extending classes will provide that
 * method in a suitable fashion. For example, in the desktop world this would be:
 * <pre>
 *      return new InputStreamReader(new FileInputStream(aFileName));
 * </pre>
 * whereas in the CLDC environment it would be:
 * <pre> 
 *      return new InputStreamReader(Connector.openInputStream(aFileName));
 * </pre>
 * This class relates to four others as follows:
 * <ol>
 * <li> BasicProperties - This class provides the foundation class. It
 * is designed to be usable in the restrictive J2ME CLDC environment. It
 * provides enhanced property management as well as providing support for
 * values containing strings of the form <b><tt>${key}</tt></b>.
 * <li> EnhancedProperties - Provides a concrete implementation of ImportableProperties
 * useable in the J2SE (desktop) world.
 * <li> ExpandedProperties - Extends EnhancedProperties and adds support for fetching
 * system environment variables (those usable from the OS shell). This class would need
 * to be carefully considered in different environments.
 * <li> PropertiesException - Extends RuntimeException and is thrown under various
 * error conditions by these classes.
 </ol>
 * @author Dick Cowan - HP Labs
 */
public abstract class ImportableProperties extends BasicProperties {


    String importKey = "import";
    Hashtable importNames = null;

    /**
     * Construct empty property collection.
     */
    public ImportableProperties() {
        super();
    }

    /**
     * Construct properties from arguments.
     * @param theArgs The applications original arguments.
     */
    public ImportableProperties(String[] theArgs) {
        super();
        parseArgs(theArgs);
    }

    /**
     * Construct a Reader for the specified file name.
     * @param aFileName The name of the file.
     * @return Reader The reader for the file.
     * @throws IOException if anything goes wrong.
     */
    protected abstract Reader fileReader(String aFileName) throws IOException;

    /**
     * Get the string used to trigger import activity.
     * @return String The import key. Default value is "import".
     */
    public String getImportKey() {
        return importKey;
    }

    /**
     * Change value of import directive.
     * @param aKey New value to change default "import".
     */
    public void setImportKey(String aKey) {
        importKey = aKey;
    }

    /**
     * Add properties from named file.
     * Creates a Reader for the file and calls addFromReader.
     * @param name The name of the property file.
     * @param importNames Hashtable with names of files processed - used
     * to catch circular imports.
     * @throws IOException if anything goes wrong.
     * @throws PropertiesException if circular import.
     */
    public void addFromFile(String aFileName) throws IOException {
        if (importNames == null) {
            importNames = new Hashtable();
        }
        if (importNames.put(aFileName, "x") == null) {  // value doesn't matter
            boolean holdCRState = CRState;
            addFromReader(fileReader(aFileName));
            CRState = holdCRState;
        } else {
            throw new PropertiesException("Circular import: " + aFileName);
        }
    }

    /**
     * Detect import dirrective and import file.
     * @param key The key value string - check if equal to "import".
     * @param value The value string - if import then this is file name.
     * @return False if import, true otherwise.
     */
    protected boolean storableProperty(String key, String value) {
        if (key.equalsIgnoreCase(importKey)) {
            // Evaluate value to get actual file name
            value = doSubstitutions(value);
            try {
                addFromFile(value);
            } catch (IOException ioe) {
                throw new PropertiesException("Error reading file. " + ioe);
            }
            return false;
        }
        return true;
    }

}
