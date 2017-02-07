package video.pixel.com.app5;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class LoginActivity extends Activity {
    private EditText mEditText;
    private RadioButton mRadioButton0;
    private RadioButton mRadioButton1;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEditText = (EditText) findViewById(R.id.editText);
        mRadioButton0 = (RadioButton) findViewById(R.id.radioButton0);
        mRadioButton1 = (RadioButton) findViewById(R.id.radioButton1);
        mButton = (Button) findViewById(R.id.button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = mEditText.getText().toString().trim();
                if (ip.length() <= 0) {
                    Toast.makeText(LoginActivity.this, "服务器地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("IP", ip);
                if (mRadioButton0.isChecked()) {
                    intent.setClass(LoginActivity.this, ShowActivity.class);
                    startActivity(intent);
                } else if (mRadioButton1.isChecked()) {
                    intent.setClass(LoginActivity.this, PushActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "异常", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 0x99);
        }
    }
}
