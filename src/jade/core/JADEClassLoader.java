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

package jade.core;

//#MIDP_EXCLUDE_FILE

/**
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
class JADEClassLoader extends ClassLoader {

	private AgentContainer classServer;
	private int verbosity;

  public JADEClassLoader(AgentContainer ac, int v) {
  	//#PJAVA_EXCLUDE_BEGIN
    super(Thread.currentThread().getContextClassLoader());
  	//#PJAVA_EXCLUDE_END
    classServer = ac;
    verbosity = v;
  }

  protected Class findClass(String name) throws ClassNotFoundException {
    byte[] classFile;

    try {
    	log("Remote retrieval of code for class "+name, 4);
      classFile = classServer.fetchClassFile(name);
    }
    catch (IMTPException re) {
      throw new ClassNotFoundException(name);
    } 

    if (classFile != null) {           	
    	log("Code of class "+name+" retrieved. Length is "+classFile.length, 4);
      return defineClass(name, classFile, 0, classFile.length);
    }
    else
      throw new ClassNotFoundException(name);
  }
  
  protected Class loadClass(String name,	
    	                    boolean resolve) throws ClassNotFoundException {
  
    Class c;
    log("Loading class "+name, 5);
    //#PJAVA_EXCLUDE_BEGIN
    c = super.loadClass(name, resolve);
    //#PJAVA_EXCLUDE_END
    /*#PJAVA_INCLUDE_BEGIN In PersonalJava loadClass(String, boolean) is abstract --> we must implement it
  	// 1) Try to see if the class has already been loaded
  	c = findLoadedClass(name);
  	
		// 2) Try to load the class using the system class loader
  	if (c == null) {
  		try {
  			c = findSystemClass(name);
  		}
  		catch (ClassNotFoundException cnfe) {
  		}
  	}
  	
  	// 3) If still not found, try to load the class from the proper site
  	if (c == null) {
  		c = findClass(name);
  	}
  	
  	if (resolve) {
  		resolveClass(c);
  	}
		#PJAVA_INCLUDE_END*/
    
		log("Class "+name+" loaded", 5);
  	return c;
	}  	
	
  private void log(String s, int level) {
  	if (verbosity >= level) {
	  	System.out.println("JCC-log: "+s);
  	}
  }  
}
