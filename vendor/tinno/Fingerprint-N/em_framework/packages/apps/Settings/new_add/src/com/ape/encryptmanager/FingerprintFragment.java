package com.ape.encryptmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.internal.logging.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.Indexable;

public class FingerprintFragment extends SettingsPreferenceFragment implements Indexable {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getContext();
        ComponentName cpnm = new ComponentName("com.android.settings", FingerprintMainScreen.class.getName());
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.setComponent(cpnm);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(mainIntent);
        finish();
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ACCESSIBILITY;
    }
}
