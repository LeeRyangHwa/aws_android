package com.example.android_resapi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android_resapi.R;


public class MainActivity extends AppCompatActivity {
    final static String TAG = "AndroidAPITest";
    String urlstr;
    EditText editUrl, editlogUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //기본 url을 설정하여 따로 url을 작성하지 않고 디바이스 목록을 조회할 수 있게 한다.
        urlstr = "https://bs7p0t1nzb.execute-api.ap-northeast-2.amazonaws.com/prod/devices";
        Button listThingsBtn = findViewById(R.id.listThingsBtn);
        listThingsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //저장된 urlstr을 ListThingsActivity.class에 넘겨주며 listThingsActivity로 넘어간다.
                Log.i(TAG, "listThingsURL=" + urlstr);
                Intent intent = new Intent(MainActivity.this, ListThingsActivity.class);
                intent.putExtra("listThingsURL", urlstr);
                startActivity(intent);
            }
        });

        //디바이스 목록 조회용 url을 변경하기 위한 edittext
        editUrl = findViewById(R.id.editurl);
        Button changeurl = findViewById(R.id.changeUrl);
        changeurl.setOnClickListener(new View.OnClickListener(){
            @Override
            //버튼이 클릭되면 editUrl에 적혀있던 url이 복사되어 urlstr에 저장된다.
            public void onClick(View view) {
                urlstr = editUrl.getText().toString();
                if (urlstr == null || urlstr.equals("")) {
                    Toast.makeText(MainActivity.this, "사물목록 조회 API URI 입력이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //디바이스 로그 조회용, 현재 위에서 연결한 디바이스의 로그가 아닌 다른 디바이스의 로그를 보고싶다면 아래 기능으로 로그 조회.
        editlogUrl = findViewById(R.id.editlogurl);
        Button changelogurl = findViewById(R.id.changeLogUrl);
        changelogurl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //조회할 로그를 urlstr2로 저장하여 이를 LogActivity.class로 넘겨 해당 디바이스의 로그를 조회한다.
                String urlstr2 = editlogUrl.getText().toString();
                if (urlstr2 == null || urlstr2.equals("")) {
                    Toast.makeText(MainActivity.this, "사물로그 조회 API URI 입력이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                intent.putExtra("getLogsURL", editlogUrl.getText().toString());
                startActivity(intent);
            }
        });
    }
}


