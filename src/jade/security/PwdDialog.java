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

package jade.security;


/**
	The <code>PwdDialog</code> interface is implemented by classes 
	that provide a mechanism to get username and password from the user.
	Implementation can use either text-based or GUI-based interface.

	@author Giosue Vitaglione - TILAB
	@version $Date$ $Revision$
*/
public interface PwdDialog{

	/**
		start the action of asking for user/passwd.
	*/
	public void ask();
	

	/**
		Returns the prodived User Name.
	*/
	public String getUserName();

	/**
		Returns the prodived Password.
	*/
	public char[] getPassword();

	/**
		Sets the User Name, the user will provide only 
		password and confirmation.
	*/
	public void setUserName(String name);

	/**
		Set the Password.
		If also Username is set by using <code>setUserName(String)</code>, 
		the user will provide only confirmation.
	*/
	public void setPassword(String pass);
	
}
