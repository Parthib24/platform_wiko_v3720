package android.hardware.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;
import android.content.Intent;
import android.net.Uri;

public final class ApeFpData implements Parcelable {
    private int mFingerId;
	
    private String mQBAppName = "";
    private String mQBPackageName= "";
    private String mQBClassName= "";
    private String mQBCategory = "";	
    private String mQBAction = "";
    private String mQBData = "";
	
    private String mQBPhoneName="";
    private String mQBPhoneNumber="";
    private int mQBContactPhotoId = -1;
    private int mQBContactId = -1;
	
    private int mAllowUnlockApp = 0;

    public ApeFpData(int fingerId) {
        mFingerId = fingerId;

        mQBAppName ="";
        mQBPackageName="";
        mQBClassName ="";
	mQBCategory = "";	
        mQBAction = "";
        mQBData = "";	
	 	
		
        mQBPhoneName="";
        mQBPhoneNumber="";
        mQBContactPhotoId = -1;
        mQBContactId = -1;

	mAllowUnlockApp = 0;	
    }

    public ApeFpData(int fingerId,
        String appName, String packageName, String className, String category, String action, String data,
        String phoneName, String phoneNumber, int contactPhotoId, int contactId,
        int allowUnlockApp) {
        mFingerId = fingerId;
		
        mQBAppName = appName;
        mQBPackageName= packageName;
        mQBClassName= className;
	 mQBCategory = category;	
	 mQBAction = action;
        mQBData = data; 

        mQBPhoneName= phoneName;
        mQBPhoneNumber= phoneNumber;
        mQBContactPhotoId = contactPhotoId;
        mQBContactId = contactId;   
		
	 mAllowUnlockApp = allowUnlockApp;	
    }
    
	

    private ApeFpData(Parcel in) {
        mFingerId = in.readInt();

        mQBAppName = in.readString();
        mQBPackageName= in.readString();
        mQBClassName= in.readString();
        mQBCategory = in.readString();		
	mQBAction = in.readString();	
        mQBData = in.readString();
		
        mQBPhoneName= in.readString();
        mQBPhoneNumber= in.readString();
        mQBContactPhotoId = in.readInt();
        mQBContactId = in.readInt();
		
	mAllowUnlockApp = in.readInt();

    }

    public int getFingerId() { return mFingerId; }

    public String getQBAppName() { return mQBAppName; }
    public String getQBPackageName() { return mQBPackageName; }
    public String getQBClassName() { return mQBClassName; }
    public String getQBCategory() { return mQBCategory; }	
    public String getQBAction() { return mQBAction; }
    public String getQBData() { return mQBData; }
	
	
    public String getQBPhoneName() { return mQBPhoneName; }
    public String getQBPhoneNumber() { return mQBPhoneNumber; }
    public int getQBContactPhotoId() { return mQBContactPhotoId; }
    public int getQBContactId() { return mQBContactId; }
	
    public int getAllowUnlockAppValue() { return mAllowUnlockApp; }
    public void setAllowUnlockAppValue(int allowUnlockApp) { mAllowUnlockApp = allowUnlockApp; }	


    public Intent getQBAppIntent(){
        Intent intent = new Intent();
	 intent.setPackage(mQBPackageName);
	 if (!mQBClassName.isEmpty()) {
	     intent.setClassName(mQBPackageName, mQBClassName);
        }
	 
        if (!mQBAction.isEmpty()) {
            intent.setAction(mQBAction);
        } else {
	     intent.setAction(Intent.ACTION_MAIN);
        }
		
        if (!mQBData.isEmpty()) {
            intent.setData(Uri.parse(mQBData));
        }
        if (!mQBCategory.isEmpty()) {
            intent.addCategory(mQBCategory);
	 }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
            Intent.FLAG_ACTIVITY_SINGLE_TOP);
	 	
	 return intent;	
   }
	
    
    public boolean isPackageInfoEmpty(){
        return (mQBAppName.isEmpty() || mQBPackageName.isEmpty());
    }
	
    public boolean isContactInfoEmpty(){
        return (mQBPhoneName.isEmpty() || mQBPhoneNumber.isEmpty());
    }
	
    public boolean isQuickBootDataEmpty(){
        return (isPackageInfoEmpty() && isContactInfoEmpty());
    }
  
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mFingerId);
		
        out.writeString(mQBAppName);
        out.writeString(mQBPackageName);
        out.writeString(mQBClassName);
	out.writeString(mQBCategory);	
        out.writeString(mQBAction);
        out.writeString(mQBData);
		
        out.writeString(mQBPhoneName);
        out.writeString(mQBPhoneNumber);
        out.writeInt(mQBContactPhotoId);
        out.writeInt(mQBContactId); 

	out.writeInt(mAllowUnlockApp);	
        
    }

    public static final Parcelable.Creator<ApeFpData> CREATOR
            = new Parcelable.Creator<ApeFpData>() {
        public ApeFpData createFromParcel(Parcel in) {
            return new ApeFpData(in);
        }

        public ApeFpData[] newArray(int size) {
            return new ApeFpData[size];
        }
    };
};
