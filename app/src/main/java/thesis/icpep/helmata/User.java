package thesis.icpep.helmata;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class User extends AppCompatActivity {

    private String username;
    private TextView txtName;
    private Button edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_user);

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        username = prefs.getString("user", null);

        txtName = (TextView)findViewById(R.id.user1);
        txtName.setText(username);

        edit = (Button)findViewById(R.id.edit);

        if(txtName.getText().toString().length()>0 || txtName == null){
            edit.setEnabled(false);
        }

        edit = (Button)findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user();
            }
        });
    }

    public void user() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(User.this);
        View mview = getLayoutInflater().inflate(R.layout.add_ip, null);
        mBuilder.setView(mview);

        final EditText user = (EditText) mview.findViewById(R.id.user);

        final AlertDialog dialog = mBuilder.create();

        dialog.show();

        final Button edit = (Button) mview.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editorUser = getSharedPreferences("user", MODE_PRIVATE).edit();
                editorUser.putString("user", user.getText().toString());
                editorUser.apply();
                user.setEnabled(false);
                edit.setEnabled(false);
                dialog.dismiss();
                finish();
                startActivity(new Intent(User.this,User.class));
                Toasty.success(User.this, "Update Success", Toast.LENGTH_LONG).show();
            }
        });

    }
}
