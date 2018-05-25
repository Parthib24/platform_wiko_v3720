package com.ape.backtouch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.internal.logging.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.Indexable;

import com.android.settings.widget.SwitchBar;
import android.widget.Switch;
import com.android.settings.SettingsActivity;
import android.util.Log;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.CheckBoxPreference;
import com.ape.emFramework.BtFrameworkStatic;
import com.ape.emFramework.BtFeatureOptions;

import com.android.settings.R;

public class BackTouchFragment extends SettingsPreferenceFragment implements Indexable,SwitchBar.OnSwitchChangeListener {

    private static final String TAG = "BackTouchFragment";

    private PreferenceScreen back_touch;
    private CheckBoxPreference back_touch_open_camera;
    private CheckBoxPreference back_touch_take_photos;
    private CheckBoxPreference back_touch_change_screen;
    private CheckBoxPreference back_touch_call;

    private static final String KEY_BACK_TOUCH = BtFrameworkStatic.KEY_BACK_TOUCH;
    private static final String KEY_BACK_TOUCH_OPEN_CAMERA = BtFrameworkStatic.KEY_BACK_TOUCH_OPEN_CAMERA;
    private static final String KEY_BACK_TOUCH_TAKE_PHOTOS = BtFrameworkStatic.KEY_BACK_TOUCH_TAKE_PHOTOS;
    private static final String KEY_BACK_TOUCH_CHANGE_SCREEN = BtFrameworkStatic.KEY_BACK_TOUCH_CHANGE_SCREEN;
    private static final String KEY_BACK_TOUCH_CALL = BtFrameworkStatic.KEY_BACK_TOUCH_CALL;

    private SwitchBar mSwitchBar;
    private Switch mSwitch;
    private boolean mValidListener = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

    }

    @Override
    protected int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ACCESSIBILITY;
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Log.i(TAG, "onSwitchChanged isChecked =" + isChecked);
        setMenuEnable(isChecked);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");

        createPreferenceHierarchy();
        initializeAllPreferences();
        
        final SettingsActivity activity = (SettingsActivity) getActivity();
        mSwitchBar = activity.getSwitchBar();
        mSwitch = mSwitchBar.getSwitch();
        
        mSwitchBar.show();
        mSwitch.setChecked(BtFrameworkStatic.getBackTouchState(BtFrameworkStatic.KEY_BACK_TOUCH));
        setMenuEnable(mSwitch.isChecked());

        if (!mValidListener) {
            mSwitchBar.addOnSwitchChangeListener(this);
            mValidListener = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == back_touch){
        }else if (preference == back_touch_call){
            setMenuItemValue(KEY_BACK_TOUCH_CALL,back_touch_call.isChecked());
        }else if (preference == back_touch_change_screen){
            setMenuItemValue(KEY_BACK_TOUCH_CHANGE_SCREEN,back_touch_change_screen.isChecked());
        }else if (preference == back_touch_open_camera){
            setMenuItemValue(KEY_BACK_TOUCH_OPEN_CAMERA,back_touch_open_camera.isChecked());
        }else if (preference == back_touch_take_photos){
            setMenuItemValue(KEY_BACK_TOUCH_TAKE_PHOTOS,back_touch_take_photos.isChecked());
        }

        return true;
    }

    private PreferenceScreen createPreferenceHierarchy() {
        final SettingsActivity activity = (SettingsActivity) getActivity();
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }

        addPreferencesFromResource(R.xml.back_touch);
        root = getPreferenceScreen();

        return root;
    }

    private void initializeAllPreferences() {
        back_touch = (PreferenceScreen) findPreference(KEY_BACK_TOUCH);
        back_touch_open_camera = (CheckBoxPreference) findPreference(KEY_BACK_TOUCH_OPEN_CAMERA);
        back_touch_take_photos = (CheckBoxPreference) findPreference(KEY_BACK_TOUCH_TAKE_PHOTOS);
        back_touch_change_screen = (CheckBoxPreference) findPreference(KEY_BACK_TOUCH_CHANGE_SCREEN);
        back_touch_call = (CheckBoxPreference) findPreference(KEY_BACK_TOUCH_CALL);
 
        if (BtFeatureOptions.isScreenOnOpenCamera()) {
            back_touch_open_camera.setSummary(R.string.zzz_back_touch_open_camera_summary);
        }

        updateAllPrefreferences();
    }

    private void updateAllPrefreferences(){
/*
        setMenuItemValue(KEY_BACK_TOUCH_OPEN_CAMERA,BtFrameworkStatic.getBackTouchState(KEY_BACK_TOUCH_OPEN_CAMERA));
        setMenuItemValue(KEY_BACK_TOUCH_TAKE_PHOTOS,BtFrameworkStatic.getBackTouchState(KEY_BACK_TOUCH_TAKE_PHOTOS));
        setMenuItemValue(KEY_BACK_TOUCH_CHANGE_SCREEN,BtFrameworkStatic.getBackTouchState(KEY_BACK_TOUCH_CHANGE_SCREEN));
        setMenuItemValue(KEY_BACK_TOUCH_CALL,BtFrameworkStatic.getBackTouchState(KEY_BACK_TOUCH_CALL));
*/

        back_touch_open_camera.setChecked(BtFrameworkStatic.getBackTouchState(KEY_BACK_TOUCH_OPEN_CAMERA));
        back_touch_take_photos.setChecked(BtFrameworkStatic.getBackTouchState(KEY_BACK_TOUCH_TAKE_PHOTOS));
        back_touch_change_screen.setChecked(BtFrameworkStatic.getBackTouchState(KEY_BACK_TOUCH_CHANGE_SCREEN));
        back_touch_call.setChecked(BtFrameworkStatic.getBackTouchState(KEY_BACK_TOUCH_CALL));
    }

    private void setMenuEnable(boolean enabled){
        Log.d(TAG, "enabled = " + enabled);
        setMenuItemValue(KEY_BACK_TOUCH,enabled);
        back_touch.setEnabled(enabled);
    }

    public void setMenuItemValue(String key,boolean isChecked){
        BtFrameworkStatic.setBackTouchState(key,isChecked);
    }
}
