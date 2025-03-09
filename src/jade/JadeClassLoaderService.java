package jade;

/**
 * The Interface JadeClassLoaderService describes the method structure that enables
 * to place an alternative method that loads classes that are described by their names.
 * In an OSGI context, this won't work, since each bundle has its own ClassLoader.  
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public interface JadeClassLoaderService {

	/**
	 * Returns the Class object associated with the class or interface with the given string name.
	 * @param className the fully qualified name of the desired class
	 * @return the class
	 */
	public Class<?> forName(String className) throws ClassNotFoundException, NoClassDefFoundError;
	
	/**
	 * Returns the Class object associated with the class or interface with the given string name, using the given class loader.
	 * @param className the fully qualified name of the desired class
	 * @param initialize whether the class must be initialized
	 * @param loader the class loader from which the class must be loaded
	 * @return the class
	 */
	public Class<?> forName(String className, boolean initialize, ClassLoader loader) throws ClassNotFoundException, NoClassDefFoundError;
	
}
