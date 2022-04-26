/**
 * Hero is the main entity we'll be using to . . .
 *
 * @author NFCRead
 */
package com.sanalogi.ReaderSDK;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sanalogi.cameralibrary.CardType;
import com.sanalogi.cameralibrary.Engine;
import com.sanalogi.cameralibrary.IEngineCallback;
import com.sanalogi.cameralibrary.PassportModel;
import com.sanalogi.cameralibrary.ProcessCamera;
import com.sanalogi.cameralibrary.Reader;
import com.sanalogi.cameralibrary.SDKModel;
import com.sanalogi.cameralibrary.ScanResultInterface;
import com.sanalogi.cameralibrary.nfc.NfcConnection;
import com.sanalogi.cameralibrary.nfc.NfcScanResultInterface;

public class MainActivity extends AppCompatActivity implements ScanResultInterface, NfcScanResultInterface {

    private final String TAG = "MainActivity";
    private ProcessCamera myView;
    private TextView documentType;
    private TextView documentNumber;
    private TextView documentCountry;
    private TextView documentOwnerNationality;
    private TextView fullname;
    private TextView idno;
    private TextView name;
    private TextView surname;
    private ImageView ownerImage;
    private ImageView docFrontImage,imgUpperChipset,imgFaceCard,imgGhostCard,imgSignCard,imgFlag,imgHologram,imgBarcodeResult;
    private TextView birthDate;
    private TextView expiryDate;
    private TextView placeOfBirth;
    private TextView phoneNumber;
    private TextView profession;
    private TextView adress;
    private TextView gender;
    private TextView mrz;
    private ScrollView infoScroll;
    private TextView nfcDialogInfo;
    private Button nfcbutton;
    private Button faceMatchBtn;
    Dialog dialog;
    private ProgressDialog progressBar;
    private SDKModel licence;
    private LinearLayout readerLayout;
    private Handler handler;
    private PassportModel passportDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        infoScroll = findViewById(R.id.infoScroll);
        documentType = findViewById(R.id.documentType);
        imgUpperChipset = findViewById(R.id.imgUpperChipset);
        imgFaceCard = findViewById(R.id.imgFaceCard);
        imgGhostCard = findViewById(R.id.imgGhostCard);
        imgSignCard = findViewById(R.id.imgSignCard);
        imgFlag = findViewById(R.id.imgFlag);
        imgHologram = findViewById(R.id.imgHologram);
        imgBarcodeResult = findViewById(R.id.imgBarcodeResult);
        documentNumber = findViewById(R.id.documentNumber);
        documentCountry = findViewById(R.id.documentCountry);
        documentOwnerNationality = findViewById(R.id.documentOwnerNationality);
        fullname = findViewById(R.id.nfcDialogInfo);
        idno = findViewById(R.id.documentOwnerPersonNumber);
        name = findViewById(R.id.documentOwnerName2);
        surname = findViewById(R.id.documentOwnerSurname);
        ownerImage = findViewById(R.id.nfcDialogImage);
        docFrontImage = findViewById(R.id.idCardFrontImage);
        birthDate = findViewById(R.id.documentOwnerBirthDate);
        expiryDate = findViewById(R.id.documentExpiryDate);
        placeOfBirth = findViewById(R.id.documentOwnerBirthPlace);
        phoneNumber = findViewById(R.id.documentOwnerPhone);
        profession = findViewById(R.id.documentOwnerProfession);
        adress = findViewById(R.id.documentOwnerAdress);
        gender = findViewById(R.id.documentOwnerGender);
        mrz = findViewById(R.id.documentMRZ);
        nfcbutton = findViewById(R.id.nfcbutton);
        faceMatchBtn = findViewById(R.id.faceMatchBtn);
        infoScroll.setVisibility(View.GONE);

        nfcbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Eger terminalden okumak istersen bu satiri aktif hale getirmelisin.
                //Reader.getInstance().startReadFromTerminal();
                showMyCustomAlertDialog();
            }
        });

        faceMatchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passportDatas != null){
                    Intent intent = new Intent(MainActivity.this,FaceMatchActivity.class);
                    intent.putExtra("firstEncodedbase64",passportDatas.getBiometricImage());
                    startActivity(intent);
                    finish();
                }
            }
        });

        settingsForNFCREAD();
    }

    void settingsForNFCREAD() {
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage("Bekleyiniz");
        progressBar.setCancelable(false);
        progressBar.show();

        readerLayout = (LinearLayout) findViewById(R.id.readerLayout); //Kamera ekraninin goruntulenmesi icin
        NfcConnection.getInstance().init(MainActivity.this); //NFC taginin bulanabilmesi icin eklenmeli

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (progressBar != null && progressBar.isShowing() && licence != null) {
                    progressBar.dismiss();
                }
                Log.e(TAG, "handleMessage: " + msg.what);

                if (msg.what == 0) {
                    Toast.makeText(getApplicationContext(), "Lisans oluşturulamadı", Toast.LENGTH_LONG).show();
                    return;
                }

                if (msg.what == 1) {
                    try {
                        Reader reader = new Reader.Builder()
                                .setContext(MainActivity.this)
                                .setSdkModel(licence)
                                .setLinearLayout(readerLayout)
                                .setCardType(CardType.IdCard)
                                .enableIDCardFrontScan(true)
                                .setScanResultInterface(MainActivity.this)
                                .setNfcScanResultInterface(MainActivity.this)
                                .build();
                        Log.d(TAG, "run: " + licence);
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


    public void setDocValues(PassportModel passportModel) {
        documentType.setText(passportModel.getDocumentCode() != null ? passportModel.getDocumentCode() : "-");
        documentNumber.setText(passportModel.getDocumentNumber() != null ? passportModel.getDocumentNumber() : "-");
        documentCountry.setText(passportModel.getIssuingState() != null ? passportModel.getIssuingState() : "-");
        documentOwnerNationality.setText(passportModel.getNationality() != null ? passportModel.getNationality() : "-");
        fullname.setText(passportModel.getFirstName() + " " + passportModel.getLastName() != null ? passportModel.getFirstName() + " " + passportModel.getLastName() : "-");
        name.setText(passportModel.getFirstName() != null ? passportModel.getFirstName() : "-");
        surname.setText(passportModel.getLastName() != null ? passportModel.getLastName() : "-");
        birthDate.setText(passportModel.getDateOfBirth() != null ? passportModel.getDateOfBirth() : "-");
        expiryDate.setText(passportModel.getDateOfExpiry() != null ? passportModel.getDateOfExpiry() : "-");
        placeOfBirth.setText(passportModel.getPlaceOfBirth() != null ? passportModel.getPlaceOfBirth().toString() : "-");
        phoneNumber.setText(passportModel.getTelephone() != null ? passportModel.getTelephone() : "-");
        adress.setText(passportModel.getPermanentAddress() != null ? passportModel.getPermanentAddress().toString() : "-");
        gender.setText(passportModel.getGender() != null ? passportModel.getGender() : "-");
        profession.setText(passportModel.getProfession() != null ? passportModel.getProfession() : "-");
        mrz.setText(passportModel.getMrz() != null ? passportModel.getMrz() : "-");
        idno.setText(passportModel.getPersonalNumber() != null ? passportModel.getPersonalNumber() : "-");
        if (passportModel.getBiometricImage() != null) {
            ownerImage.setImageBitmap(passportModel.getBiometricImage());
        }
        imgFlag.setImageBitmap(convertBitmapFromBase64(passportModel.getFlagImageBase64()));
        imgGhostCard.setImageBitmap(convertBitmapFromBase64(passportModel.getGhostImageBase64()));
        imgUpperChipset.setImageBitmap(convertBitmapFromBase64(passportModel.getUpperChipset()));
        imgFaceCard.setImageBitmap(convertBitmapFromBase64(passportModel.getFaceImageBase64()));
        imgSignCard.setImageBitmap(convertBitmapFromBase64(passportModel.getSignImageBase64()));
        imgHologram.setImageBitmap(convertBitmapFromBase64(passportModel.getHologramImageBase64()));
        imgBarcodeResult.setImageBitmap(convertBitmapFromBase64(passportModel.getBarcodeBase64()));
     //   if (passportModel.getPassportImage() != null) {
     //   docImage.setImageBitmap(convertBitmapFromBase64(passportModel.getPassportImageBase64()));
      //  }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Reader.getInstance().onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart: ");
        super.onRestart();
        infoScroll.setVisibility(View.GONE);
        nfcbutton.setVisibility(View.GONE);
        faceMatchBtn.setVisibility(View.GONE);
        progressBar.dismiss();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        Reader.getInstance().onPause();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: ");
        super.onNewIntent(intent);
        Reader.getInstance().onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        Reader.getInstance().onResume();
    }

    public void showMyCustomAlertDialog() {

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        final LinearLayout dialogView = dialog.findViewById(R.id.mainDialogView);
        ViewGroup.LayoutParams dialogViewParams = dialogView.getLayoutParams();
        dialogViewParams.height = (int) (height * 0.45);
        dialogViewParams.width = (int) (width);
        dialogView.setLayoutParams(dialogViewParams);

        nfcDialogInfo = (TextView) dialog.findViewById(R.id.nfcDialogInfo);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                infoScroll.setVisibility(View.GONE);
                nfcbutton.setVisibility(View.GONE);
                faceMatchBtn.setVisibility(View.GONE);
                progressBar.dismiss();
            }
        });

        dialog.show();
    }
    private Bitmap convertBitmapFromBase64(String base64){
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
    @Override
    public void scanResult(PassportModel passportModel) {
        if (passportModel != null) {
            Log.d("heyhey", "result: " + passportModel.toString());
            passportDatas = passportModel;
            setDocValues(passportModel);

          ///*  if (passportModel.getIdCardFrontImage() != null) {
                docFrontImage.setVisibility(View.VISIBLE);
                docFrontImage.setImageBitmap(convertBitmapFromBase64(passportModel.getFrontImageBase64()));
          //  }*/

            infoScroll.setVisibility(View.VISIBLE);
            nfcbutton.setVisibility(View.VISIBLE);
            faceMatchBtn.setVisibility(View.VISIBLE);
            try {
                //OCR isleminden sonra NFC ile okuma baslatmak icin
                Reader.getInstance().setPassportData(passportModel);
                //NFC okuması sırasındaki aşamaları isteğe göre ayarlar
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
                Reader.getInstance().setNfcScanSteps(arr);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    @Override
    public void nfcResult(PassportModel nfcData) {
        try {
            if (nfcData != null) {
                passportDatas = nfcData;
                setDocValues(nfcData);
                Log.d(TAG, "result: " + nfcData.toString());
                dialog.dismiss();
            }
        } catch (Exception ex) {
            Log.d(TAG, "result: " + ex.getMessage());
        }
    }

    @Override
    public void nfcSteps(String file, String status) {
        Log.d(TAG, "steps: " + file + status);
        nfcDialogInfo.setText(file + " " + status);
    }

    @Override
    public void nfcError(Exception ex, String message) {
        Log.d(TAG, "error: " + ex.getMessage());
        nfcDialogInfo.setText(message);
        Toast.makeText(getApplicationContext(), "message : " + ex.getMessage() + " additional: " + message, Toast.LENGTH_LONG);
    }
}
