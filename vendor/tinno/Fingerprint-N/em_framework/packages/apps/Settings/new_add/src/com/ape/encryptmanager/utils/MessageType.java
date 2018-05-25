package com.ape.encryptmanager.utils;

public class MessageType {
	
/*****************huiding  start**********************************/
public static final int MSG_TYPE_COMMON_BASE = 0x00000000;
public static final int MSG_TYPE_COMMON_TOUCH = MSG_TYPE_COMMON_BASE + 1;
public static final int MSG_TYPE_COMMON_UNTOUCH = MSG_TYPE_COMMON_BASE + 2;
public static final int MSG_TYPE_COMMON_NOTIFY_INFO = MSG_TYPE_COMMON_BASE + 7;

public static final int MSG_TYPE_REGISTER_BASE = 0x00000010;
public static final int MSG_TYPE_REGISTER_PIECE = MSG_TYPE_REGISTER_BASE + 1;
public static final int MSG_TYPE_REGISTER_NO_PIECE = MSG_TYPE_REGISTER_BASE + 2;
public static final int MSG_TYPE_REGISTER_NO_EXTRAINFO = MSG_TYPE_REGISTER_BASE + 3;
public static final int MSG_TYPE_REGISTER_LOW_COVER = MSG_TYPE_REGISTER_BASE + 4;
public static final int MSG_TYPE_REGISTER_BAD_IMAGE = MSG_TYPE_REGISTER_BASE + 5;
public static final int MSG_TYPE_REGISTER_GET_DATA_FAILED = MSG_TYPE_REGISTER_BASE + 6;
public static final int MSG_TYPE_REGISTER_TIMEOUT = MSG_TYPE_REGISTER_BASE + 7;
public static final int MSG_TYPE_REGISTER_COMPLETE = MSG_TYPE_REGISTER_BASE + 8;
public static final int MSG_TYPE_REGISTER_CANCEL = MSG_TYPE_REGISTER_BASE + 9;
public static final int MSG_TYPE_REGISTER_DUPLICATE_REG = MSG_TYPE_REGISTER_BASE + 10;

public static final int MSG_TYPE_RECONGNIZE_BASE = 0x00000100;
public static final int MSG_TYPE_RECONGNIZE_SUCCESS = MSG_TYPE_RECONGNIZE_BASE + 1;
public static final int MSG_TYPE_RECONGNIZE_TIMEOUT = MSG_TYPE_RECONGNIZE_BASE + 2;
public static final int MSG_TYPE_RECONGNIZE_FAILED = MSG_TYPE_RECONGNIZE_BASE + 3;
public static final int MSG_TYPE_RECONGNIZE_BAD_IMAGE = MSG_TYPE_RECONGNIZE_BASE + 4;
public static final int MSG_TYPE_RECONGNIZE_GET_DATA_FAILED = MSG_TYPE_RECONGNIZE_BASE + 5;
public static final int MSG_TYPE_RECONGNIZE_NO_REGISTER_DATA = MSG_TYPE_RECONGNIZE_BASE + 6;// new

public static final int MSG_TYPE_DELETE_BASE = 0x00001000;
public static final int MSG_TYPE_DELETE_SUCCESS = MSG_TYPE_DELETE_BASE + 1;
public static final int MSG_TYPE_DELETE_NOEXIST = MSG_TYPE_DELETE_BASE + 2;
public static final int MSG_TYPE_DELETE_TIMEOUT = MSG_TYPE_DELETE_BASE + 3;

public static final int MSG_TYPE_ERROR = 0x00010000;

public static String getString(int msg) {
	switch (msg) {
	case MSG_TYPE_COMMON_BASE:
		return "MSG_TYPE_COMMON_BASE";
	case MSG_TYPE_COMMON_TOUCH:
		return "TOUCH";
	case MSG_TYPE_COMMON_UNTOUCH:
		return "UNTOUCH";
	case MSG_TYPE_COMMON_NOTIFY_INFO:
		return "MSG_TYPE_COMMON_NOTIFY_INFO";
	case MSG_TYPE_REGISTER_BASE:
		return "MSG_TYPE_REGISTER_BASE";
	case MSG_TYPE_REGISTER_PIECE:
		return "MSG_TYPE_REGISTER_PIECE";
	case MSG_TYPE_REGISTER_NO_PIECE:
		return "MSG_TYPE_REGISTER_NO_PIECE";
	case MSG_TYPE_REGISTER_NO_EXTRAINFO:
		return "MSG_TYPE_REGISTER_NO_EXTRAINFO";
	case MSG_TYPE_REGISTER_LOW_COVER:
		return "MSG_TYPE_REGISTER_LOW_COVER";
	case MSG_TYPE_REGISTER_BAD_IMAGE:
		return "MSG_TYPE_REGISTER_BAD_IMAGE";
	case MSG_TYPE_REGISTER_GET_DATA_FAILED:
		return "MSG_TYPE_REGISTER_GET_DATA_FAILED";
	case MSG_TYPE_REGISTER_TIMEOUT:
		return "MSG_TYPE_REGISTER_TIMEOUT";
	case MSG_TYPE_REGISTER_COMPLETE:
		return "MSG_TYPE_REGISTER_COMPLETE";
	case MSG_TYPE_REGISTER_CANCEL:
		return "MSG_TYPE_REGISTER_CANCEL";
	case MSG_TYPE_RECONGNIZE_BASE:
		return "MSG_TYPE_RECONGNIZE_BASE";
	case MSG_TYPE_RECONGNIZE_SUCCESS:
		return "SUCCESS";
	case MSG_TYPE_RECONGNIZE_TIMEOUT:
		return "TIMEOUT";
	case MSG_TYPE_RECONGNIZE_FAILED:
		return "FAILED";
	case MSG_TYPE_RECONGNIZE_BAD_IMAGE:
		return "BAD_IMAGE";
	case MSG_TYPE_RECONGNIZE_GET_DATA_FAILED:
		return "GET_DATA_FAILED";
	case MSG_TYPE_RECONGNIZE_NO_REGISTER_DATA:
		return "NO_REGISTER_DATA";
	case MSG_TYPE_DELETE_BASE:
		return "MSG_TYPE_DELETE_BASE";
	case MSG_TYPE_DELETE_SUCCESS:
		return "MSG_TYPE_DELETE_SUCCESS";
	case MSG_TYPE_DELETE_NOEXIST:
		return "MSG_TYPE_DELETE_NOEXIST";
	case MSG_TYPE_DELETE_TIMEOUT:
		return "MSG_TYPE_DELETE_TIMEOUT";
	case MSG_TYPE_ERROR:
		return "MSG_TYPE_ERROR";
	default:
		return "UNKNOWN Message : " + msg;
	}
}

	
public static final int CHECK_IMG_FAIL = -1;
public static final int SL_TOUCH_TOO_FAST = -1005;
public static final int SL_RSP_SESSION_BUSY_IN_VERI = -4;
public static final int SL_RSP_SESSION_BUSY_IN_ENRO = -5;
public static final int SL_RSP_SESSION_BUSY_IN_WAKEUP_VERI = -6;

public static final int ENROLL_SUCCESS = 0;
public static final int ENROLL_CANCLED = -2;
public static final int ENROLL_NOT_SUPPORT = -3;
public static final int ENROLL_ERROR = -4;
public static final int ENROLL_FAIL = -105;
public static final int ENROLL_CHECK_ERROR = -108;//add by arthur 2015.10.27

public static final int SL_ENROLL_ERROR_LOW_COVERAGE = -109;
public static final int SL_ENROLL_ERROR_LOW_QUALITY = -110;
public static final int SL_ENROLL_ERROR_SAME_AREA = -111;

public static final int ENROLLING = 0;
public static final int REENROLL = 5;
public static final int ENROLL_INDEX = 1;
public static final int ENROLL_CREDENTIAL_RSP = 1;
/** for current single enroll failure due to image is not suitable*/
public static final int SL_ENROLL_CURR_ENR_FAIL = -106;
/** for image is not good when finger is putting on chip*/
public static final int SL_ENROLL_CURR_IMG_BAD = -107;

public static final int INIT_FP_FAIL = 0;
public static final int INIT_FP_SUCCESS = 1;

public static final int IDENTIFY = 2;
public static final int IDENTIFY_SUCCESS = 0;
public static final int IDENTIFY_TMEOUT = -1;
public static final int IDENTIFY_CANCELED = -2;
public static final int IDENTIFY_ERR_MATCH = -3;
public static final int IDENTIFY_ERROR = -4;
public static final int IDENTIFY_FAIL = -5;
public static final int IDENTIFY_MAX = 5;

public static final int IDENTIFY_INDEX = 2;
public static final int IDENTIFY_CREDENTIAL_RSP = 0;
public static final int IDENTIFY_WAKEUP_NOT_MATCHED = -206;
/**success to wake by verify*/
public static final int IDENTIFY_WAKEUP_MATCHED = -207;
public static final int IDENTIFY_WAKEUP_BAD_IMG = -208;
/** for image is not good when finger is put on chip*/
public static final int IDENTIFY_CURR_IMG_BAD = -209;

public static final int FP_GENERIC_CB = 3;
public static final int FP_KEY_CB = 4;

public static final int VK_STATE_ON = 1;
public static final int VK_STATE_OFF = 0;


/** this finger info is checked, can used for verify*/
public final static int FLAG_FINGER_ENABLED = 1;
/** this finger info is not checked, can't used for verify*/
public final static int FLAG_FINGER_DISABLED = 0;




/*
* TINNO fingerprint Message
*/
public static final int TINNO_MSG_ELAN_GET_SERVICE_MANAGER_ERROR  =995;

public static final int TINNO_MSG_SILEAD_GET_SERVICE_MANAGER_ERROR  =996;

public static final int TINNO_MSG_GOODIX_GET_SERVICE_MANAGER_ERROR  =997;
public static final int TINNO_MSG_CM_VERIFY_REGISTER_CALLBACK  =998;
public static final int TINNO_MSG_SLW_CONTINUE_VERIFY= 999;  

/* default msg */
public static final int TINNO_MSG_DEFAULT= 1000;  
/* remote service connected */
public static final int TINNO_MSG_SERVICE_CONNECTED = 1001;       
/* fingerprint data is ready */
public static final int TINNO_MSG_DATA_IS_READY = 1002;               
/* verify success */
public static final int TINNO_MSG_VERIFY_SUCCESS = 1003;        
/* verify failed */
public static final int TINNO_MSG_VERIFY_FAILED = 1004;         

public static final int TINNO_MSG_SYSTEM_ERROR = 1005;   


public static final int TINNO_MSG_ENROLL_CORRECT = 1006;        
public static final int TINNO_MSG_ENROLL_FAILED = 1007;         
public static final int TINNO_MSG_VERIFY_ERROR_LOCKOUT = 1008;

//public static final int TINNO_MSG_ENROLL_INVALID  = 1008;       
//public static final int TINNO_MSG_ENROLL_CANCLED = 1009;         
public static final int TINNO_MSG_ENROLL_DUPLICATE  = 1010;       

public static final int TINNO_MSG_ENROLL_TOUCH_TOO_FAST  = 1011;       
public static final int TINNO_MSG_ENROLL_IMG_BAD  = 1012;     
public static final int TINNO_MSG_ENROLL_REMOVE_HOME  = 1013;      


public static final int TINNO_MSG_VERIFY_IDENTIFY_WAKEUP_MATCHED = 1014;
public static final int TINNO_MSG_ENROLL_NO_EXTRAINFO= 1015;

public static final int TINNO_MSG_APPLOCK_CHECK_SUCCESS = 1016;      

public static final int TINNO_MSG_FINGER_LOCK= 1016;
public static final int TINNO_MSG_FINGER_UNLOCK= 1018;
public static final int TINNO_MSG_VERIFY_HELP = 1019;
public static final int TINNO_MSG_FINGER_CANCEL = 1020;


public static final int TINNO_MSG_START_APP = 100001;
public static final int TINNO_MSG_MAKE_CALL = 100002;
public static final int TINNO_MSG_UNLOCK_SCREEN = 100003;
public static final int TINNO_MSG_START_VIDEO_APP_DELAY = 100004;


// enroll
public static final int ELAN_MSG_ENROLLING= 10;
public static final int ELAN_MSG_ENROLL_SUCCESS = 11;
public static final int ELAN_MSG_ENROLL_FINGER_IS_EXSIT = 12;
public static final int ELAN_MSG_ENROLL_TOO_FAST = 13;
public static final int ELAN_MSG_ENROLL_BAD_IMG= 14;
public static final int ELAN_MSG_ENROLL_IMG_NOT_GOOD= 15;
public static final int ELAN_MSG_ENROLL_TOO_NEARBY= 16;

//for mtk/qcom compatible.
public static final int FINGERPRINT_ACQUIRED_ENROLL_DUPLICATE= 6; 
public static final int FINGERPRINT_ACQUIRED_ENROLL_TOO_NEARBY = 7;

// verify
public static final int ELAN_MSG_VERIFY_OK = 30;
public static final int ELAN_MSG_VERIFY_FAILED = 31;   

	
}
