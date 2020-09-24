package com.example.photogallery;

import android.net.Uri;

public class ImageItem {
    private Uri _uri;
    private String _title;

    public ImageItem(Uri uri, String title) {
        this._uri = uri;
        this._title = title;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public Uri getUri() {
        return _uri;
    }

    public void setUri(Uri _uri) {
        this._uri = _uri;
    }
}
