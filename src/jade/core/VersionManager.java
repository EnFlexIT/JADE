package jade.core;

import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import jade.util.Logger;

//#MIDP_EXCLUDE_FILE
//#DOTNET_EXCLUDE_FILE

public class VersionManager {
	
	private static Logger logger = Logger.getMyLogger(VersionManager.class.getName());
	
	private static final String GROUP = "Jade Informations";
	private static final String WCVER = "Specification-Version";
	private static final String WCREV = "SVN-Revision";
	private static final String WCDATE = "SVN-Date";
	
	private boolean isVersionFromOSGIBundle;
	
	private static final String BUNDLE_SYMBOLIC_NAME  = "Bundle-SymbolicName";
	private static final String BUNDLE_VERSION = "Bundle-Version";
	private static final String BUNDLE_DATE  = "Bundle-Date";
	private static final String BUNDLE_VENDOR  = "Bundle-Vendor";
	
	private Attributes attributes;
	

	/**
	 * Instantiates a new version manager.
	 */
	public VersionManager() {
		
		this.isVersionFromOSGIBundle = this.loadVersionInfoFromSOGIBundle(); 
		if (this.isVersionFromOSGIBundle==false) {
			this.loadVersionInfoFromJar();
		}
	}
	
	/**
	 * Load version info from SOGI bundle (the new, preferred way).
	 * @return true, if successful
	 */
	private boolean loadVersionInfoFromSOGIBundle() {
		
		try {
			Manifest manifest = this.getManifest();
			attributes = manifest.getMainAttributes();
			return true;
			
		} catch (Exception e) {
			logger.log(Logger.WARNING, "Error retrieving version information", e);
		}
		return false;
	}
	
	/**
	 * Load version info from jar (the legacy JADE way).
	 * @return true, if successful
	 */
	private boolean loadVersionInfoFromJar() {
		
		try {

			// Check if class is into jar
			String classPath = this.getClassPath();
			if (!classPath.startsWith("jar")) {
				logger.log(Logger.WARNING, "VersionManager not from jar -> no version information available");
				return false;
			}

			Manifest manifest = this.getManifest();
			attributes = manifest.getAttributes(GROUP);
			return true;
			
		} catch (Exception e) {
			logger.log(Logger.WARNING, "Error retrieving versions info", e);
		}
		return false;
	}
	
	/**
	 * Gets the class path.
	 * @return the class path
	 */
	private String getClassPath() {
		Class<?> clazz = this.getClass();
		String className = clazz.getSimpleName() + ".class";
		return clazz.getResource(className).toString();
	}
	/**
	 * Gets the manifest.
	 * @return the manifest
	 */
	private Manifest getManifest() {
		
		Manifest manifest = null;
		try {
			InputStream is = this.getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			manifest = new Manifest(is);			
			is.close();
			
		} catch (Exception e) {
			logger.log(Logger.WARNING, "Error retrieving versions info", e);
		}
		return manifest;
	}
	
	
	
	public String getVersion() {
		if (this.attributes != null) {
			if (this.isVersionFromOSGIBundle==true) {
				return this.attributes.getValue(BUNDLE_VERSION);
			} 
			return this.attributes.getValue(WCVER);
		} else {
			return "UNKNOWN";
		}
	}
	
	public String getRevision() {
		if (this.attributes != null) {
			if (this.isVersionFromOSGIBundle==true) {
				return null;
			}
			return this.attributes.getValue(WCREV);
		} else {
			return "UNKNOWN";
		}
	}
	
	public String getDate() {
		if (this.attributes != null) {
			if (this.isVersionFromOSGIBundle==true) {
				return this.attributes.getValue(BUNDLE_DATE);
			}
			return this.attributes.getValue(WCDATE);
		} else {
			return "UNKNOWN";
		}
	}

	public String getBundleName() {
		if (this.attributes != null && this.isVersionFromOSGIBundle==true) {
			return this.attributes.getValue(BUNDLE_SYMBOLIC_NAME);
		}
		return "UNKNOWN";
	}

	public String getBundleVendor() {
		if (this.attributes != null && this.isVersionFromOSGIBundle==true) {
			return this.attributes.getValue(BUNDLE_VENDOR);
		}
		return "UNKNOWN";
	}

}
