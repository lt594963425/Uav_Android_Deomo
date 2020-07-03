package com.dji.ux.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.useraccount.UserAccountManager;

/**
 * Main activity that displays three choices to user
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "MainActivity";
    private static final String LAST_USED_BRIDGE_IP = "bridgeip";
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static boolean isAppStarted = false;
    private static boolean isAppLogin = false;
    private static boolean isAppRegist = false;

    private DJISDKManager.SDKManagerCallback registrationCallback = new DJISDKManager.SDKManagerCallback() {
        @Override
        public void onRegister(DJIError error) {
            isRegistrationInProgress.set(false);
            if (error == DJISDKError.REGISTRATION_SUCCESS) {
                isAppRegist = true;
                loginAccount();
                DJISDKManager.getInstance().startConnectionToProduct();
            } else {
                isAppRegist = false;
                hindProgress();
                Toast.makeText(getApplicationContext(), "sdk注册失败, 请检查网络并重试!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onProductDisconnect() {
            baseProduct = null;
            Toast.makeText(getApplicationContext(), "无人机连接失败!", Toast.LENGTH_LONG).show();
            notifyStatusChange();
        }

        @Override
        public void onProductConnect(BaseProduct product) {
            baseProduct = product;
//            Toast.makeText(getApplicationContext(), "无人机连接成功!", Toast.LENGTH_LONG).show();
            notifyStatusChange();
        }

        @Override
        public void onComponentChange(BaseProduct.ComponentKey key,
                                      BaseComponent oldComponent,
                                      BaseComponent newComponent) {
//            Toast.makeText(getApplicationContext(), key.toString() + " changed", Toast.LENGTH_LONG).show();
            if (newComponent != null) {
                newComponent.setComponentListener(mDJIComponentListener);
            }
        }

        @Override
        public void onInitProcess(DJISDKInitEvent event, int totalProcess) {

        }

        @Override
        public void onDatabaseDownloadProgress(long current, long total) {

        }
    };
    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {

        @Override
        public void onConnectivityChange(boolean isConnected) {
            notifyStatusChange();
        }
    };
    private TextView uavStatus;

    private void notifyStatusChange() {
        EventBus.getDefault().post(new OnSDKManagerCallbackEvent());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (baseProduct != null && uavStatus != null) {
                    uavStatus.setText(baseProduct.getModel().getDisplayName());
                    uavStatus.setTextColor(Color.argb(100, 36, 203, 203));
                } else {
                    uavStatus.setText("设备未连接");
                    uavStatus.setTextColor(Color.argb(100, 250, 110, 110));
                }
            }
        });
    }

    private LinearLayout showProgressLl;
    private BaseProduct baseProduct;

    private void loginAccount() {
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {

                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        hindProgress();
                        Log.e("登录", "-----" + userAccountState.name() + "-----");
                        isAppLogin = true;
//                        Toast.makeText(getApplicationContext(), "Login Success!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(DJIError error) {
                        hindProgress();
                        isAppLogin = false;
                        Toast.makeText(getApplicationContext(), "取消登录!", Toast.LENGTH_LONG).show();
                    }
                });
        getDJIUserAccountName();
    }

    private void getDJIUserAccountName() {
        UserAccountManager.getInstance().getLoggedInDJIUserAccountName(
                new CommonCallbacks.CompletionCallbackWith<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.e("登录", "-----" + s + "-----");
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        Log.e("登录", "-----" + djiError.getDescription() + "-----");


                    }
                });
    }

    public void hindProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressLl.setVisibility(View.INVISIBLE);
            }
        });
    }

    public static boolean isStarted() {
        return isAppStarted;
    }

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE, // Gimbal rotation
            Manifest.permission.INTERNET, // API requests
            Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
            Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
            Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
            Manifest.permission.ACCESS_FINE_LOCATION, // Maps
            Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            Manifest.permission.BLUETOOTH, // Bluetooth connected products
            Manifest.permission.BLUETOOTH_ADMIN, // Bluetooth connected products
            Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
            Manifest.permission.RECORD_AUDIO // Speaker accessory
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private List<String> missingPermission = new ArrayList<>();
    private EditText bridgeModeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_main);
        StatusBarUtils.setNavBarVisibility(getWindow(),this, false);
        isAppStarted = true;
        showProgressLl = findViewById(R.id.show_progress_ll);
        uavStatus = findViewById(R.id.uav_status);
        showProgressLl.setVisibility(View.VISIBLE);
        findViewById(R.id.complete_ui_widgets).setOnClickListener(this);
        findViewById(R.id.bt_customized_ui_widgets).setOnClickListener(this);
        findViewById(R.id.bt_map_widget).setOnClickListener(this);
        TextView versionText = (TextView) findViewById(R.id.version);
        versionText.setText(getResources().getString(R.string.sdk_version, DJISDKManager.getInstance().getSDKVersion()));
        bridgeModeEditText = (EditText) findViewById(R.id.edittext_bridge_ip);
        bridgeModeEditText.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(LAST_USED_BRIDGE_IP, ""));
        bridgeModeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event != null
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event != null && event.isShiftPressed()) {
                        return false;
                    } else {
                        // the user is done typing.
                        handleBridgeIPTextChange();
                    }
                }
                return false; // pass on to other listeners.
            }
        });
        bridgeModeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.toString().contains("\n")) {
                    // the user is done typing.
                    // remove new line characcter
                    final String currentText = bridgeModeEditText.getText().toString();
                    bridgeModeEditText.setText(currentText.substring(0, currentText.indexOf('\n')));
                    handleBridgeIPTextChange();
                }
            }
        });
        checkAndRequestPermissions();
    }

    @Override
    protected void onDestroy() {
        DJISDKManager.getInstance().destroy();
        isAppStarted = false;
        super.onDestroy();
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            Toast.makeText(getApplicationContext(), "Missing permissions! Will not register SDK to connect to aircraft.", Toast.LENGTH_LONG).show();
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    DJISDKManager.getInstance().registerApp(MainActivity.this, registrationCallback);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        Class nextActivityClass;
        int id = view.getId();
        if (id == R.id.complete_ui_widgets) {
            if (isAppRegist) {
                if (isAppLogin) {
                    nextActivityClass = CompleteWidgetActivity.class;
                    Intent intent = new Intent(this, nextActivityClass);
                    startActivity(intent);
                } else {
                    loginAccount();
                }
            } else {
                checkAndRequestPermissions();
            }

        } else if (id == R.id.bt_customized_ui_widgets) {
            nextActivityClass = CustomizedWidgetsActivity.class;
            Intent intent = new Intent(this, nextActivityClass);
            startActivity(intent);
        } else {
            //nextActivityClass = MapWidgetActivity.class;
            PopupMenu popup = new PopupMenu(this, view);
            popup.setOnMenuItemClickListener(this);
            Menu popupMenu = popup.getMenu();
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.map_select_menu, popupMenu);
            popupMenu.findItem(R.id.here_map).setEnabled(isHereMapsSupported());
            popupMenu.findItem(R.id.google_map).setEnabled(isGoogleMapsSupported(this));
            popup.show();
            return;
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        Intent intent = new Intent(this, MapWidgetActivity.class);
        int mapBrand = 0;
        switch (menuItem.getItemId()) {
            case R.id.here_map:
                mapBrand = 0;
                break;
            case R.id.google_map:
                mapBrand = 1;
                break;
            case R.id.amap:
                mapBrand = 2;
                break;
            case R.id.mapbox:
                mapBrand = 3;
                break;
        }
        intent.putExtra(MapWidgetActivity.MAP_PROVIDER, mapBrand);
        startActivity(intent);
        return false;
    }

    public static boolean isHereMapsSupported() {
        String abi;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            abi = Build.CPU_ABI;
        } else {
            abi = Build.SUPPORTED_ABIS[0];
        }
        DJILog.d(TAG, "abi=" + abi);

        //The possible values are armeabi, armeabi-v7a, arm64-v8a, x86, x86_64, mips, mips64.
        return abi.contains("arm");
    }

    public static boolean isGoogleMapsSupported(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private void handleBridgeIPTextChange() {
        // the user is done typing.
        final String bridgeIP = bridgeModeEditText.getText().toString();

        if (!TextUtils.isEmpty(bridgeIP)) {
            DJISDKManager.getInstance().enableBridgeModeWithBridgeAppIP(bridgeIP);
            Toast.makeText(getApplicationContext(), "BridgeMode ON!\nIP: " + bridgeIP, Toast.LENGTH_SHORT).show();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(LAST_USED_BRIDGE_IP, bridgeIP).apply();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        StatusBarUtils.setNavBarVisibility(getWindow(), this, false);
    }
}
