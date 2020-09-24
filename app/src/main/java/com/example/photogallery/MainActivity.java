package com.example.photogallery;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GridView _gridView;
    private GridViewArrayAdapter _adapter;
    private ArrayList<ImageItem> _images;
    int tmpWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissions();
        initComponents();
        updateGridView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.setting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int densityDpi = (int) (metrics.density * 160f);
        int dp = (int) (densityDpi / 160);
        switch (item.getItemId()) {
            case R.id.refresh:
                _gridView.setColumnWidth(GridView.AUTO_FIT);
                updateGridView();
                break;
            case R.id.zoom_in:
                _gridView.setColumnWidth((int) (_gridView.getColumnWidth() + 10 * dp));
                break;
            case R.id.zoom_out:
                _gridView.setColumnWidth((int)(tmpWidth));
                tmpWidth-=10 * dp;
                Log.d("AA", Integer.toString(tmpWidth));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndRequestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        Log.d("cHENG", "isExternalStorageAvailable: " + extStorageState);
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private void updateGridView() {
        (new AsyncTaskLoadImage(this)).execute();
    }

    private void initComponents() {
        _gridView = findViewById(R.id.gridViewGallery);
    }

    private class AsyncTaskLoadImage extends AsyncTask<Void, Void, List<ImageItem>> {
        private ProgressDialog _dialog;

        public AsyncTaskLoadImage(Activity activity) {
            _dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            _dialog.setTitle("Loading");
            _dialog.show();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected List<ImageItem> doInBackground(Void... voids) {
            // https://developer.android.com/training/data-storage/shared/media
            ArrayList<ImageItem> results = new ArrayList<>();

            String[] projection = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME
            };
            String sortOrder = MediaStore.Images.Media.DISPLAY_NAME + " ASC";

            try (Cursor cursor = getApplicationContext().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
            )) {
                // Cache column indices.
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int nameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);

                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    ImageItem item = new ImageItem(contentUri, name);
                    results.add(item);
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<ImageItem> results) {
            _images = (ArrayList<ImageItem>) results;
            _adapter = new GridViewArrayAdapter(MainActivity.this,
                    R.layout.gridview_item, _images);
            _gridView.setAdapter(_adapter);
            tmpWidth = _gridView.getColumnWidth();
            if (_dialog.isShowing()) {
                _dialog.dismiss();
            }
        }
    }
}
