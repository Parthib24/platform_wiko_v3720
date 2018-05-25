package com.ape.encryptmanager.quickaction;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import android.util.AttributeSet;
import com.ape.emFramework.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ape.encryptmanager.EncryptBaseActivity;
import com.android.settings.R;

public class QuickActionFingerListActivity extends EncryptBaseActivity {
    public static final int MSG_DELAYED_SHOW = 1;
    public static final int ADD_FINGERPRINT_REQ = 100;
    public static final int AUTO_ADD_FINGERPRINT_REQ = 101;
    public static final String PREFERENCE_SHOW_GUIDE_KEY = "preference_show_guide_key";

    private ActionBar mActionBar;
    private boolean mDisplayHomeAsUpEnabled = true;
    private int mFingerListSize = 0;
    private SharedPreferences preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_fingerprint_quickaction);
        preference = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);        
        switch (requestCode) {

            default:
                break;
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

        }
    };

}
