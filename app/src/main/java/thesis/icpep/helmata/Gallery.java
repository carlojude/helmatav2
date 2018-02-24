package thesis.icpep.helmata;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class Gallery extends ListActivity {

    ListView listView;
    ArrayList<String> arrayList;
    ArrayAdapter<String> mAdapter;
    private static  final int MY_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_gallery);

        if(ContextCompat.checkSelfPermission(Gallery.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(Gallery.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(Gallery.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(Gallery.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSION_REQUEST);
            }
        } else {
            getFiles();
        }

        listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = String.valueOf(parent.getItemAtPosition(position));
                Intent i = new Intent(Gallery.this, Player.class);
                i.putExtra("video", item);
                startActivity(i);
//                Toast.makeText(getApplicationContext(),
//                        item, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(Gallery.this, MainActivity.class);
        startActivity(intent);
    }

    public void getFiles(){
        File f = new File(Environment.getExternalStorageDirectory(), "Helmata");
        if(!f.exists()){

        } else {
            File[] vid = f.listFiles();
            arrayList = new ArrayList<>();

            for (int i = 0; i < vid.length; i++)
            {
                arrayList.add(vid[i].getName());
                mAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, arrayList);
                getListView().setAdapter(mAdapter);
            }
        }
    }
}
