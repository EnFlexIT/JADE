package leapTools;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.DirectoryScanner;

import java.io.*;

/**
   ANT-task used in jar "minimization" to remove all unnecessary 
   class files
   @author Giovanni Caire - TILAB
 */
public class Minimizer extends Task {
	private boolean verbose = false;
	private int removedCnt = 0;
	
	// The directory where to remove unnecessary class files from
	private String basedir;
	
	// The directory where to check if a class file is required or not
	private String checkdir;
		
	// The setter method for the "basedir" attribute
	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}
	
	// The setter method for the "checkdir" attribute
	public void setCheckdir(String checkdir) {
		this.checkdir = checkdir;
	}
	
	// The setter method for the "verbose" attribute
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	// The method executing the task
	public void execute() throws BuildException {
		try {
			if (basedir != null) {
				basedir = getProject().getBaseDir().toString()+"/"+basedir;
				// Recursive preprocessing (use basedir arg and ignore target arg if any)
				File bd = new File(basedir);
				if (bd.isDirectory()) {
					if (checkdir != null) {
						checkdir = getProject().getBaseDir().toString()+"/"+checkdir;
						File cd = new File(checkdir);
						if (cd.isDirectory()) {
							DirectoryScanner scanner = new DirectoryScanner();
							scanner.setBasedir(bd);
							scanner.setIncludes(new String[] {"**/*.class"});
							scanner.scan();
							String[] files = scanner.getIncludedFiles();
							
							System.out.println("Checking "+files.length+" files.");
							for (int i = 0; i < files.length; ++i) {
								check(files[i], bd, cd);
							}
							System.out.println("Removed  "+removedCnt+" un-necessary files.");
						}
						else {
							throw new BuildException("Error: "+checkdir+" is not a directory.");
						}
					}
					else {
						throw new BuildException("Error: checkdir not specified.");
					}
				}
				else {
					throw new BuildException("Error: "+basedir+" is not a directory.");
				}
			}
			else {
				throw new BuildException("Error: basedir not specified.");
			}
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw new BuildException(t.getMessage());
		}
	}
	
	/**
	 */
	private void check(String file, File bd, File cd) throws Throwable {
		// Chec if the file exists in the "check directory"
		String name = cd.getPath()+"/"+file;
		if (verbose) {
			System.out.println("Checking file "+name);
		}
		File f = new File(name);
		if (!f.exists()) {
			// The file does not exist --> Remove it from the "base directory"
			name = bd.getPath()+"/"+file;
			if (verbose) {
				System.out.println("Removing file "+name);
			}
			File toBeRemoved = new File(name);
			if (!toBeRemoved.delete()) {
				System.out.println("Can't delete file "+toBeRemoved.getName());
				throw new BuildException("Can't delete file "+toBeRemoved.getName());
			}
			removedCnt++;
		}
	}
}