package com.ape.encryptmanager.parse;

public class fpItem {
	private String id;
	private String value;

	public fpItem() {

	}
	
	public fpItem(String id, String value) {
		this.id = id;
		this.value = value;
	}
	
	public fpItem setId(String id) {
		this.id = id;
		return this;
	}
	public fpItem setValue(String value) {
		this.value = value;
		return this;
	}
	public String getId() {
		return this.id;
	}
	public String getValue() {
		return this.value;
	}
	
}
