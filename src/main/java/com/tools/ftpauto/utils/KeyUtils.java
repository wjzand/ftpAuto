package com.tools.ftpauto.utils;

import com.intellij.openapi.diagnostic.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

public class KeyUtils {
    private volatile static KeyUtils httpUtils;
    private Logger logger;

    private String keyD = "qwert18768494086";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


    public static KeyUtils getInstance() {
        synchronized (KeyUtils.class){
            if(httpUtils == null){
                httpUtils = new KeyUtils();
            }
        }
        return httpUtils;
    }

    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] keyPre = keyD.getBytes("UTF-8");
        SecretKey key = new SecretKeySpec(keyPre,"AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] keyPre = keyD.getBytes("UTF-8");
        SecretKey key = new SecretKeySpec(keyPre,"AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    public SimpleDateFormat getDateFormat(){
        return dateFormat;
    }

    public static void main(String[] args) {
        try {
            String buildIconPathPre = "d3b7c09b45aaee801b88e7ad65bef0eb".substring(0,5);
            String buildIconPath = String.join("/",buildIconPathPre.split(""));
            System.out.println("111 === " + buildIconPathPre + " === " + buildIconPath);
            Calendar calendar =  Calendar.getInstance();
            calendar.add(Calendar.MONTH, Integer.valueOf(4));
            String newK = KeyUtils.getInstance().encrypt(KeyUtils.getInstance().getDateFormat().format(calendar.getTime()));
            String newKd = getInstance().decrypt(newK);
            String s = getInstance().encrypt("2025-6-19");
            String sd = getInstance().decrypt(s);
            System.out.println(s);
            System.out.println(sd);
            System.out.println(newK);
            System.out.println(newKd);
            System.out.println("--" + Calendar.getInstance().getTime());
            System.out.println("--" + getInstance().getDateFormat().parse(newKd));
            System.out.println(Calendar.getInstance().getTime().after(getInstance().getDateFormat().parse(newKd)));
            System.out.println(Calendar.getInstance().getTime().before(getInstance().getDateFormat().parse(newKd)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
