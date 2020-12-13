package com.example.android_resapi.ui.apicall;

import android.app.Activity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.example.android_resapi.R;
import com.example.android_resapi.httpconnection.GetRequest;


public class GetLog extends GetRequest {
    final static String TAG = "AndroidAPITest";
    String urlStr;
    static String Dstate;
    public GetLog(Activity activity, String urlStr) {
        super(activity);
        this.urlStr = urlStr;
    }

    @Override
    protected void onPreExecute() {
        try {

            TextView textView_Date1 = activity.findViewById(R.id.textView_date1);
            TextView textView_Time1 = activity.findViewById(R.id.textView_time1);
            TextView textView_Date2 = activity.findViewById(R.id.textView_date2);
            TextView textView_Time2 = activity.findViewById(R.id.textView_time2);

            String params = String.format("?from=%s:00&to=%s:00",textView_Date1.getText().toString()+textView_Time1.getText().toString(),
                                                            textView_Date2.getText().toString()+textView_Time2.getText().toString());

            Log.i(TAG,"urlStr="+urlStr+params);
            url = new URL(urlStr+params);

        } catch (MalformedURLException e) {
            Toast.makeText(activity,"URL is invalid:"+urlStr, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        TextView message = activity.findViewById(R.id.message2);
        message.setText("조회중...");
    }

    @Override
    protected void onPostExecute(String jsonString) {
        TextView message = activity.findViewById(R.id.message2);
        if (jsonString == null) {
            message.setText("로그 없음");
            return;
        }
        message.setText("");
        //아래에서 Tag에 저장된 값들을 리스트로 표현.
        ArrayList<Tag> arrayList = getArrayListFromJSONString(jsonString);

        final ArrayAdapter adapter = new ArrayAdapter(activity,
                android.R.layout.simple_list_item_1,
                arrayList.toArray());
        ListView txtList = activity.findViewById(R.id.logList);
        txtList.setAdapter(adapter);
        txtList.setDividerHeight(10);
    }

    protected ArrayList<Tag> getArrayListFromJSONString(String jsonString) {
        ArrayList<Tag> output = new ArrayList();
        try {
            // 처음 double-quote와 마지막 double-quote 제거
            jsonString = jsonString.substring(1,jsonString.length()-1);
            // \\\" 를 \"로 치환
            jsonString = jsonString.replace("\\\"","\"");

            Log.i(TAG, "jsonString="+jsonString);

            JSONObject root = new JSONObject(jsonString);
            JSONArray jsonArray = root.getJSONArray("data");

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = (JSONObject)jsonArray.get(i);

                Tag thing = new Tag(jsonObject.getString("deviceId"),
                                    jsonObject.getString("Dustdensity"),
                                    jsonObject.getString("State"),
                                    jsonObject.getString("Auto"),
                                    jsonObject.getString("Motor"),
                                    jsonObject.getString("timestamp")
                        );
                output.add(thing);
                //각 deviceId, dustdensity, state 등의 표에 존재하는 값들을 받는다.
            }

        } catch (JSONException e) {
            //Log.e(TAG, "Exception in processing JSONString.", e);
            e.printStackTrace();
        }
        return output;
    }

    //DB에 저장된 deviceId, dustdensity 등의 값을 받기 위해 Tag에 선언.
    class Tag {
        String deviceId;
        String dustdensity;
        String duststate;
        String auto;
        String motor;
        String timestamp;

        public Tag(String DeviceId, String Ddensity, String Dstate, String Dauto, String Dmotor, String time) {
            deviceId = DeviceId;
            dustdensity = Ddensity;
            duststate = Dstate;
            auto = Dauto;
            motor = Dmotor;
            timestamp = time;
        }

        public String toString() {
            return String.format("[%s] DeviceId: %s, Dustdensity: %s, Duststate: %s, Auto: %s, Motor: %s", timestamp, deviceId, dustdensity, duststate, auto, motor);
        }
    }
}

