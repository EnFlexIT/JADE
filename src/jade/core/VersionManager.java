package jade.core;

import java.io.InputStream;
import java.net.URL;
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
	
	private Attributes attributes;
	
	public VersionManager() {
		try {
			Class clazz = this.getClass();
			String className = clazz.getSimpleName() + ".class";
			String classPath = clazz.getResource(className).toString();
			
			// Check if class is into jar 
			if (!classPath.startsWith("jar")) {
				logger.log(Logger.WARNING, "VersionManager not from jar -> no version information available");
			  return;
			}

			// Get manifest attributes 
			String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
			InputStream is = new URL(manifestPath).openStream();
			Manifest manifest = new Manifest(is);			
			attributes = manifest.getAttributes(GROUP);
			is.close();
		}
		catch (Exception e) {
			logger.log(Logger.WARNING, "Error retrieving versions info", e);
		}
	}
	
	public String getVersion() {
		if (attributes != null) {
			return attributes.getValue(WCVER);
		}
		else {
			return "UNKNOWN";
		}
	}
	
	public String getRevision() {
		if (attributes != null) {
			return attributes.getValue(WCREV);
		}
		else {
			return "UNKNOWN";
		}
	}
	
	public String getDate() {
		if (attributes != null) {
			return attributes.getValue(WCDATE);
		}
		else {
			return "UNKNOWN";
		}
	}

}
