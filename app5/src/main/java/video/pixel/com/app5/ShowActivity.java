package video.pixel.com.app5;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ShowActivity extends Activity {
    private ImageView mIamgeView;
    private String SERVICE_IP = "192.168.0.199";
    private final Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        SERVICE_IP = getIntent().getStringExtra("IP");mIamgeView = (ImageView) findViewById(R.id.iamgeView);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(SERVICE_IP, 7000);
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
