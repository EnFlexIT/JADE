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

import java.util.*;
import java.io.*;

/**
 * Extends EnhancedProperties and adds support for fetching system environment
 * variables (those usable from the OS shell). This class would need
 * to be carefully considered in different environments.
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
 * <li> EnhancedProperties - Provides a concrete implementation of ImportableProperties
 * useable in the J2SE (desktop) world.
 * <li> PropertiesException - Extends RuntimeException and is thrown under various
 * error conditions by these classes.
 * @author Dick Cowan - HP Labs
 */
public class ExpandedProperties extends EnhancedProperties {

    BasicProperties envProperties = null;
    boolean expandedSearch = false;

    /**
     * For testing. Simply pass command line arguments to constructor then display
     * all key=value pairs using sorted enumeration.
     */
    public static void main(String[] args) {
        ExpandedProperties prop = new ExpandedProperties(args);
        prop.list(System.out);
    }

    /**
     * Construct empty property collection.
     */
    public ExpandedProperties() {
        super();
    }

    /**
     * Construct properties from arguments.
     * @param theArgs The applications original arguments.
     */
    public ExpandedProperties(String[] theArgs) {
        super();
        parseArgs(theArgs);
    }

    /**
     * Construct properties from specified file.
     * @param aFileName The name of the properties file.
     * @throws IOException if anything goes wrong.
     */
    public ExpandedProperties(String aFileName) throws IOException {
        super(aFileName);
    }


    protected String getEnvironmentProperty(String key) {
        String value = super.getEnvironmentProperty(key.toLowerCase());
        if (value == null) {
            // Try it with the key "as is" from the envProperties
            value = getEnvProperties().getProperty(key);
        }
        return value;
    }

    /**
     * Used to fetch environment properties and initialize environment
     * properties object if this is first invocation.
     * @return Properties The environment properties.
     */
    protected BasicProperties getEnvProperties() {
        if (envProperties == null) {
            try {
                envProperties = getEnvironmentProperties();
            } catch (Exception e) {
                envProperties = new EnhancedProperties();
            }
        }

        return envProperties;
    }

    /**
     * Fetch the system's environment properties and return them in
     * an EnhancedProperties object. The fetching will be done using the
     * appropriate shell command based on the OS. Based on method initially
     * created by David Bell.
     * @return EnhancedProperties fetched from environment.
     */
    public static BasicProperties getEnvironmentProperties() {
        BasicProperties properties = new EnhancedProperties();
        String command = null;
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.startsWith("windows")) {
            command = "set";
        } else {
            command = "env";    // linux, unix 
        }

        String commandOutput = getCommandOutput(command, true);

        if (commandOutput != null) {
            StringTokenizer parser =
                new StringTokenizer(commandOutput,
                                    System.getProperty("line.separator"));

            while (parser.hasMoreTokens()) {
                String line = parser.nextToken();
                int i = line.indexOf('=');

                properties.setProperty(line.substring(0, i),
                                       line.substring(i + 1));
            }
        }

        return properties;
    }

    /**
     * Locate property file. The search follows the following algorithm:
     * <ol>
     * <li>Look in current directory.
     * <li>Move up one level in directory and search it as well as just
     * first level of immediate subdirectories.
     * <li>Repeat above step until it is found or there are no more parent
     * directories.
     * </ol>
     * @param aPropertyFileName Name of desired property file.
     * @param aSearchCeiling If none null then this specifies the uppermost
     * directory name where at which the search should ston.
     * @return The full name of the located property file or null if not found.
     */
    public String locatePropertyFile(String aPropertyFileName, String aSearchCeiling) {

        File searchCeilingFile = null;

        if (aSearchCeiling != null) {
            searchCeilingFile = new File(aSearchCeiling);
        }


        String result = null;

        // Create initial path
        String path = (new File(".")).getAbsolutePath();

        path = path.substring(0, path.length()
                              - 2);    // drop last separator and dot
        expandedSearch = false;

        do {
            File dir = new File(path);

            path = dir.getPath();
            result = scanDirectory(aPropertyFileName, path, 0);

            if (result != null) {
                break;
            }
            if ((searchCeilingFile != null)
                    //#DOTNET_EXCLUDE_BEGIN
					&& (dir.compareTo(searchCeilingFile) == 0)) {
					//#DOTNET_EXCLUDE_END
					/*#DOTNET_INCLUDE_BEGIN
                    && (dir.Equals(searchCeilingFile) )) {
					#DOTNET_INCLUDE_END*/
                break;
            }

            path = dir.getParent();    // reduce the path by one
            expandedSearch = true;
        } while (path != null);

        return result;
    }

    /**
     * Execute a command and return its output as a string.
     * If the command generates multiple lines, each line
     * will be separated using a separator obtianed from
     * Java's System.getProperty("line.separator"). If the
     * command fails in any way the result will be null.
     * @param aCommand The command to run.
     * @param isShellCommand If true, then the command will be
     * prefixed with the appropriate system dependent shell invocation
     * (cmd.exe, command.com, or /bin/sh/).
     * @return String The commands output or null on failure.
     */
    protected static String getCommandOutput(String aCommand,
                                           boolean isShellCommand) {
        StringBuffer result = new StringBuffer();
        boolean success = false;    // assume failure

        try {
            String prefix = null;

            if (isShellCommand) {
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.startsWith("windows")) {
                    // Windows OS
                    if (osName.startsWith("windows 95") || osName.startsWith("windows 98")) {
                        // 16 bit windows hybrid
                        prefix = "command.com /e:16000 /c ";
                    } else {
                        // true 32-bit windows
                        // NT, 2000, XP, whatever is next?
                        prefix = "cmd.exe /c ";
                    }
                } else {
                    // Linux/Unix -c indicates command follows 
                    prefix = "/bin/sh -c ";
                }
            } else {
                prefix = "";
            }

            Process shell = Runtime.getRuntime().exec(prefix + aCommand);
            BufferedReader commandOutput =
                new BufferedReader(new InputStreamReader(shell
                    .getInputStream()));

            // Read output lines from command
            String line = null;
            int lineCount = 0;
            String eol = System.getProperty("line.separator");

            while ((line = commandOutput.readLine())
                    != null) {    // reads single full line
                if (lineCount > 0) {
                    result.append(
                        eol);    // if multiple lines, separate with system separator
                }

                result.append(line);

                lineCount++;
            }

            // Wait for command to terminate
            shell.waitFor();

            success = (shell.exitValue()
                       == 0);    // final determination of success

            commandOutput.close();
        } catch (Exception e) {

            // if anything goes wrong success will still be false.
        }

        if (!success) {
            return null;
        }

        return result.toString();
    }

    /**
     * Scan a directory for a property file and examine just first level
     * of subdirectories.
     * @param aPropertyFileName Name of desired property file.
     * @param aPath Current full path.
     * @param depth Current depth - used to control recursive search depth of only
     * first level subdirectories.
     * @return The property file name or null.
     */
    protected String scanDirectory(String aPropertyFileName, String aPath, int depth) {
        aPath = aPath.replace('\\', '/');    // normalize

        String result = null;
        File dir = new File(aPath);

        if (!dir.isDirectory()) {
            throw new PropertiesException("Not directory: " + aPath);
        }

        String[] names = dir.list();

        if (names == null) {    // Returns null if an I/O error occurs (access violation)
            return null;
        }

        for (int i = 0; i < names.length; i++) {
            String fileName = names[i];
            String fullName = aPath;

            if (!fullName.endsWith("/")) {
                fullName = fullName + "/";
            }

            fullName = fullName + fileName;

            if ((expandedSearch) && (depth == 0)) {
                File target = new File(fullName);

                if (target.isDirectory()) {
                    result = scanDirectory(aPropertyFileName, fullName,
                                           depth + 1);

                    if (result != null) {
                        break;
                    }
                }
            }

            if (fileName.equalsIgnoreCase(aPropertyFileName)) {
                result = (expandedSearch)
                         ? testPropertyFile(fullName) : fullName;

                if (result != null) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Once the property file search has expanded beyond the current
     * directory this method is used to examine property files
     * which match the desired name - if it contains an import directive
     * then return the value of that directive otherwise return
     * the name of the property file.
     * If any problem are encountered reading the property file
     * return null.
     * @param aPropertyFileName Name of property file.
     * @return The property file name or null.
     */
    protected String testPropertyFile(String aPropertyFileName) {
        Properties prop = new Properties();
        String result = null;

        try {
            prop.load(new FileInputStream(aPropertyFileName));

            String importName = doSubstitutions(prop.getProperty(importKey));

            result = (importName != null) ? importName : aPropertyFileName;
        } catch (IOException ioe) {}

        return result;
    }

}
