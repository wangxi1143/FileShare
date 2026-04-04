package main.java.com.localnas.wx;

import main.java.com.localnas.wx.init.FileInit;
import main.java.com.localnas.wx.ui.MainUI;
import main.java.com.localnas.wx.util.LogUtil;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        LogUtil.info("应用程序启动");
        LogUtil.config("正在初始化系统组件...");
        
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
