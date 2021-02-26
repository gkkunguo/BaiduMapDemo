package com.hk.baidumapdemo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class BaiduActivityDemo2 extends AppCompatActivity implements BaiduMap.OnMarkerClickListener, BaiduMap.OnMapClickListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private final static String MAP_TITLE = "MAP_TITLE";
    private final static String MAP_CONTENT = "MAP_CONTENT";
    private View showInfoWindow;
    private List<LatLng> points = new ArrayList<>();//轨迹经纬度
    private static final String TAG = "BaiduActivityDemo2";
    public static final int MODEL_PATROL_START = 23;
    private static final float MAPZOOM_DEFAULT = 13f;
    private static final float MAPZOOM_TRACK = 17f;
    private BitmapDescriptor mBlueBitmap = BitmapDescriptorFactory.fromAsset("icon_road_blue_arrow.png");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_demo2);
        requestPermission();
        initView();
        initMap();
        initLocationOption();

    }

    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1002);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1003);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1001){
            Toast.makeText(this, "申请成功", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView(){
        mMapView = this.findViewById(R.id.mMapView);
    }

    private void initMap(){
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnMapClickListener(this);

        //点击marker时弹出展示的窗口
        showInfoWindow = LinearLayout.inflate(this,R.layout.act_baidu_demo2_map_showinfo,null);
        showInfoWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaiduActivityDemo2.this,BaiduActivityDemoDetail.class);
                BaiduActivityDemo2.this.startActivity(intent);
            }
        });
    }
    /**
     * 初始化定位参数配置
     */
    private void initLocationOption() {
        mBaiduMap.setMyLocationEnabled(true);
        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        LocationClient locationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类实例并配置定位参数
        LocationClientOption locationOption = new LocationClientOption();
        MyLocationListener myLocationListener = new MyLocationListener();
        //注册监听函数
        locationClient.registerLocationListener(myLocationListener);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(2000);
        //可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
        //可选，设置是否需要地址描述
        locationOption.setIsNeedLocationDescribe(true);
        //可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
        //可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        locationClient.setLocOption(locationOption);
        //开始定位
        locationClient.start();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(mBaiduMap != null){
            mBaiduMap.hideInfoWindow();
        }
    }

    @Override
    public void onMapPoiClick(MapPoi mapPoi) {

    }

    /**
     * 实现定位回调
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            if(location != null){
                //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
                //以下只列举部分获取经纬度相关（常用）的结果信息
                //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

                //获取纬度信息
                double latitude = location.getLatitude();
                //获取经度信息
                double longitude = location.getLongitude();
                //获取定位精度，默认值为0.0f
                float radius = location.getRadius();
                //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
                String coorType = location.getCoorType();
                //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
                int errorCode = location.getLocType();

                LatLng latLng = new LatLng( latitude,longitude);
                drawLine(location,latLng);
            }

        }
    }


    private void drawLine(BDLocation location,LatLng latLng){
        Toast.makeText(this,"latLng="+latLng.latitude,Toast.LENGTH_LONG).show();
        Log.i(TAG, "drawLine: "+points.toString());
//        mBaiduMap.clear();
        mMapView.invalidate();
        points.add(latLng);

        //开始位置
        BitmapDescriptor bitmapDescriptorStart = BitmapDescriptorFactory.fromResource(R.drawable.map_lvl_ld_start);
        OverlayOptions optionStart = new MarkerOptions()
                .position(points.get(0))//标注位置
                .icon(bitmapDescriptorStart)//图标
                .animateType(MarkerOptions.MarkerAnimateType.none)//动画类型
                .perspective(true)//是否开启近大远小效果
                .zIndex(900)//设置覆盖物的zIndex
                ;
        Overlay overlay = mBaiduMap.addOverlay(optionStart);
        // TODO: 2021/1/13 标注携带参数
        Bundle bundle = new Bundle();
        bundle.putString(MAP_TITLE,"marker标题");//传入需要使用的标注信息，当点击该标注时可以取出该标注对应的值进行相应的业务处理
        bundle.putString(MAP_CONTENT,"这是marker展示的信息");
        overlay.setExtraInfo(bundle);

        if(points.size() >= 2 ){
            //轨迹过程中的线
            OverlayOptions mOverlayOptions = new PolylineOptions()
                    .width(12)//折线宽度
                    .color(Color.argb(255, 255, 0, 0))//折线颜色
                    .dottedLine(true)////设置折线显示为虚线
                    .customTexture(mBlueBitmap)//设置路径图片
                    .points(points);
            Overlay mPolyline = mBaiduMap.addOverlay(mOverlayOptions);

        }

        //当前位置
       /* BitmapDescriptor bitmapDescriptorNow = BitmapDescriptorFactory.fromResource(R.drawable.map_lvl_ld_loc);
        OverlayOptions optionNow = new MarkerOptions()
                .position(points.get(points.size()-1))//标注位置 取得最后一个点数据
                .icon(bitmapDescriptorNow)//图标
                .animateType(MarkerOptions.MarkerAnimateType.none)//动画类型
                .perspective(true)//是否开启近大远小效果
                .zIndex(900)//设置覆盖物的zIndex
                ;
        Overlay overlayNow = mBaiduMap.addOverlay(optionNow);*/


        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.getDirection())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
        mBaiduMap.setMyLocationData(locData);

        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().target(latLng).zoom(MODEL_PATROL_START).build());
        mBaiduMap.animateMapStatus(mapStatusUpdate);


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //取出被点击marker对应的值，可以传多个
        Bundle bundle = marker.getExtraInfo();

        TextView title = showInfoWindow.findViewById(R.id.tvTitle);
        TextView content = showInfoWindow.findViewById(R.id.tvContent);

        title.setText(bundle.getString(MAP_TITLE));
        content.setText(bundle.getString(MAP_CONTENT));

        //该marker所在位置
        LatLng latLng = marker.getPosition();
        InfoWindow infoWindow = new InfoWindow(showInfoWindow,latLng,0);
        mBaiduMap.showInfoWindow(infoWindow);

        return true;//若响应点击事件，返回true，否则返回false
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}
