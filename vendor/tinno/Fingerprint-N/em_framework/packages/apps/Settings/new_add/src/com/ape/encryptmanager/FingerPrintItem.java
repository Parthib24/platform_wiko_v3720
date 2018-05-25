package com.ape.encryptmanager;

public class FingerPrintItem {

	private int mId = -1;
	private String mName = "";
	private QuickBootData mQuickBootData = new QuickBootData();
	private int mFingerPrintData = -1;
	private int mAllowUnlockScreenValue = 1;
	private int mAllowUnlockAppValue = -1;	
	private int mQuickBootListPosition=-1;
	private int mContactPhotoId = -1;
	private int mContactId = -1;
	

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}


	public int getFingerPrintData() {
		return mFingerPrintData;
	}

	public void setFingerPrintData(int fingerPrintData) {
		this.mFingerPrintData = fingerPrintData;
	}
	
	public int getAllowUnlockScreenValue() {
		return mAllowUnlockScreenValue;
	}

	public void setAllowUnlockScreenValue(int allow) {
		this.mAllowUnlockScreenValue = allow;
	}	
	public int getAllowUnlockAppValue() {
		return mAllowUnlockAppValue;
	}

	public void setAllowUnlockAppValue(int allow) {
		this.mAllowUnlockAppValue = allow;
	}	

	public int getQuickBootListPosition() {
		return mQuickBootListPosition;
	}

	public void setQuickBootListPosition(int quickBootListPosition) {
		this.mQuickBootListPosition = quickBootListPosition;
	}	

	public QuickBootData getQuickBootData() {
		return mQuickBootData;
	}

	public void setQuickBootData(QuickBootData quickBootData) {
		this.mQuickBootData = quickBootData;
	}
	public int getContactPhotoId(){
		return mContactPhotoId;
	}
	public void setContactPhotoId(int contactPhotoId){
		mContactPhotoId = contactPhotoId;		
	}
	public int getContactId(){
		return mContactId;
	}
	public void setContactId(int contactId){
		mContactId = contactId;		
	}	
}