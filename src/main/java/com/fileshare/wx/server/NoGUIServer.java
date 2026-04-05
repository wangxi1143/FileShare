package main.java.com.fileshare.wx.server;

import main.java.com.fileshare.wx.util.LogUtil;

import java.io.IOException;
import java.util.Map;

public class NoGUIServer {
    private FileServer fileServer;
    
    public NoGUIServer(Map<String, Object> args) {
        LogUtil.info("=== FileShareServer 启动 (无GUI模式) ===");
        LogUtil.info("日志系统已初始化");
        LogUtil.config("无GUI模式已启用");
        
        startFileServer();
    }
    
    private void startFileServer() {
        try {
            fileServer = new FileServer();
            fileServer.start();
            LogUtil.info("文件服务器已成功启动");
            LogUtil.info("按 Ctrl+C 停止服务器");
        } catch (IOException e) {
            LogUtil.info("启动文件服务器失败：" + e.getMessage());
            System.exit(1);
        }
    }
    
    public void stop() {
        if (fileServer != null) {
            fileServer.stop();
            LogUtil.info("文件服务器已停止");
        }
    }
}
