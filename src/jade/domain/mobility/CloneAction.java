package jade.domain.mobility;

import jade.core.*;

public class CloneAction extends MoveAction {

    private String newName;

	public void set_0(String nn) {
		setNewName(nn);
	}
	
	public String get_0() {
		return getNewName();
	}


    public void setNewName(String nn) {
      newName = nn;
    }

    public String getNewName() {
      return newName;
    }

}