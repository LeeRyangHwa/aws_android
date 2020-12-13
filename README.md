# AWS-arduino-smoke-Box

서비스개요
공공 흡연부스를 효율적으로 관리하도록 만들었습니다. 구현한 기술로 흡연실 내부 공기질을 파악하여 공기질이 나쁠 시(일정 수치 이상) 정화 시스템이 돌아가며 흡연실 내부를 정화하고, 모든 시스템을 공공 흡연실을 관리하는 사람이 앱으로 관리할 수 있도록 만들었습니다. 

결과사진
<div>
<img src="https://user-images.githubusercontent.com/75929596/102011164-ee20be00-3d85-11eb-8121-c36849a8e116.png" width=200></img>         
<img src="https://user-images.githubusercontent.com/75929596/102011203-26280100-3d86-11eb-9120-36da49743843.jpg" width=200></img> 
<img src="https://user-images.githubusercontent.com/75929596/102011412-342a5180-3d87-11eb-99bd-5ac6471dd59a.png" width=200></img>
</div>
준비물 : 아두이노 MKR WiFi 1010 보드,  GP2Y1010AU0F 센서, DC 모터 모듈 L9110

주요기능 : 미세먼지센서가 담배연기로 측정수가 높아지고 특정 값을 넘는 순간 모터모듈이 ON으로 바뀌며 가동된다. 앱으로 데이터들을 관리하고 모듈을 제어한다.

아두이노 코드

![33333333333](https://user-images.githubusercontent.com/75929596/102010862-1d363000-3d84-11eb-9569-9b70fb5dd7e6.jpg)

## java AWS LAMbda 코드작성

### 1.람다자바를 통해 DB에 디바이스에서 전송하는 값을 저장한다.
~~~
    private String persistData(Document document) throws ConditionalCheckFailedException {
        // Epoch Conversion Code: https://www.epochconverter.com/
        SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String timeString = sdf.format(new java.util.Date (document.timestamp*1000));
        
        //DustDensity의 값이 바뀌어야만 저장
        if (document.current.state.reported.Dustdensity.equals(document.previous.state.reported.Dustdensity)) {
                return null;
        }

        return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                .putItem(new PutItemSpec().withItem(new Item().withPrimaryKey("deviceId", document.device)
                        .withLong("time", document.timestamp)
                        .withString("GPS", document.current.state.reported.GPS)
                        .withString("Dustdensity", document.current.state.reported.Dustdensity)
                        .withString("Auto", document.current.state.reported.Auto)
                        .withString("Motor", document.current.state.reported.Motor)
                        .withString("State", document.current.state.reported.State)
                        .withString("timestamp",timeString)))
                .toString();
    }
    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("ap-northeast-2").build();

        this.dynamoDb = new DynamoDB(client);
    }
}

class Document {
    public Thing previous;       
    public Thing current;
    public long timestamp;
    public String device;       // AWS IoT에 등록된 사물 이름 
}

class Thing {
    public State state = new State();
    public long timestamp;
    public String clientToken;

    public class State {
        public Tag reported = new Tag();
        public Tag desired = new Tag();

        public class Tag {
            public String GPS;
            public String Dustdensity;
            public String Auto;
            public String Motor;
            public String State;
        }
    }
}
~~~~
### 2. RESTAPI를 구현하여 앱으로 디바이스를 제어하게 한다


 1. 디바이스 목록 조회
~~~
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.List;
import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClientBuilder;
import com.amazonaws.services.iot.model.ListThingsRequest;
import com.amazonaws.services.iot.model.ListThingsResult;
import com.amazonaws.services.iot.model.ThingAttribute;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class ListingDeviceHandler implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {

        // AWSIot 객체를 얻는다. 
        AWSIot iot = AWSIotClientBuilder.standard().build();

        // ListThingsRequest 객체 설정. 
        ListThingsRequest listThingsRequest = new ListThingsRequest();

        // listThings 메소드 호출하여 결과 얻음. 
        ListThingsResult result = iot.listThings(listThingsRequest);

        // result 객체로부터 API 응답모델 문자열 생성하여 반
        return getResponse(result);
    }

    /**
     * ListThingsResult 객체인 result로 부터 ThingName과 ThingArn을 얻어서 Json문자 형식의
     * 응답모델을 만들어 반환한다.
     * {
     *  "things": [ 
     *       { 
     *          "thingName": "string",
     *          "thingArn": "string"
     *       },
     *       ...
     *     ]
     * }
     */
    private String getResponse(ListThingsResult result) {
        List<ThingAttribute> things = result.getThings();

        String response = "{ \"things\": [";
        for (int i =0; i<things.size(); i++) {
            if (i!=0) 
                response +=",";
            response += String.format("{\"thingName\":\"%s\", \"thingArn\":\"%s\"}", 
                                                things.get(i).getThingName(),
                                                things.get(i).getThingArn());

        }
        response += "]}";
        return response;
    }
    ~~~
    2. 로그 조회
    ~~~
 import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TimeZone;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LogDeviceHandler implements RequestHandler<Event, String> {

    private DynamoDB dynamoDb;
    

    @Override
    public String handleRequest(Event input, Context context) {
        this.initDynamoDbClient();

        Table table = dynamoDb.getTable(input.device);

        long from=0;
        long to=0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            from = sdf.parse(input.from).getTime() / 1000;
            to = sdf.parse(input.to).getTime() / 1000;
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        ScanSpec scanSpec = new ScanSpec()
                .withFilterExpression("#t between :from and :to").withNameMap(new NameMap().with("#t", "time"))
                .withValueMap(new ValueMap().withNumber(":from", from).withNumber(":to", to));

        ItemCollection<ScanOutcome> items=null;
        try {
            items = table.scan(scanSpec);
        }
        catch (Exception e) {
            System.err.println("Unable to scan the table:");
            System.err.println(e.getMessage());
        }

        return getResponse(items);
    }

    private String getResponse(ItemCollection<ScanOutcome> items) {

        Iterator<Item> iter = items.iterator();
        String response = "{ \"data\": [";
        for (int i =0; iter.hasNext(); i++) {
            if (i!=0) 
                response +=",";
            response += iter.next().toJSON();
        }
        response += "]}";
        return response;
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("ap-northeast-2").build();

        this.dynamoDb = new DynamoDB(client);
    }
}

class Event {
    public String device;
    public String from;
    public String to;
}
~~~
3. 상태변경
~~~
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.amazonaws.services.iotdata.AWSIotData;
import com.amazonaws.services.iotdata.AWSIotDataClientBuilder;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import com.amazonaws.services.iotdata.model.UpdateThingShadowResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.annotation.JsonCreator;

public class UpdateDeviceHandler implements RequestHandler<Event, String> {

    @Override
    public String handleRequest(Event event, Context context) {
        context.getLogger().log("Input: " + event);

        AWSIotData iotData = AWSIotDataClientBuilder.standard().build();

        String payload = getPayload(event.tags);

        UpdateThingShadowRequest updateThingShadowRequest  = 
                new UpdateThingShadowRequest()
                    .withThingName(event.device)
                    .withPayload(ByteBuffer.wrap(payload.getBytes()));

        UpdateThingShadowResult result = iotData.updateThingShadow(updateThingShadowRequest);
        byte[] bytes = new byte[result.getPayload().remaining()];
        result.getPayload().get(bytes);
        String resultString = new String(bytes);
        return resultString;
    }

    private String getPayload(ArrayList<Tag> tags) {
        String tagstr = "";
        for (int i=0; i < tags.size(); i++) {
            if (i !=  0) tagstr += ", ";
            tagstr += String.format("\"%s\" : \"%s\"", tags.get(i).tagName, tags.get(i).tagValue);
        }
        return String.format("{ \"state\": { \"desired\": { %s } } }", tagstr);
    }

}

class Event {
    public String device;
    public ArrayList<Tag> tags;

    public Event() {
         tags = new ArrayList<Tag>();
    }
}

class Tag {
    public String tagName;
    public String tagValue;

    @JsonCreator 
    public Tag() {
    }

    public Tag(String n, String v) {
        tagName = n;
        tagValue = v;
    }
}
~~~
4. 상태조회
~~~
import com.amazonaws.services.iotdata.AWSIotData;
import com.amazonaws.services.iotdata.AWSIotDataClientBuilder;
import com.amazonaws.services.iotdata.model.GetThingShadowRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class GetDeviceHandler implements RequestHandler<Event, String> {

    @Override
    public String handleRequest(Event event, Context context) {
        AWSIotData iotData = AWSIotDataClientBuilder.standard().build();

        GetThingShadowRequest getThingShadowRequest  = 
        new GetThingShadowRequest()
            .withThingName(event.device);

        iotData.getThingShadow(getThingShadowRequest);

        return new String(iotData.getThingShadow(getThingShadowRequest).getPayload().array());
    }
}

class Event {
    public String device;
}
~~~

 
