
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
    private String ftpapkbasepath;
    private String dev = "开发";
    private String test = "测试";
    private String pre = "准生产";
    private String pro = "生产";

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

    public void setFtpapkbasepath(String ftpapkbasepath) {
        this.ftpapkbasepath = ftpapkbasepath;
    }
    public String getFtpapkbasepath() {
        return ftpapkbasepath;
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