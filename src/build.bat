idltojava FIPA_Agent_97.idl
call javacc -OUTPUT_DIRECTORY:jade\lang\acl jade\lang\acl\ACLParser.jj
call javacc -OUTPUT_DIRECTORY:jade\domain jade\domain\AgentManagementParser.jj
javac -deprecation jade\Boot.java
javac -deprecation jade\core\ComplexBehaviour.java jade\core\NonDeterministicBehaviour.java jade\core\SequentialBehaviour.java jade\core\SimpleBehaviour.java jade\core\SenderBehaviour.java jade\core\ReceiverBehaviour.java jade\domain\rma.java jade\domain\SearchDFBehaviour.java jade\proto\FipaRequestInitiatorBehaviour.java jade\proto\FipaRequestResponderBehaviour.java jade\proto\ProtocolDrivenBehaviour.java jade\proto\Standard.java jade\proto\Waker.java jade\proto\FipaContractNetInitiatorBehaviour.java jade\proto\FipaContractNetResponderBehaviour.java jade\gui\AMSMainFrame.java
rmic -d . jade.core.AgentContainerImpl jade.core.AgentPlatformImpl
javac examples\ex1\*.java
javac examples\ex2\*.java
javac examples\ex3\*.java
javac examples\ex4\*.java
javac examples\ex5\*.java


