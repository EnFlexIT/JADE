package jade.core.faultRecovery;

import jade.core.ServiceException;
import jade.core.ServiceHelper;

public interface FaultRecoveryHelper extends ServiceHelper {
	
	void reattach() throws ServiceException;

}
