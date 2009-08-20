/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;

/**
 * static helper for ssl/nio related handshaking/input/output
 * @author eduard
 */
public class NIOSSLHelper {

    private NIOSSLHelper() {
    }
    
    public static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocateDirect(0);
    private static final Logger log = Logger.getLogger(NIOSSLHelper.class.getName());

    public static void logBuffer(ByteBuffer b) {
        logBuffer(b, "unknown");
    }

    public static void logBuffer(ByteBuffer b, String name) {
        log.fine("bufferinfo " + name + ": pos " + b.position() + ", rem " + b.remaining() + ", lim " + b.limit());
    }

    /**
     * First initialize your {@link SSLEngineHelper} by clearing its buffers and filling {@link SSLEngineHelper#getSocketData() }
     * with data from a connection. After this this method possibly recursively deals with handshaking and unwrapping
     * application data. The supplied appBuffer argument will be filled remaining unwrapped data will stay in the
     * {@link SSLEngineHelper#getUnwrapData() }.
     * @param helper a helper that has access to all needed instance data for handshaking and input and output
     * @param appBuffer the Buffer where unwrapped application data will be stored, possibly leaving data in the
     * {@link SSLEngineHelper#getUnwrapData() }.
     * @return the number of application bytes generated, -1 means end of stream, 0 means nothing read yet
     * @throws IOException
     */
    public static int doHandshake(SSLEngineHelper helper, ByteBuffer appBuffer) throws IOException {

        SSLEngineResult result = null;
        ByteBuffer socketData = helper.getSocketData();
        ByteBuffer unwrapData = helper.getUnwrapData();
        ByteBuffer wrapData = helper.getWrapData();

        do {
            try {
                if (log.isLoggable(Level.FINE)) {
                    logBuffer(socketData,"unwrapping socketData");
                }
                result = helper.decode(socketData);
            } catch (SSLException e) {
                // send close message to the client, rethrow
                if (log.isLoggable(Level.WARNING)) {
                    log.log(Level.WARNING, "unwrap failure " + helper.getRemoteHost(), e);
                }
                try {
                    helper.close();
                } catch(IOException ex) {

                }
                throw e;
            }
        } while (result.getStatus().equals(Status.OK) &&
                result.getHandshakeStatus().equals(HandshakeStatus.NEED_UNWRAP) &&
                result.bytesProduced() == 0);

        SSLEngineResult.Status status = result.getStatus();
        SSLEngineResult.HandshakeStatus handshakeStatus = result.getHandshakeStatus();

        if (log.isLoggable(Level.FINE)) {
            log.fine(result.toString());
        }

        if (status.equals(Status.OK)) {
            if (handshakeStatus.equals(HandshakeStatus.FINISHED)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(" finished handshaking " + helper.getRemoteHost());
                }
            } else if (handshakeStatus.equals(HandshakeStatus.NEED_TASK)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(" need to run handshake tasks " + helper.getRemoteHost());
                }
                // re evaluate handshake status
                handshakeStatus = helper.runHandshakTasks();

                if (handshakeStatus.equals(HandshakeStatus.FINISHED)) {
                    log.warning("not expected finished handshaking " + helper.getRemoteHost());
                } else if (handshakeStatus.equals(HandshakeStatus.NEED_TASK)) {
                    log.warning("not expected need task " + helper.getRemoteHost());
                } else if (handshakeStatus.equals(HandshakeStatus.NEED_UNWRAP)) {
                    // still data in the inputbuffer to decode
                    if (log.isLoggable(Level.FINE)) {
                        log.fine(("need unwrap, going to recurse " + helper.getRemoteHost()));
                    }
                    // prepare buffers for use in recursion
                    unwrapData.clear();
                    wrapData.clear();
                    return doHandshake(helper, appBuffer);
                } else if (handshakeStatus.equals(HandshakeStatus.NEED_WRAP)) {
                    if (log.isLoggable(Level.FINE)) {
                        log.fine(" need to wrap and send to client " + helper.getRemoteHost());
                    }
                    wrapAndSend(helper);
                } else if (handshakeStatus.equals(HandshakeStatus.NOT_HANDSHAKING)) {
                    log.warning("not expected not handshaking " + helper.getRemoteHost());
                }

            } else if (handshakeStatus.equals(HandshakeStatus.NEED_UNWRAP)) {
                log.warning(" need to unwrap, should not happen here " + helper.getRemoteHost());
            } else if (handshakeStatus.equals(HandshakeStatus.NEED_WRAP)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(" need to wrap and send to client " + helper.getRemoteHost());
                }
                wrapAndSend(helper);
            } else if (handshakeStatus.equals(HandshakeStatus.NOT_HANDSHAKING)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(" unwrapped application data: " + result.bytesProduced() + " from " + helper.getRemoteHost());
                }
                /*
                 * we have read all data from the socket
                 *
                 * we copy data from the decode buffer to the app Buffer and leave the rest in the unwrap Buffer
                 *
                 * applications should take care of these data left behind to process
                 *
                 */
                unwrapData.flip();
                if (log.isLoggable(Level.FINE)) {
                    logBuffer(unwrapData, "unwrapData");
                    logBuffer(appBuffer, "appBuffer");
                }
                return copyAsMuchAsFits(unwrapData, appBuffer);
            }
        } else if (status.equals(Status.CLOSED)) {
            log.info(" sslengine closed " + helper.getRemoteHost());
            // send ssl close
            helper.close();
        } else if (status.equals(Status.BUFFER_UNDERFLOW)) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("not enough data read yet " + helper.getRemoteHost());
            }
        } else if (status.equals(Status.BUFFER_OVERFLOW)) {
            logBuffer(socketData,"socketData");
            logBuffer(unwrapData,"overflow unwrapData");
            log.severe(" buffer overflow should not be possible " + helper.getRemoteHost());
        }
        // default return 0, no application data read yet
        return 0;
    }

    /**
     * generates handshake data and calls {@link NIOJICPConnection#writeToChannel(java.nio.ByteBuffer) }, possibly
     * recursive when required by handshaking
     * @param helper
     * @return the amount of bytes written to the connection
     * @throws SSLException
     * @throws IOException
     */
    static int wrapAndSend(SSLEngineHelper helper) throws SSLException, IOException {
        ByteBuffer wrapData = helper.getWrapData();
        wrapData.clear();
        int n = 0;
        SSLEngineResult result = helper.encode(EMPTY_BUFFER);
        if (log.isLoggable(Level.FINE)) {
            log.fine("wrapped " + result);
        }
        if (result.bytesProduced() > 0) {
            wrapData.flip();
            n = writeToSocket(wrapData, helper.getConnection());

            if (result.getHandshakeStatus().equals(HandshakeStatus.NEED_WRAP)) {
                n += wrapAndSend(helper);
            }
            return n;
        } else {
            log.warning("wrap produced no data " + helper.getRemoteHost());
        }
        return n;
    }

    /**
     * copy data from src to dst, as much as fits in dst. src's position() will be moved
     * when data are copied.
     * @param src copy from
     * @param dst copy to
     * @return number of bytes copied
     */
    public static int copyAsMuchAsFits(ByteBuffer src, ByteBuffer dst) {
        // current position in dst
        int pos = dst.position();

        // read from src as much as fits in dst
        int limit = src.limit();
        if (src.remaining() > dst.remaining()) {
            // data from src does not fit, set limit so that data will fit
            if (log.isLoggable(Level.FINE)) {
                log.fine("setting limit of src buffer to " + (src.position() + dst.remaining()));
            }
            src.limit(src.position() + dst.remaining());
        }

        dst.put(src);

        // reset limit, to make rest of data available to put in payload buffer
        src.limit(limit);

        if (log.isLoggable(Level.FINE)) {
            log.fine("bytes copied to dst " + (dst.position() - pos) + ", bytes left in src " + src.remaining());
            logBuffer(src, "src");
            logBuffer(dst, "dst");
        }
        // return number of data read into dst
        return dst.position() - pos;
    }

    /**
     * encrypt application data, does not write data to socket. After this method call the data to be
     * send can be retrieved through {@link SSLEngineHelper#getWrapData() }, the data will be ready for usage.
     * @param helper
     * @param b the appData to wrap
     * @return the status object for the wrap or null
     * @throws SSLException
     * @throws IOException
     */
    public static SSLEngineResult wrapAppData(SSLEngineHelper helper, ByteBuffer b) throws SSLException, IOException {
        ByteBuffer wrapData = helper.getWrapData();
        wrapData.clear();
        SSLEngineResult result = helper.encode(b);
        if (log.isLoggable(Level.FINE)) {
            log.fine("wrapped " + result);
        }
        if (result.bytesProduced() > 0) {
            wrapData.flip();
            return result;
        } else {
            throw new IOException("wrap produced no data " + helper.getRemoteHost());
        }
    }

    /**
     * tries to write all data to the socket
     * @param b
     * @return
     * @throws IOException
     */
    private static int writeToSocket(ByteBuffer b, NIOJICPConnection connection) throws IOException {
        int n = 0;
        while (b.hasRemaining() && n != -1) {
            n += connection.writeToChannel(b);
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine(n + " bytes written");
        }
        return n;
    }

}
