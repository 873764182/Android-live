package com.example.service;

import java.awt.Graphics;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class VideoService {
    private static final int DEFAULT_WIDTH = 1280;   // 屏幕与画面的宽度 注意 要与客户端上传的图像宽度一致
    private static final int DEFAULT_HEIGHT = 720;   // 屏幕与画面的高度 注意 要与客户端上传的图像高度一致
    private static final int RECEIVE_PORT = 6000;   // 接收图像上传的端口
    private static final int DISTRIBUTED_PORT = 7000;   // 图像派发端口
    /* 图像的当前帧 */
    private static volatile ByteArrayOutputStream mByteArrayOutputStream;
    /* 显示图像的控件 */
    private static volatile VideoUI mVideoUI;

    /**
     * 程序主入口
     */
    public static void main(String args[]) throws IOException {
        mVideoUI = new VideoUI();
        new ReceiveThread().start();
        new DistributedThread().start();
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------------- 接收图像上传
     */
    private static class ReceiveThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;
            InputStream inputStream = null;
            try {
                ServerSocket serverSocket = new ServerSocket(RECEIVE_PORT);
                while (true) {
                    socket = serverSocket.accept();
                    inputStream = socket.getInputStream();
                    mByteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] tempByte = new byte[1024];
                    int readLength = 0;
                    while ((readLength = inputStream.read(tempByte)) != -1) {
                        mByteArrayOutputStream.write(tempByte, 0, readLength);
                    }
                    mByteArrayOutputStream.flush();

                    mVideoUI.refresh(); // TODO 显示图像在服务器窗口
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------------- 处理图像发布
     */
    private static class DistributedThread extends Thread {
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(DISTRIBUTED_PORT);
                while (true) {
                    Socket socket = serverSocket.accept();
                    new PushThread(socket).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class PushThread extends Thread {
        private Socket socket;

        public PushThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = new ByteArrayInputStream(mByteArrayOutputStream.toByteArray());
                byte[] tempByte = new byte[1024];
                int readLength = 0;
                outputStream = socket.getOutputStream();
                while ((readLength = inputStream.read(tempByte)) != -1) {
                    outputStream.write(tempByte, 0, readLength);
                }
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    outputStream.close();
                    inputStream.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------------- 生成一个显示图像的界面
     */
    private static class VideoUI extends JFrame {
        private ImageView imageView;

        public VideoUI() {
            this.setTitle("monitor screen");
            this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            this.setLocation(0, 0);
            this.setLayout(null);

            imageView = new ImageView();
            imageView.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            imageView.setLocation(0, 0);

            this.add(imageView);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setVisible(true);
        }

        public void refresh() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        imageView.update(ImageIO.read(new ByteArrayInputStream(mByteArrayOutputStream.toByteArray())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private static class ImageView extends JPanel {
        private Image image;

        public void update(Image image) {
            if (image != null) {
                this.image = image;
                this.repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            graphics.drawImage(image, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
        }
    }

}
