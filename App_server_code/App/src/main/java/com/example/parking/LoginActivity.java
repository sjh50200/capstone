package com.example.parking;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    LinearLayout li_login;
    EditText edit_id;
    EditText edit_pw;
    Login login; //DB에 넘겨줄 Id, Password 클래스 담기 위해

    static String strJson = "";
    public static Context context_login;
    protected String seatNum; //차량 자리 번호
    protected String carNum; //차량 번호
    private String[] carAct; //사고 차량 넣는 배열

    @Override
    protected void onCreate(Bundle savedInstanceState) { //각종 초기화 작업
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context_login = this; //다른 액티비티에서 접근을 위해

        edit_id = findViewById(R.id.edit_id); //아이디
        edit_pw = findViewById(R.id.edit_pw); //패스워드
        li_login = findViewById(R.id.li_login); //로그인 버튼
        li_login.setClickable(true);

        TransMqttThread.start();
    }

    public Thread TransMqttThread = new Thread() {
        @Override
        public void run() { //MQTT
            final MqttAndroidClient mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), "tcp://test.mosquitto.org" + ":1883", MqttClient.generateClientId());
            IMqttToken token = null;    //mqtttoken 이라는것을 만들어 connect option을 달아줌
            try {
                token = mqttAndroidClient.connect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우
                    Log.d("Connect_success", "Connect_Success");

                    try {
                        mqttAndroidClient.subscribe("/accident", 0);   //연결에 성공하면 jmlee 라는 토픽으로 subscribe함
                        Log.d("Subscribe_success", "Subscribe_Success");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    mqttAndroidClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            if (topic.equals("/accident")) {     //topic 별로 분기처리하여 작업을 수행할수도있음
                                String msg = new String(message.getPayload()); //메세지 여기로 옴
                                Log.d("Accident message", msg); //사고 차량 메세지를 받는다
                                carAct = msg.split(" ");

                                for (int i = 0; i < carAct.length; i++) {
                                    if (carNum.equals(carAct[i])) {
                                        Notification.Builder mBuilder =
                                                new Notification.Builder(LoginActivity.this)
                                                        .setSmallIcon(R.drawable.loading)
                                                        .setContentTitle("차량 사고 알림")
                                                        .setContentText("차량 번호 "+carNum+" 사고가 발생하였습니다")
                                                        .setDefaults(Notification.DEFAULT_VIBRATE)
                                                        .setAutoCancel(true);
                                        NotificationManager mNotificationManager =
                                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                        mNotificationManager.notify(0, mBuilder.build());
                                    }
                                }
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                        }
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        }
    };

    @Override
    protected void onResume() { //사용자의 상호작용
        super.onResume();

        li_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.li_login:
                        if (!validate())
                            Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                        else {
                            HttpAsyncTask httpTask = new HttpAsyncTask(LoginActivity.this);
                            httpTask.execute("http://223.131.2.220:1818/android/member", edit_id.getText().toString(), edit_pw.getText().toString());
                        }
                        break;
                }
            }
        });
    }

    public String POST(String url, Login login) {
        InputStream is = null;
        String result = "";
        try {
            URL urlCon = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) urlCon.openConnection();

            String json = "";
            JSONObject jsonObject = new JSONObject(); //jsonObject 생성
            jsonObject.accumulate("id", login.getId());
            Log.d("로그인 ID", login.getId());
            jsonObject.accumulate("pw", login.getPw());
            Log.d("로그인 PW", login.getPw());
            // convert JSONObject to JSON to String
            json = jsonObject.toString();
            // Set some headers to inform server about the type of the content
            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setRequestProperty("Content-type", "application/json");
            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            httpCon.setDoOutput(true);
            // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
            httpCon.setDoInput(true);

            OutputStream os = httpCon.getOutputStream();
            os.write(json.getBytes("euc-kr"));
            os.flush();

            // receive response as inputStream
            try {
                is = httpCon.getInputStream();
                // convert inputstream to string
                if (is != null) {
                    result = convertInputStreamToString(is);
                    JSONObject jsonObj = new JSONObject(result);
                    seatNum = jsonObj.getString("seatNum");
                    carNum = jsonObj.getString("carNum");
                } else
                    result = "Did not work!";
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Log.d("HttpStatusCode", String.valueOf(httpCon.getResponseCode())); //http 상태 코드 출력
                if (httpCon.getResponseCode() == 200) {
                    Log.d("seatNum -> 주차장에 주차했을 경우 주차된 자리",seatNum);
                    Log.d("carNum -> 내 차량 번호",carNum);
                    Intent intent = new Intent(LoginActivity.this, Park2Activity.class);
                    LoginActivity.this.startActivity(intent); //로그인 정보 확인되면 다음 화면으로
                } else if (httpCon.getResponseCode() == 302) {
                    Log.d("seatNum -> 주차장에 주차했을 경우 주차된 자리",seatNum);
                    Log.d("carNum -> 내 차량 번호",carNum);
                    Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                    LoginActivity.this.startActivity(intent); //로그인 정보 확인되면 다음 화면으로
                }
                httpCon.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        private LoginActivity loginAct;

        HttpAsyncTask(LoginActivity loginActivity) {
            this.loginAct = loginActivity;
        }

        @Override
        protected String doInBackground(String... urls) { //실제로 통신 할때 작동하는 부분

            login = new Login();
            login.setId(urls[1]);
            login.setPw(urls[2]);

            return POST(urls[0], login); //통신 코드 호출
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            strJson = result;
            loginAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });

        }
    }

    private boolean validate() { //아이디 유효성 검사
        if (edit_id.getText().toString().trim().equals(""))
            return false;
        else if (edit_pw.getText().toString().trim().equals(""))
            return false;
        else
            return true;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}