package com.nfcread.com.sampleandroid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sanalogi.nfcreader.PassportModel;
import com.sanalogi.nfcreader.ScanCardIntent;
import com.sanalogi.nfcreader.nfc.NfcConnection;
import com.sanalogi.nfcreader.nfc.NfcScanResultInterface;


public class MainActivity extends AppCompatActivity implements NfcScanResultInterface {
    private static final int REQUEST_CODE_SCAN_CARD = 1;

    Button tvAction;

    Button btnReadNFC;
    TextView tvResultMRZ;
    TextView tvSureName;
    TextView tvGivenName;
    TextView tvBirthDay;
    TextView tvDocumentNumber;
    TextView tvValidUntil;
    TextView tvGender;
    TextView tvNfcInfo;
    TextView tvPersonalNumber;
    TextView tvNationality;
    ImageView imgFrontMain;
    ImageView imgbackMain;
    ImageView imgCardNFC;
    SwitchCompat aSwitch;
    Dialog dialog;
    PassportModel card;

    ProgressBar progressBarNFC;
    private TextView nfcDialogInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvAction = findViewById(R.id.btnAction);
        btnReadNFC = findViewById(R.id.btnReadNfcChip);
        tvPersonalNumber = findViewById(R.id.tvPersonalNumber);
        tvSureName = findViewById(R.id.tvSureName);
        tvGivenName = findViewById(R.id.tvGivenName);
        tvBirthDay = findViewById(R.id.tvBirthDay);
        tvDocumentNumber = findViewById(R.id.tvDocumentNumber);
        tvValidUntil = findViewById(R.id.tvValidUntil);
        tvGender = findViewById(R.id.tvGender);
        tvNationality = findViewById(R.id.tvNationality);
        imgFrontMain = findViewById(R.id.imgFrontMain);
        imgbackMain = findViewById(R.id.imgbackMain);
        tvResultMRZ = findViewById(R.id.tvResultMRZ);
        imgCardNFC = findViewById(R.id.imgCardNFC);
        aSwitch = findViewById(R.id.switch1);

        btnReadNFC.setOnClickListener(view -> showMyCustomAlertDialog());
        tvAction.setOnClickListener(view -> {
            imgbackMain.setImageBitmap(null);
            imgFrontMain.setImageBitmap(null);
            imgCardNFC.setImageBitmap(null);
            tvSureName.setText("");
            tvGivenName.setText("");
            tvBirthDay.setText("");
            tvDocumentNumber.setText("");
            tvValidUntil.setText("");
            tvGender.setText("");
            tvResultMRZ.setText("");

            tvNationality.setText("");
            Intent intent = new ScanCardIntent.Builder(MainActivity.this ).setPassportMode(aSwitch.isChecked()).build();
            startActivityForResult(intent, REQUEST_CODE_SCAN_CARD);
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        NfcConnection.getInstance().onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SCAN_CARD:
                if (resultCode == Activity.RESULT_OK) {
                    card = (PassportModel) data.getSerializableExtra(ScanCardIntent.RESULT_PAYCARDS_CARD);
                    //  Bitmap card1 = data.getParcelableExtra(ScanCardIntent.RESULT_FRONT);
                    tvResultMRZ.setText(card.getMrz());
                    byte[] front = data.getByteArrayExtra(ScanCardIntent.RESULT_CARD_FRONT);
                    byte[] back = data.getByteArrayExtra(ScanCardIntent.RESULT_CARD_BACK);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(front, 0, front.length);
                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(back, 0, back.length);
                    imgFrontMain.setImageBitmap(bitmap);
                    imgbackMain.setImageBitmap(bitmap1);
                    tvSureName.setText("Surname : " + card.getFirstName());
                    tvGivenName.setText("GivenName : " + card.getLastName());
                    tvBirthDay.setText("BirthDay : " + card.getDateOfBirth());
                    tvDocumentNumber.setText("DocumentNumber : " + card.getDocumentNumber());
                    tvValidUntil.setText("ValidUntil : " + card.getDateOfExpiry());
                    tvGender.setText("Gender : " + card.getGender());
                    tvNationality.setText("Nationality  : " + card.getNationality());
                    tvPersonalNumber.setText("Personal Number  : " + card.getPersonalNumber());
                    tvAction.setText("New Scan");
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
                    NfcConnection.getInstance().init(this); //NFC taginin bulanabilmesi icin eklenmeli
                    NfcConnection.getInstance().setNfcScanResultInterface(this);
                    NfcConnection.getInstance().setNfcScanSteps(arr);

                    NfcConnection.getInstance().setPassportModel(card);
                    PackageManager pm = getPackageManager();
                    if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
                        btnReadNFC.setVisibility(View.GONE);
                    } else {
                        btnReadNFC.setVisibility(View.VISIBLE);
                    }


                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /**
         * Aktivity yasam dongusune gore NFC sonlandirilmasi yada baslatilmasi saglanir
         */

        NfcConnection.getInstance().onNewIntent(intent);
    }
    @Override
    public void nfcResult(PassportModel nfcData) {
        card = nfcData;
        tvSureName.setText("Surname : " + card.getFirstName());
        tvGivenName.setText("GivenName : " + card.getLastName());
        tvBirthDay.setText("BirthDay : " + card.getDateOfBirth());
        tvDocumentNumber.setText("DocumentNumber : " + card.getDocumentNumber());
        tvValidUntil.setText("ValidUntil : " + card.getDateOfExpiry());
        tvGender.setText("Gender : " + card.getGender());
        tvNationality.setText("Nationality  : " + card.getNationality());
        imgCardNFC.setImageBitmap(nfcData.getBiometricImage());
        dialog.dismiss();

    }

    @Override
    public void nfcSteps(String file, String status) {
        if(dialog != null){
            progressBarNFC.setVisibility(View.VISIBLE);
            nfcDialogInfo.setText(file + " " + status);
            tvNfcInfo.setVisibility(View.GONE);
        }

    }

    @Override
    public void nfcError(Exception ex, String message) {
        if(dialog != null){
            progressBarNFC.setVisibility(View.GONE);
            Toast.makeText(this, "NFC ERROR", Toast.LENGTH_SHORT).show();
            tvNfcInfo.setText("NFC Error : "+ ex.getMessage() + " \n " + "Please take out the card and put it back again");
            nfcDialogInfo.setText("");
            tvNfcInfo.setVisibility(View.VISIBLE);
        }




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
        progressBarNFC = (ProgressBar) dialog.findViewById(R.id.progressBarNFC);
        tvNfcInfo = (TextView) dialog.findViewById(R.id.tvNfcInfo);
        Button btnCancelNFC = (Button) dialog.findViewById(R.id.btnCancelNFC);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        btnCancelNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });

        dialog.show();
    }

}