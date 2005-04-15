// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 04/03/2005 16.48.01
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   DashoA12275

package javax.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

// Referenced classes of package javax.net:
//            ServerSocketFactory

class DefaultServerSocketFactory extends ServerSocketFactory
{

    DefaultServerSocketFactory()
    {
    }

    public ServerSocket createServerSocket()
        throws IOException
    {
        return new ServerSocket(10000);
    }

    public ServerSocket createServerSocket(int i)
        throws IOException
    {
        return new ServerSocket(i);
    }

    public ServerSocket createServerSocket(int i, int j)
        throws IOException
    {
        return new ServerSocket(i, j);
    }

    public ServerSocket createServerSocket(int i, int j, InetAddress inetaddress)
        throws IOException
    {
        return new ServerSocket(i, j, inetaddress);
    }
}