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
    private List<Dduser> dduser;
    private Ftpconfig ftpconfig;
    private Ddconfig ddconfig;

    public List<Client> getClient() {
        return client;
    }

    public void setClient(List<Client> client) {
        this.client = client;
    }

    public List<Dduser> getDduser() {
        return dduser;
    }

    public void setDduser(List<Dduser> dduser) {
        this.dduser = dduser;
    }

    public void setFtpconfig(Ftpconfig ftpconfig) {
        this.ftpconfig = ftpconfig;
    }
    public Ftpconfig getFtpconfig() {
        return ftpconfig;
    }

    public void setDdconfig(Ddconfig ddconfig) {
        this.ddconfig = ddconfig;
    }
    public Ddconfig getDdconfig() {
        return ddconfig;
    }

}