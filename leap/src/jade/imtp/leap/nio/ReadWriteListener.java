package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

/**
 *
 * @author Eduard Drenth: Logica, 21-sep-2009
 *
 */
public interface ReadWriteListener {

    /**
     * will be called by the {@link BEManagementService} when {@link NIOJICPConnection#writeToChannel(java.nio.ByteBuffer) } was successfull.
     */
    public void handleWriteSuccess();

    /**
     * will be called by the {@link BEManagementService} when {@link NIOJICPConnection#writeToChannel(java.nio.ByteBuffer) } caused a problem.
     */
    public void handleWriteError();

}
