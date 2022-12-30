package com.tools.ftpauto.utils;

import java.io.*;

public class FileUtils {
    /*
     * *读取文件方法
     */
    @SuppressWarnings("resource")
    public static String readFile(String strpath) {
        System.out.println("readFile:" + strpath);
        String res = null;
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File(strpath);
            InputStreamReader br = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader reader = new BufferedReader(br);
            String str;
            while ((str = reader.readLine()) != null) {
                System.out.println("readFiledddd:" + str);
                sb.append(str + "\n");
            }
            res = sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /*
     * 写入文件方法
     */
    public static void writeFile(File file, String content) {
        if (file == null || content == null) {
            return;
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
