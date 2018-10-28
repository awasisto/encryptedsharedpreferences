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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static java.lang.Thread.sleep;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class EncryptedSharedPreferencesTest {

    @Test
    public void putAndGetString() throws Exception {
        String encryptedSharedPreferencesName = "putAndGetString";
        String key = "foo";
        String value = "i hate it when hitler steals my nutella";

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putString(key, value).apply();

        assertEquals(value, encryptedSharedPreferences.getString(key, null));
    }

    @Test
    public void putAndGetStringSet() throws Exception {
        String encryptedSharedPreferencesName = "putAndGetStringSet";
        String key = "foo";
        Set<String> values = new HashSet<String>() {{
            add("potato");
            add("unicorn");
            add("rubber");
        }};

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putStringSet(key, values).apply();

        assertEquals(values, encryptedSharedPreferences.getStringSet(key,
                null));
    }

    @Test
    public void putAndGetInt() throws Exception {
        String encryptedSharedPreferencesName = "putAndGetInt";
        String key = "foo";
        int value = -110883086;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putInt(key, value).apply();

        assertEquals(value, encryptedSharedPreferences.getInt(key, 0));
    }

    @Test
    public void putAndGetLong() throws Exception {
        String encryptedSharedPreferencesName = "putAndGetLong";
        String key = "foo";
        long value = 836613320883456075L;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putLong(key, value).apply();

        assertEquals(value, encryptedSharedPreferences.getLong(key, 0L));
    }

    @Test
    public void putAndGetFloat() throws Exception {
        String encryptedSharedPreferencesName = "putAndGetFloat";
        String key = "foo";
        float value = 9.61f;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putFloat(key, value).apply();

        assertEquals(value, encryptedSharedPreferences.getFloat(key, 0f), 0f);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void putAndGetBoolean() throws Exception {
        String encryptedSharedPreferencesName = "putAndGetBoolean";
        String key = "foo";
        boolean value = true;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putBoolean(key, value).apply();

        assertEquals(value, encryptedSharedPreferences.getBoolean(key, false));
    }

    @Test(expected = ClassCastException.class)
    public void getString_wrongType() throws Exception {
        String encryptedSharedPreferencesName = "getString_wrongType";
        String key = "foo";
        int value = -110883086;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putLong(key, value).apply();
        encryptedSharedPreferences.getString(key, null);
    }

    @Test(expected = ClassCastException.class)
    public void getStringSet_wrongType() throws Exception {
        String encryptedSharedPreferencesName = "getStringSet_wrongType";
        String key = "foo";
        int value = -110883086;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putInt(key, value).apply();
        encryptedSharedPreferences.getStringSet(key, null);
    }

    @Test(expected = ClassCastException.class)
    public void getInt_wrongType() throws Exception {
        String encryptedSharedPreferencesName = "getInt_wrongType";
        String key = "foo";
        long value = 836613320883456075L;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putLong(key, value).apply();
        encryptedSharedPreferences.getInt(key, 0);
    }

    @Test(expected = ClassCastException.class)
    public void getLong_wrongType() throws Exception {
        String encryptedSharedPreferencesName = "getLong_wrongType";
        String key = "foo";
        int value = -110883086;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putInt(key, value).apply();
        encryptedSharedPreferences.getLong(key, 0L);
    }

    @Test(expected = ClassCastException.class)
    public void getFloat_wrongType() throws Exception {
        String encryptedSharedPreferencesName = "getFloat_wrongType";
        String key = "foo";
        int value = -110883086;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putInt(key, value).apply();
        encryptedSharedPreferences.getFloat(key, 0f);
    }

    @Test(expected = ClassCastException.class)
    public void getBoolean_wrongType() throws Exception {
        String encryptedSharedPreferencesName = "getBoolean_wrongType";
        String key = "foo";
        int value = -110883086;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.edit().putInt(key, value).apply();
        encryptedSharedPreferences.getBoolean(key, false);
    }

    @Test
    public void remove() throws Exception {
        String encryptedSharedPreferencesName = "remove";
        String key = "foo";
        int value = -110883086;
        String defaultValue = "muffin";

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        SharedPreferences.Editor encryptedSharedPreferencesEditor =
                encryptedSharedPreferences.edit();
        encryptedSharedPreferencesEditor.putInt(key, value).apply();
        encryptedSharedPreferencesEditor.remove(key).apply();

        assertEquals(defaultValue, encryptedSharedPreferences.getString(key, defaultValue));
    }

    @Test
    public void clear() throws Exception {
        String encryptedSharedPreferencesName = "clear";
        String key = "foo";
        int value = -110883086;
        int defaultValue = 174050977;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        SharedPreferences.Editor encryptedSharedPreferencesEditor =
                encryptedSharedPreferences.edit();
        encryptedSharedPreferencesEditor.putInt(key, value).apply();
        encryptedSharedPreferencesEditor.clear().apply();

        assertEquals(defaultValue, encryptedSharedPreferences.getInt(key, defaultValue));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void getAll() throws Exception {
        String encryptedSharedPreferencesName = "getAll";
        String stringKey = "foo";
        String stringSetKey = "bar";
        String intKey = "baz";
        String longKey = "qux";
        String floatKey = "quux";
        String booleanKey = "quuz";
        String stringValue = "i hate it when hitler steals my nutella";
        Set<String> stringSetValues = new HashSet<String>() {{
            add("potato");
            add("unicorn");
            add("rubber");
        }};
        int intValue = -110883086;
        long longValue = 836613320883456075L;
        float floatValue = 9.61f;
        boolean booleanValue = true;

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        SharedPreferences.Editor encryptedSharedPreferencesEditor =
                encryptedSharedPreferences.edit();
        encryptedSharedPreferencesEditor.putString(stringKey, stringValue);
        encryptedSharedPreferencesEditor.putStringSet(stringSetKey, stringSetValues);
        encryptedSharedPreferencesEditor.putInt(intKey, intValue);
        encryptedSharedPreferencesEditor.putLong(longKey, longValue);
        encryptedSharedPreferencesEditor.putFloat(floatKey, floatValue);
        encryptedSharedPreferencesEditor.putBoolean(booleanKey, booleanValue);
        encryptedSharedPreferencesEditor.apply();

        Map<String, ?> preferences = encryptedSharedPreferences.getAll();

        assertEquals(stringValue, preferences.get(stringKey));
        assertTrue(preferences.get(stringSetKey) instanceof Set);
        for (String stringSetValue : stringSetValues) {
            assertTrue(((Set) preferences.get(stringSetKey)).contains(stringSetValue));
        }
        assertEquals(intValue, (int) (Integer) preferences.get(intKey));
        assertEquals(longValue, (long) (Long) preferences.get(longKey));
        assertEquals(floatValue, (Float) preferences.get(floatKey), 0f);
        assertEquals(booleanValue, preferences.get(booleanKey));
    }

    @Test
    public void registerOnSharedPreferenceChangeListener() throws Exception {
        String encryptedSharedPreferencesName = "registerOnSharedPreferenceChangeListener";
        String key = "foo";
        int value = -110883086;

        OnSharedPreferenceChangeListener onSharedPreferenceChangeListenerMock =
                mock(OnSharedPreferenceChangeListener.class);

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.registerOnSharedPreferenceChangeListener(
                onSharedPreferenceChangeListenerMock);
        encryptedSharedPreferences.edit().putInt(key, value).apply();

        verify(onSharedPreferenceChangeListenerMock, timeout(1000)).onSharedPreferenceChanged(
                eq(encryptedSharedPreferences), eq(key));
    }

    @Test
    public void unregisterOnSharedPreferenceChangeListener() throws Exception {
        String encryptedSharedPreferencesName = "unregisterOnSharedPreferenceChangeListener";
        String key = "foo";
        int value = -110883086;

        OnSharedPreferenceChangeListener listenerMock =
                mock(OnSharedPreferenceChangeListener.class);

        SharedPreferences encryptedSharedPreferences =
                EncryptedSharedPreferences.getEncryptedSharedPreferences(getTargetContext(),
                        encryptedSharedPreferencesName);

        encryptedSharedPreferences.registerOnSharedPreferenceChangeListener(listenerMock);
        encryptedSharedPreferences.unregisterOnSharedPreferenceChangeListener(listenerMock);
        encryptedSharedPreferences.edit().putInt(key, value).apply();

        sleep(1000);

        verify(listenerMock, never()).onSharedPreferenceChanged(any(), any());
    }
}