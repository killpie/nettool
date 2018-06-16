package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;

public class JsonParser {

    public static final String  comma = "%2C";// 逗号
    public static final String  space = "+";// 空格
    static ObjectMapper mapper = new ObjectMapper();

    public static JsonObject httpCovertJson(String param){

        JsonObject jsonObject = new JsonObject();

        if (param==null||"".equals(param)){
            jsonObject.put("","");
            return jsonObject;
        }

        param = param.replaceAll(comma,",");
        String[] start = param.split("&");

        for (int i = 0; i < start.length; i++) {
            String[] end = start[i].split("=");
            jsonObject.put(end[0],end[1]);
        }


        return jsonObject;
    }

    /**
     *将object对象转化为json结果集
     */
    public static JsonObject ObjectTojson(Object object) {
        try {
            String s = mapper.writeValueAsString(object);
            JsonObject jsonObject = new JsonObject(s);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
