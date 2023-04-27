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
        
     }
     
}

allprojects {
        repositories {
            
            mavenCentral()
            maven {
                url 'https://jitpack.io'
                credentials { username authToken }
            }
        }
    }
```

## 2: Include the Jitpack token to your gradle.properties (root/gradle.properties)

```
    authToken=jp_7co03jomd4r4i1929q93i601bi
```

## 3: Add the dependencies to your app's build.gradle file (app/build.gradle)

```groovy
android {
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
dependencies {

    implementation 'org.bitbucket.sanalogi:nfc_read_android_sdk:0.0.9'
}
```

After performing this step, run Gradle Sync to let Android Studio download the the SDK from the Jitpack repository.

## 4: Include the following to Android Manifest file

```xml
<application
	android:requestLegacyExternalStorage="true"
	android:usesCleartextTraffic="true">

	<meta-data android:name="com.sanalogi.cameralibrary" android:value="@string/apiKey" />

	<activity>
		
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

** Start activity from your app **
```java
public class MainActivity extends AppCompatActivity implements NfcScanResultInterface {
    private static final int REQUEST_CODE_SCAN_CARD = 1;
}

```
```java
    boolean passportMode = false;
    Intent intent = new ScanCardIntent.Builder(MainActivity.this ).setPassportMode(passportMode).build();
    startActivityForResult(intent, REQUEST_CODE_SCAN_CARD);
```


```java
    @Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case REQUEST_CODE_SCAN_CARD:
			if (resultCode == Activity.RESULT_OK) {
				PassportModel card = (PassportModel) data.getSerializableExtra(ScanCardIntent.RESULT_PAYCARDS_CARD);

			}
		break;
        default:
        	super.onActivityResult(requestCode, resultCode, data);
        }
}

```

## Use NFC interface for read NFC-chip
implement interface NFC connection
```java
public class MainActivity extends AppCompatActivity implements NfcScanResultInterface
```
```java
@Override
    public void nfcResult(PassportModel nfcData) {

    }

    @Override
    public void nfcSteps(String file, String status) {
    
    }

    @Override
    public void nfcError(Exception ex, String message) {
    
    }
```

after read cart from OCR set NFC connection step and model 
```java
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQUEST_CODE_SCAN_CARD:
        if (resultCode == Activity.RESULT_OK) {
        PassportModel card = (PassportModel) data.getSerializableExtra(ScanCardIntent.RESULT_PAYCARDS_CARD);
        String[] arr ={
        "1 NFC çipine bağlanılıyor.",
        "2 NFC çipine bağlanıldı.",
        "3 Kimlik bilgileri alınıyor, lütfen bekleyiniz.",
        "4 Kimlik bilgiler alındı.",
        "5 Kimlik sahibinin bilgileri alınıyor",
        "6 Kimlik sahibinin bilgileri alındı.",
        "7 Kimlik sahibinin biometrik resmi alınıyor.",
        "8 Kimlik sahibinin bilgileri alındı.",
        "9 NFC okuması tamamlandı."};
        NfcConnection.getInstance().init(this); //NFC taginin bulanabilmesi icin eklenmeli
        NfcConnection.getInstance().setNfcScanResultInterface(this);
        NfcConnection.getInstance().setNfcScanSteps(arr);

        NfcConnection.getInstance().setPassportModel(card);
        }
        break;
default:
        super.onActivityResult(requestCode, resultCode, data);
        }
        }
                    

```


# Frequently Asked Questions

## Generating API keys

Please note that this step requires having a NFC developer account. For registering, please head to our [signup page](https://login.nfcread.com/signup)

If you have a developer account, simply navigate to [login.nfcread.com](https://login.nfcread.com) and enter your credentials. Once logged in select "NFCRead SDK Key" on the leftmost menu, press on the "GENERATE NFCRead Mobile SDK Key" button and then follow through with the steps.


### To prevent NFC related issues:

Simply navigate to your proguard-rules.pro file and add the following rule:

```proguard
-keep class net.sf.scuba.smartcards.IsoDepCardService { *;}
-keep class org.spongycastle.** { *;}
-keep class org.bouncycastle.** { *;}
```

