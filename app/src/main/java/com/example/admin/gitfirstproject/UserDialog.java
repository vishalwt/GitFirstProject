/*
 * COPYRIGHT(c) UNIONCOMMUNITY 2013
 * This software is the proprietary information of UNIONCOMMUNITY
 *
 */
package com.example.admin.gitfirstproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

/**
 * com.nitgen.SDK.AndroidBSP
 * UserDialog.java
 *
 *@author : Kim Do Hyun ( rkwkgo@unioncomm.co.kr ) 
 *@since : 2013. 9. 6.
 *update history 
 *-------------------------------------------------
 *@editor : 
 *@edit date : 
 *@edit content :
 *-------------------------------------------------
 */
public class UserDialog extends DialogFragment{
	
	public interface UserDialogListener{
		
		public void onClickPositiveBtn(DialogFragment dialogFragment, String id);
		
	}
	
	UserDialogListener userDialogListener;

	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		try{			
			userDialogListener = (UserDialogListener)activity;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
		
	}
	
	
	EditText edit_id;
	
	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		edit_id = new EditText(getActivity());
		edit_id.setInputType(InputType.TYPE_CLASS_NUMBER);
		edit_id.setHint("Input your ID");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder
		.setView(edit_id)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
									
				userDialogListener.onClickPositiveBtn(UserDialog.this, edit_id.getText().toString());
				
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
			
				
			}
		});
		
		return builder.create();
		
	}

}
