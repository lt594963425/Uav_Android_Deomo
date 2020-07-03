package com.dji.ux.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.dji.ux.sample.base.BaseDialog;
import com.dji.ux.sample.base.Constanst;
import com.dji.ux.sample.base.SPManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightMode;
import dji.common.flightcontroller.RTKState;
import dji.common.util.CommonCallbacks;
import dji.keysdk.CameraKey;
import dji.keysdk.KeyManager;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.RTK;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.ux.widget.FPVWidget;
import dji.ux.widget.controls.CameraControlsWidget;

/**
 * Activity that shows all the UI elements together
 */
public class CompleteWidgetActivity extends AppCompatActivity implements AMap.OnMapClickListener, LocationSource, AMap.OnCameraChangeListener, AMap.OnMarkerClickListener, GeocodeSearch.OnGeocodeSearchListener, AMapLocationListener {
    /**
     * 地图设置
     * map
     ***/
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private GeocodeSearch geocoderSearch;
    public AMapLocation aMapLocation;
    private MapView mapWidget;

    private ViewGroup parentView;
    private FPVWidget fpvWidget;
    private FPVWidget secondaryFPVWidget;
    private RelativeLayout primaryVideoView;
    private FrameLayout secondaryVideoView;
    //是否第一次定位
    private boolean isFirst = true;
    public LatLng myLatLng;
    private int height;
    private int width;
    private int margin;
    private int deviceWidth;
    private int deviceHeight;
    private AMap aMap;
    private UiSettings mUiSettings;
    // true 视频
    public boolean FPVISBIG = true;
    public boolean isSelectedType = false;
    private CameraPosition mCameraPosition;
    //面积
    private TextView rangAreTv;
    //经纬度
    private TextView latlngTv;
    private FlightMode mFlightMode;
    private ImageView cleanPointIv;
    private LinearLayout controlBottomLayout;
    private Switch measureSc;
    private CheckBox uav_map_lock_cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_default_widgets);
        StatusBarUtils.setNavBarVisibility(getWindow(), this, false);
        initWindowParas();
        EventBus.getDefault().register(this);
        measureSc = findViewById(R.id.measure_sc);
        controlBottomLayout = findViewById(R.id.control_bottom_layout);
        openLittltUAVLationLock = SPManager.getBoolean(SPManager.SP_MAIN_FLAG, Constanst.UAVMAPLOCK, false);
        isSelectedType = SPManager.getBoolean(SPManager.SP_MAIN_FLAG, Constanst.UAVMAPDATA, false);

        uav_map_lock_cb = findViewById(R.id.uav_map_lock_cb);
        uav_map_lock_cb.setChecked(openLittltUAVLationLock);
        uav_map_lock_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                openLittltUAVLationLock = isChecked;
                SPManager.saveBoolean(SPManager.SP_MAIN_FLAG, Constanst.UAVMAPLOCK, openLittltUAVLationLock);
            }
        });
        findViewById(R.id.clean_point_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapMeaureUtil.getInstance().clearMarker();
            }
        });
        ;
        findViewById(R.id.revoke_point_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapMeaureUtil.getInstance().revokeMarker();
            }
        });
        findViewById(R.id.input_point_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.getInstance().showInputLngLatDialog(CompleteWidgetActivity.this, new DialogUtils.OnButtonLngLatClickListener() {
                    @Override
                    public void onPositiveButtonClick(BaseDialog dialog, double lng, double lat) {
                        dialog.dismiss();
                        converter.from(CoordinateConverter.CoordType.GPS);
                        converter.coord(new LatLng(lat, lng));
                        LatLng latLng = converter.convert();
                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                        MapMeaureUtil.getInstance().addMarker(latLng);
                    }

                    @Override
                    public void onCancelButtonClick(BaseDialog dialog) {
                        dialog.dismiss();
                    }
                });
            }
        });
        findViewById(R.id.add_point_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectedType) {
                    if (mCameraPosition != null) {
                        MapMeaureUtil.getInstance().addMarker(mCameraPosition.target);
                    }
                } else {
                    if (gaoDeDroneLatLng != null) {
                        MapMeaureUtil.getInstance().addMarker(gaoDeDroneLatLng);
                    } else {
                        Toast.makeText(CompleteWidgetActivity.this, "未获取到无人机位置", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        mapWidget = findViewById(R.id.map_widget);
        mapWidget.onCreate(savedInstanceState);
        initmap();
        //面积显示
        rangAreTv = (TextView) findViewById(R.id.rang_are_tv);
        latlngTv = (TextView) findViewById(R.id.latlng_tv);
        parentView = (ViewGroup) findViewById(R.id.root_view);
        findViewById(R.id.app_ui_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        fpvWidget = findViewById(R.id.fpv_widget);
        fpvWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!FPVISBIG) {
                    onViewClick(fpvWidget);
                }
            }
        });
        primaryVideoView = (RelativeLayout) findViewById(R.id.fpv_container);
        secondaryVideoView = (FrameLayout) findViewById(R.id.secondary_video_view);
        secondaryFPVWidget = findViewById(R.id.secondary_fpv_widget);
        secondaryFPVWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapVideoSource();
            }
        });
        updateSecondaryVideoVisibility();

        onSDKManagerCallbackEvent(new OnSDKManagerCallbackEvent());

        MapMeaureUtil.getInstance().init(aMap, new MapMeaureUtil.CalculationAreaListener() {
            @Override
            public void updateData(String s) {
                rangAreTv.setText(s + "");
            }
        });
        measureSc.setChecked(isSelectedType);
        measureSc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSelectedType = isChecked;
                SPManager.saveBoolean(SPManager.SP_MAIN_FLAG, Constanst.UAVMAPDATA, isSelectedType);
            }
        });
    }

    private void initWindowParas() {
        height = DensityUtil.dip2px(this, 100);
        width = DensityUtil.dip2px(this, 150);

        margin = DensityUtil.dip2px(this, 12);
        // 275 413 手机
        // 300 450 平板
        Log.e("initWindowParas", height + "---------------" + width);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;
    }


    private void initmap() {
        if (aMap == null) {
            aMap = mapWidget.getMap();
            mUiSettings = aMap.getUiSettings();
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);

            setUpMap();
            aMap.setOnMapClickListener(this);
            geocoderSearch = new GeocodeSearch(this);
            geocoderSearch.setOnGeocodeSearchListener(this);

        }
    }

    private void setUpMap() {
        //定位的小图标 默认是蓝点 这里自定义一团火，其实就是一张图片
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 设置LinkFly的图标
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.rtk_enable_icon);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(this, 26), DensityUtil.dip2px(this, 26)));
        BitmapDescriptor markerIcon = BitmapDescriptorFactory
                .fromView(imageView);
        myLocationStyle.myLocationIcon(markerIcon);
        myLocationStyle.radiusFillColor(android.R.color.transparent);
        myLocationStyle.strokeColor(android.R.color.transparent);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        //alert("开始设置位置监听", Toast.LENGTH_SHORT);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setRotateGesturesEnabled(false);// 禁止手势旋转
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setMyLocationButtonEnabled(false);
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMarkerClickListener(this);

        aMap.setMyLocationType(AMap.MAP_TYPE_NORMAL);

    }


    private void updateSecondaryVideoVisibility() {
        if (secondaryFPVWidget.getVideoSource() == null) {
            secondaryVideoView.setVisibility(View.GONE);
        } else {
            secondaryVideoView.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSDKManagerCallbackEvent(OnSDKManagerCallbackEvent event) {
        if (DJIModuleVerificationUtil.isFlightControllerAvailable()) {
            getFlightControllerState();
            getCompass();
        }
    }

    public void getFlightControllerState() {
        if (MApplication.getAircraftInstance() == null) {
            return;
        }
        MApplication.mFpvHandler.post(new Runnable() {
            @Override
            public void run() {
                //判断是否是RTK链接
                if (DJIModuleVerificationUtil.isRtkAvailable()) {
                    MApplication.getAircraftInstance().getFlightController().getRTK()
                            .getRtkEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {
                                    if (aBoolean) {
                                        RTK mRtk = MApplication.getAircraftInstance().getFlightController().getRTK();
                                        if (mRtk != null && mRtk.isConnected()) {
                                            getMflyRKTControlStatus(mRtk);
                                        } else {
                                            getMflyControlStatus();
                                        }
                                    } else {
                                        getMflyControlStatus();
                                    }
                                }

                                @Override
                                public void onFailure(DJIError djiError) {
                                    getMflyControlStatus();
                                }
                            });
                } else {
                    getMflyControlStatus();
                }

            }
        });


    }

    private void getMflyRKTControlStatus(RTK mRtk) {
        mRtk.setStateCallback(new RTKState.Callback() {
            @Override
            public void onUpdate(@NonNull RTKState rtkState) {
                MApplication.mFpvHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        flightControllerState = MApplication.getAircraftInstance().getFlightController().getState();
                        mFlightMode = flightControllerState.getFlightMode();
                        getUpdataFpvRTKState(rtkState);
                    }
                });
            }
        });
    }

    private void getMflyControlStatus() {
        MApplication.getAircraftInstance().getFlightController()
                .setStateCallback(new FlightControllerState.Callback() {
                    @Override
                    public void onUpdate(@NonNull FlightControllerState state) {
                        MApplication.mFpvHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                flightControllerState = state;
                                mFlightMode = flightControllerState.getFlightMode();

                                update(flightControllerState);
                            }
                        });

                    }
                });
    }

    // 飞行器主体
    protected FlightControllerState flightControllerState;
    public float droneHeight;// 飞机相对高度
    public float mAltitude;//海拔高度
    public double mHomeLatitude;// 飞机返航点纬度
    public double mHomeLongitude;// 飞机返航点经度
    public double drone_lat;// 飞机纬度
    public double drone_log;// 飞机经度
    public double yaw = 0;// 飞机方位角
    public double pitch;//
    public double roll;//
    public LatLng homeLatLng = null;
    private Marker mHomeMarker;  //返航点的 Marker
    public boolean isUpdateBackPoint = false;// 是否更新返航点坐标
    public CoordinateConverter converter = new CoordinateConverter(this);
    public LatLng gaoDeDroneLatLng = null;//转换后的飞机坐标
    public Marker aircraftMarker;//飞机位置
    //锁定飞机位置;
    private boolean openLittltUAVLationLock = false;
    public Polyline polyline;//飞机和Home点的连线
    public Compass compass;

    private void getCompass() {
        try {
            if (MApplication.getAircraftInstance() == null) {
                return;
            }
            FlightController flightController = ((Aircraft) MApplication
                    .getProductInstance()).getFlightController();
            compass = flightController.getCompass();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新飞行状态
     *
     * @param state
     */
    public void update(FlightControllerState state) {
        try {
            droneHeight = state.getAircraftLocation().getAltitude();
            //海拔高度
            mAltitude = state.getTakeoffLocationAltitude();
            if (!new Float(mAltitude).isNaN()) {
                mAltitude = (Math.round(state.getTakeoffLocationAltitude() * 100)) / 100;
            }
            mHomeLatitude = state.getHomeLocation().getLatitude();
            mHomeLongitude = state.getHomeLocation().getLongitude();
            int satellite_count = state.getSatelliteCount();
            drone_lat = state.getAircraftLocation().getLatitude();
            drone_log = state.getAircraftLocation().getLongitude();

            yaw = state.getAttitude().yaw;
            pitch = state.getAttitude().pitch;
            roll = state.getAttitude().roll;


            if (checkGpsCoordinate(mHomeLatitude, mHomeLongitude) && !isUpdateBackPoint) {
                isUpdateBackPoint = true;
                setHomeImage(mHomeLatitude, mHomeLongitude);
            }
            if (checkGpsCoordinate(drone_lat, drone_log)) {
                //画出飞机的marker
                showAircraftImage();
                drowLine();
            }

            if (null != compass) {
                if (compass.hasError() && !compass.isCalibrating()) {
                    Toast.makeText(this, "强磁干扰！请远离磁源", Toast.LENGTH_SHORT).show();
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    latlngTv.setText("经纬度：" + String.format("%.15f",drone_log)+ "," +String.format("%.15f",drone_lat));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
//            Intent attachedIntent = new Intent();
//            attachedIntent.setAction(DJISDKManager.USB_ACCESSORY_ATTACHED);
//            sendBroadcast(attachedIntent);
        }
    }

    /**
     * 飞机状态更新RTK
     *
     * @param state
     */
    private void getUpdataFpvRTKState(RTKState state) {
        /*
        String description =
                "A=RTKBeingUsed是否正在使用rtk: " + rtkState.isRTKBeingUsed() + "\n"
                        + "B=distanceToHomePointDataSource到本站数据源的距离: " + rtkState.getDistanceToHomePointDataSource() + "\n"
                        + "C=TakeoffAltitudeRecorded，true表示飞机起飞时飞行控制器记录了高度\n: " + rtkState.isTakeoffAltitudeRecorded() + "\n" +

                        "DA=DistanceToHomePointDataSource起始点数据源: " + rtkState.getDistanceToHomePointDataSource() + "\n" +

                        "DA=DistanceToHomePointDataSource起始点数据源Two: " + rtkState.getHomePointDataSource() +
                        "DB=HomePointLocation起始点位置: " + rtkState.getHomePointLocation() + "\n" +
                        "DC=SatelliteCount,GPS或RTK卫星计数: " + rtkState.getSatelliteCount() + "\n"
                        + "E=TakeOffAltitude起飞高度: " + rtkState.getTakeOffAltitude() + "\n"
                        + "F=DistanceToHomePoint离家点的距离: " + rtkState.getDistanceToHomePoint() + "\n" +

                        "G=PositioningSolution描述了用于确定定位的方法: " + rtkState.getPositioningSolution() + "\n"
                        + "H=Error错误信息: " + rtkState.getError() + "\n"
                        + "I=HeadingValid航向有效性: " + rtkState.isHeadingValid() + "\n" +

                        "J=Heading移动站度数: " + rtkState.getHeading() + "\n"
                        + "K=HeadingSolution确定精度的方法: " + rtkState.getHeadingSolution() + "\n"
                        + "L=MobileStationLocation指示RTK位置数据: " + rtkState.getMobileStationLocation() + "\n" +

                        "M=MobileStationAltitude移动台接收器相对于地面系统位置的高度: " + rtkState.getMobileStationAltitude() + "\n"
                        + "N=MobileStationStandardDeviation以米为单位的定位精度的标准偏差: " + rtkState.getMobileStationStandardDeviation().getStdLatitude()
                        + "," + rtkState.getMobileStationStandardDeviation().getStdLongitude() + "," + rtkState.getMobileStationStandardDeviation().getStdAltitude() + "\n"
                        + "O=FusionMobileStationLocation移动台的融合位置: " + rtkState.getFusionMobileStationLocation() + "\n" +

                        "P=FusionMobileStationAltitude移动台的融合高度: " + rtkState.getFusionMobileStationAltitude() + "\n"
                        + "Q=FusionHeading移动台的融合航向: " + rtkState.getFusionHeading() + "\n"
                        + "R=BaseStationLocation基站的位置坐标: " + rtkState.getBaseStationLocation() + "\n" +

                        "S=BaseStationAltitude基站在海平面以上的高度: " + rtkState.getBaseStationAltitude() + "\n"
                        + "T=MobileStationReceiver1GPSInfo单个RTK接收器GPS信息,卫星计数: " + rtkState.getMobileStationReceiver1GPSInfo().getSatelliteCount() + "\n"
                        + "U=MobileStationReceiver1BeiDouInfo单个RTK接收器北斗信息,卫星计数: " + rtkState.getMobileStationReceiver1BeiDouInfo().getSatelliteCount() + "\n" +

                        "V=MobileStationReceiver1GLONASSInfo 每个接收器连接到单个天线: " + rtkState.getMobileStationReceiver1GLONASSInfo().getSatelliteCount() + "\n"
                        + "W=MobileStationReceiver1GalileoInfo每个接收器连接到单个天线: " + rtkState.getMobileStationReceiver1GalileoInfo().getSatelliteCount() + "\n"
                        + "X=MobileStationReceiver2GPSInfo移动台2GPS信息: " + rtkState.getMobileStationReceiver2GPSInfo().getSatelliteCount() + "\n" +

                        "Y=MobileStationReceiver2BeiDouInfo移动台2北斗信息: " + rtkState.getMobileStationReceiver2BeiDouInfo().getSatelliteCount() + "\n"
                        + "Z=MobileStationReceiver2GLONASSInfo移动台信息: " + rtkState.getMobileStationReceiver2GLONASSInfo().getSatelliteCount() + "\n"
                        + "ZA=MobileStationReceiver2GalileoInfo移动台信息: " + rtkState.getMobileStationReceiver2GalileoInfo().getSatelliteCount() + "\n" +

                        "ZB=BaseStationReceiverGPSInfo: " + rtkState.getBaseStationReceiverGPSInfo().getSatelliteCount() + "\n"
                        + "ZC=BaseStationReceiverBeiDouInfo: " + rtkState.getBaseStationReceiverBeiDouInfo().getSatelliteCount() + "\n"
                        + "ZD=BaseStationReceiverGLONASSInfo: " + rtkState.getBaseStationReceiverGLONASSInfo().getSatelliteCount() + "\n" +
                        "ZE=BaseStationReceiverGalileoInfo: " + rtkState.getBaseStationReceiverGalileoInfo().getSatelliteCount() + "\n"
                        + "ZF=MobileStationReceiver1GPSInfo: " + rtkState.getMobileStationReceiver1GPSInfo().getSatelliteCount() + "\n"
                        + "ZG=MobileStationReceiver1BeiDouInfo: " + rtkState.getMobileStationReceiver1BeiDouInfo().getSatelliteCount() + "\n";
*/
        try {
            droneHeight = state.getFusionMobileStationAltitude();
            //海拔高度
            mAltitude = state.getTakeOffAltitude();
            if (!new Float(mAltitude).isNaN()) {
                mAltitude = (Math.round(state.getTakeOffAltitude() * 100)) / 100;
            }
            mHomeLatitude = state.getHomePointLocation().getLatitude();
            mHomeLongitude = state.getHomePointLocation().getLongitude();
            drone_lat = state.getFusionMobileStationLocation().getLatitude();
            drone_log = state.getFusionMobileStationLocation().getLongitude();

            yaw = state.getFusionHeading();
            pitch = flightControllerState.getAttitude().pitch;
            roll = flightControllerState.getAttitude().roll;


            if (checkGpsCoordinate(mHomeLatitude, mHomeLongitude) && !isUpdateBackPoint) {
                isUpdateBackPoint = true;
                setHomeImage(mHomeLatitude, mHomeLongitude);
            }
            if (checkGpsCoordinate(drone_lat, drone_log)) {
                //画出飞机的marker
                showAircraftImage();
                drowLine();
            }
            if (null != compass) {
                if (compass.hasError() && !compass.isCalibrating()) {
                    Toast.makeText(this, "强磁干扰！请远离磁源", Toast.LENGTH_SHORT).show();
                }
            }
            // 1 转换后的飞机坐标 gaoDeDroneLatLng
            // 2距离 ，云台俯仰角，gimbalPitch
            // 3 飞机相对高度 ，
            // 4飞机飞行速度
            // 5 homeDistance
            // 6飞机经度纬度
            // 7 中心点的距离 mLen
            // 8 高德坐标 根据红外测距计算得出第三点的坐标 latLonPoint
            // 9 GPSTargetPoint = null;// 根据红外测距计算得出第三点的坐标 GPSTargetPoint
            // 10 飞机方位角 yaw

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    latlngTv.setText("经纬度：" + String.format("%.15f",drone_log)+ "," +String.format("%.15f",drone_lat));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
//            Intent attachedIntent = new Intent();
//            attachedIntent.setAction(DJISDKManager.USB_ACCESSORY_ATTACHED);
//            sendBroadcast(attachedIntent);
        }
    }

    /**
     * 画出飞机和Home点的连线
     */
    private void drowLine() {
        try {
            if (polyline != null) {
                polyline.remove();
            }
            if (gaoDeDroneLatLng == null) {
                return;
            }
            PolylineOptions opts = new PolylineOptions();
            List<LatLng> points = new ArrayList<LatLng>();
            points.add(new LatLng(homeLatLng.latitude, homeLatLng.longitude));
            points.add(gaoDeDroneLatLng);
            opts.addAll(points);
            opts.width(5);
            opts.color(0xFFF1BB07);
            polyline = aMap.addPolyline(opts);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setHomeImage(double lat, double lng) {
        try {
            //
            converter.from(CoordinateConverter.CoordType.GPS);
            // sourceLatLng待转换坐标点 DPoint类型
            converter.coord(new LatLng(lat, lng));
            // 执行转换操作
            homeLatLng = converter.convert();
            if (aMap != null) {
                if (mHomeMarker == null) {
                    mHomeMarker = aMap.addMarker(new MarkerOptions().position(homeLatLng).zIndex(1).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_compass_home))));
                    mHomeMarker.setAnchor(0.5f, 0.5f);
                } else {
                    mHomeMarker.setPosition(homeLatLng);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示飞机位置
     */
    private void showAircraftImage() {
        try {
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(new LatLng(drone_lat, drone_log));
            gaoDeDroneLatLng = converter.convert();
            if (openLittltUAVLationLock) {
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(gaoDeDroneLatLng));
            }
            if (aircraftMarker != null && gaoDeDroneLatLng != null) {
                aircraftMarker.setPosition(gaoDeDroneLatLng);
                aircraftMarker.setZIndex(5);
                aircraftMarker.setRotateAngle((float) parseYawToAntiClockwise360(yaw));
            } else {
                if (aMap != null && gaoDeDroneLatLng != null) {
                    //飞机位置
                    if (aircraftMarker == null) {
                        aircraftMarker = aMap.addMarker(new MarkerOptions().zIndex(5).
                                icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                        .decodeResource(getResources(), R.drawable.ic_compass_aircraft))));
                    }
                    aircraftMarker.setAnchor(0.5f, 0.4f);
                    aircraftMarker.setPosition(gaoDeDroneLatLng);
                    aircraftMarker.setRotateAngle((float) parseYawToAntiClockwise360(yaw));
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * -180~0~（180）转逆时针方位角0-360
     * <p>
     * -90  -> 90
     * 90   -> 270
     *
     * @param value
     * @return
     */
    public static double parseYawToAntiClockwise360(double value) {
        double yaw = 0;
        try {
            if (value < 0) {
                yaw = -value;
            } else if (value > 0) {
                yaw = 360 - value;
            } else {
                yaw = value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return yaw;
    }

    private void hidePanels() {
        //These panels appear based on keys from the drone itself.
        if (KeyManager.getInstance() != null) {
            KeyManager.getInstance().setValue(CameraKey.create(CameraKey.HISTOGRAM_ENABLED), false, null);
            KeyManager.getInstance().setValue(CameraKey.create(CameraKey.COLOR_WAVEFORM_ENABLED), false, null);
        }

        //These panels have buttons that toggle them, so call the methods to make sure the button flightControllerState is correct.
        CameraControlsWidget controlsWidget = findViewById(R.id.CameraCapturePanel);
        controlsWidget.setAdvancedPanelVisibility(false);
        controlsWidget.setExposurePanelVisibility(false);

        //These panels don't have a button flightControllerState, so we can just hide them.
        findViewById(R.id.pre_flight_check_list).setVisibility(View.GONE);
        findViewById(R.id.rtk_panel).setVisibility(View.GONE);
        findViewById(R.id.spotlight_panel).setVisibility(View.GONE);
        findViewById(R.id.speaker_panel).setVisibility(View.GONE);
    }

    private void onViewClick(View view) {
        if (isFastDoubleClick()) {
            Toast.makeText(this, "切换过快", Toast.LENGTH_SHORT).show();
            return;
        }
        if (view == fpvWidget && !FPVISBIG) {
            resizeFPVWidget(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 0, 0);
            resizeMapWidget(mapWidget, width, height, margin);
        } else if (view == mapWidget && FPVISBIG) {
            hidePanels();
            resizeFPVWidget(width, height, margin, 12);
            resizeMapWidget(mapWidget, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 0);
        }
        FPVISBIG = !FPVISBIG;
    }


    private void swapVideoSource() {
        if (secondaryFPVWidget.getVideoSource() == FPVWidget.VideoSource.SECONDARY) {
            fpvWidget.setVideoSource(FPVWidget.VideoSource.SECONDARY);
            secondaryFPVWidget.setVideoSource(FPVWidget.VideoSource.PRIMARY);
        } else {
            fpvWidget.setVideoSource(FPVWidget.VideoSource.PRIMARY);
            secondaryFPVWidget.setVideoSource(FPVWidget.VideoSource.SECONDARY);
        }
    }

    private void resizeMapWidget(View mView, int mToWidth, int mToHeight, int margin) {
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mView.getLayoutParams();
        p.height = (int) mToHeight;
        p.width = (int) mToWidth;
        p.rightMargin = margin;
        p.bottomMargin = margin;
        mView.requestLayout();
    }

    private void resizeFPVWidget(int width, int height, int margin, int fpvInsertPosition) {
        RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) fpvWidget.getLayoutParams();
        RelativeLayout.LayoutParams fpvLayoutparams = (RelativeLayout.LayoutParams) primaryVideoView.getLayoutParams();
        fpvParams.height = height;
        fpvParams.width = width;
        fpvLayoutparams.height = height;
        fpvLayoutparams.width = width;
        if (FPVISBIG) {
            fpvLayoutparams.rightMargin = margin;
            fpvLayoutparams.bottomMargin = margin;
            fpvLayoutparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            fpvLayoutparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            primaryVideoView.setLayoutParams(fpvLayoutparams);
            fpvWidget.setLayoutParams(fpvParams);
            primaryVideoView.bringToFront();
            fpvWidget.setVideoSource(FPVWidget.VideoSource.AUTO);
        } else { //视频放大
//            fpvLayoutparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
//            fpvLayoutparams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            fpvLayoutparams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            primaryVideoView.setLayoutParams(fpvLayoutparams);
            fpvWidget.setLayoutParams(fpvParams);
            fpvWidget.requestLayout();
            parentView.removeView(primaryVideoView);
            parentView.addView(primaryVideoView, fpvInsertPosition);
            fpvWidget.setVideoSource(FPVWidget.VideoSource.AUTO);
        }
    }

    /**
     * Prevent continuous click, jump two pages
     */
    private static long lastClickTime;
    private final static long TIME = 2000;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < TIME) {
            return true;
        }
        lastClickTime = time;

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private long exitTime = 0;

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Hide both the navigation bar and the status bar.
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mapWidget.onResume();
    }

    @Override
    protected void onPause() {
        mapWidget.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapWidget.onDestroy();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapWidget.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapWidget.onLowMemory();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        StatusBarUtils.setNavBarVisibility(getWindow(), this, false);
    }

    /**
     * 地图点击
     *
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        if (FPVISBIG) {
            onViewClick(mapWidget);
        }


    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setMockEnable(false);
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(3000);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        mCameraPosition = cameraPosition;
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        mCameraPosition = cameraPosition;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                aMapLocation = amapLocation;
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                if (isFirst) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                    isFirst = false;
                }
                myLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                //获取定位信息
                StringBuffer buffer = new StringBuffer();
                buffer.append(amapLocation.getCountry() + ""
                        + amapLocation.getProvince() + ""
                        + amapLocation.getCity() + ""
                        + amapLocation.getDistrict() + ""
                        + amapLocation.getStreet() + ""
                        + amapLocation.getStreetNum());

            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();

                Log.e("AmapErr", errText);
            }
        }
    }

    public boolean checkGpsCoordinate(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f
                && longitude != 0f);
    }


}
