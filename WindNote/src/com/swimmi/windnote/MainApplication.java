package com.swimmi.windnote;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.swimmi.common.LocationHelper;

import android.app.Application;
import android.os.Handler;

/**
 * ���ȫ������
 * @author zhoubin02
 *
 */
public class MainApplication extends Application {
	// �ٶ�MapAPI�Ĺ�����
    public LocationClient mLocationClient = null;
    
    
    @Override
    public void onCreate() {
        
        super.onCreate();
        SDKInitializer.initialize(this);
        mLocationClient = new LocationClient(getApplicationContext());     //����LocationClient��
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);                                //��gps
        option.setCoorType("bd09ll");
        option.setAddrType("all");
        option.setServiceName("com.baidu.location.service_v2.9");//������������Ϊbd09ll
        option.setPriority(LocationClientOption.NetWorkFirst);  //������������
        option.setScanSpan(1000);
        //option.disableCache(true);
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener( new BDLocationListener() {
			
			@Override
			public void onReceivePoi(BDLocation arg0) {
				// TODO Auto-generated method stub
				
			}
			
			//�õ�λ��
			@Override
			public void onReceiveLocation(BDLocation location) {
				LocationHelper.location = location;
			}
		} );    //ע���������
        mLocationClient.start();
    }
    
    /*private void setLocationOption()
    {
        if(mLocationClient!= null)
        {
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);                                //��gps
            option.setCoorType("bd09ll");
            option.setAddrType("all");
            option.setServiceName("com.baidu.location.service_v2.9");//������������Ϊbd09ll
            option.setPriority(LocationClientOption.NetWorkFirst);  //������������
            option.setScanSpan(5000);
            option.disableCache(true);
            mLocationClient.setLocOption(option);
        }
    }
    
    public void closeGps()
    {
        try
        {
            mLocationClient.stop();
        }
        catch(Exception e)
        {
            
        }
    }
    
    public void locate(BDLocationListener litener)
    {
        try
        {
            mLocationClient.stop();
            
            setLocationOption();
            mLocationClient.registerLocationListener(litener);
            mLocationClient.start();
            
            handler.postDelayed(new Runnable()
            {
                
                @Override
                public void run()
                {
                    closeGps();
                }
            }, 30000);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }*/
}
