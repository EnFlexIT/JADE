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

//#MIDP_EXCLUDE_FILE

// DO NOT ADD ANY IMPORTS FOR CLASSES NOT DEFINED IN J2ME CLDC!
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Date;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.PrintStream;
import java.io.IOException;
import java.io.EOFException;

import jade.util.leap.Properties;

/**
 * Provides the foundation class for property management. It
 * is designed to be usable in the restrictive J2ME CLDC environment. It
 * provides enhanced property management as well as providing support for
 * values containing strings of the form <b><tt>${key}</tt></b>.
 * <p>
 * A property may be set such that it can't be altered by ending the key value
 * with a '!'. For example:
 * <pre>
 *      agentClass!=com.hp.agent.Foo
 * </pre>
 * One still references this property as ${agentClass}.
 * <p>
 * This class relates to four others as follows:
 * <ol>
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
 * <li> ExpandedProperties - Extends EnhancedProperties and adds support for fetching
 * system environment variables (those usable from the OS shell). This class would need
 * to be carefully considered in different environments.
 * <li> PropertiesException - Extends RuntimeException and is thrown under various
 * error conditions by these classes.
 </ol>
 * Properties presented via parseArgs or read from an input stream may be specified in
 * either of two formats:
 * <b>key=value</b> or <b>key:value</b>.
 * To substitute the value of a key in a value use the format <b><tt>${key}</tt></b>.
 * @author Dick Cowan - HP Labs
 */
public class BasicProperties extends Properties {

    boolean CRState = false;
    Hashtable keyNames = new Hashtable();  // for detecting circular definitions
    Vector sortVector = null;   // only used by sortedKeys

    /**
     * For testing. Simply pass command line arguments to constructor then display
     * all key=value pairs using sorted enumeration.
     */
    public static void main(String[] args) {
        BasicProperties prop = new BasicProperties(args);
        prop.list(System.out);
    }

    /**
     * Construct empty property collection.
     */
    public BasicProperties() {
    }

    /**
     * Construct properties from arguments.
     * @param theArgs The applications original arguments.
     */
    public BasicProperties(String[] theArgs) {
        this();
        parseArgs(theArgs);
    }

    /**
     * Add properties from a specified InputStream. Properties
     * will be added to any existing collection.
     * @param aFileName The name of the file.
     * @throws IOException if anything goes wrong.
     */
    public synchronized void load(InputStream inStream) throws IOException {
        addFromReader(new InputStreamReader(inStream, "8859_1"));
    }

    /**
     * Writes this property collection to the output stream in a format suitable for
     * loading into a Properties table using the load method.
     * @param out An output stream.
     * @param header A description of the property list - may be null.
     * @throws IOException if anything goes wrong.
     */
    public synchronized void store(OutputStream out, String header) throws IOException
    {
        String lineSeparator = System.getProperty("line.separator");
        Writer writer = new OutputStreamWriter(out, "8859_1");
        if (header != null) {
            writer.write("#" + header);
            writer.write(lineSeparator);
        }
        writer.write("#" + new Date().toString());
        writer.write(lineSeparator);
        for (Enumeration e = sortedKeys(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            Object data = super.get(key);
            if (data != null) {
                writer.write(key + "=" + data.toString());
                writer.write(lineSeparator);
            }
        }
        writer.flush();
    }

    /**
     * Return a sorted enumertion of this properties keys.
     * @return Enumeration Sorted enumeration.
     */
    public synchronized Enumeration sortedKeys() {
        if (sortVector == null) {
            sortVector = new Vector();
        } else {
            sortVector.removeAllElements();
        }
        for (Enumeration e = super.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            int i = 0;
            while (i < sortVector.size()) {
                if (key.compareTo((String)sortVector.elementAt(i)) < 0) {
                    break;
                }
                i++;
            }
            sortVector.insertElementAt(key, i);
        }
        return new Enumeration() {
            Enumeration en = BasicProperties.this.sortVector.elements();

            public boolean hasMoreElements() {
                return en.hasMoreElements();
            } 

            public Object nextElement() {
                return en.nextElement();
            } 

        };
    } 

    /**
     * Parse the arguments and place them in this properties collection.
     * This method uses a number of protected helper methods to accomplish
     * its parsing. This enables an extending class to easily change its
     * behavior without completely replacing this method. For additional
     * understanding see prepareArgs, isCandidate, specialHandling, isolateKey,
     * isolateValue, and nextArgIndex.  
     * @param args The array of arguments - typically from a command line.
     * If null, this method does nothing.
     */
    public synchronized void parseArgs(String[] args) {
        if (args != null) {
            int argIndex = 0;
            String[] newArgs = prepareArgs(args);
            while (argIndex < newArgs.length) {
                String arg = newArgs[argIndex];
                parseArgument(arg);
                argIndex = nextArgIndex(newArgs, argIndex);
                if ((argIndex < 0) || (argIndex >= newArgs.length)) {
                    return;  // stop parsing
                }
            }
        }
    }
    
    /**
     * Called to handle either an argument or line from an import file.
     * @param arg The argument or line. Typically of the form key=value.
     */
    protected void parseArgument(String arg) {
        if (isCandidate(arg)) {
            String key = isolateKey(arg);
            String value = isolateValue(arg);
            if (storableProperty(key, value)) {
                setProperty(key, value);
            }
        } else {
            specialHandling(arg);
        }
    }

    /**
     * Called by parseArgs to perform any preprocessing of the arguments.
     * By default this method does nothing and simply returns the parameter its passed.
     * However an extending class could override this method to modify the original
     * arguments if necessary.
     * @param args The original arguments passed to parseArgs.
     * @return String[] The collection which parseArgs will actually use.
     */
    protected String[] prepareArgs(String[] args) {
        return args;
    }
    
	/**
   * Used by isCandidate, isolateKey and isolateValue to determine
	 * the index of the separation character (':' or '=') within an argument string.
	 * @param arg The argument being processed.
	 */
    protected int getSeparatorIndex(String arg) {
      int idxA = arg.indexOf('=');
      int idxB = arg.indexOf(':');
	  
      if (idxA == -1)  // key:value 
        return idxB;
      if (idxB == -1)  // key=value
        return idxA;
      if (idxA < idxB) // key=value with :
	    return idxA;
	  else             // key:value with =
	    return idxB;   
	}
	
    /**
     * Called by parseArgument to determine if an argument is a candidate key, value combination.
     * By default this method will return true if the argument similar
     * to any of the following:
     * <ol>
     *   <li>key=value
     *   <li>key:value
     *   <li>-key  This form is a shorthand for key:true
     * </ol>
     * An extending class may override this method to implement a different
     * strategy for recognizing candidates.
     * @param arg The argument being processed.
     * @return True if it is a candidate, false if not.
     */
    protected boolean isCandidate(String arg) {
        if (getSeparatorIndex(arg) > 0) {  // key=value or key:value
            return true;
        }
        
        if ((arg.length() > 1) && (arg.startsWith("-"))) {    // "-x" -> "arg=true"
            return true;
        }
        
        return false;
    }

    /**
     * Called by parseArgument when the isCandidate method returns false.
     * This allows an extending class to override this method
     * and provide whatever special processing may be required.
     * The default behavior is simply to throw a PropertiesException
     * indicating which property was unrecognized. 
     * @param arg The argument being processed.
     */
    protected void specialHandling(String arg) {
        throw new PropertiesException("Unrecognized: " + arg);
    }

    /**
     * Called by parseArgument to extract the key component from an argument.
     * @param arg The argument being processed.
     * @param index Index into args of current argument.
     */
    protected String isolateKey(String arg) {
        int separatorIndex = getSeparatorIndex(arg);    // key=value or key:value

        if (separatorIndex > 0) {
            return arg.substring(0, separatorIndex);
        }
        
        if ((arg.length() > 1) && (arg.startsWith("-"))) {    // "-x" -> "arg=true"
            return arg.substring(1);
        }
        
        throw new PropertiesException("Unable to identify key part in argument: " + arg);
    }        

    /**
     * Called by parseArgument to extract the value component from the current argument.
     * By default, any value of the form 'hello world' will be returned as "hello world".
     * A value of '' (two single quotes) will be returned as an empty string. An argument
     * of the form "key=" will return null for its value and cause the key to be removed
     * from the properties.
     * @param arg The argument being processed.
     * @return String The resultant value, may be null.
     */
    protected String isolateValue(String arg) {
        String value = null;
        
        int separatorIndex = getSeparatorIndex(arg);    // key=value or key:value

        if (separatorIndex > 0) {
            if (separatorIndex == (arg.length()-1)) {
                return null;
            }
            value = arg.substring(separatorIndex+1);
        } else {
            if ((arg.length() > 1) && (arg.startsWith("-"))) {    // "-x" -> "arg=true"
                value = "true";
            }
        }

        if (value != null) {
            if ( (value.startsWith("'")) && (value.endsWith("'")) ) {
                if (value.length() == 2) {
                    value = "";
                } else {
                    // Replace the single quotes with double quote as this
                    // quoting character works the same on Windows and Unix.
                    value = "\"" + value.substring(1, value.length()-1) + "\"";
                }
            }
            return value;
        }
        
        throw new PropertiesException("Unable to identify value part in argument: " + arg);
    }

    /**
     * Called by parseArgument as a final step prior to actually storing the key=value pair.
     * By default it simply returns true, which directs parseArgs to store the pair.
     * An extending class could change this behavior to say look for something special
     * such as an import directive and take different action.
     * @param key The key string.
     * @param value The value string.
     * @return True if the key=value pair should be stored, false otherwise.
     */
    protected boolean storableProperty(String key, String value) {
        return true;
    }

    /**
     * Called by parseArgs to set the next argument index. By default
     * it simply returns the current index + 1.    
     * @return int If the returned value is negative or greater
     * than or equal to args.length, parseArgs will stop parsing.
     * Otherwise parseArgs will use the returned value as its
     * next index.
     */
    protected int nextArgIndex(String[] args, int argIndex) {
        return argIndex+1;
    }
    
    /**
     * Copy a data from standard Properties.
     * @param source The Hashtable to copy from.
     */
    public synchronized void copyProperties(BasicProperties source) {
        for (Enumeration e = source.keys(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            super.put(key, source.getRawProperty(key));
        }
    }

    /**
     * Create a new PropertiesCollection from this one by coping those
     * attributes which begin with a particular prefix string.
     * The prefix of selected attributes is deleted when those
     * keys are placed in the new collection.
     * @param anArgPrefix The prefix string. Ex: "server."
     */
    public synchronized BasicProperties extractSubset(String anArgPrefix) {
        BasicProperties result = new BasicProperties();
        for (Enumeration e = super.keys(); e.hasMoreElements(); ) {
            String originalKey = (String) e.nextElement();
            String newKey = null;

            if (originalKey.startsWith(anArgPrefix)) {
                newKey =
                    originalKey
                        .substring(anArgPrefix
                            .length());    // could be nothing left
            } else {
                newKey = "";    // this argument not in result
            }

            if (newKey.length() > 0) {
                result.setProperty(newKey, getRawProperty(originalKey));
            }
        }

        return result;
    }

    /**
     * Get the object associated with a key.
     * @param aKey Key for desired property.
     * @return The object associated with this key or null if none exits.
     */
    public Object get(String aKey) {
        String testKey = (aKey.endsWith("!")) ? aKey.substring(0, aKey.length()) : aKey;
        if (testKey.length() == 0) {
            return null;
        }        
        Object data = super.get(testKey);
        if (data == null) {
            data = super.get(testKey + "!" );
        }
        return data;
    }        

    /**
     * Set property value to specified object.
     * @param aKey The key used to store the data. The key may contain strings of
     * the form <b><tt>${key}</tt></b> which will be evaluated first.
     * @param aValue The object to be stored.
     * @return The previous value of the specified key, or null if it did not have one.
     */
    public Object put(String aKey, Object aValue) {
        String actualKey = doSubstitutions(aKey);
        String testKey = (actualKey.endsWith("!")) ? actualKey.substring(0, actualKey.length()) : actualKey;
        if (super.containsKey(testKey + "!")) {
            throw new PropertiesException("Attempt to alter read only property:" + testKey);
        }
        return super.put(actualKey, aValue);
    }        

    /**
     * Override getProperty in base class so all occurances of
     * the form <b><tt>${key}</tt></b> are replaced by their
     * associated value.
     * @param aKey Key for desired property.
     * @return The keys value with substitutions done.
     */
    public String getProperty(String aKey) {
        return getProperty(doSubstitutions(aKey), null);
    }

    /**
     * Set property value. If value is null the property (key and value) will be removed.
     * @param aKey The key used to store the data. The key may contain strings of
     * the form <b><tt>${key}</tt></b> which will be evaluated first.
     * @param aValue The value to be stored, if null they property will be removed.
     * @return The previous value of the specified key, or null if it did not have one.
     */
    public Object setProperty(String aKey, String aValue) {
        String actualKey = doSubstitutions(aKey);
        String testKey = (actualKey.endsWith("!")) ? actualKey.substring(0, actualKey.length()) : actualKey;
        if (super.containsKey(testKey + "!")) {
            throw new PropertiesException("Attempt to alter read only property:" + testKey);
        }
        if (aValue == null) {
            return super.remove(actualKey);
        } else {
            return super.put(actualKey, aValue);
        }
    }

    /**
     * Set property value only if its not set already.
     * @param aKey The key used to store the data. The key may contain strings of
     * the form <b><tt>${key}</tt></b> which will be evaluated first.
     * @param value The value to be stored.
     * @return Null if store was done, non-null indicates store not done and the
     * returned value in the current properties value.
     */
    public Object setPropertyIfNot(String aKey, String value) {
        String current = getProperty(aKey);
        if (current == null) {
            return setProperty(aKey, value);
        }
        return current;
    }

    /**
     * Fetch property value for key which may contain strings
     * of the form <b><tt>${key}</tt></b>. 
     * @param aKey Key for desired property.
     * @return The keys value with no substitutions done.
     */
    public String getRawProperty(String aKey) {
        Object data = super.get(aKey);
	    return (data != null) ? data.toString() : null;
    }

    /**
     * Use this method to fetch a property ignoring case of key.
     * @param aKey The key of the environment property.
     * @return The key's value or null if not found.
     */
    public String getPropertyIgnoreCase(String aKey) {
        for (Enumeration e = super.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();

            if (aKey.equalsIgnoreCase(key)) {
                return getProperty(key);
            }
        }
        return null;
    }

    /**
     * Perform substitution when a value is fetched. Traps circular definitions,
     * and calls valueFilter with value prior to returning it.
     * @param aKey The property key.
     * @param defaultValue Value to return if property not defined. May be null.
     * If non null it will be passes to valueFilter first.
     * @return The resultant value - could be null or empty.
     * @throws PropertiesException if circular definition.
     */
    public String getProperty(String aKey, String defaultValue) {
        String testKey = (aKey.endsWith("!")) ? aKey.substring(0, aKey.length()) : aKey;
        if (testKey.length() == 0) {
            return null;
        }
        String value = null;
        // This synchronized block prevents a "Circular argument substitution key" error in case two threads
        // search for the same key in parallel
        synchronized (keyNames) {
	        if (keyNames.put(testKey, "x") != null) {  // value doesn't matter
	            throw new PropertiesException(
	                "Circular argument substitution with key: " + aKey);
	        }
	        Object data = super.get(testKey);
	        if (data == null) {
	            data = super.get(testKey + "!" );
	        }
		    value = (data != null) ? data.toString() : null;
	        if (value != null) {
	            if (value.length() >= 4) {    // shortest possible value: ${x}
	                value = doSubstitutions(value);
	            }
	        } else {
	            value = defaultValue;
	        }
	        if (value != null) {
	            value = valueFilter(aKey, value);
	        }
	        
	        keyNames.remove(testKey);
        }

        return value;
    }

    /**
     * Called by getProperty(key, default) to perform any post processing of the
     * value string. By default, this method provides special processing on the value
     * associated with any property whose key name has the string "path" as part of it
     * (ex: "classpath", "sourcepath", "mypath"). When the value for such keys is fetched
     * any occurance of '|' will be converted to a ':' on Unix systems and a ';' on
     * Windows systems. Therefore to increase the direct reuse of your property files,
     * always use a '|' as a separator and always assign a key name which has "path" as
     * part of it.
     * @param key The properties key.
     * @param value The properties value.
     * @return String New potentially altered value. 
     */
    protected String valueFilter(String key, String value) {
        if (key.toLowerCase().indexOf("path") >= 0) {    // convert separators to be correct for this system
            String correctSeparator = System.getProperty("path.separator");

            if (correctSeparator.equals(";")) {
                value = value.replace('|', ';');
            } else {
                value = value.replace('|', ':');
            }
        }
        return value;
    }

    /**
     * Extract a string value and convert it to an integer.
     * If there isn't one or there is a problem with the conversion,
     * return the default value.
     * @param aKey The key which will be used to fetch the attribute.
     * @param aDefaultValue Specifies the default value for the int.
     * @return int The result.
     */
    public int getIntProperty(String aKey, int aDefaultValue) {
        int result = aDefaultValue;

        try {
            result = Integer.parseInt(getProperty(aKey));
        } catch (Exception e) {}

        return result;
    }

    /**
     * Store an int as a string with the specified key.
     * @param aKey The key which will be used to store the attribute.
     * @param aValue The int value.
     */
    public int setIntProperty(String aKey, int aValue) {
        setProperty(aKey, Integer.toString(aValue));
        return aValue;
    }

    /**
     * Extract a string value ("true" or "false") and convert it to
     * a boolean. If there isn't one or there is a problem with the
     * conversion, return the default value.
     * @param aKey The key which will be used to fetch the attribute.
     * @param aDefaultValue Specifies the default value for the boolean.
     * @return boolean The result.
     */
    public boolean getBooleanProperty(String aKey, boolean aDefaultValue) {
        boolean result = aDefaultValue;

        try {
            String value = getProperty(aKey);

            result = value.equalsIgnoreCase("true");
        } catch (Exception e) {}

        return result;
    }

    /**
     * Store a boolean as a string ("true" or "false") with the specified key.
     * @param aKey The key which will be used to store the attribute.
     * @param aValue The boolean value.
     */
    public void setBooleanProperty(String aKey, boolean aValue) {
        setProperty(aKey, (aValue) ? "true" : "false");
    }

    /**
     * Change key string associated with existing value.
     * @param existintKey The current key.
     * @param newKey The new key.
     * @return Non null is former value of object associated with new key.
     * Null indicates that either the existing key didn't exist or there
     * was no former value associated with the new key. i.e. null => success.
     */
    public Object renameKey(String existingKey, String newKey) {
        Object value = super.remove(doSubstitutions(existingKey));
        if (value != null) {
            return super.put(doSubstitutions(newKey), value);
        }
        return null;
    }

    /**
     * Replace all substrings of the form ${xxx} with the property value
     * using the key xxx. Calls doSubstitutions(anInputString, false).
     * @param anInputString The input string - may be null.
     * @return The resultant line with all substitutions done or null if input string was.
     */
    public String doSubstitutions(String anInputString) {
        return doSubstitutions(anInputString, false);
    }

    /**
     * Replace all substrings of the form ${xxx} with the property value
     * using the key xxx. If the key is all caps then the property is
     * considered to be a system property.
     * @param anInputString The input string - may be null.
     * @param allowUndefined If true, undefined strings will remain as is,
     * if false, an exception will be thrown.
     * @return The resultant line with all substitutions done or null if input string was.
     */
    public String doSubstitutions(String anInputString, boolean allowUndefined) {
        if (anInputString == null) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        int si = 0;    // source index
        int oi = 0;    // opening index
        int ci = 0;    // closing index

        do {
            oi = anInputString.indexOf("${", si);
            ci = anInputString.indexOf('}', si);

            if (oi > si) {    // xxxxxx${key}
                result.append(anInputString.substring(si, oi));

                si = oi;
            }

            if ((oi == si) && (ci > oi + 2)) {    // ${key}xxxxx
                String key = anInputString.substring(oi + 2, ci);

                // Try our properties first as this allows the user
                // to override system or environment setting
                String value = getProperty(key, null);

                // If we didn't find the property and its key is all uppercase
                // them check if its a Java or environment property
                if ((value == null) && key.equals(key.toUpperCase())) {
                    value = getEnvironmentProperty(key);
                }

                if (value == null) {
                    if (allowUndefined) {
                        value = "${" + key + "}";
                    } else {
                        throw new PropertiesException("Unable to get property value for key: " + key);
                    }
                }

                if (oi > si) {
                    result.append(anInputString.substring(si, oi));
                }

                result.append(value);

                si = ci + 1;
            } else {
                if (oi == -1) {    // xxxxxxxxx
                    result.append(anInputString.substring(si, anInputString.length()));

                    si = anInputString.length();
                } else {    // xxxxxx${xxxxxx
                    result.append(anInputString.substring(si, oi + 2));

                    si = oi + 2;
                }
            }
        } while (si < anInputString.length());

        return result.toString();
    }

    /**
     * Fetch environment property by looking calling System.getProperty.
     * @param key The key of the desired property.
     * @return The resultant property if it exists or null.
     */
    protected String getEnvironmentProperty(String key) {
        String value = System.getProperty(key.toLowerCase());
        return value;
    }
    /**
     * Add properties from Reader. Explicitly handled so as to enable
     * handling of import=<file> directive. Blank lines as well as
     * those beginning with a '#' character (comments) are ignored.
     * @param reader The buffered reader to read from.
     * to catch circular imports.
     * @throws IOException if anything goes wrong.
     */
    protected void addFromReader(Reader reader) throws IOException {

        String line = null;
        String key = null;
        String value = null;

        do {
            line = getOneLine(reader);

            if (line != null) {
                line = line.trim();

                if (line.length() == 0) {
                    continue;    // empty line
                }

                if (line.startsWith("#") || line.startsWith("!")) {
                    continue;    // comment line
                }

                parseArgument(line);
            }
        } while (line != null);
    }

    /**
     * Get a logical line. Any physical line ending in '\' is considered
     * to continue on the next line.
     * @param reader The input reader to read.
     * @return The resultant logical line which may have been constructed
     * from one or more physical lines.
     * @throws IOException if anything goes wrong.
     */
    protected String getOneLine(Reader reader) throws IOException {
        StringBuffer sb = null;
        String line = null;
        boolean continued;

        do {
            continued = false;

            try {
                line = readLine(reader);
                
                if (line != null) {
                    line = line.trim();
                    // If we already have something going ignore blank lines and comments
                    if ((sb != null)
                            && ((line.length() == 0) || 
                                (line.startsWith("#") || line.startsWith("!")))) {
                        continued = true;
                        continue;
                    }

                    continued = line.endsWith("\\");

                    if (continued) {    // delete the ending slash
                        line = line.substring(0, line.length() - 1);
                    }
                    if (sb == null) {
                        sb = new StringBuffer();
                    }
                    sb.append(line);
                }
            } catch (EOFException eof) {
                continued = false;
            }
        } while (continued);

        return (sb == null) ? null : sb.toString();
    }

    /**
     * Read one line from the Reader. A line may be terminated
     * by a single CR or LF, or the pair CR LF.
     * @param aReader The Reader to read characters from.
     * @return Next physical line.
     * @throws IOException if anything goes wrong.
     */
    protected String readLine(Reader aReader) throws IOException {
        StringBuffer sb = new StringBuffer();
        boolean done = false;
        while (!done) {
            int result = aReader.read();
            if (result == -1) {
                if (sb.length() > 0) {
                    break;
                }
                throw new EOFException();
            } else {
                char ch = (char)result;
                if (ch == '\n') {  // LF
                    if (CRState) {
                        CRState = false;
                        continue;                  
                    }
                    break;
                } else {
                    if (ch == '\r') {
                        CRState = true;
                        break;
                    } else {
                        sb.append(ch);
                        CRState = false;
                    }
                }
            }
        }
        return sb.toString();
    }        

    /**
     * List properties to provided PrintStream.
     * Output will be in sorted key sequence.
     * If a value is null, it will appear as "key=".
     * @param out The print stream.
     */
    public void list(PrintStream out) {
        for (Enumeration e = sortedKeys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = getProperty(key);
            if (value != null) {
	            out.println(key + "=" + value);
	        } else {
	            out.println(key + "=");
	        }
        }
	}

    /**
     * Create a String[] for the properties with one key=value pair per array entry.
     * If a value is null, it will appear as "key=".
     * @return The resultant String[].
     */
    public String[] toStringArray() {
        String[] result = new String[super.size()];
        int i = 0;
        for (Enumeration e = sortedKeys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            String value = getProperty(key);
            if (value != null) {
                result[i++] = key + "=" + value;
            } else {
                result[i++] = key + "=";
            }
        }
        return result;
	}

}
