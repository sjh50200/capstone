package com.example.parking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class StartActivity extends AppCompatActivity {

    LinearLayout li_signin;
    LinearLayout li_signup;
    ImageView image_movingcar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        image_movingcar = findViewById(R.id.image_movingcar);
        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(image_movingcar);
        Glide.with(this).load(R.drawable.movingcar).into(gifImage);
    }

    @Override
    public void onResume(){
        super.onResume();

        li_signin = findViewById(R.id.li_signin);
        li_signin.setClickable(true);
        li_signin.setOnClickListener(new View.OnClickListener() { //sign in 버튼 클릭 시 이벤트 처리
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        
    }

}