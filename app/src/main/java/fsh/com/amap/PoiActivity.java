package fsh.com.amap;

/**
 * Created by fsh on 2017/7/28.
 */

import android.app.Activity;
import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;

import java.util.ArrayList;
import java.util.List;

import static com.amap.api.mapcore.util.cz.w;

public class PoiActivity extends Activity implements
        PoiSearch.OnPoiSearchListener, AMap.OnMarkerClickListener {
    MapView mapView;
    AMap aMap;
    Marker lastCheckedMarker;
    ArrayList<BitmapDescriptor> lastCheckedBitmapDescriptorList;

    @Override
    public boolean onMarkerClick(Marker marker) {
        lastCheckedMarker.setIcons(lastCheckedBitmapDescriptorList);
        lastCheckedBitmapDescriptorList = marker.getIcons();
        lastCheckedMarker = marker;

        ArrayList<BitmapDescriptor> bitmapDescriptorArrayList = new ArrayList<>();
        bitmapDescriptorArrayList.add(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_pressed));
        marker.setIcons(bitmapDescriptorArrayList);
        return false;
    }

    Query query = null;
    PoiResult poiResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapView = new MapView(this);
        setContentView(mapView);

        mapView.onCreate(savedInstanceState);

        aMap = mapView.getMap();
        aMap.setOnMarkerClickListener(this);
        //poi search
        int currentPage = 0;
        LatLng latLng = new LatLng(36.672262, 117.136);
// 第一个参数表示搜索字符串，第二个参数表示POI搜索类型二选其一
// 第三个参数表示POI搜索区域的编码，必设
        query = new Query("", "建筑物1", "地点1");

        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        PoiSearch poiSearch = new PoiSearch(this, query);
        //boundary search
//设置搜索的范围
        poiSearch.setBound(new SearchBound(
                new LatLonPoint(latLng.latitude, latLng.longitude), 20000));

        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();

    }

    @Override
    public void onPoiItemSearched(PoiItem arg0, int arg1) {

    }

    public void onPoiSearched(PoiResult result, int rCode) {
        // 搜索POI的结果
        if (result != null && result.getQuery() != null) {
            // 是否是同一条
            if (result.getQuery().equals(query)) {
                poiResult = result;
                // 取得搜索到的poi items有多少页
                int resultPages = poiResult.getPageCount();
                // 取得第一页的poiitem数据，页数从数字0开始
                List<PoiItem> poiItems = poiResult.getPois();
                if (poiItems != null && poiItems.size() > 0) {
                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    for (int j = 0; j < Math.min(poiItems.size(), 10); j++) {
                        //1.通过图片名称字符串，获取对应的resId.
                        String iconName = "poi_marker_" + (j + 1);
                        int iconId = getResources().getIdentifier(iconName, "drawable", this.getPackageName());
                        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(iconId));
                        markerOptions.position(new LatLng(poiItems.get(j).getLatLonPoint().getLatitude(), poiItems.get(j)
                                .getLatLonPoint().getLongitude()));
                        Marker marker = aMap.addMarker(markerOptions);
                        //为了POI填充整个地图区域
                        boundsBuilder.include(new LatLng(poiItems.get(j).getLatLonPoint().getLatitude(), poiItems.get(j)
                                .getLatLonPoint().getLongitude()));
                        if (j == 0) {
                            lastCheckedMarker = marker;
                            lastCheckedBitmapDescriptorList = lastCheckedMarker.getIcons();
                            ArrayList<BitmapDescriptor> bitmapDescriptorArrayList = new ArrayList<>();
                            bitmapDescriptorArrayList.add(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_pressed));
                            marker.setIcons(bitmapDescriptorArrayList);
                        }
                    }
                    LatLngBounds bounds = boundsBuilder.build();

                    // 移动地图，所有marker自适应显示。LatLngBounds与地图边缘10像素的填充区域
                    aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));

                } else {

                    // 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();
                }
            }
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        mapView.onDestroy();
    }
}