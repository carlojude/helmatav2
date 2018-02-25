package thesis.icpep.helmata;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String path;
    ArrayList<String> list = new ArrayList<String>();
    File[] listFile;
    Bitmap bitmap;
    public  final int PLAY_VIDEO = 1;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set window fullscreen
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GridView gridView = (GridView)findViewById(R.id.gridView);
        gridView.setAdapter(new ImageAdapter(this));

        //set app directory
        File f = new File(Environment.getExternalStorageDirectory(), "Helmata");
        if (!f.exists()) {
            f.mkdirs();
            path = f.getAbsolutePath();
            SharedPreferences.Editor editor = getSharedPreferences("FilePath", MODE_PRIVATE).edit();
            editor.putString("filePath", path);
            editor.apply();
        }

        //store path to array
        if (f.isDirectory())
        {
            listFile = f.listFiles();
            for (int i = 0; i < listFile.length; i++)
            {
                list.add(listFile[i].getAbsolutePath());
            }
        }


        //        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        GridView grid = (GridView)findViewById(R.id.gridView);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                Uri data = Uri.parse(list.get(position));
//                intent.setDataAndType(data,"video/*");
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(intent);
//                }
//                startActivityForResult(Intent.createChooser(intent,
//
//                        "Play Video"), PLAY_VIDEO);

                final String item = list.get(position);

//                Toast.makeText(MainActivity.this, list.get(position), Toast.LENGTH_SHORT).show();
                final File getFileName = new File(Environment.getExternalStorageDirectory(), "Helmata/" + item);
                final String path = getFileName.getPath();
                String filename= path.substring(path.lastIndexOf("/")+1);

                final File theFilePath = new File(Environment.getExternalStorageDirectory(), "Helmata");
                final File theFile = new File(Environment.getExternalStorageDirectory(), "Helmata/" + filename);

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                builder.setTitle(filename);
//                builder.setSingleChoiceItems(items, -1,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int item) {
//                                Toast.makeText(getApplicationContext(), items[item],
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        });

                builder.setPositiveButton("Play", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent;

                        verifyStoragePermissions(MainActivity.this);
                        File f = new File(Environment.getExternalStorageDirectory(), "Helmata/");
                        Uri uri = Uri.parse(item);

                        intent = new Intent(MainActivity.this, Player.class);
                        intent.putExtra(Player.VIDEO_TYPE, Player.SIMPLE_VIDEO);
                        intent.putExtra(Player.VIDEO_URI, "file:///" + uri);
                        startActivity(intent);
//                        Intent i = new Intent(MainActivity.this, Player.class);
//                        i.putExtra("video", item);
//                        startActivity(i);
                    }
                });

                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(theFilePath.exists()){
                            theFile.delete();
                            finish();
                            startActivity(new Intent(MainActivity.this,MainActivity.class));
                            Toasty.success(MainActivity.this, "Successfully Deleted", Toast.LENGTH_LONG).show();
                        } else {
                            String filename= path.substring(path.lastIndexOf("/")+1);
                            Toasty.error(MainActivity.this, "File does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                android.app.AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            ip();
        } else if (id == R.id.nav_online) {
            startActivity(new Intent(MainActivity.this,Online.class));
        } else if (id == R.id.nav_offline) {
            finish();
            startActivity(new Intent(MainActivity.this,Offline.class));
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_gallery) {
            startActivity(new Intent(MainActivity.this,Gallery.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void ip(){
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Manage IP");
        builder.setMessage("Enter IP Camera's IP Address");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input); // uncomment this line

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SharedPreferences.Editor editor = getSharedPreferences("IPADD", MODE_PRIVATE).edit();
                editor.putString("ip", input.getText().toString());
                editor.apply();
                Toasty.success(MainActivity.this, input.getText().toString(), Toast.LENGTH_SHORT);
            }
        });

        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    //get ip from user or edit ip
    public void getIp(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mview = getLayoutInflater().inflate(R.layout.add_ip, null);

        final EditText ip_add = (EditText)mview.findViewById(R.id.txtIp);
        Button confirm = (Button)mview.findViewById(R.id.add);
        Button cancel = (Button)mview.findViewById(R.id.cancel);

        mBuilder.setView(mview);

        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        //set ip dialog
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("IPADD", MODE_PRIVATE).edit();
                editor.putString("ip", ip_add.getText().toString());
                editor.apply();
                dialog.dismiss();
            }
        });

        //close dialog
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private class ImageAdapter extends BaseAdapter {

        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                if(list.get(position).contains(".mp4"))
                {
                    //create thumbnail of video
                    bitmap = ThumbnailUtils.createVideoThumbnail(list.get(position), 0);
                }
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(350, 350));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
                imageView.setImageBitmap(bitmap);
            } else {
                imageView = (ImageView) convertView;
            }
            return imageView;
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
