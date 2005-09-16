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
 
package jade.core.management;

//#J2ME_EXCLUDE_FILE

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
/**
 * This ClassLoader is intended to be used to load agent classes
 * packed within a jar file. If the specified jar does not exist this ClassLoader
 * will attempt to load classes from the system ClassLoader.
 * @author <a href="mailto:Joan.Ametller@uab.es">Joan Ametller</a>
 * 
 */
public class JarClassLoader extends ClassLoader {
  
	public static final int BUFFER_SIZE = 1024;
	
  /**
   * @param Path and name of the JAR file
   * @throws IOException If there are problems opening the file
   */
  public JarClassLoader(File f, ClassLoader parent) throws IOException{
    super(parent);
    if (f != null) {
    	_file = new JarFile(f);
    }
  	/*File file = new File(filename);
    if(file.exists()){
      _file = new JarFile(new File(filename));
    }*/
  }
  
  /**
   * Method to close file descriptor for the JAR used by this ClassLoader.
   * If this method is invoked, no more classes from the JAR file will be loaded
   * using this ClassLoader.
   * @throws IOException File cannot be closed
   */
  public void close() throws IOException{
    if(_file != null) _file.close();
  }
  
  protected Class findClass(String className) throws ClassNotFoundException{
    if (_file != null) {
	  	ZipEntry zEntry = _file.getEntry(className.replace('.', '/') + ".class");
	    try{
	      InputStream is = _file.getInputStream(zEntry);
	      byte[] rawClass = readFully(is);
	      is.close();
	      return defineClass(className, rawClass, 0, rawClass.length);
	    }
	    catch(IOException ioe){
	      throw new ClassNotFoundException("IOError while reading jar file for class "+className+". "+ioe);
	    }
    }
    else {
    	throw new ClassNotFoundException(className);
    }
  }
  
  private byte[] readFully(InputStream is) throws IOException{
  	byte[] buffer = new byte[BUFFER_SIZE];
  	ByteArrayOutputStream baos = new ByteArrayOutputStream();
  	int read = 0;
  	
  	while((read=is.read(buffer))>= 0) baos.write(buffer,0,read);
  	
  	return baos.toByteArray();
  }
  
  private JarFile _file = null;

}
