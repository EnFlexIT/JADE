package jade.domain.mobility;

import jade.core.*;

public class CloneAction extends MoveAction {

    private String newName;

    public void setNewName(String nn) {
      newName = nn;
    }

    public String getNewName() {
      return newName;
    }

}