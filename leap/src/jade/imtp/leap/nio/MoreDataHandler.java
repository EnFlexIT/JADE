package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

/**
 *
 * interface for a class responsable for handling data left in a socketbuffer after constructing a packet.
 * @see MoreDataNotifier
 * @author Eduard Drenth: Logica, 23-sep-2009
 *
 */
public interface MoreDataHandler {

    /**
     * @see MoreDataNotifier#notifyMoreDataAvailable() 
     */
    public void handleExtraData();

}
