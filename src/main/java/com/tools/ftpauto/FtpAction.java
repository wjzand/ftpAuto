package com.tools.ftpauto;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.tools.ftpauto.entity.*;
import com.tools.ftpauto.utils.FileUtils;
import com.tools.ftpauto.utils.HttpUtils;
import com.tools.ftpauto.utils.SftpUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
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
    private int height = 270;

    private int lastProgress = -1;

    /**
     * ui
     */
    private void initUi(AnActionEvent eve,ConfigEntity configEntity){
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
        JRadioButton pre = new JRadioButton("预生产");
        buttonGroup.add(pre);
        JRadioButton pro = new JRadioButton("正式");
        buttonGroup.add(pro);
        envirmentJPanel.add(envirmentTip);
        envirmentJPanel.add(dev);
        envirmentJPanel.add(test);
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
                    parent.setSize(width,height + 125);
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
        JButton completeBt = new JButton("配置完成");
        JProgressBar jProgressBar = new JProgressBar();
        jProgressBar.setStringPainted(true);

        JPanel tip = new JPanel();
        tip.setLayout(new VerticalFlowLayout());
        JLabel fileLab = new JLabel("上传的文件目录");
        JLabel ftpLab = new JLabel("上传的ftp目录");
        JLabel progressLab = new JLabel("上传进度");
        tip.add(fileLab);
        tip.add(ftpLab);
        tip.add(progressLab);
        completeBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!isUpload){
                    isUpload = true;
//                    File file = new File("F:\\project\\operation-android\\app\\build\\outputs\\apk\\debug\\operation_debug_v1.2.6_1222_1756.apk");
                    File file = null;
                    String envirment = "开发";
                    if(dev.isSelected()){
                        envirment = configEntity.getFtpConfig().getDev();
                        file = findFile(eve.getProject().getBasePath() + File.separator + "app/debug",
                                eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/debug","");
                    }
                    if(test.isSelected()){
                        envirment = configEntity.getFtpConfig().getTest();
                        file = findFile(eve.getProject().getBasePath() + File.separator + "app/dat",
                                eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/dat",
                                eve.getProject().getBasePath() + File.separator + "app/build/outputs/apk/debug");
                    }
                    if(pre.isSelected()){
                        envirment = configEntity.getFtpConfig().getPre();
                        file = findFile(eve.getProject().getBasePath() + File.separator + "app/pre",
                                eve.getProject().getBasePath() + File.separator + "app/release","");
                    }
                    if(pro.isSelected()){
                        envirment = configEntity.getFtpConfig().getPro();
                        file = findFile(eve.getProject().getBasePath() + File.separator + "app/release","","");
                    }
                    file = new File("F:\\project\\operation-android\\app\\build\\outputs\\apk\\debug\\operation_debug_v1.3.0_0111_0941.apk");
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
                            EventQueue.invokeLater(() -> {
                                progressLab.setText("上传进度->" + p);
                                if(lastProgress != p){
                                    lastProgress = p;
                                    jProgressBar.setValue(p);
                                }
                            });
                        }

                        @Override
                        public void onComplete() {
                            logger.info("上传成功");
                            isUpload = false;
//                            if(ddRadio.isSelected()){
//                                String content = "最新包" + finalFile.getName() + "已上传";
//                                content = content + "\n" + "文件所在ftp的目录：" + finalRemotePath;
//                                if(!ddMsg.getText().isEmpty()){
//                                    content  = content +  "\n" + ddMsg.getText();
//                                }
//                                HttpUtils.getInstance().setLogger(logger);
//                                HttpUtils.getInstance().dd(configEntity.getDdConfig().getSocket(),content,ddUsersSelectMap.values());
//                            }
                            JOptionPane.showMessageDialog(null, "上传成功");
                        }
                    });
                }else {
                    JOptionPane.showMessageDialog(null, "正在上传中");
                }
            }
        });
        completeJPanel.add(completeBt);
        completeJPanel.add(jProgressBar);
        parentJPanel.add(completeJPanel);
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
    private File findFile(String path,String secondPath,String thirdPath) {
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
                        file = f;
                        break;
                    }
                }
            }
        }
        return file;
    }
}
