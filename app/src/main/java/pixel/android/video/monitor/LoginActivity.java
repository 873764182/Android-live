package pixel.android.video.monitor;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private EditText mEditText;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEditText = (EditText) findViewById(R.id.editText);
        mButton = (Button) findViewById(R.id.button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = mEditText.getText().toString().replace(" ", "");
                if (ip.length() <= 0) {
                    Toast.makeText(LoginActivity.this, "输入服务器地址", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("SERVICE_IP", ip);
                startActivity(intent);
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 0x99);

    }
}
