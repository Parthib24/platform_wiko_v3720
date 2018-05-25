package com.ape.encryptmanager.privacylock;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.settings.R;
import com.ape.encryptmanager.service.EncryptServiceUtil;
import com.ape.encryptmanager.utils.EncryUtil;

public class ForgetPasswordActivity extends Activity {
        

	private String[] questionArr;
	private int questionid=0;
	private EditText mQuestionTitle;
	private EditText mInputPassword;
	private Button mComplete;
//	private TextView mClew;
       //private EncryptServiceUtil mEncryptServiceUtil;
	private int resultCode = 0;
	private final int defaultValue = 0;
    private  boolean isMyosUi =  SystemProperties.getBoolean("ro.pt.myos.ui_support",false);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        if (isMyosUi){
            int layoutId = getResources().getIdentifier("myos_forget_password_activity_layout", "layout", getPackageName());
            //setContentView(R.layout.myos_forget_password_activity_layout);
            setContentView(layoutId);
        }else {
            setContentView(R.layout.forget_password_activity_layout);
        }
		//mEncryptServiceUtil = EncryptServiceUtil.getInstance(this);
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		questionArr =getResources().getStringArray(R.array.spinner_array);
		questionid = EncryptServiceUtil.getInstance(this).getPasswordQuestionID();
		if(questionid < 0){
		     questionid = 0;
	   }
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.answer_password_question);
		actionBar.setDisplayHomeAsUpEnabled(true); 
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			resultCode = bundle.getInt(EncryUtil.REQ_KEY, defaultValue);
		}
		initUI();
	}

	private void initUI() {
		  mQuestionTitle = (EditText)findViewById(R.id.question_title);
		  mQuestionTitle.setText(questionArr[questionid]);
		  mQuestionTitle.setEnabled(false);
		  mQuestionTitle.setFocusable(false);
		  mInputPassword = (EditText)findViewById(R.id.input_password);
		  mInputPassword.setFocusable(true);
		  mComplete = (Button)findViewById(R.id.button_complete);
	//	  mClew = (TextView)findViewById(R.id.textview_show_clew);
		//  mInputPassword.addTextChangedListener(Password);
		  mComplete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String temp = mInputPassword.getText().toString();
				if(temp!=null && temp.length()>0){
					if(EncryptServiceUtil.getInstance(ForgetPasswordActivity.this).checkPasswordQuestion(temp)){
						Intent i = new Intent();
					//	i.setAction("action.settings.encryptmanager.setting.activity");
						i.setClassName(ForgetPasswordActivity.this, "com.ape.encryptmanager.privacylock.EncryptSettingActivity");
						Bundle data = new Bundle();
						data.putInt(EncryUtil.REQ_KEY ,EncryptSettingActivity.CHANGE_PASSWORD_CODE);
						i.putExtras(data);
						startActivityForResult(i, resultCode);	
					}else{
						//mClew.setVisibility(View.VISIBLE);
						Toast  toast  = Toast.makeText(ForgetPasswordActivity.this, R.string.check_password_error, Toast.LENGTH_SHORT);
						   toast.show();
						   mInputPassword.setText("");
					}
				}else if(temp ==null ||  temp.length()<=0){
                                        Toast  toast  = Toast.makeText(ForgetPasswordActivity.this, R.string.check_password_null, Toast.LENGTH_SHORT);
                                        toast.show();
                                }
			}
		});
      
	}
/*
	private TextWatcher Password = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			if(count>0){
				mComplete.setEnabled(true);
			}else{
			mComplete.setEnabled(false);
			}
			mClew.setVisibility(View.INVISIBLE);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}
	};
	*/
	
	@Override
	public void onBackPressed() {
		cancel();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
		        cancel();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void cancel() {
		
		Intent intent = getIntent();
		Bundle data = new Bundle();
		intent.putExtras(data); 
		ForgetPasswordActivity.this.setResult(resultCode, intent);
		ForgetPasswordActivity.this.finish();
}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		

		// TODO Auto-generated method stub
		// super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == this.resultCode && resultCode == EncryptSettingActivity.CHANGE_PASSWORD_CODE) {
			Bundle bundle = data.getExtras();
			String answer = bundle.getString(EncryUtil.RESULT_KEY, null);
			Intent intent = getIntent();
			Bundle bundle1 = new Bundle();
			bundle1.putString(EncryUtil.RESULT_KEY, answer);
			intent.putExtras(bundle1);
			ForgetPasswordActivity.this.setResult(this.resultCode, intent);
			ForgetPasswordActivity.this.finish();

		}
	
	}
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
