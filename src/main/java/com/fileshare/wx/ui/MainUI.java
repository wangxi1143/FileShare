package main.java.com.fileshare.wx.ui;

import main.java.com.fileshare.wx.server.FileServer;
import main.java.com.fileshare.wx.util.LogUtil;

import javax.swing.*;
import java.io.IOException;

public class MainUI extends javax.swing.JFrame  {
    private JTextArea logArea;
    private JScrollPane logScrollPane;
    private JTextField inputField;
    private JButton startServerBtn;
    private JButton stopServerBtn;
    private FileServer fileServer;
    private boolean serverRunning = false;

    public MainUI() {
        setTitle("LocalNASServer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000,700);
        setLocationRelativeTo(null);
        setLayout(null);

        initLogComponent();
        initControlPanel();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
            }
        });
        
        setVisible(true);
        LogUtil.info("=== LocalNASServer 启动 ===");
        LogUtil.info("日志系统已初始化");
        LogUtil.config("UI 组件加载完成");
        
        startFileServer();
    }

    private void initLogComponent() {
        int margin = 20;
        int width = getWidth();
        int height = getHeight();
        
        int logHeight = height - (margin * 4);
        int logWidth = (width / 2) - margin;

        logArea = new JTextArea();
        logArea.setEditable(false);
        
        LogUtil.setLogTextArea(logArea);

        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBounds(width / 2, margin, logWidth, logHeight);

        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        add(logScrollPane);

        inputField = new JTextField();
        inputField.setBounds(width / 2, margin + logHeight, logWidth, margin);
        add(inputField);
    }

    private void initControlPanel() {
    }
    
    private void startFileServer() {
        if (!serverRunning) {
            try {
                fileServer = new FileServer();
                fileServer.start();
                serverRunning = true;
                LogUtil.info("文件服务器已成功启动");
            } catch (IOException e) {
                LogUtil.info("启动文件服务器失败：" + e.getMessage());
            }
        }
    }
    
    private void stopFileServer() {
        if (serverRunning && fileServer != null) {
            fileServer.stop();
            serverRunning = false;
            LogUtil.info("文件服务器已停止");
        }
    }

}
