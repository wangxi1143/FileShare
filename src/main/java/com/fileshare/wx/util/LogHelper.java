package main.java.com.fileshare.wx.util;

import java.util.logging.Logger;

public class LogHelper {
    private static final Logger log = Logger.getLogger(LogHelper.class.getName());
    
    public void exampleMethod() {
        log.info("这是一条信息日志");
        log.warning("这是一条警告日志");
        log.severe("这是一条错误日志");
        log.config("这是一条配置日志");
        log.fine("这是一条详细日志");
        
        // 带变量的日志输出
        String username = "admin";
        int count = 100;
        log.info(String.format("用户 %s 执行了操作，处理了 %d 条数据", username, count));
    }
}
