package com.ape.encryptmanager;

import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;
import com.ape.encryptmanager.quicksearch.CharacterParser;
import com.ape.encryptmanager.utils.EncryUtil;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.ape.emFramework.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.Collection;
import java.util.Collections;
import com.ape.fpShortcuts.Shortcut;
import com.ape.fpShortcuts.ShortcutManager;
import android.widget.ExpandableListView;
import android.widget.BaseExpandableListAdapter;
import android.os.UserHandle;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.content.ComponentName;
import android.util.ArraySet;
import java.util.Iterator;
import android.util.AttributeSet;
import java.util.Set;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.os.Handler;
import android.os.Message;
import com.ape.encryptmanager.service.EncryptService;
import android.content.ServiceConnection;
import android.os.IBinder; 
import com.ape.encryptmanager.service.AppData;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;


public class AppDataListChoiceActivity extends Activity implements OnItemClickListener, OnChildClickListener{
    private ExpandableListView mAppsExpandListView;
    private PackageManager mPackageManager;
    private ActionBar mActionBar;
    private boolean mDisplayHomeAsUpEnabled = true;
    private List<AppData> mAppDataList = new ArrayList<AppData>();

    private List<Integer> mAppDataListExpandState = new ArrayList<Integer>();		


    public static final String TAG = "AppDataListChoiceActivity";
    private static final boolean DEBUG = false;


    public Intent mResultIntent;
    public Intent mRequestIntent;
    public String mIntentFromStr;
    public int mFingerId;

    private static CharacterParser characterParser;

    private static int APP_QUICKACTIONS_MODE_DEFAULT = 0;
    private static int APP_QUICKACTIONS_MODE_COLLAPSED = 1;
    private static int APP_QUICKACTIONS_MODE_EXPANDED = 2;
    private static final int DEFAULT_APP_QUICKACTIONS_COUNT = 0;

    private int mAppQuickActionsMode = APP_QUICKACTIONS_MODE_DEFAULT;

    private int visibleLastIndex = 0;
    private int visibleItemCount;

    private AppExpandableListViewAdapter mAppExpandableListViewAdapter;
    private List<ResolveInfo> mResolveInfos;
    private AppDataCompartor  mAppDataCompartor = new AppDataCompartor();

    private Thread mDataLoadThread;
    private static final int MSG_UPDATE_EXPAND_LIST_VIEW = 1000; 
    private static final int MSG_EXPAND_LIST_DATA_LOAD_COMPLETE = 1001; 
    private static final int MSG_CHECK_REMOTE_APP_DATA = 1002; 
    private static final int EXPAND_LIST_DATA_INIT_SIZE = 8;

    private EncryptService.EncryptServiceWrapper mEncryptBinder;  
    EncryptServiceConnection mEncryptServiceConn;
    private View mLoadingContainer;
    private View mListContainer;


    Handler mHandler = new Handler() {     
        @Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg);  
            switch (msg.what) {  
                case MSG_UPDATE_EXPAND_LIST_VIEW:
                    //Log.d(TAG, "AppExpandableListViewAdapter -->notifyDataSetChanged !!!");        				
                    //mAppExpandableListViewAdapter.notifyDataSetChanged();
                    updateView();
                    break;
                case MSG_EXPAND_LIST_DATA_LOAD_COMPLETE:
                    //mAppsExpandListView.setEnabled(true);
                    break;
                case MSG_CHECK_REMOTE_APP_DATA:
                    boolean isDataReady = checkRemoteAppData();
                    if (DEBUG) Log.d(TAG, "handleMessage -->isDataReady =" + isDataReady);        					  
                    if (isDataReady) {
                        updateView();
                    } else {
                        EncryUtil.sendFingerprintEntryBroadcast(AppDataListChoiceActivity.this);
                    }
                    break;
                default:  
                    break;  
            }  
        }  
    };  

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onCreate " + this);        
        super.onCreate(savedInstanceState);
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.quick_boot_application);
            mActionBar.setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
            mActionBar.setHomeButtonEnabled(mDisplayHomeAsUpEnabled);
            mActionBar.setDisplayUseLogoEnabled(true);
        }

        this.setContentView(R.layout.quick_boot_all_application);

        mLoadingContainer = (View)findViewById(R.id.loading_container);
        mLoadingContainer.setVisibility(View.VISIBLE);
        mListContainer = (View)findViewById(R.id.list_container);

        bindEncryptService();		
        mAppsExpandListView = (ExpandableListView) mListContainer.findViewById(R.id.apps_expand_list);
        mAppsExpandListView.setGroupIndicator(null);	
        //mPackageManager = AppDataListChoiceActivity.this.getPackageManager();

        //characterParser = CharacterParser.getInstance();

        //getAllApps(AppDataListChoiceActivity.this);
        //initAppDataResolveInfos();
        // initExpandableListViewAdapter(this);
        // mAppsExpandListView.setEnabled(false);

        mAppExpandableListViewAdapter = new AppExpandableListViewAdapter(this);  	
        mAppsExpandListView.setAdapter(mAppExpandableListViewAdapter);

        mAppsExpandListView.setOnItemClickListener(this);
        mAppsExpandListView.setOnChildClickListener(this);
        //mAppsExpandListView.setOnGroupClickListener(this);
        //mAppsExpandListView.setOnScrollListener(this); 


        mResultIntent = new Intent();
        mRequestIntent = this.getIntent();
        mIntentFromStr = mRequestIntent.getStringExtra(EncryUtil.REQUEST_INTENT_FROM);
        mFingerId = mRequestIntent.getIntExtra(EncryUtil.KEY_QUICK_BOOT_FINGERID,0);

        /*mDataLoadThread = new Thread(new Runnable() {
          @Override
          public void run() {
          loadAdapterAppDatas(AppDataListChoiceActivity.this, EXPAND_LIST_DATA_INIT_SIZE, mResolveInfos.size(), false);
          mHandler.sendEmptyMessageDelayed(MSG_EXPAND_LIST_DATA_LOAD_COMPLETE, 0);
          }
          });*/		
    }


    @Override
    protected void onResume() {
        super.onResume();   
        Log.d(TAG, "onResume " + this);
        /*if (mDataLoadThread != null) {
          mDataLoadThread.start();	
          }*/		
    }

    public void initAppDataResolveInfos() {
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mResolveInfos = this.getPackageManager().queryIntentActivities(intent, 0);
    }

    public void bindEncryptService() {
        Intent service = new Intent(AppDataListChoiceActivity.this,
                EncryptService.class);
        mEncryptServiceConn = new EncryptServiceConnection();		
        bindService(service, mEncryptServiceConn, Context.BIND_AUTO_CREATE);

    }


    public void updateView() {
        removeTheDuplicateItemFromList();		
        updateLoading(true);
        initAppDatasExpandState();			
        mAppExpandableListViewAdapter.notifyDataSetChanged();
    }

    private void updateLoading(boolean isLoadingDone) {
        boolean needAnimate = isLoadingDone?true:false; 	
        handleLoadingContainer(mLoadingContainer,mListContainer,isLoadingDone, needAnimate);
    }
    public static void handleLoadingContainer(View loadingView, View doneView, boolean done,
            boolean animate) {
        setViewShown(loadingView, !done, animate);
        setViewShown(doneView, done, animate);
    }

    private static void setViewShown(final View view, boolean shown, boolean animate) {
        if (animate) {
            Animation animation = AnimationUtils.loadAnimation(view.getContext(),
                    shown ? android.R.anim.fade_in : android.R.anim.fade_out);
            if (shown) {
                view.setVisibility(View.VISIBLE);
            } else {
                animation.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.INVISIBLE);
                }
                });
            }
            view.startAnimation(animation);
        } else {
            view.clearAnimation();
            view.setVisibility(shown ? View.VISIBLE : View.INVISIBLE);
        }
    }


    public void initAppDatasExpandState() {
        int appDataSize = mAppDataList.size();
        if (appDataSize <= 0)
            return;	 
        for (int i=0;i<appDataSize;i++) {
            mAppDataListExpandState.add(0);
        }

    }

    public class EncryptServiceConnection implements ServiceConnection
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d(TAG, "onServiceConnected" );
            mEncryptBinder = (EncryptService.EncryptServiceWrapper) service;
            if (checkRemoteAppData()) {
                mHandler.sendEmptyMessage(MSG_UPDATE_EXPAND_LIST_VIEW);
            }
        }

        @Override		
        public void onServiceDisconnected(ComponentName name)
        {
            Log.d(TAG, "onServiceDisconnected" ); 	 	
        }
    }


    public boolean checkRemoteAppData(){
        if (mEncryptBinder == null)
            return false;
        //mAppDataList = mEncryptBinder.getQuickBootAppDatas();
        mAppDataList.addAll(mEncryptBinder.getQuickBootAppDatas());
        if (mAppDataList.size() <=0) {
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_REMOTE_APP_DATA, 50);
            return false;	   
        }
        return true;
    }
    /* 	
        public void getAllApps(Context context) {

        AppDataCompartor mAppDataCompartor = new AppDataCompartor();

        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = context.getPackageManager()
        .queryIntentActivities(intent, 0);
        Log.d(TAG, "--------resolveInfos.size()----->" + resolveInfos.size());		
        for (int i = 0; i < resolveInfos.size(); i++) {
        ResolveInfo s = resolveInfos.get(i);
        AppData tempData = new AppData();
        Log.d(TAG, "--------s.activityInfo.packageName------"+s.activityInfo.packageName);
        Log.d(TAG, "---------s.activityInfo.name-----"+s.activityInfo.name);
        Log.d(TAG, "---------s.activityInfo.label-----"+s.loadLabel(context.getPackageManager()).toString());

        tempData.SetAppName(s.loadLabel(context.getPackageManager())
        .toString());
        tempData.SetPackageName(s.activityInfo.packageName);
        tempData.SetClassName(s.activityInfo.name);
        tempData.setAppIcon(s.getIconResource());
        tempData.setAppLetters(setSortLetters(s.loadLabel(context.getPackageManager()).toString()));

        List<Shortcut> shortcutListOrigin = ShortcutManager.getShortcutsFromApplication(this, UserHandle.of(this.getUserId()), s.activityInfo.packageName);
        List<Shortcut> shortcutListNew = new ArrayList<Shortcut>();
        shortcutListNew.add(getDefaultAppLaunchShortcutItem(s.activityInfo.packageName, s.activityInfo.name)); 	 
        Log.d(TAG, "--------getAllApps--->shortcutListOrigin=" + shortcutListOrigin);

        if (shortcutListOrigin != null) {
        if (shortcutListOrigin.size() > 0) {
        for (int j=0; j<shortcutListOrigin.size(); j++) {
        Shortcut shortcut = shortcutListOrigin.get(j);
        Log.d(TAG, "shortcut[" + j + "] =" + shortcut +
        "| shortcut.id=" + shortcut.id +
        "| shortcut.shortLabel=" + shortcut.shortLabel +
        "| shortcut.longLabel=" + shortcut.longLabel +
        "| shortcut.disabledMessage=" + shortcut.disabledMessage +
        "| shortcut.icon=" + shortcut.icon +
        "| shortcut.enabled=" + shortcut.enabled +
        "| shortcut.intent=" + shortcut.intent +
        "| shortcut.id=" + shortcut.id +
        "| shortcut.packageName=" + shortcut.packageName);
        if (shortcut.enabled) {
        shortcutListNew.add(shortcut);
        }		
        }
        }
        }

        tempData.setAppActionsList(shortcutListNew);

        if (isNotSupportedApp(s.activityInfo.name)) {
        ((AppDataListChoiceActivity)context).getListData().remove(tempData);
        } else {
        ((AppDataListChoiceActivity) context).getListData().add(tempData);
        }

        }

        Collections.sort(((AppDataListChoiceActivity) context).getListData(), mAppDataCompartor);

        }


        public void loadAdapterAppDatas(Context context, int start_index, int size, boolean isInitState) {
        Log.d(TAG, "--------loadAdapterAppDatas------start_index ="+start_index);		
        for (int i = start_index; i < size; i++) {
        ResolveInfo s = mResolveInfos.get(i);
        AppData tempData = new AppData();
Log.d(TAG, "mResolveInfos[" +  i  +"]" +   
        "| s.activityInfo.packageName =" + s.activityInfo.packageName +
        "| s.activityInfo.name = "+s.activityInfo.name +
        "| s.activityInfo.label = "+s.loadLabel(context.getPackageManager()).toString());

tempData.SetAppName(s.loadLabel(context.getPackageManager())
        .toString());
    tempData.SetPackageName(s.activityInfo.packageName);
    tempData.SetClassName(s.activityInfo.name);
    tempData.setAppIcon(s.loadIcon(context.getPackageManager()));
    tempData.setAppLetters(setSortLetters(s.loadLabel(context.getPackageManager()).toString()));

    List<Shortcut> shortcutListOrigin = ShortcutManager.getShortcutsFromApplication(this, UserHandle.of(this.getUserId()), s.activityInfo.packageName);
    List<Shortcut> shortcutListNew = new ArrayList<Shortcut>();
    shortcutListNew.add(getDefaultAppLaunchShortcutItem(s.activityInfo.packageName, s.activityInfo.name)); 	 
    Log.d(TAG, "--------getAllApps--->shortcutListOrigin=" + shortcutListOrigin);

    if (shortcutListOrigin != null) {
        if (shortcutListOrigin.size() > 0) {
            for (int j=0; j<shortcutListOrigin.size(); j++) {
                Shortcut shortcut = shortcutListOrigin.get(j);
                Log.d(TAG, "shortcut[" + j + "] =" + shortcut +
                        "| shortcut.id=" + shortcut.id +
                        "| shortcut.shortLabel=" + shortcut.shortLabel +
                        "| shortcut.longLabel=" + shortcut.longLabel +
                        "| shortcut.disabledMessage=" + shortcut.disabledMessage +
                        "| shortcut.icon=" + shortcut.icon +
                        "| shortcut.enabled=" + shortcut.enabled +
                        "| shortcut.intent=" + shortcut.intent +
                        "| shortcut.id=" + shortcut.id +
                        "| shortcut.packageName=" + shortcut.packageName);
                if (shortcut.enabled) {
                    shortcutListNew.add(shortcut);
                }		
            }
        }
    }

tempData.setAppActionsList(shortcutListNew);

if (isNotSupportedApp(s.activityInfo.name)) {
    ((AppDataListChoiceActivity)context).getListData().remove(tempData);
} else {
    ((AppDataListChoiceActivity) context).getListData().add(tempData);
}
if (!isInitState) {
    mHandler.sendEmptyMessage(MSG_UPDATE_EXPAND_LIST_VIEW);
}
        }

Collections.sort(((AppDataListChoiceActivity) context).getListData(), mAppDataCompartor);

        }

public List<AppData> getListData() {
    return mAppDataList;
}

private void initExpandableListViewAdapter(Context context) {
    loadAdapterAppDatas(context,0, EXPAND_LIST_DATA_INIT_SIZE, true);
    mAppExpandableListViewAdapter = new AppExpandableListViewAdapter(this);  
}


public List<Shortcut> getDefaultAppShortcutsDataList() {
    List<Shortcut> init_shortcutList;
    Shortcut init_shortcutItem;
    init_shortcutList = new ArrayList<Shortcut>(); 
    init_shortcutItem = new Shortcut();
    init_shortcutItem.shortLabel = this.getResources().getString(R.string.quick_boot_shortcuts_item_default);
    init_shortcutList.add(init_shortcutItem);
    return init_shortcutList;
}

public List<AppData> getListData() {
    return mAppDataList;
}
public Shortcut getDefaultAppLaunchShortcutItem(String packageName, String className) {
    Shortcut init_shortcutItem ;
    Intent intent = new Intent();
    intent.setPackage(packageName);
    intent.setClassName(packageName, className);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    init_shortcutItem = new Shortcut();
    init_shortcutItem.shortLabel = this.getResources().getString(R.string.quick_boot_shortcuts_item_default);
    init_shortcutItem.packageName = packageName;
    init_shortcutItem.intent = intent;
    return init_shortcutItem;
}
    */	
    public static Drawable getAppInfoIcon(Context context, String packageName,String className)
    {		
        try {
            PackageManager pm = context.getPackageManager();
            Drawable appIcon = pm.getActivityIcon(new ComponentName(packageName,className));
            return appIcon;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } 
        return null;
    }    

    private boolean hasMoreAppQuickActions(List<Shortcut> actionsList) {
        return mAppQuickActionsMode == APP_QUICKACTIONS_MODE_COLLAPSED
                    || (mAppQuickActionsMode == APP_QUICKACTIONS_MODE_DEFAULT && 
                    actionsList.size() > DEFAULT_APP_QUICKACTIONS_COUNT);
    }


    private class AppExpandableListViewAdapter  extends BaseExpandableListAdapter {
        private Context context;

        public AppExpandableListViewAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getGroupCount() {
            return mAppDataList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mAppDataList.get(groupPosition).getAppActionsList().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mAppDataList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mAppDataList.get(groupPosition).getAppActionsList().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
            AppsGroupViewHolder appGroupholder;
            View preView;
            View nextView; 	 
            Drawable preViewBg = null;
            Drawable nextViewBg = null;		
            if (convertView == null) {
                convertView = (View) getLayoutInflater().from(context).inflate(R.layout.quick_boot_all_application_display, null);
                appGroupholder = new AppsGroupViewHolder();
                appGroupholder.icon = (ImageView) convertView.findViewById(R.id.apps_image);
                appGroupholder.label = (TextView) convertView.findViewById(R.id.apps_textview);
                appGroupholder.letters = (TextView) convertView.findViewById(R.id.apps_catalog);
                appGroupholder.expandIcon = (ImageView) convertView.findViewById(R.id.actions_expand);
                convertView.setTag(appGroupholder);
            } else {
                appGroupholder = (AppsGroupViewHolder) convertView.getTag();
            }

            int section = getSectionForPosition(groupPosition);

            if (groupPosition == getPositionForSection(section)) {
                appGroupholder.letters.setVisibility(View.VISIBLE);
            } else {
                appGroupholder.letters.setVisibility(View.INVISIBLE);
            }
            AppData appGroupData = mAppDataList.get(groupPosition);

            Drawable appIcon = getAppInfoIcon(AppDataListChoiceActivity.this, 
                appGroupData.getPackageName(), appGroupData.getClassName());

            if (DEBUG) Log.d(TAG, "appName = "+appGroupData.getAppName());		         
            if (DEBUG) Log.d(TAG, "appIcon = "+appIcon);

            int prvItemIndex = groupPosition -1;
            int nextItemIndex = groupPosition +1;		
            if(prvItemIndex > 0) {
                preView = (View)parent.getChildAt(prvItemIndex);

                if (preView != null) {
                    preViewBg = preView.getBackground();
                }
                if (DEBUG) Log.d(TAG, "prvItemIndex = "+prvItemIndex + " | preView.background =" + preViewBg);        
            }	

            if(nextItemIndex > 0) {
                nextView= (View)parent.getChildAt(nextItemIndex);

                if (nextView != null) {
                    nextViewBg = nextView.getBackground();
                }
                if (DEBUG) Log.d(TAG, "nextItemIndex = "+prvItemIndex + " | nextView.background =" + nextViewBg);        
            }	


            if(appIcon == null ||appGroupData.getAppName().isEmpty()) {
                appIcon = context.getDrawable(
                        android.R.drawable.sym_def_app_icon);		 	
                mAppDataList.remove(groupPosition);
                mAppExpandableListViewAdapter.notifyDataSetChanged();
            }

            appGroupholder.icon.setImageDrawable(appIcon);//context.getResources().getDrawable(appGroupData.getAppIcon()));
            appGroupholder.label.setText(appGroupData.getAppName());
            appGroupholder.letters.setText(appGroupData.getAppLetters());

            if (isExpanded) {
                mAppDataListExpandState.set(groupPosition, 1);		
                appGroupholder.expandIcon.setImageResource(R.drawable.quickboot_item_expand_less);	
                if (groupPosition > 0 && preViewBg == null && mAppDataListExpandState.get(prvItemIndex) ==0){
                    convertView.setBackground(getResources().getDrawable(R.drawable.quickapp_item_bg_new));
                } else {
                    convertView.setBackground(null);
                }		
            } else {
                mAppDataListExpandState.set(groupPosition, 0);
                appGroupholder.expandIcon.setImageResource(R.drawable.quickboot_item_expand_more);	
                convertView.setBackground(null);				
            }		

            return convertView;

        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
            ActionsItemViewHolder actionItemHolder = null;
            Shortcut shorcutItem = mAppDataList.get(groupPosition).getAppActionsList().get(childPosition);
            if (convertView == null) {
                convertView = (View) getLayoutInflater().from(context).inflate(R.layout.quick_boot_app_actions_item, null);
                actionItemHolder = new ActionsItemViewHolder();
                actionItemHolder.actionMessage = (TextView) convertView.findViewById(R.id.action_message);
                convertView.setTag(actionItemHolder);
            } else {
                actionItemHolder = (ActionsItemViewHolder) convertView.getTag();
            }
            actionItemHolder.actionMessage.setText(shorcutItem.shortLabel);
            if (isLastChild) {
                convertView.setBackground(getResources().getDrawable(R.drawable.quickactions_item_bg_new));
            } else {
                convertView.setBackground(getResources().getDrawable(R.drawable.ripple_bg));
            }		
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public int getSectionForPosition(int position) {
            return mAppDataList.get(position).getAppLetters().charAt(0);
        }

        public int getPositionForSection(int section) {
            for (int i = 0; i < getGroupCount(); i++) {
                String sortStr = mAppDataList.get(i).getAppLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }		
            return -1;
        }
    }


    private class AppsGroupViewHolder {
        private ImageView icon;
        private TextView label;
        private TextView letters;
        private ImageView expandIcon;  
    }
    private class ActionsItemViewHolder {
        private TextView actionMessage;
    }	

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.i(TAG, "onPause");
        this.unbindService(mEncryptServiceConn);
        this.finish();
    }	

    /*
       @Override  
       public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {  
           this.visibleItemCount = visibleItemCount;  
           visibleLastIndex = firstVisibleItem + visibleItemCount - 1; 
           Log.i(TAG, "onScroll visibleItemCount = " + visibleItemCount + "| visibleLastIndex =" + visibleLastIndex + " firstVisibleItem ="  + firstVisibleItem + "####");

       }  

       @Override  
       public void onScrollStateChanged(AbsListView view, int scrollState) {  
           int itemsLastIndex = mAppExpandableListViewAdapter.getGroupCount() - 1;
           int lastIndex = itemsLastIndex;
           Log.i(TAG, "onScrollStateChanged" + 
               "| itemsLastIndex = " + itemsLastIndex + 
               "| lastIndex =" + lastIndex +
               "| visibleLastIndex =" + visibleLastIndex +
               "| scrollState =" + scrollState +
               "| mResolveInfos.size() = " + mResolveInfos.size());		
           if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == lastIndex 
               && visibleLastIndex < (mResolveInfos.size()-1)
               && getListData().size() < mResolveInfos.size() ) { 
               Log.i(TAG, "onScrollStateChanged loading...");
               loadMore(view);
           } 
       }
  */

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long value) {
        // TODO Auto-generated method stub
        /*Log.i(TAG, "onItemClick position = " + position + 
          " |v.getId() = " + view.getId() + 
          " | mIntentFromStr =" + mIntentFromStr + "####");

      String appName = mAppDataList.get(position).getAppName();
      String packageName = mAppDataList.get(position).getPackageName();
      String className = mAppDataList.get(position).getClassName();
      mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_APP_NAME,appName);
      mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_PACKAGE_NAME, packageName);
      mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_CLASS_NAME, className);
      mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_FINGERID, mFingerId);

      if("QuickBoot".equals(mIntentFromStr))
      {
      this.setResult(EncryUtil.APP_FOR_QUICK_BOOT_RESULT_CODE,mResultIntent);
      } else {
      this.setResult(EncryUtil.APP_FOR_NORMAL_CUST_RESULT_CODE,mResultIntent);
      }
      finish();

      if (hasMoreAppQuickActions(mAppDataList.get(position).getAppActionsList())) {
      mAppQuickActionsMode = APP_QUICKACTIONS_MODE_EXPANDED;
      } else {
      mAppQuickActionsMode = APP_QUICKACTIONS_MODE_COLLAPSED;
      }*/
       //recountAppQuickActionSubItems();
    }	


    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
        try {
            String appName = "";
            String packageName = ""; 
            String className = "";
            String category = "";
            String action = "";
            String data = "";
            String shortcut_name = "";
            Shortcut shortcutItemSelected;
            Intent shortcut_intent;
            AppData appDataSelected;

            appDataSelected = mAppDataList.get(groupPosition);		
            shortcutItemSelected = appDataSelected.getAppActionsList().get(childPosition);
            shortcut_intent = shortcutItemSelected.intent;
            shortcut_name = shortcutItemSelected.shortLabel;
            packageName = appDataSelected.getPackageName();

            if (DEBUG)Log.i(TAG, "onChildClick groupPosition = " + groupPosition +
                " | childPosition = " + childPosition + 
                " | id = " + id + 
                " |shortcut_intent = " + shortcut_intent);

            if (shortcut_intent != null) {
                ComponentName component;
                component = shortcut_intent.getComponent();
                if (component != null) {
                    packageName = component.getPackageName();
                    className = component.getClassName();
                }

                if (shortcut_intent.getCategories() != null) {
                    Set<String> set = shortcut_intent.getCategories();
                    Iterator it = set.iterator(); 	
                    if (it.hasNext()) {
                        String cat = (String)it.next();			
                        if (!cat.equals(Intent.CATEGORY_LAUNCHER)) {
                            category = cat;	  	
                        }			
                    }
                }

                if (shortcut_intent.getAction() != null) {
                    action = shortcut_intent.getAction();
                }

                if (shortcut_intent.getDataString() != null) {
                    data = shortcut_intent.getDataString();
                }
            }	

            appName = appDataSelected.getAppName();

            if ( childPosition == 0) {
                mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_APP_NAME,appName);
            } else {
                mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_APP_NAME,shortcut_name);
            }

            mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_PACKAGE_NAME, packageName);
            mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_CLASS_NAME, className);
            mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_APP_CATEGORY, category);
            mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_APP_ACTION, action);
            mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_APP_DATA, data);
            mResultIntent.putExtra(EncryUtil.KEY_QUICK_BOOT_FINGERID, mFingerId);

            if ("QuickBoot".equals(mIntentFromStr))
            {
                this.setResult(EncryUtil.APP_FOR_QUICK_BOOT_RESULT_CODE,mResultIntent);
            } else {
                this.setResult(EncryUtil.APP_FOR_NORMAL_CUST_RESULT_CODE,mResultIntent);
            }

            finish();	
            return true;
        } catch(Exception e) { 
            Log.e(TAG, "onChildClick =====Error = " +e);
            e.printStackTrace();
            return false;
        }

     }

/*
@Override  
public boolean onGroupClick(ExpandableListView parent, View clickedView, int groupPosition, long groupId) {  
if (mAppsExpandListView.isGroupExpanded(groupPosition)) {

} else {

}
return true;
}  

public void loadMore(View view) {
mHandler.post(new Runnable() {  
@Override  
public void run() {
int current_scroll_position;
loadData();          
mAppExpandableListViewAdapter.notifyDataSetChanged(); 			
current_scroll_position = visibleLastIndex - visibleItemCount+1;
Log.i(TAG, "loadMore current_scroll_position="+current_scroll_position); 
mAppsExpandListView.setSelection(current_scroll_position);  
}  
});
}  

private void loadData() {
int count = mAppExpandableListViewAdapter.getGroupCount();
int size = count + 2;
if ( size < mResolveInfos.size() ) {
loadAdapterAppDatas(AppDataListChoiceActivity.this, count, size, false);
} else {
loadAdapterAppDatas(AppDataListChoiceActivity.this, count, mResolveInfos.size(), false);
} 
}  
*/
    public static String setSortLetters(String date){
        String pinyin = CharacterParser.getInstance().getSelling(date);
        String sortString = pinyin.substring(0, 1).toUpperCase();

        if (sortString.matches("[A-Z]")) {
            return sortString.toUpperCase();
        } else {
            return "#";
        }	
    }

    public static boolean isNotSupportedApp(String name) {
        String activityList[] = {
            "com.android.launcher3.Launcher",
            "com.ape.secrecy.EncryptBoxMain",
            "com.android.inputmethod.latin.setup.SetupActivity",
            "com.google.android.apps.enterprise.dmagent.DMAgentActivity",
            "com.ironsource.appmanager.ui.activities.LauncherPostOOBEActivity"
        };

        if (DEBUG) Log.i(TAG, "isNotSupportedApp="+name);

        if (name == null) {
            return false ;
        }

        for (int i=0 ;i<activityList.length ;i++){
            if(name.equals(activityList[i])){
                if (DEBUG) Log.i(TAG, "isNotSupportedApp="+name+"--------return true");
                return true;
            }
        }
        return false;
    }

    void removeTheDuplicateItemFromList(){
        for (int i=0; i < mAppDataList.size() -1; i++) {
            for (int j= mAppDataList.size() -1; j > i; j--) {
                if (mAppDataList.get(j).getClassName().equals(mAppDataList.get(i).getClassName())) {
	             mAppDataList.remove(j);		
                }
            }   
        }

    }

}
