package com.fileshare;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Main {
    public final static Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        log.info("Server started on port " + port);
        InetAddress ip = (Inet4Address) InetAddress.getLocalHost();

        log.info("Server IPv4 address: " + ip.getHostAddress());

        //full url
        log.info("Full URL: http://" + ip.getHostAddress() + ":" + port);


        while (true) {
            //阻塞接受请求
            Socket socket = serverSocket.accept();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter pw = new PrintWriter(socket.getOutputStream())) {

                // 读取所有请求头
                List<String> list = new LinkedList<>();
                String line;
                while ((line = br.readLine()) != null && !line.isEmpty()) {
                    list.add(line);
                }

                //解析请求头
                String[] parts = list.getFirst().split(" ");
                String sharePath = "share";
                String pageFilePath = " ";
                String contentType = "text/html; charset=utf-8";  // 默认内容类型
                switch (parts[1]) {
                    case "/":
                        pageFilePath = "page/index.html";
                        break;
                    case "/css/index.css":
                        pageFilePath = "page/css/index.css";
                        contentType = "text/css; " + "charset=utf-8";
                        break;
                    case "/js/index.js":
                        pageFilePath = "page/js/index.js";
                        contentType = "application/javascript; " + "charset=utf-8";
                        break;
                    case "/api/list?path=/" :
                        String json = getFileList(sharePath);
                        if (json == null) break;
                        StringBuilder responseBody = new StringBuilder();
                        responseBody.append(json);
                        contentType = "application/json; " + "charset=utf-8";
                        String httpResponse = buildHttpResponse(contentType, responseBody);

                        // 发送响应
                        pw.write(httpResponse);
                        pw.flush();
                        continue;
                    default:
                        pageFilePath = "page/404.html";
                        break;
                }


                StringBuilder response = readResoutceFile(pageFilePath);

                String httpResponse = buildHttpResponse(contentType, response);

                // 发送响应
                pw.write(httpResponse);
                pw.flush();

            } catch (IOException e) {
                log.error("Error handling request", e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error("Error closing socket", e);
                }
            }
        }
    }

    private static String getFileList(String sharePath) throws JsonProcessingException {
        File file = new File(sharePath);
        if (!file.isDirectory()) {
            return null;
        }
        Map<String, Object> outerMap = new java.util.HashMap<>();
        List<Map<String, String>> fileMapList = new java.util.ArrayList<>();
        for (int i = file.listFiles().length - 1; i >= 0; i--) {
            Map<String,String> fileMap = new java.util.HashMap<>();
            fileMap.put(file.listFiles()[i].getName(), file.listFiles()[i].isDirectory() ? "folder" : "file");
            fileMapList.add(fileMap);
        }
        outerMap.put("list", fileMapList);
        outerMap.put("total", fileMapList.size());
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(outerMap);
        return json;
    }

    private static StringBuilder readResoutceFile(String pageFilePath) throws IOException {
        // 读取 资源
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(pageFilePath);
        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String fileLine;
            while ((fileLine = fileReader.readLine()) != null) {
                responseBody.append(fileLine);
            }
        }
        return responseBody;
    }

    private static String buildHttpResponse(String contentType, StringBuilder responseBody) {
        // 构造HTTP响应
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + responseBody.toString().getBytes().length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                responseBody;
        return httpResponse;
    }
}