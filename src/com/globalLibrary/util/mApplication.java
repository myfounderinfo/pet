
/**
* Filename:    mApplication.java
* Copyright:   Copyright (c)2010
* Company:     Founder Mobile Media Technology(Beijing) Co.,Ltd.g
* @version:    1.0
* @since:       JDK 1.6.0_21
* Create at:   2015-6-12 下午1:47:31
* Description:
* Modification History:
* Date     Author           Version           Description
* ------------------------------------------------------------------
* 2015-6-12    王涛             1.0          1.0 Version
*/
package com.globalLibrary.util;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Vibrator;

public class mApplication extends Application{
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
        super.onCreate();
        mLocationClient = new LocationClient(this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        mGeofenceClient = new GeofenceClient(getApplicationContext());
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
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
                // address = location.getProvince();
                // if (!location.getProvince().equals(location.getCity())){
                // address = address + location.getCity();
                // }
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



