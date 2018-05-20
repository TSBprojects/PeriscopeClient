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
        if (sharedPrefWrapper.getString(getString(R.string.first_name)) == null) {
            setDefaultData();
        } else {
            String firstName = sharedPrefWrapper.getString(getString(R.string.first_name));
            String lastName = sharedPrefWrapper.getString(getString(R.string.last_name));
            if (firstName.equals("") && lastName.equals("")) {
                firstName = "Ваше имя";
                lastName = "и фамилия";
            }
            String aboutMe = sharedPrefWrapper.getString(getString(R.string.about_me));
            if (aboutMe.equals("")) {
                aboutMe = "Напишите пару строк о себе";
            }

            imageLoader.displayImage(sharedPrefWrapper.getString(getString(R.string.profile_img_path)), profile_img);
            name_field.setText(String.format("%1s %2s", firstName, lastName));
            login_field.setText(String.format("@%s",sharedPrefWrapper.getString(getString(R.string.login))));
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
        sharedPrefWrapper.setString(getString(R.string.profile_img_path), user.getProfileImagePath());
        sharedPrefWrapper.setString(getString(R.string.first_name), user.getFirstName());
        sharedPrefWrapper.setString(getString(R.string.last_name), user.getLastName());
        sharedPrefWrapper.setString(getString(R.string.login), user.getLogin());
        sharedPrefWrapper.setString(getString(R.string.about_me), user.getAboutMe());
        sharedPrefWrapper.setString(getString(R.string.update_date), user.getUpdateDate().toString());
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
                .baseUrl(String.format("http://%1$s:%2$s", getString(R.string.server_domain_name), getString(R.string.servers_port)))
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