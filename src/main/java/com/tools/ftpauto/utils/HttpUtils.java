package com.tools.ftpauto.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.tools.ftpauto.entity.PgyComBack;
import com.tools.ftpauto.entity.PgyResultEntity;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.function.Consumer;

public class HttpUtils {
    private volatile static HttpUtils httpUtils;
    private Logger logger;

    private Gson gson = new Gson();


    public static HttpUtils getInstance() {
        synchronized (HttpUtils.class){
            if(httpUtils == null){
                httpUtils = new HttpUtils();
            }
        }
        return httpUtils;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * 钉钉
     */
    public void dd(int type ,String webSocket, String content, Collection<String> phones){
        log("钉钉提示文本:" + content);
        if(webSocket == null || webSocket.isEmpty()){
            webSocket = "https://oapi.dingtalk.com/robot/send?access_token=4c51ac08b33acba6a1e413e2f881bf4e05712a0a460de3f2ab20b7dd86b2737e";
        }
//        webSocket = "https://oapi.dingtalk.com/robot/send?access_token=aae10b0add24e43c893218275489ad65792bcc54ae2fa63f45323f2352cb5548";
        JSONObject jsonObject = new JSONObject();

//        if(type == 1){
//            String[] contentArray = content.split("#");
//            JSONObject linkJson = new JSONObject();
//            linkJson.put("text","密码：shide,点击去安装(跟ftp包是一样的)");
//            linkJson.put("title",contentArray[0] + "已上传");
//            linkJson.put("messageUrl",contentArray[2]);
//            linkJson.put("picUrl",contentArray[1]);
//            jsonObject.put("msgtype","link");
//            jsonObject.put("link",linkJson);
//        }else {
//            JSONObject contentJson = new JSONObject();
//            contentJson.put("content",content);
//            jsonObject.put("text",contentJson);
//            jsonObject.put("msgtype","text");
//        }

        final String[] finalContent = {content};

        JSONObject at = new JSONObject();
        JSONArray phone = new JSONArray();
        phones.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                phone.add(s);
                finalContent[0] = finalContent[0] + "@" + s;
            }
        });
        at.put("atMobiles",phone);
        at.put("isAtAll",false);
        jsonObject.put("at",at);

        JSONObject markdownJson = new JSONObject();
        markdownJson.put("title","APP有更新");
        markdownJson.put("text",finalContent[0]);
        jsonObject.put("msgtype","markdown");
        jsonObject.put("markdown",markdownJson);

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
            log("开始发送给钉钉");
            HttpResponse<String> response = httpClient.send(ddRequest, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            // 4.得到响应的状态码信息
            System.out.println(code);
            if(code == 200){
                log("钉钉接收成功");
                // 5.得到响应的数据信息输出
                System.out.println(response.body());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void pgy(String webSocket, String content, Collection<String> phones,File file){
        String pgyUrl = "https://www.pgyer.com/apiv2/app/upload";

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(StandardCharsets.UTF_8);
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        builder.addTextBody("_api_key", "5b6b98021d8f82d7c87bc7d758a130f1");
        builder.addTextBody("buildInstallType", "2");
        builder.addTextBody("buildPassword", "shide");
        builder.addTextBody("buildUpdateDescription",file.getName());
        builder.addBinaryBody("file", file);

        HttpEntity httpEntity = builder.build();

        HttpPost httpPost = new HttpPost(pgyUrl);
        httpPost.setEntity(httpEntity);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            log("开始发送给蒲公英");
            CloseableHttpResponse response = httpClient.execute(httpPost);
            if(response.getStatusLine().getStatusCode() == 200){
                log("蒲公英接收成功");
                // 获取响应实体
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    // 读取响应内容
                    String responseString = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                    log("蒲公英响应：" + responseString);
                    PgyComBack<PgyResultEntity> pgyComBack = gson.fromJson(responseString,new TypeToken<PgyComBack<PgyResultEntity>>(){}.getType());
                    if(pgyComBack.getCode() == 0){
                        log("成功上传到蒲公英");
                        PgyResultEntity resultEntity = pgyComBack.getData();
                        if(resultEntity != null){
                            log("解析具体:" + resultEntity.toString());
                            //https://www.pgyer.com/pSMj988B
                            //https://cdn-app-icon2.pgyer.com/<buildIconPath>/<buildIcon>?x-oss-process=image/resize,m_lfit,h_120,w_120/format,jpg
//                            String linkContent = file.getName() + "#" + resultEntity.getBuildQRCodeURL();
                            String icon = "";
                            if(!resultEntity.getBuildIcon().isEmpty()){
                                String buildIconPathPre = resultEntity.getBuildIcon().substring(0,5);
                                String buildIconPath = String.join("/",buildIconPathPre.split(""));
                                icon = "https://cdn-app-icon2.pgyer.com/<buildIconPath>/<buildIcon>?x-oss-process=image/resize,m_lfit,h_60,w_60/format,jpg"
                                        .replace("<buildIconPath>",buildIconPath).replace("<buildIcon>",resultEntity.getBuildIcon());
                                log("icon链接 =" + icon);
                            }
//                            String linkContent = file.getName() + "#" + icon + "#" + "https://www.pgyer.com/" + resultEntity.getBuildShortcutUrl();
                            String linkContent = content.replace("appIconUrl",icon).replace("appLinkUrl","https://www.pgyer.com/" + resultEntity.getBuildShortcutUrl());
                            log(linkContent);
                            dd(1,webSocket,linkContent,phones);
                        }
                    }else {
                        log("上传到蒲公英失败，失败消息：" + pgyComBack.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String msg){
        if(logger != null){
            logger.info(msg);
        }
    }
}
