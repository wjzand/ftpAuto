package com.tools.ftpauto.entity;

import java.util.List;
/**
 * Auto-generated: 2022-12-08 17:40:16
 *
 * @author www.pcjson.com
 * @website http://www.pcjson.com/json2java/
 */
public class ConfigEntity {

    private List<Client> client;
    private List<Dduser> ddUser;
    private Ftpconfig ftpConfig;
    private Ddconfig ddConfig;

    public List<Client> getClient() {
        return client;
    }

    public void setClient(List<Client> client) {
        this.client = client;
    }


    public List<Dduser> getDdUser() {
        return ddUser;
    }

    public void setDdUser(List<Dduser> ddUser) {
        this.ddUser = ddUser;
    }

    public Ftpconfig getFtpConfig() {
        return ftpConfig;
    }

    public void setFtpConfig(Ftpconfig ftpConfig) {
        this.ftpConfig = ftpConfig;
    }

    public Ddconfig getDdConfig() {
        return ddConfig;
    }

    public void setDdConfig(Ddconfig ddConfig) {
        this.ddConfig = ddConfig;
    }
}