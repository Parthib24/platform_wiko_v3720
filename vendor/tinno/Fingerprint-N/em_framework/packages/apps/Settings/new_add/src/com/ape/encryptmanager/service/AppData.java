package com.ape.encryptmanager.service;

import java.io.Serializable;

import android.graphics.drawable.Drawable;
import com.ape.fpShortcuts.Shortcut;
import java.util.ArrayList;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

public class AppData implements Parcelable{
	
	private String mClassName;
	private String mPackageName;
	private String mAppName;
	private int mAppIcon;
	private String mAppLetters;
       private List<Shortcut> mAppActionsList = new ArrayList<Shortcut>();

	public AppData() {
	}
	
	private AppData(Parcel source) {  
	    readFromParcel(source);  
	}  

	@Override  
	public int describeContents() {  
	    return 0;  
	}  

	@Override  
	public void writeToParcel(Parcel dest, int flags) {  
	    dest.writeString(mClassName);
	    dest.writeString(mPackageName); 
	    dest.writeString(mAppName);  
	    dest.writeInt(mAppIcon);			
	    dest.writeString(mAppLetters);
	    dest.writeTypedList(mAppActionsList);
		
	}  

	public void readFromParcel(Parcel source) {  
	    mClassName = source.readString();
	    mPackageName= source.readString();
	    mAppName= source.readString();
	    mAppIcon= source.readInt();
	    mAppLetters= source.readString();		
	    source.readTypedList(mAppActionsList, Shortcut.CREATOR);
	    /*int mAppActionsListSize = source.readInt();
           Shortcut shortcutItem;
	    if (mAppActionsListSize > 0) {
		 for (int i = 0; i < mAppActionsListSize; i++)	
               shortcutItem =  Shortcut.CREATOR.createFromParcel(source);
	        mAppActionsList.add(shortcutItem);		   
	    }*/
	    
		
	} 

	
	public String getClassName() {
		return mClassName;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public String getAppName() {
		return mAppName;
	}

	public int getAppIcon() {
		return mAppIcon;
	}
	
	public String getAppLetters(){
		return mAppLetters;
	}

	public List<Shortcut> getAppActionsList() {
		return mAppActionsList;
	}
	
	
	public void SetClassName(String className) {
		mClassName = className;
	}

	public void SetPackageName(String packageName) {
		mPackageName = packageName;
	}

	public void SetAppName(String appName) {
		mAppName = appName;
	}

	public void setAppIcon(int appIconId) {
		 mAppIcon = appIconId;
	}
	
	public void setAppLetters(String appLetters) {
		mAppLetters = appLetters;
	}

	public void setAppActionsList(List<Shortcut> shortcutList) {
		mAppActionsList = shortcutList;
	}

    public static final Parcelable.Creator<AppData> CREATOR = new Parcelable.Creator<AppData>() {  
  
        @Override  
        public AppData createFromParcel(Parcel source) {  
            return new AppData(source);  
        }  
  
        @Override  
        public AppData[] newArray(int size) {  
            return new AppData[size];  
        }  
    };
	

}
