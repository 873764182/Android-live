package pixel.android.video.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by pixel on 2017/1/13.
 */

public class VideoPushService {
    public static final int port = 6005;

    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        new PushThread(socket).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    static class PushThread extends Thread {
        private byte[] buffer = new byte[1024];
        private Socket socket;

        public PushThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = VideoMoitorService.getmInputStream();
                int amout = 0;
                while ((amout = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, amout);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println(" === VideoPushService Receive the connection === ");
            }
        }
    }

}
