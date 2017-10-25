package co.energenes.quikchat.network;

/**
 * Created by rfkamd on 1/10/2017.
 */

public interface ApiResponse {

    public void onResponse(boolean isSuccess, Object responseObject);

    public void onFailure(Throwable throwable, String message);

}
