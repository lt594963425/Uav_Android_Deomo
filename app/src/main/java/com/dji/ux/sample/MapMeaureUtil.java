package com.dji.ux.sample;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Created by ${LiuTao}.
 * KmlDataBean: Administrator
 * Name: UAV_Android
 * functiona: 测量
 * Date: 2020/7/2 0002
 * Time: 下午 15:13
 */
public class MapMeaureUtil {
    private static MapMeaureUtil mapMeaureUtil;
    // 标注点集合
    private List<Marker> labelMarkers = new ArrayList<Marker>();
    //标注点的连线集合
    private List<Polyline> labelPolylines = new ArrayList<Polyline>();
    //距离
    private List<Text> mDistanceText = new ArrayList<>();
    public double sideLength;// 边长
    public double roundLength;// 周长
    public double area;// 面积
    private Polyline mLabelPolyline = null;//连线
    private Marker mLabelMarker = null;
    private AMap aMap;
    private Polyline polyline;//封闭线
    private int fenColor = Color.argb(255, 255, 64, 129);
    private int backColor = Color.argb(187, 255, 255, 255);
    //标定点数
    private int marker_size = 0;
    private int lasIndexId = 0;
    //保存hangdianjilu
    private List<Integer> mIndexList = new ArrayList<>();
    private List<LatLng> mBoundsEWSNLatLng;
    private String mianji;
    //    private String changdu;
    private String zhouchang;
    //多边形
    public Polygon mPolygon = null;
    public List<LatLng> polygons = new ArrayList<LatLng>();

    /**
     * 获取单例
     *
     * @return
     */
    public static MapMeaureUtil getInstance() {
        if (mapMeaureUtil == null) {
            synchronized (MapMeaureUtil.class) {
                if (mapMeaureUtil == null) {
                    mapMeaureUtil = new MapMeaureUtil();
                }
            }
        }
        return mapMeaureUtil;
    }

    public void init(AMap amap, CalculationAreaListener listener) {
        this.aMap = amap;
        calculationAreaListener = listener;
    }

    private CalculationAreaListener calculationAreaListener;


    //添加点
    public void addMarker(LatLng latLng) {
//        addProductORCMapView(latLng);
        calculationArea(latLng);
    }

    //清除点
    public void clearMarker() {

        for (Marker m : labelMarkers) {
            if (m != null) {
                m.destroy();
            }

        }
        if (polyline != null) {
            polyline.remove();
        }
        for (Polyline p : labelPolylines) {
            if (p != null) {
                p.remove();
            }
        }
        labelMarkers.clear();
        labelPolylines.clear();
        for (int i = 0; i < mDistanceText.size(); i++) {
            if (mDistanceText.get(i) != null) {
                mDistanceText.get(i).remove();
            }
        }
        mDistanceText.clear();
        mLabelPolyline = null;
        polyline = null;
        mLabelMarker = null;
        marker_size = 0;
//        changdu = " 长度:0" + "千米";
        mianji = " 面积:0" + "平方千米" + "(" + 0 + " 亩)";
        zhouchang = "周长:0" + "千米";
        updateShowData();
    }

    private void updateShowData() {
        if (calculationAreaListener != null) {
            calculationAreaListener.updateData(zhouchang + mianji);
        }
    }

    //撤销点
    public void revokeMarker() {
        if (labelMarkers.isEmpty()) {
            marker_size = labelMarkers.size();
//            changdu = " 长度:0" + "千米";
            mianji = " 面积:0" + "平方千米" + "(" + 0 + " 亩)";
            zhouchang = "周长:0" + "千米";
            updateShowData();
            return;
        }
        // 删除最后一个点
        deleteEndPoint();
        // 删除最后一条线
        deleteEndLine();
        addLengthtarget(labelMarkers);
        // 计算面积
        double s = workOutArea(labelMarkers);
        double gul = formatDouble1(s * 1000000 / 666.67);

        Log.e("MapMeaureUtil", "计算面积：" + s + "(" + gul + " 亩)");

        // 计算边长
        double length = workOutLength();
        Log.d("MapMeaureUtil", "计算边长：" + length);
        // 计算周长，即 边长+第一点和最后一点的距离
        marker_size = labelMarkers.size();
        double length2 = workOutLength1(length);
//        changdu = String.format(" 长度:%.3f", length) + "千米";
        mianji = String.format(" 面积:%.6f", s) + "平方千米" + "(" + gul + " 亩)";
        zhouchang = String.format("周长:%.3f", length2) + "千米";

        updateShowData();
    }

    //计算面积
    public void calculationArea(LatLng latLng) {
        try {
            // 记录中心点位置，并在地图上显示
            showPointInMap(latLng);
            // 2中心点划线
            drawMarkerLine();
            // 计算面积
            double s = workOutArea(labelMarkers);
            double gul = formatDouble1(s * 1000000 / 666.67);

            Log.e("MapMeaureUtil", "计算面积：" + s + " （" + gul + " 亩）");

            // 计算边长
            double length = workOutLength();
            Log.d("MapMeaureUtil", "计算边长：" + length);
            // 计算周长，即 边长+第一点和最后一点的距离
            double length2 = workOutLength1(length);
            marker_size = labelMarkers.size();
//            changdu = String.format(" 长度:%.3f", length) + "千米";
            mianji = String.format(" 面积:%.6f", s) + "平方千米" + "(" + gul + " 亩)";
            zhouchang = String.format("周长:%.3f", length2) + "千米";
            updateShowData();
        } catch (Exception e) {
        }
    }

    public double workOutArea(List<Marker> centMarkers) {
        if (labelMarkers.size() < 3) {
            return 0;
        }
        int j = 0;
        double s = 0;

        for (int i = 0; i < centMarkers.size(); i++) {
            j = i + 2;
            double a = getDistance(
                    centMarkers.get(0).getPosition().latitude,
                    centMarkers.get(0).getPosition().longitude, centMarkers
                            .get(i + 1).getPosition().latitude, centMarkers
                            .get(i + 1).getPosition().longitude) / 1000;
            double b = getDistance(centMarkers.get(i + 1)
                    .getPosition().latitude, centMarkers.get(i + 1)
                    .getPosition().longitude, centMarkers.get(i + 2)
                    .getPosition().latitude, centMarkers.get(i + 2)
                    .getPosition().longitude) / 1000;
            double c = getDistance(
                    centMarkers.get(0).getPosition().latitude,
                    centMarkers.get(0).getPosition().longitude, centMarkers
                            .get(i + 2).getPosition().latitude, centMarkers
                            .get(i + 2).getPosition().longitude) / 1000;

            double p = (a + b + c) / 2;

            s += Math.sqrt(p * (p - a) * (p - b) * (p - c));
            if (j >= centMarkers.size() - 1) {
                break;
            }
        }

        // 画出封闭线
        if (polyline != null) {
            polyline.remove();
        }
        Marker marker0 = centMarkers.get(0);// 第一个中心点
        Marker marker1 = centMarkers.get(centMarkers.size() - 1);// 倒数第一个中心点

        PolylineOptions opts = new PolylineOptions();

        List<LatLng> points = new ArrayList<LatLng>();
        points.add(marker0.getPosition());
        points.add(marker1.getPosition());

        opts.addAll(points);
        opts.width(5);
        opts.color(0xAAFF0000);
        polyline = aMap.addPolyline(opts);
        area = s;
        return area;
    }

    /**
     * 计算周长
     *
     * @param length 边长
     * @return
     */
    private double workOutLength1(double length) {
        if (labelMarkers.size() < 3) {
            return length;
        }
        double a = getDistance(
                labelMarkers.get(0).getPosition().latitude,
                labelMarkers.get(0).getPosition().longitude,
                labelMarkers.get(labelMarkers.size() - 1).getPosition().latitude,
                labelMarkers.get(labelMarkers.size() - 1).getPosition().longitude) / 1000;
        roundLength = length + a;
        return roundLength;
    }

    /**
     * 计算边长
     *
     * @return
     */
    private double workOutLength() {
        if (labelMarkers.size() < 2) {
            return 0;
        }
        double sum = 0;
        int j = 0;
        for (int i = 0; i < labelMarkers.size(); i++) {
            double a = getDistance(labelMarkers.get(i).getPosition().latitude,
                    labelMarkers.get(i).getPosition().longitude, labelMarkers
                            .get(i + 1).getPosition().latitude, labelMarkers
                            .get(i + 1).getPosition().longitude) / 1000;
            sum += a;
            j = i + 1;
            if (j >= labelMarkers.size() - 1) {
                break;
            }
        }
        sideLength = sum;
        return sideLength;
    }

    /**
     * 在地图上显示中心点的位置
     */
    private void showPointInMap(LatLng latlng) {
        if (latlng == null) {
            // 测试代码
            return;
        }
        if (latlng == null) {
            Toast.makeText(MApplication.getInstance().getApplicationContext(), "标注失败，未获取到中心点坐标！", Toast.LENGTH_SHORT).show();
            return;
        }

        mLabelMarker = aMap.addMarker(new MarkerOptions().position(new LatLng(latlng.latitude, latlng.longitude)).icon(
                BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(MApplication.getInstance().getResources(), R.drawable.ic_credpoint))));
        mLabelMarker.setAnchor(0.5f, 0.5f);
        labelMarkers.add(mLabelMarker);
        addLengthtarget(labelMarkers);
        marker_size = labelMarkers.size();
    }

    //计算边长
    public void addLengthtarget(List<Marker> listMarkers) {
        for (int i = 0; i < mDistanceText.size(); i++) {
            if (mDistanceText.get(i) != null) {
                mDistanceText.get(i).remove();
            }
        }
        mDistanceText.clear();
        if (listMarkers.size() < 2) {
            return;
        }
        for (int i = 0; i < listMarkers.size(); i++) {
            LatLng latLng1;
            LatLng latLng2;
            if (i == listMarkers.size() - 1) {
                latLng1 = listMarkers.get(i).getPosition();
                latLng2 = listMarkers.get(0).getPosition();
            } else {
                latLng1 = listMarkers.get(i).getPosition();
                latLng2 = listMarkers.get(i + 1).getPosition();
            }
            float distance = (float) (Math.round(AMapUtils.calculateLineDistance(latLng1, latLng2) * 10)) / 10;
            LatLng mediaLatlng = getMidLatLng(latLng1, latLng2);
            if (!mDistanceText.contains(mediaLatlng)) {
                mDistanceText.add(aMap.addText(new TextOptions().position(mediaLatlng).fontColor(fenColor).backgroundColor(backColor).text(distance + "米")));
            }
        }
    }

    /**
     * 连接标定点
     */
    private void drawMarkerLine() {
        if (labelMarkers.size() < 2) {
            return;
        }

        Marker marker0 = labelMarkers.get(labelMarkers.size() - 2);// 倒数第二个中心点
        Marker marker1 = labelMarkers.get(labelMarkers.size() - 1);// 倒数第一个中心点

        PolylineOptions opts = new PolylineOptions();

        List<LatLng> points = new ArrayList<LatLng>();
        points.add(marker0.getPosition());
        points.add(marker1.getPosition());

        opts.addAll(points);
        opts.width(5);
        opts.color(0xAAFF0000);

        mLabelPolyline = aMap.addPolyline(opts);
        labelPolylines.add(mLabelPolyline);
    }

    /**
     * 求两个经纬度的中点
     *
     * @param l1
     * @param l2
     * @return
     */
    public LatLng getMidLatLng(LatLng l1, LatLng l2) {
        return new LatLng((l1.latitude + l2.latitude) / 2, (l1.longitude + l2.longitude) / 2);
    }

    public double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS * 1000;
        //s = Math.round(s * 10000) / 10000;
        return s;
    }

    private static double EARTH_RADIUS = 6378.137;//地球半径

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 保留两位小数，四舍五入的一个老土的方法
     *
     * @param d
     * @return
     */
    public static double formatDouble1(double d) {
        return (double) Math.round(d * 100) / 100;
    }

    public interface CalculationAreaListener {
        void updateData(String s);

    }


    /**
     * 删除最后一个标记点
     */
    private void deleteEndPoint() {

        if (!labelMarkers.isEmpty()) {
            labelMarkers.get(labelMarkers.size() - 1).destroy();
            labelMarkers.remove(labelMarkers.size() - 1);
        }
    }

    /**
     * 删除最后一条连接线
     */
    private void deleteEndLine() {
        if (polyline != null) {
            polyline.remove();
        }
        if (mLabelPolyline != null) {
            mLabelPolyline.remove();
        }
        if (!labelPolylines.isEmpty()) {
            Polyline polyline = labelPolylines.get(labelPolylines.size() - 1);
            if (polyline != null) {
                polyline.remove();
            }
            labelPolylines.remove(labelPolylines.size() - 1);
        }
    }

    /**
     * 区域点
     *
     * @param latLng 点
     */
    public void addProductORCMapView(LatLng latLng) {
        if (mPolygon != null) {
            if (mPolygon.contains(latLng)) {
                //在区域内
                List<Double> list = new ArrayList<>();
                for (int i = 0; i < polygons.size(); i++) {
                    LatLng latLng1;
                    LatLng latLng2;
                    if (i == polygons.size() - 1) {
                        latLng1 = polygons.get(i);
                        latLng2 = polygons.get(0);
                    } else {
                        latLng1 = polygons.get(i);
                        latLng2 = polygons.get(i + 1);
                    }
                    list.add(RouteUtils2.PointToSegDist(latLng.longitude, latLng.latitude, latLng1.longitude, latLng1.latitude, latLng2.longitude, latLng2.latitude));
                }
                double x = Collections.min(list);
                int index = list.indexOf(x);
                polygons.add(index + 1, latLng);
                lasIndexId = index + 1;
                mIndexList.add(lasIndexId);
            } else {
                LatLng centerLatlng = mBoundsEWSNLatLng.get(0);
                for (int i = 0; i < polygons.size(); i++) {
                    LatLng latLng1;
                    LatLng latLng2;
                    if (i == polygons.size() - 1) {
                        latLng1 = polygons.get(i);
                        latLng2 = polygons.get(0);
                        if (RouteUtils2.doIntersect(RouteUtils2.latlng2px(aMap, centerLatlng), RouteUtils2.latlng2px(aMap, latLng), RouteUtils2.latlng2px(aMap, latLng1), RouteUtils2.latlng2px(aMap, latLng2))) {
                            lasIndexId = i + 1;
                            mIndexList.add(lasIndexId);
                            polygons.add(i + 1, latLng);
                            break;
                        } else {
                            LatLng latLng3 = null;
                            LatLng latLng4 = null;
                            List<LatLng> latLngs = new ArrayList<>();
                            for (int j = 0; j < polygons.size(); j++) {
                                if (j == polygons.size() - 1) {
                                    latLng3 = polygons.get(j);
                                    latLng4 = polygons.get(0);
                                } else {
                                    latLng3 = polygons.get(j);
                                    latLng4 = polygons.get(j + 1);
                                }
                                latLngs.add(RouteUtils2.getMidLatLng(latLng3, latLng4));
                            }
                            List<Float> integerList = new ArrayList<>();
                            for (int j = 0; j < latLngs.size(); j++) {
                                integerList.add(AMapUtils.calculateLineDistance(latLngs.get(j), centerLatlng));
                            }
                            float maxd = Collections.min(integerList);
                            int j = integerList.indexOf(maxd);
                            lasIndexId = j + 1;
                            mIndexList.add(lasIndexId);
                            polygons.add(j + 1, latLng);

                        }
                    } else {
                        latLng1 = polygons.get(i);
                        latLng2 = polygons.get(i + 1);
                        boolean iscross = RouteUtils2.doIntersect(RouteUtils2.latlng2px(aMap, centerLatlng), RouteUtils2.latlng2px(aMap, latLng), RouteUtils2.latlng2px(aMap, latLng1), RouteUtils2.latlng2px(aMap, latLng2));
                        if (iscross) {
                            polygons.add(lasIndexId, latLng);
                            lasIndexId = i + 1;
                            mIndexList.add(lasIndexId);
                            break;
                        }
                    }

                }
            }
        } else {
            polygons.add(latLng);
            lasIndexId = polygons.size() - 1;
            mIndexList.add(lasIndexId);
        }
        mBoundsEWSNLatLng = RouteUtils2.createPolygonBounds(polygons);
        if (mPolygon != null) {
            mPolygon.remove();
        }
        mPolygon = drawPolygonOptions(polygons);
    }

    /**
     * 边框
     *
     * @param linelatLngs
     * @return
     */
    private Polygon drawPolygonOptions(List<LatLng> linelatLngs) {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(linelatLngs);
        polygonOptions.strokeWidth(1) // 多边形的边框
                .strokeColor(Color.argb(0, 0, 0, 0)) // 边框颜色
                .fillColor(Color.argb(0, 0, 0, 0));   // 多边形的填充色
        return aMap.addPolygon(polygonOptions);
    }
}
