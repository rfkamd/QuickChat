package co.energenes.quikchat.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

import co.energenes.quikchat.Utilities.Config;
import co.energenes.quikchat.models.ArrayListContacts;
import co.energenes.quikchat.models.Contacts;
import co.energenes.quikchat.models.Countries;
import co.energenes.quikchat.models.UserResponse;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Restful services consumption client
 */
public class RestApi {


    //region singleton pattern related stuff

    public static String PROXY_AUTH;
    private static RestApi ourInstance = new RestApi();
    private static Retrofit retrofit;
    private static ApiCalls service;

    private RestApi() {
        try {

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();

            Retrofit.Builder builder = new Retrofit.Builder();
            retrofit = builder
                    .baseUrl(Config.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            service = retrofit.create(ApiCalls.class);
            //PROXY_AUTH = Config.getAuth();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static RestApi getInstance() {
        return ourInstance;
    }


    //endregion singleton pattern related stuff

    //region Countries API

    public void getCountries(final ApiResponse callBack) {
        Call<Countries> response = service.getCountries();
        response.enqueue(new Callback<Countries>() {
            @Override
            public void onResponse(Call<Countries> call, Response<Countries> response) {
                if (response.isSuccessful()) {
                    System.out.println("Successfull response");
                    callBack.onResponse(response.isSuccessful(), response.body());
                } else {
                    System.out.println("Server Unreacheable");
                    callBack.onResponse(response.isSuccessful(), null);
                }
            }

            @Override
            public void onFailure(Call<Countries> call, Throwable throwable) {
                System.out.println("fail to get response: \n" + throwable.getMessage());
                callBack.onFailure(throwable, null);
            }
        });
    }

    public void findFriends(ArrayListContacts users, final ApiResponse callBack) {
        Call<Contacts> response = service.findFriends(users);
        response.enqueue(new Callback<Contacts>() {
            @Override
            public void onResponse(Call<Contacts> call, Response<Contacts> response) {
                if (response.isSuccessful()) {
                    System.out.println("Successfull response");
                    callBack.onResponse(response.isSuccessful(), response.body());
                } else {
                    System.out.println("Server Unreacheable");
                    callBack.onResponse(response.isSuccessful(), null);
                }
            }

            @Override
            public void onFailure(Call<Contacts> call, Throwable throwable) {
                System.out.println("fail to get response: \n" + throwable.getMessage());
                callBack.onFailure(throwable, null);
            }
        });
    }

    //endregion Countries API

    //region USER API

    public void postUser(Map<String, RequestBody> user, final ApiResponse callBack) {
        Call<UserResponse> response = service.postUser(user);
        response.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    System.out.println("Successfull response");
                    callBack.onResponse(response.isSuccessful(), response.body());
                } else {
                    System.out.println("Server Unreacheable");
                    callBack.onResponse(response.isSuccessful(), null);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                System.out.println("fail to get response: \n" + throwable.getMessage());
                callBack.onFailure(throwable, null);
            }
        });
    }


//    public void getUserDetails(int id, final ApiResponse callBack) {
//        Call<User> response = service.getUser(id);
//        response.enqueue(new Callback<User>() {
//            @Override
//            public void onResponse(Call<User> call, Response<User> response) {
//                if(response.isSuccessful()){
//                    System.out.println("Successfull response");
//                    callBack.onResponse(response.isSuccessful(), response.body());
//                }else{
//                    System.out.println("Server Unreacheable");
//                    callBack.onResponse(response.isSuccessful(), null);
//                }
//            }
//            @Override
//            public void onFailure(Call<User> call, Throwable throwable) {
//                System.out.println("fail to get response: \n" + throwable.getMessage());
//                callBack.onFailure(throwable, null);
//            }
//        });
//    }

//    public void putUser(int id, User user, final ApiResponse callBack) {
//        Call<UserResponse> response = service.updateUser(id, user);
//        response.enqueue(new Callback<UserResponse>() {
//            @Override
//            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
//                if (response.isSuccessful() ) {
//                    System.out.println("Successfull response");
//                    callBack.onResponse(response.isSuccessful(), response.body().getData());
//                } else {
//                    System.out.println("Server Unreacheable");
//                    callBack.onResponse(response.isSuccessful(), null);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<UserResponse> call, Throwable throwable) {
//                System.out.println("fail to get response: \n" + throwable.getMessage());
//                callBack.onFailure(throwable, null);
//            }
//        });
//    }

    //endregion


}
