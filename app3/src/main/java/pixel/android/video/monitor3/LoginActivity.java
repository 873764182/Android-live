package pixel.android.video.monitor3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
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
    }
}
