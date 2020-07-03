package com.dji.ux.sample;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import com.secneo.sdk.Helper;

import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

import static com.dji.ux.sample.DJIConnectionControlActivity.ACCESSORY_ATTACHED;


public class MApplication extends Application {
    public static BaseProduct mProduct = null;
    public static Handler mFpvHandler;
    private static MApplication app = null;
    @Override
    public void onCreate() {
        super.onCreate();
        BroadcastReceiver br = new OnDJIUSBAttachedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACCESSORY_ATTACHED);
        registerReceiver(br, filter);
        mFpvHandler = new Handler(Looper.getMainLooper());
    }

    public static MApplication getInstance() {
        return MApplication.app;
    }
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        app = this;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) getProductInstance();
    }
    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }
    /**
     * This function is used to get the instance of DJIBaseProduct.
     * If no product is connected, it returns null.
     */
    public static synchronized BaseProduct getProductInstance() {
        mProduct = DJISDKManager.getInstance().getProduct();
        return mProduct;
    }

}
