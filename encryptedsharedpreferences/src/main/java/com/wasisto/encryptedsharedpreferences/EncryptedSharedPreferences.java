/*
 * Copyright 2018 Andika Wasisto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wasisto.encryptedsharedpreferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.wasisto.androidkeystoreencryption.EncryptionService;
import com.wasisto.androidkeystoreencryption.exception.EncryptionKeyLostException;
import com.wasisto.androidkeystoreencryption.model.EncryptedDataAndIv;
import com.wasisto.encryptedsharedpreferences.exception.PreferencesLostException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static android.util.Base64.DEFAULT;

/**
 * The encrypted SharedPreferences implementation.
 */
public class EncryptedSharedPreferences implements SharedPreferences {

    private static final String ENCRYPTED_SHARED_PREFERENCES_NAME_SUFFIX = ".esp";

    private static final String CONTEXT_DEFAULT_ENCRYPTED_SHARED_PREFERENCES_NAME_SUFFIX =
            "_preferences";

    private static final String ESP_SHARED_PREFERENCES_NAME =
            "com.wasisto.encryptedsharedpreferences";

    private static final String PREFERENCE_ENCRYPTED_SHARED_PREFERENCES_NAMES =
            "encryptedSharedPreferencesNames";

    private static final String TYPE = "type";
    private static final String ENCRYPTED_DATA = "encrypted_data";
    private static final String IV = "iv";

    private EncryptionService encryptionService;

    private SharedPreferences sharedPreferences;

    private Map<Object, OnSharedPreferenceChangeListener> listeners = new HashMap<>();

    private EncryptedSharedPreferences(Context context, String encryptedPreferencesName)
            throws PreferencesLostException {
        try {
            encryptionService = EncryptionService.getInstance(context);
            sharedPreferences = context.getSharedPreferences(encryptedPreferencesName +
                    ENCRYPTED_SHARED_PREFERENCES_NAME_SUFFIX, MODE_PRIVATE);

            SharedPreferences espSharedPreferences = context.getSharedPreferences(
                    ESP_SHARED_PREFERENCES_NAME, MODE_PRIVATE);

            Set<String> encryptedSharedPreferencesNames = espSharedPreferences.getStringSet(
                    PREFERENCE_ENCRYPTED_SHARED_PREFERENCES_NAMES, new HashSet<>());

            encryptedSharedPreferencesNames.add(encryptedPreferencesName);

            espSharedPreferences.edit().putStringSet(PREFERENCE_ENCRYPTED_SHARED_PREFERENCES_NAMES,
                    encryptedSharedPreferencesNames).apply();
        } catch (EncryptionKeyLostException e) {
            throw new PreferencesLostException(e);
        }
    }

    /**
     * Returns an EncryptedSharedPreferences with the specified name. Equivalent to
     * {@link Context#getSharedPreferences(String, int)}.
     *
     * @param context The context of the preferences whose values are wanted.
     *
     * @param name The preferences name.
     *
     * @return An EncryptedSharedPreferences instance that can be used to retrieve and listen to
     * values of the preferences.
     *
     * @throws PreferencesLostException if the encryption key is lost.
     */
    public static EncryptedSharedPreferences getEncryptedSharedPreferences(Context context,
                                                                           String name)
            throws PreferencesLostException {
        return new EncryptedSharedPreferences(context, name);
    }

    /**
     * Asynchronously returns an EncryptedSharedPreferences with the specified name. Equivalent to
     * {@link Context#getSharedPreferences(String, int)}.
     *
     * @param context The context of the preferences whose values are wanted.
     *
     * @param name The preferences name.
     *
     * @param callback The callback.
     */
    public static void getEncryptedSharedPreferencesAsync(Context context,
                                                          String name,
                                                          GetEncryptedSharedPreferencesAsyncCallback callback) {
        Handler handler = new Handler(Looper.myLooper() != null ? Looper.myLooper() :
                Looper.getMainLooper());

        new Thread(() -> {
            try {
                EncryptedSharedPreferences encryptedSharedPreferences =
                        getEncryptedSharedPreferences(context, name);
                handler.post(() -> callback.onSuccess(encryptedSharedPreferences));
            } catch (Throwable t) {
                if (t instanceof PreferencesLostException) {
                    callback.onPreferencesLost((PreferencesLostException) t);
                } else {
                    callback.onError(t);
                }
            }
        }).start();
    }

    /**
     * Returns an EncryptedSharedPreferences that is private to the specified activity. Equivalent
     * to {@link Activity#getPreferences(int)}.
     *
     * @param activity The activity of the preferences whose values are wanted.
     *
     * @return An EncryptedSharedPreferences instance that can be used to retrieve and listen to
     * values of the preferences.
     *
     * @throws PreferencesLostException if the encryption key is lost.
     */
    public static EncryptedSharedPreferences getActivityEncryptedPreferences(Activity activity)
            throws PreferencesLostException {
        return getEncryptedSharedPreferences(activity, activity.getLocalClassName());
    }

    /**
     * Asynchronously returns an EncryptedSharedPreferences that is private to the specified
     * activity. Equivalent to {@link Activity#getPreferences(int)}.
     *
     * @param activity The activity of the preferences whose values are wanted.
     *
     * @param callback The callback.
     */
    public static void getActivityEncryptedPreferencesAsync(Activity activity,
                                                            GetEncryptedSharedPreferencesAsyncCallback callback) {
        Handler handler = new Handler(Looper.myLooper() != null ? Looper.myLooper() :
                Looper.getMainLooper());

        new Thread(() -> {
            try {
                EncryptedSharedPreferences activityEncryptedPreferences =
                        getActivityEncryptedPreferences(activity);
                handler.post(() -> callback.onSuccess(activityEncryptedPreferences));
            } catch (Throwable t) {
                if (t instanceof PreferencesLostException) {
                    callback.onPreferencesLost((PreferencesLostException) t);
                } else {
                    callback.onError(t);
                }
            }
        }).start();
    }

    /**
     * Returns the default EncryptedSharedPreferences of the specified context. Equivalent to
     * {@link PreferenceManager#getDefaultSharedPreferences(Context)}.
     *
     * @param context The context of the preferences whose values are wanted.
     *
     * @return An EncryptedSharedPreferences instance that can be used to retrieve and listen to
     * values of the preferences.
     *
     * @throws PreferencesLostException if the encryption key is lost.
     */
    public static EncryptedSharedPreferences getContextDefaultEncryptedSharedPreferences(Context context)
            throws PreferencesLostException {
        return getEncryptedSharedPreferences(context, context.getPackageName() +
                        CONTEXT_DEFAULT_ENCRYPTED_SHARED_PREFERENCES_NAME_SUFFIX);
    }

    /**
     * Asynchronously returns the default EncryptedSharedPreferences of the specified context.
     * Equivalent to {@link PreferenceManager#getDefaultSharedPreferences(Context)}.
     *
     * @param context The context of the preferences whose values are wanted.
     *
     * @param callback The callback.
     */
    public static void getContextDefaultEncryptedSharedPreferencesAsync(Context context,
                                                                        GetEncryptedSharedPreferencesAsyncCallback callback) {
        Handler handler = new Handler(Looper.myLooper() != null ? Looper.myLooper() :
                Looper.getMainLooper());

        new Thread(() -> {
            try {
                EncryptedSharedPreferences contextDefaultEncryptedSharedPreferences =
                        getContextDefaultEncryptedSharedPreferences(context);
                handler.post(() -> callback.onSuccess(contextDefaultEncryptedSharedPreferences));
            } catch (Throwable t) {
                if (t instanceof PreferencesLostException) {
                    callback.onPreferencesLost((PreferencesLostException) t);
                } else {
                    callback.onError(t);
                }
            }
        }).start();
    }

    /**
     * Resets the encryption key and clear all EncryptedSharedPreferences.
     *
     * @param context The context.
     */
    public static synchronized void resetEncryptedSharedPreferences(Context context) {
        EncryptionService.resetEncryptionKey(context);

        SharedPreferences espSharedPreferences = context.getSharedPreferences(
                ESP_SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        Set<String> encryptedSharedPreferencesNames = espSharedPreferences.getStringSet(
                PREFERENCE_ENCRYPTED_SHARED_PREFERENCES_NAMES, null);
        if (encryptedSharedPreferencesNames != null) {
            for (String encryptedSharedPreferencesName : encryptedSharedPreferencesNames) {
                context.getSharedPreferences(encryptedSharedPreferencesName +
                        ENCRYPTED_SHARED_PREFERENCES_NAME_SUFFIX, MODE_PRIVATE).edit().clear()
                        .apply();
            }
            espSharedPreferences.edit().clear().apply();
        }
    }

    /**
     * Asynchronously resets the encryption key and clear all EncryptedSharedPreferences.
     *
     * @param context The context.
     * @param callback The callback.
     */
    public static void resetEncryptedSharedPreferencesAsync(Context context,
                                                            ResetEncryptedSharedPreferencesAsyncCallback callback) {
        Handler handler = new Handler(Looper.myLooper() != null ? Looper.myLooper() :
                Looper.getMainLooper());

        new Thread(() -> {
            try {
                resetEncryptedSharedPreferences(context);
                handler.post(callback::onSuccess);
            } catch (Throwable t) {
                handler.post(() -> callback.onError(t));
            }
        }).start();
    }

    private static EncryptedDataAndIv createEncryptedDataAndIv(JSONObject encryptedValueJsonObject) {
        try {
            EncryptedDataAndIv encryptedDataAndIv = new EncryptedDataAndIv();
            encryptedDataAndIv.setEncryptedData(Base64.decode(encryptedValueJsonObject.getString(
                    ENCRYPTED_DATA), DEFAULT));
            encryptedDataAndIv.setIv(Base64.decode(encryptedValueJsonObject.getString(IV),
                    DEFAULT));
            return encryptedDataAndIv;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean parseBoolean(int intValue) {
        if (intValue == 1) {
            return true;
        } else if (intValue == 0) {
            return false;
        } else {
            throw new RuntimeException("Failed to parse boolean value. intValue: " + intValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ?> getAll() {
        Map<String, ?> encryptedPreferences = sharedPreferences.getAll();
        Map<String, Object> decryptedPreferences = new HashMap<>(encryptedPreferences.size());
        for (Map.Entry<String, ?> encryptedPreference : encryptedPreferences.entrySet()) {
            try {
                Object encryptedPreferenceValue = encryptedPreference.getValue();
                if (encryptedPreferenceValue instanceof Set<?>) {
                    Set<String> decryptedSetValues = new HashSet<>();
                    Set<String> encryptedSetValueJsonSet = (Set<String>) encryptedPreferenceValue;
                    for (String encryptedSetValueJson : encryptedSetValueJsonSet) {
                        JSONObject encryptedValueJsonObject = new JSONObject(encryptedSetValueJson);
                        String valueType = encryptedValueJsonObject.getString(TYPE);
                        if (!valueType.equals(String.class.getCanonicalName())) {
                            throw new ClassCastException(valueType + " cannot be cast to " +
                                    String.class.getCanonicalName());
                        }
                        EncryptedDataAndIv encryptedDataAndIv = createEncryptedDataAndIv(
                                encryptedValueJsonObject);
                        String decryptedValue = encryptionService.decryptString(
                                encryptedDataAndIv);
                        decryptedSetValues.add(decryptedValue);
                    }
                    decryptedPreferences.put(encryptedPreference.getKey(), decryptedSetValues);
                } else {
                    String encryptedValueJson = (String) encryptedPreference.getValue();
                    JSONObject encryptedValueJsonObject = new JSONObject(encryptedValueJson);
                    EncryptedDataAndIv encryptedDataAndIv = createEncryptedDataAndIv(
                            encryptedValueJsonObject);
                    String valueType = encryptedValueJsonObject.getString(TYPE);
                    if (valueType.equals(String.class.getCanonicalName())) {
                        String decryptedValue = encryptionService.decryptString(
                                encryptedDataAndIv);
                        decryptedPreferences.put(encryptedPreference.getKey(), decryptedValue);
                    } else if (valueType.equals(Integer.class.getCanonicalName())) {
                        int decryptedValue = encryptionService.decryptInt(encryptedDataAndIv);
                        decryptedPreferences.put(encryptedPreference.getKey(), decryptedValue);
                    } else if (valueType.equals(Long.class.getCanonicalName())) {
                        long decryptedValue = encryptionService.decryptLong(encryptedDataAndIv);
                        decryptedPreferences.put(encryptedPreference.getKey(), decryptedValue);
                    } else if (valueType.equals(Float.class.getCanonicalName())) {
                        float decryptedValue = encryptionService.decryptFloat(encryptedDataAndIv);
                        decryptedPreferences.put(encryptedPreference.getKey(), decryptedValue);
                    } else if (valueType.equals(Boolean.class.getCanonicalName())) {
                        int decryptedIntValue = encryptionService.decryptInt(encryptedDataAndIv);
                        boolean decryptedValue = parseBoolean(decryptedIntValue);
                        decryptedPreferences.put(encryptedPreference.getKey(), decryptedValue);
                    } else {
                        throw new RuntimeException("Unsupported value type. valueType: " +
                                valueType);
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return decryptedPreferences;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        String encryptedValueJson = sharedPreferences.getString(key, null);
        if (encryptedValueJson != null) {
            try {
                JSONObject encryptedValueJsonObject = new JSONObject(encryptedValueJson);
                String valueType = encryptedValueJsonObject.getString(TYPE);
                if (!valueType.equals(String.class.getCanonicalName())) {
                    throw new ClassCastException(valueType + " cannot be cast to " +
                            String.class.getCanonicalName());
                }
                EncryptedDataAndIv encryptedDataAndIv = createEncryptedDataAndIv(
                        encryptedValueJsonObject);
                return encryptionService.decryptString(encryptedDataAndIv);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return defValue;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        Set<String> decryptedValues = new HashSet<>();
        Set<String> encryptedValueJsonSet = sharedPreferences.getStringSet(key, null);
        if (encryptedValueJsonSet != null) {
            for (String encryptedValueJson : encryptedValueJsonSet) {
                try {
                    JSONObject encryptedValueJsonObject = new JSONObject(encryptedValueJson);
                    String valueType = encryptedValueJsonObject.getString(TYPE);
                    if (!valueType.equals(String.class.getCanonicalName())) {
                        throw new ClassCastException(valueType + " cannot be cast to " +
                                String.class.getCanonicalName());
                    }
                    EncryptedDataAndIv encryptedDataAndIv = createEncryptedDataAndIv(
                            encryptedValueJsonObject);
                    String decryptedValue = encryptionService.decryptString(encryptedDataAndIv);
                    decryptedValues.add(decryptedValue);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            return decryptedValues;
        }
        return defValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(String key, int defValue) {
        String encryptedValueJson = sharedPreferences.getString(key, null);
        if (encryptedValueJson != null) {
            try {
                JSONObject encryptedValueJsonObject = new JSONObject(encryptedValueJson);
                String valueType = encryptedValueJsonObject.getString(TYPE);
                if (!valueType.equals(Integer.class.getCanonicalName())) {
                    throw new ClassCastException(valueType + " cannot be cast to " +
                            Integer.class.getCanonicalName());
                }
                EncryptedDataAndIv encryptedDataAndIv = createEncryptedDataAndIv(
                        encryptedValueJsonObject);
                return encryptionService.decryptInt(encryptedDataAndIv);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return defValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLong(String key, long defValue) {
        String encryptedValueJson = sharedPreferences.getString(key, null);
        if (encryptedValueJson != null) {
            try {
                JSONObject encryptedValueJsonObject = new JSONObject(encryptedValueJson);
                String valueType = encryptedValueJsonObject.getString(TYPE);
                if (!valueType.equals(Long.class.getCanonicalName())) {
                    throw new ClassCastException(valueType + " cannot be cast to " +
                            Long.class.getCanonicalName());
                }
                EncryptedDataAndIv encryptedDataAndIv = createEncryptedDataAndIv(
                        encryptedValueJsonObject);
                return encryptionService.decryptLong(encryptedDataAndIv);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return defValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloat(String key, float defValue) {
        String encryptedValueJson = sharedPreferences.getString(key, null);
        if (encryptedValueJson != null) {
            try {
                JSONObject encryptedValueJsonObject = new JSONObject(encryptedValueJson);
                String valueType = encryptedValueJsonObject.getString(TYPE);
                if (!valueType.equals(Float.class.getCanonicalName())) {
                    throw new ClassCastException(valueType + " cannot be cast to " +
                            Float.class.getCanonicalName());
                }
                EncryptedDataAndIv encryptedDataAndIv = createEncryptedDataAndIv(
                        encryptedValueJsonObject);
                return encryptionService.decryptFloat(encryptedDataAndIv);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return defValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBoolean(String key, boolean defValue) {
        String encryptedValueJson = sharedPreferences.getString(key, null);
        if (encryptedValueJson != null) {
            try {
                JSONObject encryptedValueJsonObject = new JSONObject(encryptedValueJson);
                String valueType = encryptedValueJsonObject.getString(TYPE);
                if (!valueType.equals(Boolean.class.getCanonicalName())) {
                    throw new ClassCastException(valueType + " cannot be cast to " +
                            Boolean.class.getCanonicalName());
                }
                EncryptedDataAndIv encryptedDataAndIv = createEncryptedDataAndIv(
                        encryptedValueJsonObject);
                int decryptedIntValue = encryptionService.decryptInt(encryptedDataAndIv);
                return parseBoolean(decryptedIntValue);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return defValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Editor edit() {
        return new Editor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        if (listener != null && !listeners.containsKey(listener)) {
            OnSharedPreferenceChangeListener customListener = (sharedPreferences, key) ->
                    listener.onSharedPreferenceChanged(
                            EncryptedSharedPreferences.this, key);
            listeners.put(listener, customListener);
            sharedPreferences.registerOnSharedPreferenceChangeListener(customListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listeners.get(listener));
        listeners.remove(listener);
    }

    private class Editor implements SharedPreferences.Editor {

        private SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        private String createEncryptedValueJson(EncryptedDataAndIv encryptedDataAndIv,
                                                Class<?> dataClass) {
            try {
                JSONObject encryptedValueJsonObject = new JSONObject();
                encryptedValueJsonObject.put(TYPE, dataClass.getCanonicalName());
                encryptedValueJsonObject.put(ENCRYPTED_DATA, Base64.encodeToString(
                        encryptedDataAndIv.getEncryptedData(), DEFAULT));
                encryptedValueJsonObject.put(IV, Base64.encodeToString(encryptedDataAndIv.getIv(),
                        DEFAULT));
                return encryptedValueJsonObject.toString();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SharedPreferences.Editor putString(String key, @Nullable String value) {
            if (value != null) {
                EncryptedDataAndIv encryptedDataAndIv = encryptionService.encrypt(value);
                String encryptedValueJson = createEncryptedValueJson(encryptedDataAndIv,
                        String.class);
                sharedPreferencesEditor.putString(key, encryptedValueJson);
            } else {
                sharedPreferencesEditor.putString(key, null);
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SharedPreferences.Editor putStringSet(String key, @Nullable Set<String> values) {
            if (values != null) {
                Set<String> encryptedValueJsonSet = new HashSet<>(values.size());
                for (String value : values) {
                    EncryptedDataAndIv encryptedDataAndIv = encryptionService.encrypt(value);
                    String encryptedValueJson = createEncryptedValueJson(encryptedDataAndIv,
                            String.class);
                    encryptedValueJsonSet.add(encryptedValueJson);
                }
                sharedPreferencesEditor.putStringSet(key, encryptedValueJsonSet);
            } else {
                sharedPreferencesEditor.putStringSet(key, null);
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            EncryptedDataAndIv encryptedDataAndIv = encryptionService.encrypt(value);
            String encryptedValueJson = createEncryptedValueJson(encryptedDataAndIv, Integer.class);
            sharedPreferencesEditor.putString(key, encryptedValueJson);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            EncryptedDataAndIv encryptedDataAndIv = encryptionService.encrypt(value);
            String encryptedValueJson = createEncryptedValueJson(encryptedDataAndIv, Long.class);
            sharedPreferencesEditor.putString(key, encryptedValueJson);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            EncryptedDataAndIv encryptedDataAndIv = encryptionService.encrypt(value);
            String encryptedValueJson = createEncryptedValueJson(encryptedDataAndIv, Float.class);
            sharedPreferencesEditor.putString(key, encryptedValueJson);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            int intValue = value ? 1 : 0;
            EncryptedDataAndIv encryptedDataAndIv = encryptionService.encrypt(intValue);
            String encryptedValueJson = createEncryptedValueJson(encryptedDataAndIv, Boolean.class);
            sharedPreferencesEditor.putString(key, encryptedValueJson);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SharedPreferences.Editor remove(String key) {
            sharedPreferencesEditor.remove(key);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SharedPreferences.Editor clear() {
            sharedPreferencesEditor.clear();
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean commit() {
            return sharedPreferencesEditor.commit();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void apply() {
            sharedPreferencesEditor.apply();
        }
    }

    /**
     * The callback interface for
     * {@link #getEncryptedSharedPreferencesAsync(Context, String, GetEncryptedSharedPreferencesAsyncCallback)},
     * {@link #getActivityEncryptedPreferencesAsync(Activity, GetEncryptedSharedPreferencesAsyncCallback)},
     * and {@link #getContextDefaultEncryptedSharedPreferencesAsync(Context, GetEncryptedSharedPreferencesAsyncCallback)}
     * methods.
     */
    public interface GetEncryptedSharedPreferencesAsyncCallback {

        /**
         * Called if the operation is successful.
         *
         * @param encryptedSharedPreferences that can be used to retrieve and listen to values of
         * the preferences.
         */
        void onSuccess(EncryptedSharedPreferences encryptedSharedPreferences);

        /**
         * Called if the encryption key is lost.
         *
         * @param e The exception.
         */
        void onPreferencesLost(PreferencesLostException e);

        /**
         * Called if an error occurred.
         *
         * @param error The error.
         */
        void onError(Throwable error);
    }

    /**
     * The callback interface for the
     * {@link #resetEncryptedSharedPreferencesAsync(Context, ResetEncryptedSharedPreferencesAsyncCallback)}
     * method.
     */
    public interface ResetEncryptedSharedPreferencesAsyncCallback {

        /**
         * Called if the operation is successful.
         */
        void onSuccess();

        /**
         * Called if an error occurred.
         *
         * @param error The error.
         */
        void onError(Throwable error);
    }
}
