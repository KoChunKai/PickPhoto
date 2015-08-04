package com.kochunkai.pickphoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.util.ArrayList;
import java.util.HashMap;


public class PickPhoto extends Activity {

    public static int PickPhoto_requestCode = 23231;
    public static int PickPhoto_ResultCode = 1;
    public static String overLimitString = "Sorry, cannot pick more photos";
    public static String photoArrayList = "photoArrayList";
    public static String LimitValue = "LimitVaule";

    ImageLoader imageLoader = ImageLoader.getInstance();

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .showImageOnLoading(null)
            .showImageForEmptyUri(null)
            .showImageOnFail(null)
            .cacheInMemory(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .build();

    GridView gridView;
    AlbumAdapter adapter;
    ImageView close, done;
    TextView tv_count;

    HashMap<String,ArrayList<ImageBox>> Album = new HashMap<>();
    ArrayList<String> FolderName = new ArrayList<>();
    ArrayList<String> pickPhoto = new ArrayList<>();

    boolean isInAlbum = false;
    int count = 0;
    int limit = 9;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        gridView = (GridView)findViewById(R.id.gridView);
        close = (ImageView)findViewById(R.id.close);
        done = (ImageView)findViewById(R.id.done);
        tv_count = (TextView)findViewById(R.id.count);

        initVaule();

        getAllShownImagesPath();
        adapter = new AlbumAdapter();
        gridView.setAdapter(adapter);
        gridView.setNumColumns(2);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putStringArrayListExtra(photoArrayList,pickPhoto);
                setResult(PickPhoto_ResultCode, data);
                finish();
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isInAlbum) {
                    if (view.findViewById(R.id.pick).getVisibility() == View.VISIBLE) {
                        ImageBox box = (ImageBox) parent.getAdapter().getItem(position);
                        box.isPick = false;
                        view.findViewById(R.id.pick).setVisibility(View.GONE);
                        tv_count.setText(Integer.toString(--count));
                        pickPhoto.remove(box.Path);
                    } else {
                        if (count == limit) {
                            Toast.makeText(getApplicationContext(), overLimitString, Toast.LENGTH_LONG).show();
                            return;
                        }
                        ImageBox box = (ImageBox) parent.getAdapter().getItem(position);
                        box.isPick = true;
                        pickPhoto.add(box.Path);
                        view.findViewById(R.id.pick).setVisibility(View.VISIBLE);
                        tv_count.setText(Integer.toString(++count));
                    }
                } else {
                    isInAlbum = true;
                    gridView.setAdapter(new ChildAdapter(Album.get(FolderName.get(position))));
                    close.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                }
            }
        });
    }

    private void initVaule(){
        if(getIntent().hasExtra(photoArrayList)){
            pickPhoto = getIntent().getStringArrayListExtra(photoArrayList);
            count = pickPhoto.size();
            tv_count.setText(Integer.toString(count));
        }
        if(getIntent().hasExtra(LimitValue)) limit = getIntent().getIntExtra(LimitValue, limit);
        if(getIntent().hasExtra(overLimitString)) overLimitString = getIntent().getStringExtra(overLimitString);
    }

    @Override
    public void onBackPressed() {
        if(isInAlbum){
            gridView.setAdapter(adapter);
            isInAlbum = false;
            close.setImageResource(R.drawable.ic_close_white_24dp);
        }else{
            Intent data = new Intent();
            data.putStringArrayListExtra(photoArrayList,pickPhoto);
            setResult(PickPhoto_ResultCode, data);
            finish();
        }
    }

    public void getAllShownImagesPath() {
        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_ADDED, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        Cursor cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, sortOrder);
        int data_path = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            String folder_name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
            if(!FolderName.contains(folder_name)){
                FolderName.add(folder_name);
                Album.put(folder_name,new ArrayList<ImageBox>());
            }
            ArrayList<ImageBox> box = Album.get(folder_name);
            ImageBox item = new ImageBox();
            item.Path = cursor.getString(data_path);
            if(pickPhoto.contains(item.Path)) item.isPick = true;
            box.add(item);
        }
    }

    public class AlbumAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return Album.size();
        }

        @Override
        public Object getItem(int position) {
            return Album.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final Holder holder;
            if(view == null){
                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.photo_item, null);
                holder = new Holder();
                holder.photo = (ImageView) view.findViewById(R.id.photo);
                holder.folder_name = (TextView) view.findViewById(R.id.folder_name);
                view.setTag(holder);
            } else{
                holder = (Holder) view.getTag();
            }
            imageLoader.displayImage(
                    ImageDownloader.Scheme.FILE.wrap(Album.get(FolderName.get(position)).get(0).Path),
                    holder.photo,options);
            holder.folder_name.setText(FolderName.get(position));
            return view;
        }

        class Holder{
            ImageView photo/*,mask*/;
            TextView folder_name;
        }
    }

    public class ChildAdapter extends BaseAdapter{

        ArrayList data;

        public ChildAdapter(ArrayList data){
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final Holder holder;
            if(view == null){
                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.photo_item, null);
                holder = new Holder();
                holder.photo = (ImageView) view.findViewById(R.id.photo);
                holder.pick = (ImageView) view.findViewById(R.id.pick);
                holder.folder_name = (TextView) view.findViewById(R.id.folder_name);
                view.setTag(holder);
            } else{
                holder = (Holder) view.getTag();
            }
            ImageBox box = (ImageBox) data.get(position);
            imageLoader.displayImage(
                    ImageDownloader.Scheme.FILE.wrap(box.Path),
                    holder.photo,options);
            holder.folder_name.setVisibility(View.GONE);
            if(box.isPick){
                holder.pick.setVisibility(View.VISIBLE);
            }else{
                holder.pick.setVisibility(View.GONE);
            }
            return view;
        }

        class Holder{
            ImageView photo, pick;
            TextView folder_name;
        }
    }
}
