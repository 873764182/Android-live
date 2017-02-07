package pixel.android.video.monitor;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * 上传图像
 */
public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    // 预览视图
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    // 服务器地址
    private String service_ip = "";
    // 现在是否正在预览中
    private boolean isPreview = false;
    // 屏幕的宽高
    private int sWidth, sHeight;
    // 相机对象
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        sWidth = point.x;
        sHeight = point.y;

        service_ip = getIntent().getStringExtra("SERVICE_IP");
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();

        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {  // 创建
                if (!isPreview) {
                    camera = Camera.open();
                    if (camera != null) {
                        this.initCameraParameters(camera);

                        mSurfaceView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                initCameraParameters(camera);
                            }
                        });

                        isPreview = true;
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {   // 改变
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {    // 销毁
                if (camera != null) {
                    if (isPreview) camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }

            // 初始化相机参数
            private void initCameraParameters(Camera camera) {
                try {
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setPreviewSize(sWidth, sHeight);  // 设置预览图像的宽高
                    parameters.setPreviewFpsRange(16, 24);   // 每秒最低帧与最高帧
                    parameters.setPictureFormat(ImageFormat.NV21);  // 图片格式
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);    // 闪光灯模式
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);   // 连续性对焦

                    camera.cancelAutoFocus();
                    camera.setPreviewDisplay(mSurfaceHolder);   // 设置处理预览对象
                    camera.setPreviewCallback(new PreviewCallback(service_ip));   // 设置预览回调
                    camera.startPreview();  // 开始预览
                    camera.autoFocus(null);
                } catch (Exception e) {
                    Log.e(TAG, "相机初始化异常", e);
                }
            }

            // 设置预览画面方向
            private void setOrientation(Camera camera, int rotation) {
                if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
                    try {
                        Method downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
                        if (downPolymorphic != null) {
                            downPolymorphic.invoke(camera, new Object[]{rotation});
                        }
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "设置方向出错", e);
                    }
                } else {
                    camera.getParameters().setRotation(rotation);
                }
            }
        });
    }

    // 处理预览回调
    static class PreviewCallback implements Camera.PreviewCallback {
        private Thread thread;
        private String ip;

        public PreviewCallback(String service_ip) {
            this.ip = service_ip;
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            try {
                Camera.Size size = camera.getParameters().getPreviewSize();
                YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 10, outputStream);
                outputStream.flush();

                thread = new PushThread(outputStream, ip);
                thread.start();
            } catch (Exception e) {
                Log.e(TAG, "预览回调异常", e);
            }
        }
    }

    // 推送数据到服务器
    static class PushThread extends Thread {
        private static final int port = 6000;
        private final byte tempByte[] = new byte[1024];
        private ByteArrayOutputStream outputStream;
        private String ip;

        public PushThread(ByteArrayOutputStream outputStream, String ip) {
            this.outputStream = outputStream;
            this.ip = ip;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(ip, port);
                OutputStream os = socket.getOutputStream();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                int amout = 0;
                while ((amout = inputStream.read(tempByte)) != -1) {
                    os.write(tempByte, 0, amout);
                }
                outputStream.flush();
                outputStream.close();

                os.flush();
                os.close();

                inputStream.close();

                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "推流到服务器异常", e);
            }
        }
    }

}
