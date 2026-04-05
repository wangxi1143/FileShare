package main.java.com.fileshare.wx.init;

import main.java.com.fileshare.wx.util.LogUtil;
import main.java.com.fileshare.wx.util.FileUtil;

import java.io.File;
import java.nio.file.FileSystems;

public class FileInit {
    public static final String ROOT_PATH = System.getProperty("user.dir");
    public static final String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();

    private FileInit(){}

    public static void systemInit(){
        LogUtil.info("系统初始化开始...");
        LogUtil.fine("根路径：" + ROOT_PATH);
        LogUtil.fine("文件分隔符：" + FILE_SEPARATOR);
        
        File fi =new File(ROOT_PATH);
        FileUtil.creatFolderIfNotExit(fi,"log");
        LogUtil.config("日志目录创建完成");
        
        FileUtil.creatFolderIfNotExit(fi,"share");
        LogUtil.config("共享目录创建完成");
        
        LogUtil.info("系统初始化完成");
    }
}
