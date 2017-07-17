package com.example.admin.gitfirstproject;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.admin.gitfirstproject.database.DBAdapter;
import com.nitgen.SDK.AndroidBSP.NBioBSPJNI;
import com.nitgen.SDK.AndroidBSP.StaticVals;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Admin on 12-07-2017.
 */
public class EnrollmentActivity extends AppCompatActivity {

    EditText emailId, userName, password, confirmPassword;
    Button captureImage, submitBTN, openDevice;
    ImageView imageView;


    private NBioBSPJNI bsp;
    private NBioBSPJNI.Export exportEngine;
    private byte[] byTemplate1;
    private byte[] byCapturedRaw1;
    private int nCapturedRawWidth1;
    private int nCapturedRawHeight1;


   // DialogFragment sampleDialogFragment;
    UserDialog userDialog;
    public static final int QUALITY_LIMIT = 60;
    String msg = "";

    NBioBSPJNI.FIR_TEXTENCODE savedData = null;

    DBAdapter db;
    Button loginBTN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        db = new DBAdapter(EnrollmentActivity.this);
        db.open();

        openDevice = (Button) findViewById(R.id.btnOpenDevice);

        loginBTN = (Button)findViewById(R.id.gotologin);
        loginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        userName = (EditText) findViewById(R.id.user_name);
        emailId = (EditText) findViewById(R.id.email_id);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirm_password);
        imageView = (ImageView) findViewById(R.id.capture_image);
        submitBTN = (Button) findViewById(R.id.finger_print_registration);
        captureImage = (Button) findViewById(R.id.capture_fingerprint_btn);



        captureImage.setOnClickListener(new View.OnClickListener() {
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

        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitMethod();
            }
        });

    }

    public void OnBtnOpenDevice() {

     //   sampleDialogFragment.show(getSupportFragmentManager(), "DIALOG_TYPE_PROGRESS");
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

      //  sampleDialogFragment = new SampleDialogFragment();
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
           /* if (sampleDialogFragment != null)
                sampleDialogFragment.dismiss();
*/
            String message = "Device Open Success : ";
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

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

    public void submitMethod() {

        String userN = userName.getText().toString().trim();
        String ema = emailId.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();
        if (userN == null || userN.length() < 3) {
            Toast.makeText(getApplicationContext(), "Please enter User Name", Toast.LENGTH_SHORT).show();
        } else if (ema == null || ema.length() < 3) {
            Toast.makeText(getApplicationContext(), "Please enter email id", Toast.LENGTH_SHORT).show();
        } else if (pass == null || pass.length() < 5) {
            Toast.makeText(getApplicationContext(), "Please enter Password", Toast.LENGTH_SHORT).show();
        } else if (confirmPass == null || !pass.equalsIgnoreCase(confirmPass)) {
            Toast.makeText(getApplicationContext(), "Please enter confirm password same as password", Toast.LENGTH_SHORT).show();
        } else if (savedData == null) {
            Toast.makeText(getApplicationContext(), "Please capture finger print", Toast.LENGTH_SHORT).show();
        } else {


            SQLiteDatabase SqliteDB = db.getSQLiteDatabase();
            SqliteDB.beginTransaction();


            db.db.execSQL("delete from " + DBAdapter.TABLE_ENROLLMENT);
            String insertUserInformations = "INSERT INTO " + DBAdapter.TABLE_ENROLLMENT + "(" + DBAdapter.USER_EMAIL + ","
                    + DBAdapter.USER_NAME + ","
                    + DBAdapter.USER_PASSWORD + ","
                    + DBAdapter.FINGERPRINT + ") VALUES (?,?,?,? )";

            SQLiteStatement insertVehicleStatement = SqliteDB.compileStatement(insertUserInformations);

            insertVehicleStatement.bindString(1, ema);
            insertVehicleStatement.bindString(2, userN);
            insertVehicleStatement.bindString(3, pass);
            insertVehicleStatement.bindString(4, savedData.TextFIR);

            insertVehicleStatement.execute();
            SqliteDB.setTransactionSuccessful();
            SqliteDB.endTransaction();

            new AlertDialog.Builder(EnrollmentActivity.this)
                    .setMessage("Finger print registered  successfully")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                            userName.setText("");
                            emailId.setText("");
                            password.setText("");
                            confirmPassword.setText("");
                            savedData = null;
                            imageView.setImageBitmap(null);

                        }
                    })
                    .show();


        }

    }


}
