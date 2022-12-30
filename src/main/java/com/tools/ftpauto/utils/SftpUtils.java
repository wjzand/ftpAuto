package com.tools.ftpauto.utils;

import com.jcraft.jsch.*;
import com.tools.ftpauto.UploadProgress;
import com.tools.ftpauto.UploadProgressMonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SftpUtils {
    private JSch jSch;

    private Session session =null;

    private ChannelSftp sftp =null;

    private Session sshSession =null;

    private static SftpUtils sftpUtils =null;

    private String host = "10.7.0.8";
    private String userName = "driver";
    private String password = "driver01@";

    public SftpUtils() {

        jSch =new JSch();

    }

    public static SftpUtils getInstance(){
        if (sftpUtils==null){
            sftpUtils =new SftpUtils();
        }
        return sftpUtils;
    }

    public void bind(String host,String userName,String password){
        this.userName = userName;
        this.host = host;
        this.password = password;
    }

    /*
     * 启动连接
     * */
    public void connect(){
        try {
            sshSession =jSch.getSession(userName,host,22);
            sshSession.setPassword(password);

            Properties sshConfig =new Properties();

            sshConfig.put("StrictHostKeyChecking", "no");
            sshConfig.put("PreferredAuthentications", "password");

            sshSession.setConfig(sshConfig);
            sshSession.connect();

            Channel channel = sshSession.openChannel("sftp");
            channel.connect();

            sftp = (ChannelSftp) channel;

            }catch (JSchException e) {
                e.printStackTrace();
                System.out.println("异常:" + e.getMessage());
            }
    }

    /**
     * 上传单个文件
     *
     * @param remotePath：远程保存目录

     * @param remoteFileName：保存文件名

     * @return

     */

    public boolean uploadFile(File file, String remoteFileName, String remotePath, UploadProgress uploadProgress) {
        FileInputStream fip = null;
        try {
            fip = new FileInputStream(file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        boolean _flag =false;
        try {
            if (!isConnect()){
                connect();
                _flag =true;
            }

            createDir(remotePath);
            sftp.put(fip, remoteFileName,new UploadProgressMonitor(uploadProgress,file.length()));

            return true;

            }catch (SftpException e) {
                e.printStackTrace();
            }finally {
            if (fip !=null) {
                try {
                    fip.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (_flag)
                disConnect();
            }
        return false;
    }

    /**

     * 创建目录

     *

     * @param createpath

     * @return

     */

    private boolean createDir(String createpath) {

        boolean _flag =false;
         try {
            if (!isConnect()){
                connect();
                _flag =true;
            }
            if (isDirExist(createpath)) {
                this.sftp.cd(createpath);
                return true;
            }
            String pathArry[] = createpath.split("/");
            StringBuffer filePath =new StringBuffer("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path +"/");
                if (isDirExist(filePath.toString())) {
                    sftp.cd(filePath.toString());
                }else {
                    // 建立目录
                    sftp.mkdir(filePath.toString());
                    // 进入并设置为当前目录
                    sftp.cd(filePath.toString());
                }
            }
            this.sftp.cd(createpath);
            return true;
            }catch (SftpException e) {
            e.printStackTrace();
            }finally {
            if (_flag)
                disConnect();
        }
        return false;
    }

    /**

     * 判断目录是否存在

     * @param directory

     * @return

     */

    private boolean isDirExist(String directory) {
        boolean isDirExistFlag =false;
        boolean _flag =false;
        try {
            if (!isConnect()){
                connect();
                _flag =true;
            }
            SftpATTRS sftpATTRS =sftp.lstat(directory);
            isDirExistFlag =true;
            return sftpATTRS.isDir();
        }catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag =false;
            }

        }finally {

            if (_flag)

                disConnect();
        }

        return isDirExistFlag;
    }

    private boolean isConnect() {

        return (this.sftp !=null &&
                this.sftp.isConnected() &&
                this.sshSession !=null &&
                this.sshSession.isConnected());

    }

    /**
     * 关闭连接
     * */
    public void disConnect(){
        if (this.sftp!=null){
            if (this.sftp.isConnected()){
                this.sftp.disconnect();
            }
        }

        if (this.sshSession !=null) {
            if (this.sshSession.isConnected()) {
                this.sshSession.disconnect();
            }
        }
    }
}
