/**
 * Hero is the main entity we'll be using to . . .
 *
 * @author Can Samet KATKAT
 */
package com.sanalogi.ReaderSDK;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    private ImageView docImage;
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
    Dialog dialog;
    private ProgressDialog progressBar;
    private SDKModel licence;
    private LinearLayout readerLayout;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        infoScroll = (ScrollView) findViewById(R.id.infoScroll);
        documentType = (TextView) findViewById(R.id.documentType);
        documentNumber = (TextView) findViewById(R.id.documentNumber);
        documentCountry = (TextView) findViewById(R.id.documentCountry);
        documentOwnerNationality = (TextView) findViewById(R.id.documentOwnerNationality);
        fullname = (TextView) findViewById(R.id.nfcDialogInfo);
        idno = (TextView) findViewById(R.id.documentOwnerPersonNumber);
        name = (TextView) findViewById(R.id.documentOwnerName2);
        surname = (TextView) findViewById(R.id.documentOwnerSurname);
        ownerImage = (ImageView) findViewById(R.id.nfcDialogImage);
        docImage = (ImageView) findViewById(R.id.passportImage);
        birthDate = (TextView) findViewById(R.id.documentOwnerBirthDate);
        expiryDate = (TextView) findViewById(R.id.documentExpiryDate);
        placeOfBirth = (TextView) findViewById(R.id.documentOwnerBirthPlace);
        phoneNumber = (TextView) findViewById(R.id.documentOwnerPhone);
        profession = (TextView) findViewById(R.id.documentOwnerProfession);
        adress = (TextView) findViewById(R.id.documentOwnerAdress);
        gender = (TextView) findViewById(R.id.documentOwnerGender);
        mrz = (TextView) findViewById(R.id.documentMRZ);
        nfcbutton = (Button) findViewById(R.id.nfcbutton);
        infoScroll.setVisibility(View.GONE);

        nfcbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Eger terminalden okumak istersen bu satiri aktif hale getirmelisin.
                Reader.getInstance().startReadFromTerminal();
                showMyCustomAlertDialog();
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
                    Toast.makeText(getApplicationContext(), "Lisans olusturulamadi", Toast.LENGTH_LONG).show();
                    return;
                }

                if (msg.what == 1) {
                    try {
                        Reader reader = new Reader.Builder()
                                .setContext(MainActivity.this)
                                .setSdkModel(licence)
                                .setLinearLayout(readerLayout)
                                .setCardType(CardType.Passport)
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
        if (passportModel.getPassportImage() != null) {
            docImage.setImageBitmap(passportModel.getPassportImage());
        }
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
                progressBar.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void scanResult(PassportModel passportModel) {
        Log.d("heyhey", "result: " + passportModel.toString());
        if (passportModel != null) {
            Log.d("heyhey", "result: " + passportModel.toString());
            setDocValues(passportModel);
            infoScroll.setVisibility(View.VISIBLE);
            nfcbutton.setVisibility(View.VISIBLE);
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
