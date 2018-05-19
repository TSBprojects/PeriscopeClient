package ru.sstu.vak.periscopeclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.Retrofit.PeriscopeApi;
import ru.sstu.vak.periscopeclient.Retrofit.RetrofitWrapper;
import ru.sstu.vak.periscopeclient.Retrofit.models.LoginModel;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyRequest;
import ru.sstu.vak.periscopeclient.Retrofit.models.MyResponse;
import ru.sstu.vak.periscopeclient.Retrofit.models.UserModel;
import ru.sstu.vak.periscopeclient.infrastructure.LoadingDialog;

public class AuthorizationActivity extends AppCompatActivity implements View.OnClickListener {

    private final int ANIM_DURATION = 400;
    private Point SCREEN_SIZE;
    private PeriscopeApi periscopeApi;
    private RetrofitWrapper retrofitWrapper;

    private Toolbar toolbar;
    private LinearLayout toolbar_layout;

    private ImageButton back_btn;
    private TextView toolbar_layout_description;
    private TextView reg_btn;
    private TextView enter_btn;

    private LinearLayout buttons_layout;
    private FrameLayout reg_container;
    private FrameLayout login_container;

    private TextView submit_reg_btn;
    private TextView submit_login_btn;

    private TextView reg_login_field;
    private TextView reg_pass_field;
    private TextView reg_repeatpass_field;
    private TextView reg_login_error_field;
    private TextView reg_pass_error_field;
    private TextView reg_repeatpass_error_field;

    private TextView login_login_field;
    private TextView login_pass_field;
    private TextView login_login_error_field;
    private TextView login_pass_error_field;
    private TextView login_global_error;

    private boolean animationDuring = false;
    private boolean loginActive = false;
    private boolean regActive = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        initializeScreenSize();
        setActivitiesItems();
        initializeServerApi();
    }

    @Override
    public void onBackPressed() {
        if (!regActive && !loginActive) {
            finishActivity(true, "");
        } else {
            if (loginActive) {
                hideLoginLayout(ANIM_DURATION);
                showMainButtons(ANIM_DURATION);
                cleanForms();
            } else {
                hideRegLayout(ANIM_DURATION);
                showMainButtons(ANIM_DURATION);
                cleanForms();
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.reg_btn: {
                hideMainButtons(ANIM_DURATION, 190, 175);
                showRegLayout(ANIM_DURATION);
                break;
            }
            case R.id.enter_btn: {
                hideMainButtons(ANIM_DURATION, 150, 150);
                showLoginLayout(ANIM_DURATION);

                break;
            }
            case R.id.submit_reg_btn: {
                if (isValidRegForm()) {
                    registerUser(reg_login_field.getText().toString(), reg_pass_field.getText().toString());
                }
                break;
            }
            case R.id.submit_login_btn: {
                if (isValidLoginForm()) {
                    loginUser(login_login_field.getText().toString(), login_pass_field.getText().toString());
                }
                break;
            }
            case R.id.back_btn: {
                if (getCurrentFocus() != null) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus().
                                    getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (loginActive) {
                    hideLoginLayout(ANIM_DURATION);
                    showMainButtons(ANIM_DURATION);
                    cleanForms();
                } else {
                    hideRegLayout(ANIM_DURATION);
                    showMainButtons(ANIM_DURATION);
                    cleanForms();
                }
                break;
            }
        }
    }

//    class MyTask extends AsyncTask<String, Integer, Void> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Void doInBackground(String... urls) {
//            try {
//                TimeUnit.SECONDS.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//
//            Intent intent = new Intent();
//            intent.putExtra("exit", false);
//            setResult(RESULT_OK, intent);
//            finish();
//        }
//    }

    private void registerUser(String login, String password) {
        final LoadingDialog dialog = showLoadDialog();
        retrofitWrapper.registerUser(login, password, new RetrofitWrapper.Callback<MyResponse<String>>() {
            @Override
            public void onSuccess(MyResponse<String> response) {
                if (response.getError() == null) {
                    dialog.dismiss();
                    finishActivity(false, response.getData());
                } else {
                    //логин уже занят
                    dialog.dismiss();
                    reg_login_error_field.setVisibility(View.VISIBLE);
                    reg_login_error_field.setText(response.getError());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                dialog.dismiss();
                cleanForms();
                showMessage(t.getMessage());
            }
        });
    }

    private void loginUser(String login, String password) {
        final LoadingDialog dialog = showLoadDialog();
        retrofitWrapper.loginUser(login, password, new RetrofitWrapper.Callback<MyResponse<String>>() {
            @Override
            public void onSuccess(MyResponse<String> response) {
                if (response.getError() == null) {
                    dialog.dismiss();
                    finishActivity(false, response.getData());
                } else {
                    //неправильный логин или пароль
                    dialog.dismiss();
                    login_global_error.setVisibility(View.VISIBLE);
                    login_global_error.setText(response.getError());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                dialog.dismiss();
                cleanForms();
                showMessage(t.getMessage());
            }
        });
    }

    private void finishActivity(boolean exit, String token) {
        Intent intent = new Intent();
        intent.putExtra("exit", exit);
        intent.putExtra("token", token);
        setResult(RESULT_OK, intent);
        finish();
    }


    private boolean isValidRegForm() {
        boolean login = true;
        boolean pass = true;
        boolean repeatPass = true;
        if (reg_login_field.getText().toString().equals("")) {
            reg_login_error_field.setVisibility(View.VISIBLE);
            reg_login_error_field.setText("введите логин!");
            login = false;
        } else {
            reg_login_error_field.setVisibility(View.INVISIBLE);
        }
        if (reg_pass_field.getText().toString().equals("")) {
            reg_pass_error_field.setVisibility(View.VISIBLE);
            reg_pass_error_field.setText("введите пароль!");
            pass = false;
        } else {
            reg_pass_error_field.setVisibility(View.INVISIBLE);
        }
        if (reg_repeatpass_field.getText().toString().equals("")) {
            reg_repeatpass_error_field.setVisibility(View.VISIBLE);
            reg_repeatpass_error_field.setText("подтвердите пароль!");
            repeatPass = false;
        } else if (!reg_pass_field.getText().toString().equals(reg_repeatpass_field.getText().toString())) {
            reg_repeatpass_error_field.setVisibility(View.VISIBLE);
            reg_repeatpass_error_field.setText("пароли не совпадают!");
            repeatPass = false;
        } else {
            reg_repeatpass_error_field.setVisibility(View.INVISIBLE);
        }
        return login && pass && repeatPass;
    }

    private boolean isValidLoginForm() {
        boolean login = true;
        boolean pass = true;
        if (login_login_field.getText().toString().equals("")) {
            login_login_error_field.setVisibility(View.VISIBLE);
            login_login_error_field.setText("введите логин!");
            login_global_error.setText("");
            login = false;
        } else {
            login_login_error_field.setVisibility(View.INVISIBLE);
        }
        if (login_pass_field.getText().toString().equals("")) {
            login_pass_error_field.setVisibility(View.VISIBLE);
            login_pass_error_field.setText("введите пароль!");
            login_global_error.setText("");
            pass = false;
        } else {
            login_pass_error_field.setVisibility(View.INVISIBLE);
        }
        return login && pass;
    }

    private void cleanForms() {
        reg_login_field.setText("");
        reg_pass_field.setText("");
        reg_repeatpass_field.setText("");
        reg_repeatpass_error_field.setText("");
        login_login_field.setText("");
        login_pass_field.setText("");
        login_global_error.setText("");
        reg_login_error_field.setVisibility(View.INVISIBLE);
        reg_pass_error_field.setVisibility(View.INVISIBLE);
        reg_repeatpass_error_field.setVisibility(View.INVISIBLE);
        login_login_error_field.setVisibility(View.INVISIBLE);
        login_pass_error_field.setVisibility(View.INVISIBLE);
        login_global_error.setVisibility(View.GONE);
    }

    private LoadingDialog showLoadDialog() {
        FragmentManager fm = getSupportFragmentManager();
        LoadingDialog dialog = new LoadingDialog();
        dialog.setCancelable(false);
        dialog.show(fm, "tag");
        return dialog;
    }


    private void showMainButtons(int duration) {
        loginActive = false;
        regActive = false;
        back_btn.setVisibility(View.INVISIBLE);
        toolbar_layout_description.animate()
                .setDuration(duration)
                .alpha(1);
        toolbar.animate()
                .setDuration(duration)
                .translationY(0);
        toolbar_layout.animate()
                .setDuration(duration)
                .translationY(0);

        buttons_layout.animate()
                .setDuration(duration)
                .alpha(1);
        buttons_layout.animate()
                .setDuration(duration)
                .translationX(0);

    }

    private void hideMainButtons(int duration, int toolbarTranslationDP, int toolbarLayoutTranslationDP) {
        if (!animationDuring) {
            animationDuring = true;
            back_btn.setVisibility(View.VISIBLE);
            toolbar_layout_description.animate()
                    .setDuration(duration)
                    .alpha(0);
            toolbar.animate()
                    .setDuration(duration)
                    .translationY(-convertDpToPixel(toolbarTranslationDP));
            toolbar_layout.animate()
                    .setDuration(duration)
                    .translationY(convertDpToPixel(toolbarLayoutTranslationDP));

            buttons_layout.animate()
                    .setDuration(duration)
                    .alpha(0);
            buttons_layout.animate()
                    .setDuration(duration)
                    .translationX(-SCREEN_SIZE.x)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animationDuring = false;
                        }
                    });
        }
    }

    private void showRegLayout(int duration) {
        if (!loginActive && !regActive) {
            loginActive = false;
            regActive = true;
            reg_container.animate()
                    .setDuration(duration)
                    .alpha(1);
            reg_container.animate()
                    .setDuration(duration)
                    .translationX(0);
        }
    }

    private void hideRegLayout(int duration) {
        regActive = false;
        reg_container.setVisibility(View.VISIBLE);
        reg_container.animate()
                .setDuration(duration)
                .alpha(0);
        reg_container.animate()
                .setDuration(duration)
                .translationX(SCREEN_SIZE.x);
    }

    private void showLoginLayout(int duration) {
        if (!loginActive && !regActive) {
            loginActive = true;
            regActive = false;
            login_container.animate()
                    .setDuration(duration)
                    .alpha(1);
            login_container.animate()
                    .setDuration(duration)
                    .translationX(0);
        }
    }

    private void hideLoginLayout(int duration) {
        loginActive = false;
        login_container.setVisibility(View.VISIBLE);
        login_container.animate()
                .setDuration(duration)
                .alpha(0);
        login_container.animate()
                .setDuration(duration)
                .translationX(SCREEN_SIZE.x);

    }

    private void setActivitiesItems() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_layout = (LinearLayout) findViewById(R.id.toolbar_layout);
        back_btn = (ImageButton) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(this);
        toolbar_layout_description = (TextView) findViewById(R.id.toolbar_layout_description);
        reg_btn = (TextView) findViewById(R.id.reg_btn);
        reg_btn.setOnClickListener(this);
        enter_btn = (TextView) findViewById(R.id.enter_btn);
        enter_btn.setOnClickListener(this);
        buttons_layout = (LinearLayout) findViewById(R.id.buttons_layout);
        reg_container = (FrameLayout) findViewById(R.id.reg_container);
        login_container = (FrameLayout) findViewById(R.id.login_container);
        submit_reg_btn = (TextView) findViewById(R.id.submit_reg_btn);
        submit_reg_btn.setOnClickListener(this);
        submit_login_btn = (TextView) findViewById(R.id.submit_login_btn);
        submit_login_btn.setOnClickListener(this);
        reg_login_field = (TextView) findViewById(R.id.reg_login_field);
        reg_pass_field = (TextView) findViewById(R.id.reg_pass_field);
        reg_repeatpass_field = (TextView) findViewById(R.id.reg_repeatpass_field);
        reg_login_error_field = (TextView) findViewById(R.id.reg_login_error_field);
        reg_pass_error_field = (TextView) findViewById(R.id.reg_pass_error_field);
        reg_repeatpass_error_field = (TextView) findViewById(R.id.reg_repeatpass_error_field);
        login_login_field = (TextView) findViewById(R.id.login_login_field);
        login_pass_field = (TextView) findViewById(R.id.login_pass_field);
        login_login_error_field = (TextView) findViewById(R.id.login_login_error_field);
        login_pass_error_field = (TextView) findViewById(R.id.login_pass_error_field);
        login_global_error = (TextView) findViewById(R.id.login_global_error);
        hideRegLayout(0);
        hideLoginLayout(0);
        retrofitWrapper = new RetrofitWrapper(this);
    }

    private void initializeScreenSize() {
        SCREEN_SIZE = new Point();
        getWindowManager().getDefaultDisplay().getSize(SCREEN_SIZE);
    }

    private void initializeServerApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://anton-var.ddns.net:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        periscopeApi = retrofit.create(PeriscopeApi.class);
    }


    private int convertDpToPixel(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.getResources().getDisplayMetrics());
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
