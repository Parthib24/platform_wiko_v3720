package com.ape.encryptmanager;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Environment;

import com.ape.emFramework.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;


public class EncryptBaseActivity extends Activity {
    private ActionBar mActionBar;
    private boolean mDisplayHomeAsUpEnabled = true;
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
            mActionBar.setHomeButtonEnabled(mDisplayHomeAsUpEnabled);
            mActionBar.setDisplayShowHomeEnabled(false);
        }

//for tst.
//	Log.i("EncryptBaseActivity", "chip info:"+com.ape.encryptmanager.utils.EncryUtil.getFp_DrvInfo());

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == android.R.id.home)
		{
		       /*Intent upIntent = NavUtils.getParentActivityIntent(this);  
		        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {  
		            TaskStackBuilder.create(this)  
		                    .addNextIntentWithParentStack(upIntent)  
		                    .startActivities();
		        } else {  
		            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
		            NavUtils.navigateUpTo(this, upIntent);  
		        }*/
			    finish();
		        return true;  
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}
