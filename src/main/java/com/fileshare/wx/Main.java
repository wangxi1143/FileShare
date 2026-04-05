package main.java.com.fileshare.wx;

import main.java.com.fileshare.wx.init.FileInit;
import main.java.com.fileshare.wx.server.NoGUIServer;
import main.java.com.fileshare.wx.ui.MainUI;
import main.java.com.fileshare.wx.util.ArgsParser;
import main.java.com.fileshare.wx.util.LogUtil;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

public class Main {
    private static Map<String, Object> arg;
    public static void main(String[] args) {
        arg = ArgsParser.argsParser(args);
        FileInit.systemInit();

        if (arg.containsKey("no-gui") && (boolean) arg.get("no-gui")){
            noGUIOption();
        }else {
            GUIOption();
        }


    }

    private static void noGUIOption(){
        try {
            new NoGUIServer(arg);
            synchronized (Main.class) {
                Main.class.wait();
            }
        } catch (InterruptedException e) {
            LogUtil.info("无GUI模式被中断");
            Thread.currentThread().interrupt();
        }
    }
    private static void GUIOption(){
        try {
            SwingUtilities.invokeAndWait(() -> {
                LogUtil.info("正在启动界面...");
                MainUI mainUI = new MainUI(arg);

                mainUI.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        LogUtil.info("应用程序正在关闭...");
                    }
                });

                LogUtil.info("应用程序启动完成");
            });
            synchronized (Main.class) {
                Main.class.wait();
            }
        } catch (Exception e) {
            LogUtil.info("GUI启动失败，切换到无GUI模式: " + e.getMessage());
            noGUIOption();
        }
    }
}

