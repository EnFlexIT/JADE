// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 04/03/2005 16.48.01
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   DashoA12275

package javax.net;

import java.io.IOException;
import java.net.*;

// Referenced classes of package javax.net:
//            DefaultServerSocketFactory

public abstract class ServerSocketFactory
{

    protected ServerSocketFactory()
    {
    }

    public ServerSocket createServerSocket()
        throws IOException
    {
        throw new SocketException("Unbound server sockets not implemented");
    }

    public abstract ServerSocket createServerSocket(int i)
        throws IOException;

    public abstract ServerSocket createServerSocket(int i, int j)
        throws IOException;

    public static ServerSocketFactory getDefault()
    {
        synchronized(javax.net.ServerSocketFactory.class)
        {
            if(a == null)
                a = new DefaultServerSocketFactory();
        }
        return a;
    }

    public abstract ServerSocket createServerSocket(int i, int j, InetAddress inetaddress)
        throws IOException;

    private static ServerSocketFactory a;
}