package jade.applet;

import java.applet.Applet;
import jade.gui.DFGUI;


/**
 * This applet is a client of SocketProxyAgent and executes an applet
 * showing the GUI of the default DF.
 * @see jade.applet.DFAppletCommunicator
 */
public class DFApplet extends Applet {

  public void init() {
    DFAppletCommunicator dfc = new DFAppletCommunicator(this);
    //GUI2DFCommunicatorInterface dfc = new DFAppletCommunicator(this);
    DFGUI gui = new DFGUI(dfc);
    dfc.setGUI(gui);
    gui.setVisible(true);
  }

}

