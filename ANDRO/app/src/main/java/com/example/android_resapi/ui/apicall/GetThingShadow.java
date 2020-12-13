package com.example.android_resapi.ui.apicall;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.example.android_resapi.R;
import com.example.android_resapi.httpconnection.GetRequest;

public class GetThingShadow extends GetRequest {
    final static String TAG = "AndroidAPITest";
    String urlStr;
    public GetThingShadow(Activity activity, String urlStr) {
        super(activity);
        this.urlStr = urlStr;
    }

    @Override
    protected void onPreExecute() {
        try {
            Log.e(TAG, urlStr);
            url = new URL(urlStr);

        } catch (MalformedURLException e) {
            Toast.makeText(activity,"URL is invalid:"+urlStr, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            activity.finish();
        }
    }

    //DeviceActivity에서 표현할 값들을 설정, 이들을 set으로 만들어 값을 연결해주는 역할을 한다.
    //desire하여 ON OFF 제어를 할 객체는 AUTO와 MOTOR 뿐이기에 이 둘만 desired도 만들어준다.
    @Override
    protected void onPostExecute(String jsonString) {
        if (jsonString == null)
            return;
        Map<String, String> state = getStateFromJSONString(jsonString);
        TextView reported_dustdensity = activity.findViewById(R.id.reported_dustdensity);
        TextView reported_duststate = activity.findViewById(R.id.reported_duststate);
        TextView reported_auto = activity.findViewById(R.id.reported_auto);
        TextView reported_motor = activity.findViewById(R.id.reported_motor);
        reported_dustdensity.setText(state.get("reported_dustdensity"));
        reported_duststate.setText(state.get("reported_duststate"));
        reported_auto.setText(state.get("reported_auto"));
        reported_motor.setText(state.get("reported_motor"));

        TextView desired_auto = activity.findViewById(R.id.desired_auto);
        TextView desired_motor = activity.findViewById(R.id.desired_motor);
        desired_motor.setText(state.get("desired_motor"));
        desired_auto.setText(state.get("desired_auto"));
    }

    protected Map<String, String> getStateFromJSONString(String jsonString) {
        Map<String, String> output = new HashMap<>();
        try {
            // 처음 double-quote와 마지막 double-quote 제거
            jsonString = jsonString.substring(1,jsonString.length()-1);
            // \\\" 를 \"로 치환
            jsonString = jsonString.replace("\\\"","\"");
            Log.i(TAG, "jsonString="+jsonString);
            JSONObject root = new JSONObject(jsonString);
            JSONObject state = root.getJSONObject("state");
            JSONObject reported = state.getJSONObject("reported");
            String densityValue = reported.getString("Dustdensity");
            String stateValue = reported.getString("State");
            String autoValue = reported.getString("Auto");
            String motorValue = reported.getString("Motor");
            output.put("reported_dustdensity", densityValue);
            output.put("reported_duststate", stateValue);
            output.put("reported_auto", autoValue);
            output.put("reported_motor",motorValue);
            //JSON 문자열에서 각 Dustdensity, State, Auto, Motor 상태를 받아 이들을 저장하여 출력한다.

            JSONObject desired = state.getJSONObject("desired");
            String desired_autoValue = desired.getString("Auto");
            String desired_motorValue = desired.getString("Motor");
            output.put("desired_auto", desired_autoValue);
            output.put("desired_motor",desired_motorValue);
        } catch (JSONException e) {
            Log.e(TAG, "Exception in processing JSONString.", e);
            e.printStackTrace();
        }
        return output;
    }
}
