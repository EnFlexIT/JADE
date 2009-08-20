package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.http.HTTPRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Eduard Drenth: Logica, 10-jul-2009
 * 
 */
public class NIOHTTPRequest  extends HTTPRequest {

    public void readRequest(InputStream is) throws IOException {
        readFrom(is);
    }

}
