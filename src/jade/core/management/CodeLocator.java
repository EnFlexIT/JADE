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

import jade.util.leap.HashMap;
import jade.core.AID;
import java.io.InputStream;
import java.io.File;
import java.util.Enumeration;
import java.util.Random;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;

import sun.misc.BASE64Encoder;

/**
 * This class maintains the mapping between agents and jar files
 * 
 * @author <a href="mailto:Joan.Ametller@uab.es">Joan Ametller Esquerra</a>
 * @author Carles Garrigues
 * @author <a href="mailto:Jordi.Cucurull@uab.es">Jordi Cucurull Juan</a>
 * 
 */
//TODO: Arreglar el sincronisme.
public class CodeLocator {
	
	private static final String JAR_UID_ATTR = "jar-UID";
	private static final String HASH_ALGORITHM = "MD5";	
	private static final int SIZE_CREATE_JAR_BUFFER = 4096;	
	private static final int SIZE_HASH_JAR_BUFFER = 4096;
	
	private static BASE64Encoder b64Encoder = new BASE64Encoder();
	
	private final String TMP_JAR;
	
	public CodeLocator(String agentsPath) {
		_agentsUsingJar = new HashMap();
		_jarHash = new HashMap();
		_jarFolder = agentsPath;
		TMP_JAR = _jarFolder + File.separator + "tmp.jar";
		_random = new Random();
	}
	
	public synchronized String getAgentCodeLocation(AID name) {
		String mobileAgentUID = (String) _agentsUsingJar.get(name);
		if (mobileAgentUID != null) {
			Row r = (Row) _jarHash.get(mobileAgentUID);
			return r != null ? r.getLocation() : null;
		} else
			return null;
	}
	
	private synchronized String getCodeByHash(String mobileAgentUID) {
		Row r = (Row) _jarHash.get(mobileAgentUID);
		return r != null ? r.getLocation() : null;
	}
	
	public synchronized String registerAgent(AID name, byte[] code) throws Exception {
		return registerAgent(name, new ByteArrayInputStream(code));
	}
	
	public synchronized String registerAgent(AID name, InputStream codestream) throws Exception {
		File f = null;
		
		do {
			f = new File(_jarFolder + File.separator + randomString(8) + ".jar");
		} while (f.exists());
		
		// Store code to disk.
		FileOutputStream fos = new FileOutputStream(f);
		byte[] buffy = new byte[512];
		int bytes;
		while ((bytes = codestream.read(buffy)) != -1)
			fos.write(buffy, 0, bytes);
		codestream.close();
		fos.close();
		
		return registerAgent(name, f, false);
	}
	
	public synchronized String registerAgent(AID name, File f, boolean userCreatedJar) throws Exception {
		JarFile jf = new JarFile(f);
		Manifest man = null;
		Attributes att = null;
		String jarUID = null;
		try {
			if ((man = jf.getManifest()) != null) {
				//Manifest exists: Check if it already contains the JAR_UID attribute.
				att = man.getMainAttributes();
				if ((jarUID = att.getValue(JAR_UID_ATTR)) != null) {
					jf.close();
					return registerJar(name, f.getPath(), jarUID, userCreatedJar);
				}
			}
			else {
				// Manifest does not exist: create one
				man = new Manifest();
				att = man.getMainAttributes();
			}
			
			// Create a new JAR including the jar-UID attribute in its manifest
			jarUID = calculateJarUID(jf);
			att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
			att.putValue(JAR_UID_ATTR, jarUID);
			
			// Do a temporary copy of JAR with the new manifest.
			copyJar(jf, TMP_JAR, man);
			jf.close();
			
			// Rename the new JAR with the original name
			//System.out.println("################ Deleting file "+f.getPath());
			if (!f.delete()) {
				System.out.println("####################### DELETE of " 
						+ f.getPath() + "didn't work properly");
			}
			if (!(new File(TMP_JAR)).renameTo(f)) {
				System.out.println("####################### RENAME of "
						+ f.getPath() + "didn't work properly");
			}
			
			// Register agent.
			return registerJar(name, f.getPath(), jarUID, userCreatedJar);
		}
		finally {
			try {jf.close();} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String registerJar(AID name, String path, String jarUID, boolean userCreatedJar) {
		//Check for an equal hash registered JAR.
		Row r = (Row) _jarHash.get(jarUID);
		
		if (r == null) {
			// FIXME: We should use the Agent class name
			_jarHash.put(jarUID, new Row(path, userCreatedJar));
			_agentsUsingJar.put(name, jarUID);
			return path;
		} else {
			// A jar file with the same code already exists (other agents of the same class are already 
			// active in the local container) --> avoid dupplications and just increment the number 
			// of agents refering to that jar file
			_agentsUsingJar.put(name, jarUID);
			r.incRef();
			if (!userCreatedJar) deleteFile(path);
			return r.getLocation();
		}
	}
	
	private void deleteFile(String path) {
		File f = new File(path);
		f.delete();
	}
	
	private void copyJar(JarFile jf, String path, Manifest man) throws IOException {
		Enumeration entries = jf.entries();
		FileOutputStream fos = new FileOutputStream(path);
		JarOutputStream jos = new JarOutputStream(fos, man);
		
		ZipEntry ze;
		InputStream is;
		int readed = 0;
		byte[] buffer = new byte[SIZE_CREATE_JAR_BUFFER];
		
		while (entries.hasMoreElements()) {
			ze = (ZipEntry) entries.nextElement();
			//Prevent to copy manifest files.
			if (!ze.getName().startsWith("META-INF")) {
				is = jf.getInputStream(ze);
				jos.putNextEntry(ze);
				while ((readed = is.read(buffer)) >= 0)
					jos.write(buffer, 0, readed);
				
				is.close();
				jos.closeEntry();
			}
		}
		
		jos.flush();
		jos.close();
		fos.close();
		
	}
	
	private String calculateJarUID(JarFile jf) throws IOException, NoSuchAlgorithmException {
		
		MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
		Enumeration entries = jf.entries();
		ZipEntry ze;
		InputStream is;
		int readed = 0;
		byte[] buffer = new byte[SIZE_HASH_JAR_BUFFER];
		byte[] finalDigest = new byte[16];
		
		if (!entries.hasMoreElements())
			throw new IOException();
		
		while (entries.hasMoreElements()) {
			
			ze = (ZipEntry) entries.nextElement();
			//Prevent to get hash of directory or manifest files.
			if ((!ze.isDirectory()) && (!ze.getName().startsWith("META-INF"))) {
				md.reset();
				is = jf.getInputStream(ze);
				while ((readed = is.read(buffer)) >= 0)
					md.update(buffer, 0, readed);
				is.close();
				finalDigest = xor(finalDigest, md.digest());
			}
		}
		return b64Encoder.encode(finalDigest);
		
	}
	
	private byte[] xor(byte[] first, byte[] second) {
		byte[] result = new byte[first.length];
		for (int i = 0; i < first.length; i++) {
			result[i] = (byte) (first[i] ^ second[i]);
		}
		return result;
	}
	
	synchronized void changeAgentName(AID oldName, AID newName) {
		String jarUID = (String) _agentsUsingJar.get(oldName);
		if (jarUID != null) {
			_agentsUsingJar.put(newName, jarUID);
		}
	}
	
	public synchronized void removeAgentRef(AID name) {
		String jarUID = (String) _agentsUsingJar.get(name);
		
		if (jarUID != null) {
			_agentsUsingJar.remove(name);
			Row r = (Row) _jarHash.get(jarUID);
			if (r != null) {
				if (r.value() > 1) {
					r.decRef();
					
				} else
					removeAgentCode(jarUID);
			}
		}
	}
	
	private synchronized void removeAgentCode(String mobileAgentUID) {
		Row r = (Row) _jarHash.get(mobileAgentUID);
		if ((r != null) && !r.userCreatedJar())
			new File(r.getLocation()).delete();
		_jarHash.remove(mobileAgentUID);
	}
	
	private String randomString(int length) {
		StringBuffer randomName = new StringBuffer();
		int c;
		for (int i = 0; i < length; i++) {
			if (_random.nextBoolean())
				c = _random.nextInt(26) + 97;
			else
				c = _random.nextInt(10) + 48;
			randomName.append((char) c);
		}
		return randomName.toString();
	}
	
	private class OutputStreamHasher extends OutputStream {
		
		public OutputStreamHasher(OutputStream os, MessageDigest md) {
			super();
			_os = os;
			_md = md;
		}
		
		public void write(int b) throws IOException {
			_md.update((byte) b);
			_os.write(b);
		}
		
		public void write(byte[] b, int off, int len) throws IOException {
			_md.update(b, off, len);
			_os.write(b, off, len);
		}
		
		private OutputStream _os;
		
		private MessageDigest _md;
	}
	
	private HashMap _agentsUsingJar;
	
	private HashMap _jarHash;
	
	private String _jarFolder;
	
	private Random _random;
	
	private static class Row {
		public Row(String location, boolean userCreated) {
			_refs = 1;
			_location = location;
			_userCreatedJar = userCreated;
		}
		
		public String getLocation() {
			return _location;
		}
		
		public void incRef() {
			_refs++;
		}
		
		public void decRef() {
			_refs--;
		}
		
		public int value() {
			return _refs;
		}
		
		public boolean userCreatedJar() {
			return _userCreatedJar;
		}
		
		private int _refs;
		
		private String _location;
		
		private boolean _userCreatedJar;
	}
}
