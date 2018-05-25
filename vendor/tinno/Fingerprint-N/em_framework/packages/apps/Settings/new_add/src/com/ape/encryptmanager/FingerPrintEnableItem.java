package com.ape.encryptmanager;

public class FingerPrintEnableItem {

	private int mId = -1;
	private int mScreenLockOnOff = 0;	
	private int mQuickBootOnOff=0;
	

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
	}

	public int getScreenLockOnOffValue() {
		return mScreenLockOnOff;
	}

	public void setScreenLockOnOffValue(int screenLockOnOff) {
		this.mScreenLockOnOff = screenLockOnOff;
	}


	public int getQuickBootOnOffValue() {
		return mQuickBootOnOff;
	}

	public void setQuickBootOnOffValue(int quickBootOnOff) {
		this.mQuickBootOnOff = quickBootOnOff;
	}
}