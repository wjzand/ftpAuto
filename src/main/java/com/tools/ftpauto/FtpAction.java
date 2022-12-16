package com.tools.ftpauto;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.tools.ftpauto.entity.*;
import com.tools.ftpauto.utils.FileUtils;

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
import java.util.function.Consumer;

public class FtpAction extends AnAction {

    private String[] listData = new String[]{"司机端", "货主端", "商户端", "运营端"};
    private String[] ddUsers = new String[]{"刘峰杰1", "刘峰杰2", "刘峰杰3", "刘峰杰4"};

    private HashMap<String,String> ddUsersMap = new HashMap<>();

    private Ftpconfig ftpconfig;

    private Ddconfig ddconfig;

    //当前选中的客户端
    private int currentSelectClientIndex = 0;

    //当前选中的钉钉@端
    private int currentSelectDdUser = 0;

    private Gson gson = new Gson();

    @Override
    public void actionPerformed(AnActionEvent e) {
        String projectPath = e.getProject().getBasePath();
        getLocalConfig(e,projectPath);
        initUi(e);
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
            ftpconfig = configEntity.getFtpconfig();
            ddconfig = configEntity.getDdconfig();
            if(configEntity.getClient() != null && !configEntity.getClient().isEmpty()){
                listData = new String[configEntity.getClient().size()];
                for(int i = 0; i < configEntity.getClient().size();i++){
                    String clientName = configEntity.getClient().get(i).getClientName();
                    if(clientName.equalsIgnoreCase(e.getProject().getName())){
                        currentSelectClientIndex = i;
                    }
                    listData[i] = configEntity.getClient().get(i).getName();
                }
            }
            if(configEntity.getDduser() != null && !configEntity.getDduser().isEmpty()){
                ddUsers = new String[configEntity.getDduser().size()];
                ddUsersMap.clear();
                for(int i = 0; i < configEntity.getDduser().size();i++){
                    String name = configEntity.getDduser().get(i).getName();
                    String phone = configEntity.getDduser().get(i).getPhone();
                    ddUsers[i] = name;
                    ddUsersMap.put(name,phone);
                }
            }
        }
    }

    /**
     * ui
     */
    private void initUi(AnActionEvent e){
        JFrame parent = new JFrame("ftp自动上传配置");
        parent.setSize(300,200);
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
        JLabel envirmentTip = new JLabel("请选择环境");
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton test = new JRadioButton("测试");
        buttonGroup.add(test);
        JRadioButton pre = new JRadioButton("预生产");
        buttonGroup.add(pre);
        JRadioButton pro = new JRadioButton("正式");
        buttonGroup.add(pro);
        envirmentJPanel.add(envirmentTip);
        envirmentJPanel.add(test);
        envirmentJPanel.add(pre);
        envirmentJPanel.add(pro);
        parentJPanel.add(envirmentJPanel);


        //level 3
        JPanel DDJPanel = new JPanel();
        DDJPanel.setLayout(new VerticalFlowLayout());
        JRadioButton ddRadio = new JRadioButton("上传完毕后是否需要通知到钉钉群");
        DDJPanel.add(ddRadio);
        JLabel ddUserTip = new JLabel("请选择需要@的钉钉人员");
        JComboBox<String> userBox = new JComboBox<String>(ddUsers);
        userBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    currentSelectDdUser = userBox.getSelectedIndex();
                }
            }
        });
        userBox.setSelectedIndex(currentSelectDdUser);
        JLabel ddTip = new JLabel("请输入钉钉提示的文本,可不填");
        JTextArea ddMsg = new JTextArea();
        ddMsg.setLineWrap(true);
        ddRadio.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    DDJPanel.add(ddUserTip);
                    DDJPanel.add(userBox);
                    DDJPanel.add(ddTip);
                    DDJPanel.add(ddMsg);
                    parentJPanel.updateUI();
                    parent.setSize(300,330);
                }else {
                    DDJPanel.remove(ddUserTip);
                    DDJPanel.remove(userBox);
                    DDJPanel.remove(ddTip);
                    DDJPanel.remove(ddMsg);
                    parentJPanel.updateUI();
                    parent.setSize(300,200);
                }
            }
        });
        parentJPanel.add(DDJPanel);


        //level 4
        JPanel completeJPanel = new JPanel();
        JButton completeBt = new JButton("配置完成");
        completeBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "配置完成");
            }
        });
        completeJPanel.add(completeBt);
        parentJPanel.add(completeJPanel);


        parent.setContentPane(parentJPanel);
        parent.setVisible(true);
    }
}
