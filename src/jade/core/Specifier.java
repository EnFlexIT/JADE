/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.core;

/**
 * This class represent a specifier and collects
 * a name, a className, and an array of arguments.
 * 
 * @author LEAP
 */
public class Specifier {
    private String   name = null;
    private String   className = null;
    private Object[] args = null;

    /**
     * Method declaration
     * 
     * @param n
     * 
     * @see
     */
    public void setName(String n) {
        name = n;
    } 

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    public String getName() {
        return name;
    } 

    /**
     * Method declaration
     * 
     * @param cn
     * 
     * @see
     */
    public void setClassName(String cn) {
        className = cn;
    } 

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    public String getClassName() {
        return className;
    } 

    /**
     * Method declaration
     * 
     * @param a
     * 
     * @see
     */
    public void setArgs(Object[] a) {
        args = a;
    } 

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    public Object[] getArgs() {
        return args;
    } 

    /**
     * This method is used by Boot, ProfileImpl, and RMA in order
     * to have a String representation of this Specifier according to the
     * format <code>name:className(arg1 arg2 argn)</code>
     **/
    public String toString() {
	// TAKE CARE: do not change this method otherwise Boot might fail
	StringBuffer tmp = new StringBuffer();
	if (name != null) {
	    tmp.append(name);
	    tmp.append(":");
	}
	if (className != null) {
	    tmp.append(className);
	}
	if (args != null) {
	    tmp.append("(");
	    for (int i=0; i<args.length; i++) {
		tmp.append(args[i]);
		if (i<args.length-1)
		    tmp.append(" ");
	    }
	    tmp.append(")");
	}
	return tmp.toString();
    }

}

