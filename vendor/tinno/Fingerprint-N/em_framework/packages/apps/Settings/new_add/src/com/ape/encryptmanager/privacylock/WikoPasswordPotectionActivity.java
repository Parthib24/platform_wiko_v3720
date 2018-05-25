package com.ape.encryptmanager.privacylock;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;
import com.ape.encryptmanager.service.EncryptServiceUtil;
import com.ape.encryptmanager.utils.EncryUtil;
import com.ape.emFramework.Log;

public class WikoPasswordPotectionActivity extends Activity  implements
View.OnClickListener {


	public static final String TAG = "WikoPasswordPotectionActivity";
	private String[] questionArr;
	private TextView mSpinner;
	private EditText mEditText;
	private View questionLlayout;
	private View mMainRootLayout;
	private View mheaderLayout;
	private ImageView iconImage;
	private TextView headerTitle;
	private TextView headerSummary;
	private LinearLayout rightButtonLayout;
	private LinearLayout leftButtonLayout;
	private ImageView progressImage1;
	private ImageView progressImage2;
	private ImageView progressImage3;
	private ImageView rightButtonImage;
	private TextView rightButtonText;
	private TextView countTextView;

	private int currentlyQuestion = 0;
	private int resultCode = 0;
	private final int defaultValue = 0;
	private EncryptServiceUtil mEncryptServiceUtil;
	public static final String QUESTION_KEY = "QUESTION_KEY";
	public static final String ANSWER_KEY = "ANSWER_KEY";
	public static final int CHANGE_PASSWORD_POTECTION = 1101;

	public static final int MSG_LAYOUT_GONE = 0X01;
	public static final int MSG_LAYOUT_VISIBLE = 0X02;

	private int screenHeight = 0;
	private int keyHeight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setSoftInputMode();
		setContentView(R.layout.wiko_encrypt_password_potection);

		questionArr = getResources().getStringArray(R.array.spinner_array);
		ActionBar actionBar = getActionBar();
		mEncryptServiceUtil = EncryptServiceUtil.getInstance(this);
		if(actionBar!=null) {
			//if(!MainFeatureOptions.isAppLockSupported(this)){
			if(true){
				actionBar.setTitle(R.string.file_lock_title);

			}else {
				actionBar.setTitle(R.string.app_and_file_lock_title);
			}
		}
		//actionBar.setTitle(R.string.app_and_file_lock_title);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setElevation(0);

		initUI();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			resultCode = bundle.getInt(EncryUtil.REQ_KEY, defaultValue);
		}
		screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
		keyHeight = screenHeight / 3;

	}

	private void initUI() {
		mMainRootLayout = (View) findViewById(R.id.main_root_layout);
		mheaderLayout = (View) findViewById(R.id.header_layout);
		mSpinner = (TextView) findViewById(R.id.spinner);
		mEditText = (EditText) findViewById(R.id.edittext);
		mEditText.addTextChangedListener(mTextWatcher);
		countTextView = (TextView) findViewById(R.id.answer_word_count);
		questionLlayout = (View) findViewById(R.id.question_layout);
		questionLlayout.setOnClickListener(this);
		iconImage = (ImageView) findViewById(R.id.header_icon);
		headerTitle = (TextView) findViewById(R.id.header_title);
		headerSummary = (TextView) findViewById(R.id.header_summary);
		rightButtonLayout = (LinearLayout) findViewById(R.id.right_button_layout);
		leftButtonLayout = (LinearLayout) findViewById(R.id.left_button_layout);
		progressImage1 = (ImageView) findViewById(R.id.progress_1);
		progressImage2 = (ImageView) findViewById(R.id.progress_2);
		progressImage3 = (ImageView) findViewById(R.id.progress_3);
		rightButtonImage = (ImageView) findViewById(R.id.right_image);
		rightButtonText = (TextView) findViewById(R.id.right_text);
		leftButtonLayout.setVisibility(View.VISIBLE);
		rightButtonLayout.setVisibility(View.VISIBLE);
		rightButtonLayout.setOnClickListener(this);
		leftButtonLayout.setOnClickListener(this);
		rightButtonLayout.setClickable(false);

		iconImage.setImageResource(R.drawable.boot_guide_header_icon_question);

		progressImage1.setImageResource(R.drawable.indicator_other);
		progressImage2.setImageResource(R.drawable.indicator_other);
		progressImage3.setImageResource(R.drawable.indicator_current);

		headerTitle.setText(R.string.set_secure_question);
		headerSummary.setText(R.string.set_secure_question_summary);
		setRightButtonStyles(false);
		mSpinner.setText(questionArr[currentlyQuestion]);

		mMainRootLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				// TODO Auto-generated method stub

				Log.d(TAG, "----------screenHeight = " + screenHeight);
				Log.d(TAG, "----------keyHeight = " + keyHeight);
				Log.d(TAG, "----------oldBottom - bottom = " + (oldBottom - bottom));

				if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
					mHandler.sendEmptyMessage(MSG_LAYOUT_GONE);
                    /*
                     * if((oldBottom - bottom) > screenHeight*7/20){
                     * mHandler.sendEmptyMessage(MSG_LAYOUT_GONE); }else{
                     * mHandler.sendEmptyMessage(MSG_LAYOUT_VISIBLE); }
                     */
				} else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
					mHandler.sendEmptyMessage(MSG_LAYOUT_VISIBLE);

				}

			}
		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Editable edit = mEditText.getText();
		if (edit != null && edit.length() > 0 && edit.length() <= 20) {
			rightButtonLayout.setClickable(true);
			setRightButtonStyles(true);
			countTextView.setText("" + edit.length() + "/20");
		} else {
			rightButtonLayout.setClickable(false);
			setRightButtonStyles(false);
			countTextView.setText("0/20");
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (CHANGE_PASSWORD_POTECTION == resultCode) {
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
		WikoPasswordPotectionActivity.this.setResult(resultCode, intent);
		WikoPasswordPotectionActivity.this.finish();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.right_button_layout:
				String temp = mEditText.getText().toString();
				if (temp != null && temp.length() > 0 && temp.length() <= 20) {
					if (resultCode == CHANGE_PASSWORD_POTECTION) {
						String Result = mEncryptServiceUtil.changePasswordQuestion(currentlyQuestion, temp);
						Intent intent = getIntent();
						Bundle data = new Bundle();
						data.putString(EncryUtil.RESULT_KEY, Result);
						intent.putExtras(data);
						WikoPasswordPotectionActivity.this.setResult(resultCode, intent);
						WikoPasswordPotectionActivity.this.finish();
					} else {
						Intent intent = getIntent();
						Bundle data = new Bundle();
						data.putInt(QUESTION_KEY, currentlyQuestion);// currentlyQuestion);
						data.putString(ANSWER_KEY, mEditText.getText().toString());
						intent.putExtras(data);
						WikoPasswordPotectionActivity.this.setResult(resultCode, intent);
						WikoPasswordPotectionActivity.this.finish();
					}
				}

				break;
			case R.id.left_button_layout:
				cancel();
				break;

			case R.id.question_layout:
				SecureQuestionDialog Dialog = new SecureQuestionDialog(WikoPasswordPotectionActivity.this, currentlyQuestion, questionArr, mHandler);
				Dialog.show();
				break;
			default:
				break;
		}
	}

	TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			String text = null;
			if (s != null) {
				text = s.toString();
			}
			if (text != null && (text.length() > 0 && text.length() <= 20)) {
				rightButtonLayout.setClickable(true);
				setRightButtonStyles(true);
				countTextView.setText("" + text.length() + "/20");
			} else {
				rightButtonLayout.setClickable(false);
				setRightButtonStyles(false);
				countTextView.setText("0/20");
			}

		}
	};

	private void setRightButtonStyles(Boolean clickable) {

		if (clickable) {
			rightButtonImage.setImageResource(R.drawable.indicator_next);
			rightButtonText.setTextColor(Color.rgb(0x00, 0x00, 0x00));
		} else {
			rightButtonImage.setImageResource(R.drawable.indicator_next_disable);
			rightButtonText.setTextColor(Color.rgb(0xa4, 0xa6, 0xa7));
		}

	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case SecureQuestionDialog.SECURE_QUESTION_MSG:
					int index = msg.arg1;
					if (index >= 0 && index < questionArr.length) {
						mSpinner.setText(questionArr[index]);
						currentlyQuestion = index;
					}
					break;
				case MSG_LAYOUT_VISIBLE:
					if (mheaderLayout.getVisibility() != View.VISIBLE) {
						mheaderLayout.setVisibility(View.VISIBLE);
					}
					break;
				case MSG_LAYOUT_GONE:
					if (mheaderLayout.getVisibility() != View.GONE) {
						mheaderLayout.setVisibility(View.GONE);
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
}
