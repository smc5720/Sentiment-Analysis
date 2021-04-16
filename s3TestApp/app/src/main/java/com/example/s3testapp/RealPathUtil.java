package com.example.s3testapp;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

public class RealPathUtil {

    final static String TAG = "GalleryPhoto";

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri photoUri){
        Log.d(TAG, "> API 19");
        String path = "";
        if (DocumentsContract.isDocumentUri(context, photoUri)) {

            Log.d(TAG, "isDocumentUri");
            Log.d(TAG, "photoUri:"+photoUri);
// ExternalStorageProvider
            if (isExternalStorageDocument(photoUri)) {
                Log.d(TAG, "externalStorageDocument");
                final String docId = DocumentsContract.getDocumentId(photoUri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    path = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
// DownloadsProvider
            else if (isDownloadsDocument(photoUri)) {
                Log.d(TAG, "DownloadsDocument");
                final String id = DocumentsContract.getDocumentId(photoUri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                path = getDataColumn(context, contentUri, null, null);
            }
// MediaProvider
            else if (isMediaDocument(photoUri)) {
                Log.d(TAG, "MediaDocument");
                final String docId = DocumentsContract.getDocumentId(photoUri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                path = getDataColumn(context, contentUri, selection, selectionArgs);
            }

            else{
                path = String.valueOf(photoUri);
            }
        }
// MediaStore (and general)
        else if ("content".equalsIgnoreCase(photoUri.getScheme())) {
            Log.d(TAG, "content");
            path = getDataColumn(context, photoUri, null, null);
        }
// File
        else if ("file".equalsIgnoreCase(photoUri.getScheme())) {
            Log.d(TAG, "file");
            path = photoUri.getPath();
        }
        Log.d(TAG,path);
        return path;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        Log.d(TAG, "API 11 to 18");
        String result = null;

        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
        Log.d(TAG, "API Below 11");
        String path = "";
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.https://github.com/whdals0

     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
