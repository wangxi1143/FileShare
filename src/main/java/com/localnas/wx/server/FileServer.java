package main.java.com.localnas.wx.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import main.java.com.localnas.wx.init.FileInit;
import main.java.com.localnas.wx.util.LogUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

public class FileServer {
    private static final int PORT = 8080;
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
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n")
                .append("<html><head>\n")
                .append("<meta charset='UTF-8'>\n")
                .append("<title>LocalNAS - 文件列表</title>\n")
                .append("<script>\n")
                .append("function deleteFile(encodedFileName, fileName) {\n")
                .append("  if (confirm('确定要删除文件 \"' + fileName + '\"？')) {\n")
                .append("    window.location.href = '/delete?file=' + encodedFileName;\n")
                .append("  }\n")
                .append("}\n")
                .append("</script>\n")
                .append("<style>\n")
                .append("body { font-family: Arial, sans-serif; margin: 20px; }\n")
                .append("h1 { color: #333; }\n")
                .append(".upload-form { margin: 20px 0; padding: 20px; background: #f5f5f5; border-radius: 5px; }\n")
                .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n")
                .append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n")
                .append("th { background-color: #4CAF50; color: white; }\n")
                .append("tr:hover { background-color: #f5f5f5; }\n")
                .append("a { color: #2196F3; text-decoration: none; }\n")
                .append("a:hover { text-decoration: underline; }\n")
                .append(".download-btn { background-color: #4CAF50; color: white; padding: 5px 10px; border-radius: 3px; }\n")
                .append("</style>\n")
                .append("</head><body>\n")
                .append("<h1>📁 LocalNAS 文件共享</h1>\n")
                .append("<div class='upload-form'>\n")
                .append("<h3>上传文件</h3>\n")
                .append("<form action='/upload' method='post' enctype='multipart/form-data'>\n")
                .append("<input type='file' name='file' id='file' required>\n")
                .append("<input type='submit' value='上传' style='margin-left: 10px; padding: 5px 15px; background-color: #4CAF50; color: white; border: none; border-radius: 3px; cursor: pointer;'>\n")
                .append("</form>\n")
                .append("</div>\n")
                .append("<h3>文件列表</h3>\n")
                .append("<table>\n")
                .append("<tr><th>文件名</th><th>大小</th><th>操作</th></tr>\n");
            
            File shareDir = new File(SHARE_DIR);
            if (shareDir.exists() && shareDir.isDirectory()) {
                File[] files = shareDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.isHidden()) {
                            String size = formatFileSize(file.length());
                            String encodedFileName = java.net.URLEncoder.encode(file.getName(), "UTF-8");
                            html.append("<tr>")
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
            
            html.append("</table>\n")
                .append("</body></html>");
            
            sendResponse(exchange, 200, html.toString(), "text/html; charset=UTF-8");
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
