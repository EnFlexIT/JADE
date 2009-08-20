package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.http.HTTPResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Eduard Drenth: Logica, 10-jul-2009
 * 
 */
public class NIOHTTPResponse extends HTTPResponse {

    public void writeResponse(OutputStream out) throws IOException {
        writeTo(out);
    }
}
