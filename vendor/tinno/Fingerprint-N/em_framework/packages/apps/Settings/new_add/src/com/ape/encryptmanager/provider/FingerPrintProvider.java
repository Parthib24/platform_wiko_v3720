package com.ape.encryptmanager.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.ape.emFramework.Log;

public class FingerPrintProvider extends ContentProvider {

	public static final String AUTHORITY = "com.ape.encryptmanager";
	public static final String FINGER_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.ape.encryptmanager.fingerprint.id";
	public static final String FINGER_LIST_TYPE = "vnd.android.cursor.dir/vnd.com.ape.encryptmanager.fingerprint.list";
	public static final String STATUS_LIST_TYPE = "vnd.android.cursor.dir/vnd.com.ape.encryptmanager.fingerstatus.list";
	public static final String APP_LOCK_LIST_TYPE = "vnd.android.cursor.dir/vnd.com.ape.encryptmanager.applock.list";
	public static final String APP_LOCK_ENABLE_LIST_TYPE = "vnd.android.cursor.dir/vnd.com.ape.encryptmanager.applockenable.list";
	public static final String PASSWPRD_LIST_TYPE = "vnd.android.cursor.dir/vnd.com.ape.encryptmanager.password.list";	
	public static final String TOUCH_CONTROL_TYPE = "vnd.android.cursor.dir/vnd.com.ape.encryptmanager.touchcontrol.list";
	public static final String FINGER_ENABLE_LIST_TYPE = "vnd.android.cursor.dir/vnd.com.ape.encryptmanager.fingerprintenable.list";

	private static final String DB_NAME = "settingfingerprint.db";
	public static final String DEFAULT_SORT_ORDER = FingerColumn.ID + " ASC";
	private DatabaseHelper dbHelper;
	private UriMatcher uriMatcher;
	private static final int CODE_FINGER = 1;
	private static final int CODE_FINGER_LIST = CODE_FINGER + 1;
	private static final int CODE_STATUS_LIST = CODE_FINGER_LIST + 1;
	private static final int CODE_APP_LOCK = CODE_STATUS_LIST + 1;
	private static final int CODE_APP_LOCK_ENABLE = CODE_APP_LOCK + 1;
	private static final int CODE_PASSWORD = CODE_APP_LOCK_ENABLE + 1;
	private static final int CODE_TOUCH_CONTROL = CODE_PASSWORD + 1;
	private static final int CODE_FINGER_ENABLE = CODE_TOUCH_CONTROL + 1;
	public static final String TAG = "FingerPrintProvider";
	
	public interface FingerColumn {
		public static final String TABLE_NAME = "fingerprint";
		public static final String ID = "_id";
		public static final String NAME = "name";
		public static final String APP_NAME="app_name";
		public static final String PACKAGE_NAME = "package";
		public static final String CLASS_NAME = "class";
		public static final String PHONE_NAME = "phone_name";
		public static final String PHONE_NUMBER="phone_number";		
		public static final String FINGER_DATA = "data";
		public static final String ALLOW_UNLOCK_SCREEN = "allow_unlock_screen";
		public static final String ALLOW_UNLOCK_APP = "allow_unlock_app";	
		public static final String QUICK_BOOT_LIST_POSITION="quick_boot_list_position";
		public static final String CONTACT_PHOTO_ID ="contact_photo_id";
		public static final String CONTACT_ID = "contact_id";
	}
	public interface FingerEnableColumn {
		public static final String TABLE_NAME = "fingerprintenable";
		public static final String ID = "_id";
		public static final String SCREEN_LOCK_ONOFF = "screen_lock_onoff";
		public static final String QUICK_BOOT_ONOFF="quick_boot_onoff";
	}
	public interface StatusBackupColumn {
		public static final String TABLE_NAME = "fingerstatus";
		public static final String ID = "_id";
		public static final String NAME = "name";
		public static final String APP_NAME="app_name";		
		public static final String PACKAGE_NAME = "package";
		public static final String CLASS_NAME = "class";
		public static final String PHONE_NAME = "phone_name";
		public static final String PHONE_NUMBER="phone_number";		
		public static final String FINGER_DATA = "data";
		public static final String ALLOW_UNLOCK_SCREEN = "allow_unlock_screen";	
		public static final String ALLOW_UNLOCK_APP = "allow_unlock_app";	
		public static final String QUICK_BOOT_LIST_POSITION="quick_boot_list_position";
		public static final String CONTACT_PHOTO_ID ="contact_photo_id";
		public static final String CONTACT_ID = "contact_id";		
	}

	public interface AppLockColumn {
		public static final String TABLE_NAME = "applock";
		public static final String ID = "_id";
		public static final String APP_NAME = "app_name";
		public static final String PACKAGE_NAME = "package_name";
		public static final String CLASS_NAME = "class_name";
		public static final String ENCRYPT_ONOFF = "encrypt_onoff";
		public static final String APPLOCK_ONOFF = "applock_onoff";
		public static final String FILELOCK_ONOFF = "filelock_onoff";
		public static final String GALLERY_ONOFF = "gallery_onoff";
		public static final String QUICK_BOOT_LIST_POSITION="quick_boot_list_position";			
	}	

	public interface AppLockEnableColumn {
		public static final String TABLE_NAME = "applockenable";
		public static final String ID = "_id";
		public static final String ENCRYPT_ONOFF = "encrypt_onoff";
		public static final String APPLOCK_ONOFF = "applock_onoff";
		public static final String FILELOCK_ONOFF = "filelock_onoff";
		public static final String GALLERY_ONOFF = "gallery_onoff";
	}	
	
	public interface PasswordColumn {
		public static final String TABLE_NAME = "password";
		public static final String ID = "_id";
		public static final String CODE = "code";
		public static final String TOKEN = "token";
		public static final String PASSWORD_LENGTH  = "password_Length";
		public static final String  QUESTION  = "question";
		public static final String   ANSWER  = "Answer";
		public static final String   PATTERN_PASSWORD   = "Pattern_password";
		public static final String   CURRENT_PASSWORD_TYPE = "current_password_type";

	}	
	
	public interface TouchControlColumn {
		public static final String TABLE_NAME = "touchcontrol";
		public static final String ID = "_id";
		public static final String FUNC_ONOFF = "FUNC_ONOFF";
		public static final String TOUCH_LEFT = "TOUCH_LEFT";
		public static final String TOUCH_RIGHT = "TOUCH_RIGHT";
		public static final String TOUCH_DOWN = "TOUCH_DOWN";
		public static final String TOUCH_PRESS_ONCE = "TOUCH_PRESS_ONCE";
		public static final String TOUCH_HEAVY_PRESS_ONCE = "HEAVY_PRESS_ONCE";
		public static final String TOUCH_LONGPRESS = "TOUCH_LONGPRESS";
		public static final String TOUCH_DOUBLE_CLICK = "TOUCH_DOUBLE_CLICK";
	}	
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		int count = 0;
		switch (uriMatcher.match(uri)) {
		case CODE_FINGER:
			String whereID = DatabaseUtils.concatenateWhere(FingerColumn.ID + "="
					+ uri.getLastPathSegment(), selection);
			count = db.delete(FingerColumn.TABLE_NAME, whereID, selectionArgs);
			break;

		case CODE_FINGER_LIST:
			count = db.delete(FingerColumn.TABLE_NAME, selection, selectionArgs);
			break;

		case CODE_STATUS_LIST:
			count = db.delete(StatusBackupColumn.TABLE_NAME, selection,
					selectionArgs);
			break;

		case CODE_APP_LOCK:
			count = db.delete(AppLockColumn.TABLE_NAME, selection,
					selectionArgs);
			break;

		case CODE_APP_LOCK_ENABLE:
			count = db.delete(AppLockEnableColumn.TABLE_NAME, selection,
					selectionArgs);
			break;

		case CODE_TOUCH_CONTROL:
			count = db.delete(TouchControlColumn.TABLE_NAME, selection,
					selectionArgs);
			break;

		case CODE_PASSWORD:
			count = db.delete(PasswordColumn.TABLE_NAME, selection,
					selectionArgs);
			break;

		case CODE_FINGER_ENABLE:
			count = db.delete(FingerEnableColumn.TABLE_NAME, selection,
					selectionArgs);
			break;			
			
		}
		db.close();
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case CODE_FINGER:
			return FINGER_ITEM_TYPE;

		case CODE_FINGER_LIST:
			return FINGER_LIST_TYPE;

		case CODE_STATUS_LIST:
			return STATUS_LIST_TYPE;

		case CODE_APP_LOCK:
			return APP_LOCK_LIST_TYPE;
			
		case CODE_APP_LOCK_ENABLE:
			return APP_LOCK_ENABLE_LIST_TYPE;
			
		case CODE_PASSWORD:
			return PASSWPRD_LIST_TYPE;
		
		case CODE_FINGER_ENABLE:
			return FINGER_ENABLE_LIST_TYPE;
				
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = 0;
		switch (uriMatcher.match(uri)) {
		case CODE_FINGER_LIST:
			if (values == null || values.size() == 0) {
				throw new IllegalArgumentException("insert parameters may not be null");
			} /*else if (!values.containsKey(FingerColumn.NAME)) {
				throw new IllegalArgumentException("Must pass NAME parameter.");
			}else if (!values.containsKey(FingerColumn.APP_NAME)) {
				throw new IllegalArgumentException("Must pass PACKAGE_NAME parameter.");
			}else if (!values.containsKey(FingerColumn.APP_NAME)) {
				throw new IllegalArgumentException("Must pass PACKAGE_NAME parameter.");
			} else if (!values.containsKey(FingerColumn.CLASS_NAME)) {
				throw new IllegalArgumentException("Must pass CLASS_NAME parameter.");
			} else if (!values.containsKey(FingerColumn.PHONE_NAME)) {
				throw new IllegalArgumentException("Must pass PHONE_NAME parameter.");
			} else if (!values.containsKey(FingerColumn.PHONE_NUMBER)) {
				throw new IllegalArgumentException("Must pass PHONE_NUMBER parameter.");
			} else if (!values.containsKey(FingerColumn.FINGER_DATA)) {
				throw new IllegalArgumentException("Must pass FINGER_DATA parameter.");
	        } else if (!values.containsKey(FingerColumn.ALLOW_UNLOCK_SCREEN)) {
				throw new IllegalArgumentException("Must pass ALLOW_UNLOCK_SCREEN parameter.");
			} else if (!values.containsKey(FingerColumn.ALLOW_UNLOCK_APP)) {
				throw new IllegalArgumentException("Must pass ALLOW_UNLOCK_APP parameter.");
			} else if (!values.containsKey(FingerColumn.QUICK_BOOT_LIST_POSITION)) {
				throw new IllegalArgumentException("Must pass QUICK_BOOT_LIST_POSITION parameter.");
			}*/
			
			rowId = db.insert(FingerColumn.TABLE_NAME, null, values);
			break;

		case CODE_STATUS_LIST:
			if (values == null || values.size() == 0) {
				throw new IllegalArgumentException("insert parameters may not be null");
			}/* else if (!values.containsKey(StatusBackupColumn.NAME)) {
				throw new IllegalArgumentException("Must pass NAME parameter.");
			} else if (!values.containsKey(StatusBackupColumn.APP_NAME)) {
				throw new IllegalArgumentException("Must pass APP_NAME parameter.");
			} else if (!values.containsKey(StatusBackupColumn.PACKAGE_NAME)) {
				throw new IllegalArgumentException("Must pass PACKAGE_NAME parameter.");
			} else if (!values.containsKey(StatusBackupColumn.CLASS_NAME)) {
				throw new IllegalArgumentException("Must pass CLASS_NAME parameter.");
			} else if (!values.containsKey(StatusBackupColumn.PHONE_NAME)) {
				throw new IllegalArgumentException("Must pass PHONE_NAME parameter.");
			} else if (!values.containsKey(StatusBackupColumn.PHONE_NUMBER)) {
				throw new IllegalArgumentException("Must pass PHONE_NUMBER parameter.");
			} else if (!values.containsKey(StatusBackupColumn.FINGER_DATA)) {
				throw new IllegalArgumentException("Must pass FINGER_DATA parameter.");
			} else if (!values.containsKey(StatusBackupColumn.ALLOW_UNLOCK_SCREEN)) {
				throw new IllegalArgumentException("Must pass ALLOW_UNLOCK_SCREEN parameter.");
			} else if (!values.containsKey(StatusBackupColumn.ALLOW_UNLOCK_APP)) {
				throw new IllegalArgumentException("Must pass ALLOW_UNLOCK_APP parameter.");
			} else if (!values.containsKey(StatusBackupColumn.QUICK_BOOT_LIST_POSITION)) {
				throw new IllegalArgumentException("Must pass QUICK_BOOT_LIST_POSITION parameter.");
			} */   
			rowId = db.insert(StatusBackupColumn.TABLE_NAME, null, values);
			break;
			
		case CODE_APP_LOCK:
			if (values == null || values.size() == 0) {
				throw new IllegalArgumentException("insert parameters may not be null");
			} 
			rowId = db.insert(AppLockColumn.TABLE_NAME, null, values);
			break;
			
		case CODE_APP_LOCK_ENABLE:
			if (values == null || values.size() == 0) {
				throw new IllegalArgumentException("insert parameters may not be null");
			} 
			rowId = db.insert(AppLockEnableColumn.TABLE_NAME, null, values);
			break;

		case CODE_TOUCH_CONTROL:
			if (values == null || values.size() == 0) {
				throw new IllegalArgumentException("insert parameters may not be null");
			} 
			rowId = db.insert(TouchControlColumn.TABLE_NAME, null, values);
			break;

			
		case CODE_PASSWORD:
			if (values == null || values.size() == 0) {
				throw new IllegalArgumentException("insert parameters may not be null");
			} else if (!values.containsKey(PasswordColumn.CODE)) {
				//throw new IllegalArgumentException("Must pass APP_NAME parameter.");
			} else if (!values.containsKey(PasswordColumn.TOKEN)) {
				//throw new IllegalArgumentException("Must pass PACKAGE_NAME parameter.");
			} 
			rowId = db.insert(PasswordColumn.TABLE_NAME, null, values);
			break;	
            
		case CODE_FINGER_ENABLE:
			if (values == null || values.size() == 0) {
				throw new IllegalArgumentException("insert parameters may not be null");
			} 
			rowId = db.insert(FingerEnableColumn.TABLE_NAME, null, values);
			break;
            
		default:	
			throw new IllegalArgumentException(
					"Can only insert into to file URI");
		}
		db.close();
		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + uri);
		} else {
			Uri noteUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, FingerColumn.TABLE_NAME + "/#", CODE_FINGER);
		uriMatcher.addURI(AUTHORITY, FingerColumn.TABLE_NAME, CODE_FINGER_LIST);
		uriMatcher.addURI(AUTHORITY, StatusBackupColumn.TABLE_NAME,
				CODE_STATUS_LIST);
		uriMatcher.addURI(AUTHORITY, AppLockColumn.TABLE_NAME, CODE_APP_LOCK);		
		uriMatcher.addURI(AUTHORITY, AppLockEnableColumn.TABLE_NAME, CODE_APP_LOCK_ENABLE);	
		uriMatcher.addURI(AUTHORITY, PasswordColumn.TABLE_NAME, CODE_PASSWORD);			
		uriMatcher.addURI(AUTHORITY, TouchControlColumn.TABLE_NAME, CODE_TOUCH_CONTROL);	
		uriMatcher.addURI(AUTHORITY, FingerEnableColumn.TABLE_NAME, CODE_FINGER_ENABLE);	
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		if(selectionArgs!=null)
			Log.d(TAG, "query  uriMatcher.match(uri) = " +uriMatcher.match(uri) +"| selection = " + selection  + " | selectionArgs = " + selectionArgs[0] + "  ========");
	
		switch (uriMatcher.match(uri)) {
		case CODE_FINGER:
			queryBuilder.setTables(FingerColumn.TABLE_NAME);
			queryBuilder.appendWhere(FingerColumn.ID + "="
					+ uri.getLastPathSegment());
			break;

		case CODE_FINGER_LIST:
			queryBuilder.setTables(FingerColumn.TABLE_NAME);
			break;

		case CODE_STATUS_LIST:
			queryBuilder.setTables(StatusBackupColumn.TABLE_NAME);
			break;

		case CODE_APP_LOCK:
			queryBuilder.setTables(AppLockColumn.TABLE_NAME);
			break;
			
		case CODE_APP_LOCK_ENABLE:
			queryBuilder.setTables(AppLockEnableColumn.TABLE_NAME);
			break;
			
		case CODE_TOUCH_CONTROL:
			queryBuilder.setTables(TouchControlColumn.TABLE_NAME);
			break;
			
		case CODE_PASSWORD:
			queryBuilder.setTables(PasswordColumn.TABLE_NAME);
			break;

		case CODE_FINGER_ENABLE:
			queryBuilder.setTables(FingerEnableColumn.TABLE_NAME);
			break;
            
		default:
			queryBuilder.setTables(FingerColumn.TABLE_NAME);
			break;
		}

		if (sortOrder == null || sortOrder.length() == 0) {
			sortOrder = DEFAULT_SORT_ORDER;
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count = 0;
		Log.d(TAG, "update  uriMatcher.match(uri) = " + uriMatcher.match(uri) +"  ========");
		
		switch (uriMatcher.match(uri)) {
		case CODE_FINGER:
			String whereID = DatabaseUtils.concatenateWhere(FingerColumn.ID + "="
					+ uri.getLastPathSegment(), selection);
			count = db.update(FingerColumn.TABLE_NAME, values, whereID,
					selectionArgs);
			break;

		case CODE_FINGER_LIST:
			count = db.update(FingerColumn.TABLE_NAME, values, selection,
					selectionArgs);
			break;

		case CODE_STATUS_LIST:
			count = db.update(StatusBackupColumn.TABLE_NAME, values, selection,
					selectionArgs);
			break;

		case CODE_APP_LOCK:
			count = db.update(AppLockColumn.TABLE_NAME, values, selection,
					selectionArgs);
			break;		
			
		case CODE_APP_LOCK_ENABLE:
			count = db.update(AppLockEnableColumn.TABLE_NAME, values, selection,
					selectionArgs);
			break;	

		case CODE_TOUCH_CONTROL:
			count = db.update(TouchControlColumn.TABLE_NAME, values, selection,
					selectionArgs);
			break;		
			
		case CODE_PASSWORD:
			count = db.update(PasswordColumn.TABLE_NAME, values, selection,
					selectionArgs);
			break;	
            
		case CODE_FINGER_ENABLE:
			count = db.update(FingerEnableColumn.TABLE_NAME, values, selection,
					selectionArgs);
			break;	            

		}
		db.close();
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	class DatabaseHelper extends SQLiteOpenHelper {

		private static final String CREATE_FINGER_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ FingerColumn.TABLE_NAME
				+ "("
				+ FingerColumn.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT "
				+ ","
				+ FingerColumn.NAME
				+ " TEXT "
				+ ","
				+ FingerColumn.APP_NAME
				+ " TEXT "
				+ ","				
				+ FingerColumn.PACKAGE_NAME
				+ " TEXT "
				+ ","
				+ FingerColumn.CLASS_NAME
				+ " TEXT "
				+ ","
				+ FingerColumn.PHONE_NAME
				+ " TEXT "
				+ ","	
				+ FingerColumn.PHONE_NUMBER
				+ " TEXT "
				+ ","						
				+ FingerColumn.FINGER_DATA
				+ " INTEGER "
				+ ","				
				+ FingerColumn.ALLOW_UNLOCK_SCREEN
				+ " INTEGER "
				+ ","
				+ FingerColumn.ALLOW_UNLOCK_APP
				+ " INTEGER "
				+ ","				
				+ FingerColumn.QUICK_BOOT_LIST_POSITION
				+ " INTEGER " 
				+ ","
				+ FingerColumn.CONTACT_PHOTO_ID
				+ " INTEGER "
				+ ","				
				+ FingerColumn.CONTACT_ID
				+ " INTEGER " + ");";

		private static final String CREATE_STATUS_BACKUP_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ StatusBackupColumn.TABLE_NAME
				+ "("
				+ StatusBackupColumn.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT "
				+ ","
				+ StatusBackupColumn.NAME
				+ " TEXT "
				+ ","
				+ StatusBackupColumn.APP_NAME
				+ " TEXT "
				+ ","				
				+ StatusBackupColumn.PACKAGE_NAME
				+ " TEXT "
				+ ","
				+ StatusBackupColumn.CLASS_NAME
				+ " TEXT "
				+ ","
				+ StatusBackupColumn.PHONE_NAME
				+ " TEXT "
				+ ","	
				+ StatusBackupColumn.PHONE_NUMBER
				+ " TEXT "
				+ ","					
				+ StatusBackupColumn.FINGER_DATA
				+ " INTEGER "
				+ ","				
				+ StatusBackupColumn.ALLOW_UNLOCK_SCREEN
				+ " INTEGER "
				+ ","				
				+ StatusBackupColumn.ALLOW_UNLOCK_APP
				+ " INTEGER "
				+ ","					
				+ StatusBackupColumn.QUICK_BOOT_LIST_POSITION
				+ " INTEGER " 
				+ ","
				+ StatusBackupColumn.CONTACT_PHOTO_ID
				+ " INTEGER "
				+ ","				
				+ StatusBackupColumn.CONTACT_ID
				+ " INTEGER " + ");";
		
		private static final String CREATE_APP_LOCK_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ AppLockColumn.TABLE_NAME
				+ "("
				+ AppLockColumn.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT "
				+ ","
				+ AppLockColumn.APP_NAME
				+ " TEXT "
				+ ","
				+ AppLockColumn.PACKAGE_NAME
				+ " TEXT "
				+ ","
				+ AppLockColumn.CLASS_NAME
				+ " TEXT "
				+ ");";	
		
		private static final String CREATE_APP_LOCK_ENABLE_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ AppLockEnableColumn.TABLE_NAME
				+ "("
				+ AppLockEnableColumn.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT "
				+ ","
				+ AppLockEnableColumn.ENCRYPT_ONOFF 
				+ " TEXT " 
				+ ","
				+ AppLockEnableColumn.APPLOCK_ONOFF 
				+ " TEXT " 
				+ ","
				+ AppLockEnableColumn.FILELOCK_ONOFF 
				+ " TEXT " 
				+ ","
				+ AppLockEnableColumn.GALLERY_ONOFF 
				+ " TEXT " 
				+ ");";	
		
		private static final String CREATE_FINGER_ENABLE_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ FingerEnableColumn.TABLE_NAME
				+ "("
				+ FingerEnableColumn.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT "
				+ ","
				+ FingerEnableColumn.SCREEN_LOCK_ONOFF 
				+ " TEXT " 
				+ ","
				+ FingerEnableColumn.QUICK_BOOT_ONOFF 
				+ " TEXT " 					
				+ ");";	
			
		private static final String CREATE_PASSWORD_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ PasswordColumn.TABLE_NAME
				+ "("
				+ PasswordColumn.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT "
				+ ","
				+ PasswordColumn.CODE
				+ " TEXT "
				+ ","
				+ PasswordColumn.TOKEN 
				+ " TEXT " 
				+ ","
				+PasswordColumn.QUESTION
				+ " TEXT " 
				+ ","
				+PasswordColumn.PASSWORD_LENGTH
				+ " TEXT " 
				+ ","
				+PasswordColumn.ANSWER
				+ " TEXT " 
				+ ","
				+PasswordColumn.PATTERN_PASSWORD
				+ " TEXT " 
				+ ","
				+PasswordColumn.CURRENT_PASSWORD_TYPE
				+ " TEXT " 				
				+ ");";	
		
		
		
		private static final String CREATE_TOUCH_CONTROL_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ TouchControlColumn.TABLE_NAME
				+ "("
				+ TouchControlColumn.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT "
				+ ","
				+ TouchControlColumn.FUNC_ONOFF
				+ " TEXT " 
				+ ","
				+ TouchControlColumn.TOUCH_LEFT
				+ " TEXT " 
				+ ","
				+ TouchControlColumn.TOUCH_RIGHT
				+ " TEXT " 
				+ ","
				+ TouchControlColumn.TOUCH_DOWN
				+ " TEXT " 
				+ ","
				+ TouchControlColumn.TOUCH_PRESS_ONCE
				+ " TEXT " 
				+ ","
				+ TouchControlColumn.TOUCH_HEAVY_PRESS_ONCE
				+ " TEXT "
				+ ","
				+ TouchControlColumn.TOUCH_LONGPRESS
				+ " TEXT " 
				+ ","
				+ TouchControlColumn.TOUCH_DOUBLE_CLICK
				+ " TEXT " 
				+ ");";	
		
		
		DatabaseHelper(Context context) {
			super(context, DB_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_FINGER_TABLE);
			db.execSQL(CREATE_STATUS_BACKUP_TABLE);
			db.execSQL(CREATE_APP_LOCK_TABLE);
			db.execSQL(CREATE_APP_LOCK_ENABLE_TABLE);
			db.execSQL(CREATE_PASSWORD_TABLE);
			db.execSQL(CREATE_TOUCH_CONTROL_TABLE);
			db.execSQL(CREATE_FINGER_ENABLE_TABLE);		
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

		}
	}
}
