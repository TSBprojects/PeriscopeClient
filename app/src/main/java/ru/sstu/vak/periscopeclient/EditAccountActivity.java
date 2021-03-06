package ru.sstu.vak.periscopeclient;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.Retrofit.PeriscopeApi;
import ru.sstu.vak.periscopeclient.Retrofit.RetrofitWrapper;
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
    private RetrofitWrapper retrofitWrapper;
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
                // do nothing
            }
        }
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
                    finishActivity(false);
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
                break;
            }
        }
    }



    private void setDataFromCache() {
        imageLoader.displayImage(sharedPrefWrapper.getString(getString(R.string.profile_img_path)), profile_img);
        first_name_field.setText(sharedPrefWrapper.getString(getString(R.string.first_name)));
        last_name_field.setText(sharedPrefWrapper.getString(getString(R.string.last_name)));
        login_field.setText(sharedPrefWrapper.getString(getString(R.string.login)));
        aboutme_field.setText(sharedPrefWrapper.getString(getString(R.string.about_me)));
    }

    private void saveChanges() {

        final LoadingDialog dialog = showLoadDialog();

        UserModel userModel = new UserModel(-1,
                login_field.getText().toString(),
                "",
                0,
                first_name_field.getText().toString(),
                last_name_field.getText().toString(),
                aboutme_field.getText().toString());

        retrofitWrapper.saveAccountChanges(localFileUri, userModel, new RetrofitWrapper.Callback<MyResponse<Void>>() {
            @Override
            public void onSuccess(MyResponse<Void> response) {
                if (response.getError() == null) {
                    localFileUri = null;
                    finishActivity(false);
                } else {
                    login_error_field.setVisibility(View.VISIBLE);
                    login_error_field.setText(response.getError());
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Throwable t) {
                showMessage(t.getMessage());
                dialog.dismiss();
            }
        });
    }

    private boolean isValidSize(Uri fileUri) {
        //File file = new File(getRealPathFromURI(fileUri));
        File file = new File(fileUri.getPath());
        long s = file.length();
        if (file.length() <= 1048576) {
            return true;
        }
        showMessage(getString(R.string.file_is_too_big));
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
            login_error_field.setText(R.string.at_least_3_characters);
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
        retrofitWrapper = new RetrofitWrapper(this);
        sharedPrefWrapper = new SharedPrefWrapper(this);
    }

    private void initializeServerApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.format("http://%1$s:%2$s", getString(R.string.server_domain_name), getString(R.string.server_port)))
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

    private void finishActivity(boolean exit) {
        Intent intent = new Intent();
        intent.putExtra("exit", exit);
        setResult(RESULT_OK, intent);
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