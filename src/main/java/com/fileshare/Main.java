package com.fileshare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public final static Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            //阻塞接受请求
            Socket socket = serverSocket.accept();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter pw = new PrintWriter(socket.getOutputStream())) {

                // 读取并打印所有请求头
                List<String> list = new LinkedList(){};
                String line;
                System.out.println("=== New Request ===");
                while ((line = br.readLine()) != null && !line.isEmpty()) {
                    list.add(line);
                }
                for (int i = 0; i < list.size(); i++) {
                    System.out.println(list.get(i));
                }
                System.out.println("=== End of Request ===\n");

                //解析请求头
                System.out.println(list.getFirst());
                String[] parts = list.getFirst().split(" ");
                if (parts.length >= 3) {
                    String method = parts[0];
                    String path = parts[1];
                    String protocol = parts[2];
                    System.out.println("Method: " + method);
                    System.out.println("Path: " + path);
                    System.out.println("Protocol: " + protocol);
                }
                String file =" ";
                String contentType = "text/html; charset=utf-8";  // 默认内容类型
                switch (parts[1]){
                    case "/" : file = "page/index.html"; break;
                    case "/page/css/index.css": file = "page/css/index.css"; contentType = "text/css; charset=utf-8"; break;
                    case "/page/js/index.js": file = "page/js/index.js"; contentType = "application/javascript; charset=utf-8"; break;
                    default: file = "page/404.html"; break;
                }

                // 读取 资源
                InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(file);
                StringBuilder response = new StringBuilder();
                try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String fileLine;
                    while ((fileLine = fileReader.readLine()) != null) {
                        response.append(fileLine);
                    }
                }

                // 构造HTTP响应
                String httpResponse = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Content-Length: " + response.toString().getBytes().length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        response;

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
}