package com.project.seniorpal.skill.service.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Json {

    public static Map<String, String> jsonToStringMap(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        Map<String, String> res = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String key = it.next();
            res.put(key, jsonObject.optString(key));
        }
        return res;
    }

    public static String stringMapToJson(Map<String, String> map) {
        return new JSONObject(map).toString();
    }

    private Json() {

    }
}
