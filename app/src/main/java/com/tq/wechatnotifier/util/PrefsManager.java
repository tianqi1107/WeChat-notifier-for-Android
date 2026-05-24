package com.tq.wechatnotifier.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

public class PrefsManager {
    private static final String PREFS_NAME = "wechat_notifier_prefs";
    private static final String KEY_WHITELIST = "whitelist_contacts";
    private static final String KEY_ALL_SEEN = "all_seen_contacts";
    private static final String KEY_SERVICE_ENABLED = "service_enabled";
    private static final String KEY_TIME_ENABLED = "time_period_enabled";
    private static final String KEY_START_HOUR = "start_hour";
    private static final String KEY_START_MINUTE = "start_minute";
    private static final String KEY_END_HOUR = "end_hour";
    private static final String KEY_END_MINUTE = "end_minute";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Whitelist
    public Set<String> getWhitelist() {
        return getStringSet(KEY_WHITELIST);
    }

    public void setWhitelist(Set<String> contacts) {
        setStringSet(KEY_WHITELIST, contacts);
    }

    public void addToWhitelist(String name) {
        Set<String> set = getWhitelist();
        set.add(name);
        setWhitelist(set);
    }

    public void removeFromWhitelist(String name) {
        Set<String> set = getWhitelist();
        set.remove(name);
        setWhitelist(set);
    }

    // All seen contacts (from notification history)
    public Set<String> getAllSeenContacts() {
        return getStringSet(KEY_ALL_SEEN);
    }

    public void addSeenContact(String name) {
        Set<String> set = getAllSeenContacts();
        if (!set.contains(name)) {
            set.add(name);
            setStringSet(KEY_ALL_SEEN, set);
        }
    }

    // Service enabled
    public boolean isServiceEnabled() {
        return prefs.getBoolean(KEY_SERVICE_ENABLED, false);
    }

    public void setServiceEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply();
    }

    // Time period
    public boolean isTimePeriodEnabled() {
        return prefs.getBoolean(KEY_TIME_ENABLED, false);
    }

    public void setTimePeriodEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_TIME_ENABLED, enabled).apply();
    }

    public int getStartHour() {
        return prefs.getInt(KEY_START_HOUR, 22);
    }

    public int getStartMinute() {
        return prefs.getInt(KEY_START_MINUTE, 0);
    }

    public int getEndHour() {
        return prefs.getInt(KEY_END_HOUR, 7);
    }

    public int getEndMinute() {
        return prefs.getInt(KEY_END_MINUTE, 0);
    }

    public void setTimePeriod(int startH, int startM, int endH, int endM) {
        prefs.edit()
                .putInt(KEY_START_HOUR, startH)
                .putInt(KEY_START_MINUTE, startM)
                .putInt(KEY_END_HOUR, endH)
                .putInt(KEY_END_MINUTE, endM)
                .apply();
    }

    // JSON-based String set storage (avoids StringSet serialization quirks)
    private Set<String> getStringSet(String key) {
        String json = prefs.getString(key, "[]");
        Set<String> result = new HashSet<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                result.add(arr.getString(i));
            }
        } catch (JSONException e) {
            // ignore, return empty set
        }
        return result;
    }

    private void setStringSet(String key, Set<String> set) {
        JSONArray arr = new JSONArray();
        for (String s : set) {
            arr.put(s);
        }
        prefs.edit().putString(key, arr.toString()).apply();
    }
}
