package com.ape.encryptmanager.quickaction;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.os.Handler;
import android.os.Message;

import java.util.Locale;
import java.util.logging.LogRecord;

import android.content.ContentValues;

import com.ape.encryptmanager.AppDataListChoiceActivity;
import com.android.settings.R;
import com.ape.encryptmanager.quicksearch.ContactsDataListChoiceActivitys;
import com.ape.encryptmanager.utils.EncryUtil;
import com.ape.encryptmanager.utils.MessageType;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.ContactsContract;
import com.ape.emFramework.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Vibrator;
import com.ape.encryptmanager.RoundedQuickActionImageView;
import android.hardware.fingerprint.FingerprintManager;
//import com.apefinger.util.MainFeatureOptions;
import com.ape.emFramework.emService.ApeCustomFpManager;
import android.hardware.fingerprint.Fingerprint;
import android.text.TextUtils;
import android.hardware.fingerprint.ApeFpData;
import android.content.pm.ApplicationInfo;

public class QuickActionFingerListFragment extends Fragment implements OnItemClickListener {

        private static final Uri PROVIDER_URI_FINGERPRINT = EncryUtil.URI_FINGERPRINT;
        		//Uri.parse("content://com.ape.encryptmanager/fingerprint");
        

        private static final String TAG = "QuickActionFingerListFragment";

        private FingerPrintListAdapter mFingerPrintListAdapter = null;
        private List<ApeFpData> mFingerList;
        private List<Fingerprint> mOriginFingerList;
		
        private LayoutInflater mInflater;
        private ListView mFingerListView;
        private View mLineView;        
        
        private PreferenceScreen root;
        private static Context mContext;
        private AlertDialog mFingerEditAlertDialog;
	 private ApeFpData mApeFpDataItem;	
	 
        private static final int TINNO_MSG_CHANGE_BG = 300;
        private static final int   MSG_FINGERPRINT_DELAY_VERIFY_START = 301;

        private Boolean serviceConnFlag = false;
        private Boolean verifyStartFlag = false;
        private int fingerId;
       // DataManager mDataManager;
	     private Vibrator mVb;
        private View mListHeaderView;

       private TextView quick_action_dialog_title;
    	private LinearLayout quick_action_open_app;
    	private LinearLayout quick_action_special_contact;
    	private LinearLayout quick_action_unlock_only;
       private FingerprintManager mFingerprintManager;  
       
    	private static AlertDialog mLinkDialog;
    	private View mDialogView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
                // TODO Auto-generated method stub
                super.onCreate(savedInstanceState);
                mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mContext = getActivity();
		  mVb = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                mFingerprintManager = (FingerprintManager) mContext.getSystemService(
                    Context.FINGERPRINT_SERVICE);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                View v = inflater.inflate(R.layout.fragment_fingerprint_quickaction, container, false);
                mFingerListView = (ListView) v.findViewById(R.id.finger_list);
                mListHeaderView = inflater.inflate(R.layout.fingerprint_quickaction_list_item_header, null);
                mFingerListView.addHeaderView(mListHeaderView, null, false);
                return v;
        }

        @Override
        public void onResume() {
                // TODO Auto-generated method stub
                super.onResume();
                updateFingerPrintList();
                if (mFingerList != null && mFingerList.size() == 0) {
                    Log.d("FingerprintMainScreen-cls","222222222");
                    //getActivity().finish();
                }
                mFingerPrintListAdapter = new FingerPrintListAdapter();
                mFingerListView.setAdapter(mFingerPrintListAdapter);
                mFingerListView.setHeaderDividersEnabled(false);				

        }

        @Override
        public void onPause() {
                // TODO Auto-generated method stub
                super.onPause();
        }

        private void showEditDialog(View view, int fingerId, String fingerName) {
        	
        	       view =mInflater.inflate(R.layout.quick_action_select_dialog, null);              
        	       mLinkDialog =  new AlertDialog.Builder(getActivity())
	               .setView(view)
	               .create();
				   
                    ApeFpData apeFpDataItem = mFingerprintManager.getApeFpDataItem(mContext.getUserId(),fingerId);
					
                    quick_action_dialog_title = (TextView)view.findViewById(R.id.quick_action_dialog_title);
                    quick_action_dialog_title.setText(fingerName.toString());
                    setQMbileString(view);
                    
 			quick_action_special_contact = (LinearLayout)view.findViewById(R.id.quick_action_dialog_contact_item_layout);
 			quick_action_special_contact.setTag(fingerId);
 			quick_action_special_contact.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Integer fingerId = (Integer) v.getTag();
				Intent intent = new Intent(getActivity(),ContactsDataListChoiceActivitys.class);
				intent.putExtra(EncryUtil.REQUEST_INTENT_FROM, "QuickBoot");
                                intent.putExtra(EncryUtil.KEY_QUICK_BOOT_FINGERID, fingerId);
				startActivityForResult(intent,0);
				mLinkDialog.dismiss();

			        }
		        });        	      

 			 quick_action_open_app = (LinearLayout)view.findViewById(R.id.quick_action_dialog_app_item_layout);
 			 quick_action_open_app.setTag(fingerId);
 			 quick_action_open_app.setOnClickListener(new View.OnClickListener() {
	
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Integer fingerId = (Integer) v.getTag();
						Intent intent = new Intent(getActivity(),AppDataListChoiceActivity.class);
						intent.putExtra(EncryUtil.REQUEST_INTENT_FROM, "QuickBoot");
                                                intent.putExtra(EncryUtil.KEY_QUICK_BOOT_FINGERID, fingerId);
						startActivityForResult(intent,0);
						mLinkDialog.dismiss();
					}
				});

                       quick_action_unlock_only = (LinearLayout)view.findViewById(R.id.quick_action_dialog_unlock_item_layout);
                       if(!apeFpDataItem.isQuickBootDataEmpty()){      
                           quick_action_unlock_only.setVisibility(View.VISIBLE);
                       }      
                       quick_action_unlock_only.setTag(fingerId);
            	         quick_action_unlock_only.setOnClickListener(new View.OnClickListener() {

            			@Override
            			public void onClick(View v) {
            				// TODO Auto-generated method stub
            				Integer fingerId = (Integer) v.getTag();
					mFingerprintManager.updateQBPackageInfo(fingerId, mContext.getUserId(), 
					    "", "", "", "", "", "");
					mFingerprintManager.updateQBContactsInfo(fingerId, mContext.getUserId(), 
					    "", "", -1, -1);		   					   
                                   updateFingerPrintList();
                                   mFingerPrintListAdapter.notifyDataSetChanged();
            				mLinkDialog.dismiss();
            			}
            		   });

                       mLinkDialog.show();  
				      
        }



        private void setQMbileString(View view){
            //if(MainFeatureOptions.isQmobile()&& view !=null){
            if(false && view !=null){
                TextView appItem  = (TextView)view.findViewById(R.id.quick_action_dialog_app_item_str);
                TextView contactItem  = (TextView)view.findViewById(R.id.quick_action_dialog_contact_item_str);
                String language = Locale.getDefault().getLanguage();
                if(language !=null && language.equals("en")) {
                    if (appItem != null) {
                        appItem.setText(R.string.quick_action_dialog_app_item_str_pk);
                    }
                    if (contactItem != null) {
                        contactItem.setText(R.string.quick_action_dialog_contact_item_str_pk);
                    }
                }
            }


        }

        public boolean updateFingerPrintList() {
                mOriginFingerList = mFingerprintManager.getEnrolledFingerprints(mContext.getUserId());
		mFingerList = mFingerprintManager.getApeFpDataList(mContext.getUserId());				
                return true;

        }
   
        private Drawable getAppInfoIcon(String packageName,String className)
    	 {	
                Log.d(TAG,"getAppInfoIcon packageName = " + packageName + " | className = " + className);
    	         if (packageName.equals("com.android.vending")) {
    	             className = "com.android.vending.AssetBrowserActivity";
    	         }
                try {
    		      PackageManager pm = mContext.getPackageManager();
    		      Drawable appIcon = pm.getActivityIcon(new ComponentName(packageName,className));
    		      return appIcon;
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                } 
                return null;
	
    	 }    
        public Drawable getAppInfoIconByPackageName(String packageName) {

            // TODO Auto-generated constructor stub
            Log.d(TAG,"getAppInfoIconByPackageName packageName = " + packageName);
            try {
                PackageManager pm = mContext.getPackageManager();
                ApplicationInfo application=pm.getPackageInfo(packageName, 0).applicationInfo;		
                Drawable appIcon = application.loadIcon(pm);
                return appIcon;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } 
            return null;

        }

        private boolean isSamePackageNameApp(String packageName,String className) {	
    	    if (packageName.equals("com.ape.myseneschal") && className.equals("com.ape.oneclean.shortcut.OneCleanShortcut")) {
    	        return true;
            }
            return false;
    	} 
    	
        private Drawable getContactPhotoImage(long photoId, long contactId){

            Log.d(TAG, "getContactPhotoImage photoId =" + photoId + " | contactId =" + contactId + "@@@@@@@@@@@");

            ContentResolver resolver = mContext.getContentResolver();
            Bitmap contactPhoto;
            if(photoId > 0 ) {  
                Uri uri =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactId);  
                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);  
                contactPhoto = BitmapFactory.decodeStream(input);
                if(contactPhoto == null){
                    Log.d(TAG, "getContactPhotoImage contactPhoto is null, set to default");
                    contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_person_dk);
                }
            } else {  
                contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_person_dk);  
            }  
            return new BitmapDrawable(contactPhoto);
        }        	

   /*    private String getContactName(String number) {  
            if (TextUtils.isEmpty(number)) {  
                return null;  
            }          
      
            final ContentResolver resolver = mResolver.get();  
              
            Uri lookupUri = null;  
            String[] projection = new String[] { PhoneLookup._ID, PhoneLookup.DISPLAY_NAME };  
            Cursor cursor = null;  
            try {  
                lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));  
                cursor =resolver.query(lookupUri, projection, null, null, null);  
            } catch (Exception ex) {  
                ex.printStackTrace();  
                try {  
                    lookupUri = Uri.withAppendedPath(android.provider.Contacts.Phones.CONTENT_FILTER_URL,  
                            Uri.encode(number));  
                    cursor = resolver.query(lookupUri, projection, null, null, null);  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
      
            String ret = null;  
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {  
                ret = cursor.getString(1);  
            }  
      
            cursor.close();  
            return ret;  
        }  
*/
		
    	

        class FingerPrintListAdapter extends BaseAdapter implements OnClickListener {
        		
        	  private RoundedQuickActionImageView mFingerImage = null;
                private TextView mFingerName = null;
                private TextView mFingerFunction = null;
                private RelativeLayout mFingerEdit = null;

                @Override
                public int getCount() {

                        if (mFingerList != null) {
                                return mFingerList.size();
                        }
                        return 0;
                }

                @Override
                public Object getItem(int position) {

                        if (mFingerList != null) {
                                if (position < mFingerList.size()) {
                                        return mFingerList.get(position);
                                }
                        }
                        return null;
                }

                @Override
                public long getItemId(int position) {

                        return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                        View view = mInflater.inflate(R.layout.fingerprint_quickaction_list_item, parent, false);
                        Log.i(TAG, "getView position = " + position + "FingerEdit.onClick()");
			   ApeFpData apeFpDataItem = mFingerList.get(position);			
                        int fingerId;
		          if (apeFpDataItem != null) {
                            fingerId = apeFpDataItem.getFingerId();
			   } 				
			   Fingerprint fpItem = mOriginFingerList.get(position);				
                        //QuickBootData quickBootData;
                        mFingerImage = (RoundedQuickActionImageView) view.findViewById(R.id.finger_image);
                        mFingerName = (TextView) view.findViewById(R.id.finger_name);
                        mFingerFunction = (TextView) view.findViewById(R.id.finger_function);
                        mFingerEdit = (RelativeLayout) view.findViewById(R.id.finger_edit);

                        StringBuffer showFunction = new StringBuffer();

                        boolean functionFlag = false;
                        if (apeFpDataItem.isQuickBootDataEmpty()) {
                                showFunction.append(mContext.getResources().getString(R.string.finger_for_screen_lock));
                                mFingerImage.setImageResource(R.drawable.unlock_icon);

                        } else if (!apeFpDataItem.isPackageInfoEmpty()) {
                        	    String packageName = apeFpDataItem.getQBPackageName();
                        	    String className = apeFpDataItem.getQBClassName();
                                    
                                Drawable AppIcon = getAppInfoIconByPackageName(packageName);//getAppInfoIcon(packageName, className);
                                
                                // add for the app Which has the same package name.
                                if (isSamePackageNameApp(packageName, className)){
                                    AppIcon = getAppInfoIcon(packageName, className);
                                }

                                if (AppIcon != null ) {
                    	               mFingerImage.setImageDrawable(AppIcon);
                                } else {
                                    mFingerImage.setImageResource(R.drawable.lock_icon);
                                }

                                showFunction.append(apeFpDataItem.getQBAppName());
                                functionFlag = true;
                        } else if (!apeFpDataItem.isContactInfoEmpty()) {	
                        	    long photoId = apeFpDataItem.getQBContactPhotoId();
				    long contactId = apeFpDataItem.getQBContactId();
				    mFingerImage.setImageDrawable(getContactPhotoImage(photoId, contactId));
                                showFunction.append(apeFpDataItem.getQBPhoneName());
                                functionFlag = true;
                        }

                        //if (functionFlag && mFingerList.get(position).getAllowUnlockAppValue() == 1) {
                             //   showFunction.append(";");
                            //    showFunction.append(mContext.getResources().getString(R.string.finger_for_privacy_lock));
                        //}
                        if (showFunction.length() > 0) {
                                mFingerFunction.setText(showFunction);
                        }

                        mFingerName.setText(fpItem.getName().toString());
                        mFingerEdit.setOnClickListener(this);
                        mFingerEdit.setTag(new Integer(position));
                        return view;
                }

                @Override
                public void onClick(View view) {
                        Integer position = (Integer) view.getTag();
			   int fingerId = mFingerList.get(position).getFingerId();			
                        Log.i(TAG, "position = " + position + "FingerEdit.onClick()");
                        Log.i(TAG, "view.getId() = " + view.getId() + "FingerEdit.onClick()");
                        mApeFpDataItem = mFingerList.get(position);
			   Fingerprint fpItem = mOriginFingerList.get(position);//mFingerprintManager.getFingerprintById(mContext.getUserId(), fingerId);
                        if (view.getId() == R.id.finger_edit) {
                        	   showEditDialog(view,mFingerList.get(position).getFingerId(),fpItem.getName().toString()); 	   
                        }
                }

        }

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // TODO Auto-generated method stub
                int fingerId = mFingerList.get(position).getFingerId();
                Log.i(TAG, "onItemClick position = " + position);
                Log.i(TAG, "onItemClick v.getId() = " + v.getId());
		  Fingerprint fpItem = mOriginFingerList.get(position);//mFingerprintManager.getFingerprintById(mContext.getUserId(), fingerId);							
                showEditDialog(v,mFingerList.get(position).getFingerId(),fpItem.getName().toString());

        }

		@Override
		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			// TODO Auto-generated method stub
			super.onActivityResult(requestCode, resultCode, data);
			Log.d("xiaowen", "requestCode =" + requestCode + "| resultCode = " + resultCode + "   222222222222222################");	
			
			ApeFpData apeFpDataItem;
			if(data == null || resultCode ==0)
				return;	

                        //add by zhiqin.lin,for fingerId is negative
                        requestCode = data.getIntExtra(EncryUtil.KEY_QUICK_BOOT_FINGERID,0);
			Log.d(TAG, "######## requestCode =" + requestCode + " ########");	
                        	
			switch(resultCode)
			{
				case EncryUtil.CONTACT_FOR_QUICK_BOOT_RESULT_CODE:
					String contactName;
					String contactNumber;
					int photoId;
					int contactId;
					Drawable contactPhotoDrawable;
					contactName = data.getStringExtra(EncryUtil.KEY_QUICK_BOOT_CONTACT_NAME);
					contactNumber = data.getStringExtra(EncryUtil.KEY_QUICK_BOOT_CONTACT_NUMBER);
					photoId = (int) data.getLongExtra(EncryUtil.KEY_QUICK_BOOT_CONTACT_PHOTO, -1);
					contactId = (int) data.getLongExtra(EncryUtil.KEY_QUICK_BOOT_CONTACT_ID, -1);

					apeFpDataItem = mFingerprintManager.getApeFpDataItem(mContext.getUserId(), requestCode);
					
					/*QuickBootData quickBootData1 = new QuickBootData(contactName,contactNumber);	
					fingerItem.setQuickBootData(quickBootData1);
					
					fingerItem.setContactPhotoId(photoId);
					fingerItem.setContactId(contactId);
					
					mFingerPrintDataUtils.UpdateFingerPrintData(fingerItem);*/
					mFingerprintManager.updateQBContactsInfo(requestCode, mContext.getUserId(), 
					    contactName, contactNumber, photoId, contactId);
					
					Log.d("xiaowen", "fingerId =" + requestCode + 
						       "| resultCode = " + resultCode + 
							"| contactName =" + contactName + 
							"| contactNumber = " + contactNumber + 
							"| photoId = " + photoId +
							"| contactId =" + contactId);

					break;
				case EncryUtil.APP_FOR_QUICK_BOOT_RESULT_CODE:
					String appName = "";
					String packageName = "";
					String className = "";
					String category = "";
					String action = "";
					String data_uri = "";
					
					appName = data.getStringExtra(EncryUtil.KEY_QUICK_BOOT_APP_NAME);
					packageName = data.getStringExtra(EncryUtil.KEY_QUICK_BOOT_PACKAGE_NAME);
					className = data.getStringExtra(EncryUtil.KEY_QUICK_BOOT_CLASS_NAME);	
					category = data.getStringExtra(EncryUtil.KEY_QUICK_BOOT_APP_CATEGORY);	
					action = data.getStringExtra(EncryUtil.KEY_QUICK_BOOT_APP_ACTION);	
					data_uri = data.getStringExtra(EncryUtil.KEY_QUICK_BOOT_APP_DATA);	
					
					apeFpDataItem = mFingerprintManager.getApeFpDataItem(mContext.getUserId(), requestCode);;
					
					/*QuickBootData quickBootData2 = new QuickBootData(appName,packageName,className);	
					fingerItem.setQuickBootData(quickBootData2);
					mFingerPrintDataUtils.UpdateFingerPrintData(fingerItem);*/
					
					mFingerprintManager.updateQBPackageInfo(requestCode, mContext.getUserId(), 
					    appName, packageName, className, category, action, data_uri);					
					
					Log.d(TAG, "fingerId =" + requestCode + "| resultCode = " + resultCode + 
							"| appName =" + appName +
							"| packageName = " + packageName + 
							"| className = "+ className + 
							"| category = "+ category + 
							"| action = "+ action + 
							"| data_uri = "+ data_uri);		
					break;
				default:
					break;
			}			
		}

}
