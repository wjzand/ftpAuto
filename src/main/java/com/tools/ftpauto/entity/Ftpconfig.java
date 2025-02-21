
package com.tools.ftpauto.entity;

/**
 * Auto-generated: 2022-12-08 17:40:16
 *
 * @author www.pcjson.com
 * @website http://www.pcjson.com/json2java/ 
 */
public class Ftpconfig {

    private String host;
    private String account;
    private String password;
    private String ftpApkBasePath;
    private String dev = "开发";
    private String test = "测试";
    private String pre = "准生产";
    private String pro = "生产";

    private String sit = "sit";

    private String jdUrl = "https://runenc.ijiami.cn:8080/?#/login";

    private String authKey = "";

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getJdUrl() {
        return jdUrl;
    }

    public void setJdUrl(String jdUrl) {
        this.jdUrl = jdUrl;
    }

    public String getSit() {
        return sit;
    }

    public void setSit(String sit) {
        this.sit = sit;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setAccount(String account) {
        this.account = account;
    }
    public String getAccount() {
        return account;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }

    public void setFtpApkBasePath(String ftpApkBasePath) {
        this.ftpApkBasePath = ftpApkBasePath;
    }

    public String getFtpApkBasePath() {
        return ftpApkBasePath;
    }

    public void setTest(String test) {
        this.test = test;
    }
    public String getTest() {
        return test;
    }

    public void setPre(String pre) {
        this.pre = pre;
    }
    public String getPre() {
        return pre;
    }

    public void setPro(String pro) {
        this.pro = pro;
    }
    public String getPro() {
        return pro;
    }

    public String getDev() {
        return dev;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }
}