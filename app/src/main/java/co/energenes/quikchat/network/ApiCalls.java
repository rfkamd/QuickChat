package co.energenes.quikchat.network;


import java.util.Map;

import co.energenes.quikchat.models.ArrayListContacts;
import co.energenes.quikchat.models.Contacts;
import co.energenes.quikchat.models.Countries;
import co.energenes.quikchat.models.UserResponse;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

/**
 * Created by rfkamd on 12/28/2016.
 */

public interface ApiCalls {


    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @GET("countries")
    public Call<Countries> getCountries();

    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("users/friends")
    public Call<Contacts> findFriends(@Body ArrayListContacts contacts);


    @Multipart
    @POST("users")
    public Call<UserResponse> postUser(@PartMap Map<String, RequestBody> user);
//
//    @Headers({
//            "Accept: application/json",
//            "Content-Type: application/json"
//    })
//    @GET("users/{id}")
//    public Call<User> getUser(@Path("id") int id);
//
//
//    @PUT("users/{id}")
//    public Call<UserResponse> updateUser(@Path("id") int id, @Body User user);
//
//    @Headers({
//            "Accept: application/json",
//            "Content-Type: application/json"
//    })
//    @GET("users/orders/{id}")
//    public Call<OrderResponse> getOrdersByUserId(@Path("id") int id);
//
//    @Headers({
//            "Accept: application/json",
//            "Content-Type: application/json"
//    })
//    @GET("orders/{id}")
//    public Call<OrderResponse> getOrdersByOrderId(@Path("id") int id);
//
//    @Multipart
//    @POST("orders")
//    public Call<OrderResponse> postOrder(@PartMap Map<String, RequestBody> order);


}
