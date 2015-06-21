package com.tata.trackit;

import java.text.DecimalFormat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import cn.sharesdk.demo.Laiwang;
import cn.sharesdk.demo.OneKeyShareCallback;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.globalLibrary.util.Conts;
import com.tata.trackit.base.BaseActivity;

public class ActivityWorkRun extends BaseActivity implements OnClickListener,BaiduMap.OnMapClickListener, OnGetRoutePlanResultListener {

    ImageView backIv;
    ImageView dog_iv;
    AnimationDrawable animationDrawable;
    TextView command_tv;
    RelativeLayout start_work_dog_layout;
    boolean isStop = false;
    boolean isFrist = false;
    LinearLayout layout_bottom_info;
    TextView check_sort_info_tv;
    TextView share_info_tv;
    RouteLine route = null;
    OnekeyShare oks;
    private GetLocateBroadcastInfo getLocateBroadcastInfo = null;
    private LocationClient mLocationClient;
    private boolean isGetLocateBroadcastRegister = false;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    OverlayManager routeOverlay = null;
    BitmapDescriptor dingwei = BitmapDescriptorFactory.fromResource(R.drawable.dingwei);
    BitmapDescriptor center = BitmapDescriptorFactory.fromResource(R.drawable.dian_map);
    TextView work_dog_time ;
    TextView work_dog_distance ;
    Long finisTime = 0l;
    Long curTime ;
    Double finisDistance ;
    boolean isFristGetLocation = true;
    LatLng startLatLng;
    LatLng endLatLng;
    
    PlanNode stNode = null;
    PlanNode enNode = null;
    // 搜索相关
    RoutePlanSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
          
            switch (msg.what) {
            case 0:
                finisTime = System.currentTimeMillis()-curTime+finisTime;
                long second =finisTime/1000;
                long minute1=second/60;  
                long second1=second%60; 
                work_dog_time.setText(minute1+":"+second1);
                mHandler.sendEmptyMessageDelayed(0, 1000);
                curTime = System.currentTimeMillis();
                break;

            default:
                break;
            }
            super.handleMessage(msg);
        }
        
        
    };
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_work_dog_map);
        backIv = (ImageView) findViewById(R.id.back);
        dog_iv = (ImageView) findViewById(R.id.dog_iv);
        command_tv = (TextView) findViewById(R.id.command_tv);
        check_sort_info_tv = (TextView) findViewById(R.id.check_sort_info_tv);
        share_info_tv = (TextView) findViewById(R.id.share_info_tv);
        work_dog_time= (TextView) findViewById(R.id.work_dog_time);
        work_dog_distance= (TextView) findViewById(R.id.work_dog_distance);
        layout_bottom_info = (LinearLayout) findViewById(R.id.layout_bottom_info);
        start_work_dog_layout = (RelativeLayout) findViewById(R.id.start_work_dog_layout);
        backIv.setOnClickListener(this);
        check_sort_info_tv.setOnClickListener(this);
        isFristGetLocation = true;
        share_info_tv.setOnClickListener(this);
        start_work_dog_layout.setOnClickListener(this);
        dog_iv.setImageResource(R.drawable.animation_run);
        animationDrawable = (AnimationDrawable) dog_iv.getDrawable();
        animationDrawable.start();
        isFrist = true;
        finisTime = 0l;
        layout_bottom_info.setVisibility(View.GONE);
        start_work_dog_layout.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                animationDrawable = (AnimationDrawable) dog_iv.getDrawable();
                animationDrawable.stop();
                command_tv.setText("开始遛狗");
                mHandler.removeMessages(0);
                isStop = true;
                isFrist = true;
                layout_bottom_info.setVisibility(View.VISIBLE);
                start_work_dog_layout.setBackgroundResource(R.drawable.bg_work_dog_iv);
                mLocationClient = ((IApplication) getApplication()).mLocationClient;
                ((IApplication) getApplication()).InitLocation();
                mLocationClient.start();
                return false;
            }
        });
        mMapView = (MapView) findViewById(R.id.merchant_map_activity_mapview);
        mBaiduMap = mMapView.getMap();
        ShareSDK.initSDK(this);
        ShareSDK.registerPlatform(Laiwang.class);
        oks = new OnekeyShare();
        if (getLocateBroadcastInfo == null) {
            getLocateBroadcastInfo = new GetLocateBroadcastInfo();
        }
        registerLocateBroadcastReceiver();
        mLocationClient = ((IApplication) getApplication()).mLocationClient;
        ((IApplication) getApplication()).InitLocation();
        mLocationClient.start();
        if (((IApplication) getApplication()).Latitude != 0.0 && ((IApplication) getApplication()).Longitude != 0.0) {

            LatLng cenpt = new LatLng(((IApplication) getApplication()).Latitude, ((IApplication) getApplication()).Longitude);
            MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(14).build();
            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            mBaiduMap
                    .setMyLocationConfigeration(new MyLocationConfiguration(LocationMode.FOLLOWING, true, center));
            mBaiduMap.setMapStatus(mMapStatusUpdate);
            initOverlay();
        }
        work_dog_distance.setText("0.0KM");
        work_dog_time.setText("00:00");
        curTime =System.currentTimeMillis();
        mHandler.sendEmptyMessage(0);
        hide();
        mBaiduMap.setOnMapClickListener(this);
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
    }
    
    public Bitmap createViewBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }
    
    public void clearOverlay(View view) {
        mBaiduMap.clear();
    }

    public void initOverlay() {

        clearOverlay(null);
        LatLng cenpt = new LatLng(((IApplication) getApplication()).Latitude, ((IApplication) getApplication()).Longitude);
        OverlayOptions ooA = new MarkerOptions().position(cenpt).icon(center).zIndex(-1).draggable(false)
                .perspective(false);
        mBaiduMap.addOverlay(ooA);
    }
    private void hide() {
        int childCount = mMapView.getChildCount();
        View zoom = null;
        for (int i = 0; i < childCount; i++) {
            View child = mMapView.getChildAt(i);
            if (child instanceof ZoomControls) {
                zoom = child;
                break;
            }
        }
        zoom.setVisibility(View.GONE);
        int count = mMapView.getChildCount();
        View scale = null;
        for (int i = 0; i < count; i++) {
            View child = mMapView.getChildAt(i);
            if (child instanceof ZoomControls) {
                scale = child;
                break;
            }
        }
        scale.setVisibility(View.GONE);
        UiSettings mUiSettings = mBaiduMap.getUiSettings();
        mUiSettings.setCompassEnabled(true);
        mMapView.removeViewAt(1);
    }
    /*
     * 取消定位广播接收器
     */
    private void unregisterLocateBroadcastReceiver() {
        if (isGetLocateBroadcastRegister) {
            isGetLocateBroadcastRegister = false;
            if (getLocateBroadcastInfo != null) {
                try {
                    unregisterReceiver(getLocateBroadcastInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * 注册获取定位广播接收器,设置过滤器
     */
    public void registerLocateBroadcastReceiver() {
        if (!isGetLocateBroadcastRegister) {
            isGetLocateBroadcastRegister = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Conts.LOCATEOVER_KEY);
            filter.addAction(Conts.LOCATEFAILED_KEY);
            registerReceiver(getLocateBroadcastInfo, filter);
        }
    }


    class GetLocateBroadcastInfo extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Conts.LOCATEFAILED_KEY.equals(action)) {
                if (mLocationClient != null && mLocationClient.isStarted()) {
                    mLocationClient.stop();
                }
            } else if (Conts.LOCATEOVER_KEY.equals(action)) {
                if (mLocationClient != null && mLocationClient.isStarted()) {
                    mLocationClient.stop();
                }
                LatLng cenpt = new LatLng(((IApplication) getApplication()).Latitude, ((IApplication) getApplication()).Longitude);
                if(isFristGetLocation){
                   
                    isFristGetLocation = false;
                    startLatLng = cenpt;
                  
                }else{
                    
                    LatLng cenpt2 = new LatLng(((IApplication) getApplication()).Latitude, ((IApplication) getApplication()).Longitude);
                    endLatLng = cenpt2;
                    
                    Double double1 = DistanceUtil.getDistance(endLatLng,
                            startLatLng)/1000;
                    if(double1 <= 0.01){
                        Toast.makeText(getApplicationContext(), "亲你还没开始动呢", 1).show();
                    }
                    DecimalFormat    df   = new DecimalFormat("######0.00"); 
                    work_dog_distance.setText(df.format(double1)+"KM") ; 
                    stNode = PlanNode.withLocation(startLatLng);
                    enNode = PlanNode.withLocation(endLatLng);
                    mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
                }
                MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(14).build();
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                initOverlay();
            }

        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.check_sort_info_tv:
            ActivityWorkRunSort.showActivity(this);
            break;
        case R.id.share_info_tv:
             oks.setText("测试");
             oks.setTitle("测试分享的标题");
             oks.setTitleUrl("http://sharesdk.cn");
             oks.setComment( "我对此分享内容的评论");
             oks.setSite("发布分享的网站名称");
             oks.setSiteUrl("发布分享网站的地址");
             oks. setCallback ( new OneKeyShareCallback() ) ; 
             oks.show(this.getApplicationContext());
            break;
        case R.id.back:
            finish();
            break;
        case R.id.start_work_dog_layout:

            if (isFrist) {
                isFrist = false;
                return;
            } else {
                if (isStop) {
                    clearOverlay(null);
                    OverlayOptions ooA = new MarkerOptions().position(endLatLng).icon(center).zIndex(-1).draggable(false)
                            .perspective(false);
                    mBaiduMap.addOverlay(ooA);
                    curTime =System.currentTimeMillis();
                    mHandler.sendEmptyMessage(0);
                    isStop = false;
                    layout_bottom_info.setVisibility(View.GONE);
                    dog_iv.setImageResource(R.drawable.animation_run);
                    animationDrawable = (AnimationDrawable) dog_iv.getDrawable();
                    command_tv.setText("长按停止");
                    animationDrawable.start();
                    
                    start_work_dog_layout.setBackgroundResource(R.drawable.bg_work_dog_run_iv);
                }
            }

            break;

        default:
            break;
        }

    }

    public static void showActivity(Context ctx) {
        Intent intent = new Intent(ctx, ActivityWorkRun.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterLocateBroadcastReceiver();
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            route = result.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }

        
    }
    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
//            if (useDefaultIcon) {
            
//                return BitmapDescriptorFactory.fromResource(R.drawable.dian_map);
//            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
//            if (useDefaultIcon) {
//                return BitmapDescriptorFactory.fromResource(R.drawable.dingwei);
//            }
            return null;
        }
    }
    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean onMapPoiClick(MapPoi arg0) {
        // TODO Auto-generated method stub
        return false;
    }

}
