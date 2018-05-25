package com.ape.fpShortcuts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
//import android.support.annotation.RequiresApi;

/**
 * Created by christopher ney on 10/10/2016.
 */

public class Shortcut implements Parcelable{

    public String id;
    public String shortLabel;
    public String longLabel;
    public String disabledMessage;
    public int icon;
    public boolean enabled;
    public Intent intent;
    public String packageName;

    public Shortcut() {}

    public Shortcut(Context context, LauncherApps launcherApps, ShortcutInfo shortcutInfo) {
        if (shortcutInfo != null) {
            int density = (int)context.getResources().getDisplayMetrics().density;
            id = shortcutInfo.getId();
            shortLabel = (shortcutInfo.getShortLabel() != null)
                    ? shortcutInfo.getShortLabel().toString() : null;
            longLabel = (shortcutInfo.getLongLabel() != null)
                    ? shortcutInfo.getLongLabel().toString() : null;
            disabledMessage = (shortcutInfo.getDisabledMessage() != null)
                    ? shortcutInfo.getDisabledMessage().toString() : null;
            icon = -1;//launcherApps.getShortcutIconResId(shortcutInfo);
            enabled = shortcutInfo.isEnabled();
            intent = shortcutInfo.getIntent();
            packageName = shortcutInfo.getPackage();
        }
    }

      public void init(Context context) {}

      public String[] getPermissions() { return null; }
	   
      @Override
      public String toString() {
          return (shortLabel != null) ? shortLabel : super.toString();
      }

	private Shortcut(Parcel source) {  
	    readFromParcel(source);  
	}  

       @Override
       public int describeContents() {
           return 0;
       }

	@Override  
	public void writeToParcel(Parcel dest, int flags) {  
	    dest.writeString(id);
	    dest.writeString(shortLabel); 
	    dest.writeString(longLabel);  
	    dest.writeString(disabledMessage);			
	    dest.writeInt(icon);
	    dest.writeInt(enabled?1:0);
           dest.writeParcelable(intent,flags);		
	    dest.writeString(packageName); 
	}  

	public void readFromParcel(Parcel source) {  		
	    id = source.readString();
	    shortLabel= source.readString();
	    longLabel= source.readString();
	    disabledMessage= source.readString();
	    icon= source.readInt();   
	    int enableValue = source.readInt();
	    if (enableValue > 0) {
	        enabled = true;
	    } else {
               enabled = false;
	    }	
           intent =  source.readParcelable(Intent.class.getClassLoader());
	    packageName = source.readString();	   
		
	} 
	
    public static final Parcelable.Creator<Shortcut> CREATOR = new Parcelable.Creator<Shortcut>() {
		
        @Override  
        public Shortcut createFromParcel(Parcel source) {
            return new Shortcut(source);  
        }  
  
        @Override  
        public Shortcut[] newArray(int size) {
            return new Shortcut[size];  
        }  
    };
		
	
}
