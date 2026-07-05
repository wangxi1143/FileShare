package com.fileshare.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInit {
    public final static Logger log = LogManager.getLogger(FileInit.class);
    public static String path = "share/";
    private FileInit(){}
    public static  void init() {
        log.info("Initaial File Directory");
        fileExistCheakAndCreat();
        if (Files.isDirectory(Path.of(path))) {
            log.info("directory is exists");
            return;
        }
        log.info("Share is not a directory");
        try {
            log.info("try to delete file and create directory");
            Files.delete(Path.of(path));
            log.info("delete success");
            Files.createDirectories(Path.of(path));
            log.info("creat success");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }

    private static void fileExistCheakAndCreat() {
        if(!Files.exists(Path.of(path))){
            try {
                log.info("Not find share directory,try to creat.");
                Files.createDirectories(Path.of(path));
                log.info("Creat success.");
            } catch (IOException e) {
                log.info("Failed creat share dircetcory,System will exit.");
                throw new RuntimeException(e);
            }
        }
    }
}
