package com.example.admin.gitfirstproject;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nitgen.SDK.AndroidBSP.NBioBSPJNI;
import com.nitgen.SDK.AndroidBSP.StaticVals;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    ImageView img;
    Button capture,verify;
    private NBioBSPJNI bsp;
    private NBioBSPJNI.Export exportEngine;
    private NBioBSPJNI.IndexSearch indexSearch;
    private byte[] byTemplate1;
    private byte[] byTemplate2;

    private byte[] byCapturedRaw1;
    private int nCapturedRawWidth1;
    private int nCapturedRawHeight1;

    private byte[] byCapturedRaw2;
    private int nCapturedRawWidth2;
    private int nCapturedRawHeight2;


    DialogFragment sampleDialogFragment;
    UserDialog userDialog;

    private boolean bCapturedFirst, bAutoOn = false;

    public static final int QUALITY_LIMIT = 60;

    NBioBSPJNI.FIR_TEXTENCODE sss,sss1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = (ImageView)findViewById(R.id.capture_image);
        capture = (Button)findViewById(R.id.store);
        verify = (Button)findViewById(R.id.verify);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnCapture1(10000);


            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OnCapture2(10000);
                OnBtnVerifyRaw();
            }
        });

        initData();
    }

    public void OnBtnOpenDevice(View target) {

        sampleDialogFragment.show(getSupportFragmentManager(), "DIALOG_TYPE_PROGRESS");
        bsp.OpenDevice();

    }

    int nFIQ = 0;
    String msg = "";

    public void initData() {

        NBioBSPJNI.CURRENT_PRODUCT_ID = 0;
        if (bsp == null) {
            bsp = new NBioBSPJNI("010701-613E5C7F4CC7C4B0-72E340B47E034015", this, mCallback);
            String msg = null;
            if (bsp.IsErrorOccured())
                msg = "NBioBSP Error: " + bsp.GetErrorCode();
            else {
                msg = "SDK Version: " + bsp.GetVersion();
                exportEngine = bsp.new Export();
                indexSearch = bsp.new IndexSearch();
            }

        }

        sampleDialogFragment = new SampleDialogFragment();
        userDialog = new UserDialog();

    }

    NBioBSPJNI.CAPTURE_CALLBACK mCallback = new NBioBSPJNI.CAPTURE_CALLBACK() {

        public void OnDisConnected() {
            NBioBSPJNI.CURRENT_PRODUCT_ID = 0;

            if (sampleDialogFragment != null)
                sampleDialogFragment.dismiss();

            String message = "NBioBSP Disconnected: " + bsp.GetErrorCode();


        }

        public void OnConnected() {
            if (sampleDialogFragment != null)
                sampleDialogFragment.dismiss();

            String message = "Device Open Success : ";

            ByteBuffer deviceId = ByteBuffer.allocate(StaticVals.wLength_GET_ID);
            deviceId.order(ByteOrder.BIG_ENDIAN);
            bsp.getDeviceID(deviceId.array());

            if (bsp.IsErrorOccured()) {
                msg = "NBioBSP GetDeviceID Error: " + bsp.GetErrorCode();

                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();

                return;
            }

            ByteBuffer setValue = ByteBuffer.allocate(StaticVals.wLength_SET_VALUE);
            setValue.order(ByteOrder.BIG_ENDIAN);

            byte[] src = new byte[StaticVals.wLength_SET_VALUE];
            for (int i = 0; i < src.length; i++) {
                src[i] = 1;
            }
            setValue.put(src);
            bsp.setValue(setValue.array());

            if (bsp.IsErrorOccured()) {
                msg = "NBioBSP SetValue Error: " + bsp.GetErrorCode();
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                return;
            }

            ByteBuffer getvalue = ByteBuffer.allocate(StaticVals.wLength_GET_VALUE);
            getvalue.order(ByteOrder.BIG_ENDIAN);
            bsp.getValue(getvalue.array());

            if (bsp.IsErrorOccured()) {
                msg = "NBioBSP GetValue Error: " + bsp.GetErrorCode();
                Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                return;
            }
            src = new byte[StaticVals.wLength_SET_VALUE];
            System.arraycopy(getvalue.array(), 0, src, 0, StaticVals.wLength_GET_VALUE);
//	        message += " \n";
//	        for(int i=0;i<src.length;i++){
//	        	message += src[i];
//	        }

            NBioBSPJNI.INIT_INFO_0 init_info_0 = bsp.new INIT_INFO_0();
            bsp.GetInitInfo(init_info_0);

            NBioBSPJNI.CAPTURE_QUALITY_INFO mCAPTURE_QUALITY_INFO = bsp.new CAPTURE_QUALITY_INFO();
            bsp.GetCaptureQualityInfo(mCAPTURE_QUALITY_INFO);

            mCAPTURE_QUALITY_INFO.EnrollCoreQuality = 70;
            mCAPTURE_QUALITY_INFO.EnrollFeatureQuality = 30;
            mCAPTURE_QUALITY_INFO.VerifyCoreQuality = 70;
            mCAPTURE_QUALITY_INFO.VerifyFeatureQuality = 30;
            bsp.SetCaptureQualityInfo(mCAPTURE_QUALITY_INFO);


        }

        public int OnCaptured(NBioBSPJNI.CAPTURED_DATA capturedData) {

            if (capturedData.getImage() != null) {

                img.setImageBitmap(capturedData.getImage());

            }

            // quality : 40~100
            if (capturedData.getImageQuality() >= QUALITY_LIMIT) {
                if (sampleDialogFragment != null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
                    sampleDialogFragment.dismiss();
                return NBioBSPJNI.ERROR.NBioAPIERROR_USER_CANCEL;
            } else if (capturedData.getDeviceError() != NBioBSPJNI.ERROR.NBioAPIERROR_NONE) {
                if (sampleDialogFragment != null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
                    sampleDialogFragment.dismiss();
                return capturedData.getDeviceError();
            } else {
                return NBioBSPJNI.ERROR.NBioAPIERROR_NONE;
            }
        }

    };


    public synchronized void OnCapture1(int timeout) {

        NBioBSPJNI.FIR_HANDLE hCapturedFIR, hAuditFIR;
        NBioBSPJNI.CAPTURED_DATA capturedData;

        hCapturedFIR = bsp.new FIR_HANDLE();
        hAuditFIR = bsp.new FIR_HANDLE();
        capturedData = bsp.new CAPTURED_DATA();

        bCapturedFirst = true;

        bsp.Capture(NBioBSPJNI.FIR_PURPOSE.ENROLL, hCapturedFIR, timeout, hAuditFIR, capturedData, mCallback, 0, null);

        if (bsp.IsErrorOccured() == false)  {
            NBioBSPJNI.FIR_TEXTENCODE textSavedFIR;
            textSavedFIR = bsp.new FIR_TEXTENCODE();
            bsp.GetTextFIRFromHandle(hCapturedFIR, textSavedFIR);
            sss1 = textSavedFIR;

            SharedPreferences sharedPreferences = getSharedPreferences("private",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("key",sss1.TextFIR);
            editor.apply();

        }

        if (sampleDialogFragment != null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
            sampleDialogFragment.dismiss();

        if (bsp.IsErrorOccured()) {
            msg = "NBioBSP Capture Error: " + bsp.GetErrorCode();
        } else {
            NBioBSPJNI.INPUT_FIR inputFIR;

            inputFIR = bsp.new INPUT_FIR();

            // Make ISO 19794-2 data
            {
                NBioBSPJNI.Export.DATA exportData;

                inputFIR.SetFIRHandle(hCapturedFIR);

                exportData = exportEngine.new DATA();

                exportEngine.ExportFIR(inputFIR, exportData, NBioBSPJNI.EXPORT_MINCONV_TYPE.OLD_FDA);

                if (bsp.IsErrorOccured()) {
                    runOnUiThread(new Runnable() {

                        public void run() {
                            msg = "NBioBSP ExportFIR Error: " + bsp.GetErrorCode();

                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                if (byTemplate1 != null)
                    byTemplate1 = null;

                byTemplate1 = new byte[exportData.FingerData[0].Template[0].Data.length];
                byTemplate1 = exportData.FingerData[0].Template[0].Data;
            }

            // Make Raw Image data
            {
                NBioBSPJNI.Export.AUDIT exportAudit;

                inputFIR.SetFIRHandle(hAuditFIR);

                exportAudit = exportEngine.new AUDIT();

                exportEngine.ExportAudit(inputFIR, exportAudit);

                if (bsp.IsErrorOccured()) {

                    runOnUiThread(new Runnable() {

                        public void run() {
                            msg = "NBioBSP ExportAudit Error: " + bsp.GetErrorCode();

                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });

                    return;
                }

                if (byCapturedRaw1 != null)
                    byCapturedRaw1 = null;


                byCapturedRaw1 = new byte[exportAudit.FingerData[0].Template[0].Data.length];
                byCapturedRaw1 = exportAudit.FingerData[0].Template[0].Data;

                nCapturedRawWidth1 = exportAudit.ImageWidth;
                nCapturedRawHeight1 = exportAudit.ImageHeight;

                msg = "First Capture Success";

                NBioBSPJNI.NFIQInfo info = bsp.new NFIQInfo();
                info.pRawImage = byCapturedRaw1;
                info.nImgWidth = nCapturedRawWidth1;
                info.nImgHeight = nCapturedRawHeight1;


                if (bsp.IsErrorOccured()) {
                    runOnUiThread(new Runnable() {

                        public void run() {
                            msg = "NBioBSP getNFIQInfoFromRaw Error: " + bsp.GetErrorCode();
                        }
                    });

                    return;
                }


                nFIQ = info.pNFIQ;
            }


        }


    }

    public synchronized void OnCapture2(int timeout) {


        NBioBSPJNI.FIR_HANDLE hCapturedFIR, hAuditFIR;
        NBioBSPJNI.CAPTURED_DATA capturedData;
        hCapturedFIR = bsp.new FIR_HANDLE();
        hAuditFIR = bsp.new FIR_HANDLE();
        capturedData = bsp.new CAPTURED_DATA();
        bCapturedFirst = false;

        bsp.Capture(NBioBSPJNI.FIR_PURPOSE.ENROLL, hCapturedFIR, timeout, hAuditFIR, capturedData, mCallback, 0, null);

        if (bsp.IsErrorOccured() == false)  {
            NBioBSPJNI.FIR_TEXTENCODE textSavedFIR;
            textSavedFIR = bsp.new FIR_TEXTENCODE();
            bsp.GetTextFIRFromHandle(hCapturedFIR, textSavedFIR);
            sss = textSavedFIR;

        }

/*
        Bitmap ssss = capturedData.getImage();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ssss.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        writeToSDFile("" + encoded);*/

        if (bsp.IsErrorOccured()) {
            msg = "NBioBSP Capture Error: " + bsp.GetErrorCode();
        } else {
            NBioBSPJNI.INPUT_FIR inputFIR;

            inputFIR = bsp.new INPUT_FIR();

            // Make ISO 19794-2 data
            {
                NBioBSPJNI.Export.DATA exportData;
                inputFIR.SetFIRHandle(hCapturedFIR);
                exportData = exportEngine.new DATA();
                exportEngine.ExportFIR(inputFIR, exportData, NBioBSPJNI.EXPORT_MINCONV_TYPE.OLD_FDA);

                if (bsp.IsErrorOccured()) {
                    runOnUiThread(new Runnable() {

                        public void run() {
                            msg = "NBioBSP ExportFIR Error: " + bsp.GetErrorCode();

                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                if (byTemplate2 != null)
                    byTemplate2 = null;

                byTemplate2 = new byte[exportData.FingerData[0].Template[0].Data.length];
                byTemplate2 = exportData.FingerData[0].Template[0].Data;
            }

            // Make Raw Image data
            {
                NBioBSPJNI.Export.AUDIT exportAudit;

                inputFIR.SetFIRHandle(hAuditFIR);

                exportAudit = exportEngine.new AUDIT();

                exportEngine.ExportAudit(inputFIR, exportAudit);

                if (bsp.IsErrorOccured()) {

                    runOnUiThread(new Runnable() {

                        public void run() {
                            msg = "NBioBSP ExportAudit Error: " + bsp.GetErrorCode();
                            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();

                        }
                    });

                    return;
                }

                if (byCapturedRaw2 != null)
                    byCapturedRaw2 = null;

                byCapturedRaw2 = new byte[exportAudit.FingerData[0].Template[0].Data.length];
                byCapturedRaw2 = exportAudit.FingerData[0].Template[0].Data;

                nCapturedRawWidth2 = exportAudit.ImageWidth;
                nCapturedRawHeight2 = exportAudit.ImageHeight;

                msg = "second Capture Success";
            }

        }


    }


    public void OnBtnVerifyRaw() {

        NBioBSPJNI.INPUT_FIR inputFIR = bsp.new INPUT_FIR();
        NBioBSPJNI.INPUT_FIR inputFIR2 = bsp.new INPUT_FIR();
        Boolean bResult = new Boolean(false);
        NBioBSPJNI.FIR_PAYLOAD payload = bsp.new FIR_PAYLOAD();

        SharedPreferences sharedPreferences = getSharedPreferences("private",MODE_PRIVATE);
        String key = sharedPreferences.getString("key",null);

        NBioBSPJNI.FIR_TEXTENCODE textSavedFIR;
        textSavedFIR = bsp.new FIR_TEXTENCODE();
        textSavedFIR.TextFIR = key;

        inputFIR.SetTextFIR(sss);
        inputFIR2.SetTextFIR(sss1);


        bsp.VerifyMatch(inputFIR, inputFIR2, bResult, payload);
        if (bsp.IsErrorOccured() == false) {
            if (bResult)

                // labelStatus.setText("Verify OK - Payload: "+payload.GetText());
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            else
                // labelStatus.setText("Verify Failed");
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
        }
    }



}
