package com.sanalogi.ReaderSDK;


import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.camera2.params.Face;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sanalogi.cameralibrary.Engine;
import com.sanalogi.cameralibrary.FaceMatching;
import com.sanalogi.cameralibrary.FaceResultInterface;
import com.sanalogi.cameralibrary.IEngineCallback;
import com.sanalogi.cameralibrary.SDKModel;

public class FaceMatchActivity extends AppCompatActivity implements FaceResultInterface {

    LinearLayout faceMatchLayout;
    ImageView faceImage;
    TextView faceInfo;
    Bitmap passportFace;
    private SDKModel licence;
    private Handler handler;
    private ProgressDialog progressBar;
    private final String TAG = "FaceMatchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_face_match);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        faceImage = findViewById(R.id.faceImage);
        faceInfo = findViewById(R.id.faceInfo);

        /* kimlik okunduktan sonra gelen biometrik foto kullanilmak istendiginde bir onceki
           aktivityden 'firstEncodedbase64' keyi ile bu aktivitiye gondermelisin
        */
        Intent intent = getIntent();
        if (intent != null && intent.getParcelableExtra("firstEncodedbase64") != null) {
            passportFace = (Bitmap) intent.getParcelableExtra("firstEncodedbase64");
        }

        settingForFaceMatching();

    }

    public void settingForFaceMatching(){
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage("Bekleyiniz");
        progressBar.setCancelable(false);
        progressBar.show();

        faceMatchLayout = findViewById(R.id.faceMatchLayout);

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
                        FaceMatching reader = new FaceMatching.Builder()
                                .setContext(FaceMatchActivity.this)
                                .setSdkModel(licence)
                                .setLinearLayout(faceMatchLayout)
                                .setFaceToBeMatch(passportFace)//karsilastirilmak istenen foto
                                .setOptionData("NFCREAD-FACE-MATCHING")
                                .setSendFaceData(FaceMatchActivity.this)
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                faceInfo.setText(status);
            }
        });
    }

    @Override
    public void success(Bitmap bmp) {
//        faceImage.setVisibility(View.VISIBLE);
//        faceImage.setImageBitmap(bmp);
    }

}
