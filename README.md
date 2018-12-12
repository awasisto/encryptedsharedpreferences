EncryptedSharedPreferences
==========================

An Android SharedPreferences implementation that encrypts the preference values using AES-256
algorithm and securely store the encryption key using the Android KeyStore system.

Usage
-----

**Synchronous instance creation**

```java
try {
    // Equivalent to Context#getSharedPreferences(String) method
    SharedPreferences encryptedSharedPreferences =
            EncryptedSharedPreferences.getEncryptedSharedPreferences(context,
                    "encryptedPreferencesName");

    // Equivalent to Activity#getPreferences() method
    SharedPreferences activityEncryptedPreferences =
            EncryptedSharedPreferences.getActivityEncryptedPreferences(activity);

    // Equivalent to PreferenceManager#getDefaultSharedPreferences(Context) method
    SharedPreferences contextDefaultEncryptedSharedPreferences =
            EncryptedSharedPreferences.getContextDefaultEncryptedSharedPreferences(context);
} catch (PreferencesLostException e) {
    // you might want to reset the EncryptedSharedPreferences (you will not be able to retrieve the
    // previously saved preferences!)
    EncryptedSharedPreferences.resetEncryptedSharedPreferences(context);

    // and then do something useful
}
```

**Asynchronous instance creation**

```java
EncryptedSharedPreferences.getEncryptedSharedPreferencesAsync(context, "encryptedPreferencesName",
        new GetEncryptedSharedPreferencesAsyncCallback() {
            @Override
            public void onSuccess(EncryptedSharedPreferences encryptedSharedPreferences) {
                mEncryptedSharedPreferences = encryptedSharedPreferences;
            }

            @Override
            public void onPreferencesLost(PreferencesLostException e) {
                // you might want to reset the EncryptedSharedPreferences (you will not be able to
                // retrieve the previously saved preferences!)
                EncryptedSharedPreferences.resetEncryptedSharedPreferencesAsync(context,
                        new ResetEncryptedSharedPreferencesAsyncCallback() {
                            @Override
                            public void onSuccess() {
                                // and then do something useful
                            }

                            @Override
                            public void onError(Throwable error) {
                                // do something useful
                            }
                        });
            }

            @Override
            public void onError(Throwable error) {
                // do something useful
            }
        });

// getActivityEncryptedPreferencesAsync() and getContextDefaultEncryptedSharedPreferencesAsync()
// methods are also available
```

How it works
------------

Android SharedPreferences implementation stores preferences in
`/data/data/APP_PACKAGE_NAME/shared_prefs/PREFERENCES_NAME.xml` in plain text. For example:

```xml
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="username">john.smith</string>
    <string name="password">secret</string>
</map>
```

This implementation of SharedPreferences stores the preferences in
`/data/data/APP_PACKAGE_NAME/shared_prefs/PREFERENCES_NAME.esp.xml` in encrypted format. For
example:

```xml
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="username">{"type":"java.lang.String","encrypted_data":"BmApN75DMMAb\/xP6DBIvqA==\n","iv":"lW5GDGF5pNibgrDS6V0rKw==\n"}</string>
    <string name="password">{"type":"java.lang.String","encrypted_data":"WAC1z93PFMIP3ZgT1of6UQ==\n","iv":"zrmihzs19lKZ1AIwTu2SOA==\n"}</string>
</map>
```

API <21 Issue
-------------

Prior API 21, an Android bug may cause the Android Keystore system to lose the encryption key when
the device lock screen is changed. Read about it here:
[Android Security: The Forgetful Keystore](https://doridori.github.io/android-security-the-forgetful-keystore/#sthash.2oefHeqm.dpbs).

If you are going to use this library on API <21, make sure you use it only to encrypt the
recoverable data such as password, API token, etc.

Download
--------

Download via Gradle:

    implementation 'com.wasisto.encryptedsharedpreferences:encryptedsharedpreferences:2.0.3'
    implementation 'com.wasisto.androidkeystoreencryption:androidkeystoreencryption:1.1.4'

License
-------

    Copyright 2018 Andika Wasisto

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.