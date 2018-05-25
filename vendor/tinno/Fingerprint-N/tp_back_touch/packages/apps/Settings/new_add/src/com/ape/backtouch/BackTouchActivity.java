package com.ape.backtouch;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;

import com.ape.backtouch.service.BtService;
import com.ape.backtouch.util.SharePreferenceUtil;
import com.ape.backtouch.widget.SwitchBar;
import com.ape.backtouch.widget.ToggleSwitch;

import com.android.settings.R;

public class BackTouchActivity extends Activity {

    public static final String TAG = "BackTouchActivity";

    private PreferenceScreen back_touch;
    private CheckBoxPreference back_touch_open_camera;
    private CheckBoxPreference back_touch_take_photos;
    private CheckBoxPreference back_touch_change_screen;
    private CheckBoxPreference back_touch_call;

    private static final String KEY_BACK_TOUCH = "back_touch";
    private static final String KEY_BACK_TOUCH_OPEN_CAMERA = "back_touch_open_camera";
    private static final String KEY_BACK_TOUCH_TAKE_PHOTOS = "back_touch_take_photos";
    private static final String KEY_BACK_TOUCH_CHANGE_SCREEN = "back_touch_change_screen";
    private static final String KEY_BACK_TOUCH_CALL = "back_touch_call";

    private SwitchBar mSwitchBar;
    protected ToggleSwitch mToggleSwitch;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mSwitchBar = (SwitchBar)findViewById(R.id.switch_bar);
        startBtService();
        sWitchToFragment();
*/


    }

/*
    private void startBtService(){
        Intent mBtServiceIntent = new Intent(this, BtService.class);
        this.startService(mBtServiceIntent);
    }

    public SwitchBar getSwitchBar(){
        Log.d(TAG,"getSwitchBar()");
        return mSwitchBar;
    }

    private void sWitchToFragment(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        BackTouchFragment rightFragment = new BackTouchFragment();
        transaction.add(R.id.content, rightFragment);
        transaction.commit();
    }

    public static class BackTouchFragment extends PreferenceFragment{

        public static final String TAG = "BtEventManager";

        protected ToggleSwitch mToggleSwitch;
        private SwitchBar mSwitchBars;
        private SharePreferenceUtil mSharePreferenceUtil;

        private PreferenceScreen back_touch;
        private CheckBoxPreference back_touch_open_camera;
        private CheckBoxPreference back_touch_take_photos;
        private CheckBoxPreference back_touch_change_screen;
        private CheckBoxPreference back_touch_call;

        private static final String KEY_BACK_TOUCH = "back_touch";
        private static final String KEY_BACK_TOUCH_OPEN_CAMERA = "back_touch_open_camera";
        private static final String KEY_BACK_TOUCH_TAKE_PHOTOS = "back_touch_take_photos";
        private static final String KEY_BACK_TOUCH_CHANGE_SCREEN = "back_touch_change_screen";
        private static final String KEY_BACK_TOUCH_CALL = "back_touch_call";
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.back_touch);
            mSharePreferenceUtil = new SharePreferenceUtil(this.getContext());
            initializeAllPreferences();
            onInstallActionBarToggleSwitch();
        }

        protected void onInstallActionBarToggleSwitch() {
            final BackTouchActivity activity = (BackTouchActivity) getActivity();
            mSwitchBars = activity.getSwitchBar();
            
            mSwitchBars.show();
            mToggleSwitch = mSwitchBars.getSwitch();
            mToggleSwitch.setChecked(mSharePreferenceUtil.getSharePreference(mSharePreferenceUtil.BT));
            setMenuEnable(mToggleSwitch.isChecked());

            mToggleSwitch.setOnBeforeCheckedChangeListener(new ToggleSwitch.OnBeforeCheckedChangeListener() {
                @Override
                public boolean onBeforeCheckedChanged(
                        ToggleSwitch toggleSwitch, boolean checked) {
                    toggleSwitch.setCheckedInternal(checked);
                    setMenuEnable(checked);
                    return false;
                }
            });
        }

        private void setMenuEnable(boolean enabled){
            Log.d(TAG, "enabled = " + enabled);
            setMenuItemValue(mSharePreferenceUtil.BT, enabled);
            back_touch.setEnabled(enabled);
        }

        private void initializeAllPreferences() {
            back_touch = (PreferenceScreen) findPreference(KEY_BACK_TOUCH);
            back_touch_open_camera = (CheckBoxPreference) findPreference(KEY_BACK_TOUCH_OPEN_CAMERA);
            back_touch_take_photos = (CheckBoxPreference) findPreference(KEY_BACK_TOUCH_TAKE_PHOTOS);
            back_touch_change_screen = (CheckBoxPreference) findPreference(KEY_BACK_TOUCH_CHANGE_SCREEN);
            back_touch_call = (CheckBoxPreference) findPreference(KEY_BACK_TOUCH_CALL);

            updateAllPrefreferences();
        }

        private void updateAllPrefreferences(){
            setMenuItemValue(mSharePreferenceUtil.BT_CALL, back_touch_call.isChecked());
            setMenuItemValue(mSharePreferenceUtil.BT_ROTATE,back_touch_change_screen.isChecked());
            setMenuItemValue(mSharePreferenceUtil.BT_CAMERA, back_touch_open_camera.isChecked());
            setMenuItemValue(mSharePreferenceUtil.BT_PHOTOS,back_touch_take_photos.isChecked());

            mSharePreferenceUtil.printMenuLog();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == back_touch){
            }else if (preference == back_touch_call){
                setMenuItemValue(mSharePreferenceUtil.BT_CALL, back_touch_call.isChecked());
            }else if (preference == back_touch_change_screen){
                setMenuItemValue(mSharePreferenceUtil.BT_ROTATE,back_touch_change_screen.isChecked());
            }else if (preference == back_touch_open_camera){
                setMenuItemValue(mSharePreferenceUtil.BT_CAMERA,back_touch_open_camera.isChecked());
            }else if (preference == back_touch_take_photos){
                setMenuItemValue(mSharePreferenceUtil.BT_PHOTOS,back_touch_take_photos.isChecked());
            }
            mSharePreferenceUtil.printMenuLog();
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        public void setMenuItemValue(String menuItem,boolean isChecked){
            mSharePreferenceUtil.putSharePreferencce(menuItem,isChecked);
        }
    }
*/

}
