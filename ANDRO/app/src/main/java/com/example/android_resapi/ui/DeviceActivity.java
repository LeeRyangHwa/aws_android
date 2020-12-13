package com.example.android_resapi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android_resapi.R;
import com.example.android_resapi.ui.apicall.GetThingShadow;
import com.example.android_resapi.ui.apicall.UpdateShadow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceActivity extends AppCompatActivity {
    String urlStr, urlstr;
    final static String TAG = "AndroidAPITest";
    Timer timer;
    Button startGetBtn;
    Button stopGetBtn;
    Button Logbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Intent intent = getIntent();
        urlStr = intent.getStringExtra("thingShadowURL");
        urlstr = "https://bs7p0t1nzb.execute-api.ap-northeast-2.amazonaws.com/prod/devices/DustDB/log";
        //연결된 디바이스의 로그조회용 url을 미리 설정하여 따로 기입할 필요 없게 구현. 다른 로그를 보고싶다면 MainActivity에서 기입하여 보면 된다.

        startGetBtn = findViewById(R.id.startGetBtn);
        startGetBtn.setEnabled(true);
        startGetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        new GetThingShadow(DeviceActivity.this, urlStr).execute();
                    }
                },
                        0,2000);

                startGetBtn.setEnabled(false);
                stopGetBtn.setEnabled(true);
            }
        });

        stopGetBtn = findViewById(R.id.stopGetBtn);
        stopGetBtn.setEnabled(false);
        stopGetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer != null)
                    timer.cancel();
                clearTextView();
                startGetBtn.setEnabled(true);
                stopGetBtn.setEnabled(false);
            }
        });

        //AUTO와 MOTOR의 상태를 변경하기 위해 존재.
        Button updateBtn = findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edit_auto = findViewById(R.id.edit_auto);
                EditText edit_motor = findViewById(R.id.edit_motor);

                JSONObject payload = new JSONObject();
                try {
                    JSONArray jsonArray = new JSONArray();
                    String auto_input = edit_auto.getText().toString();
                    if (auto_input != null && !auto_input.equals("")) {
                        JSONObject tag1 = new JSONObject();
                        tag1.put("tagName", "Auto");
                        tag1.put("tagValue", auto_input);
                        jsonArray.put(tag1);
                    }//AUTO 값 변경, ON, OFF를 tag1에 저장.
                    String motor_input = edit_motor.getText().toString();
                    if (motor_input != null && !motor_input.equals("")) {
                        JSONObject tag2 = new JSONObject();
                        tag2.put("tagName", "Motor");
                        tag2.put("tagValue", motor_input);
                        jsonArray.put(tag2);
                    }//MOTOR 값 변경, ON, OFF를 tag2에 저장
                    if (jsonArray.length() > 0)
                        payload.put("tags", jsonArray);
                        //위에서 저장한 tag를 payload에 저장.
                } catch (JSONException e) {
                    Log.e(TAG, "JSONEXception");
                }
                Log.i(TAG,"payload="+payload);
                if (payload.length() >0 )
                    new UpdateShadow(DeviceActivity.this,urlStr).execute(payload);
                    //저장한 payload를 UpdateShadow로 보내어 reported, desired 상태를 변경하여 출력하게 한다.
                else
                    Toast.makeText(DeviceActivity.this,"변경할 상태 정보 입력이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        });

        //로그조회용 버튼, 앞서 연결한 로그조회용 URL를 통해 해당 디바이스의 로그를 조회할 수 있게했다.
        Logbtn = findViewById(R.id.logbutton);
        Logbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeviceActivity.this, LogActivity.class);
                intent.putExtra("getLogsURL", urlstr);
                startActivity(intent);
            }
        });

    }

    private void clearTextView() {
        TextView reported_dustdensity = findViewById(R.id.reported_dustdensity);
        TextView reported_duststate = findViewById(R.id.reported_duststate);
        TextView reported_motor = findViewById(R.id.reported_motor);
        TextView reported_auto = findViewById(R.id.reported_auto);
        reported_dustdensity.setText("");
        reported_duststate.setText("");
        reported_motor.setText("");
        reported_auto.setText("");

        TextView desired_motor = findViewById(R.id.desired_motor);
        TextView desired_auto = findViewById(R.id.desired_auto);
        desired_auto.setText("");
        desired_motor.setText("");
    }

}


