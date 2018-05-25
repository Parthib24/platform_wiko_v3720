package com.ape.encryptmanager.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.android.settings.R;
import com.ape.encryptmanager.provider.FingerPrintProvider.FingerColumn;
import com.ape.encryptmanager.provider.FingerPrintProvider.PasswordColumn;
import com.ape.encryptmanager.utils.EncryUtil;
import com.ape.encryptmanager.utils.LogUtil;
import android.util.SparseArray;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FlPassword;
import android.hardware.fingerprint.ApeFpData;


public class EncryptServiceUtil {

    private final String TAG = "EncryptServiceUtil";
    private Context mContext;
    private FingerprintManager mFingerprintManager;

    private EncryptServiceUtil(Context context) {
        mContext = context;
	 mFingerprintManager = (FingerprintManager)mContext.getSystemService(Context.FINGERPRINT_SERVICE);
    }
	
    private static EncryptServiceUtil sInstance;

    public static EncryptServiceUtil getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new EncryptServiceUtil(context);
            }
        return sInstance;
    }

	

    private int getFindStringCount(String from, String to) {
            if (from == null || to == null) {
                    return 0;
            }
            int count = 0;
            for (int i = 0; i < from.length();) {

                    int index = from.indexOf(to, i);
                    if (index >= 0) {
                            count++;
                            i = index + 1;
                    } else {
                            return count;
                    }
            }
            return count;

    }

    private String getSpaceFilter(String original) {
            if (original == null || original.length() <= 0) {
                    return original;
            }
            String startSpace = "^[  ]*";
            String endSpace = "[  ]*$";
            String buffer = original.replaceAll(startSpace, "").replaceAll(endSpace, "");
            return buffer;
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }	   
	   		

        public Boolean getAppSwitch(Context context, int userId) {
		  List<ApeFpData> enrolled_items = mFingerprintManager.getApeFpDataList(userId);//mFingerprintManager.getEnrolledFingerprints(userId);
		  if (enrolled_items == null) {
		      return false;
		  }
                final int fpEnrolledCount = enrolled_items.size();
		  if(fpEnrolledCount <=0) {
			return false;
		  } 		
                for(int i = 0; i < fpEnrolledCount; i++) {
                   int allowUnlockApp =  enrolled_items.get(i).getAllowUnlockAppValue();
		     if (allowUnlockApp > 0) {
                        return true;
		      }		   
                }
		  return false; 		
        }

        public Boolean getAppFingerprint() {
                return getAppSwitch(mContext, mContext.getUserId());
        }

        public Boolean getFileLock() {
                return getAppSwitch(mContext, mContext.getUserId());
        }

        public Boolean getGalleryLock() {
                return getAppSwitch(mContext, mContext.getUserId());
        }

        public String changePatternPassword(String patternPassword) {
                return mFingerprintManager.changePatternPassword(mContext.getUserId(), patternPassword);       
        }

        public String changeNumberPassword(String numberPassword) {          
                return mFingerprintManager.changeNumberPassword(mContext.getUserId(), numberPassword);
        }

        public String changePasswordQuestion(int question, String answer) {
            return mFingerprintManager.changePasswordQuestion(mContext.getUserId(), question, answer);
        }

        public String setNumberPassword(String numberPassword, int question, String answer) {
            return mFingerprintManager.setNumberPassword(mContext.getUserId(), numberPassword, question, answer);
        }
		
        public String setPatternPassword(String patternPassword, int question, String answer) {
             return mFingerprintManager.setPatternPassword(mContext.getUserId(), patternPassword, question, answer);               
        }
		
        public int getPasswordLength() {
            FlPassword flPassword = mFingerprintManager.getFlPasswordData(mContext.getUserId());
	     if (flPassword == null) {
                return -1;
	     }	             
            return flPassword.getPasswordLength();   
	 }
		
        public String getToken() {
            FlPassword flPassword = mFingerprintManager.getFlPasswordData(mContext.getUserId());
	     if (flPassword == null) {
                return null;
	     }	             
            return flPassword.getToken();               
        }

        public int getPasswordQuestionID() {
            FlPassword flPassword = mFingerprintManager.getFlPasswordData(mContext.getUserId());
	     if (flPassword == null) {
                return -1;
	     }	
            String questionId = flPassword.getQuestion();
		 
            return Integer.parseInt(questionId);	
                
        }

        public boolean checkPasswordQuestion(String security) {
            String md5 = md5(getSpaceFilter(security));
            FlPassword flPassword = mFingerprintManager.getFlPasswordData(mContext.getUserId());
	     if (flPassword == null) {
                return false;
	     }					
            String answer = flPassword.getAnswer();
            if (answer != null && answer.equals(md5)) {
                return true;
            }
			
	     return false;			
        }

        public int getPasswordType() {
            FlPassword flPassword = mFingerprintManager.getFlPasswordData(mContext.getUserId());
	     if (flPassword == null) {
                return EncryUtil.PATTERN_PASSWORD;
	     }	
            return flPassword.getCurrentPasswordType();
        }

        public String[] getQuestionList() {
                String[] questionArr = mContext.getResources().getStringArray(R.array.spinner_array);
                return questionArr;
        }

        public String checkPassword(String password) {
            String md5 = md5(password);
            FlPassword flPassword = mFingerprintManager.getFlPasswordData(mContext.getUserId());
	     if(flPassword == null) {
	         return null;
	     }

	     String numberPassword = flPassword.getCode();
	     String token = flPassword.getToken();
	     String patternPassword = flPassword.getPatternPassword();	 
      			
             if ((numberPassword == null && patternPassword != null && patternPassword.equals(md5)) 
		    || (numberPassword != null && patternPassword == null && numberPassword.equals(md5))
                  || ((numberPassword != null && patternPassword != null) && (numberPassword.equals(md5) || patternPassword.equals(md5)))) {
                 return token;
             }
	      return null;		 

        }

        public boolean checkToken(String token) {
            FlPassword flPassword = mFingerprintManager.getFlPasswordData(mContext.getUserId());
	     if(flPassword == null) {
	         return false;
	     }			
            String dbtoken = flPassword.getToken();
            if (dbtoken != null && dbtoken.equals(token)) {
                return true;
            }			
	     return false;            				
        }

        public boolean isMustSetPassword() {
            FlPassword flPassword = mFingerprintManager.getFlPasswordData(mContext.getUserId());
            if(flPassword == null)
	         return true;	
	     String numberPassword = flPassword.getCode();
	     String patternPassword = flPassword.getPatternPassword();
            if ((numberPassword == null && patternPassword == null)||(numberPassword.isEmpty() && patternPassword.isEmpty())) {
                    return true;
            }	
	     return false;
        }

        public boolean checkBindPrivacyLockFingerprint(int fingerId) {
                LogUtil.d(TAG, "input fingerId" + fingerId + ">>>>>>>>>>>>");

                ArrayList<Integer> fingerListData = (ArrayList<Integer>) getBindPrivacyLockFingerprint();
                for (int i = 0; i < fingerListData.size(); i++) {
                        LogUtil.d(TAG, "fingerListData.get(i)" + fingerListData.get(i) + "######");
                        if (fingerId == fingerListData.get(i)) {
                                LogUtil.d(TAG, "checkBindPrivacyLockFingerprint  ---> OK");
                                return true;
                        }
                }
                return false;
        }

        public List<Integer> getBindPrivacyLockFingerprint() {
		  //List<Fingerprint> enrolled_items = mFingerprintManager.getEnrolledFingerprints(mContext.getUserId());
		  List<ApeFpData> apeFpDataItems = mFingerprintManager.getApeFpDataList(mContext.getUserId());
		  
	         final int fpEnrolledCount = apeFpDataItems.size();
		  if(fpEnrolledCount <=0) {
			return null;
		  }		  
                List<Integer> fingerIDList = new ArrayList<Integer>();
				
                for(int i = 0; i < fpEnrolledCount; i++) {
                   int allowUnlockApp =  apeFpDataItems.get(i).getAllowUnlockAppValue();
		     if (allowUnlockApp > 0) {
                        fingerIDList.add(apeFpDataItems.get(i).getFingerId());
		      }   
                }
		  return 	fingerIDList;	
        }
}
