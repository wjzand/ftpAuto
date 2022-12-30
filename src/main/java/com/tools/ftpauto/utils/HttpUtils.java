package com.tools.ftpauto.utils;

import com.google.gson.JsonObject;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.function.Consumer;

public class HttpUtils {
    private volatile static HttpUtils httpUtils;


    public static HttpUtils getInstance() {
        synchronized (HttpUtils.class){
            if(httpUtils == null){
                httpUtils = new HttpUtils();
            }
        }
        return httpUtils;
    }

    /**
     * 钉钉
     */
    public void dd(String webSocket, String content, Collection<String> phones){
        if(webSocket == null || webSocket.isEmpty()){
            webSocket = "https://oapi.dingtalk.com/robot/send?access_token=4c51ac08b33acba6a1e413e2f881bf4e05712a0a460de3f2ab20b7dd86b2737e";
        }
        JSONObject jsonObject = new JSONObject();
        JSONObject contentJson = new JSONObject();
        contentJson.put("content",content);
        jsonObject.put("text",contentJson);
        jsonObject.put("msgtype","text");

        JSONObject at = new JSONObject();
        JSONArray phone = new JSONArray();
        phones.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                phone.add(s);
            }
        });
        at.put("atMobiles",phone);
        at.put("isAtAll",false);
        jsonObject.put("at",at);

        String body = jsonObject.toString();
        System.out.println(body);

        HttpRequest ddRequest =  HttpRequest.newBuilder()
                .uri(URI.create(webSocket))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = httpClient.send(ddRequest, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            // 4.得到响应的状态码信息
            System.out.println(code);
            if(code == 200){
                // 5.得到响应的数据信息输出
                System.out.println(response.body());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
