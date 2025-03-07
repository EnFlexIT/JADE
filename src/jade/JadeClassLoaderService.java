package jade;

public interface JadeClassLoaderService {

	/**
	 * Returns the Class object associated with the class or interface with the given string name.
	 * @param className the fully qualified name of the desired class
	 * @return the class
	 */
	public Class<?> forName(String className);
	
	/**
	 * Returns the Class object associated with the class or interface with the given string name, using the given class loader.
	 * @param className the fully qualified name of the desired class
	 * @param initialize whether the class must be initialized
	 * @param loader the class loader from which the class must be loaded
	 * @return the class
	 */
	public Class<?> forName(String className, boolean initialize, ClassLoader loader);
	
}
