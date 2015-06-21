package com.globalLibrary.util;

import android.content.Context;

import java.util.Map;

import com.tata.trackit.Constants;

public class SharedPreferences {
    private android.content.SharedPreferences sharedPreferences;

    public static SharedPreferences with(Context context) {
        return new SharedPreferences(context, Constants.SHAREDPREFERENCESNAME);
    }

    private SharedPreferences(Context context, String name) {
        sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public String getString(String key, String defaultVal) {
        return sharedPreferences.getString(key, defaultVal);
    }

    public int getInteger(String key, int defVal) {
        return sharedPreferences.getInt(key, defVal);
    }

    public Long getLong(String key, long defVal) {
        return sharedPreferences.getLong(key, defVal);
    }

    public boolean getBoolean(String key, Boolean defVal) {
        return sharedPreferences.getBoolean(key, defVal);
    }
    public void clear() {
        sharedPreferences.edit().clear().commit();
    }

    public void delete(String... keys) {
        if (keys != null && keys.length > 0) {
            android.content.SharedPreferences.Editor edit = sharedPreferences.edit();
            for (String key : keys) {
                edit.remove(key);
            }
            edit.commit();
        }
    }

    public void set(Map<String, Object> map) {
        android.content.SharedPreferences.Editor edit = sharedPreferences.edit();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (Integer.class.isInstance(entry.getValue())) {
                edit.putInt(entry.getKey(), (Integer) entry.getValue());
            } else if (Float.class.isInstance(entry.getValue())) {
                edit.putFloat(entry.getKey(), (Float) entry.getValue());
            } else if (Long.class.isInstance(entry.getValue())) {
                edit.putLong(entry.getKey(), (Long) entry.getValue());
            } else if (Boolean.class.isInstance(entry.getValue())) {
                edit.putBoolean(entry.getKey(), (Boolean) entry.getValue());
            } else {
                edit.putString(entry.getKey(), entry.getValue().toString());
            }
        }
        edit.commit();
    }

    public void setInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).commit();
    }
    public void setLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).commit();
    }

    public void setBoolean(String key, Boolean value) {
        sharedPreferences.edit().putBoolean(key, value).commit();
    }

    public void setString(String key, String value) {
        sharedPreferences.edit().putString(key, value).commit();
    }
}
