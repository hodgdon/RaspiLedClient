package com.example.dhodgdon.raspiledclient;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.dhodgdon.raspiledclient.net.LedPatternModel;
import com.example.dhodgdon.raspiledclient.net.RaspiFunService;
import com.squareup.okhttp.HttpUrl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import retrofit.Callback;
import retrofit.MoshiConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity implements ServerSelectionDialogFragment.ServerSelectionFeedback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_views = new MainActivityViewHolder(this);

        m_views.redButton.setBackgroundColor(Color.RED);
        m_views.greenButton.setBackgroundColor(Color.GREEN);
        m_views.blueButton.setBackgroundColor(Color.BLUE);
        m_views.yellowButton.setBackgroundColor(Color.YELLOW);

        initListeners();

        m_raspiFunService = getRaspiFunServiceFromPrefs();
    }

    private void initListeners() {
        m_views.redButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onColorMotionEvent(MotionEventCompat.getActionMasked(event), COLOR_FLAG_RED);
            }
        });

        m_views.greenButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onColorMotionEvent(MotionEventCompat.getActionMasked(event), COLOR_FLAG_GREEN);
            }
        });

        m_views.blueButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onColorMotionEvent(MotionEventCompat.getActionMasked(event), COLOR_FLAG_BLUE);
            }
        });

        m_views.yellowButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onColorMotionEvent(MotionEventCompat.getActionMasked(event), COLOR_FLAG_YELLOW);
            }
        });
    }

    private boolean onColorMotionEvent(int action, @ColorFlag int color) {
        @ColorFlag int newColorState;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                newColorState = m_colorsState | color;
                break;
            case MotionEvent.ACTION_UP:
                newColorState = m_colorsState & ~color;
                break;
            default:
                return false;
        }
        m_colorsState = newColorState;

        Log.d(MainActivity.class.getSimpleName(), "ColorState(" + m_colorsState + ")");

        LedPatternModel ledPatternModel = getLedPatternModel();

        m_raspiFunService.postLedPattern(ledPatternModel).enqueue(new Callback<LedPatternModel>() {
            @Override
            public void onResponse(Response<LedPatternModel> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    Log.d(MainActivity.class.getSimpleName(), response.message());
                } else {
                    Log.d(MainActivity.class.getSimpleName(), "onResponse fail");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(MainActivity.class.getSimpleName(), "onFailure fail");
                if (getSupportFragmentManager().findFragmentByTag(SERVER_ENTRY_DIALOG_TAG) == null) {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    String ip = settings.getString(SERVER_INFO_IP_PREF_KEY, null);
                    Integer port;
                    if (settings.contains(SERVER_INFO_PORT_PREF_KEY)) {
                        port = settings.getInt(SERVER_INFO_PORT_PREF_KEY, 0);
                    } else {
                        port = null;
                    }

                    AppCompatDialogFragment dialogFragment = ServerSelectionDialogFragment.newInstance(ip, port);
                    dialogFragment.show(getSupportFragmentManager(), SERVER_ENTRY_DIALOG_TAG);
                }
            }
        });

        return true;
    }

    @NonNull
    private LedPatternModel getLedPatternModel() {
        LedPatternModel ledPatternModel = new LedPatternModel();
        ledPatternModel.blue = 0 != (m_colorsState & COLOR_FLAG_BLUE);
        ledPatternModel.green = 0 != (m_colorsState & COLOR_FLAG_GREEN);
        ledPatternModel.red = 0 != (m_colorsState & COLOR_FLAG_RED);
        ledPatternModel.yellow = 0 != (m_colorsState & COLOR_FLAG_YELLOW);
        return ledPatternModel;
    }

    @Override
    public void setServerIpPort(@Nullable String ip, @Nullable Integer port) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (null != ip || null != port) {
            SharedPreferences.Editor editor = settings.edit();
            if (null != ip) {
                editor.putString(SERVER_INFO_IP_PREF_KEY, ip);
            }
            if (null != port) {
                editor.putInt(SERVER_INFO_PORT_PREF_KEY, port);
            }
            editor.apply();
        }
        m_raspiFunService = getRaspiFunServiceFromPrefs();
    }

    private RaspiFunService getRaspiFunServiceFromPrefs() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String ip = settings.getString(SERVER_INFO_IP_PREF_KEY, "127.0.0.1");
        int port = settings.getInt(SERVER_INFO_PORT_PREF_KEY, 8080);

        HttpUrl baseUrl = new HttpUrl.Builder().scheme("http").host(ip).port(port).build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .baseUrl(baseUrl)
                .build();
        return retrofit.create(RaspiFunService.class);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true, value = {
            COLOR_FLAG_RED,
            COLOR_FLAG_BLUE,
            COLOR_FLAG_GREEN,
            COLOR_FLAG_YELLOW
    })
    private @interface ColorFlag {
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    private static final int
            COLOR_FLAG_RED = 1 << 0,
            COLOR_FLAG_BLUE = 1 << 1,
            COLOR_FLAG_GREEN = 1 << 2,
            COLOR_FLAG_YELLOW = 1 << 3;

    @ColorFlag
    private int m_colorsState;
    private MainActivityViewHolder m_views;
    private RaspiFunService m_raspiFunService;

    private static final String SERVER_ENTRY_DIALOG_TAG = "dialog";
    private static final String
            SERVER_INFO_IP_PREF_KEY = "serverIp",
            SERVER_INFO_PORT_PREF_KEY = "serverPort";
}
