package com.ape.encryptmanager.privacylock;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.settings.R;
import com.ape.encryptmanager.service.EncryptServiceUtil;
import com.ape.encryptmanager.utils.EncryUtil;
//import com.apefinger.util.MainFeatureOptions;

import java.util.List;

public class WikoEncryptSettingActivity extends Activity   implements
View.OnClickListener{
	
    private EncryptServiceUtil mEncryptServiceUtil;
    private int mActivityType=EncryUtil.PATTERN_PASSWORD;  // 0 is number password ,1 is Pattern password
    private int resultCode=0;
    private final int defaultValue = 0;
    private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 1000;
    private int   currentState =STATE_START  ;
    private static final int   STATE_START= 1;  
    private static final int   STATE_SECOND  = 2;  
    private static final int   STATE_COMPLETE = 3;
    public static final int CHANGE_PASSWORD_CODE = 1100;
	
    private static final int   MAX_INPUT_NUMBER = 10;
    private static final int    HANDLER_SHOW_MSG = 1;
    private static final int HANDLER_PASS_MSG = 2;
    private static final int HANDLER_DELAYED_SHOW__MSG =3;

    
    private StringBuffer stringFirstBuffer = new StringBuffer();
    private StringBuffer stringAgainBuffer = new StringBuffer();
    
    private Boolean secondPatternFlag = false;
    private Boolean secondNumberFlag = false;
    private String  patternContent ;

    private boolean isMyosUi = SystemProperties.getBoolean("ro.pt.myos.ui_support",false);

    private ImageView iconImage;
    private TextView  headerTitle;
    private TextView  headerSummary;
    private LockPatternView  mLockPatternView;
    private LinearLayout patternLayout;
    private LinearLayout pinLayout;
    private LinearLayout rightButtonLayout;
    private ImageView rightButtonImage;
    private TextView rightButtonText;

    private LinearLayout leftButtonLayout;
    private ImageView progressImage1;
    private ImageView progressImage2;
    private ImageView progressImage3;
    private TextView  mChangPasswordType;
    private View complete_LinearLayout;
    private View header_layout_view;
    private View back_layout;
    private Button complete;
    
    private Spanned changPinshow;
    private Spanned changPatternshow;
	
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
        if(isMyosUi){
            int layoutId = getResources().getIdentifier("myos_set_encrypt_activity_layout", "layout", getPackageName());
            //setContentView(R.layout.myos_set_encrypt_activity_layout);
            setContentView(layoutId);
        }else {
            setContentView(R.layout.wiko_set_encrypt_activity_layout);
        }
		ActionBar actionBar = getActionBar();
		if(actionBar!=null) {
                        //if(!MainFeatureOptions.isAppLockSupported(this)){
			if(true){
				actionBar.setTitle(R.string.file_lock_title);

			}else{
				actionBar.setTitle(R.string.app_and_file_lock_title);
			}
		}
		//actionBar.setTitle(R.string.app_and_file_lock_title);
		actionBar.setDisplayHomeAsUpEnabled(true);
		mEncryptServiceUtil = EncryptServiceUtil.getInstance(this);
		mActivityType = mEncryptServiceUtil.getPasswordType();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			resultCode = bundle.getInt(EncryUtil.REQ_KEY, defaultValue);
			mActivityType = bundle.getInt(EncryUtil.PASSWORD_SETTING_TYPE_KEY, mActivityType);
		}
		initUi();
	}
	
	private void initUi() {
        if (isMyosUi){

        }else {
            iconImage = (ImageView) findViewById(R.id.header_icon);
        }
		headerTitle = (TextView) findViewById(R.id.header_title);
		headerSummary = (TextView) findViewById(R.id.header_summary);
		mLockPatternView = (LockPatternView) findViewById(R.id.lock_pattern);
		EncryUtil.setLockpatternViewAttrsValue(this,mLockPatternView);
		patternLayout = (LinearLayout) findViewById(R.id.pattern_layout);
		pinLayout = (LinearLayout) findViewById(R.id.pin_layout);
		rightButtonLayout = (LinearLayout) findViewById(R.id.right_button_layout);
		leftButtonLayout = (LinearLayout) findViewById(R.id.left_button_layout);
		progressImage1 = (ImageView) findViewById(R.id.progress_1);
		progressImage2 = (ImageView) findViewById(R.id.progress_2);
		progressImage3 = (ImageView) findViewById(R.id.progress_3);
		mChangPasswordType = (TextView) findViewById(R.id.chang_type);
        complete = (Button) findViewById(R.id.complete);
        complete_LinearLayout = (View) findViewById(R.id.complete_LinearLayout);
        header_layout_view = (View) findViewById(R.id.header_layout);
        back_layout =  (View) findViewById(R.id.back_layout);
        rightButtonImage = (ImageView) findViewById(R.id.right_image);
        rightButtonText  = (TextView) findViewById(R.id.right_text);
		
        getHtmlString();        
        
		mChangPasswordType.setOnClickListener(this);
		rightButtonLayout.setOnClickListener(this);
		leftButtonLayout.setOnClickListener(this);
		complete.setOnClickListener(this);
		
		progressImage1.setImageResource(R.drawable.indicator_current);
		progressImage2.setImageResource(R.drawable.indicator_other);
		progressImage3.setImageResource(R.drawable.indicator_other);
		
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
			
		mLockPatternView.setTactileFeedbackEnabled(true);
		mLockPatternView
				.setOnPatternListener(mConfirmExistingLockPatternListener);
		changeActivityType(mActivityType);
		
		

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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	//	super.onBackPressed();
		cancel();
	}
	
	private void cancel() {
		Intent intent = getIntent();
		Bundle data = new Bundle();
		data.putString(EncryUtil.RESULT_KEY, mResult);
		intent.putExtras(data);
		WikoEncryptSettingActivity.this.setResult(resultCode,
				intent);
		WikoEncryptSettingActivity.this.finish();
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
	    
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        mHandler.removeMessages(HANDLER_SHOW_MSG);
      if(  mHandler.hasMessages(HANDLER_PASS_MSG)){
              return;
      }
      char c = 0;
		switch (v.getId()) {
		case R.id.chang_type:
			changPasswordType();
			break;
			
		case R.id.right_button_layout:
			if(mActivityType == EncryUtil.NUMBER_PASSWORD && !secondNumberFlag ){
			 	headerTitle.setText(R.string.confirm_your_pin);
            	headerSummary.setTextColor(Color.rgb(0x75, 0x75, 0x75));
				headerSummary.setText(R.string.finger_pin_summary);
                showKeypadDisplay(0);
                secondNumberFlag = true;
            	mChangPasswordType.setVisibility(View.INVISIBLE);
            	leftButtonLayout.setVisibility(View.VISIBLE);
            	rightButtonLayout.setVisibility(View.INVISIBLE);
			}
			
			break;
		case R.id.left_button_layout:
			if ((mActivityType == EncryUtil.NUMBER_PASSWORD && secondNumberFlag)
					|| (mActivityType == EncryUtil.PATTERN_PASSWORD && secondPatternFlag)) {
				
				changeActivityType(mActivityType);
			//	stringFirstBuffer = new StringBuffer();
			//	stringAgainBuffer = new StringBuffer();
			//	secondPatternFlag = false;
			//	secondNumberFlag = false;
		//		patternContent = null;
		//		progressImage1.setImageResource(R.drawable.indicator_current);
		//		progressImage2.setImageResource(R.drawable.indicator_other);
		//		progressImage3.setImageResource(R.drawable.indicator_other);
		//		leftButtonLayout.setVisibility(View.INVISIBLE);
		//		rightButtonLayout.setClickable(false);
	//			mChangPasswordType.setVisibility(View.VISIBLE);
			}
	
			break;
		case R.id.complete :
            if (CHANGE_PASSWORD_CODE == resultCode) {      
                Intent intent = getIntent();
                Bundle data = new Bundle();
                data.putString(EncryUtil.RESULT_KEY, mResult);
                intent.putExtras(data);
                WikoEncryptSettingActivity.this.setResult(resultCode, intent);
                WikoEncryptSettingActivity.this.finish();

        } else {
                Intent i = new Intent(WikoEncryptSettingActivity.this, WikoPasswordPotectionActivity.class);
                Bundle data = new Bundle();
                data.putInt("REQ_KEY", resultCode);
                i.putExtras(data);
                startActivityForResult(i, resultCode);

        }
			break;
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
		if(c!=0){
			pinInput(c);
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
                                    Result = mEncryptServiceUtil.setNumberPassword(stringAgainBuffer.toString(), question, answer);
                            } else {
                                    Result = mEncryptServiceUtil.setPatternPassword(patternContent, question, answer);
                            }
                    }
                    Intent intent = getIntent();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString(EncryUtil.RESULT_KEY, Result);
                    intent.putExtras(bundle1);
                    WikoEncryptSettingActivity.this.setResult(this.resultCode, intent);
                    WikoEncryptSettingActivity.this.finish();

            }
    }
	

	private void changPasswordType(){
		if(mActivityType == EncryUtil.PATTERN_PASSWORD){
			mActivityType = EncryUtil.NUMBER_PASSWORD ;
		}else{
			mActivityType = EncryUtil.PATTERN_PASSWORD ;
		}
		changeActivityType(mActivityType);
	}
	
	private void pinInput(char c) {
		if (secondNumberFlag) {
			if (c >= '0' && c <= '9'
					&& stringAgainBuffer.length() < stringFirstBuffer.length()) {
				stringAgainBuffer.append(c);
				if (stringAgainBuffer.length() == stringFirstBuffer.length()
						&& stringAgainBuffer.toString().equals(
								stringFirstBuffer.toString())) {
					showKeypadDisplaPass(stringAgainBuffer.length());
					mHandler.sendEmptyMessageDelayed(HANDLER_DELAYED_SHOW__MSG,
							100);
					mHandler.sendEmptyMessageDelayed(HANDLER_PASS_MSG, 150);
					return;
				} else if (stringAgainBuffer.length() == stringFirstBuffer
						.length()
						&& !stringAgainBuffer.toString().equals(
								stringFirstBuffer.toString())) {
					showKeypadDisplayError(stringAgainBuffer.toString()
							.length());
					stringAgainBuffer = new StringBuffer();
					mHandler.sendEmptyMessageDelayed(HANDLER_SHOW_MSG, 500);
					headerSummary.setTextColor(Color.rgb(0xfb, 0x30, 0x30));
					headerSummary.setText(R.string.pin_not_match);
					postClearPatternRunnable();
					return;
				}
				showKeypadDisplay(stringAgainBuffer.toString().length());
			} else if (c == ' ' && stringAgainBuffer.length() > 0) {
				stringAgainBuffer.deleteCharAt(stringAgainBuffer.length() - 1);
				showKeypadDisplay(stringAgainBuffer.toString().length());

			}

		} else {
			if (c >= '0' && c <= '9'
					&& stringFirstBuffer.length() < MAX_INPUT_NUMBER) {
				stringFirstBuffer.append(c);
				showKeypadDisplay(stringFirstBuffer.toString().length());
			} else if (c == ' ' && stringFirstBuffer.length() > 0) {
				stringFirstBuffer.deleteCharAt(stringFirstBuffer.length() - 1);
				showKeypadDisplay(stringFirstBuffer.toString().length());
			} else if (stringFirstBuffer.length() >= MAX_INPUT_NUMBER) {
				showKeypadDisplayError(MAX_INPUT_NUMBER);
				stringFirstBuffer = new StringBuffer();
				mHandler.sendEmptyMessageDelayed(HANDLER_SHOW_MSG, 500);
			}
			
			if(stringFirstBuffer == null || stringFirstBuffer.length()<4){
				rightButtonLayout.setClickable(false);
				setRightButtonStyles(false);
			}else{
				rightButtonLayout.setClickable(true);
				setRightButtonStyles(true);
			}
			
		}
	}
	
	
	private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener =
			new LockPatternView.OnPatternListener() {

		public void onPatternStart() {
			
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
			headerSummary.setTextColor(Color.rgb(0x75, 0x75, 0x75));
			headerSummary.setText(R.string.release_finger_when_done);
		}

		public void onPatternCleared() {
		
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
	
		}

		public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
		
		}

		public void onPatternDetected(List<LockPatternView.Cell> pattern) {
			
			if (pattern.size()>=LockPatternUtils.MIN_LOCK_PATTERN_SIZE
					&& !secondPatternFlag
                        ) {
                                currentState = STATE_SECOND;
                                
                            	headerTitle.setText(R.string.confirm_your_pattern);
                            	headerSummary.setTextColor(Color.rgb(0x75, 0x75, 0x75));
								headerSummary.setText(R.string.finger_draw_summary);
								
                                secondPatternFlag = true;
                                patternContent = pattern.toString();
                                mLockPatternView.clearPattern();
                               updateStage(Stage.NeedToUnlock);
                       		leftButtonLayout.setVisibility(View.VISIBLE);
                    		rightButtonLayout.setVisibility(View.INVISIBLE);
                    		progressImage1.setImageResource(R.drawable.indicator_other);
                    		progressImage2.setImageResource(R.drawable.indicator_current);
                    		progressImage3.setImageResource(R.drawable.indicator_other);
                    		mChangPasswordType.setVisibility(View.INVISIBLE);

                        }else if(secondPatternFlag &&pattern.toString().equals(patternContent)){

                                currentState = STATE_COMPLETE;
                               mLockPatternView.disableInput();
                                if (CHANGE_PASSWORD_CODE == resultCode) {
                                        complete_LinearLayout.setVisibility(View.VISIBLE);
                                        header_layout_view.setVisibility(View.GONE);
                                        patternLayout.setVisibility(View.GONE);
                                        pinLayout.setVisibility(View.GONE);              
                                        back_layout.setVisibility(View.GONE);     
                                        mChangPasswordType.setVisibility(View.GONE);   
                                        

                                        mResult =  mEncryptServiceUtil.changePatternPassword(patternContent.toString());
                                } else {
                                        setComplete(true);
                                }
				
			}else if(secondPatternFlag && ! pattern.toString().equals(patternContent) ){
								headerSummary.setTextColor(Color.rgb(0xfb, 0x30, 0x30));
								headerSummary.setText(R.string.patterns_not_match);
                                updateStage(Stage.NeedToUnlockWrong);
                                postClearPatternRunnable();
			}else if(!secondPatternFlag && pattern.size()< LockPatternUtils.MIN_LOCK_PATTERN_SIZE    ){
								headerSummary.setTextColor(Color.rgb(0xfb, 0x30, 0x30));
								headerSummary.setText(R.string.lockpattern_too_short);
                                updateStage(Stage.NeedToUnlockWrong);
                                postClearPatternRunnable();
			}else{
		
			}
		}
	};
        private Runnable mClearPatternRunnable = new Runnable() {
                public void run() {
                	headerSummary.setTextColor(Color.rgb(0x75, 0x75, 0x75));
                			  if(mActivityType == EncryUtil.PATTERN_PASSWORD){
                					mLockPatternView.clearPattern();
									headerSummary.setText(R.string.finger_draw_summary);
                			  }else{
                					headerSummary.setText(R.string.finger_pin_summary);
                			  }
                }
        };


        private void postClearPatternRunnable() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
                mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
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
                    mLockPatternView.setEnabled(false); 
                    break;
            }
        }
	
    	private void changeActivityType(int type){
    		if(type ==EncryUtil.NUMBER_PASSWORD){
    			patternLayout.setVisibility(View.GONE);
    			pinLayout.setVisibility(View.VISIBLE);
                if (isMyosUi){

                }else {
                    iconImage.setImageResource(R.drawable.boot_guide_header_icon_pincode);
                }
    			headerTitle.setText(R.string.set_pin_unlock_title) ;
    			headerSummary.setText(R.string.finger_pin_summary) ;
    			leftButtonLayout.setVisibility(View.INVISIBLE);
    			rightButtonLayout.setVisibility(View.VISIBLE);
    			rightButtonLayout.setClickable(false);
    			showKeypadDisplay(0);
    			setRightButtonStyles(false);
    			if(changPatternshow !=null){
    				mChangPasswordType.setText(changPatternshow);
    			}
    		}else{
    			pinLayout.setVisibility(View.GONE);
    			patternLayout.setVisibility(View.VISIBLE);
                if (isMyosUi){

                }else {
                    iconImage.setImageResource(R.drawable.boot_guide_header_icon_pattern);
                }
    			headerTitle.setText(R.string.set_pattern_unlock_title) ;
    			headerSummary.setText(R.string.finger_draw_summary) ;
    			leftButtonLayout.setVisibility(View.INVISIBLE);
    			rightButtonLayout.setVisibility(View.INVISIBLE);
    			if(changPinshow !=null){
    				mChangPasswordType.setText(changPinshow);
    			}
    		}
    		progressImage1.setImageResource(R.drawable.indicator_current);
    		progressImage2.setImageResource(R.drawable.indicator_other);
    		progressImage3.setImageResource(R.drawable.indicator_other);
    		mChangPasswordType.setVisibility(View.VISIBLE);
    	
    		secondPatternFlag = false;
    	    secondNumberFlag = false;
    	    stringFirstBuffer = new StringBuffer();
    	    stringAgainBuffer = new StringBuffer();
    	    patternContent = null;
    	}
    	
	
        private void setComplete(Boolean isPattern){
            
            if (CHANGE_PASSWORD_CODE == resultCode) {
                    String Result = null;
                    if(mActivityType ==EncryUtil.PATTERN_PASSWORD){
                            Result = null;mEncryptServiceUtil.changePatternPassword(patternContent);
                    }else{
                            Result = mEncryptServiceUtil.changeNumberPassword(stringAgainBuffer.toString());
                    }
                    
                    Intent intent = getIntent();
                    Bundle data = new Bundle();
                    data.putString(EncryUtil.RESULT_KEY, Result);
                    intent.putExtras(data);
                    WikoEncryptSettingActivity.this.setResult(resultCode, intent);
                    WikoEncryptSettingActivity.this.finish();

            } else {
                    Intent i = new Intent(WikoEncryptSettingActivity.this, WikoPasswordPotectionActivity.class);
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
                        break;
                case    HANDLER_PASS_MSG:
                        if (CHANGE_PASSWORD_CODE == resultCode) {
                            complete_LinearLayout.setVisibility(View.VISIBLE);
                            header_layout_view.setVisibility(View.GONE);
                            patternLayout.setVisibility(View.GONE);
                            pinLayout.setVisibility(View.GONE);              
                            back_layout.setVisibility(View.GONE);     
                            mChangPasswordType.setVisibility(View.GONE);   
                            mResult =  mEncryptServiceUtil.changeNumberPassword(stringAgainBuffer.toString());
                                
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
    

private void setRightButtonStyles(Boolean clickable){
	
	if(clickable){
        rightButtonImage.setImageResource(R.drawable.indicator_next);
        rightButtonText.setTextColor(Color.rgb(0x00, 0x00, 0x00));
	}else{
        rightButtonImage.setImageResource(R.drawable.indicator_next_disable);
        rightButtonText.setTextColor(Color.rgb(0xa4, 0xa6, 0xa7));
	}
	
}

    private void getHtmlString() {
        String pin = getResources().getString(R.string.chang_pin_code);
        String pattern = getResources().getString(R.string.chang_pattern);
        if (pin != null && pattern != null) {
            int indexPin = pin.indexOf('&');
            int indexPattern = pattern.indexOf('&');

            if (isMyosUi){
                if (indexPin > 0 && ( indexPin+1)< pin.length()) {
                    changPinshow = Html.fromHtml("<font  color='#757575'>" + pin.substring(0, indexPin) + "</font>" + "<font color='#009191'>"
                            + pin.substring(indexPin + 1) + "</font>");

                } else {
                    changPinshow = Html.fromHtml( "<font color='#009191'>" + pin+ "</font>");
                }

                if (indexPattern > 0 && (indexPattern+1)<pattern.length() ) {
                    changPatternshow = Html.fromHtml("<font  color='#757575'>" + pattern.substring(0, indexPattern) + "</font>" + "<font color='#009191'>"
                            + pattern.substring(indexPattern + 1) + "</font>");
                } else {
                    changPatternshow = Html.fromHtml("<font color='#009191'>"+ pattern+ "</font>");
                }
            }else {
                if (indexPin > 0 && ( indexPin+1)< pin.length()) {
                    changPinshow = Html.fromHtml("<font  color='#757575'>" + pin.substring(0, indexPin) + "</font>" + "<font color='#169dff'>"
                            + pin.substring(indexPin + 1) + "</font>");

                } else {
                    changPinshow = Html.fromHtml( "<font color='#169dff'>" + pin+ "</font>");
                }

                if (indexPattern > 0 && (indexPattern+1)<pattern.length() ) {
                    changPatternshow = Html.fromHtml("<font  color='#757575'>" + pattern.substring(0, indexPattern) + "</font>" + "<font color='#169dff'>"
                            + pattern.substring(indexPattern + 1) + "</font>");
                } else {
                    changPatternshow = Html.fromHtml("<font color='#169dff'>"+ pattern+ "</font>");
                }
            }
        }

    }
    
}
