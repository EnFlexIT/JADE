package leapTools;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.DirectoryScanner;

import java.io.*;

/**
   ANT-task preprocessing JADE sources into JADE-LEAP sources
   for J2SE, PJAVA and MIDP environments
   @author Giovanni Caire - TILAB
 */
public class Preprocessor extends Task {
	// File exclusion markers
	private static final String ALL_EXCLUDE_FILE_MARKER = "//#ALL_EXCLUDE_FILE"; 
	private static final String J2ME_EXCLUDE_FILE_MARKER = "//#J2ME_EXCLUDE_FILE";
	private static final String PJAVA_EXCLUDE_FILE_MARKER = "//#PJAVA_EXCLUDE_FILE";
	private static final String MIDP_EXCLUDE_FILE_MARKER = "//#MIDP_EXCLUDE_FILE";
	
	// Code exclusion/inclusion markers
	private static final String ALL_EXCLUDE_BEGIN_MARKER = "//#ALL_EXCLUDE_BEGIN"; 
	private static final String ALL_EXCLUDE_END_MARKER = "//#ALL_EXCLUDE_END";
	
	private static final String J2SE_INCLUDE_BEGIN_MARKER = "//#J2SE_INCLUDE_BEGIN"; 
	private static final String J2SE_INCLUDE_END_MARKER = "//#J2SE_INCLUDE_END";
	
	private static final String J2ME_EXCLUDE_BEGIN_MARKER = "//#J2ME_EXCLUDE_BEGIN"; 
	private static final String J2ME_EXCLUDE_END_MARKER = "//#J2ME_EXCLUDE_END";
	private static final String J2ME_INCLUDE_BEGIN_MARKER = "/*#J2ME_INCLUDE_BEGIN"; 
	private static final String J2ME_INCLUDE_END_MARKER = "#J2ME_INCLUDE_END*/";

	private static final String PJAVA_EXCLUDE_BEGIN_MARKER = "//#PJAVA_EXCLUDE_BEGIN"; 
	private static final String PJAVA_EXCLUDE_END_MARKER = "//#PJAVA_EXCLUDE_END";
	private static final String PJAVA_INCLUDE_BEGIN_MARKER = "/*#PJAVA_INCLUDE_BEGIN"; 
	private static final String PJAVA_INCLUDE_END_MARKER = "#PJAVA_INCLUDE_END*/";

	private static final String MIDP_EXCLUDE_BEGIN_MARKER = "//#MIDP_EXCLUDE_BEGIN"; 
	private static final String MIDP_EXCLUDE_END_MARKER = "//#MIDP_EXCLUDE_END";
	private static final String MIDP_INCLUDE_BEGIN_MARKER = "/*#MIDP_INCLUDE_BEGIN"; 
	private static final String MIDP_INCLUDE_END_MARKER = "#MIDP_INCLUDE_END*/";
	
	// No-debug version generation markers
	private static final String NODEBUG_EXCLUDE_BEGIN_MARKER = "//#NODEBUG_EXCLUDE_BEGIN"; 
	private static final String NODEBUG_EXCLUDE_END_MARKER = "//#NODEBUG_EXCLUDE_END";
	
	// Preprocessing types
	private static final String J2SE = "j2se"; 
	private static final String PJAVA = "pjava"; 
	private static final String MIDP = "midp"; 

	private boolean verbose = false;
	private int removedCnt = 0;
	
	// The file that is being preprocessed
	private String target;
	
	// The base directory for recursive pre-processing
	private String basedir;
	
	// The type of preprocessing
	private String type;

	// The setter method for the "target" attribute
	public void setTarget(String target) {
		this.target = target;
	}
	
	// The setter method for the "basedir" attribute
	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}
	
	// The setter method for the "type" attribute
	public void setType(String type) {
		this.type = type;
	}
	
	// The setter method for the "verbose" attribute
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	// The method executing the task
	public void execute() throws BuildException {
		if (basedir != null) {
			// Recursive preprocessing (ignore target argument if any)
			File d = new File(basedir);
			if (d.isDirectory()) {
				DirectoryScanner scanner = new DirectoryScanner();
				scanner.setBasedir(d);
				scanner.setIncludes(new String[] {"**/*.java"});
				scanner.scan();
				String[] files = scanner.getIncludedFiles();
				System.out.println("Preprocessing "+files.length+" files.");
				for (int i = 0; i < files.length; ++i) {
					execute(d.getPath()+"/"+files[i]);
				}
				System.out.println("Removed "+removedCnt+" files.");
			}
			else {
				throw new BuildException("Error: "+basedir+" is not a directory.");
			}
		}
		else {
			// Slingle file preprocessing (use target argument)
			execute(target);
		}
	}
	
	// The method executing the task
	private void execute(String file) throws BuildException {
		if (verbose) {
			System.out.println("Preprocessing file "+file+" (type: "+type+")");
		}
		
		try {
			// Get a reader to read the file
			File targetFile = new File(file);
			FileReader fr = new FileReader(targetFile);
			BufferedReader reader = new BufferedReader(fr);
			
			// Get a writer to write into the preprocessed file
			File preprocFile = File.createTempFile(targetFile.getName(), "tmp", targetFile.getParentFile()); 
			FileWriter fw = new FileWriter(preprocFile);
			BufferedWriter writer = new BufferedWriter(fw);
			
			// Prepare appropriate preprocessing markers
			String[] ebms = null;
			String[] eems = null;
			String[] ims = null;
			String[] efms = null;
			if (MIDP.equalsIgnoreCase(type)) {
				// For MIDP
				ebms = new String[] {
					ALL_EXCLUDE_BEGIN_MARKER, 
					J2ME_EXCLUDE_BEGIN_MARKER, 
					MIDP_EXCLUDE_BEGIN_MARKER};
				eems = new String[] {
					ALL_EXCLUDE_END_MARKER, 
					J2ME_EXCLUDE_END_MARKER, 
					MIDP_EXCLUDE_END_MARKER};
				ims = new String[] {
					J2ME_INCLUDE_BEGIN_MARKER, 
					J2ME_INCLUDE_END_MARKER, 
					MIDP_INCLUDE_BEGIN_MARKER, 
					MIDP_INCLUDE_END_MARKER};
				efms = new String[] {
					ALL_EXCLUDE_FILE_MARKER, 
					J2ME_EXCLUDE_FILE_MARKER, 
					MIDP_EXCLUDE_FILE_MARKER};
			}
			else if (PJAVA.equalsIgnoreCase(type)) {
				// For PJAVA
				ebms = new String[] {
					ALL_EXCLUDE_BEGIN_MARKER, 
					J2ME_EXCLUDE_BEGIN_MARKER, 
					PJAVA_EXCLUDE_BEGIN_MARKER};
				eems = new String[] {
					ALL_EXCLUDE_END_MARKER, 
					J2ME_EXCLUDE_END_MARKER, 
					PJAVA_EXCLUDE_END_MARKER};
				ims = new String[] {
					J2ME_INCLUDE_BEGIN_MARKER, 
					J2ME_INCLUDE_END_MARKER, 
					PJAVA_INCLUDE_BEGIN_MARKER, 
					PJAVA_INCLUDE_END_MARKER};
				efms = new String[] {
					ALL_EXCLUDE_FILE_MARKER, 
					J2ME_EXCLUDE_FILE_MARKER, 
					PJAVA_EXCLUDE_FILE_MARKER};
			}
			else if (J2SE.equalsIgnoreCase(type)) {
				// For J2SE
				ebms = new String[] {
					ALL_EXCLUDE_BEGIN_MARKER};
				eems = new String[] {
					ALL_EXCLUDE_END_MARKER};
				ims = new String[] {
					J2SE_INCLUDE_BEGIN_MARKER, 
					J2SE_INCLUDE_END_MARKER}; 
				efms = new String[] {
					ALL_EXCLUDE_FILE_MARKER};
			}
			else {
				throw new BuildException("Unknown pre-processing type ("+type+") for file "+file);
			}

			// Preprocess
			boolean keepFile = preprocess(reader, writer, ebms, eems, ims, efms);
			
			// Close both streams
			reader.close();
			writer.close();
			
			// Overwrite the target file with the preprocessed file or 
			// remove both if the file must be excluded
			if (!targetFile.delete()) {
				System.out.println("Can't overwrite target file with preprocessed file");
				throw new BuildException("Can't overwrite target file "+target+" with preprocessed file");
			} 
			if (keepFile) {
				preprocFile.renameTo(targetFile);
			}
			else {
				if (!preprocFile.delete()) {
					System.out.println("Can't delete temporary preprocessed file "+preprocFile.getName());
					throw new BuildException("Can't delete temporary preprocessed file "+preprocFile.getName());
				}
				if (verbose) {
					System.out.println("File "+preprocFile.getName()+" removed.");
				}
				removedCnt++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(e.getMessage());
		}
	}

	private boolean preprocess(BufferedReader reader, BufferedWriter writer, String[] excludeBeginMarkers, String[] excludeEndMarkers, String[] includeMarkers, String[] excludeFileMarkers) throws IOException { 
		String line = null;
		boolean skip = false;
		String nextExcludeEndMarker = null;
		while (true) {
			line = reader.readLine();
			if (line == null) {
				// Preprocessing terminated
				break;
			}
			String trimmedLine = line.trim();
			
			// Check if this is an exclude-file marker
			if (isMarker(trimmedLine, excludeFileMarkers)) {
				return false;
			}
			
			if (!skip) {
				// Normal processing: Check if this is a BEGIN_EXCLUDE Marker
				if (isMarker(trimmedLine, excludeBeginMarkers)) {
					// Enter SKIP mode
					skip = true;
					nextExcludeEndMarker = getExcludeEndMarker(trimmedLine, excludeEndMarkers);
				}
				else {
					// Just copy the line into the preprocessed file unless
					// the (trimmed) line starts with an INCLUDE Marker
					if (!isMarker(trimmedLine, includeMarkers)) {
						writer.write(line);
						writer.newLine();
					}
				}
			}
			else {
				// SKIP mode
				if (trimmedLine.startsWith(nextExcludeEndMarker)) {
					// Exit SKIP mode
					skip = false;
					nextExcludeEndMarker = null;
				}
			}
		}
		return true;
	}

	private boolean isMarker(String s, String[] markers) {
		for (int i = 0; i < markers.length; ++i) {
			if (s.startsWith(markers[i])) {
				return true;
			}
		}
		return false;
	}	
	
	private String getExcludeEndMarker(String s, String[] endMarkers) {
		int rootLength = s.indexOf("BEGIN");
		String root = s.substring(0, rootLength);
		for (int i = 0; i < endMarkers.length; ++i) {
			if (endMarkers[i].startsWith(root)) {
				return endMarkers[i];
			}
		}
		return null;
	}	
}