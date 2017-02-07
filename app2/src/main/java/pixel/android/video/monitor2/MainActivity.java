package pixel.android.video.monitor2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * 显示图像
 */
public class MainActivity extends Activity {
    public static final int port = 6005;
    // 服务器地址
    private String service_ip = "";
    // 显示画面控件
    private ImageView mImageView;
    // 连接对象
    private Socket socket;
    // 轮询对象
    private Thread thread;
    // 开始标记
    private boolean action = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageView);
        service_ip = getIntent().getStringExtra("SERVICE_IP");

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (action) {
                    try {
                        if (socket == null || socket.isClosed()) {
                            socket = new Socket(service_ip, port);
                            InputStream inputStream = socket.getInputStream();
                            if (inputStream != null) {
                                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mImageView.setImageBitmap(bitmap);
                                    }
                                });
                                inputStream.close();
                                socket.close();
                                Thread.sleep(17);
                            } else {
                                Thread.sleep(1000);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity2", "初始化连接失败", e);
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        action = false;
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // 用数据装
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();
        return outstream.toByteArray();
    }
}
