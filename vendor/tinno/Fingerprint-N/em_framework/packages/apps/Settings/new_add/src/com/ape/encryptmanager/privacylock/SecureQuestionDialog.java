package com.ape.encryptmanager.privacylock;

import com.ape.encryptmanager.FingerPrintItem;
import com.android.settings.R;
import com.ape.encryptmanager.RoundedQuickActionImageView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class SecureQuestionDialog extends Dialog implements View.OnClickListener , OnItemClickListener{
    
    public static final int  SECURE_QUESTION_MSG = 0x50;
    private Context mContext;
    private String[] mQuestionArr;
    private int mInitIndex;
    private int mCurrentIndex;
    private ListView mListView;
    private Button left;
    private Button right;
    private DialogAdapter mDialogAdapter ;
    private Handler mHandler;
 

    protected SecureQuestionDialog(Context context,int index,String[] arr,Handler handler) {
        super(context);
        mContext = context; 
        mQuestionArr = arr;
        mInitIndex = index;
        mCurrentIndex = index;
        mHandler = handler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.secure_question_dialog_layout);

        initUI();
    }

    private void initUI() {
          mListView = (ListView)findViewById(R.id.listview);
         
          left  = (Button)findViewById(R.id.button_left);
          right =(Button)findViewById(R.id.button_right);
          mListView.setOnItemClickListener(this);
          right.setOnClickListener(this);
          left.setOnClickListener(this);
          mDialogAdapter = new DialogAdapter();
          mListView.setAdapter(mDialogAdapter);
}

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.button_left:
            break;
        case R.id.button_right:
            Message msg = new Message();
            msg.what = SECURE_QUESTION_MSG;
            msg.arg1 = mCurrentIndex;
            mHandler.sendMessage(msg);
            break;
        default:
            break;
        }
        dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        if(mCurrentIndex == position){
            return ;   
        }
        if(view !=null){
            ImageView  image  = (ImageView)view.findViewById(R.id.icon);
            TextView text=(TextView)view.findViewById(R.id.text);
            image.setImageResource(R.drawable.simple_choice_dialog_radio_selected);
            text.setTextColor(Color.rgb(0x2c, 0x2c, 0x2c));
        }
        View lastView =mListView.getChildAt(mCurrentIndex);    
        if(lastView !=null){
            ImageView  image  = (ImageView)lastView.findViewById(R.id.icon);
            TextView text=(TextView)lastView.findViewById(R.id.text);
            image.setImageResource(R.drawable.simple_choice_dialog_radio_normal);
            text.setTextColor(Color.rgb(0x75, 0x75, 0x75));
        }
        mCurrentIndex = position;
    }

    private    class DialogAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            return mQuestionArr.length;
        }

        @Override
        public Object getItem(int position) {

            return null;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view =  getLayoutInflater().inflate(R.layout.secure_question_dialog_item_layout, parent, false);
            
            ImageView  image  = (ImageView)view.findViewById(R.id.icon);
            TextView text=(TextView)view.findViewById(R.id.text);
            text.setText(mQuestionArr[position]);
            if(mCurrentIndex == position){
                image.setImageResource(R.drawable.simple_choice_dialog_radio_selected);
                text.setTextColor(Color.rgb(0x2c, 0x2c, 0x2c));
            }else{
                image.setImageResource(R.drawable.simple_choice_dialog_radio_normal);
                text.setTextColor(Color.rgb(0x75, 0x75, 0x75));
            }
            
            return view;
        }
    }
}
