package com.tata.trackit;

import com.avos.avoscloud.AVOSCloud;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.globalLibrary.util.Conts;
import com.globalLibrary.util.StringUtil;
import com.globalLibrary.util.mApplication.MyLocationListener;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Vibrator;

public class IApplication extends Application {
    public LocationClient mLocationClient;
    public GeofenceClient mGeofenceClient;
    public MyLocationListener mMyLocationListener;
    public Vibrator mVibrator;
    public double Latitude = 0.0;
    public double Longitude = 0.0;
    public String city;
    public String MyAddress;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
	        mLocationClient = new LocationClient(this.getApplicationContext());
	        mMyLocationListener = new MyLocationListener();
	        mLocationClient.registerLocationListener(mMyLocationListener);
	        mGeofenceClient = new GeofenceClient(getApplicationContext());
	        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
		super.onCreate();
		AVOSCloud.initialize(this, "agxr9rx9y5adgifk1n0r9w1fjcz6lx5hgr61eyh3drfpy6uq", "3a2it4w4bk2g5ufdb1dkrper7ms8n11c4cnoddx124zj5yni");
	}
	 public void InitLocation() {
	        LocationClientOption option = new LocationClientOption();
	        option.setLocationMode(LocationMode.Hight_Accuracy);
	        option.setCoorType("gcj02");
	        int span = 2000;
	        option.setScanSpan(span);
	        option.setIsNeedAddress(true);
	        mLocationClient.setLocOption(option);
	    }

	 public class MyLocationListener implements BDLocationListener {
	        @Override
	        public void onReceiveLocation(BDLocation location) {
	            if (location == null) {
	                Intent intent = new Intent();
	                intent.setAction(Conts.LOCATEFAILED_KEY);
	                sendBroadcast(intent);
	                return;
	            }
	            Intent intent = new Intent();
	            int errorCode = location.getLocType();
	            if (errorCode == 61 || errorCode == 66 || errorCode == 161 || errorCode == 65) {
	                intent.setAction(Conts.LOCATEOVER_KEY);
	                intent.putExtra("Lon", location.getLongitude());
	                intent.putExtra("Lat", location.getLatitude());
	                Latitude = location.getLatitude();
	                Longitude = location.getLongitude();
	                String address = "";
	                address = address + location.getDistrict();
	                if (!StringUtil.isEmpty(location.getStreet())) {
	                    address = address + location.getStreet();
	                }
	                intent.putExtra("Address", address);
	                intent.putExtra("city", city);
	                city = location.getCity();
	                MyAddress = address;
	            } else {
	                intent.setAction(Conts.LOCATEFAILED_KEY);
	            }
	            sendBroadcast(intent);
	        }

	        public void onReceivePoi(BDLocation poiLocation) {
	            if (poiLocation == null) {
	                return;
	            }
	            StringBuffer sb = new StringBuffer(256);
	            sb.append("Poi time : ");
	            sb.append(poiLocation.getTime());
	            sb.append("\nerror code : ");
	            sb.append(poiLocation.getLocType());
	            sb.append("\nlatitude : ");
	            sb.append(poiLocation.getLatitude());
	            sb.append("\nlontitude : ");
	            sb.append(poiLocation.getLongitude());
	            sb.append("\nradius : ");
	            sb.append(poiLocation.getRadius());
	            if (poiLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
	                sb.append("\naddr : ");
	                sb.append(poiLocation.getAddrStr());
	            }
	        }
	    }
	
}
