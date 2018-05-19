package ru.sstu.vak.periscopeclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.Retrofit.PeriscopeApi;
import ru.sstu.vak.periscopeclient.Retrofit.RetrofitWrapper;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyRequest;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyResponse;
import ru.sstu.vak.periscopeclient.Retrofit.models.RoomModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.UserModel;
import ru.sstu.vak.periscopeclient.infrastructure.SharedPrefWrapper;
import ru.sstu.vak.periscopeclient.infrastructure.TokenUtils;


public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private RetrofitWrapper retrofitWrapper;

    private Toolbar toolbar;
    private Button exit_acc_btn;
    private ImageView profile_img;

    private TokenUtils tokenUtils;
    private PeriscopeApi periscopeApi;
    private SharedPrefWrapper sharedPrefWrapper;
    private ProgressBar profile_img_progressbar;
    private ProgressBar aboutme_progressbar;
    private TextView name_field;
    private TextView login_field;
    private TextView aboutme_field;

    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        setActivitiesItems();
        initializeImageLoader();
        initializeServerApi();
        setToolBar();

        checkForUpdates();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.exit_acc_btn: {
                signoutDialog();
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        checkForUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_tool_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_btn: {
                Intent intent = new Intent(this, EditAccountActivity.class);
                startActivityForResult(intent, 1);
                break;
            }
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


//    private void setUserInfo() {
//
//
//        //imageLoader.displayImage("http://anton-var.ddns.net:8080/public/57.jpg", profile_img);
//
//
//        aboutme_field.setVisibility(View.INVISIBLE);
//        aboutme_progressbar.setVisibility(View.VISIBLE);
//
//        String token = tokenUtils.getToken();
//
//        Call<MyResponse<UserModel>> call = periscopeApi.getUser(new MyRequest<Void>(null, token));
//        call.enqueue(new Callback<MyResponse<UserModel>>() {
//            @Override
//            public void onResponse(Call<MyResponse<UserModel>> call, Response<MyResponse<UserModel>> response) {
//                if (response.isSuccessful()) {
//                    MyResponse<UserModel> resp = response.body();
//
//                    if (resp.getError() == null) {
//                        aboutme_field.setVisibility(View.VISIBLE);
//                        aboutme_progressbar.setVisibility(View.INVISIBLE);
//
//                        UserModel user = resp.getData();
//                        currentUser = user;
//                        setProfileImg(user.getProfileImagePath());
//
//                        String userFName = user.getFirstName();
//                        String userSecName = user.getLastName();
//                        String userAboutMe = user.getAboutMe();
//
//                        if (userFName.equals("") && userSecName.equals("")) {
//                            userFName = "Без";
//                            userSecName = "имени";
//                        }
//                        if (userAboutMe.equals("")) {
//                            userAboutMe = "Напишите пару строк о себе";
//                        }
//
//                        name_field.setText(String.format("%1s %2s", userFName, userSecName));
//                        login_field.setText(user.getLogin());
//                        aboutme_field.setText(userAboutMe);
//                    } else if (resp.getError().equals("invalid authToken")) {
//                        Intent intent = new Intent(getBaseContext(), ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
//                        startActivityForResult(intent, 1);
//                    }
//                } else {
//                    showMessage("Сервер вернул ошибку");
//                    aboutme_field.setVisibility(View.VISIBLE);
//                    aboutme_progressbar.setVisibility(View.INVISIBLE);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<MyResponse<UserModel>> call, Throwable t) {
//                showMessage("Сервер не отвечает");
//            }
//        });
//    }

//    private void uploadImage(final Uri fileUri) {
//        String token = tokenUtils.getToken();
//
//        File file = new File(getRealPathFromURI(fileUri));
//
//        if (file.length() <= 2097152) {
//            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), file);
//            MultipartBody.Part filePart = MultipartBody.Part.createFormData("img", file.getName(), requestFile);
//
//            Call<MyResponse<String>> call = periscopeApi.uploadImage(filePart, token);
//            call.enqueue(new Callback<MyResponse<String>>() {
//                @Override
//                public void onResponse(Call<MyResponse<String>> call, Response<MyResponse<String>> response) {
//                    if (response.isSuccessful()) {
//                        MyResponse<String> resp = response.body();
//
//                        if (resp.getError() == null) {
//                            profile_img.setBackground(null);
//                            profile_img.setImageBitmap(null);
//                            profile_img.setImageURI(fileUri);
//                            resetImageLoader();
//                            initializeImageLoader();
//                            sharedPrefWrapper.setString(getString(R.string.profile_image_path), resp.getData());
//                        } else if (resp.getError().equals("invalid authToken")) {
//                            Intent intent = new Intent(getBaseContext(), ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
//                            startActivityForResult(intent, 1);
//                        }
//                    } else {
//                        showMessage("Сервер вернул ошибку");
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<MyResponse<String>> call, Throwable t) {
//                    showMessage("Сервер не отвечает");
//                }
//            });
//        } else {
//            showMessage("Размер файла не должен превышать 2 Мб");
//        }
//    }


    private void checkForUpdates() {
        setDataFromCache();
        retrofitWrapper.checkForUpdates(new RetrofitWrapper.Callback<UserModel>() {
            @Override
            public void onSuccess(UserModel user) {
                if (user != null) {
                    resetImageLoader();
                    initializeImageLoader();
                    setCache(user);
                    setDataFromUserModel(user);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                showMessage(t.getMessage());
                aboutme_field.setVisibility(View.VISIBLE);
                aboutme_progressbar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setProfileImg(final String url) {
        profile_img.setVisibility(View.GONE);
        profile_img_progressbar.setVisibility(View.VISIBLE);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).build();
        imageLoader.displayImage(url, profile_img, defaultOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                profile_img.setVisibility(View.VISIBLE);
                profile_img_progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                profile_img.setVisibility(View.VISIBLE);
                profile_img_progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                profile_img.setVisibility(View.VISIBLE);
                profile_img_progressbar.setVisibility(View.GONE);
            }
        });
    }

    private void setDataFromUserModel(UserModel user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        if (firstName.equals("") && lastName.equals("")) {
            firstName = "Ваше имя";
            lastName = "и фамилия";
        }
        String aboutMe = user.getAboutMe();
        if (aboutMe.equals("")) {
            aboutMe = "Напишите пару строк о себе";
        }

        setProfileImg(user.getProfileImagePath());
        name_field.setText(String.format("%1s %2s", firstName, lastName));
        login_field.setText("@" + user.getLogin());
        aboutme_field.setText(aboutMe);
    }

    private void setDataFromCache() {
        if (sharedPrefWrapper.getString("firstName") == null) {
            setDefaultData();
        } else {
            String firstName = sharedPrefWrapper.getString("firstName");
            String lastName = sharedPrefWrapper.getString("lastName");
            if (firstName.equals("") && lastName.equals("")) {
                firstName = "Ваше имя";
                lastName = "и фамилия";
            }
            String aboutMe = sharedPrefWrapper.getString("aboutMe");
            if (aboutMe.equals("")) {
                aboutMe = "Напишите пару строк о себе";
            }

            imageLoader.displayImage(sharedPrefWrapper.getString("profileImgPath"), profile_img);
            name_field.setText(String.format("%1s %2s", firstName, lastName));
            login_field.setText("@" + sharedPrefWrapper.getString("login"));
            aboutme_field.setText(aboutMe);
        }
    }

    private void setDefaultData() {
        profile_img.setImageDrawable(getResources().getDrawable(R.drawable.default_profile_img));
        name_field.setText(".............");
        login_field.setText("@......");
        aboutme_field.setText("О себе");
    }

    private void setCache(UserModel user) {
        sharedPrefWrapper.setString("profileImgPath", user.getProfileImagePath());
        sharedPrefWrapper.setString("firstName", user.getFirstName());
        sharedPrefWrapper.setString("lastName", user.getLastName());
        sharedPrefWrapper.setString("login", user.getLogin());
        sharedPrefWrapper.setString("aboutMe", user.getAboutMe());
        sharedPrefWrapper.setString("updateDate", user.getUpdateDate().toString());
    }


    private void signoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Вы уверены, что хотите выйти?");
        builder.setNegativeButton("Отмена", null);
        builder.setPositiveButton("Выйти",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        signOut();
                    }
                });
        builder.show();
    }

    private void setToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
    }

    private void signOut() {
        Intent intent = new Intent();
        intent.putExtra("signOut", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setActivitiesItems() {
        exit_acc_btn = (Button) findViewById(R.id.exit_acc_btn);
        exit_acc_btn.setOnClickListener(this);
        profile_img = (ImageView) findViewById(R.id.profile_img);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tokenUtils = new TokenUtils(this);
        retrofitWrapper = new RetrofitWrapper(this);
        sharedPrefWrapper = new SharedPrefWrapper(this);
        profile_img_progressbar = (ProgressBar) findViewById(R.id.profile_img_progressbar);
        aboutme_progressbar = (ProgressBar) findViewById(R.id.aboutme_progressbar);
        name_field = (TextView) findViewById(R.id.name_field);
        login_field = (TextView) findViewById(R.id.login_field);
        aboutme_field = (TextView) findViewById(R.id.aboutme_field);
    }

    private void resetImageLoader() {
        imageLoader.clearDiscCache();
        imageLoader.clearMemoryCache();
        imageLoader.destroy();
    }

    private void initializeImageLoader() {
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration.Builder f = new ImageLoaderConfiguration.Builder(this);
        imageLoader.init(f.build());
    }

    private void initializeServerApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://anton-var.ddns.net:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        periscopeApi = retrofit.create(PeriscopeApi.class);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getBaseContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private boolean isDefaultImg(String url) {
        Pattern p = Pattern.compile(".+default\\..+");
        Matcher m = p.matcher(url);

        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }
}