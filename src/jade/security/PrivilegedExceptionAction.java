package jade.security;


/**
	The <code>PrivilegedExceptionAction</code> interface represents
	actions that can be executed in a privileged block.
   
	@author Michele Tomaiuolo - Universita` di Parma
	@version $Date$ $Revision$
*/
public interface PrivilegedExceptionAction
//__JADE_ONLY__BEGIN
		extends java.security.PrivilegedExceptionAction
//__JADE_ONLY__END
{

	/**
		The action body.
		@throws An AuthException is some permission is not owned.
	*/
	public Object run() throws Exception;

}