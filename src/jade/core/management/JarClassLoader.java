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
 * @author <a href="mailto:Joan.Ametller@gmail.com">Joan Ametller Esquerra </a>
 * @author <a href="mailto:jcucurull@deic.uab.cat">Jordi Cucurull Juan</a>
 * 
 * @version 1.1
 */
public class JarClassLoader extends ClassLoader {

	public static final int BUFFER_SIZE = 1024;

	/**
	 * @param Path and name of the JAR file
	 * @throws IOException If there are problems opening the file
	 */
	public JarClassLoader(File f, ClassLoader parent) throws IOException {
		super(parent);
		_file = f;
	}

	/**
	 * Method to open the JAR file until the "close" method
	 * is called. This optimizes the consecutive class loading.
	 *
	 */
	public void enablePersistentOpen() throws IOException {
		if (_file != null) {
			_jarFile = new JarFile(_file);
		}

		_persistentOpenFile = true;
	}

	/**
	 * Method to disable the persistent mode of the JAR classloader.
	 * @throws IOException File cannot be closed
	 */
	public void disablePersistentOpen() throws IOException {
		_persistentOpenFile = false;
		if (_jarFile != null)
			_jarFile.close();
	}
	
	/**
	 * Method which in other releases closed the JAR file.
	 * Currently this file is opened and close at every "findclass"
	 * call or when it is specified by the methods "enablePersistentOpen"
	 * and "disablePersistentOpen".
	 * @throws IOException Currently this exception is never returned.
	 * @deprecated
	 */
	public void close() throws IOException {
	}

	protected Class findClass(String className) throws ClassNotFoundException {

		if (!_persistentOpenFile) {
			try {
				if (_file != null) {
					_jarFile = new JarFile(_file);
				}
			} catch (IOException ioe) {
				throw new ClassNotFoundException();
			}
		}

		if (_jarFile != null) {
			ZipEntry zEntry = _jarFile.getEntry(className.replace('.', '/')
					+ ".class");

			try {
				InputStream is = _jarFile.getInputStream(zEntry);
				byte[] rawClass = readFully(is);
				is.close();
				return defineClass(className, rawClass, 0, rawClass.length);
			} catch (IOException ioe) {
				throw new ClassNotFoundException(
						"IOError while reading jar file for class " + className
								+ ". " + ioe);
			}
			finally {
				if (!_persistentOpenFile) {
					if (_jarFile != null)
						try {
							_jarFile.close();
						} catch (IOException ioe) {
							System.out.println("JARClassloader: Error closing a Jar file.");
						}
				}	
			}
		} else {
			throw new ClassNotFoundException(className);
		}
		
	}

	private byte[] readFully(InputStream is) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int read = 0;

		while ((read = is.read(buffer)) >= 0)
			baos.write(buffer, 0, read);

		return baos.toByteArray();
	}

	private JarFile _jarFile = null;
	private File _file = null;
	private boolean _persistentOpenFile = false;

}

