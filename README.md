# NFCREAD SDK SAMPLE PROJECT!

NFC Read is a tool designed for reading and verifying the official documents such as identity cards or passports. An example use case can be a police officer performing ID checks on the street, where NFC Read can be used with ease via an Android or an IOS smartphone to scan and verify the presented official document. The application does not require any specialised equipment or additional training.

- There is no additional training required for the personal to detect the fraudulent identity cards.

- It is a mobile solution that does not require any specialised hardware.

- No manual data entries are required: NFC Read automatically creates entries for data without any errors.

- A face match is automatically performed by obtaining the high resolution biometric image stored inside the NFC chip of the identity document

## Installation of the SDK

## 1: Add the Jitpack repository to your build.gradle file (root/build.gradle)

```groovy
buildscript {
    repositories {
        mavenCentral()
        ...
     }
     ...
}

allprojects {
        repositories {
            ...
            mavenCentral()
            maven {
                url 'https://jitpack.io'
                credentials { username authToken }
            }
        }
    }
```

## 2: Include the Jitpack token to your gradle.properties (root/gradle.properties)

```xml
    authToken=jp_rq9s6sa9v1pop0jrovb9ird10k
```

## 3: Add the dependencies to your app's build.gradle file (app/build.gradle)

```groovy
android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
dependencies {
    ...
    implementation 'org.bitbucket.sanalogi:sanalogireaderandroid:1.0.15'
}
```

After performing this step, run Gradle Sync to let Android Studio download the the SDK from the Jitpack repository.

## 4: Include the following to Android Manifest file

```xml
<application
         ...
        android:requestLegacyExternalStorage="true"
        android:usesCleartextTraffic="true">

        <meta-data android:name="com.sanalogi.cameralibrary" android:value="@string/apiKey" />

        <activity
            ...
        </activity>
    </application>
```

## 5: Include the provided APIKEY to your strings.xml file: (res/values/strings.xml)

```xml
    <resources>
        ...
        <string name="apiKey">Your API key goes here</string>
    </resources>
```

## Getting started with the SDK

** Include the following LinearLayout to your activity's XML file, in this example it is activity_main.xml **

```xml
            <LinearLayout
             android:id="@+id/readerLayout"
             android:layout_width="match_parent"
             android:layout_height="match_parent"
             android:background="@android:color/black"
             android:orientation="vertical" />
```

** Add the following code to your activity code, in this example it is MainActivity.java **

```java

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sanalogi.cameralibrary.CardType;
import com.sanalogi.cameralibrary.Engine;
import com.sanalogi.cameralibrary.IEngineCallback;
import com.sanalogi.cameralibrary.PassportModel;
import com.sanalogi.cameralibrary.Reader;
import com.sanalogi.cameralibrary.SDKModel;
import com.sanalogi.cameralibrary.ScanResultInterface;
import com.sanalogi.cameralibrary.nfc.NfcConnection;
import com.sanalogi.cameralibrary.nfc.NfcScanResultInterface;

//ScanResultInterface and NfcScanResultInterface should be implemented to obtain data from OCR and NFC scans.
public class MainActivity extends AppCompatActivity implements ScanResultInterface, NfcScanResultInterface {

    Handler handler;
    SDKModel licence;
    LinearLayout readerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        readerLayout = (LinearLayout) findViewById(R.id.readerLayout);
        NfcConnection.getInstance().init(MainActivity.this);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0) {
                    Toast.makeText(getApplicationContext(), "Lisans olusturulamadi", Toast.LENGTH_LONG).show();
                    return;
                }

                if (msg.what == 1) {
                    try {
                        Reader reader = new Reader.Builder()
                                .setContext(MainActivity.this) 
                                .setSdkModel(licence) //sets up the permissions on the SDK Model
                                .setLinearLayout(readerLayout) //Required for enabling the camera view
                                .setCardType(CardType.Passport) // Selects and sets the card type
                                .setScanResultInterface(MainActivity.this) // Used for obtaining the results of the OCR scan
                                .setNfcScanResultInterface(MainActivity.this) // Used for obtaining the results of the NFC scan
                                .setOptionData("NFCREAD") // Optional parameter for logging purposes
                                .build();
                        Log.d("TAG", "run: " + SDKModel.getInstance());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        new Thread(() -> {

                Engine.getInstance().initEngine(MainActivity.this, new IEngineCallback() {
                    @Override
                    public void onSuccess(SDKModel sdkModel) {
                        licence = sdkModel;
                    }

                    @Override
                    public void onError(Exception ex) {
                        ex.printStackTrace();
                        handler.sendEmptyMessage(0);
                    }
                });
                handler.sendEmptyMessage(1);

        }).start();

    }

    @Override
    public void scanResult(PassportModel passportModel) {
        if (passportModel != null) {
            Log.d("heyhey", "result: " + passportModel.toString());
            try {
                //OCR isleminden sonra NFC ile okuma baslatmak icin
                Reader.getInstance().setPassportData(passportModel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    @Override
    public void nfcResult(PassportModel nfcData) {
        try {
            if (nfcData != null) {
                Log.d("TAG", "result: " + nfcData.toString());
            }
        } catch (Exception ex) {
            Log.d("TAG", "result: " + ex.getMessage());
        }
    }

    @Override
    public void nfcSteps(String file, String status) {
        Log.d("TAG", "steps: " + file + status);
    }

    @Override
    public void nfcError(Exception ex, String message) {
        Log.d("TAG", "error: " + ex.getMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Reader.getInstance().onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Reader.getInstance().onPause();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Reader.getInstance().onNewIntent(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Reader.getInstance().onResume();
    }

}
```
