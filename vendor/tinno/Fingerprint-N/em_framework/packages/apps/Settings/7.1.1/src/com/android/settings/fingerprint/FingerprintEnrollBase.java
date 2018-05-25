/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.settings.fingerprint;

import android.annotation.Nullable;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.InstrumentedActivity;
import com.android.settings.R;
import com.android.setupwizardlib.GlifLayout;

// TINNO BEGIN
import android.app.ActionBar;
import android.os.SystemProperties;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
// solve bug, 20161113.
//import android.app.StatusBarManager;
//import com.ape.emFramework.Log;
// TINNO END

/**
 * Base activity for all fingerprint enrollment steps.
 */
public abstract class FingerprintEnrollBase extends InstrumentedActivity
        implements View.OnClickListener {
    public static final int RESULT_FINISHED = FingerprintSettings.RESULT_FINISHED;
    static final int RESULT_SKIP = FingerprintSettings.RESULT_SKIP;
    static final int RESULT_TIMEOUT = FingerprintSettings.RESULT_TIMEOUT;

    protected byte[] mToken;
    protected int mUserId;

    // TINNO BEGIN
private static final String TAG = "FingerprintEnrollBase-tag";
    // solve bug, 20161113.
    //private StatusBarManager mStatusBarManager;
    // TINNO END

    //TINNO BEGIN
    //start by lipeng,for myos_ui
    protected boolean isMyosUi = SystemProperties.getBoolean("ro.pt.myos.ui_support",false);
    //TINNO END

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_FingerprintEnroll);
        mToken = getIntent().getByteArrayExtra(
                ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
        if (savedInstanceState != null && mToken == null) {
            mToken = savedInstanceState.getByteArray(
                    ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
        }
        mUserId = getIntent().getIntExtra(Intent.EXTRA_USER_ID, UserHandle.myUserId());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, mToken);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initViews();
    }

    protected void initViews() {

        // TINNO BEGIN
        // solve bug, 20161113.
        /*
        mStatusBarManager = (StatusBarManager) this.getSystemService(android.app.Service.STATUS_BAR_SERVICE);
	 if (mStatusBarManager != null) {
             mStatusBarManager.disable(StatusBarManager.DISABLE_HOME | StatusBarManager.DISABLE_RECENT);
	 }
	 else {
            Log.e(TAG, "mStatusBarManager is null -- err!");
	 }*/
        // TINNO END
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Button nextButton = getNextButton();
        if (nextButton != null) {
            nextButton.setOnClickListener(this);
        }

        //TINNO BEGIN
        //start by lipeng,for myos_ui
        if (isMyosUi){
            ActionBar bar = getActionBar();
            if (bar != null){
                bar.hide();
            }
            int headerId = getResources().getIdentifier("suw_layout_head_container", "id", getPackageName());
            LinearLayout header = (LinearLayout) getLayout().findViewById(headerId);
            header.setBackgroundResource(R.drawable.fp_enrollment_header);
            header.setGravity(Gravity.BOTTOM);
            ViewGroup.LayoutParams lp = header.getLayoutParams();
            lp.height = 606;
            header.setLayoutParams(lp);
            getLayout().findViewById(R.id.suw_layout_icon).setVisibility(View.GONE);
            TextView heaTextView = getLayout().getHeaderTextView();
            heaTextView.setTextColor(Color.WHITE);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        //TINNO END
    }

    protected GlifLayout getLayout() {
        return (GlifLayout) findViewById(R.id.setup_wizard_layout);
    }

    protected void setHeaderText(int resId, boolean force) {
        TextView layoutTitle = getLayout().getHeaderTextView();
        CharSequence previousTitle = layoutTitle.getText();
        CharSequence title = getText(resId);
        if (previousTitle != title || force) {
            if (!TextUtils.isEmpty(previousTitle)) {
                layoutTitle.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
            }
            getLayout().setHeaderText(title);
            setTitle(title);
        }
    }

    protected void setHeaderText(int resId) {
        setHeaderText(resId, false /* force */);
    }

    protected Button getNextButton() {
        return (Button) findViewById(R.id.next_button);
    }

    @Override
    public void onClick(View v) {
        if (v == getNextButton()) {
            onNextButtonClick();
        }
    }

    protected void onNextButtonClick() {
    }

    protected Intent getEnrollingIntent() {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", FingerprintEnrollEnrolling.class.getName());
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, mToken);
        if (mUserId != UserHandle.USER_NULL) {
            intent.putExtra(Intent.EXTRA_USER_ID, mUserId);
        }
        return intent;
    }


    // TINNO BEGIN
    // solve bug, 20161113.
    /*
    @Override
    protected void onStop() {
        if (mStatusBarManager != null) {
            mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
        }
	 else {
            Log.e(TAG, "mStatusBarManager disable err!");
	 }
        super.onStop();
    }*/    
    // TINNO END
	
}
