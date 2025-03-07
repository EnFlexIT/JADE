package jade;

/**
 * The Class JadeClassLoader.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JadeClassLoader {

	private static JadeClassLoaderService classLoaderService;
	
	/**
	 * Central forName method for getting class instances from the JADE bundle.
	 *
	 * @param className the fully qualified name of the desired class
	 * @return the class
	 * @throws ClassNotFoundException the class not found exception
	 */
	public static Class<?> forName(String className) throws ClassNotFoundException {
		
		if (classLoaderService!=null) {
			return classLoaderService.forName(className);
		}
		return Class.forName(className);
	}
	
	/**
	 * Central forName method for getting class instances from the JADE bundle, using the given class loader.
	 * @param className the fully qualified name of the desired class
	 * @param initialize whether the class must be initialized
	 * @param loader the class loader from which the class must be loaded
	 * @return the class
	 * @throws ClassNotFoundException the class not found exception
	 */
	public static Class<?> forName(String className, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
		if (classLoaderService!=null) {
			return classLoaderService.forName(className, initialize, loader);
		}
		return Class.forName(className, initialize, loader);
	}
	
	/**
	 * Sets the class loader service.
	 * @param classLoaderService the new class loader service
	 */
	public static void setClassLoaderService(JadeClassLoaderService classLoaderService) {
		JadeClassLoader.classLoaderService = classLoaderService;
	}
	
}
