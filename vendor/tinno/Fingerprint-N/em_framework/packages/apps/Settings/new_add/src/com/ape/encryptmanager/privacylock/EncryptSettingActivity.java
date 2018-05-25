package com.ape.encryptmanager.privacylock;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.settings.R;
import com.ape.encryptmanager.service.EncryptServiceUtil;
import com.ape.encryptmanager.utils.EncryUtil;

public class EncryptSettingActivity extends Activity  implements
View.OnClickListener {


	private static final String TAG = "EncryptSettingActivity";
//	private EditText password;
//	private EditText confirm;
//	private TextView clew;
	private Button next;
	private static final int MAXNUMBER =10;
	private int resultCode=0;
	private final int defaultValue = 0;
	public static final int CHANGE_PASSWORD_CODE = 1100;

    private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 1000;
    //private EncryptServiceUtil mEncryptServiceUtil;
    private TextView mHeaderTextView;
    private TextView mHeaderMianTextView;
    private TextView mEndTextrTextView;
    private TextView mNumberTextView;
    private TextView mheaderText_sub;
  //  private TextView mNumberTextShow;
    
    private TextView headerText_number;
    private View complete_LinearLayout;
    private Button complete;

   
	private LockPatternView mLockPatternView;
	private RelativeLayout numberLayout;
	private RelativeLayout patternLayout;	
	private int mActivityType=EncryUtil.PATTERN_PASSWORD;  // 0 is number password ,1 is Pattern password
	private Boolean secondPatternFlag = false;
	private Boolean secondNumberFlag = false;
	private String  patternContent ;
	
	private int   currentState =STATE_START  ;
	private static final int   STATE_START= 1;  //\u7b2c\u4e00\u6b21\u5f55\u5165\u72b6\u6001
	private static final int   STATE_SECOND  = 2;  //\u7b2c\u4e8c\u6b21\u5f55\u5165\u72b6\u6001
	private static final int   STATE_COMPLETE = 3;  //\u5b8c\u6210\u72b6\u6001
        private StringBuffer stringFirstBuffer = new StringBuffer();
        private StringBuffer stringAgainBuffer = new StringBuffer();
        private static final int   MAX_INPUT_NUMBER = 10;
        private static final int    HANDLER_SHOW_MSG = 1;
        private static final int HANDLER_PASS_MSG = 2;
        private static final int HANDLER_DELAYED_SHOW__MSG =3;
        private Boolean isCurrentprivateType = true;
        private String mResult =null;
        
        
        private int keypadDisplayID[] = { R.id.applock_keypad_display_1, 
                        R.id.applock_keypad_display_2, 
                        R.id.applock_keypad_display_3,
                        R.id.applock_keypad_display_4,
                        R.id.applock_keypad_display_5,
                        R.id.applock_keypad_display_6,
                        R.id.applock_keypad_display_7,
                        R.id.applock_keypad_display_8,
                        R.id.applock_keypad_display_9,
                        R.id.applock_keypad_display_10 }; 
        
        
    private enum Stage {
        NeedToUnlock,
        NeedToUnlockWrong,
        LockedOut
    }


	@Override
        protected void onCreate(Bundle savedInstanceState) {
                // TODO Auto-generated method stub
                super.onCreate(savedInstanceState);
                setContentView(R.layout.set_encrypt_activity_layout);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, 
                                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                ActionBar actionBar = getActionBar();
                actionBar.setTitle(R.string.set_encrypt_activity_name);
                actionBar.setDisplayHomeAsUpEnabled(true);

                //mEncryptServiceUtil = EncryptServiceUtil.getInstance(this);
                mActivityType = EncryptServiceUtil.getInstance(this).getPasswordType();
                Bundle bundle = getIntent().getExtras();
                if (bundle != null) {
                        resultCode = bundle.getInt(EncryUtil.REQ_KEY, defaultValue);
                        mActivityType = bundle.getInt(EncryUtil.PASSWORD_SETTING_TYPE_KEY, mActivityType);
                }
                init();
        }

        private void init() {
                next = (Button) findViewById(R.id.button_next);
                mNumberTextView = (TextView) findViewById(R.id.number_endText);
                mheaderText_sub = (TextView) findViewById(R.id.headerText_sub);
                headerText_number =  (TextView) findViewById(R.id.headerText_number);
                complete = (Button) findViewById(R.id.complete);
                complete_LinearLayout = (View) findViewById(R.id.complete_LinearLayout);
                complete.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                                // TODO Auto-generated method stub
                                if (CHANGE_PASSWORD_CODE == resultCode) {      
                                        Intent intent = getIntent();
                                        Bundle data = new Bundle();
                                        data.putString(EncryUtil.RESULT_KEY, mResult);
                                        intent.putExtras(data);
                                        EncryptSettingActivity.this.setResult(resultCode, intent);
                                        EncryptSettingActivity.this.finish();

                                } else {
                                        Intent i = new Intent(EncryptSettingActivity.this, PasswordPotectionActivity.class);
                                        Bundle data = new Bundle();
                                        data.putInt("REQ_KEY", resultCode);
                                        i.putExtras(data);
                                        startActivityForResult(i, resultCode);

                                }
                        }
                });
                
                ((ImageButton) findViewById(R.id.imagebutton_0)).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.imagebutton_1)).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.imagebutton_2)).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.imagebutton_3)).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.imagebutton_4)).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.imagebutton_5)).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.imagebutton_6)).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.imagebutton_7)).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.imagebutton_8)).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.imagebutton_9)).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.imagebutton_delete)).setOnClickListener(this);


                mHeaderTextView = (TextView) findViewById(R.id.headerText);
                mHeaderMianTextView = (TextView) findViewById(R.id.headerText_main);
                mHeaderMianTextView.setText(R.string.set_pattern_title_main);
                mEndTextrTextView = (TextView) findViewById(R.id.endText);
                mLockPatternView = (LockPatternView) findViewById(R.id.lockPattern);
                EncryUtil.setLockpatternViewAttrsValue(this,mLockPatternView);
                // final LinearLayoutWithDefaultTouchRecepient topLayout
                // = (LinearLayoutWithDefaultTouchRecepient)
                // findViewById(R.id.topLayout);
                // topLayout.setDefaultTouchRecepient(mLockPatternView);
                mLockPatternView.setTactileFeedbackEnabled(true);
                mLockPatternView.setOnPatternListener(mConfirmExistingLockPatternListener);
                showKeypadDisplay(0);
                numberLayout = (RelativeLayout) findViewById(R.id.number_password);
                patternLayout = (RelativeLayout) findViewById(R.id.pattern_password);

                next.setVisibility(View.GONE);
                next.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                                // TODO Auto-generated method stub
                                
                                secondNumberFlag = true;
                                mNumberTextView.setText(R.string.parrern_reset_title);
                                  mNumberTextView.setTextColor(Color.rgb(0x00, 0x96, 0x88));
                                  next.setVisibility(View.GONE);
                                  mNumberTextView.setVisibility(View.VISIBLE);
                                  showKeypadDisplay(0);
                                  headerText_number.setText(R.string.confirm_number_encrypt);
                                
                        }
                });

                mEndTextrTextView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                                // TODO Auto-generated method stub
                                if (currentState == STATE_START) { // \u5207\u6362\u5bc6\u7801\u9501\u6216\u8005\u56fe\u6848\u9501
                                        if (mActivityType == EncryUtil.NUMBER_PASSWORD) {
                                                mActivityType = EncryUtil.PATTERN_PASSWORD;

                                                mHeaderTextView.setVisibility(View.GONE);

                                                mEndTextrTextView.setTextColor(Color.rgb(0x00, 0x96, 0x88));
                                                mEndTextrTextView.setText(R.string.make_number_password_title);
                                        } else {
                                                mActivityType = EncryUtil.NUMBER_PASSWORD;
                                        }
                                        currentState = STATE_START;
                                        secondPatternFlag = false;
                                        changeActivityType(mActivityType);
                                } else if (currentState == STATE_SECOND) { // \u91cd\u7f6emHeaderTextView
                                        currentState = STATE_START;
                                        secondPatternFlag = false;
                                        mHeaderTextView.setText(R.string.set_pattern_title);
                                        mHeaderTextView.setVisibility(View.GONE);
                                        mHeaderMianTextView.setText(R.string.set_pattern_title_main);
                                        mEndTextrTextView.setTextColor(Color.rgb(0x00, 0x96, 0x88));
                                        mEndTextrTextView.setText(R.string.make_number_password_title);

                                } else if (currentState == STATE_COMPLETE) {// \u5b8c\u6210
                                        
                                }

                        }
                });

                mNumberTextView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                                // TODO Auto-generated method stub
                                if (secondNumberFlag) {
                                        mNumberTextView.setText(R.string.make_pattern_password_title);
                                        mNumberTextView.setTextColor(Color.rgb(0x00, 0x96, 0x88));
                                        showKeypadDisplay(0);
                                        headerText_number.setText(R.string.set_number_title_main);
                                } else {

                                        if (mActivityType == EncryUtil.NUMBER_PASSWORD) {
                                                mActivityType = EncryUtil.PATTERN_PASSWORD;
                                        } else {
                                                mActivityType = EncryUtil.NUMBER_PASSWORD;
                                        }
                                        changeActivityType(mActivityType);
                                }
                                secondNumberFlag = false;
                                stringFirstBuffer = new StringBuffer();
                                stringAgainBuffer = new StringBuffer();
                                showKeypadDisplay(0);
                        }
                });

                changeActivityType(mActivityType);
        }
	
        
        @Override
        public void onClick(View v) {
                // TODO Auto-generated method stub
                char c = 0;
                mHandler.removeMessages(HANDLER_SHOW_MSG);
                mheaderText_sub.setVisibility(View.GONE);
              if(  mHandler.hasMessages(HANDLER_PASS_MSG)){
                      return;
              }
                switch (v.getId()) {
                case R.id.imagebutton_0:
                        c = '0';
                        break;
                case R.id.imagebutton_1:
                        c = '1';
                        break;
                case R.id.imagebutton_2:
                        c = '2';
                        break;
                case R.id.imagebutton_3:
                        c = '3';
                        break;
                case R.id.imagebutton_4:
                        c = '4';
                        break;
                case R.id.imagebutton_5:
                        c = '5';
                        break;
                case R.id.imagebutton_6:
                        c = '6';
                        break;
                case R.id.imagebutton_7:
                        c = '7';
                        break;
                case R.id.imagebutton_8:
                        c = '8';
                        break;
                case R.id.imagebutton_9:
                        c = '9';
                        break;
                case R.id.imagebutton_delete:
                        c = ' ';
                        break;
                default:
                        break;
                }
                if (secondNumberFlag) { // \u7b2c\u4e8c\u6b21
                        if (c >= '0' && c <= '9' && stringAgainBuffer.length() < stringFirstBuffer.length()) {
                                stringAgainBuffer.append(c);
                                if (stringAgainBuffer.length() == stringFirstBuffer.length()
                                                &&stringAgainBuffer.toString().equals(stringFirstBuffer.toString())
                                                ) {
                                        showKeypadDisplaPass(stringAgainBuffer.length());
                                        mHandler.sendEmptyMessageDelayed(HANDLER_DELAYED_SHOW__MSG, 100);
                                        mHandler.sendEmptyMessageDelayed(HANDLER_PASS_MSG, 150);
                                        return;
                                }else if(stringAgainBuffer.length() == stringFirstBuffer.length()
                                                && !stringAgainBuffer.toString().equals(stringFirstBuffer.toString())){
                                        showKeypadDisplayError(stringAgainBuffer.toString().length());
                                        stringAgainBuffer = new StringBuffer();
                                        mHandler.sendEmptyMessageDelayed(HANDLER_SHOW_MSG, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
                                        mheaderText_sub.setVisibility(View.VISIBLE);
                                         
                                        return;
                                }
                                showKeypadDisplay(stringAgainBuffer.toString().length());
                        } else if (c == ' ' && stringAgainBuffer.length() > 0) {
                                stringAgainBuffer.deleteCharAt(stringAgainBuffer.length() - 1);
                                showKeypadDisplay(stringAgainBuffer.toString().length());
                                
                        }

                } else { // \u7b2c\u4e00\u6b21
                        if (c >= '0' && c <= '9' && stringFirstBuffer.length() < MAX_INPUT_NUMBER) {
                                stringFirstBuffer.append(c);
                                showKeypadDisplay(stringFirstBuffer.toString().length());
                        } else if (c == ' ' && stringFirstBuffer.length() > 0) {
                                stringFirstBuffer.deleteCharAt(stringFirstBuffer.length() - 1);
                                showKeypadDisplay(stringFirstBuffer.toString().length());
                        } else if(stringFirstBuffer.length() >=MAX_INPUT_NUMBER){
                                showKeypadDisplayError(MAX_INPUT_NUMBER);
                                stringFirstBuffer = new StringBuffer();
                                mHandler.sendEmptyMessageDelayed(HANDLER_SHOW_MSG, 500);
                        }
   
                        if (stringFirstBuffer.length() >= 4) {
                                mNumberTextView.setVisibility(View.GONE);
                                next.setVisibility(View.VISIBLE);
                        } else {
                                mNumberTextView.setVisibility(View.VISIBLE);
                                next.setVisibility(View.GONE);
                        }
                }

        }
        
        
        
        
	private void changeActivityType(int type){
		if(type ==EncryUtil.NUMBER_PASSWORD){
			patternLayout.setVisibility(View.GONE);
			numberLayout.setVisibility(View.VISIBLE);
		}else{
			numberLayout.setVisibility(View.GONE);
			patternLayout.setVisibility(View.VISIBLE);
		}
	}
	
	private void cancel() {
		Intent intent = getIntent();
		Bundle data = new Bundle();
		data.putString(EncryUtil.RESULT_KEY, mResult);
		intent.putExtras(data);
		EncryptSettingActivity.this.setResult(resultCode,
				intent);
		EncryptSettingActivity.this.finish();
}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	//	super.onBackPressed();
		cancel();
	}

	
	
	
	
	@Override
        protected void onRestart() {
                // TODO Auto-generated method stub
                super.onRestart();
                
        }

	
	
        @Override
        protected void onStop() {
                // TODO Auto-generated method stub
                super.onStop();
                if(CHANGE_PASSWORD_CODE == resultCode){
                        cancel();
                }
                
        }

        @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	    if (!mLockPatternView.isEnabled()) {
            updateStage(Stage.NeedToUnlock);
        }
	}

	@Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                // TODO Auto-generated method stub
                // super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == this.resultCode && resultCode == this.resultCode) {
                        Bundle bundle = data.getExtras();
                        int question = bundle.getInt(PasswordPotectionActivity.QUESTION_KEY, -1);
                        String answer = bundle.getString(PasswordPotectionActivity.ANSWER_KEY, null);
                        String Result = null;
                        if (question >= 0 && answer != null) {
                                if (mActivityType == EncryUtil.NUMBER_PASSWORD) {
                                        Result = EncryptServiceUtil.getInstance(this).setNumberPassword(stringAgainBuffer.toString(), question, answer);
                                } else {
                                        Result = EncryptServiceUtil.getInstance(this).setPatternPassword(patternContent, question, answer);
                                }
                        }
                        Intent intent = getIntent();
                        Bundle bundle1 = new Bundle();
                        bundle1.putString(EncryUtil.RESULT_KEY, Result);
                        intent.putExtras(bundle1);
                        EncryptSettingActivity.this.setResult(this.resultCode, intent);
                        EncryptSettingActivity.this.finish();

                }
        }
	
	
	private Boolean isNumber(String number) {
				if (number == null) {
					return false;
				}
				for (int i = 0; i < number.length(); i++) {
					char temp = number.charAt(i);
					if (temp < '0' || temp > '9') {
						return false;
					}
				}
				return true;
		
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
	
	
	
    private void updateStage(Stage stage) {
        switch (stage) {
            case NeedToUnlock:
                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                break;
            case NeedToUnlockWrong:
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                break;
            case LockedOut:
                mLockPatternView.clearPattern();
                mLockPatternView.setEnabled(false); // appearance of being disabled
                break;
        }

        // Always announce the header for accessibility. This is a no-op
        // when accessibility is disabled.
        mHeaderTextView.announceForAccessibility(mHeaderTextView.getText());
    }
	
	private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener =
			new LockPatternView.OnPatternListener() {

		public void onPatternStart() {
			
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
			mHeaderTextView.setTextColor(Color.rgb(0x22, 0x22, 0x22));
			mHeaderTextView.setVisibility(View.VISIBLE);
			mHeaderTextView.setText(R.string.release_finger_title);
		}

		public void onPatternCleared() {
		
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
	
		}

		public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
		
		}

		public void onPatternDetected(List<LockPatternView.Cell> pattern) {
			
			if (pattern.size()>=LockPatternUtils.MIN_LOCK_PATTERN_SIZE
					&& !secondPatternFlag                                          //\u9996\u6b21\u8f93\u5165\u5b8c\u6210
                        ) {
                                currentState = STATE_SECOND;
                                mHeaderTextView.setTextColor(Color.rgb(0x22, 0x22, 0x22));
                                mHeaderTextView.setVisibility(View.VISIBLE);
                                mHeaderTextView.setText(R.string.lockpattern_need_to_unlock);
                                mHeaderMianTextView.setText(R.string.confirm_encrypt);

                                mEndTextrTextView.setTextColor(Color.rgb(0x00, 0x96, 0x88));
                                mEndTextrTextView.setText(R.string.parrern_reset_title);
                                secondPatternFlag = true;
                                patternContent = pattern.toString();
                                mLockPatternView.clearPattern();
                                updateStage(Stage.NeedToUnlock);

                        }else if(secondPatternFlag &&pattern.toString().equals(patternContent)){   //\u518d\u6b21\u8f93\u5165\u5b8c\u6210

                                currentState = STATE_COMPLETE;
                                // mHeaderTextView.setText(R.string.new_pattern_title);
                           //     mEndTextrTextView.setText(R.string.complete);
                                mLockPatternView.disableInput();
                                if (CHANGE_PASSWORD_CODE == resultCode) {
                                        complete_LinearLayout.setVisibility(View.VISIBLE);
                                        numberLayout.setVisibility(View.GONE);
                                        patternLayout.setVisibility(View.GONE);
                                        mResult =  EncryptServiceUtil.getInstance(EncryptSettingActivity.this).changePatternPassword(patternContent.toString());
                                } else {
                                        setComplete(true);
                                }
				
			}else if(secondPatternFlag && ! pattern.toString().equals(patternContent) ){  //\u4e24\u6b21\u8f93\u5165\u4e0d\u4e00\u81f4
			        
                                mHeaderTextView.setTextColor(Color.rgb(0xfb, 0x30, 0x30));
                                mHeaderTextView.setVisibility(View.VISIBLE);
                                mHeaderTextView.setText(R.string.lockpattern_check_error);
                                updateStage(Stage.NeedToUnlockWrong);
                                postClearPatternRunnable();
			}else if(!secondPatternFlag && pattern.size()<LockPatternUtils.MIN_LOCK_PATTERN_SIZE    ){  //\u81f3\u5c114\u4e2a\u70b9
			
                                String str = getString(R.string.lockpattern_recording_incorrect_too_short, LockPatternUtils.MIN_LOCK_PATTERN_SIZE);
                                mHeaderTextView.setTextColor(Color.rgb(0xfb, 0x30, 0x30));
                                mHeaderTextView.setVisibility(View.VISIBLE);
                                mHeaderTextView.setText(str);
                                updateStage(Stage.NeedToUnlockWrong);
                                postClearPatternRunnable();
			}else{
		
			}
		}
	};
        private Runnable mClearPatternRunnable = new Runnable() {
                public void run() {
                        mLockPatternView.clearPattern();
                        if (secondPatternFlag) {
                                mHeaderTextView.setTextColor(Color.rgb(0x22, 0x22, 0x22));
                                mHeaderTextView.setVisibility(View.VISIBLE);
                                mHeaderTextView.setText(R.string.lockpattern_need_to_unlock);
                        } else {
                                mHeaderTextView.setVisibility(View.GONE);
                        }
                }
        };

       // clear the wrong pattern unless they have started a new one
       // already
        private void postClearPatternRunnable() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
                mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
        }
	
        
        private void setComplete(Boolean isPattern){
                
                        if (CHANGE_PASSWORD_CODE == resultCode) {
                                String Result = null;
                                if(mActivityType ==EncryUtil.PATTERN_PASSWORD){
                                        Result = null;EncryptServiceUtil.getInstance(this).changePatternPassword(patternContent);
                                }else{
                                        Result = EncryptServiceUtil.getInstance(this).changeNumberPassword(stringAgainBuffer.toString());
                                }
                                
                                Intent intent = getIntent();
                                Bundle data = new Bundle();
                                data.putString(EncryUtil.RESULT_KEY, Result);
                                intent.putExtras(data);
                                EncryptSettingActivity.this.setResult(resultCode, intent);
                                EncryptSettingActivity.this.finish();

                        } else {
                                Intent i = new Intent(EncryptSettingActivity.this, PasswordPotectionActivity.class);
                                Bundle data = new Bundle();
                                data.putInt("REQ_KEY", resultCode);
                                i.putExtras(data);
                                startActivityForResult(i, resultCode);
                        }
                }
        
        private void showKeypadDisplay(int count){
                if(count<0||count>keypadDisplayID.length){
                        return ;
                }
                for(int i=0 ; i<keypadDisplayID.length;i++){
                        ImageView image = (ImageView)findViewById(keypadDisplayID[i]);
                        image.setImageResource(R.drawable.applock_setting_keypad_mask);
                        if(i<count){        
                                image.setVisibility(View.VISIBLE);
                        }else{
                                image.setVisibility(View.GONE);
                        }
                        
                }
                
        }
        private void showKeypadDisplayError(int count){
                if(count<0||count>keypadDisplayID.length){
                        return ;
                }
                for(int i=0 ; i<count;i++){
                        ImageView image = (ImageView)findViewById(keypadDisplayID[i]);
                        image.setImageResource(R.drawable.applock_setting_keypad_mask_error);  
                                image.setVisibility(View.VISIBLE);  
                }
                
        }
        
        private void showKeypadDisplaPass(int count){
                if(count<0||count>keypadDisplayID.length){
                        return ;
                }
                for(int i=0 ; i<count;i++){
                        ImageView image = (ImageView)findViewById(keypadDisplayID[i]);
                        image.setImageResource(R.drawable.applock_keypad_mask_ok);
                                image.setVisibility(View.VISIBLE);                   
                }
                
        }
        
        
        
        private Handler mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                        // TODO Auto-generated method stub
                        switch (msg.what) {
                        case HANDLER_SHOW_MSG:
                                showKeypadDisplay(0);
                                mheaderText_sub.setVisibility(View.GONE);
                                break;
                        case    HANDLER_PASS_MSG:
                                if (CHANGE_PASSWORD_CODE == resultCode) {
                                        complete_LinearLayout.setVisibility(View.VISIBLE);
                                        numberLayout.setVisibility(View.GONE);
                                        patternLayout.setVisibility(View.GONE);
                                        mResult =  EncryptServiceUtil.getInstance(EncryptSettingActivity.this).changeNumberPassword(stringAgainBuffer.toString());                                     
                                } else {
                                        setComplete(false);
                                }
                                break;
                        case HANDLER_DELAYED_SHOW__MSG :
                                showKeypadDisplay(0);
                                break;
                                default:
                                break;
                        }
                }
        };
        
}

