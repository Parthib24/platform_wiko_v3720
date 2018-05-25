package com.ape.encryptmanager;
import java.util.Comparator;
import com.ape.encryptmanager.service.AppData;

public class AppDataCompartor implements Comparator<AppData>{
 
	public int compare(AppData ad0, AppData ad1) {
		// TODO Auto-generated method stub
		//return ad0.getAppName().compareTo(ad1.getAppName());
		return ad0.getAppLetters().compareTo(ad1.getAppLetters());
	}
	
}
