/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A. 
 * Copyright (C) 2001,2002 TILab S.p.A. 
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

package jade.util;

/*#MIDP_INCLUDE_BEGIN
import javax.microedition.rms.RecordStore;
#MIDP_INCLUDE_END*/

/**
   This class provides a uniform way to produce logging printouts
   in a device dependent way. More in details if you are running 
   JADE in a Java 2 standard edition environment or in a 
   PersonalJava environment (through the LEAP add-on) a call to 
   <code>Logger.println()</code> simply results in a call to 
   <code>System.out.println()</code>. On the other hand if you are
   running JADE in a MIDP environment (again through the LEAP add-on)
   printouts are redirected so that they can be later viewed by 
   means of the <code>jade.util.leap.OutputViewer</code> MIDlet 
   included in the LEAP add-on.
 */
public class Logger{

	/*#MIDP_INCLUDE_BEGIN
	private static final String OUTPUT = "OUTPUT";
	static {
		try {
			RecordStore.deleteRecordStore(OUTPUT);
		}
		catch (Exception e) {
			// The RS does not exist yet --> No need to reset it
		}
	}
	#MIDP_INCLUDE_END*/
	
	public static void println() {
		println("");
	}
	
	/**
     Print a String in a device dependent way. 
   */
	public synchronized static void println(String s) {
		System.out.println(s);
		
		/*#MIDP_INCLUDE_BEGIN
		try{
			RecordStore rs =	RecordStore.openRecordStore(OUTPUT, true);
			byte[] bb = s.getBytes();
			rs.addRecord(bb,0,bb.length);
			rs.closeRecordStore();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		#MIDP_INCLUDE_END*/
	}
}

    
 
