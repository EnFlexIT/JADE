package jade.util;

public interface Callback<Result> {
	void onSuccess(Result result);
	void onFailure(Throwable throwable);
}
