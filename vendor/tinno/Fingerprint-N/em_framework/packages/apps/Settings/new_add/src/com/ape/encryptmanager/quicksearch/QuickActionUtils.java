package com.ape.encryptmanager.quicksearch;

import android.R.color;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.android.settings.R;


public class QuickActionUtils {
	
    public static Bitmap toOvalBitmap(Bitmap bitmap,float ratio) {
        Bitmap output=Bitmap.createBitmap(bitmap.getHeight(),bitmap.getWidth(),Config.ARGB_8888);
        Canvas canvas=new Canvas(output);
        Paint paint=new Paint();
        Rect rect=new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        RectF rectF=new RectF(rect);
        
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, bitmap.getWidth()/ratio, 
        bitmap.getHeight()/ratio, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect,rectF, paint);
        return output;
}
    

	public static Bitmap getViewBitmap(View comBitmap, int width, int height) {
		Bitmap bitmap = null;
		if (comBitmap != null) {
			comBitmap.clearFocus();
			comBitmap.setPressed(false);

			boolean willNotCache = comBitmap.willNotCacheDrawing();
			comBitmap.setWillNotCacheDrawing(false);

			// Reset the drawing cache background color to fully transparent
			// for the duration of this operation
			int color = comBitmap.getDrawingCacheBackgroundColor();
			comBitmap.setDrawingCacheBackgroundColor(0);
			float alpha = comBitmap.getAlpha();
			comBitmap.setAlpha(1.0f);

			if (color != 0) {
				comBitmap.destroyDrawingCache();
			}
			
			int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
			int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
			comBitmap.measure(widthSpec, heightSpec);
			comBitmap.layout(0, 0, width, height);

			comBitmap.buildDrawingCache();
			Bitmap cacheBitmap = comBitmap.getDrawingCache();
			if (cacheBitmap == null) {
				Log.e("view.ProcessImageToBlur", "failed getViewBitmap(" + comBitmap + ")", 
						new RuntimeException());
				return null;
			}
			bitmap = Bitmap.createBitmap(cacheBitmap);
			// Restore the view
			comBitmap.setAlpha(alpha);
			comBitmap.destroyDrawingCache();
			comBitmap.setWillNotCacheDrawing(willNotCache);
			comBitmap.setDrawingCacheBackgroundColor(color);
		}
		return bitmap;
	}
	
	/**
	 * Returns a deterministic color based on the provided contact identifier
	 * string.
	 */
	public static int pickColor(final String name, Context mContext) {
		int sDefaultColor = mContext.getResources().getColor(color.black);
		if (TextUtils.isEmpty(name)) {
			return sDefaultColor;
		}
		
		int[] scolors = new int[]{Color.BLUE,Color.GREEN,Color.RED,Color.parseColor("#FF7F00"),Color.parseColor("#215E21"),Color.parseColor("#FF00FF")
				,Color.parseColor("#FF2400"),Color.parseColor("#545454"),Color.parseColor("#CD7F32")};

		final int color = Math.abs(name.hashCode()) % scolors.length;
		return scolors[color];
	}


}
