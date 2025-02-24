package com.tools.ftpauto;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpProgressMonitor;
import com.tools.ftpauto.entity.*;
import com.tools.ftpauto.utils.FileUtils;
import com.tools.ftpauto.utils.HttpUtils;
import com.tools.ftpauto.utils.KeyUtils;
import com.tools.ftpauto.utils.SftpUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FtpAction extends AnAction {

    private String[] listData = new String[]{"司机端", "货主端", "商户端", "运营端"};

    private HashMap<String,String> ddUsersMap = new HashMap<>();
    private HashMap<String,String> ddUsersSelectMap = new HashMap<>();

    private Ftpconfig ftpconfig;

    private Ddconfig ddconfig;

    //当前选中的客户端
    private int currentSelectClientIndex = 0;

    private Gson gson = new Gson();

    private Logger logger = Logger.getInstance(FtpAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        String projectPath = e.getProject().getBasePath();
        getLocalConfig(e,projectPath);
    }

    /**
     * 获取本地配置
     */
    private void getLocalConfig(AnActionEvent e,String projectPath){
        String configPath = projectPath + "/ftpAutoConfig.txt";
        String localConfig = FileUtils.readFile(configPath);
        if(localConfig == null || localConfig.isEmpty()){
          return;
        }
        ConfigEntity configEntity = gson.fromJson(localConfig, ConfigEntity.class);
        if(configEntity != null){
            ftpconfig = configEntity.getFtpConfig();
            ddconfig = configEntity.getDdConfig();
            if(ftpconfig.getAuthKey().isEmpty()){
                JOptionPane.showMessageDialog(null, "当前用户未授权，请联系管理员");
                return;
            }else {
                try {
                    String auth = KeyUtils.getInstance().decrypt(ftpconfig.getAuthKey());
                    logger.info("authKey ==" + auth);
                    if(auth != null && Calendar.getInstance().getTime().after(KeyUtils.getInstance().getDateFormat().parse(auth))){
                        JOptionPane.showMessageDialog(null, "当前用户授权已过期，请联系管理员");
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "用户授权出现异常，请联系管理员");
                }
            }
            if(configEntity.getClient() != null && !configEntity.getClient().isEmpty()){
                listData = new String[configEntity.getClient().size()];
                for(int i = 0; i < configEntity.getClient().size();i++){
                    String clientName = configEntity.getClient().get(i).getClientName();
                    if(e.getProject().getName().toLowerCase().contains(clientName)){
                        currentSelectClientIndex = i;
                    }
                    listData[i] = configEntity.getClient().get(i).getName();
                }
            }
            if(configEntity.getDdUser() != null && !configEntity.getDdUser().isEmpty()){
                ddUsersMap.clear();
                for(int i = 0; i < configEntity.getDdUser().size();i++){
                    String name = configEntity.getDdUser().get(i).getName();
                    String phone = configEntity.getDdUser().get(i).getPhone();
                    ddUsersMap.put(name,phone);
                }
            }
        }
        initUi(e,configEntity);
    }


    boolean isUpload = false;

    private int width = 400;
    private int height = 340;

    private int lastProgress = -1;

    private File file = null;
    private String envirment = "开发";

    private ChannelSftp.LsEntry remoteLast = null;

    /**
     * ui
     */
    private void initUi(AnActionEvent eve,ConfigEntity configEntity){
        SftpUtils.getInstance().setLogger(logger);
        SftpUtils.getInstance().connect();

        JFrame parent = new JFrame("ftp自动上传配置");
        parent.setSize(width,height);
        parent.setLocationRelativeTo(null);
        parent.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        JPanel parentJPanel= new JPanel();
        parentJPanel.setLayout(new VerticalFlowLayout(5,2));

        //level 1
        JPanel clientJPanel = new JPanel();
        JLabel clientjLabel = new JLabel("客户端: ");
        clientJPanel.add(clientjLabel);
        JComboBox<String> client = new JComboBox<String>(listData);
        client.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    currentSelectClientIndex = client.getSelectedIndex();
                }
            }
        });
        client.setSelectedIndex(currentSelectClientIndex);
        clientJPanel.add(client);
        JRadioButton pgyRadio = new JRadioButton("蒲公英");
        clientJPanel.add(pgyRadio);
        parentJPanel.add(clientJPanel);

        //level 2
        JPanel envirmentJPanel = new JPanel();
        JLabel envirmentTip = new JLabel("请选择");
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton dev = new JRadioButton("开发");
        buttonGroup.add(dev);
        JRadioButton test = new JRadioButton("测试");
        buttonGroup.add(test);
        test.setSelected(true);
        JRadioButton sit = new JRadioButton("sit");
        buttonGroup.add(sit);
        JRadioButton pre = new JRadioButton("预生产");
        buttonGroup.add(pre);
        JRadioButton pro = new JRadioButton("正式");
        buttonGroup.add(pro);
        envirmentJPanel.add(envirmentTip);
        envirmentJPanel.add(dev);
        envirmentJPanel.add(test);
        envirmentJPanel.add(sit);
        envirmentJPanel.add(pre);
        envirmentJPanel.add(pro);
        parentJPanel.add(envirmentJPanel);


        //level 3
        JPanel DDJPanel = new JPanel();
        DDJPanel.setLayout(new VerticalFlowLayout());
        JRadioButton ddRadio = new JRadioButton("上传完毕后是否需要通知到钉钉群");
        DDJPanel.add(ddRadio);
        int row = 1;
        int col = 3;
        //最多2行
        if(ddUsersMap.size() > 3){
            row = 2;
        }
        GridLayout layout = new GridLayout(row, col);
        JPanel DDUserJPanel = new JPanel(layout);
        JLabel ddUserTip = new JLabel("请选择需要@的钉钉人员");
        ddUsersSelectMap.clear();
        ddUsersMap.forEach((s, s2) -> {
            JRadioButton ddRadio1 = new JRadioButton(s);
            DDUserJPanel.add(ddRadio1);
            ddRadio1.addItemListener(e1 -> {
                if(e1.getStateChange() == ItemEvent.SELECTED){
                    ddUsersSelectMap.put(ddRadio1.getText(),ddUsersMap.get(ddRadio1.getText()));
                }else {
                    ddUsersSelectMap.remove(ddRadio1.getText());
                }
            });
        });
        JLabel ddTip = new JLabel("请输入钉钉提示的文本,可不填");
        JTextArea ddMsg = new JTextArea();
        ddMsg.setLineWrap(true);
        ddRadio.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    DDJPanel.add(ddUserTip);
                    DDJPanel.add(DDUserJPanel);
                    DDJPanel.add(ddTip);
                    DDJPanel.add(ddMsg);
                    parentJPanel.updateUI();
                    parent.setSize(width,height + 150);
                }else {
                    DDJPanel.remove(ddUserTip);
                    DDJPanel.remove(DDUserJPanel);
                    DDJPanel.remove(ddTip);
                    DDJPanel.remove(ddMsg);
                    parentJPanel.updateUI();
                    parent.setSize(width,height);
                }
            }
        });
        parentJPanel.add(DDJPanel);


        //level 4
        JPanel completeJPanel = new JPanel();
        JButton completeBt = new JButton("上传到ftp");
        JButton jiaGuBt = new JButton("去加固");
        JButton lastBt = new JButton("查看远程最新包");


        JPanel authJPanel = new JPanel();
        JLabel authTip = new JLabel("管理员授权登录");
        JPasswordField jPasswordField = new JPasswordField(18);
        JButton authBt = new JButton("授权");
        authJPanel.add(authTip);
        authJPanel.add(jPasswordField);
        authJPanel.add(authBt);

        JPanel tip = new JPanel();
        tip.setLayout(new VerticalFlowLayout());
        JLabel lastLab = new JLabel("远程最新包信息在这里展示");
        JLabel fileLab = new JLabel("上传的文件目录");
        JLabel ftpLab = new JLabel("上传的ftp目录");
        JLabel progressLab = new JLabel("上传进度");
        tip.add(lastLab);
        tip.add(fileLab);
        tip.add(ftpLab);
        tip.add(progressLab);

        //File fileTest = new File("F:\\project\\operation-android\\app\\build\\outputs\\apk\\debug\\operation_debug_v1.2.6_1222_1756.apk");
        if(dev.isSelected()){
            envirment = configEntity.getFtpConfig().getDev();
            file = findFile(eve.getProject().getBasePath() + File.separator + "app/debug",
                    eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/debug","",false,false,false);
        }
        if(test.isSelected()){
            envirment = configEntity.getFtpConfig().getTest();
            file = findFile(eve.getProject().getBasePath() + File.separator + "app/dat",
                    eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/dat",
                    eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/debug",true,false,false);
        }
        if(sit.isSelected()){
            envirment = configEntity.getFtpConfig().getSit();
            file = findFile(eve.getProject().getBasePath() + File.separator + "app/sit",
                    eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/sit",
                    eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/debug",false,true,false);
        }
        if(pre.isSelected()){
            envirment = configEntity.getFtpConfig().getPre();
            file = findFile(eve.getProject().getBasePath() + File.separator + "app/pre",
                    eve.getProject().getBasePath() + File.separator + "app/release","",false,false,true);
        }
        if(pro.isSelected()){
            envirment = configEntity.getFtpConfig().getPro();
            file = findFile(eve.getProject().getBasePath() + File.separator + "app/release","","",false,false,false);
        }

        dev.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    envirment = configEntity.getFtpConfig().getDev();
                    file = findFile(eve.getProject().getBasePath() + File.separator + "app/debug",
                            eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/debug","",false,false,false);
                }
            }
        });

        test.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    envirment = configEntity.getFtpConfig().getTest();
                    file = findFile(eve.getProject().getBasePath() + File.separator + "app/dat",
                            eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/dat",
                            eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/debug",true,false,false);
                }
            }
        });

        sit.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    envirment = configEntity.getFtpConfig().getSit();
                    file = findFile(eve.getProject().getBasePath() + File.separator + "app/sit",
                            eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/sit",
                            eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/debug",false,true,false);
                }
            }
        });

        pre.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    envirment = configEntity.getFtpConfig().getPre();
                    file = findFile(eve.getProject().getBasePath() + File.separator + "app/pre",
                            eve.getProject().getBasePath() + File.separator + "app/release","",false,false,true);
                }
            }
        });

        pro.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    envirment = configEntity.getFtpConfig().getPro();
                    file = findFile(eve.getProject().getBasePath() + File.separator + "app/release","","",false,false,false);
                }
            }
        });

        completeBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!isUpload){
                    isUpload = true;
                    if(file == null){
                        fileLab.setText("没有找到apk文件");
                        JOptionPane.showMessageDialog(null, "没有找到apk文件");
                        isUpload = false;
                        return;
                    }
                    String remotePath = "/home/driver/apk/智运-运营端/开发";
                    remotePath = configEntity.getFtpConfig().getFtpApkBasePath() + "/" + listData[currentSelectClientIndex] + "/" + envirment;
                    File finalFile = file;
                    fileLab.setText("找到文件->" + finalFile.getName());
                    ftpLab.setText("ftp目录->" + remotePath);
                    logger.info("找到文件：" + finalFile.getAbsolutePath());
                    logger.info("根据配置需要到ftp的目录是：" + remotePath);
                    String finalRemotePath = remotePath;
                    lastProgress = -1;
                    SftpUtils.getInstance().uploadFile(file, file.getName(), remotePath, new UploadProgress() {
                        @Override
                        public void onProgress(String percent) {
                            int p = Integer.valueOf(percent);
                            logger.info("上传进度" + p);
                            if(lastProgress != p){
                                lastProgress = p;
                                progressLab.setText("上传进度->" + p);
                            }
                        }

                        @Override
                        public void onComplete() {
                            logger.info("上传成功");
                            isUpload = false;
                            if(ddRadio.isSelected()){
                                String content = "##### 最新包" + finalFile.getName() + "已上传到ftp";
                                content = content + "\n" + "远程目录：" + finalRemotePath;
                                if(pgyRadio.isSelected()) {
                                    content = content + "\n\n" + "插件已升级，没有ftp可点击下面直达链接";
                                    content = content + "\n\n" + "安装密码：shide,跟ftp上的包是一样的";
                                    content = content + " \n " + "![应用图片](appIconUrl)";
                                    content = content + " \n " + "[直达链接->KFC](appLinkUrl)";
                                }else {
                                    content = content + "\n\n" + "插件已升级，如需网页下载安装可联系开发人员";
                                }
                                if(!ddMsg.getText().isEmpty()){
                                    content  = content +  " \n ##### " + ddMsg.getText();
                                }
                                HttpUtils.getInstance().setLogger(logger);
                                if(pgyRadio.isSelected()){
                                    HttpUtils.getInstance().pgy(configEntity.getDdConfig().getSocket(),content,ddUsersSelectMap.values(),finalFile);
                                }else {
                                    HttpUtils.getInstance().dd(0,configEntity.getDdConfig().getSocket(),content,ddUsersSelectMap.values());
                                }
                            }
                            JOptionPane.showMessageDialog(null, "上传成功");
                        }
                    });
                }else {
                    JOptionPane.showMessageDialog(null, "正在上传中");
                }
            }
        });
        jiaGuBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            //爱加固
                            desktop.browse(new URI(configEntity.getFtpConfig().getJdUrl()));
                        } catch (IOException | URISyntaxException ex) {
                           ex.printStackTrace();
                        }
                    }
                }else {
                    JOptionPane.showMessageDialog(null, "该操作系统暂不支持");
                }
                if(file != null){
                    //mac
                    try {
                        ProcessBuilder builder = new ProcessBuilder("open", file.getParent());
                        builder.start();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    //windows
                    try {
                        ProcessBuilder builder = new ProcessBuilder("explorer.exe", file.getParent());
                        builder.start();
//                        Runtime.getRuntime().exec("explorer.exe " + file.getParentFile());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        remoteLast = null;
        lastBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remoteLast =  SftpUtils.getInstance().findLastedFile(configEntity.getFtpConfig().getFtpApkBasePath() + "/" + listData[currentSelectClientIndex] + "/" + envirment);
                if(remoteLast != null){
                    lastLab.setText("远程最新包信息：" + remoteLast.getFilename());
                }else {
                    lastLab.setText("很抱歉，没在该环境下找到远程最新包信息");
                }
            }
        });

        lastLab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
            }
        });


        authBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] pass = jPasswordField.getPassword();
                String passStr = new String(pass);
                if(passStr.equals("15834028264")){
                    String date = JOptionPane.showInputDialog(null, "请输入授权信息");
                    Calendar calendar =  Calendar.getInstance();
                    if(date == null || date.isEmpty()){
                        return;
                    }
                    try {
                        calendar.add(Calendar.MONTH, Integer.valueOf(date));
                        String newK = KeyUtils.getInstance().encrypt(KeyUtils.getInstance().getDateFormat().format(calendar.getTime()));
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(newK),null);
                        JOptionPane.showMessageDialog(null, "授权成功，授权码已复制到剪贴板");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "授权失败，格式错误");
                    }
                }else {
                    JOptionPane.showMessageDialog(null, "请输入正确的管理员账号");
                }
            }
        });


        completeJPanel.add(completeBt);
        completeJPanel.add(jiaGuBt);
        completeJPanel.add(lastBt);
        parentJPanel.add(completeJPanel);
        parentJPanel.add(authJPanel);
        parentJPanel.add(tip);


        parent.setContentPane(parentJPanel);
        parent.setVisible(true);
    }

    /**
     * 找到文件
     * @param path 第一个去寻找的地址
     * @param secondPath 第二个去寻找的地址
     * @return
     */
    private File findFile(String path,String secondPath,String thirdPath,boolean isTest,boolean isSit,boolean isPre) {
        File file = null;
        File pfile = new File(path);
        if (!pfile.exists() && !secondPath.isEmpty()) {
            pfile = new File(secondPath);
        }
        if (!pfile.exists() && !thirdPath.isEmpty()) {
            pfile = new File(thirdPath);
        }
        if (pfile.exists()) {
            File[] p = pfile.listFiles();
            if (p != null) {
                for (File f : p) {
                    if (f.getName().endsWith(".apk")) {
                        //重新命名
                        if(isTest || isPre || isSit){
                            logger.info("文件原名：" + f.getName());
                            String name = f.getName();
                            if(isTest){
                                name = f.getName().replace("debug","test")
                                        .replace("dat","test");
                            }
                            if(isSit){
                                name = f.getName().replace("debug","sit")
                                        .replace("test","sit")
                                        .replace("dat","sit");
                            }
                            if(isPre){
                                name = f.getName().replace("release","pre");
                            }
                            File newFile = new File(f.getAbsolutePath().replace(f.getName(),name));
                            logger.info("预计将名字改为：" + newFile.getName());
                            boolean b = f.renameTo(newFile);
                            if(b){
                                file = newFile;
                                logger.info("修改成功：" + file.getName());
                            }else {
                                file = f;
                                logger.info("修改失败：" + file.getName());
                            }
                        }else {
                            file = f;
                        }
                        break;
                    }
                }
            }
        }
        if(file != null){
            logger.info("最终文件名：" + file.getName());
        }
        return file;
    }
}
