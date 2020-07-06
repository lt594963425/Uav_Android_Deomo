package com.dji.ux.sample;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import dji.common.error.DJIError;


/**
 * Created by dji on 15/12/18.
 */



public class DJIUtils {
    public static final String FLAG_CONNECTION_CHANGE = "connection_change";
    public static final double ONE_METER_OFFSET = 0.00000899322;
    private static long lastClickTime;
    private static Handler mUIHandler = new Handler(Looper.getMainLooper());
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static void showDialogBasedOnError(Context ctx, DJIError djiError) {
        if (null == djiError) {
        }
            //DJIDialog.showDialog(ctx, R.string.success);
        else {
        }
            //DJIDialog.showDialog(ctx, djiError.getDescription());
    }


    public static void setResultToToast(final Context context, final String string) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast= Toast.makeText(context, string, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }
    public static void setResultToText(final Context context, final TextView tv, final String s) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (tv == null) {
                    Toast.makeText(context, "tv is null", Toast.LENGTH_SHORT).show();
                } else {
                    tv.setText(s);
                }
            }
        });
    }

    public static void changeVisibility(final Context context,final LinearLayout layout,final boolean visibility){
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (layout == null) {
                    Toast.makeText(context, "layout is null", Toast.LENGTH_SHORT).show();
                } else {
                    if(visibility) {
                        layout.setVisibility(View.VISIBLE);
                    }
                    else {
                        layout.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    public static void changeButtonText(final Context context, final Button btn, final String s) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (btn == null) {
                    Toast.makeText(context, "btn is null", Toast.LENGTH_SHORT).show();
                } else {
                    btn.setText(s);
                }
            }
        });
    }

    public static void setResultToEditText(final Context context, final EditText tv, final String s) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (tv == null) {
                    Toast.makeText(context, "tv is null", Toast.LENGTH_SHORT).show();
                } else {
                    tv.setText(s);
                }
            }
        });
    }

    public static boolean checkGpsCoordinate(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    public static double Radian(double x){
        return  x * Math.PI / 180.0;
    }

    public static double Degree(double x){
        return  x * 180 / Math.PI ;
    }

    public static double cosForDegree(double degree) {
        return Math.cos(degree * Math.PI / 180.0f);
    }

    public static double calcLongitudeOffset(double latitude) {
        return ONE_METER_OFFSET / cosForDegree(latitude);
    }

    public static void addLineToSB(StringBuffer sb, String name, Object value) {
        if (sb == null) {
            return;
        }
        sb.
                append(name == null ? "" : name + ": ").
                append(value == null ? "" : value + "").
                append("\n");
    }




    private static double EARTH_RADIUS = 6378.137;//地球半径
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS * 1000;
        //s = Math.round(s * 10000) / 10000;
        return s;
    }

}
