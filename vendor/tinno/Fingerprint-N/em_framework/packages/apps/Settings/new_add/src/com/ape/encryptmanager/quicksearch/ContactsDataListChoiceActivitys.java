package com.ape.encryptmanager.quicksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;  
import android.graphics.BitmapFactory;  
import android.provider.ContactsContract;  
import android.provider.ContactsContract.CommonDataKinds.Phone;  
import android.provider.ContactsContract.CommonDataKinds.Photo; 
import android.net.Uri;  
import android.content.ContentResolver;  
import android.content.ContentUris;  
import android.content.Context; 
import java.io.InputStream;  
import android.database.Cursor;
import com.ape.emFramework.Log;
import android.app.ActionBar;
import android.view.MenuItem;
import android.content.Intent;
import com.ape.encryptmanager.utils.EncryUtil;   
import android.widget.ImageButton;
import com.android.settings.R;

import com.ape.encryptmanager.quicksearch.SideBar.OnTouchingLetterChangedListener;

public class ContactsDataListChoiceActivitys extends Activity implements SearchActionBarInterface{
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private TextView mfiltle_title;
	private SortAdapter adapter;
	private ImageButton img_search;
	private ClearEditText mClearEditText;

	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;    

	private PinyinComparator pinyinComparator;

	private static final String[] PHONES_PROJECTION = new String[] {  
		Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID };   

	private static final int PHONES_DISPLAY_NAME_INDEX = 0;        

	private static final int PHONES_NUMBER_INDEX = 1;       

	private static final int PHONES_PHOTO_ID_INDEX = 2;

	private static final int PHONES_CONTACT_ID_INDEX = 3;      

	private ArrayList<String> mContactsName = new ArrayList<String>();      

	private ArrayList<String> mContactsNumber = new ArrayList<String>();   

	private ArrayList<Bitmap> mContactsPhonto = new ArrayList<Bitmap>(); 

	private static List<SortModel> mContactDatasList; 
	Context mContext = null; 
	private ActionBar mActionBar;
	private boolean mDisplayHomeAsUpEnabled = true;  

	private Intent mResultIntent;
	public int mFingerId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quick_search_activity);

		mContext = this;
/*
		mActionBar = getActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
			mActionBar.setHomeButtonEnabled(mDisplayHomeAsUpEnabled);
			mActionBar.setDisplayUseLogoEnabled(true);
		}  
		*/
		
		mActionBar = getActionBar();
		mActionBar.setCustomView(R.layout.quick_search_actionbar); 
       	mActionBar.setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP );
		 

		mContactDatasList = new ArrayList<SortModel>();
		mResultIntent = new Intent();
		mFingerId = this.getIntent().getIntExtra(EncryUtil.KEY_QUICK_BOOT_FINGERID,0);

		initViews();
	}

	private void initViews() {
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();

		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {

				int position = adapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					sortListView.setSelection(position);
				}

			}
		});

		sortListView = (ListView) findViewById(R.id.country_lvcountry);
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getApplication(), ((SortModel)adapter.getItem(position)).getName(), Toast.LENGTH_SHORT).show();
				mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_CONTACT_NAME,((SortModel)adapter.getItem(position)).getName());
				mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_CONTACT_NUMBER, ((SortModel)adapter.getItem(position)).getContactNumber());
				mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_CONTACT_PHOTO, ((SortModel)adapter.getItem(position)).getContactPhoto());
				mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_CONTACT_ID, ((SortModel)adapter.getItem(position)).getContactId());
                                mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_FINGERID, mFingerId);
				setResult(EncryUtil.CONTACT_FOR_QUICK_BOOT_RESULT_CODE,mResultIntent);
				finish();
			}
		});

		//SourceDateList = filledData(getResources().getStringArray(R.array.date));
		getPhoneContacts();

		Collections.sort(mContactDatasList, pinyinComparator);
		adapter = new SortAdapter(this, mContactDatasList);
		sortListView.setAdapter(adapter);


		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
		mClearEditText.setSearchActionBarObj(this);

		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		mfiltle_title = (TextView) mActionBar.getCustomView().findViewById(R.id.filter_title);

		img_search = (ImageButton) mActionBar.getCustomView().findViewById(R.id.search);
		img_search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mClearEditText.setVisibility(View.VISIBLE);
				img_search.setVisibility(View.GONE);
				mfiltle_title.setVisibility(View.GONE);
			}
		});
	}

	private void filterData(String filterStr){
		List<SortModel> filterDateList = new ArrayList<SortModel>();

		if(TextUtils.isEmpty(filterStr)){
			filterDateList = mContactDatasList;
		}else{
			filterDateList.clear();
			for(SortModel sortModel : mContactDatasList){
				String name = sortModel.getName();
				if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())){
					filterDateList.add(sortModel);
				}
			}
		}

		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}

	private void getPhoneContacts() {  

		ContentResolver resolver = mContext.getContentResolver();  

		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, 
				null, null, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);  

		if (phoneCursor != null) {  
			while (phoneCursor.moveToNext()) {  

				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  

				if (TextUtils.isEmpty(phoneNumber))  
					continue;  

				String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);  

				String sortLetters = setSortLetters(contactName);        

				Long contactId = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);  

				Long photoId = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);          

				Bitmap contactPhoto = null;  

				if(photoId > 0 ) {  
					Uri uri =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactId);  
					InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);  
					contactPhoto = BitmapFactory.decodeStream(input);  
				}else {  
					contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_person_dk);  
				}  

				mContactDatasList.add(new SortModel(contactName, phoneNumber, photoId, contactId,sortLetters));
			}  

			phoneCursor.close();  
		}  
	} 

	private String setSortLetters(String date){
		String pinyin = characterParser.getSelling(date);
		String sortString = pinyin.substring(0, 1).toUpperCase();

		if(sortString.matches("[A-Z]")){
			return sortString.toUpperCase();
		}else{
			return "#";
		}		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:// 点击返回图标事件
			this.finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void notifyUiChaged() {
		// TODO Auto-generated method stub
		mClearEditText.setVisibility(View.INVISIBLE);
		img_search.setVisibility(View.VISIBLE);
		mfiltle_title.setVisibility(View.VISIBLE);
	}

}
