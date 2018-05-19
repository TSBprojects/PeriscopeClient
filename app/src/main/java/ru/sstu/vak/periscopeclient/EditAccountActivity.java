package ru.sstu.vak.periscopeclient;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.Retrofit.PeriscopeApi;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyRequest;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyResponse;
import ru.sstu.vak.periscopeclient.Retrofit.models.UserModel;
import ru.sstu.vak.periscopeclient.infrastructure.LoadingDialog;
import ru.sstu.vak.periscopeclient.infrastructure.SharedPrefWrapper;
import ru.sstu.vak.periscopeclient.infrastructure.TokenUtils;

public class EditAccountActivity extends AppCompatActivity implements View.OnClickListener {

    private String[] fields = new String[4];


    private Uri localFileUri = null;

    private TokenUtils tokenUtils;
    private PeriscopeApi periscopeApi;
    private SharedPrefWrapper sharedPrefWrapper;

    private ProgressBar profile_img_progressbar;
    private Toolbar toolbar;
    private ImageView edit_profile_btn;
    private EditText first_name_field;
    private EditText last_name_field;
    private EditText login_field;
    private EditText aboutme_field;
    private TextView first_name_error_field;
    private TextView last_name_error_field;
    private TextView login_error_field;
    private TextView aboutme_error_field;

    private ImageLoader imageLoader;
    private ImageView profile_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);
        setActivitiesItems();
        initializeImageLoader();
        initializeServerApi();
        setToolBar();

        setDataFromCache();
        setFields();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri fileUri = result.getUri();
                if (isValidSize(fileUri)) {
                    localFileUri = fileUri;
                    profile_img.setImageURI(localFileUri);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
//        if (data != null) {
//            Uri fileUri = data.getData();
//            if (isValidSize(fileUri)) {
//                localFileUri = fileUri;
//                profile_img.setImageURI(localFileUri);
//            }
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_account_tool_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_btn: {
                if (isNeedSave()) {
                    if (isValidForm()) {
                        saveChanges();
                    }
                } else {
                    finishActivity();
                }
                break;
            }
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_profile_btn: {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setBorderLineColor(getResources().getColor(R.color.colorPrimary))
                        .start(this);
//                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                photoPickerIntent.setType("image/*");
//                startActivityForResult(photoPickerIntent, 1);
                break;
            }
        }
    }



    private void setDataFromCache() {
        imageLoader.displayImage(sharedPrefWrapper.getString("profileImgPath"), profile_img);
        first_name_field.setText(sharedPrefWrapper.getString("firstName"));
        last_name_field.setText(sharedPrefWrapper.getString("lastName"));
        String s =sharedPrefWrapper.getString("login");
        login_field.setText(sharedPrefWrapper.getString("login"));
        aboutme_field.setText(sharedPrefWrapper.getString("aboutMe"));
    }

    private void saveChanges() {
        String token = tokenUtils.getToken();

        File file = null;
        MultipartBody.Part filePart = null;
        if (localFileUri != null) {
            file = new File(localFileUri.getPath());
            //file = new File(getRealPathFromURI(localFileUri));
            //RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(localFileUri)), file);
            RequestBody requestFile = RequestBody.create(MediaType.parse(getMimeType(localFileUri.getPath())), file);
            filePart = MultipartBody.Part.createFormData("img", file.getName(), requestFile);
        }

        final LoadingDialog dialog = showLoadDialog();

        UserModel userModel = new UserModel(-1,
                login_field.getText().toString(),
                "",
                0,
                first_name_field.getText().toString(),
                last_name_field.getText().toString(),
                aboutme_field.getText().toString());

        Call<MyResponse<Void>> call = periscopeApi.saveChanges(filePart, new MyRequest<UserModel>(userModel, token));
        call.enqueue(new Callback<MyResponse<Void>>() {
            @Override
            public void onResponse(Call<MyResponse<Void>> call, Response<MyResponse<Void>> response) {
                if (response.isSuccessful()) {
                    MyResponse<Void> resp = response.body();

                    if (resp.getError() == null) {
                        localFileUri = null;
                        finishActivity();
                    } else if (resp.getError().equals("invalid authToken")) {
                        Intent intent = new Intent(getBaseContext(), ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
                        startActivityForResult(intent, 1);
                    } else {
                        login_error_field.setVisibility(View.VISIBLE);
                        login_error_field.setText(resp.getError());
                    }
                    dialog.dismiss();
                } else {
                    showMessage("Сервер вернул ошибку");
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<MyResponse<Void>> call, Throwable t) {
                showMessage("Сервер не отвечает");
                dialog.dismiss();
            }
        });
    }

    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private boolean isValidSize(Uri fileUri) {
        //File file = new File(getRealPathFromURI(fileUri));
        File file = new File(fileUri.toString());
        long s = file.length();
        if (file.length() <= 1048576) {
            return true;
        }
        showMessage("Слишком большой файл!");
        return false;
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

    private boolean isValidForm() {
        if (login_field.getText().toString().length() < 3) {
            login_error_field.setVisibility(View.VISIBLE);
            login_error_field.setText("логин должен содержать не менее 3 символов!");
            return false;
        } else {
            login_error_field.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    private LoadingDialog showLoadDialog() {
        FragmentManager fm = getSupportFragmentManager();
        LoadingDialog dialog = new LoadingDialog();
        dialog.setCancelable(false);
        dialog.show(fm, "tag");
        return dialog;
    }

    private void setToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
    }

    private void setActivitiesItems() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        edit_profile_btn = (ImageView) findViewById(R.id.edit_profile_btn);
        edit_profile_btn.setOnClickListener(this);
        first_name_field = (EditText) findViewById(R.id.first_name_field);
        last_name_field = (EditText) findViewById(R.id.last_name_field);
        login_field = (EditText) findViewById(R.id.login_field);
        login_field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isValidForm();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        aboutme_field = (EditText) findViewById(R.id.aboutme_field);
        first_name_error_field = (TextView) findViewById(R.id.first_name_error_field);
        last_name_error_field = (TextView) findViewById(R.id.last_name_error_field);
        login_error_field = (TextView) findViewById(R.id.login_error_field);
        aboutme_error_field = (TextView) findViewById(R.id.aboutme_error_field);
        profile_img = (ImageView) findViewById(R.id.profile_img);
        profile_img_progressbar = (ProgressBar) findViewById(R.id.profile_img_progressbar);
        tokenUtils = new TokenUtils(this);
        sharedPrefWrapper = new SharedPrefWrapper(this);
    }

    private void initializeServerApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://anton-var.ddns.net:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        periscopeApi = retrofit.create(PeriscopeApi.class);
    }

    private void initializeImageLoader() {
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration.Builder f = new ImageLoaderConfiguration.Builder(this);
        imageLoader.init(f.build());
    }


    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void finishActivity() {
        setResult(RESULT_OK, null);
        finish();
    }

    private boolean isNeedSave() {
        if (fields[0].equals(first_name_field.getText().toString())
                && fields[1].equals(last_name_field.getText().toString())
                && fields[2].equals(login_field.getText().toString())
                && fields[3].equals(aboutme_field.getText().toString())
                && localFileUri == null) {
            return false;
        }
        return true;
    }

    private void setFields() {
        fields[0] = first_name_field.getText().toString();
        fields[1] = last_name_field.getText().toString();
        fields[2] = login_field.getText().toString();
        fields[3] = aboutme_field.getText().toString();
    }
}


//    private String removeFirstSymbol(String str) {
//        return str.substring(1, str.length());
//    }
//        private boolean isDefaultImg(String url) {
//        Pattern p = Pattern.compile(".+default\\..+");
//        Matcher m = p.matcher(url);
//
//        if (m.matches()) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//    private void updateUser() {
//        final LoadingDialog dialog = showLoadDialog();
//
//        UserModel userModel = new UserModel(-1,
//                login_field.getText().toString(),
//                netFilePath,
//                first_name_field.getText().toString(),
//                last_name_field.getText().toString(),
//                aboutme_field.getText().toString());
//
//        String token = tokenUtils.getToken();
//
//
//        Call<MyResponse<Void>> call = periscopeApi.updateUser(new MyRequest<UserModel>(userModel, token));
//        call.enqueue(new Callback<MyResponse<Void>>() {
//            @Override
//            public void onResponse(Call<MyResponse<Void>> call, Response<MyResponse<Void>> response) {
//                if (response.isSuccessful()) {
//                    MyResponse<Void> resp = response.body();
//
//                    if (resp.getError() == null) {
//                        finishActivity();
//                    } else if (resp.getError().equals("invalid authToken")) {
//                        Intent intent = new Intent(getBaseContext(), ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
//                        startActivityForResult(intent, 1);
//                    } else {
//                        login_error_field.setVisibility(View.VISIBLE);
//                        login_error_field.setText(resp.getError());
//                    }
//                    dialog.dismiss();
//                } else {
//                    showMessage("Сервер вернул ошибку");
//                    dialog.dismiss();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<MyResponse<Void>> call, Throwable t) {
//                showMessage("Сервер не отвечает");
//                dialog.dismiss();
//            }
//        });
//    }
//
//    private void uploadImg() {
//        File file = new File(getRealPathFromURI(localFileUri));
//
//        if (file.length() <= 2097152) {
//            final LoadingDialog dialog = showLoadDialog();
//
//            UserModel userModel = new UserModel(-1,
//                    login_field.getText().toString(),
//                    "",
//                    first_name_field.getText().toString(),
//                    last_name_field.getText().toString(),
//                    aboutme_field.getText().toString());
//
//            String token = tokenUtils.getToken();
//            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(localFileUri)), file);
//            MultipartBody.Part filePart = MultipartBody.Part.createFormData("img", file.getName(), requestFile);
//
//            Call<MyResponse<Void>> call = periscopeApi.saveChanges(filePart, new MyRequest<UserModel>(userModel,token));
//            call.enqueue(new Callback<MyResponse<Void>>() {
//                @Override
//                public void onResponse(Call<MyResponse<Void>> call, Response<MyResponse<Void>> response) {
//                    if (response.isSuccessful()) {
//                        MyResponse<Void> resp = response.body();
//
//                        if (resp.getError() == null) {
//                            localFileUri = null;
//                            //netFilePath = resp.getData();
//                            if (isNeedSave()) {
//                                updateUser();
//                            } else {
//                                finishActivity();
//                            }
//                        } else if (resp.getError().equals("invalid authToken")) {
//                            Intent intent = new Intent(getBaseContext(), ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
//                            startActivityForResult(intent, 1);
//                        } else {
//                            login_error_field.setVisibility(View.VISIBLE);
//                            login_error_field.setText(resp.getError());
//                        }
//                        dialog.dismiss();
//                    } else {
//                        showMessage("Сервер вернул ошибку");
//                        dialog.dismiss();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<MyResponse<Void>> call, Throwable t) {
//                    showMessage("Сервер не отвечает");
//                    dialog.dismiss();
//                }
//            });
//        } else {
//            showMessage("Размер файла не должен превышать 2 Мб");
//        }
//    }
//
//    private void setProfileImg(final String url) {
//        profile_img.setImageDrawable(getResources().getDrawable(R.drawable.default_profile_img));
//        if (!isDefaultImg(url)) {
//            profile_img.setBackground(null);
//            profile_img.setImageBitmap(null);
//
//            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).imageScaleType(ImageScaleType.EXACTLY).build();
//            imageLoader.displayImage(url, profile_img, defaultOptions, new ImageLoadingListener() {
//                @Override
//                public void onLoadingStarted(String imageUri, View view) {
//                    edit_profile_btn.setVisibility(View.INVISIBLE);
//                    profile_img.setVisibility(View.INVISIBLE);
//                    profile_img_progressbar.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                    edit_profile_btn.setVisibility(View.VISIBLE);
//                    profile_img.setVisibility(View.VISIBLE);
//                    profile_img_progressbar.setVisibility(View.INVISIBLE);
//                    profile_img.setImageDrawable(getResources().getDrawable(R.drawable.default_profile_img));
//                }
//
//                @Override
//                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    edit_profile_btn.setVisibility(View.VISIBLE);
//                    profile_img.setVisibility(View.VISIBLE);
//                    profile_img_progressbar.setVisibility(View.INVISIBLE);
//                }
//
//                @Override
//                public void onLoadingCancelled(String imageUri, View view) {
//
//                }
//            });
//        }
//    }
