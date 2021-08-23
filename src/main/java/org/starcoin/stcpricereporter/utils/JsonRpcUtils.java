package org.starcoin.stcpricereporter.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonRpcUtils {

    public static Object invoke(RestTemplate restTemplate, String url, String method, List<Object> params) {
        HttpEntity<Map<String, Object>> entity = createRequestEntity(method, params);
        String resultStr = restTemplate.postForObject(url, entity, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = null;
        try {
            responseMap = objectMapper.readValue(resultStr, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON RPC read value error.", e);
        }
        if (!indicatesSuccess(responseMap)) {
            throw new RuntimeException("JSON RPC invoke error." + responseMap.get("error"));
        }
        return responseMap.get("result");
    }

    private static boolean indicatesSuccess(Map<String, Object> responseMap) {
        return !responseMap.containsKey("error");//error == null;
    }

    private static HttpEntity<Map<String, Object>> createRequestEntity(String method, List<Object> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("id", 1);
        requestMap.put("jsonrpc", "2.0");
        requestMap.put("method", method);
        requestMap.put("params", params);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestMap, headers);
        return entity;
    }

}
