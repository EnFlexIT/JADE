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

import java.util.Enumeration;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;

import java.io.FileInputStream;

/**
 * Provides a concrete implementation of ImportableProperties
 * useable in the J2SE (desktop) world.
 * This class relates to four others as follows:
 * <ol>
 * <li> BasicProperties - This class provides the foundation class. It
 * is designed to be usable in the restrictive J2ME CLDC environment. It
 * provides enhanced property management as well as providing support for
 * values containing strings of the form <b><tt>${key}</tt></b>.
 * <li> ImportableProperties - This abstract class extends BasicProperties and
 * serves as a basis for supporting the ability to import properties from files.
 * Those files may also contain further import dirrectives. It is also usable in
 * the restrictive J2ME CLDC environment. Since file support will be handled
 * differently in different environments, it contains one abstract method
 * <b><tt>fileReader</tt></b> which given the name of a file (its URL) must
 * return a Reader object. Extending classes will provide that method in a
 * suitable fashion. For example, in the desktop world this would be:
 * <pre>
 *      return new InputStreamReader(new FileInputStream(aFileName));
 * </pre>
 * whereas in the CLDC environment it would be:
 * <pre> 
 *      return new InputStreamReader(Connector.openInputStream(aFileName));
 * </pre>
 * <li> ExpandedProperties - Extends EnhancedProperties and adds support for fetching
 * system environment variables (those usable from the OS shell). This class would need
 * to be carefully considered in different environments.
 * <li> PropertiesException - Extends RuntimeException and is thrown under various
 * error conditions by these classes.
 * @author Dick Cowan - HP Labs
 */
public class EnhancedProperties extends ImportableProperties {

    /**
     * For testing. Simply pass command line arguments to constructor then display
     * all key=value pairs using sorted enumeration.
     */
    public static void main(String[] args) {
        EnhancedProperties prop = new EnhancedProperties(args);
        prop.list(System.out);
    }

    /**
     * Construct empty property collection.
     */
    public EnhancedProperties() {
        super();
    }

    /**
     * Construct properties from arguments.
     * @param theArgs The applications original arguments.
     */
    public EnhancedProperties(String[] theArgs) {
        super();
        parseArgs(theArgs);
    }

    /**
     * Construct properties from specified file.
     * @param aFileName The name of the properties file.
     * @throws IOException if anything goes wrong.
     */
    public EnhancedProperties(String aFileName) throws IOException {
        super();
        addFromReader(fileReader(aFileName));
    }

    /**
     * Construct a Reader for the specified file name.
     * @param aFileName The name of the file.
     * @return Reader The reader for the file.
     * @throws IOException if anything goes wrong.
     */
    protected Reader fileReader(String aFileName) throws IOException {
        Reader reader = null;

        try {
            reader = new InputStreamReader(new FileInputStream(aFileName));
        } catch (Exception e) {
            throw new IOException("Unable to open property file: " + aFileName);
        }
        return reader;
    }

}
