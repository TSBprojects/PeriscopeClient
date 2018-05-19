package ru.sstu.vak.periscopeclient.Retrofit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.Retrofit.models.LocationModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.LoginModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyRequest;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyResponse;
import ru.sstu.vak.periscopeclient.Retrofit.models.RoomModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.UserModel;
import ru.sstu.vak.periscopeclient.infrastructure.SharedPrefWrapper;
import ru.sstu.vak.periscopeclient.infrastructure.TokenUtils;

public class RetrofitWrapper {

    private Context context;
    private TokenUtils tokenUtils;
    private PeriscopeApi periscopeApi;
    private SharedPrefWrapper sharedPrefWrapper;

    public RetrofitWrapper(Context context) {
        this.context = context;
        tokenUtils = new TokenUtils(context);
        sharedPrefWrapper = new SharedPrefWrapper(context);
        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl("http://anton-var.ddns.net:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        periscopeApi = retrofit.create(PeriscopeApi.class);
    }

    public interface Callback<T> {

        void onSuccess(T data);

        void onFailure(Throwable t);
    }


    public void joinRoom(@NonNull String streamName, Callback callback) {
        String token = tokenUtils.getToken();
        Call<MyResponse<RoomModel>> call = periscopeApi.joinBroadcast(new MyRequest<RoomModel>(
                new RoomModel(null, null, null, streamName, null),
                token
        ));
        call.enqueue(new retrofit2.Callback<MyResponse<RoomModel>>() {
            @Override
            public void onResponse(Call<MyResponse<RoomModel>> call, Response<MyResponse<RoomModel>> response) {
                if (response.isSuccessful()) {
                    MyResponse<RoomModel> resp = response.body();
                    if (resp.getError() == null) {
                        if (resp.getData().getObservers() == null) {
                            showFinishAlertDialog("Пользователь закончил трансляцию");
                        } else {
                            callback.onSuccess(resp.getData());
                        }
                    }
                } else {
                    callback.onFailure(new Exception("Основной сервер вернул ошибку"));
                }
            }

            @Override
            public void onFailure(Call<MyResponse<RoomModel>> call, Throwable t) {
                callback.onFailure(new Exception("Основной сервер не отвечает"));
            }
        });
    }

    public void leaveRoom(@NonNull String streamName, Callback callback) {
        String token = tokenUtils.getToken();

        Call<MyResponse<Void>> call = periscopeApi.leaveBroadcast(new MyRequest<RoomModel>(
                new RoomModel(null, null, null, streamName, null),
                token
        ));
        call.enqueue(new retrofit2.Callback<MyResponse<Void>>() {
            @Override
            public void onResponse(Call<MyResponse<Void>> call, Response<MyResponse<Void>> response) {
                if (response.isSuccessful()) {
                    MyResponse<Void> resp = response.body();
                    if (resp.getError() == null) {
                        callback.onSuccess(resp.getData());
                    }
                } else {
                    callback.onFailure(new Exception("Основной сервер вернул ошибку"));
                }
            }

            @Override
            public void onFailure(Call<MyResponse<Void>> call, Throwable t) {
                callback.onFailure(new Exception("Основной сервер не отвечает"));
            }
        });
    }

    public void getObserversCount(@NonNull String streamName, Callback callback) {
        String token = tokenUtils.getToken();
        Call<MyResponse<RoomModel>> call = periscopeApi.getRoom(new MyRequest<String>(streamName, token));
        call.enqueue(new retrofit2.Callback<MyResponse<RoomModel>>() {
            @Override
            public void onResponse(Call<MyResponse<RoomModel>> call, Response<MyResponse<RoomModel>> response) {
                if (response.isSuccessful()) {
                    MyResponse<RoomModel> resp = response.body();
                    if (resp.getError() == null) {
                        callback.onSuccess(resp.getData());
                    }
                } else {
                    callback.onFailure(new Exception("Основной сервер вернул ошибку"));
                }
            }

            @Override
            public void onFailure(Call<MyResponse<RoomModel>> call, Throwable t) {
                callback.onFailure(new Exception("Основной сервер не отвечает"));
            }
        });
    }

    public void createRoom(String description, @NonNull final String streamName, LocationModel currentLocation, Callback callback) {
        String token = tokenUtils.getToken();
        Call<MyResponse<Void>> call = periscopeApi.createRoom(new MyRequest<RoomModel>(
                new RoomModel(description, null, null, streamName, currentLocation),
                token
        ));
        call.enqueue(new retrofit2.Callback<MyResponse<Void>>() {
            @Override
            public void onResponse(Call<MyResponse<Void>> call, Response<MyResponse<Void>> response) {
                if (response.isSuccessful()) {
                    MyResponse<Void> resp = response.body();
                    if (resp.getError() == null) {
                        callback.onSuccess(resp.getData());
                    }
                } else {
                    callback.onFailure(new Exception("Основной сервер вернул ошибку"));
                }
            }

            @Override
            public void onFailure(Call<MyResponse<Void>> call, Throwable t) {
                callback.onFailure(new Exception("Основной сервер не отвечает"));
            }
        });
    }

    public void removeRoom(@NonNull String streamName, Callback callback) {
        String token = tokenUtils.getToken();
        Call<MyResponse<Void>> call = periscopeApi.removeRoom(new MyRequest<RoomModel>(
                new RoomModel(null, null, null, streamName, null),
                token
        ));
        call.enqueue(new retrofit2.Callback<MyResponse<Void>>() {
            @Override
            public void onResponse(Call<MyResponse<Void>> call, Response<MyResponse<Void>> response) {
                if (response.isSuccessful()) {
                    MyResponse<Void> resp = response.body();
                    if (resp.getError() == null) {
                        callback.onSuccess(resp.getData());
                    }
                } else {
                    callback.onFailure(new Exception("Основной сервер вернул ошибку"));
                }
            }

            @Override
            public void onFailure(Call<MyResponse<Void>> call, Throwable t) {
                callback.onFailure(new Exception("Основной сервер не отвечает"));
            }
        });
    }

    public void checkForUpdates(Callback callback) {
        String token = tokenUtils.getToken();
        String updateDate = sharedPrefWrapper.getString("updateDate");

        Call<MyResponse<UserModel>> call = periscopeApi.checkForUpdates(new MyRequest<String>(updateDate, token));
        call.enqueue(new retrofit2.Callback<MyResponse<UserModel>>() {
            @Override
            public void onResponse(Call<MyResponse<UserModel>> call, Response<MyResponse<UserModel>> response) {
                if (response.isSuccessful()) {
                    MyResponse<UserModel> resp = response.body();
                    if (resp.getError() == null) {
                        callback.onSuccess(resp.getData());
                    } else if (resp.getError().equals("invalid authToken")) {
                        callback.onFailure(new Exception("invalid authToken"));
                        Intent intent = new Intent(context, ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
                        ((Activity) context).startActivityForResult(intent, 1);
                    }
                } else {
                    callback.onFailure(new Exception("Основной сервер вернул ошибку"));
                }
            }

            @Override
            public void onFailure(Call<MyResponse<UserModel>> call, Throwable t) {
                callback.onFailure(new Exception("Основной сервер не отвечает"));
            }
        });
    }

    public void refreshRooms(Callback callback) {
        String token = tokenUtils.getToken();
        Call<MyResponse<ArrayList<RoomModel>>> call = periscopeApi.getRooms(new MyRequest<Void>(null, token));
        call.enqueue(new retrofit2.Callback<MyResponse<ArrayList<RoomModel>>>() {
            @Override
            public void onResponse(Call<MyResponse<ArrayList<RoomModel>>> call, Response<MyResponse<ArrayList<RoomModel>>> response) {
                if (response.isSuccessful()) {
                    MyResponse<ArrayList<RoomModel>> resp = response.body();
                    if (resp.getError() == null) {
                        callback.onSuccess(resp.getData());
                    } else if (resp.getError().equals("invalid authToken")) {
                        callback.onFailure(new Exception("invalid authToken"));
                        Intent intent = new Intent(context, ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
                        ((Activity) context).startActivityForResult(intent, 1);
                    }
                } else {
                    callback.onFailure(new Exception("Основной сервер вернул ошибку"));
                }
            }

            @Override
            public void onFailure(Call<MyResponse<ArrayList<RoomModel>>> call, Throwable t) {
                callback.onFailure(new Exception("Основной сервер не отвечает"));
            }
        });
    }

    public void playStream(final @NonNull String streamName, Callback callback) {
        String token = tokenUtils.getToken();
        Call<MyResponse<RoomModel>> call = periscopeApi.getRoom(new MyRequest<String>(streamName, token));
        call.enqueue(new retrofit2.Callback<MyResponse<RoomModel>>() {
            @Override
            public void onResponse(Call<MyResponse<RoomModel>> call, Response<MyResponse<RoomModel>> response) {
                if (response.isSuccessful()) {
                    MyResponse<RoomModel> resp = response.body();
                    if (resp.getError() == null) {
                        callback.onSuccess(resp.getData());
                    } else if (resp.getError().equals("invalid authToken")) {
                        callback.onFailure(new Exception("invalid authToken"));
                        Intent intent = new Intent(context, ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
                        ((Activity) context).startActivityForResult(intent, 1);
                    }
                } else {
                    callback.onFailure(new Exception("Основной сервер вернул ошибку"));
                }
            }

            @Override
            public void onFailure(Call<MyResponse<RoomModel>> call, Throwable t) {
                callback.onFailure(new Exception("Основной сервер не отвечает"));
            }
        });
    }

    public void registerUser(String login, String password, Callback callback) {
        Call<MyResponse<String>> call = periscopeApi.regUser(new MyRequest<LoginModel>(
                new LoginModel(login, password), ""
        ));
        call.enqueue(new retrofit2.Callback<MyResponse<String>>() {
            @Override
            public void onResponse(Call<MyResponse<String>> call, Response<MyResponse<String>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(new Exception("Основной сервер вернул ошибку"));
                }
            }

            @Override
            public void onFailure(Call<MyResponse<String>> call, Throwable t) {
                callback.onFailure(new Exception("Основной сервер не отвечает"));
            }
        });
    }

    public void loginUser(String login, String password, Callback callback) {
        Call<MyResponse<String>> call = periscopeApi.loginUser(new MyRequest<LoginModel>(
                new LoginModel(login, password), ""
        ));
        call.enqueue(new retrofit2.Callback<MyResponse<String>>() {
            @Override
            public void onResponse(Call<MyResponse<String>> call, Response<MyResponse<String>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(new Exception("Основной сервер вернул ошибку"));
                }
            }

            @Override
            public void onFailure(Call<MyResponse<String>> call, Throwable t) {
                callback.onFailure(new Exception("Основной сервер не отвечает"));
            }
        });
    }


    private void showFinishAlertDialog(String message) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        ((Activity) context).finish();
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity) context).finish();
                    }
                }).show();
    }

    private void showAlertDialog(String message) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
