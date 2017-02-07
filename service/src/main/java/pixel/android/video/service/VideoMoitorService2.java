package pixel.android.video.service;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class VideoMoitorService2 {
    public static final int DEFAULT_WIDTH = 1280;   // 屏幕与画面的宽度 注意 要与客户端上传的图像宽度一致
    public static final int DEFAULT_HEIGHT = 720;   // 屏幕与画面的高度 注意 要与客户端上传的图像高度一致
    public static final int port = 6000;            // 图像上传的监听端口
    private static ServerSocket serverSocket;

    private static VideoPushService2 videoPushService;

    public static void main(String args[]) throws IOException {

        videoPushService = new VideoPushService2();
        videoPushService.init();

        serverSocket = new ServerSocket(port);

        ImageFrame imageFrame = new ImageFrame(serverSocket);
        imageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        imageFrame.setVisible(true);

        while (true) {
            imageFrame.imagePanel.initImageView();
            imageFrame.repaint();   // 重新绘制
        }
    }

    static class ImageFrame extends JFrame {
        public int screenWidth, screenHeight;   // 屏幕宽高
        public ImagePanel imagePanel;

        public ImageFrame(ServerSocket serverSocket) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension dimension = toolkit.getScreenSize();
            screenWidth = dimension.width;
            screenHeight = dimension.height;
            System.out.println("屏幕宽高: " + screenWidth + "\t" + screenHeight);

            this.setTitle("monitor screen");   // 设置标题
            this.setLocation((screenWidth - DEFAULT_WIDTH / 2) / 2, (screenHeight - DEFAULT_HEIGHT / 2) / 2);    // 设置窗口显示位置
            this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT); // 设置窗口宽高
            this.getContentPane().setLayout(null);

            imagePanel = new ImagePanel(serverSocket);
            imagePanel.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            imagePanel.setLocation(0, 0);

            this.add(imagePanel);
        }
    }

    static class ImagePanel extends JPanel {
        private ServerSocket serverSocket;
        private InputStream inputStream;
        private Image image;

        public ImagePanel(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        protected void paintComponent(Graphics graphics) {  // 调用repaint()时这个方法会调用
            super.paintComponent(graphics);

            if (image != null) {
                graphics.drawImage(image, 0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
            }
        }

        public void initImageView() throws IOException {
            Socket localSocket = serverSocket.accept();
            this.inputStream = localSocket.getInputStream();

            // 复制流数据
            final ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                mByteArrayOutputStream.write(buffer, 0, len);
            }
            mByteArrayOutputStream.flush();

            // 转发
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        videoPushService.push(new ByteArrayInputStream(mByteArrayOutputStream.toByteArray()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            this.inputStream.close(); // 已经复制了一份输入流 这里可以关闭
            this.image = ImageIO.read(new ByteArrayInputStream(mByteArrayOutputStream.toByteArray()));
            localSocket.close();
        }
    }
}
