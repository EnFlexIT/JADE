package jade.gui;

import javax.swing.JPopupMenu;

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date$ $Revision$
 */

class NodeDescriptor {
  JPopupMenu popupMenu;
  String pathImage;

  protected NodeDescriptor(JPopupMenu popupMenu,String pathImage){
   this.popupMenu=popupMenu;
   this.pathImage=pathImage;
  }

  protected JPopupMenu getPopupMenu() {
   return popupMenu;
  }

  protected String getPathImage() {
   return pathImage;
  }

} // End Of NodeDescriptor
