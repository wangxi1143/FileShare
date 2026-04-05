package main.java.com.fileshare.wx.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import main.java.com.fileshare.wx.init.FileInit;
import main.java.com.fileshare.wx.util.LogUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class FileServer {
    private static int PORT = 8080;
    private static final String SHARE_DIR = FileInit.ROOT_PATH + FileInit.FILE_SEPARATOR + "share";
    private HttpServer server;


    
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        server.createContext("/", new FileHandler());
        
        server.createContext("/upload", new UploadHandler());
        
        server.createContext("/delete", new DeleteHandler());
        
        server.setExecutor(null);
        server.start();
        
        LogUtil.info("文件服务器启动在端口：" + PORT);
        LogUtil.info("访问地址：http://<你的 IP>:" + PORT);
        LogUtil.info("共享目录：" + SHARE_DIR);
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            LogUtil.info("文件服务器已停止");
        }
    }
    
    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            
            if ("GET".equals(method)) {
                if (path.equals("/")) {
                    listFiles(exchange);
                } else {
                    downloadFile(exchange, path);
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }
        
        private void listFiles(HttpExchange exchange) throws IOException {
            // 读取 HTML 模板
            String htmlTemplate = loadHtmlTemplate();
            
            // 生成文件列表 HTML
            StringBuilder fileListHtml = new StringBuilder();
            File shareDir = new File(SHARE_DIR);
            if (shareDir.exists() && shareDir.isDirectory()) {
                File[] files = shareDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.isHidden()) {
                            String size = formatFileSize(file.length());
                            String encodedFileName = java.net.URLEncoder.encode(file.getName(), "UTF-8");
                            fileListHtml.append("<tr>")
                                .append("<td>").append(escapeHtml(file.getName())).append("</td>")
                                .append("<td>").append(size).append("</td>")
                                .append("<td>")
                                .append("<a href='").append(file.getName()).append("' class='download-btn' download style='margin-right: 5px;'>下载</a>")
                                .append("<button onclick=\"deleteFile('").append(encodedFileName).append("', '").append(escapeHtml(file.getName())).append("')\" style='background-color: #f44336; color: white; padding: 5px 10px; border: none; border-radius: 3px; cursor: pointer;'>删除</button>")
                                .append("</td>")
                                .append("</tr>\n");
                        }
                    }
                }
            }
            
            // 替换占位符
            String html = htmlTemplate.replace("{{FILE_LIST}}", fileListHtml.toString());
            
            sendResponse(exchange, 200, html, "text/html; charset=UTF-8");
        }
        
        private String loadHtmlTemplate() throws IOException {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("index.html");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } catch (Exception e) {
                LogUtil.info("加载 HTML 模板失败：" + e.getMessage());
                throw new IOException("Failed to load HTML template", e);
            }
        }
        
        private void downloadFile(HttpExchange exchange, String path) throws IOException {
            String fileName = path.substring(1);
            
            fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            
            if (fileName.contains("..") || fileName.startsWith("/") || fileName.startsWith("\\")) {
                LogUtil.info("非法的文件名：" + fileName);
                sendResponse(exchange, 400, "Invalid file name");
                return;
            }
            
            File file = new File(SHARE_DIR, fileName);
            
            if (!file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "File Not Found");
                return;
            }
            
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(file.getName(), "UTF-8"));
            exchange.getResponseHeaders().set("Content-Length", String.valueOf(file.length()));
            exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
            
            exchange.sendResponseHeaders(200, file.length());
            
            try (OutputStream os = exchange.getResponseBody();
                 FileInputStream fis = new FileInputStream(file)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                    os.flush();
                }
            } catch (Exception e) {
                LogUtil.info("文件下载出错：" + e.getMessage());
                e.printStackTrace();
            }
        }
        
        private String formatFileSize(long size) {
            if (size < 1024) {
                return size + " B";
            } else if (size < 1024 * 1024) {
                return String.format("%.2f KB", size / 1024.0);
            } else if (size < 1024 * 1024 * 1024) {
                return String.format("%.2f MB", size / (1024.0 * 1024.0));
            } else {
                return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
            }
        }
        
        private String escapeHtml(String input) {
            if (input == null) return "";
            return input.replace("&", "&amp;")
                       .replace("<", "&lt;")
                       .replace(">", "&gt;")
                       .replace("\"", "&quot;")
                       .replace("'", "&#39;");
        }
        
        private void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
            sendResponse(exchange, code, message, "text/plain; charset=UTF-8");
        }
        
        private void sendResponse(HttpExchange exchange, int code, String message, String contentType) throws IOException {
            byte[] responseBytes = message.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(code, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
    
    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            if (!"POST".equals(method)) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                Map<String, List<String>> headers = exchange.getRequestHeaders();
                String contentType = headers.get("Content-Type") != null ? headers.get("Content-Type").get(0) : null;
                
                if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                    sendResponse(exchange, 400, "Invalid content type");
                    return;
                }
                
                InputStream is = exchange.getRequestBody();
                byte[] body = is.readAllBytes();
                
                String boundary = contentType.split("boundary=")[1];
                if (boundary.startsWith("\"") || boundary.startsWith("'")) {
                    boundary = boundary.substring(1, boundary.length() - 1);
                }
                boundary = "--" + boundary;
                
                String bodyString = new String(body, "UTF-8");
                int boundaryStart = bodyString.indexOf(boundary);
                int boundaryEnd = bodyString.lastIndexOf(boundary);
                
                if (boundaryStart == -1 || boundaryEnd == -1) {
                    sendResponse(exchange, 400, "Invalid multipart data");
                    return;
                }
                
                int filenameStartIndex = bodyString.indexOf("filename=\"");
                if (filenameStartIndex == -1) {
                    sendResponse(exchange, 400, "No file found");
                    return;
                }
                
                filenameStartIndex += 10;
                int filenameEndIndex = bodyString.indexOf("\"", filenameStartIndex);
                String filename = bodyString.substring(filenameStartIndex, filenameEndIndex);
                
                if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                    sendResponse(exchange, 400, "Invalid file name");
                    return;
                }
                
                int contentStartIndex = bodyString.indexOf("\r\n\r\n", filenameEndIndex) + 4;
                
                int contentEndIndex = bodyString.lastIndexOf(boundary) - 4;
                
                byte[] fileContent = bodyString.substring(contentStartIndex, contentEndIndex).getBytes("UTF-8");
                
                File outputFile = new File(SHARE_DIR, filename);
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(fileContent);
                }
                
                LogUtil.info("文件上传成功：" + filename + " (" + fileContent.length + " bytes)");
                
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                
            } catch (Exception e) {
                LogUtil.info("文件上传失败：" + e.getMessage());
                sendResponse(exchange, 500, "Upload failed: " + e.getMessage());
            }
        }
        
        private void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
            byte[] responseBytes = message.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(code, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
    
    static class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            if (!"GET".equals(method)) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            
            try {
                String query = exchange.getRequestURI().getQuery();
                if (query == null || !query.startsWith("file=")) {
                    sendResponse(exchange, 400, "Invalid request");
                    return;
                }
                
                String fileName = query.substring(5);
                
                fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
                
                if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                    sendResponse(exchange, 400, "Invalid file name");
                    return;
                }
                
                File file = new File(SHARE_DIR, fileName);
                
                if (!file.exists() || file.isDirectory()) {
                    sendResponse(exchange, 404, "File Not Found");
                    return;
                }
                
                if (file.delete()) {
                    LogUtil.info("文件删除成功：" + fileName);
                    exchange.getResponseHeaders().set("Location", "/");
                    exchange.sendResponseHeaders(303, -1);
                } else {
                    LogUtil.info("文件删除失败：" + fileName);
                    sendResponse(exchange, 500, "Failed to delete file");
                }
                
            } catch (Exception e) {
                LogUtil.info("文件删除失败：" + e.getMessage());
                sendResponse(exchange, 500, "Delete failed: " + e.getMessage());
            }
        }
        
        private void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
            byte[] responseBytes = message.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(code, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}
