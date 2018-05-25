package com.ape.encryptmanager.quicksearch;

import java.io.InputStream;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.android.settings.R;
  
public class SortAdapter extends BaseAdapter implements SectionIndexer{
	private List<SortModel> list = null;
	private Context mContext;
	
	public SortAdapter(Context mContext, List<SortModel> list) {
		this.mContext = mContext;
		this.list = list;
	}

	public void updateListView(List<SortModel> list){
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final SortModel mContent = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.quick_search_item, null);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.tvName = (TextView) view.findViewById(R.id.name);
			viewHolder.tvNumber = (TextView) view.findViewById(R.id.number);
			viewHolder.ivPhoto = (ImageView) view.findViewById(R.id.photo);
			viewHolder.img_letter = (TextView) view.findViewById(R.id.img_letters);

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		int section = getSectionForPosition(position);
		
		if(position == getPositionForSection(section)){
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		}else{
			viewHolder.tvLetter.setVisibility(View.INVISIBLE);
		}
	
		viewHolder.tvName.setText(this.list.get(position).getName());
		viewHolder.tvNumber.setText(this.list.get(position).getContactNumber());
		
		View a = new View(mContext);
		a.setBackgroundColor(QuickActionUtils.pickColor(this.list.get(position).getName(), mContext));
		viewHolder.ivPhoto.setImageBitmap(QuickActionUtils.toOvalBitmap(QuickActionUtils.getViewBitmap(a, 100, 100),1.0f));
		//viewHolder.ivPhoto.setImageBitmap(getContactPhotoImage(this.list.get(position).getContactPhoto(), this.list.get(position).getContactId()));
		
		viewHolder.img_letter.setText(mContent.getSortLetters());
		
		return view;

	}
	
	 private Bitmap getContactPhotoImage(long photoId, long contactId){
	        ContentResolver resolver = mContext.getContentResolver();
	    	Bitmap contactPhoto;
	        if(photoId > 0 ) {  
	            Uri uri =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactId);  
	            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);  
	            contactPhoto = BitmapFactory.decodeStream(input);  
	        }else {  
	            contactPhoto = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_menu_person_dk);  
	        }  
	        return contactPhoto;
	    }
	


	final static class ViewHolder {
		TextView tvLetter;
		TextView tvName;
		TextView tvNumber;
		ImageView ivPhoto;
		TextView img_letter;
	}

	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}


	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		
		return -1;
	}
	
	private String getAlpha(String str) {
		String  sortStr = str.trim().substring(0, 1).toUpperCase();
	
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}
