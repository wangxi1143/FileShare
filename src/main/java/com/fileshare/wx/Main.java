package main.java.com.fileshare.wx;

import main.java.com.fileshare.wx.init.FileInit;
import main.java.com.fileshare.wx.ui.MainUI;
import main.java.com.fileshare.wx.util.ArgsParser;
import main.java.com.fileshare.wx.util.LogUtil;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        LogUtil.info("应用程序启动");
        LogUtil.config("正在初始化系统组件...");

        Map<String, Object> arg=ArgsParser.argsParser(args);
        
        FileInit.systemInit();
        
        SwingUtilities.invokeLater(() -> {
            LogUtil.info("正在启动用户界面...");
            MainUI mainUI = new MainUI();
            
            mainUI.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    LogUtil.info("应用程序正在关闭...");
                }
            });
            
            LogUtil.info("应用程序启动完成");
        });
    }
}
