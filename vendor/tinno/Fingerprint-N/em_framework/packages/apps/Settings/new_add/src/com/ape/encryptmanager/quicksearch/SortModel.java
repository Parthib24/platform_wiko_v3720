package com.ape.encryptmanager.quicksearch;

public class SortModel {

	private String name;   
	private String sortLetters; 
	private String mContactNumber;   
	private long mContactPhotoId;
	private long mContactId;

	public SortModel(String name, String number, long photoId, long contactId, String sortLetters){
	    	this.name = name;
	    	this.mContactNumber = number;
	    	this.mContactPhotoId = photoId;
	    	this.mContactId = contactId;
		this.sortLetters = sortLetters;
        }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

	public String getContactNumber() {
		return mContactNumber;
	}
	public void setContactNumber(String number) {
		this.mContactNumber = number;
	}

	public long getContactPhoto() {
		return mContactPhotoId;
	}
	public void setContactPhoto(long photo) {
		this.mContactPhotoId = photo;
	}
		
	public long getContactId() {
		return mContactId;
	}
	public void setContactId(long contactId) {
		this.mContactId = contactId;
	}	
}
