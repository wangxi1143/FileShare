package main.java.com.fileshare.wx.util;

import java.io.File;

public class FileUtil {

    private FileUtil(){}

    public static void creatFolderIfNotExit(File file, String fileName){
        File fi = new File(file, fileName);
        
        if (fi.exists() && !fi.isDirectory()) {
            fi.delete();
        }
        
        if (!fi.exists()) {
            fi.mkdir();
        }
    }
}
