package com.dji.ux.sample.base;

import android.content.Context;
import android.content.SharedPreferences;

import com.dji.ux.sample.MApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * $activityName
 *
 * @author LiuTao
 * @date 2019/2/20/020
 */


public class SPManager {

    public static String SP_MAIN_FLAG = "SP_APP_MAIN";


    public static void saveBoolean(String name, String key, boolean value) {
        try {
            SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(key, value);
            editor.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void saveInt(String name, String key, int value) {
        try {
            SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(key, value);
            editor.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void saveLong(String name, String key, Long value) {
        try {
            SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(key, value);
            editor.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void saveLong(String name, String key, long value) {
        try {
            SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(key, value);
            editor.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void saveString(String name, String key, String value) {
        try {
            SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, value);
            editor.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean getBoolean(String name, String key, boolean defaultValue) {
        SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
        return sp != null && sp.getBoolean(key, defaultValue);
    }

    public static int getInt(String name, String key, int defaultValue) {
        SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
        if (sp != null) {
            return sp.getInt(key, defaultValue);
        }
        return -1;
    }

    public static long getLong(String name, String key, long defaultValue) {
        SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
        if (sp != null) {
            return sp.getLong(key, defaultValue);
        }
        return -1;
    }

    public static Long getLong(String name, String key) {
        SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
        if (sp != null) {
            return sp.getLong(key, -1);
        }
        return null;
    }

    public static String getString(String name, String key, String defaultValue) {
        SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
        if (sp != null) {
            return sp.getString(key, defaultValue);
        }
        return "";
    }


    public static void writeObject(String name, String key, Object obj) {
        try {
            SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, 0);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            String objBase64 = new String(baos.toByteArray());
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, objBase64);
            editor.apply();
        } catch (Exception e) {
        }
    }

    public static Object readObject(String name, String key) {
        try {
            String value = getString(name, key, "");
            ByteArrayInputStream bais = new ByteArrayInputStream(value.getBytes());
            ObjectInputStream bis = new ObjectInputStream(bais);
            return bis.readObject();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 清除配置表
     *
     * @param name 配置文件名
     */
    public static void removeSP(String name) {
        SharedPreferences sp = MApplication.getInstance().getSharedPreferences(name, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}
