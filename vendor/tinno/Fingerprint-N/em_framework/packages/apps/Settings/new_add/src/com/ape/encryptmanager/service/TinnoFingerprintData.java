package com.ape.encryptmanager.service;

public class TinnoFingerprintData {
	private int fingerid;
	private int progress;
	private int result;
       private int msgId;
       private String msgstr;
	
	public TinnoFingerprintData(){
		
	}
	
	public TinnoFingerprintData(int fingerid, int result){
		this.fingerid = fingerid;
		this.result = result;
	}

  	public TinnoFingerprintData(int msgId, String msgstr){
		this.msgId = msgId;
		this.msgstr = msgstr;
	}  
	
	public TinnoFingerprintData(int fingerid,int progress , int result){
		this.fingerid = fingerid;
		this.progress = progress;
		this.result = result;
	}
	
	public int getFingerid() {
		return fingerid;
	}
	public void setFingerid(int fingerid) {
		this.fingerid = fingerid;
	}
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public int getMsgId() {
		return msgId;
	}
	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}
	public String getMsgStr() {
		return msgstr;
	}
	public void setMsgStr(String msgstr) {
		this.msgstr = msgstr;
	}	

    
}
