package ru.sstu.vak.periscopeclient.Retrofit;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import ru.sstu.vak.periscopeclient.Retrofit.models.LoginModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyRequest;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyResponse;
import ru.sstu.vak.periscopeclient.Retrofit.models.RoomModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.UserModel;

/**
 * Created by Anton on 28.04.2018.
 */

public interface PeriscopeApi {
    @POST("registration")
    Call<MyResponse<String>> regUser(@Body MyRequest<LoginModel> request);

    @POST("login")
    Call<MyResponse<String>> loginUser(@Body MyRequest<LoginModel> request);

    @POST("getRoom")
    Call<MyResponse<RoomModel>> getRoom(@Body MyRequest<String> request);

    @POST("getRooms")
    Call<MyResponse<ArrayList<RoomModel>>> getRooms(@Body MyRequest<Void> request);

    @POST("createRoom")
    Call<MyResponse<Void>> createRoom(@Body MyRequest<RoomModel> request);

    @POST("removeRoom")
    Call<MyResponse<Void>> removeRoom(@Body MyRequest<RoomModel> request);

    @POST("joinBroadcast")
    Call<MyResponse<RoomModel>> joinBroadcast(@Body MyRequest<RoomModel> request);

    @POST("leaveBroadcast")
    Call<MyResponse<Void>> leaveBroadcast(@Body MyRequest<RoomModel> request);

    @Multipart
    @POST("saveChanges")
    Call<MyResponse<Void>> saveChanges(@Part MultipartBody.Part image, @Part("request") MyRequest<UserModel> request);

    @POST("checkForUpdates")
    Call<MyResponse<UserModel>> checkForUpdates(@Body MyRequest<String> request);
}


//    @Multipart
//    @POST("uploadImage")
//    Call<MyResponse<String>> uploadImage(@Part MultipartBody.Part file, @Header("authToken") String authToken);
//
//    @POST("getUser")
//    Call<MyResponse<UserModel>> getUser(@Body MyRequest<Void> request);
//
//    @POST("updateUser")
//    Call<MyResponse<Void>> updateUser(@Body MyRequest<UserModel> request);