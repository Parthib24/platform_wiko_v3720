package com.ape.encryptmanager;


public class QuickBootData{
	private String mAppName ="";
	private String mPackageName="";
	private String mClassName="";
	private String mPhoneName="";
	private String mPhoneNumber="";
	
	public QuickBootData()
	{
		mAppName = "";
		mPackageName = "";
		mClassName = "";
		mPhoneName = "";
		mPhoneNumber = "";	
	}
	public QuickBootData(String appName, String packageName, String className, String phoneName, String phoneNumber){
		mAppName = appName;
		mPackageName = packageName;
		mClassName = className;
		mPhoneName = phoneName;
		mPhoneNumber = phoneNumber;		
	}
	
	public QuickBootData(String appName, String packageName, String className)
	{
		mAppName = appName;
		mPackageName = packageName;
		mClassName = className;			
	}
	public QuickBootData(String phoneName, String phoneNumber)
	{
		mPhoneName = phoneName;
		mPhoneNumber = phoneNumber;
	}
	public boolean isAppEmpty(){
		return (mAppName.isEmpty() || mPackageName.isEmpty() || mClassName.isEmpty());
	}
	
	public boolean isPhoneEmpty(){
		return (mPhoneName.isEmpty() || mPhoneNumber.isEmpty());
	}
	
	public boolean isQuickBootDataEmpty(){
		return (isAppEmpty() && isPhoneEmpty());
	}
	
	public String getAppName() {
		return mAppName;
	}		
	
	public void setAppName(String appName) {
		this.mAppName = appName;
	}		
	public String getPackageName() {
		return mPackageName;
	}		
	
	public void setPackageName(String packageName) {
		this.mPackageName = packageName;
	}

	public String getClassName() {
		return mClassName;
	}

	public void setClassName(String className) {
		this.mClassName = className;
	}
	public String getPhoneName() {
		return mPhoneName;
	}

	public void setPhoneName(String phoneName) {
		this.mPhoneName = phoneName;
	}

	public String getPhoneNumber() {
		return mPhoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.mPhoneNumber = phoneNumber;
	}
	
	
}