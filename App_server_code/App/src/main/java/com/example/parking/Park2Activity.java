package com.example.parking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Park2Activity extends AppCompatActivity {

    private View toolbar;
    private ActionBar actionBar;
    Button btn_fee;
    TextView text_fee;

    private int left;
    private int top=50;
    private int right;
    private int bottom = 557;

    private Integer parkedSeat =null;
    private String carNumber; //차량 번호 LoingActivity에서 받은 값 저장
    private String seatNumber; //자리 번호 LoginActivity에서 받은 값 저장

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 툴바 뒤로가기 버튼
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.menu_search:
                Snackbar.make(toolbar, "Search menu", Snackbar.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_park2);

        //툴바 설정
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar((Toolbar) toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false); //기본 제목을 없애줍니다.
        actionBar.setDisplayHomeAsUpEnabled(true); //자동 뒤로가기 버튼 만들어준다. true만 해주어도 자동으로 뒤로가기 버튼이 생김

        LinearLayout Li2 = (LinearLayout) findViewById(R.id.Li2);
        Park2Activity.DrawingView ov = new Park2Activity.DrawingView(this);
        Li2.addView(ov);

        seatNumber = ((LoginActivity)LoginActivity.context_login).seatNum;
        carNumber = ((LoginActivity)LoginActivity.context_login).carNum;

        text_fee = findViewById(R.id.text_fee);
        btn_fee = findViewById(R.id.btn_fee);

        if(seatNumber.equals("null"))
            btn_fee.setVisibility(View.INVISIBLE);

            btn_fee.setOnClickListener(new View.OnClickListener() { //주차 되있을 경우 요금조회
                @Override
                public void onClick(View view) {
                    getData("http://223.131.2.220:1818/android/fee/" + carNumber);
                }
            });
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
            protected void onPostExecute(String result) {
                text_fee.setText("차량번호: "+carNumber+"\n주차요금: "+ result+"원");
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

    public class DrawingView extends View {

        public DrawingView(Context context) {
            super(context);
        }

        public DrawingView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public DrawingView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            Paint paint2 = new Paint();
            paint2.setColor(Color.GREEN);
            paint2.setTextSize(22);
            paint2.setAntiAlias(true);

            Paint paint3 = new Paint();
            paint3.setTextSize(22);
            paint3.setColor(Color.RED);

            Paint paint4 = new Paint();
            paint4.setColor(Color.BLACK);
            paint4.setTextSize(50);

            left = 487;
            right = 710;
            try {
                parkedSeat = Integer.parseInt(seatNumber);
            }catch (NumberFormatException e) {
            }catch (Exception e){
            }

            if (parkedSeat == null) {
                int parkedIndex = 0, reservedIndex = 0;
                for (int parkinglotNum = 0; parkinglotNum < 4; parkinglotNum++) {
                    if (parkedIndex < ((Map2Activity) Map2Activity.context_main).parked.size()) {
                        if (Integer.parseInt(((Map2Activity) Map2Activity.context_main).parked.get(parkedIndex)) == parkinglotNum + 1) {
                            canvas.drawRect(left, top, right, bottom, paint3); //1번칸
                            //System.out.println("**************빨간색 칠하기");
                            parkedIndex++;
                        }
                    }
                    if (reservedIndex < ((Map2Activity) Map2Activity.context_main).reserved.size()) {
                        if (Integer.parseInt(((Map2Activity) Map2Activity.context_main).reserved.get(reservedIndex)) == parkinglotNum + 1) {
                            //System.out.println("주황색 칠하기");
                            reservedIndex++;
                        }
                    }
                    left = left + 229;
                    right = right + 229;
                }
            }

            else {
                canvas.drawRect(left + (229*(parkedSeat-1)), top, right + (229*(parkedSeat-1)), bottom, paint2); //1번칸
            }
            }
        }
    }