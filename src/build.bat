idltojava -fno-cpp FIPA_Agent_97.idl
call javacc -OUTPUT_DIRECTORY:jade\lang\acl jade\lang\acl\ACLParser.jj
call javacc -OUTPUT_DIRECTORY:jade\domain jade\domain\AgentManagementParser.jj
javac -deprecation jade\Boot.java
javac -deprecation jade\core\behaviours\ComplexBehaviour.java jade\core\behaviours\NonDeterministicBehaviour.java jade\core\behaviours\SequentialBehaviour.java jade\core\behaviours\SimpleBehaviour.java jade\core\behaviours\SenderBehaviour.java jade\core\behaviours\ReceiverBehaviour.java jade\domain\SearchDFBehaviour.java jade\proto\ProtocolDrivenBehaviour.java jade\proto\Standard.java jade\proto\FipaRequestInitiatorBehaviour.java jade\proto\FipaRequestResponderBehaviour.java jade\proto\FipaContractNetInitiatorBehaviour.java jade\proto\FipaContractNetResponderBehaviour.java jade\proto\FipaQueryResponderBehaviour.java jade\onto\DefaultOntology.java jade\tools\rma\rma.java jade\tools\rma\AMSMainFrame.java jade\tools\DummyAgent\DummyAgent.java jade\tools\sniffer\Sniffer.java jade\tools\SocketProxyAgent\SocketProxyAgent.java
rmic -d . jade.core.AgentContainerImpl jade.core.AgentPlatformImpl
javac examples\ex1\*.java
javac examples\ex2\*.java
javac examples\ex3\*.java
javac examples\ex4\*.java
javac examples\ex5\*.java
javac examples\ex6\*.java
javac examples\ex7\*.java
