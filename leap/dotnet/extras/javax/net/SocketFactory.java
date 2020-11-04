// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 04/03/2005 16.48.01
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   DashoA12275

package javax.net;

import java.io.IOException;
import java.net.*;

// Referenced classes of package javax.net:
//            DefaultSocketFactory

public abstract class SocketFactory
{

    protected SocketFactory()
    {
    }

    public Socket createSocket()
        throws IOException
    {
        throw new SocketException("Unconnected sockets not implemented");
    }

    public static SocketFactory getDefault()
    {
        synchronized(javax.net.SocketFactory.class)
        {
            if(a == null)
                a = new DefaultSocketFactory();
        }
        return a;
    }

    public abstract Socket createSocket(String s, int i)
        throws IOException, UnknownHostException;

    public abstract Socket createSocket(InetAddress inetaddress, int i)
        throws IOException;

    public abstract Socket createSocket(String s, int i, InetAddress inetaddress, int j)
        throws IOException, UnknownHostException;

    public abstract Socket createSocket(InetAddress inetaddress, int i, InetAddress inetaddress1, int j)
        throws IOException;

    private static SocketFactory a;
}