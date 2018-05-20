package ru.sstu.vak.periscopeclient;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.sstu.vak.periscopeclient.Retrofit.PeriscopeApi;
import ru.sstu.vak.periscopeclient.infrastructure.SharedPrefWrapper;
import ru.sstu.vak.periscopeclient.infrastructure.TokenUtils;
import ru.sstu.vak.periscopeclient.liveVideoBroadcaster.RecordActivity;
import ru.sstu.vak.periscopeclient.viewPager.MyFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static String RTMP_BASE_URL;
    boolean darkTheme;
    boolean exit = false;
    private MainActivity instance;
    private ViewPager pager;
    private MyFragmentPagerAdapter pagerAdapter;
    private Toolbar tool_bar;
    private TabLayout tab_bar;
    private FloatingActionButton record_activity_fab;
    private TokenUtils tokenUtils;
    private SharedPrefWrapper sharedPrefWrapper;


//    public void test(View view) {
//        Intent intent = new Intent(this, Test.class);
//        startActivity(intent);
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(ru.sstu.vak.periscopeclient.R.layout.activity_main);

        setActivitiesItems();

        //check ref token
        String token = tokenUtils.getToken();
        if (!(token != null && !token.equals(""))) {
            Intent intent = new Intent(this, ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
            startActivityForResult(intent, 1);
        }

        darkTheme = false;
        instance = this;
        setCurrentIP();
        setSupportActionBar(tool_bar);
        initializeFragmentAdapter();
        initializeTabBar();


        tab_bar.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                    case 2: {
                        Window window = instance.getWindow();
                        window.setStatusBarColor(ContextCompat.getColor(instance, R.color.colorPrimaryDark));
                        tool_bar.setBackgroundColor(ContextCompat.getColor(instance, R.color.colorPrimary));
                        darkTheme = false;
                        break;
                    }
                    case 1: {
                        Window window = instance.getWindow();
                        window.setStatusBarColor(ContextCompat.getColor(instance, R.color.darkThemePrimaryDark));
                        tool_bar.setBackgroundColor(ContextCompat.getColor(instance, R.color.darkThemePrimary));
                        darkTheme = true;
                        break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (data.getBooleanExtra("exit", this.exit)) {
            finish();
        }
        String token = data.getStringExtra("token");
        if (token != null && !token.equals("")) {
            tokenUtils.setToken(data.getStringExtra("token"));
        }
        if (data.getBooleanExtra("signOut", false)) {
            getIntent().putExtra("signOut", true);
            tokenUtils.setToken("");
            sharedPrefWrapper.setString(getString(R.string.updateDate), "");
            Intent intent = new Intent(this, ru.sstu.vak.periscopeclient.AuthorizationActivity.class);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_tool_bar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.account_btn: {
                Intent intent = new Intent(this, AccountActivity.class);
                startActivityForResult(intent, 2);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.record_activity_fab: {
                Intent i = new Intent(this, RecordActivity.class);
                startActivity(i);
                break;
            }
        }
    }


    private void setCurrentIP() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress ad = InetAddress.getByName(getString(R.string.server_domain_name));
                    RTMP_BASE_URL = String.format("rtmp://%1$s:1935/vod/", getIP(ad.toString()));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private String getIP(String inetAdressResult) {
        Pattern p = Pattern.compile("(.+\\/)(.+)");
        Matcher m = p.matcher(inetAdressResult);
        if (m.matches()) {
            return m.group(2);
        } else {
            return "";
        }
    }

    private void setActivitiesItems() {
        record_activity_fab = findViewById(R.id.record_activity_fab);
        record_activity_fab.setOnClickListener(this);
        tab_bar = findViewById(R.id.tab_bar);
        tool_bar = findViewById(R.id.my_toolbar);
        pager = findViewById(R.id.pager);
        tokenUtils = new TokenUtils(this);
        sharedPrefWrapper = new SharedPrefWrapper(this);
    }

    private void initializeFragmentAdapter() {
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
    }


    private void initializeTabBar() {
        tab_bar.setupWithViewPager(pager);
        tab_bar.getTabAt(0).setIcon(R.drawable.ic_format_list_bulleted_24dp);
        tab_bar.getTabAt(0).setText("");
        tab_bar.getTabAt(0).setContentDescription(getString(R.string.first_tab));
        tab_bar.getTabAt(1).setIcon(R.drawable.ic_world_24dp);
        tab_bar.getTabAt(1).setText("");
        tab_bar.getTabAt(1).setContentDescription(getString(R.string.second_tab));
        tab_bar.setBackground(getResources().getDrawable(R.drawable.ripple));
    }


    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
