package test.proto;

import test.common.TestGroup;
import test.common.TesterAgent;

public class SubscriptionTesterAgent extends TesterAgent {

	@Override
	protected TestGroup getTestGroup() {
		TestGroup tg = new TestGroup("test/proto/subscriptionTestsList.xml");		
		return tg;
	}

}
