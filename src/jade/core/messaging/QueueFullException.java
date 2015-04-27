package jade.core.messaging;

class QueueFullException extends RuntimeException {
	
	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}
