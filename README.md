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
    implementation 'org.bitbucket.sanalogi:sanalogiReaderAndroid:1.2.0'
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

To learn more about generating API keys, please refer to the Frequently Asked Questions section below.

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
                    Toast.makeText(getApplicationContext(), "License could not be created", Toast.LENGTH_LONG).show();
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

** Add the following code to your activity code, in this example it is FaceMatchActivity.java **

```java
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sanalogi.cameralibrary.Engine;
import com.sanalogi.cameralibrary.FaceMatching;
import com.sanalogi.cameralibrary.FaceResultInterface;
import com.sanalogi.cameralibrary.IEngineCallback;
import com.sanalogi.cameralibrary.SDKModel;

public class FaceMatchActivity extends AppCompatActivity implements FaceResultInterface {

    LinearLayout faceMatchLayout;
    Bitmap passportFace;
    private SDKModel licence;
    private Handler handler;
    private final String TAG = "FaceMatchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_face_match);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        faceMatchLayout = findViewById(R.id.readerLayout);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0) {
                    Toast.makeText(getApplicationContext(), "License could not be created", Toast.LENGTH_LONG).show();
                    return;
                }

                if (msg.what == 1) {
                    try {
                        FaceMatching reader = new FaceMatching.Builder()
                                .setContext(FaceMatchActivity.this)
                                .setSdkModel(licence)//sets up the permissions on the SDK Model
                                .setLinearLayout(faceMatchLayout)//Required for enabling the camera view
                                .setFaceToBeMatch(passportFace)//Photo desired to be compared
                                .setOptionData("NFCREAD-FACE-MATCHING")// Optional parameter for logging purposes
                                .setSendFaceData(FaceMatchActivity.this)//Used for obtaining the results of the face matching
                                .build();
                        Log.d(TAG, "run: " + licence);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        new Thread(() -> {

            Engine.getInstance().initEngine(FaceMatchActivity.this, new IEngineCallback() {
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
    public void status(String status) {
        Log.d("NFCREAD", "status: "+status);
    }

    @Override
    public void result(String s) {
        Log.d("NFCREAD", "result: "+s);
    }

}
```

# Frequently Asked Questions

## Generating API keys

Please note that this step requires having a NFC developer account. For registering, please head to our [signup page](https://login.nfcread.com/signup)

If you have a developer account, simply navigate to [login.nfcread.com](https://login.nfcread.com) and enter your credentials. Once logged in select "NFCRead SDK Key" on the leftmost menu, press on the "GENERATE NFCRead Mobile SDK Key" button and then follow through with the steps.

## Reducing the size of your application

One of the most commonly asked questions about the NFCRead is reducing the size of the library. Since we include external libraries for performing various scans inside the SDK, these packages comes with dynamically linked shared object
libraries (.so) that are compiled for different CPU architectures used widely in the mobile phones. The list of these supported instruction sets are like in the following:

-armeabi-v7a (Instruction set for 32bit ARM Processors, versions v5 and v6 are deprecated)

-arm64-v8a (Instruction set for 64bit ARM Processors)

-x86 (Instruction set for 32bit Intel and AMD Processors)

-x86_64 (Instruction set for 64bit Intel and AMD Processors)

To learn more details about these instruction sets and ABIs, please refer to [this link](https://developer.android.com/ndk/guides/abis).

The way we suggest for reducing the size of your application is creating four separate APK files that are each compiled for a specific instruction set. Google Play Store supports [the publishing of multiple APK files](https://developer.android.com/google/play/publishing/multiple-apks)
, hence it is possible to upload four different APK files and the Play Store application located on user devices will automatically determine which APK to be installed.

### To reduce the size of your APK:

Navigate to your app level build.gradle file (app/build.gradle) and add the following command:

```groovy
android{
    ...
    splits {
        abi {
            enable true
            reset()
            include "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
            universalApk false
        }
    }
}
```

More detailed instructions and information about splits function can be found in [here](https://developer.android.com/studio/build/configure-apk-splits)

## My application crashes after I set minifyEnabled to true

As mentioned before, NFCRead SDK relies on multiple libraries, one of them being OpenCV. When minifyEnabled is set true, the version of OpenCV we bundle alongside the NFCRead SDK
is also getting obfuscated alongside with the rest of the application. This essentially prevents the NFCRead library from accessing the OpenCV classes and methods and causes crashes.

### To prevent your application from crashing:

Simply navigate to your proguard-rules.pro file and add the following rule:

```proguard
-keep class org.opencv.** { *;}
```

The rule above prevents the obfuscation of the OpenCV library that is bundled with the NFCRead SDK, and prevents further obfuscation related crashes.

## NFC feature stops working after I set minifyEnabled to true

Similar to OpenCV related crashes, this bug is also caused by other external libraries getting obfuscated by ProGuard.

### To prevent NFC related issues:

Simply navigate to your proguard-rules.pro file and add the following rule:

```proguard
-keep class net.sf.scuba.smartcards.IsoDepCardService { *;}
-keep class org.spongycastle.** { *;}
-keep class org.bouncycastle.** { *;}
```

If there are further issues observed due to this crash, please get in contact with the NFCRead team if you're running a version of NFCRead SDK version 1.1.7 or later.
Please make sure to add the logcat logs by filtering with NFCREAD tag.
