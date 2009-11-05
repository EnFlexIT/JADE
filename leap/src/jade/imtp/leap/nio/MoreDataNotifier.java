package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import java.nio.channels.Selector;

/**
 * Interface for the following situation:
 * <ul>
 * <li> {@link NIOJICPConnection} reads more data from socket than needed for a packet</li>
 * <li> in this case {@link NIOJICPConnection#readPacket() } will not be triggered for the next packet (the {@link Selector} will not generate an event)</li>
 * <li> in this situation {@link #notifyMoreDataAvailable() } will be called by the {@link MoreDataHandler}.</li>
 * </ul>
 * @author Eduard Drenth: Logica, 23-sep-2009
 *
 */
public interface MoreDataNotifier {

    /**
     * will be called by a {@link NIOJICPConnection} when there's data left in the socket buffer after successfully constructing a packet.
     * {@link MoreDataHandler#handleExtraData() } should be called.
     */
    public void notifyMoreDataAvailable();

    public void setMoreDataHandler(MoreDataHandler handler);

    public MoreDataHandler removeMoreDataHandler();

}
