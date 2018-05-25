package android.hardware.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;

public final class FlPassword implements Parcelable {

    private int mPasswordId;
    private String mCode;
    private String mToken;
    private int mPasswordLength;
    private String mQuestion;
    private String mAnswer;
    private String mPatternPassword;
    private int mCurrentPasswordType;
	

    public FlPassword(int id, String code, String token, int length, String question, String answer, String patternPassword, int currentType) {
        mPasswordId = id;
        mCode = code;
        mToken = token;
        mPasswordLength = length;
        mQuestion = question;
        mAnswer = answer;
        mPatternPassword = patternPassword;
	mCurrentPasswordType = currentType;
    }

    private FlPassword(Parcel in) {
        mPasswordId = in.readInt();
        mCode = in.readString();
        mToken = in.readString();
        mPasswordLength = in.readInt();
        mQuestion = in.readString();
        mAnswer = in.readString();
        mPatternPassword = in.readString();
	 mCurrentPasswordType = in.readInt();		
    }


    public int getPasswordId() { return mPasswordId; }
	
    public String getCode() { return mCode; }
	
    public String getToken() { return mToken; }
	
    public int getPasswordLength() { return mPasswordLength; }
	
    public String getQuestion() { return mQuestion; }
	
    public String getAnswer() { return mAnswer; }
	
    public String getPatternPassword() { return mPatternPassword; }
	
    public int getCurrentPasswordType() { return mCurrentPasswordType; }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {	
        out.writeInt(mPasswordId);
        out.writeString(mCode);
        out.writeString(mToken);
        out.writeInt(mPasswordLength);
        out.writeString(mQuestion);
        out.writeString(mAnswer);
        out.writeString(mPatternPassword);
        out.writeInt(mCurrentPasswordType);		
    }

    public static final Parcelable.Creator<FlPassword> CREATOR
            = new Parcelable.Creator<FlPassword>() {
        public FlPassword createFromParcel(Parcel in) {
            return new FlPassword(in);
        }

        public FlPassword[] newArray(int size) {
            return new FlPassword[size];
        }
    };
};
