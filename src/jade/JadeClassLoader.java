package jade;

/**
 * The Class JadeClassLoader.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JadeClassLoader {

	private static JadeClassLoaderService classLoaderService;
	
	/**
	 * Central forName method for getting class instances the JADE bundle.
	 *
	 * @param className the class name
	 * @return the class
	 * @throws ClassNotFoundException the class not found exception
	 */
	public static Class<?> forName(String className) throws ClassNotFoundException {
		
		if (classLoaderService!=null) {
			return classLoaderService.forName(className);
		}
		return JadeClassLoader.forName(className);
	}
	/**
	 * Sets the class loader service.
	 * @param classLoaderService the new class loader service
	 */
	public static void setClassLoaderService(JadeClassLoaderService classLoaderService) {
		JadeClassLoader.classLoaderService = classLoaderService;
	}
	
}
