package com.example.admin.gitfirstproject;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.admin.gitfirstproject.database.DBAdapter;
import com.nitgen.SDK.AndroidBSP.NBioBSPJNI;
import com.nitgen.SDK.AndroidBSP.StaticVals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Admin on 13-07-2017.
 */
public class LoginActivity extends AppCompatActivity {

    DBAdapter db;
    Button capture,openDevice;
    EditText userEmail;
    Button loginBTN;
    ImageView imageView;


    private NBioBSPJNI bsp;
    private NBioBSPJNI.Export exportEngine;

    //DialogFragment sampleDialogFragment;
    UserDialog userDialog;
    public static final int QUALITY_LIMIT = 60;
    String msg = "";
    NBioBSPJNI.FIR_TEXTENCODE savedData = null;


    Button register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        register = (Button)findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),EnrollmentActivity.class);
                startActivity(intent);
            }
        });
        capture = (Button)findViewById(R.id.capture_fingerprint_btn);
        imageView  = (ImageView)findViewById(R.id.capture_image);
        userEmail = (EditText)findViewById(R.id.email_id);
        openDevice = (Button)findViewById(R.id.btnOpenDevice);
        loginBTN = (Button)findViewById(R.id.login_btn);

        db = new DBAdapter(LoginActivity.this);
        db.open();


        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnCapture1(10000);
            }
        });

        initData();

        openDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnBtnOpenDevice();
            }
        });

        loginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginMethod();
            }
        });
    }


    public void OnBtnOpenDevice() {

      //  sampleDialogFragment.show(getSupportFragmentManager(), "DIALOG_TYPE_PROGRESS");
        bsp.OpenDevice();

    }

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

            }

        }

       // sampleDialogFragment = new SampleDialogFragment();
        userDialog = new UserDialog();

    }


    @Override
    public void onDestroy() {

        if (bsp != null) {
            bsp.dispose();
            bsp = null;
        }
        super.onDestroy();

    }


    NBioBSPJNI.CAPTURE_CALLBACK mCallback = new NBioBSPJNI.CAPTURE_CALLBACK() {

        public void OnDisConnected() {
            NBioBSPJNI.CURRENT_PRODUCT_ID = 0;
/*
            if (sampleDialogFragment != null)
                sampleDialogFragment.dismiss();*/

            String message = "NBioBSP Disconnected: " + bsp.GetErrorCode();

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        }

        public void OnConnected() {
          /*  if (sampleDialogFragment != null)
                sampleDialogFragment.dismiss();
*/
            String message = "Device Open Success : ";

            ByteBuffer deviceId = ByteBuffer.allocate(StaticVals.wLength_GET_ID);
            deviceId.order(ByteOrder.BIG_ENDIAN);
            bsp.getDeviceID(deviceId.array());

            if (bsp.IsErrorOccured()) {
                msg = "NBioBSP GetDeviceID Error: " + bsp.GetErrorCode();

                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

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
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                return;
            }

            ByteBuffer getvalue = ByteBuffer.allocate(StaticVals.wLength_GET_VALUE);
            getvalue.order(ByteOrder.BIG_ENDIAN);
            bsp.getValue(getvalue.array());

            if (bsp.IsErrorOccured()) {
                msg = "NBioBSP GetValue Error: " + bsp.GetErrorCode();
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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

                imageView.setImageBitmap(capturedData.getImage());

            }

            if (capturedData.getImageQuality() >= QUALITY_LIMIT) {
               /* if (sampleDialogFragment != null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
                    sampleDialogFragment.dismiss();*/
                return NBioBSPJNI.ERROR.NBioAPIERROR_USER_CANCEL;
            } else if (capturedData.getDeviceError() != NBioBSPJNI.ERROR.NBioAPIERROR_NONE) {
               /* if (sampleDialogFragment != null && "DIALOG_TYPE_PROGRESS".equals(sampleDialogFragment.getTag()))
                    sampleDialogFragment.dismiss();*/
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


        bsp.Capture(NBioBSPJNI.FIR_PURPOSE.ENROLL, hCapturedFIR, timeout, hAuditFIR, capturedData, mCallback, 0, null);

        if (bsp.IsErrorOccured() == false) {
            NBioBSPJNI.FIR_TEXTENCODE textSavedFIR;
            textSavedFIR = bsp.new FIR_TEXTENCODE();
            bsp.GetTextFIRFromHandle(hCapturedFIR, textSavedFIR);

            savedData = textSavedFIR;
        }

    }

    public void loginMethod(){

        String key = null;
        String name = null;
        String email = userEmail.getText().toString().trim();

        Cursor allUser = db.getAllUser();
        if (allUser.moveToFirst()) {
            do {
                name = allUser.getString(allUser.getColumnIndex(DBAdapter.USER_NAME));
                Log.v("lenght",""+name);
            } while (allUser.moveToNext());
        }

        Log.v("lenght",""+allUser.getCount());


        if (email == null || email.length() < 3) {
            Toast.makeText(getApplicationContext(), "Please enter User email", Toast.LENGTH_SHORT).show();
        }  else if (savedData == null) {
            Toast.makeText(getApplicationContext(), "Please capture finger print", Toast.LENGTH_SHORT).show();
        } else {


            NBioBSPJNI.INPUT_FIR inputFIR = bsp.new INPUT_FIR();
            NBioBSPJNI.INPUT_FIR inputFIR2 = bsp.new INPUT_FIR();
            Boolean bResult = new Boolean(false);
            NBioBSPJNI.FIR_PAYLOAD payload = bsp.new FIR_PAYLOAD();

           Cursor fingerCursor = db.getFingerPrintByEmail(email);
            if (fingerCursor.moveToFirst()) {
                do {
                    key = fingerCursor.getString(fingerCursor.getColumnIndex(DBAdapter.FINGERPRINT));
                    name = fingerCursor.getString(fingerCursor.getColumnIndex(DBAdapter.USER_NAME));
                } while (fingerCursor.moveToNext());
            }

            Log.v("lenght",""+fingerCursor.getCount());
           /* Cursor nameCursor = db.getUserByEmail(email);
            if (nameCursor.moveToFirst()) {
                do {
                    name = nameCursor.getString(nameCursor.getColumnIndex(DBAdapter.USER_NAME));
                } while (nameCursor.moveToNext());
            }*/

            Log.v("siuahksh",""+key);


            NBioBSPJNI.FIR_TEXTENCODE textSavedFIR;
            textSavedFIR = bsp.new FIR_TEXTENCODE();
            textSavedFIR.TextFIR = key;

            inputFIR.SetTextFIR(savedData);
            inputFIR2.SetTextFIR(textSavedFIR);


            bsp.VerifyMatch(inputFIR, inputFIR2, bResult, payload);
            if (bsp.IsErrorOccured() == false) {
                if (bResult)

                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage("Hi Dear! "+name+" welcome to WRMS.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();

                                }
                            })
                            .show();
                else
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
