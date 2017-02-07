package pixel.android.video.monitor3;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

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
    // 开始标记
    private boolean action = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageView);
        service_ip = getIntent().getStringExtra("SERVICE_IP");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(service_ip, port);
                    while (action) {
                        // TODO 服务器是一帧一帧推送的 但是客户端不知道怎么区分帧 所以得到的图片是乱的
                        InputStream inputStream = socket.getInputStream();
                        if (inputStream != null) {
                            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (bitmap != null) mImageView.setImageBitmap(bitmap);
                                }
                            });
                        } else {
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    Log.e("MainActivity2", "初始化连接失败", e);
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        action = false;
    }
}
