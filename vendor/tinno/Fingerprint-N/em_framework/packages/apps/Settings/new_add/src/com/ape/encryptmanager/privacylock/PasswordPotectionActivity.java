package com.ape.encryptmanager.privacylock;

import com.ape.encryptmanager.service.EncryptServiceUtil;
import com.ape.encryptmanager.utils.EncryUtil;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.util.DisplayMetrics;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.android.settings.R;

public class PasswordPotectionActivity extends Activity {

	public static final int MX_COUNT =20;
	private String[] questionArr;
	private TextView mSpinner;

	private TextView mAnswerText;
	private TextView mAnswerCountText;
	private View questionLlayout;
	private EditText  mEditText;
	private Button  mButton;
	private int currentlyQuestion=0;
	private int resultCode = 0;
	private final int defaultValue = 0;
	private EncryptServiceUtil mEncryptServiceUtil;
	public static final String QUESTION_KEY = "QUESTION_KEY";
	public static final String ANSWER_KEY = "ANSWER_KEY";
	public static final int CHANGE_PASSWORD_POTECTION = 1101;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setSoftInputMode();
		setContentView(R.layout.encrypt_password_potection);
		questionArr =getResources().getStringArray(R.array.spinner_array);
		ActionBar actionBar = getActionBar();
		mEncryptServiceUtil = EncryptServiceUtil.getInstance(this);
		actionBar.setTitle(R.string.password_potection_activity_name);
		actionBar.setDisplayHomeAsUpEnabled(true);

		initUI();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			resultCode = bundle.getInt(EncryUtil.REQ_KEY, defaultValue);
		}

	}

	private void initUI(){

		mSpinner = (TextView)findViewById(R.id.spinner);
		mEditText = (EditText)findViewById(R.id.edittext);
		mButton = (Button) findViewById(R.id.button_complete);
		questionLlayout = (View) findViewById(R.id.question_layout);
		mAnswerText = (TextView) findViewById(R.id.answer_text);
		mAnswerCountText = (TextView) findViewById(R.id.answer_word_count);
		mAnswerText.setVisibility(View.GONE);
		mAnswerCountText.setVisibility(View.GONE);
		mEditText.addTextChangedListener(mTextWatcher);

		mButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String temp = mEditText.getText().toString();
				if (temp != null && temp.length() > 0) {
					if (resultCode == CHANGE_PASSWORD_POTECTION) {
						String Result = EncryptServiceUtil.getInstance(PasswordPotectionActivity.this).changePasswordQuestion(currentlyQuestion, temp);
						Intent intent = getIntent();
						Bundle data = new Bundle();
						data.putString(EncryUtil.RESULT_KEY, Result);
						intent.putExtras(data);
						PasswordPotectionActivity.this.setResult(resultCode, intent);
						PasswordPotectionActivity.this.finish();
					} else {
						Intent intent = getIntent();
						Bundle data = new Bundle();
						data.putInt(QUESTION_KEY, currentlyQuestion);// currentlyQuestion);
						data.putString(ANSWER_KEY, mEditText.getText().toString());
						intent.putExtras(data);
						PasswordPotectionActivity.this.setResult(resultCode, intent);
						PasswordPotectionActivity.this.finish();
					}
				}else if(temp ==null ||  temp.length()<=0){
					Toast  toast  = Toast.makeText(PasswordPotectionActivity.this, R.string.check_password_null, Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});
		mSpinner.setText(questionArr[0]);
		questionLlayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SecureQuestionDialog Dialog = new SecureQuestionDialog(PasswordPotectionActivity.this, currentlyQuestion, questionArr,mHandler);
				Dialog.show();
			}
		});

	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(CHANGE_PASSWORD_POTECTION == resultCode){
			cancel();
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

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
		PasswordPotectionActivity.this.setResult(resultCode, intent);
		PasswordPotectionActivity.this.finish();

	}

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case SecureQuestionDialog.SECURE_QUESTION_MSG:
					int index = msg.arg1;
					if(index>=0 && index<questionArr.length){
						mSpinner.setText(questionArr[index]);
						currentlyQuestion = index;
					}
					break;
				default:
					break;
			}
		}

	};

	private void setSoftInputMode() {
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		float density = metrics.density;
		if (density > (float) 0.75) {
			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
	}

	TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			String text=null;
			if(s !=null ){
				text = s.toString();
			}
			if(text !=null &&( text.length()>0)){
				mAnswerText.setVisibility(View.VISIBLE);
				mAnswerCountText.setVisibility(View.VISIBLE);
				mAnswerCountText.setText(""+text.length()+"/"+MX_COUNT);
			}else{
				mAnswerText.setVisibility(View.GONE);
				mAnswerCountText.setVisibility(View.GONE);
				mAnswerCountText.setText("");
			}

		}
	};

}
