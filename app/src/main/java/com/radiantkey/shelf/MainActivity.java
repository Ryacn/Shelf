package com.radiantkey.shelf;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.codekidlabs.storagechooser.StorageChooser;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.listeners.OnDismissListener;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    GridView gridView;
    TextView textView;

    ShelfAdapter adapter;

    StfalconImageViewer st;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        gridView = (GridView) findViewById(R.id.shelf_grid);
        textView = (TextView) findViewById(R.id.grid_textview);
        SharedPreferences sharedPreferences = getSharedPreferences("shelf_info", 0);
        String shelf_path = sharedPreferences.getString("shelf_location", "").toString();
        if(shelf_path != ""){
            textView.setVisibility(View.GONE);
            setupGridView(shelf_path);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(gridView.getAdapter() instanceof BookAdapter) {
            gridView.setAdapter(adapter);
            textView.setVisibility(View.GONE);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    File item = (File) adapter.getItem(i);
                    setupImageView(item.getAbsolutePath());
                }
            });
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
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Toast.makeText(MainActivity.this, "Permission Required", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            StorageChooser chooser = new StorageChooser.Builder()
                    .withActivity(MainActivity.this)
                    .withFragmentManager(getFragmentManager())
                    .withMemoryBar(true)
                    .allowCustomPath(true)
                    .setType(StorageChooser.DIRECTORY_CHOOSER)
                    .build();
            chooser.show();
            chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                @Override
                public void onSelect(String path) {
                    setupGridView(path);
                    SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("shelf_info", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("shelf_location", path).commit();
                }
            });
        } else if (id == R.id.nav_gallery) {
            StorageChooser chooser = new StorageChooser.Builder()
                    .withActivity(MainActivity.this)
                    .withFragmentManager(getFragmentManager())
                    .withMemoryBar(true)
                    .allowCustomPath(true)
                    .setType(StorageChooser.DIRECTORY_CHOOSER)
                    .build();
            chooser.show();
            chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                @Override
                public void onSelect(String path) {
                    setupImageView(path);
                }
            });
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setupGridView(String path){
        File directory = new File(path);
        File[] files = directory.listFiles();
        List<File> book_files = new ArrayList<File>();
        for(File file : files){
            if(file.isDirectory()){
                book_files.add(file);
            }
        }
        adapter = new ShelfAdapter(MainActivity.this, book_files);
        gridView.setAdapter(adapter);
        textView.setVisibility(View.GONE);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File item = (File) adapter.getItem(i);
                setupImageView(item.getAbsolutePath());
            }
        });
    }


    public void setupImageView(String path){
        File directory = new File(path);
        File[] image_files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if(s.toLowerCase().endsWith(".jpg") || s.toLowerCase().endsWith(".png")){
                    Log.e(s,s);
                    return true;
                }else{
                    return false;
                }
            }
        });
        if(image_files.length != 0) {
            final BookAdapter adapter = new BookAdapter(MainActivity.this, image_files);
            gridView.setAdapter(adapter);
            textView.setVisibility(View.GONE);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    st = new StfalconImageViewer.Builder<>(MainActivity.this, adapter.getImage_files(), new ImageLoader<File>() {
                        @Override
                        public void loadImage(ImageView imageView, File image_file) {
                            //load your image here
                            Picasso.get().load(image_file).into(imageView);
                        }
                    }).withTransitionFrom((ImageView) view.findViewById(R.id.shelf_gridcell_imageview)).withDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            falconDismiss();
                        }
                    }).withStartPosition(i).show();
                }
            });
            DatabaseHelper db = new DatabaseHelper(MainActivity.this);
            Cursor cursor = db.getData(directory.getAbsolutePath());
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                if(cursor.getInt(0) < adapter.getCount()) {
                    st = new StfalconImageViewer.Builder<>(MainActivity.this, adapter.getImage_files(), new ImageLoader<File>() {
                        @Override
                        public void loadImage(ImageView imageView, File image_file) {
                            //load your image here
                            Picasso.get().load(image_file).into(imageView);
                        }
                    }).withStartPosition(cursor.getInt(0)).show();
                }
            }
        }else{
            Toast.makeText(MainActivity.this, "Directory does not contain image", Toast.LENGTH_SHORT).show();
        }
    }
    public void falconDismiss(){
        if(st != null) {
            DatabaseHelper db = new DatabaseHelper(MainActivity.this);
            try {
                db.insertData(((File) gridView.getAdapter().getItem(st.currentPosition())).getParent(), st.currentPosition());
            } catch (SQLiteConstraintException e) {
                db.updatePosition(((File) gridView.getAdapter().getItem(st.currentPosition())).getParent(), st.currentPosition());
            }
        }
    }

    public class BookAdapter extends BaseAdapter{
        private Context context;
        private File[] image_files;

        public File[] getImage_files() {
            return image_files;
        }

        public BookAdapter(Context context, File[] image_files) {
            this.context = context;
            this.image_files = image_files;
        }

        @Override
        public int getCount() {
            return image_files.length;
        }

        @Override
        public Object getItem(int i) {
            return image_files[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            File book_file = image_files[i];
            final ImageView imageView;
            final TextView textView;
            if(view == null){
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                view = layoutInflater.inflate(R.layout.shelf_gridcell_layout, viewGroup, false);
            }
            imageView = (ImageView) view.findViewById(R.id.shelf_gridcell_imageview);
            textView = (TextView) view.findViewById(R.id.shelf_gridcell_textview);
            Picasso.get().load(book_file).resize(gridView.getColumnWidth(),gridView.getColumnWidth()).into(imageView);
            textView.setText(book_file.getName());
            return view;
        }
    }

    public class ShelfAdapter extends BaseAdapter{

        private Context context;
        private List<File> book_files;

        public ShelfAdapter(Context context, List<File> book_files) {
            this.context = context;
            this.book_files = book_files;
        }

        @Override
        public int getCount() {
            return book_files.size();
        }

        @Override
        public Object getItem(int i) {
            return book_files.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            File book_file = book_files.get(i);
            final ImageView imageView;
            final TextView textView;
            if(view == null){
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                view = layoutInflater.inflate(R.layout.shelf_gridcell_layout, viewGroup, false);
            }
            imageView = (ImageView) view.findViewById(R.id.shelf_gridcell_imageview);
            textView = (TextView) view.findViewById(R.id.shelf_gridcell_textview);
            File[] image_files = book_file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    if(s.toLowerCase().endsWith(".jpg") || s.toLowerCase().endsWith(".png")){
                        Log.e(s,s);
                        return true;
                    }else{
                        return false;
                    }
                }
            });
            if(image_files.length != 0)
                Picasso.get().load(image_files[0]).resize(gridView.getColumnWidth(),gridView.getColumnWidth()).into(imageView);
            textView.setText(book_file.getName());
            return view;
        }
    }
}
