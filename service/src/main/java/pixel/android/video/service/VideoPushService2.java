package pixel.android.video.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by pixel on 2017/1/13.
 */

public class VideoPushService2 {
    public static final int port = 6005;
    public static final Map<String, Socket> socketMap = new Hashtable<>();

    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        socketMap.put(socket.getInetAddress().getHostAddress(), socket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void push(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        for (Map.Entry<String, Socket> map : socketMap.entrySet()) {
            OutputStream outputStream = map.getValue().getOutputStream();
            int amout = 0;
            while ((amout = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, amout);
            }
            outputStream.flush();
            // outputStream.close();    // 关闭输出流会关闭连接
            inputStream.close();
        }
    }

}
