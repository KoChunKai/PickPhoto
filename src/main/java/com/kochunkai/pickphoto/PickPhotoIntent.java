package com.kochunkai.pickphoto;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by kevin on 2015/8/4.
 */
public class PickPhotoIntent extends Intent{

    public PickPhotoIntent() {
        super();
    }

    public PickPhotoIntent(Intent o) {
        super(o);
    }

    public PickPhotoIntent(String action) {
        super(action);
    }

    public PickPhotoIntent(String action, Uri uri) {
        super(action, uri);
    }

    public PickPhotoIntent(Context packageContext, Class<?> cls) {
        super(packageContext, cls);
    }

    public PickPhotoIntent(String action, Uri uri, Context packageContext, Class<?> cls) {
        super(action, uri, packageContext, cls);
    }

    public PickPhotoIntent(Context packageContext){
        super(packageContext, PickPhoto.class);
    }

    public void setLimit(int value){
        this.putExtra(PickPhoto.LimitValue, value);
    }


    public void setpickPhoto(ArrayList arrayList){
        this.putExtra(PickPhoto.photoArrayList,arrayList);
    }

    public ArrayList getpickPhoto(){
        return this.getStringArrayListExtra(PickPhoto.photoArrayList);
    }

    public void setoverLimitString(String str){
        this.putExtra(PickPhoto.overLimitString, str);
    }

}
