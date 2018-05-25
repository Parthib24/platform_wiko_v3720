package com.ape.encryptmanager.privacylock;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.android.settings.R;
import com.ape.encryptmanager.utils.EncryUtil;
//import com.apefinger.util.MainFeatureOptions;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.Fingerprint;
import com.ape.emFramework.EmFrameworkStatic;
import android.content.Context;
import android.hardware.fingerprint.ApeFpData;


import java.util.List;

public class PrivacyLockActivity extends PreferenceActivity implements OnPreferenceChangeListener  {
	public static final String KEY_GROUP = "key_preferencecategory";

	public static final String   PREFERENCE_DATA_KEY = "switchpreference_data_key" ;
    public static final int CHANGE_PASSWORD_CODE = 100;
    public static final int CHANGE_POTECTION_CODE = 101;
    public static final int CHANGE_PASSWORD_RETURN_CODE = 102;
    public static final int CHANGE_POTECTION_RETURN_CODE = 103;
    public static final int FORGET_PASSWORD_REQ = 104;
    
	private PreferenceCategory mPreferenceGroup;
	private ActionBar mActionBar;
	private boolean mDisplayHomeAsUpEnabled = true;
	private FingerprintManager mFingerprintManager;
	//private List<Fingerprint> mFingerList;
	private List<ApeFpData> mFingerList;
	
	private MenuItem changePassword;
	private MenuItem changePotection;
	private ListView listView;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.privacylock_preference);
		mActionBar = getActionBar();
		if (mActionBar != null) {
                        //if(!MainFeatureOptions.isAppLockSupported(this)){
			if(true){
				mActionBar.setTitle(R.string.file_lock_title);
			}else {
				mActionBar.setTitle(R.string.app_and_file_lock_title);
			}
			mActionBar.setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
			mActionBar.setHomeButtonEnabled(mDisplayHomeAsUpEnabled);
			mActionBar.setDisplayUseLogoEnabled(true);
		}
		mPreferenceGroup = (PreferenceCategory) findPreference(KEY_GROUP);
		listView = getListView();
	//	listView.setDivider(null);
	//	listView.setFooterDividersEnabled(false);
              mFingerprintManager = (FingerprintManager) this.getSystemService(
                  Context.FINGERPRINT_SERVICE);	
	}

	private void updateUI() {
		mFingerList = mFingerprintManager.getApeFpDataList(this.getUserId());//mFingerprintManager.getEnrolledFingerprints(this.getUserId());
		mPreferenceGroup.removeAll();
		for (int i = 0; i < mFingerList.size(); i++) {
			ApeFpData item = mFingerList.get(i);
			Fingerprint fp = mFingerprintManager.getFingerprintById(this.getUserId(), item.getFingerId());
			Boolean enable = item.getAllowUnlockAppValue() == 1;
			SwitchPreference f = new SwitchPreference(this);
			f.setTitle(fp.getName());
			f.setSummary(enable ? R.string.switch_on :R.string.switch_off);
			f.setIcon(enable ? R.drawable.unlock_icon : R.drawable.lock_icon);
			f.setChecked(enable);
			Bundle b = 	f.getExtras();
			b.putInt(PREFERENCE_DATA_KEY, i);
			mPreferenceGroup.addPreference(f);		
			f.setOnPreferenceChangeListener(this);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateUI();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.privacy_menu, menu);

		changePassword = menu.findItem(R.id.action_change_password);
		changePotection = menu.findItem(R.id.action_change_potection);
		// if (encryptServiceUtil == null) {
		// encryptServiceUtil = EncryptServiceUtil.getInstance(mCtx);
		// }
		Boolean isShowMenu = true; // !encryptServiceUtil.isMustSetPassword();
		changePassword.setVisible(isShowMenu);
		changePotection.setVisible(isShowMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.action_change_password: {
			
			  Intent i = new Intent();
				i.setClassName(getPackageName(),
						"com.ape.encryptmanager.privacylock.EncryptCheckActivity");
			  Bundle data = new Bundle(); data.putInt(EncryUtil.REQ_KEY, EncryptCheckActivity.Other);
			  i.putExtras(data); startActivityForResult(i,
			  CHANGE_PASSWORD_CODE);
			 
		}
			break;
		case R.id.action_change_potection: {

			Intent i = new Intent();
			i.setClassName(getPackageName(),
					"com.ape.encryptmanager.privacylock.EncryptCheckActivity");
			Bundle data = new Bundle();
			data.putInt(EncryUtil.REQ_KEY, EncryptCheckActivity.Other);
			i.putExtras(data);
			startActivityForResult(i, CHANGE_POTECTION_CODE);

		}
			break;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		
		SwitchPreference  p = (SwitchPreference)preference;
		 int index = p.getExtras().getInt(PREFERENCE_DATA_KEY, -1);
		if(index>=0 && index <mFingerList.size()){
			Boolean v = (Boolean)newValue;
			p.setSummary(v ? R.string.switch_on :R.string.switch_off);
			p.setIcon(v  ? R.drawable.unlock_icon : R.drawable.lock_icon);
			//Fingerprint item = mFingerList.get(index);
			ApeFpData item = mFingerList.get(index);
			item.setAllowUnlockAppValue(v ? 1 : 0);
			mFingerprintManager.updatePrivLockValue(item.getFingerId(), this.getUserId(), item.getAllowUnlockAppValue());
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CHANGE_PASSWORD_CODE && data != null) {
			Bundle bundle = data.getExtras();
			if(bundle !=null){
			String res = bundle.getString(EncryUtil.RESULT_KEY, null);
		 	Boolean isForget  = bundle.getBoolean(EncryUtil.FORGET_PASSWORD_KEY, false);

			if (res != null && !isForget ) {
				Intent i = new Intent();
				i.setClassName(getPackageName(),
						"com.ape.encryptmanager.privacylock.EncryptSettingActivity");
				Bundle mBundle = new Bundle();
				mBundle.putInt(EncryUtil.REQ_KEY,
						EncryptSettingActivity.CHANGE_PASSWORD_CODE);
				i.putExtras(mBundle);
				startActivityForResult(i, CHANGE_PASSWORD_RETURN_CODE);
			} else {

			}
			}
		} else if (requestCode == CHANGE_POTECTION_CODE && data != null) {
			Bundle bundle = data.getExtras();
			if(bundle !=null){
			String res = bundle.getString(EncryUtil.RESULT_KEY, null);
			if (res != null) {
				Intent i = new Intent();
				i.setClassName(getPackageName(),
						"com.ape.encryptmanager.privacylock.PasswordPotectionActivity");
				Bundle mBundle = new Bundle();
				mBundle.putInt(EncryUtil.REQ_KEY,
						PasswordPotectionActivity.CHANGE_PASSWORD_POTECTION);
				i.putExtras(mBundle);
				startActivityForResult(i, CHANGE_POTECTION_RETURN_CODE);
			} else {

			}
			}
		} else if (requestCode == CHANGE_PASSWORD_RETURN_CODE
				|| requestCode == CHANGE_POTECTION_RETURN_CODE) {

		}

	}
	
}
