package com.example.parking;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Map2Activity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener{ //인터페이스 구현 선언

    String myJSON;
    JSONArray park = null;
    JSONArray reserve = null;

    public static Context context_main;
    public List<String> parked = new ArrayList<>(); //
    public List<String> reserved = new ArrayList<>();

    private static final int REQUEST_CODE_PERMISSIONS = 1000;
    private GoogleMap mMap;
    private View toolbar;
    private ActionBar actionBar;
    private  GoogleApiClient mGoogleApiClient; //구글 api 클라이언트
    private FusedLocationProviderClient mFusedLocationClient;//현재 위치를 얻고 위치 정보를 알려주는 객체
    public InputMethodManager imm; //키보드 내려가기 하기
    double flat, flon;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 툴바 뒤로가기 버튼
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.menu_search:
                Snackbar.make(toolbar,"Search menu pressed",Snackbar.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    //Menu 사용
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);

        context_main = this;

        getData("http://223.131.2.220:1818/android/seats"); //php 서버에서 정보 가져오기

                //툴바 설정
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar((Toolbar) toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false); //기본 제목을 없애줍니다.
        actionBar.setDisplayHomeAsUpEnabled(true); //자동 뒤로가기 버튼 만들어준다. true만 해주어도 자동으로 뒤로가기 버튼이 생김
        //키보드 내리기 설정
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        // 구글 api 못받아왔을때
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);//객체 얻기
    }

    protected void showList() { //주차장 정보
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            park = jsonObj.getJSONArray("parked"); //json 형태로 넣어주기
            Log.d("parked", park.getString(0));
            reserve = jsonObj.getJSONArray("reserved");
            Log.d("reserved", reserve.getString(1));
            for(int i=0; i<park.length(); i++){
                parked.add(park.getString(i));
                reserved.add(reserve.getString(i));
                Log.d("park", parked.get(i));
                Log.d("reserve",reserved.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getData(String url) {
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String result) { //마지막으로 값을 넘겨주는 showList에 따라서 화면에 보여지는 값이 결정된다.
                myJSON = result;
                Log.d("park", myJSON);
                showList();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

    //맵이 준비 되었을 때
    @Override
    public void onMapReady(final GoogleMap googleMap) { //OnMapReadyCallback 인터페이스의 onMapReady 메서드
        // 맵이 사용할 준비가 되었을 때(NULL이 아닌 GoogleMap 객체를 파라미터로 제공해 줄 수 있을 때) 호출되어지는 메서드
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerClickListener(this);
        //지오코더 객체 선언
        final Geocoder geocoder = new Geocoder(this);
        //넘긴 값을 받는다.
        Intent intent = getIntent();
        final String receiveStr = intent.getExtras().getString("search"); // 전달한 값을 받을 때
        //권한
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling    //permission에 관한
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true); //구글맵에 현재위치 표시와 현재위치로 가는 버튼 생성 우측 상단 버튼
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {//마지막 위치 가져오기

            @Override
            public void onSuccess(Location location) { //성공했을때 이곳으로
                if (location != null) {
                    List<Address> list = null;
                    try {
                        list = geocoder.getFromLocationName
                                (receiveStr, // 지역 이름
                                        10); // 읽을 개수
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
                    }
                    if (list != null) {
                        if (list.size() == 0) {
                        } else {
                            // 해당되는 주소로 인텐트 날리기
                            Address addr = list.get(0);
                            double lat = addr.getLatitude();
                            double lon = addr.getLongitude();
                            LatLng current = new LatLng(lat, lon);

                            flat = lat;
                            flon = lon;

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions
                                    .position(new LatLng(addr.getLatitude(), addr.getLongitude())) //위도, 경도
                                    .title(receiveStr) //마커 제목 설정
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));// 내가 검색한 위치 아이콘 색 -> 파란색
                            //mMap.addMarker(markerOptions).showInfoWindow(); //검색위치 파란 마커 지도에 등록
                            CameraUpdate center = CameraUpdateFactory.newLatLng(markerOptions.getPosition()); //마커 클릭 시 중앙으로 카메라 가져오기
                            mMap.animateCamera(center);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(current)); //위치로 카메라를 움직이는 코드
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        }
                    }

                    MarkerOptions markerOptions1 = new MarkerOptions(); //마커를 한성대입구역 옆에 주차장 3개 찍기위해서
                    markerOptions1
                            .position(new LatLng(flat-0.003259,flon+0.001113)) //위도, 경도
                            .title("Parking Lot1"); //마커 제목 설정'
                    BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.picon);
                    Bitmap b=bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, 200, 200, false);
                    markerOptions1.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                    mMap.addMarker(markerOptions1).showInfoWindow();

                    MarkerOptions markerOptions2 = new MarkerOptions();
                    markerOptions2
                            .position(new LatLng(flat + 0.00123,flon - 0.00164)) //위도, 경도
                            .title("Parking Lot 2"); //마커 제목 설정
                    markerOptions2.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.pIcon));
                    mMap.addMarker(markerOptions2).showInfoWindow();

                    MarkerOptions markerOptions3 = new MarkerOptions();
                    markerOptions3
                            .position(new LatLng(flat-0.00116, flon+0.004651)) //위도, 경도
                            .title("Parking Lot3");//마커 제목 설정
                    markerOptions3.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                    mMap.addMarker(markerOptions3).showInfoWindow();

                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {  //마커 정보창 클릭 이벤트
                        @Override
                        public void onInfoWindowClick(Marker marker) {// 마커 클릭 시 주차장 1,2,3으로 따로 이동하게 끔
                            //주차장1 마커 클릭 시 주차장 1번으로 이동
                            if (marker.getTitle().equals("Parking Lot1")) {
                                Intent intent = new Intent(getApplicationContext(), Park1Activity.class);
                                startActivity(intent);
                            } else if (marker.getTitle().equals("Parking Lot 2")) {
                                Intent intent = new Intent(getApplicationContext(), Park2Activity.class);
                                startActivity(intent);
                            } else if (marker.getTitle().equals("Parking Lot3")) {
                                Intent intent = new Intent(getApplicationContext(), Park3Activity.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); //콜백 메서드 오버라이드
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                        .PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한 체크 거부 됨", Toast.LENGTH_SHORT).show(); //없을 때 있을 때는 안해놓음
                }
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}