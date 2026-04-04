package main.java.com.localnas.wx.util;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.*;

public class LogUtil {
    private static final Logger logger = Logger.getLogger(LogUtil.class.getName());
    private static LogAppender logAppender;
    private static RollingConsole console;

    static {
        initLogger();
    }

    private static void initLogger() {
        logger.setUseParentHandlers(false);
        
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        console = new RollingConsole();
        
        logAppender = new LogAppender();

        try {
            String logPath = System.getProperty("user.dir") + System.getProperty("file.separator") + "log" + System.getProperty("file.separator") + "server.log";
            FileHandler fileHandler = new FileHandler(logPath, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.addHandler(logAppender);
        
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);
        
        logger.setLevel(Level.ALL);
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warning(String message) {
        logger.warning(message);
    }

    public static void severe(String message) {
        logger.severe(message);
    }

    public static void config(String message) {
        logger.config(message);
    }

    public static void fine(String message) {
        logger.fine(message);
    }

    public static void finer(String message) {
        logger.finer(message);
    }

    public static void finest(String message) {
        logger.finest(message);
    }

    public static Logger getLogger() {
        return logger;
    }

    /**
     * 滚动控制台 - 在终端中滚动显示日志
     */
    static class RollingConsole extends OutputStream {
        private static final int MAX_LINES = 100;
        private java.util.List<String> lines = new java.util.ArrayList<>();
        
        @Override
        public void write(int b) throws IOException {
            synchronized(this) {
                StringBuilder sb = new StringBuilder();
                sb.append((char)b);
                processLine(sb.toString());
                System.out.write(b);
            }
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            String text = new String(b, off, len);
            processLine(text);
            System.out.write(b, off, len);
        }
        
        private void processLine(String text) {
        }
    }

    /**
     * 自定义 Handler，用于将日志输出到 UI
     */
    static class LogAppender extends Handler {
        public LogAppender() {
            setFormatter(new SimpleFormatter());
        }
        
        @Override
        public void publish(LogRecord record) {
            if (logArea != null) {
                String message = getFormatter().format(record);
                SwingUtilities.invokeLater(() -> {
                    logArea.append(message);
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    private static JTextArea logArea;

    public static void setLogTextArea(JTextArea logArea) {
        LogUtil.logArea = logArea;
    }
}
