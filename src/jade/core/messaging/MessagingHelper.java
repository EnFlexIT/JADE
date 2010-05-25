package jade.core.messaging;

import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.ServiceHelper;

public interface MessagingHelper extends ServiceHelper {
	void createAlias(String alias) throws IMTPException, ServiceException;
	void deleteAlias(String alias) throws IMTPException, ServiceException;
}
