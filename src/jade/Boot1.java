/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

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
package jade;

// import java.net.InetAddress;
// import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.lang.Boolean;
import java.util.StringTokenizer;
import java.util.Stack;
import java.io.*;

// import javax.swing.JOptionPane;
// import jade.gui.BootGUI;
// import jade.util.leap.*;
import jade.core.ProfileException;
import jade.core.ProfileImpl;
import jade.core.Profile;
import jade.core.Runtime;
import java.io.IOException;

/**
 * Boots <B><em>JADE</em></b> system, parsing command line arguments.
 * 
 * @author Giovanni Rimassa - Universita` di Parma
 * @version $Date$ $Revision$
 * 
 */
public class Boot1 {

    // This separates agent name from agent class on the command line
    private static final String SEPARATOR = ":";

    /**
     * Return a String with copyright Notice, Name and Version of this version of JADE
     */
    public static String getCopyrightNotice() {
        String CVSname = "$Name$";
        String CVSdate = "$Date$";
        int    colonPos = CVSname.indexOf(":");
        int    dollarPos = CVSname.lastIndexOf('$');
        String name = CVSname.substring(colonPos + 1, dollarPos);

        if (name.indexOf("JADE") == -1) {
            name = "JADE snapshot";
        } 
        else {
            name = name.replace('-', ' ');
            name = name.replace('_', '.');
            name = name.trim();
        } 

        colonPos = CVSdate.indexOf(':');
        dollarPos = CVSdate.lastIndexOf('$');

        String date = CVSdate.substring(colonPos + 1, dollarPos);

        date = date.trim();

        return ("    This is " + name + " - " + date 
                + "\n    downloaded in Open Source, under LGPL restrictions,\n    at http://sharon.cselt.it/projects/jade\n");
    } 

    /**
     * Fires up the <b><em>JADE</em></b> system.
     * This method initializes the Profile Manager and then starts the
     * bootstrap process for the <B><em>JADE</em></b>
     * agent platform.
     */
    public static void main(String args[]) {

        // Print copyright notice
        System.out.println(getCopyrightNotice());

        // Check usage
        if (args.length != 1) {
            System.err.println("Usage: java jade.Boot <filename>");
            System.exit(-1);
        } 

        // Initialize the Profile
        Profile p = new ProfileImpl(args[0]);
				Runtime.instance().createAgentContainer(p);
    } 

}

