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

/**
 * This Java application test the examples of Daniel Le Berre about JADE/JSP
 * integration.
 * This tester must be started via the following command line:
 * java -cp jade.jar jade.Boot buffer:jade.tools.DummyAgent.DummyAgent
 * java -cp jade.jar;jadeExamplesdirectory TestDanielExamples 
 *
 * The test works if a message arrives to the DummyAgent in the main container
 *
 * @author Fabio Bellifemine - TILab 
 * @version $Date$ $Revision$
 **/
public class TestDanielExample {

    public static void main(String args[]) {
	examples.jsp.Snooper snooper = new examples.jsp.Snooper();
	try {
	    String [] _args = {"-container"};	
	    jade.Boot.main(_args);
	    snooper.doStart("snooper");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	snooper.snoop("IT WORKS!"); 
    }
}
