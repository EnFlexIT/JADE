package jade.security;


public interface PrivilegedExceptionAction
//__JADE_ONLY__BEGIN
		extends java.security.PrivilegedExceptionAction
//__JADE_ONLY__END
{
	public Object run() throws Exception;
}