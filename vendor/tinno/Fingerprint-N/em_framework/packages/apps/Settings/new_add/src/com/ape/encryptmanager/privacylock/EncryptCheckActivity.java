package com.ape.encryptmanager.privacylock;

import android.hardware.fingerprint.FingerprintManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.UserHandle;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.settings.R;

import com.ape.emFramework.emService.ApeCustomFpManager;
import com.ape.encryptmanager.service.EncryptServiceUtil;
import com.ape.encryptmanager.service.TinnoFingerprintData;
import com.ape.encryptmanager.utils.EncryUtil;
import com.ape.emFramework.Log;
import com.ape.encryptmanager.utils.LogUtil;
import com.ape.encryptmanager.utils.MessageType;
import android.content.Context;
import java.util.List;
import android.hardware.fingerprint.FingerprintManager;
import android.content.Context;


public class EncryptCheckActivity extends Activity implements
		View.OnClickListener {
	// private EditText mEditText;
        public static final String TAG = "EncryptCheckActivity-cls";
        private TextView mPasswordClew;
        private TextView mpatternClew;
     //   private TextView mPasswordView;
    //    private Button mButtonCance;
     //   private Button mButtonDelete;
        private LockPatternView mLockPatternView;
        private LinearLayout numberLayout;
        private LinearLayout patternLayout;

        private TextView mHeaderPatternTextView;
        private TextView mHeaderNumberTextView;
        private TextView  mFingprintTextView;
        
        private int resultCode = 0;
        private int mActivityType = EncryUtil.PATTERN_PASSWORD; // 0 is number password ,1 is Pattern  password
        private final int defaultValue = 0;
        private StringBuffer stringBuffer = new StringBuffer();
   //     private static final int msg = 0x1;
    //    private static final int MSG_DELAY_SHOW = 0x2;
        private static final int    HANDLER_SHOW_MSG = 1;
        private static final int HANDLER_PASS_MSG = 2;
        private static final int HANDLER_DELAYED_SHOW__MSG =3;
        private static final int   MSG_DELAY_SHOW = 4;
        private static final int   MSG_FINGERPRINT_DELAY_VERIFY_START = 5;
        
        public static final int  fingerprintDelayVerifyTime = 250;
        private static final int delayTime = 1200;
        private int passwordLength = 0;

        private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 1000;
        private int mNumWrongConfirmAttempts;
        private CharSequence mHeaderText;
        private CharSequence mHeaderWrongText;
        //private EncryptServiceUtil mEncryptServiceUtil;

        private ApeCustomFpManager mApeCustomFpManager = null;
        private Boolean serviceConnFlag = false;
        private Boolean verifyStartFlag = false;
        public static final int FileManager = 1 ;
        public static final int Gallery = 2;
        public static final int CM = 3;
        public static final int Other =4;
        public static final int ForgetPassword =5;
        private static final int   MAX_INPUT_NUMBER = 10;
        private String mResult = null;
        public static final int FAILED_ATTEMPTS_BEFORE_TIMEOUT = 3;
        
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

	private boolean stop_flag = false;

   // private Runnable mCheckdVerifyRunnable = null;
	
        private Handler mHandler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                        // TODO Auto-generated method stub
                        if(stop_flag){ return; }
						
                        switch (msg.what) {
                        case HANDLER_SHOW_MSG:
                                showKeypadDisplay(0);
                                break;
                        case HANDLER_PASS_MSG:
                                check();
                                break;
                        case HANDLER_DELAYED_SHOW__MSG:
                                showKeypadDisplay(0);
                                break;
                        case MSG_DELAY_SHOW:
                                mPasswordClew.setVisibility(View.INVISIBLE);
                                mpatternClew.setVisibility(View.INVISIBLE);
			            break;
                        case MSG_FINGERPRINT_DELAY_VERIFY_START:
                                mHandler.removeMessages(MSG_FINGERPRINT_DELAY_VERIFY_START);
                                if (ApeCustomFpManager.isDeviceLocked(EncryptCheckActivity.this) 
                                    || ApeCustomFpManager.isKeyguardLocked(EncryptCheckActivity.this) 
                                    || !ApeCustomFpManager.isScreenON(EncryptCheckActivity.this)) {
                                    
                                    mHandler.sendEmptyMessageDelayed(MSG_FINGERPRINT_DELAY_VERIFY_START, 200);
                                    Log.i(TAG, "handleMessage delay verify");
                                } else {
                                    Log.i(TAG, "handleMessage verifyStart");
                                    mApeCustomFpManager.verifyStart(0, EncryUtil.TAG_PRIVATE_FMGR, UserHandle.myUserId());
                                    verifyStartFlag = true;
                                }
                                break;
				case MessageType.TINNO_MSG_SERVICE_CONNECTED:
					if (!serviceConnFlag) {
						//mServiceManager.registerCallback(EncryUtil.FINGERPRINT_REGISTER_VERIFY_TYPE);
					serviceConnFlag = true;
				}
				break;
			case MessageType.TINNO_MSG_VERIFY_SUCCESS:
				TinnoFingerprintData mTinnoFingerprintData = (TinnoFingerprintData)msg.obj;
                                // if(mEncryptServiceUtil.getBindPrivacyLockFingerprint()
                                // ==mTinnoFingerprintData.getFingerid() ){
				if(EncryptServiceUtil.getInstance(EncryptCheckActivity.this).checkBindPrivacyLockFingerprint(mTinnoFingerprintData.getFingerid()) ){
	                         //mPasswordClew.setTextColor(Color.BLACK);			
				    //mPasswordClew.setText(R.string.fingerprint_check_success);
				    //mPasswordClew.setVisibility(View.VISIBLE);
				    Log.d(TAG, "guomingyi#Success");
				    ApeCustomFpManager.vibrateFingerprintSuccess(EncryptCheckActivity.this);
				    //mpatternClew.setText(R.string.fingerprint_check_success);
				    //mpatternClew.setVisibility(View.VISIBLE);
				    String result = EncryptServiceUtil.getInstance(EncryptCheckActivity.this).getToken();
				    Intent intent = getIntent();
				    Bundle data = new Bundle();
				    data.putString(EncryUtil.RESULT_KEY, result);
				    intent.putExtras(data);
				    EncryptCheckActivity.this.setResult(resultCode, intent);
				    EncryptCheckActivity.this.finish();			
			       }else{
				    Log.d(TAG, "guomingyi#finger id error");
				    ApeCustomFpManager.vibrateFingerprintError(EncryptCheckActivity.this);
				    //mPasswordClew.setText(R.string.fingerprint_check_fail);
				    //mPasswordClew.setVisibility(View.VISIBLE);
				    //mpatternClew.setText(R.string.fingerprint_check_fail);
				    //mpatternClew.setVisibility(View.VISIBLE);
				    //mHandler.removeMessages( MSG_DELAY_SHOW);
				    //mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_SHOW), delayTime);
				    mApeCustomFpManager.verifyStart(0, EncryUtil.TAG_PRIVATE_FMGR, UserHandle.myUserId());
				    verifyStartFlag = true;
				
				}
				break;
			case MessageType.TINNO_MSG_VERIFY_FAILED:
				//mPasswordClew.setText(R.string.fingerprint_check_fail);
				/*mPasswordClew.setVisibility(View.VISIBLE);
				
				mpatternClew.setText(R.string.fingerprint_check_fail);
				//mpatternClew.setVisibility(View.VISIBLE);
				
				mHandler.removeMessages( MSG_DELAY_SHOW);
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_SHOW), delayTime);*/
			break;
			
			case MessageType.TINNO_MSG_SYSTEM_ERROR:
				//mPasswordClew.setText(R.string.fingerprint_error);
				//mPasswordClew.setVisibility(View.VISIBLE);
				/*mHandler.removeMessages( MSG_DELAY_SHOW);
	
				mpatternClew.setText(R.string.fingerprint_error);
				//mpatternClew.setVisibility(View.VISIBLE);
			
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_SHOW), delayTime);*/

			break;
   
             case MessageType.TINNO_MSG_VERIFY_ERROR_LOCKOUT:
                     mApeCustomFpManager.cancelOperation(
                        EncryUtil.FINGERPRINT_REGISTER_VERIFY_TYPE,EncryUtil.TAG_PRIVATE_FMGR);
                     resetFingerFailedAttemps();
                     mApeCustomFpManager.verifyStart(0, EncryUtil.TAG_PRIVATE_FMGR, UserHandle.myUserId());
             break;            

			default:
				break;
			}
			
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.encrypt_check_activity_layout_sub);
		//mEncryptServiceUtil = new EncryptServiceUtil.getInstance(this);
		mActivityType = EncryptServiceUtil.getInstance(EncryptCheckActivity.this).getPasswordType();
		passwordLength = EncryptServiceUtil.getInstance(EncryptCheckActivity.this).getPasswordLength();
		
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		ActionBar actionBar = getActionBar();
        if(actionBar!=null) {
            //if(!MainFeatureOptions.isAppLockSupported(this)){
            if(true){
                actionBar.setTitle(R.string.file_lock_title);

            }else{
                actionBar.setTitle(R.string.app_and_file_lock_title);
            }
        }
		actionBar.setDisplayHomeAsUpEnabled(true); 

			Bundle bundle = getIntent().getExtras();
			
                if (bundle != null) {
                        resultCode = bundle.getInt(EncryUtil.REQ_KEY, defaultValue);
                        mActivityType = bundle.getInt(EncryUtil.PASSWORD_CHECK_TYPE_KEY, mActivityType);
                        try {
                                if ((resultCode == FileManager &&EncryptServiceUtil.getInstance(EncryptCheckActivity.this).getFileLock() )
                                                || ( resultCode == Gallery &&   EncryptServiceUtil.getInstance(EncryptCheckActivity.this).getGalleryLock() )
                                                ||( resultCode == CM &&    EncryptServiceUtil.getInstance(EncryptCheckActivity.this).getAppFingerprint())
                                         //       ||( resultCode == Other &&    EncryptServiceUtil.getInstance(this).getAppSwitch())
                                                ) {
                                //FingerprintManager mFingerprintManager = (FingerprintManager) this.getSystemService(
                                    //Context.FINGERPRINT_SERVICE);                                          
                                mApeCustomFpManager = ApeCustomFpManager.getInstance(this);//new ApeCustomFpManager(this, mHandler);
                                mApeCustomFpManager.setHandler(mHandler);                                     
										
                                }
                        } catch (Exception e) {
                                LogUtil.d(TAG, "Exception :" + e.toString());
                        }
                }
			
		init();

	}

	private void init() {
		// mEditText = (EditText) findViewById(R.id.input_password);

		mpatternClew =  (TextView) findViewById(R.id.textview_show_pattern_clew);

		mPasswordClew = (TextView) findViewById(R.id.textview_show_number_clew);

        mHeaderNumberTextView = (TextView)findViewById(R.id.textview_title);
        mHeaderPatternTextView = (TextView)findViewById(R.id.headerText_main);
        mFingprintTextView =  (TextView)findViewById(R.id.textview_fingprint);
  //    mFooterTextView = (TextView) findViewById(R.id.footerText);
		mLockPatternView = (LockPatternView) findViewById(R.id.lockPattern);
        EncryUtil.setLockpatternViewAttrsValue(this,mLockPatternView);

        mLockPatternView.setTactileFeedbackEnabled(true);
        mLockPatternView.setOnPatternListener(mConfirmExistingLockPatternListener);
		
    	 numberLayout =(LinearLayout)findViewById(R.id.number_password);
    	 patternLayout =(LinearLayout)findViewById(R.id.pattern_password);




		((ImageButton) findViewById(R.id.imagebutton_0))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.imagebutton_1))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.imagebutton_2))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.imagebutton_3))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.imagebutton_4))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.imagebutton_5))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.imagebutton_6))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.imagebutton_7))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.imagebutton_8))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.imagebutton_9))
				.setOnClickListener(this);
	         ((ImageButton) findViewById(R.id.imagebutton_delete))
                      .setOnClickListener(this);
		
		/*
		if(mActivityType ==EncryUtil.NUMBER_PASSWORD){
			patternLayout.setVisibility(View.GONE);
			numberLayout.setVisibility(View.VISIBLE);
		}else{
			numberLayout.setVisibility(View.GONE);
			patternLayout.setVisibility(View.VISIBLE);
		}
		*/
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(serviceConnFlag && mApeCustomFpManager !=null ){
			serviceConnFlag = false;
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        mPasswordClew.setVisibility(View.INVISIBLE);
        mHandler.removeMessages(HANDLER_SHOW_MSG);
        mHandler.removeMessages(MSG_DELAY_SHOW);
        if(  mHandler.hasMessages(HANDLER_PASS_MSG)){
            return;
        }
        
		char temp = 0;
		switch (v.getId()) {
		case R.id.imagebutton_0:
			temp = '0';
			break;
		case R.id.imagebutton_1:
			temp = '1';
			break;
		case R.id.imagebutton_2:
			temp = '2';
			break;
		case R.id.imagebutton_3:
			temp = '3';
			break;
		case R.id.imagebutton_4:
			temp = '4';
			break;
		case R.id.imagebutton_5:
			temp = '5';
			break;
		case R.id.imagebutton_6:
			temp = '6';
			break;
		case R.id.imagebutton_7:
			temp = '7';
			break;
		case R.id.imagebutton_8:
			temp = '8';
			break;
		case R.id.imagebutton_9:
			temp = '9';
			break;
		case R.id.imagebutton_delete:
		        temp = ' ';
			break;
		default:
			break;
		}

        if (temp >= '0' && temp <= '9' && stringBuffer.length() < passwordLength) {
            stringBuffer.append(temp);
            if (stringBuffer.length() == passwordLength && passwordLength != 0) {
                String result = EncryptServiceUtil.getInstance(EncryptCheckActivity.this).checkPassword(stringBuffer.toString());
                if (result != null) {
                        mResult = result;
                        showKeypadDisplaPass(stringBuffer.length());
                        mHandler.sendEmptyMessageDelayed(HANDLER_DELAYED_SHOW__MSG, 100);
                        mHandler.sendEmptyMessageDelayed(HANDLER_PASS_MSG, 150);
                        
                        
                } else {
                        showKeypadDisplayError(stringBuffer.toString().length());
                        stringBuffer = new StringBuffer();
                        mHandler.sendEmptyMessageDelayed(HANDLER_SHOW_MSG, 500);
                }
                return;
            }                    
            showKeypadDisplay(stringBuffer.toString().length());
        } 
		else if (temp == ' ' && stringBuffer.length() > 0) {
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            showKeypadDisplay(stringBuffer.toString().length());
        }
	}

public void onStop() {
        super.onRestart();
        Log.i(TAG, "onStop");

        mpatternClew.setVisibility(View.INVISIBLE);
        if(verifyStartFlag){
            verifyStartFlag = false;
        }

     //   if(mCheckdVerifyRunnable != null){
     //       mHandler.removeCallbacks(mCheckdVerifyRunnable);
     //       mCheckdVerifyRunnable = null;
     //    }
    }

	 @Override
     public void onPause() {
        super.onPause();  
        Log.i(TAG, "onPause");
        mHandler.removeMessages(MSG_FINGERPRINT_DELAY_VERIFY_START);
        if (verifyStartFlag /*&& serviceConnFlag*/ && mApeCustomFpManager !=null ) {
            boolean b = ApeCustomFpManager.isDeviceLocked(EncryptCheckActivity.this);
            if(!b){
                Log.i(TAG, "onPause cancelOperation");
                mApeCustomFpManager.cancelOperation(
                    EncryUtil.FINGERPRINT_REGISTER_VERIFY_TYPE, EncryUtil.TAG_PRIVATE_FMGR);
                verifyStartFlag = false;
            }
        }

        stop_flag = true;
     }
	
	@Override
        protected void onResume() {
                // TODO Auto-generated method stub
                //Log.i(TAG, "onResume");
                super.onResume();
                mActivityType = EncryptServiceUtil.getInstance(EncryptCheckActivity.this).getPasswordType();
                passwordLength = EncryptServiceUtil.getInstance(EncryptCheckActivity.this).getPasswordLength();
                if(mActivityType ==EncryUtil.NUMBER_PASSWORD){
                    patternLayout.setVisibility(View.GONE);
                    numberLayout.setVisibility(View.VISIBLE);
                }else{
                    numberLayout.setVisibility(View.GONE);
                    patternLayout.setVisibility(View.VISIBLE);
                }
		
                int StringID = 0;
                if (resultCode == FileManager || resultCode == Gallery || resultCode == CM) {
                        if (EncryptServiceUtil.getInstance(EncryptCheckActivity.this).getFileLock()) {
                                StringID = R.string.check_pattern_title;
                                mFingprintTextView.setVisibility(View.VISIBLE);
                                changeTextAlpha( mFingprintTextView);
                        } else {
                                StringID = R.string.check_pattern_title_sub;
                                mFingprintTextView.setVisibility(View.INVISIBLE);
                                clearTextAlpha(mFingprintTextView);
                        }
                } else if (resultCode == Other) {
                  //      if (EncryptServiceUtil.getInstance(this).getAppSwitch()) {
                  //              StringID = R.string.check_pattern_title;
                //                mFingprintTextView.setVisibility(View.VISIBLE);
                //                changeTextAlpha( mFingprintTextView);
              //          } else {
                                StringID = R.string.check_pattern_title_sub;
              //          }
                }

                if (StringID != 0) {
                        mHeaderNumberTextView.setText(StringID);
                        mHeaderPatternTextView.setText(StringID);
                }

                if (!mLockPatternView.isEnabled()) {
                        mNumWrongConfirmAttempts = 0;
                        updateStage(Stage.NeedToUnlock);
                }
                

                if (!verifyStartFlag && mApeCustomFpManager != null) {

                 mHandler.removeMessages(MSG_FINGERPRINT_DELAY_VERIFY_START);
                 mHandler.sendEmptyMessageDelayed(MSG_FINGERPRINT_DELAY_VERIFY_START, 200);
		   
		   
                /*
                    if(mCheckdVerifyRunnable != null){
                        mHandler.removeCallbacks(mCheckdVerifyRunnable);
                        mCheckdVerifyRunnable = null;
                    }
                    mHandler.postDelayed(mCheckdVerifyRunnable = new Runnable() {
                      @Override
                      public void run() {
                          //boolean b = false;//ApeCustomFpManager.isDeviceLocked(EncryptCheckActivity.this);
                          Log.i(TAG, "---- onResume:verifyStart +++----");
                          //mHandler.sendEmptyMessage(MSG_FINGERPRINT_DELAY_VERIFY_START);
                         
                          mApeCustomFpManager.verifyStart(0, EncryUtil.TAG_PRIVATE_FMGR, 0);
                          verifyStartFlag = true;
                      }
                    }, 300);
                    */
                }
			
				
                stop_flag = false;
        }


    private void cancel() {
        Intent intent = getIntent();
        Bundle data = new Bundle();
        intent.putExtras(data);
        EncryptCheckActivity.this.setResult(resultCode, intent);
        EncryptCheckActivity.this.finish();
	}

	private void check() {

		// TODO Auto-generated method stub
		if (mResult != null) {
				Intent intent = getIntent();
				Bundle data = new Bundle();
				data.putString(EncryUtil.RESULT_KEY, mResult);
				intent.putExtras(data);
				EncryptCheckActivity.this.setResult(resultCode, intent);
			EncryptCheckActivity.this.finish();
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	//	super.onBackPressed();
		cancel();
	}
	
	
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		 getMenuInflater().inflate(R.menu.forget_password, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			{
				cancel();
				return true;
			}
		case R.id.action_forget_password :
			{
				Intent i = new Intent(EncryptCheckActivity.this,ForgetPasswordActivity.class);		
				Bundle data = new Bundle();
				data.putInt(EncryUtil.REQ_KEY,resultCode);
				i.putExtras(data);
				startActivityForResult(i, PrivacyLockActivity.FORGET_PASSWORD_REQ);
				return true;
			}
		default:
		return super.onOptionsItemSelected(item);
		}
	}
	
	
	@Override
        protected void onActivityResult(int requestCode, int resultCode,
                        Intent data) {
                // TODO Auto-generated method stub
                // super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == this.resultCode
                                && resultCode == this.resultCode) {
                        Bundle bundle = data.getExtras();
                        String answer = bundle.getString(EncryUtil.RESULT_KEY,  null);
                        if (answer != null) {
                                Intent intent = getIntent();
                                Bundle bundle1 = new Bundle();
                                bundle1.putString(EncryUtil.RESULT_KEY, answer);
                                intent.putExtras(bundle1);
                                EncryptCheckActivity.this.setResult(this.resultCode, intent);
                                EncryptCheckActivity.this.finish();
                        }
                } else if (requestCode == PrivacyLockActivity.FORGET_PASSWORD_REQ
                                && resultCode == this.resultCode) {

                        Bundle bundle = data.getExtras();
                        String answer = bundle.getString(EncryUtil.RESULT_KEY, null);
                        if (answer != null) {
                                Intent intent = getIntent();
                                Bundle bundle1 = new Bundle();
                                bundle1.putString(EncryUtil.RESULT_KEY, answer);
                                bundle1.putBoolean(EncryUtil.FORGET_PASSWORD_KEY, true);
                                intent.putExtras(bundle1);
                                EncryptCheckActivity.this.setResult( this.resultCode, intent);
                                EncryptCheckActivity.this.finish();
                        }
                }

        }
	

	private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener =
			new LockPatternView.OnPatternListener() {

		public void onPatternStart() {
		        mHandler.removeMessages( MSG_DELAY_SHOW);
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
		}

		public void onPatternCleared() {
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
		}

		public void onPatternCellAdded(List<Cell> pattern) {

		}

		public void onPatternDetected(List<LockPatternView.Cell> pattern) {		
                  String result =EncryptServiceUtil.getInstance(EncryptCheckActivity.this).checkPassword(pattern.toString());
                  if (result !=null &&result.length()>0) {  
                      Intent intent = getIntent();
                      Bundle data = new Bundle();
                      data.putString(EncryUtil.RESULT_KEY, result);
                      intent.putExtras(data);
                      EncryptCheckActivity.this.setResult(resultCode, intent);
                      EncryptCheckActivity.this.finish();					
                  } else { 
                      String str = getString(R.string.lockpattern_input_error_sub);
                      mpatternClew.setText(str);
                      mpatternClew.setVisibility(View.VISIBLE);
                      updateStage(Stage.NeedToUnlockWrong);
                      postClearPatternRunnable();
		    }
		}
	};
	   private Runnable mClearPatternRunnable = new Runnable() {
           public void run() {
               mpatternClew.setText("");
               mpatternClew.setVisibility(View.INVISIBLE);
               mLockPatternView.clearPattern();
           }
       };

       // clear the wrong pattern unless they have started a new one
       // already
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
                   // enabled = false means: disable input, and have the
                   // appearance of being disabled.
                   mLockPatternView.setEnabled(false); // appearance of being disabled
                   break;
           }

           // Always announce the header for accessibility. This is a no-op
           // when accessibility is disabled.
      //     mHeaderTextView.announceForAccessibility(mHeaderTextView.getText());
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
       
       private void changeTextAlpha(final TextView textView){
               
               final AlphaAnimation aa = new AlphaAnimation(0.5f,1.0f);
               aa.setDuration(500);
               aa.setRepeatCount(1000);
               aa.setRepeatMode(Animation.REVERSE);
               textView.startAnimation(aa);
               
       }

    private void clearTextAlpha(final TextView textView) {
               textView.clearAnimation();
    }

    private boolean isStartUnlockFingerVerify() {
/*
        IEncryptService es = IEncryptService.Stub.asInterface(
           ServiceManager.getService(EncryptService.ENCRYPT_SERVICE));
        if(es != null) {
            try {
                return es.isStartUnlockFingerVerify(TAG);
            }
            catch(Exception e){
                Log.e(TAG, "Exception:"+e);
            }
        }
        else {
            Log.e(TAG, "cannot get IEncryptService!");
        }
*/
        return false;
    }

public void resetFingerFailedAttemps(){
    Log.d(TAG, "###@resetFingerFailedAttemps ####");
    FingerprintManager fm = (FingerprintManager) EncryptCheckActivity.this.getSystemService(Context.FINGERPRINT_SERVICE);
    if (fm != null) {
        byte[] token = null;
        fm.resetTimeout(token);
    }

}

}
