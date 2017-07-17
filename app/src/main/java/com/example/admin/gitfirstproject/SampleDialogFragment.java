/*
 * COPYRIGHT(c) UNIONCOMMUNITY 2013
 * This software is the proprietary information of UNIONCOMMUNITY
 *
 */
package com.example.admin.gitfirstproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * com.nitgen.SDK.sample
 * SampleDialogFragment.java
 *
 *@author : KimDoHyun ( rkwkgo@unioncomm.co.kr ) 
 *@since : 2013. 8. 1.
 *update history 
 *-------------------------------------------------
 *@editor : 
 *@edit date : 
 *@edit content :
 *-------------------------------------------------
 */
public class SampleDialogFragment extends DialogFragment {
	
	ProgressDialog progressDialog;
	
	public interface SampleDialogListener{
		
		public void onClickStopBtn(DialogFragment dialogFragment);
		
	}
	
	SampleDialogListener sampleDialogListener;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try{			
			sampleDialogListener = (SampleDialogListener)activity;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
		
	}
	
	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		if("DIALOG_TYPE_PROGRESS".equalsIgnoreCase(this.getTag())){
			
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage("Loading...");
			progressDialog.setIndeterminate(true);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(false);
			return progressDialog;
		}else{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder
			.setTitle("AUTO ON")
			.setPositiveButton("STOP", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					
					sampleDialogListener.onClickStopBtn(SampleDialogFragment.this);
					
				}
			});
			
			return builder.create();
		}
			
	}

}