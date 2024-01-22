package jade.content.abs;

import jade.content.schema.ReferenceSchema;
import jade.content.schema.VariableSchema;

/**
 * A term representing a reference to an object or an attribute of an object 
 * typically expressed as &OBJECT_TYPE#name
 * where name may be either an ObjectID or have the form ObjectID.attribute
 * 
 * @author Caire
 */
public class AbsReference extends AbsObjectImpl implements AbsTerm {
	private static final long serialVersionUID = 6167359403561714663L;
	
	public static final String REFERENCE_PREFIX = "&";
	public static final String REFERENCE_NAME_SEPARATOR = "#";
	public static final String REFERENCE_ATTRIBUTE_SEPARATOR = ".";

	/**
	 * Create an AbsReference from its String representation. The latter
	 * can have the form &OBJECT_TYPE#name or OBJECT_TYPE#name.
	 */
	public static AbsReference parse(String referenceStr) {
		// If referenceStr starts with & remove it 
		if (referenceStr.charAt(0) == REFERENCE_PREFIX.charAt(0)) {
			referenceStr = referenceStr.substring(1);
		}

		String type = null;
		String name = null;
		int k = referenceStr.indexOf(REFERENCE_NAME_SEPARATOR);
		if (k > 0) {
			type = referenceStr.substring(0, k);
			name = referenceStr.substring(k+1);
		}
		else {
			// If there is no # --> Use the whole string as the name (type remains null)
			name = referenceStr;
		}
		return new AbsReference(name, type);
	}
	
	/**
	 * Construct an Abstract descriptor to hold a reference
	 */
	public AbsReference() {
		super(ReferenceSchema.BASE_NAME);
	}

	/**
	 * Construct an AbsReference with the given name and value type 
	 * @param name The name of the reference.
	 * @param type The type of the referenced object
	 */
	public AbsReference(String name, String valueType) {
		super(ReferenceSchema.BASE_NAME);

		setType(valueType);
		setName(name);
	}

	/**
	 * Sets the name of this reference. 
	 * @param name The new name of this reference.
	 */
	public void setName(String name) {
//		if (name != null) {
//			// If name is in the form ttt#nnn --> 
//			// - type = ttt
//			// - name = nnn
//			int k = name.indexOf(REFERENCE_NAME_SEPARATOR);
//			if (k > 0) {
//				setType(name.substring(0, k));
//				name = name.substring(k+1);
//			}
//		}
		set(ReferenceSchema.NAME, AbsPrimitive.wrap(name));
	} 

	/**
	 * Sets the type of the referenced object
	 * @param type The type of the referenced object
	 */
	public void setType(String type) {
		set(ReferenceSchema.OBJECT_TYPE, AbsPrimitive.wrap(type));
	} 

	/**
	 * Gets the name of this reference.
	 * @return The name of this reference.
	 */
	public String getName() {
		AbsPrimitive abs = (AbsPrimitive) getAbsObject(ReferenceSchema.NAME);
		if (abs != null) {
			return abs.getString();
		}
		else {
			return null;
		}
	} 

	/**
	 * Gets the type of the referenced object.
	 * @return The type of the referenced object.
	 */
	public String getType() {
		AbsPrimitive abs = (AbsPrimitive) getAbsObject(ReferenceSchema.OBJECT_TYPE);
		if (abs != null) {
			return abs.getString();
		}
		else {
			return null;
		}
	} 

	/**
	 * Redefine the <code>isGrounded()</code> method in order to 
	 * always return <code>false</code>. 
	 */
	public boolean isGrounded() {
		return false;
	} 

	// Easy way to access the Java class representing AbsReference.
	// Useful in MIDP where XXX.class is not available
	private static Class absReferenceClass = null;
	public static Class getJavaClass() {
		if (absReferenceClass == null) {
			try {
				absReferenceClass = Class.forName("jade.content.abs.AbsReference");
			}
			catch (Exception e) {
				// Should never happen
				e.printStackTrace();
			}
		}
		return absReferenceClass;
	}

	public int getAbsType() {
		return ABS_REFERENCE;
	}
	
	public String getAttribute() {
		String n = getName();
		int k = n.indexOf(REFERENCE_ATTRIBUTE_SEPARATOR);
		if (k > 0) {
			return n.substring(k+1);
		}
		else {
			return null;
		}
	}
	
	public String getInstanceId() {
		String n = getName();
		int k = n.indexOf(REFERENCE_ATTRIBUTE_SEPARATOR);
		if (k > 0) {
			return n.substring(0, k);
		}
		else {
			return n;
		}
	}
	
	public boolean isInstance() {
		return getName().indexOf(REFERENCE_ATTRIBUTE_SEPARATOR) > -1;
		
	}
	
	public boolean isAttribute() {
		return getName().indexOf(REFERENCE_ATTRIBUTE_SEPARATOR) == -1;
	}
	
	public static final String asString(String type, String name) {
		//#MIDP_EXCLUDE_BEGIN
		StringBuilder sb = new StringBuilder(REFERENCE_PREFIX);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 StringBuffer sb = new StringBuffer(REFERENCE_PREFIX);
		 #MIDP_INCLUDE_END*/		
		sb.append(type);
		sb.append(REFERENCE_NAME_SEPARATOR);
		sb.append(name);
		return sb.toString();
	}
	
	public static final String asString(String type, String id, String attribute) {
		//#MIDP_EXCLUDE_BEGIN
		StringBuilder sb = new StringBuilder(REFERENCE_PREFIX);
		//#MIDP_EXCLUDE_END
		/*#MIDP_INCLUDE_BEGIN
		 StringBuffer sb = new StringBuffer(REFERENCE_PREFIX);
		 #MIDP_INCLUDE_END*/		
		sb.append(REFERENCE_NAME_SEPARATOR);
		sb.append(id);
		if (attribute != null) {
			sb.append(REFERENCE_ATTRIBUTE_SEPARATOR);
			sb.append(attribute);
		}
		return sb.toString();
	}
}
