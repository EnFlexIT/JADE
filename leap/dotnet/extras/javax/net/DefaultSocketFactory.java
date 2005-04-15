// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 04/03/2005 16.48.01
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   DashoA12275

package javax.net;

import java.io.IOException;
import java.net.*;

// Referenced classes of package javax.net:
//            SocketFactory

class DefaultSocketFactory extends SocketFactory
{

    DefaultSocketFactory()
    {
    }

    public Socket createSocket()
		throws IOException, UnknownHostException
    {
        return new Socket("localhost", 10001);
    }

    public Socket createSocket(String s, int i)
        throws IOException, UnknownHostException
    {
        return new Socket(s, i);
    }

    public Socket createSocket(InetAddress inetaddress, int i)
        throws IOException
    {
        return new Socket(inetaddress, i);
    }

    public Socket createSocket(String s, int i, InetAddress inetaddress, int j)
        throws IOException, UnknownHostException
    {
        return new Socket(s, i, inetaddress, j);
    }

    public Socket createSocket(InetAddress inetaddress, int i, InetAddress inetaddress1, int j)
        throws IOException
    {
        return new Socket(inetaddress, i, inetaddress1, j);
    }
}