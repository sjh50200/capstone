package com.example.parking;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private View toolbar;
    private ActionBar actionBar;
    //private EditText search;
    //private List<String> list; // 데이터를 넣은 리스트변수
    private List<searchItem> searchList; //검색어 데이터를 넣을 리스트

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 툴바 뒤로가기 버튼
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
        setContentView(R.layout.activity_search);
        //툴바 설정
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar((Toolbar) toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false); //기본 제목을 없애줍니다.
        actionBar.setDisplayHomeAsUpEnabled(true); //자동 뒤로가기 버튼 만들어준다. true만 해주어도 자동으로 뒤로가기 버튼이 생김

        final Button btn_search = findViewById(R.id.btn_search); //버튼 객체 선언 후 View의 id 를 R클래스에서 받아온다.
        final AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        searchList();
        AutoCompleteTextView AT = findViewById(R.id.autoCompleteTextView);
        //커스텀 어댑터를 생성
        AutoCompleteAdapter adapter = new AutoCompleteAdapter(this, searchList);
        //생성한 어댑터를 AT에 세팅
        AT.setAdapter(adapter);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, Map2Activity.class);
                intent.putExtra("search", autoCompleteTextView.getText().toString());// 이 메서드를 통해 데이터를 전달합니다.
                startActivity(intent);
            }
        });
    }

    //검색어 데이터 추가
    private void searchList() {
        searchList = new ArrayList<>();
        searchList.add(new searchItem("한성대입구역", R.drawable.ic_search_white));
        searchList.add(new searchItem("한성대학교", R.drawable.ic_search_white));
        searchList.add(new searchItem("발산역", R.drawable.ic_search_white));
        searchList.add(new searchItem("마곡역", R.drawable.ic_search_white));
    }

    public class searchItem {
        private String name;
        private int image;

        public searchItem(String name, int image) {
            this.name = name;
            this.image = image;
        }

        public String getname() {
            return name;
        }

        public int getimage() {
            return image;
        }
    }
}
