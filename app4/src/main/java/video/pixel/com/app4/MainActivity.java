package video.pixel.com.app4;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 对应service2
 */
public class MainActivity extends AppCompatActivity {
    private ImageView mIamgeView;
    private String SERVICE_IP = "192.168.0.199";
    public static final int PORT = 7000;
    private final Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SERVICE_IP = getIntent().getStringExtra("SERVICE_IP");

        mIamgeView = (ImageView) findViewById(R.id.iamgeView);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(SERVICE_IP, PORT);
                    InputStream inputStream = socket.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mIamgeView.setImageBitmap(bitmap);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Socket异常", e);
                }
            }
        }, 40, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        timer.cancel();
    }
}
