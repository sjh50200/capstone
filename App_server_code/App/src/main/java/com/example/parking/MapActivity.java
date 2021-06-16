package com.example.parking;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener { //인터페이스 구현 선언

    private static final int REQUEST_CODE_PERMISSIONS = 1000;
    private View toolbar;
    private ActionBar actionBar;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient; //구글 api 클라이언트
    private FusedLocationProviderClient mFusedLocationClient; //현재 위치를 얻고 위치 정보를 알려주는 객체

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //툴바 뒤로가기 버튼
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.menu_search:
                Snackbar.make(toolbar, "Search menu pressed", Snackbar.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_map); //R->res라는 의미
        //툴바 설정
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar((Toolbar) toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false); //기본 제목을 없애줍니다.
        actionBar.setDisplayHomeAsUpEnabled(true); //자동 뒤로가기 버튼 만들어준다. true만 해주어도 자동으로 뒤로가기 버튼이 생김

        final TextView text_search = (TextView) findViewById(R.id.text_search); //지도 입력칸
        text_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //검색창 눌렀을 때 완전 검색창으로 넘기기
                Intent intent = new Intent(MapActivity.this, SearchActivity.class);
                Pair[] pairs = new Pair[1];
                pairs[0] = new Pair<View, String>(text_search, "sign2");
                //액티비티에서 움직일 뷰와 트랜지션이름을 Pair배열에 담아둔다.
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MapActivity.this, pairs);
                //액티비티 옵션을 적용하기 위해 ActivityOptions객체를 만들고 트랜지션 에니메이션에 대한 설정을 넣는다
                startActivity(intent, options.toBundle());
            }
        });
        //지오코더 객체 선언
        final Geocoder geocoder = new Geocoder(this);
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
    //맵이 준비 되었을 때
    @Override
    public void onMapReady(final GoogleMap googleMap) { //OnMapReadyCallback 인터페이스의 onMapReady 메서드
        // 맵이 사용할 준비가 되었을 때(NULL이 아닌 GoogleMap 객체를 파라미터로 제공해 줄 수 있을 때) 호출되어지는 메서드
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerClickListener(this);
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
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() { //마지막 위치 가져오기
            @Override
            public void onSuccess(Location location) { //성공했을때 이곳으로
                if (location != null) {
                    LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());//위도 경도 잡아주기
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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

