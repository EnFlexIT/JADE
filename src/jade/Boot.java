/**
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
 */

/** Tests:
 * java jade.Boot (with/without rmiregistry started): run a main-container
 * java jade.Boot -host pippo.cselt.it
 * java jade.Boot -host fbellif.cselt.it
 * java jade.Boot -host fbellif.cselt.it -port 1200
 * java jade.Boot -help
 * java jade.Boot -gui
 **/
package jade;

//#ALL_EXCLUDE_FILE

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.File;
import java.io.PrintStream;
import java.util.Enumeration;    // J2ME CLDC OK
import java.util.Vector;         // J2ME CLDC OK

import jade.core.Runtime;
import jade.core.Profile;
//import jade.core.BootProfileImpl;
import jade.core.Specifier;
import jade.util.BasicProperties;
import jade.util.PropertiesException;
//import jade.security.SecurityFactory;

/**
 * Boots <B><em>JADE</em></b> system, parsing command line arguments.
 * @author Giovanni Rimassa - Universita' di Parma
 * @author Dick Cowan - HP Labs
 * @version $Date$ $Revision$
 */
public class Boot {


    /** separator between the agent name and the agent class */
    private static final String NAME2CLASS_SEPARATOR = ":";
    private BasicProperties properties = null;
    private BootProfileImpl profile = null;

    /**
     * Main entry point for invocation.
     * @param args The command line arguments. These use the form key:value
     * or -key (shorthand for key:true).
     */
    public static void main(String args[]) {
        new Boot(args);
    }

    /**
     * Constructor. Starts Jade with provided arguments.
     * @param args The command line arguments. These use the form key:value
     */
    public Boot(String[] args) {
        try {
            profile = new BootProfileImpl(prepareArgs(args)); // qui adesso gli passa anche dbconf
        } catch (PropertiesException pe) {
            System.out.println(pe);
            System.exit(-1);
        } 
        properties = profile.getArgProperties();

        if (properties.getBooleanProperty(profile.DUMP_KEY, false)) {
            listProperties(System.out);
        }

        if (properties.getBooleanProperty(profile.VERSION_KEY, false)) {
            System.out.println(Runtime.getCopyrightNotice());
            return;
        }

        if (properties.getBooleanProperty(profile.HELP_KEY, false)) {
            usage(System.out);
            return;
        }

        if (properties.getProperty(profile.MAIN_HOST) == null) {
            try {
                properties.setProperty(profile.MAIN_HOST, InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException uhe) {
                System.out.print("Unknown host exception in getLocalHost(): ");
                System.out.println(" please use '-host' and/or '-port' options to setup JADE host and port");
                System.exit(1);
            }
        }

        if (properties.getBooleanProperty(profile.CONF_KEY, false)) {
            new BootGUI(this);
            if (properties.getBooleanProperty(profile.DUMP_KEY, false)) {
                listProperties(System.out);
            }
        }


        // --- initialize JVM-scoped Security Factory ---
        // (the security settings contained into this profile
        //  will be used for all containers into this JVM in
        //  despite of settings into profile of other future containers)
        //SecurityFactory sf = SecurityFactory.getSecurityFactory(profile);


        try {
					check();
					// Exit the JVM when there are no more containers around
					Runtime.instance().setCloseVM(true);
					if (profile.getBooleanProperty(Profile.MAIN, true)) {
						Runtime.instance().createMainContainer(profile);
					}
					else {
						Runtime.instance().createAgentContainer(profile);
					}
        } catch (BootException be) {
            System.err.println(be);
            return;
        }
    }

    /**
     * Transform original style boot arguments to new form.
     * <pre>
     * In the following 'x' and 'y' denote arbitrary strings; 'n' an integer.
     * Transformation Rules:
     * Original       New
     * ------------------------------
     * -host x        host:x
     * -owner x       owner:x
     * -name x        name:x
     * -port n        port:n
     * -mtp  x        mtp:x
     * -aclcodec:x    aclcodec:x
     * -conf x        import:x
     * -conf          -conf
     * -container     -container
     * -gui           -gui
     * -version       -version
     * -v             -version
     * -help          -help
     * -h             -help
     * -nomtp         -nomtp
     * -nomobility    -nomobility
     * -y x           y:x
     * agent list     agents:agent list
     * </pre>
     * If the arguments contain either import:x or agents:x
     * we will assume that the arguments are already in the new
     * format and leave them alone. For "import:" we test if
     * what follows is a file name and in the event it isn't we
     * assume that it was if there are any other "-x" options following.
     * <p>
     * You can't mix the old form with the new as this would make the
     * distinction between foo:bar as meaning a property named foo with
     * a value bar or an agent named foo implmented by class bar impossible.
     * <p>
     * @param args The command line arguments.
     */
    protected String[] prepareArgs(String[] args) {
        boolean printUsageInfo = false;

        if ((args == null) || (args.length == 0)) {
            // printUsageInfo = true;
        } else {
            boolean isNew = false;
            boolean likely = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("import:")) {
                    int j = args[i].indexOf(':');
                    isNew = ( (j < args[i].length()-1) && (isFileName(args[i].substring(j+1))) );
                    likely = !isNew;  // in case malformed file name
                } else
                if (args[i].startsWith("agents:")) {
                    isNew = true;
                } else
                if (args[i].startsWith("-") && likely) {
                    isNew = true;
                } 
            }

            if (isNew) {
                return args;
            }
        }

        int n = 0;
        boolean endCommand =
            false;    // true when there are no more options on the command line
        Vector results = new Vector();

        while ((n < args.length) &&!endCommand) {
            String theArg = args[n];

            if (theArg.equalsIgnoreCase("-conf")) {
                if (++n == args.length) {
                    // no modifier
                    results.add(theArg);
                } else {
                    // Use whatever is next as a candidate file name
                    String nextArg = args[n];
                    if (isFileName(nextArg)) {
                        // it was a file name
                        results.add("import:" + nextArg);
                    } else {
                        // its either an illformed file name or something else
                        results.add(theArg);
                        n--;
                    }
                }
            } else if (theArg.equalsIgnoreCase("-host")) {
                if (++n == args.length) {
                    System.err.println("Missing host name ");

                    printUsageInfo = true;
                } else {
                    results.add("host:" + args[n]);
                }
            }else if (theArg.equalsIgnoreCase("-owner")) {
                if (++n == args.length) {

					// "owner:password" not provided on command line
					results.add("owner:" + ":");

                } else {
                    results.add("owner:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-name")) {
                if (++n == args.length) {
                    System.err.println("Missing platform name");

                    printUsageInfo = true;
                } else {
                    results.add("name:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-imtp")) {
                if (++n == args.length) {
                    System.err.println("Missing IMTP class");

                    printUsageInfo = true;
                } else {
                    results.add("imtp:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-port")) {
                if (++n == args.length) {
                    System.err.println("Missing port number");

                    printUsageInfo = true;
                } else {
                    try {
                        Integer.parseInt(args[n]);
                    } catch (NumberFormatException nfe) {
                        System.err.println("Wrong int for the port number");

                        printUsageInfo = true;
                    }

                    results.add("port:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-container")) {
                results.add(theArg);
	    } else if (theArg.equalsIgnoreCase("-backupmain")) {
		results.add(theArg);
            } else if (theArg.equalsIgnoreCase("-gui")) {
                results.add(theArg);
            } else if (theArg.equalsIgnoreCase("-version")
                       || theArg.equalsIgnoreCase("-v")) {
                results.add("-version");
            } else if (theArg.equalsIgnoreCase("-help")
                       || theArg.equalsIgnoreCase("-h")) {
                results.add("-help");
            } else if (theArg.equalsIgnoreCase("-nomtp")) {
                results.add(theArg);
            } else if(theArg.equalsIgnoreCase("-nomobility")){
                results.add(theArg);
            } else if (theArg.equalsIgnoreCase(
                    "-dump")) {    // new form but useful for debugging
                results.add(theArg);
            } else if (theArg.equalsIgnoreCase("-mtp")) {
                if (++n == args.length) {
                    System.err.println("Missing mtp specifiers");

                    printUsageInfo = true;
                } else {
                    results.add("mtp:" + args[n]);
                }
            } else if (theArg.equalsIgnoreCase("-aclcodec")) {
                if (++n == args.length) {
                    System.err.println("Missing aclcodec specifiers");

                    printUsageInfo = true;
                } else {
                    results.add("aclcodec:" + args[n]);
                }
            } else if (theArg.startsWith("-") && n+1 < args.length) {
            	// Generic option
            	results.add(theArg.substring(1)+":"+args[++n]);
            } else {
                endCommand = true;    //no more options on the command line
            }

            n++;    // go to the next argument
        }    // end of while

        // all options, but the list of Agents, have been parsed
        if (endCommand) {    // parse the list of agents, now
            --n;    // go to the previous argument

            StringBuffer sb = new StringBuffer();

            for (int i = n; i < args.length; i++) {
                sb.append(args[i] + " ");
            }

            results.add("agents:" + sb.toString());
        }

        if (printUsageInfo) {
            results.add("-help");
        }

        String[] newArgs = new String[results.size()];

        for (int i = 0; i < newArgs.length; i++) {
            newArgs[i] = (String) results.elementAt(i);
        }

        return newArgs;
    }

    /**
     * Test if an argument actually references a file.
     * @param arg The argument to test.
     * @return True if it does, false otherwise.
     */
    protected boolean isFileName(String arg) {
        File testFile = new File(arg);
        return testFile.exists();
    }

    /**
     * Show usage information.
     * @param out The print stream to output to.
     */
    public void usage(PrintStream out) {
        out.println("Usage: java jade.Boot [options] [agent specifiers]");
        out.println("");
        out.println("where options are:");
        out.println("  -host <host name>\tHost where RMI registry for the platform is located");
        out.println("  -port <port number>\tThe port where RMI registry for the platform resides");
        out.println("  -gui\t\t\tIf specified, a new Remote Management Agent is created.");
        out.println("  -container\t\tIf specified, a new Agent Container is added to an existing platform");
        out.println("  \t\t\tOtherwise a new Agent Platform is created");
        out.println("  -conf\t\t\tShows the gui to set the configuration properties to start JADE.");
        out.println("  -conf <file name>\tStarts JADE using the configuration properties read in the specified file.");
        out.println("  -dump\t\t\tIf specified, lists boot's current properties.");
        out.println("  -version\t\tIf specified, current JADE version number and build date is printed.");
        out.println("  -mtp\t\t\tSpecifies a list, separated by ';', of external Message Transport Protocols to be activated.");
        out.println("  \t\t\tBy default the HTTP-MTP is activated on the main-container and no MTP is activated on the other containers.");
        out.println("  -nomtp\t\tHas precedence over -mtp and overrides it.");
        out.println("  \t\t\tIt should be used to override the default behaviour of the main-container (by default the -nomtp option unselected).");
        out.println("  -aclcodec\t\tSpecifies a list, separated by ';', of ACLCodec to use. By default the string codec is used.");
        out.println("  -name <platform name>\tThe symbolic platform name specified only for the main container.");
        out.println("  -owner <username:password>\tThe owner of a container or platform.");
	out.println("  -nomobility\t\tIf specified, disables the mobility and cloning support for the container.");
        out.println("  -auth <Simple|Unix|NT|Kerberos>\tThe user authentication module to be used.");
        out.println("  -help\t\t\tPrints out usage informations.");
        out.println("  -<key> <value>\t\tApplication specific options.");
        out.println("");
        out.print("An agent specifier is composed of an agent name and an agent class, separated by \"");
        out.println(NAME2CLASS_SEPARATOR + "\"");
        out.println("");
        out.println("Take care that the specified agent name represents only the local name of the agent.");
        out.println("Its guid (globally unique identifier) is instead assigned by the AMS after concatenating");
        out.println("the home agent platform identifier (e.g. john@foo.cselt.it:1099/JADE)");
        out.println("");
        out.println("Examples:");
        out.println("  Connect to default platform, starting an agent named 'peter'");
        out.println("  implemented in 'myAgent' class:");
        out.println("  \tjava jade.Boot -container peter:myAgent");
        out.println("");
        out.println("  Connect to a platform on host zork.zot.za, on port 1100,");
        out.println("  starting two agents");
        out.println("  java jade.Boot -container -host zork.zot.za -port 1100 peter:heAgent paula:sheAgent");
        out.println("");
        out.println("  Create an Agent Platform and starts an agent on the local Agent Container");
        out.println("  \tjava jade.Boot Willy:searchAgent");
        out.println("");
        System.exit(0);
    }

    /**
     * List boot properties to provided print stream.
     * @param out PrintStream to list properties to.
     */
    public void listProperties(PrintStream out) {
        out.println("---------- Jade Boot property values ----------");
        for (Enumeration e = properties.sortedKeys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            out.println(key + "=" + properties.getProperty(key));
        }
        out.println("-----------------------------------------------");
    }    

    /**
     * Get boot properties.
     * @return BasicProperties Boot properties.
     */
    public BasicProperties getProperties() {
        return properties;
    }

    /**
     * Set boot properties. Copies provided properties over existing ones.
     * @param updates Properties to be copied.
     */
    public void setProperties(BasicProperties updates) throws BootException {
        properties.copyProperties(updates);
        profile.setArgProperties(properties);
    }

    /**
     * This method verifies the configuration properties and eventually correct them.
     * It checks if the port number is a number greater than 0 otherwise it throws a BootException,
     * and if the -nomtp has been set, then delete some other mtp wrongly set.
     * If the user wants to start a platform the host
     * must be the local host so if a different name is speficied it
     * will be corrected and an exception will be thrown.
     * @throws BootException if anything is found to be inconsistent.
     */
    protected void check() throws BootException {
        try {
            Integer.parseInt(profile.getParameter(profile.MAIN_PORT, Integer.toString(profile.DEFAULT_PORT)));
        } catch (NumberFormatException nfe) {
            throw new BootException("Malformed port number");
        }

        // Remove the MTP list if '-nomtp' is specified
        if (profile.getBooleanProperty(profile.NOMTP_KEY, false)) {
            if (profile.getParameter(profile.MTP_KEY, null) != null) {
                throw new BootException("Error: If noMTP is set, you can't specify MTPs.");
            }
        }

        // Check that the local-host is actually local
        try {
            InetAddress myPlatformAddrs[] = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            InetAddress hostAddrs[] = InetAddress.getAllByName(profile.getParameter(profile.LOCAL_HOST, null));

            // The trick here is to compare the InetAddress
            // objects, not the strings since the one string might be a
            // fully qualified Internet domain name for the host and the 
            // other might be a simple name.  
            // Example: myHost.hpl.hp.com and myHost might
            // acutally be the same host even though the hostname strings do
            // not match.  When the InetAddress objects are compared, the IP
            // addresses will be compared.
            int i = 0;
            boolean isLocal = false;

            while ((!isLocal) && (i < myPlatformAddrs.length)) {
                int j = 0;

                while ((!isLocal) && (j < hostAddrs.length)) {
                    isLocal = myPlatformAddrs[i].equals(hostAddrs[j]);

                    j++;
                }

                i++;
            }

            if (!isLocal) {
                throw new BootException("Error: Not possible to launch JADE a remote host ("+properties.getProperty(profile.LOCAL_HOST)+"). Check the -host and -local-host options.");
            }
        } catch (UnknownHostException uhe) {
            throw new BootException("Error: Not possible to launch JADE an unknown host ("+properties.getProperty(profile.LOCAL_HOST)+"). Check the -host and -local-host options.");
        }
    }



/*    
     -- Authentication is moving to a service --
 
    // get user/pass from the user and 
    // put them into the profile
    private void getUsernamePassword(SecurityFactory sf) {

      // check if username:password has been provided
      String own = properties.getProperty(profile.OWNER);
      if (own != null) {
        String user = "";
        char[] passwd = new char[1]; // initial value to avoid java error;
  
        // separate username:password
        int sep = own.indexOf(':');
        if (sep > 0) {
          user = own.substring(0, sep);
          passwd = new char[ (own.length()) - sep];
          own.getChars(sep + 1, own.length(), passwd, 0);
        }
        else {
          // no ":" found
          user = own;
          if (sep == 0)
            user = "";
        }
  
        // if username or passwd have not been provided
        // ask for them
        try {
          if ( (user.length() == 0)) {
            // ask for user:password
            jade.security.PwdDialog pwdD = sf.getPwdDialog();
            pwdD.ask();
            user = pwdD.getUserName();
            passwd = pwdD.getPassword();
          }
          else {
            if ( (sep < 0)) {
              // ask for the password
              jade.security.PwdDialog pwdD = sf.getPwdDialog();
              pwdD.setUserName(user);
              pwdD.ask();
              user = pwdD.getUserName();
              passwd = pwdD.getPassword();
            } // end if
          } // end if
        }
        catch (Exception e) {
          //} catch (jade.core.ProfileException e) {
          e.printStackTrace();
        }
  
        // set username:password
        profile.setParameter(profile.OWNER, user + ":" + new String(passwd));
      } // end if (own != null) 
    } // end getUsernamePassword()
*/

} // end class Boot
