package jade.util;

/*#MIDP_INCLUDE_BEGIN
import javax.microedition.rms.RecordStore;
#MIDP_INCLUDE_END*/

public class Logger{

	/*#MIDP_INCLUDE_BEGIN
	static {
		try {
			RecordStore.deleteRecordStore("OUTPUT");
		}
		catch (Exception e) {
			// The RS does not exist yet --> No need to reset it
		}
	}
	#MIDP_INCLUDE_END*/
	
	public static void println() {
		println("");
	}
	
	public static void println(String s) {
		System.out.println(s);
		
		/*#MIDP_INCLUDE_BEGIN
		try{
			RecordStore rs =	RecordStore.openRecordStore("OUTPUT", true);
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

    
 
