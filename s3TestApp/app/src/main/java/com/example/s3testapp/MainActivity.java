package com.example.s3testapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "MainActivity";

    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;

    Button uploadBtn, selectBtn;
    ImageView imageview;
    File f;

    private String userChoosenTask;
    Uri imageUri;
    String imagePath;
    private Uri mImageUri;
    private int PICTURE_CHOICE = 1;
    private int REQUEST_CAMERA = 2;
    private int SELECT_FILE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadBtn = (Button) findViewById(R.id.uploadBtn);
        selectBtn = (Button) findViewById(R.id.selectBtn);
        imageview = (ImageView) findViewById(R.id.imageview1);

        uploadBtn.setOnClickListener(this);
        selectBtn.setOnClickListener(this);

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:a6e044e8-64f4-4f05-a039-d710d40837b0", // 자격 증명 풀 ID
                Regions.US_EAST_1 // 리전
        );
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        s3.setEndpoint("s3.us-east-1.amazonaws.com");

        transferUtility = new TransferUtility(s3, getApplicationContext());
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.uploadBtn:
                TransferObserver observer = transferUtility.upload(
                        "ohjs3test2",
                        f.getName(),
                        f
                );
                break;
            case R.id.selectBtn:
                selectImage();
                break;
        }
    }
    private void selectImage() {

        Log.d(TAG, "select Image");
        final CharSequence[] items = {"촬영하기", "사진 가져오기",
                "취소"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진가져오기");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(MainActivity.this);

                if (items[item].equals("촬영하기")) {
                    userChoosenTask = "촬영하기";
                    if (result)
                        cameraIntent();
                } else if (items[item].equals("사진 가져오기")) {
                    userChoosenTask = "사진 가져오기";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("취소")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("촬영하기"))
                        cameraIntent();
                    else if (userChoosenTask.equals("사진 가져오기"))
                        galleryIntent();
                } else {
//code for deny
                }
                break;
        }
    }

    private void galleryIntent() {
        Log.d(TAG, "Gallery Intent");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photo = null;
        try {
// place where to store camera taken picture
            photo = this.createTemporaryFile("picture", ".jpg");
            photo.delete();
        } catch (Exception e) {
            Log.v(TAG, "Can't create file to take picture!");
            Toast.makeText(this, "sd카드를 확인해주세요", Toast.LENGTH_SHORT);
        }
        mImageUri = Uri.fromFile(photo);
        Log.d(TAG, mImageUri.toString());
        if (Build.VERSION.SDK_INT <= 19) {

        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        }
        startActivityForResult(intent, REQUEST_CAMERA);

    }

    private File createTemporaryFile(String part, String ext) throws Exception {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                Log.d(TAG, "onActivityResult, SELECT_FILE");
                onSelectFromGalleryResult(data, SELECT_FILE);
            } else if (requestCode == REQUEST_CAMERA) {
                try {
                    onCaptureImageResult(data, REQUEST_CAMERA);
                } catch (Exception e) {
                    this.grabImage(imageview, REQUEST_CAMERA);
                }
            }

        }
    }

    private void onCaptureImageResult(Intent data, int imagetype) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Bitmap bm = null;
        bm = null;
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        Bitmap correctBmp = null;
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();

            File f = new File(RealPathUtil.getRealPathFromURI_API19(this, data.getData()));
            ExifInterface exif = new ExifInterface(f.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int angle = 0;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);

            Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
            correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bm = getResizedBitmap(correctBmp, getResources().getDimensionPixelSize(R.dimen.idcard_pic_height), getResources().getDimensionPixelSize(R.dimen.idcard_pic_width));
        if (imagetype == REQUEST_CAMERA) {
            f = SaveImage(bm);
            imageview.setImageBitmap(bm);
        }
    }
    private void onSelectFromGalleryResult(Intent data, int imagetype) {

        Log.d(TAG, "onSelectFromGalleryResult");
        Bitmap bm = null;
        imageUri = data.getData();
        if (Build.VERSION.SDK_INT < 11) {
            imagePath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, imageUri);
            Log.d(TAG, Build.VERSION.SDK_INT + "");
        } else if (Build.VERSION.SDK_INT < 19) {
            Log.d(TAG, Build.VERSION.SDK_INT + "");
            imagePath = RealPathUtil.getRealPathFromURI_API11to18(this, imageUri);
        } else {
            Log.d(TAG, Build.VERSION.SDK_INT + "");
            imagePath = RealPathUtil.getRealPathFromURI_API19(this, imageUri);
        }
        Log.d(TAG, imagePath);


        try {
            bm = getResizedBitmap(decodeUri(data.getData()), getResources().getDimensionPixelSize(R.dimen.idcard_pic_height), getResources().getDimensionPixelSize(R.dimen.idcard_pic_width));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (imagetype == SELECT_FILE) {
            f = new File(imagePath);
            imageview.setImageBitmap(bm);
        }
    }

    public static Bitmap getResizedBitmap(Bitmap image, int newHeight, int newWidth) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
// create a matrix for the manipulation
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        if (Build.VERSION.SDK_INT <= 19) {
//matrix.postRotate(90);
        }
// recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                this.getContentResolver().openInputStream(selectedImage), null, o);

        final int REQUIRED_SIZE = 100;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(
                this.getContentResolver().openInputStream(selectedImage), null, o2);
    }

    public File SaveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/MobileCard");

        if (!myDir.exists()) {
            Log.d("SaveImage", "non exists : " + myDir);
            myDir.mkdirs();
        }

        long now = System.currentTimeMillis();
        String fname = now + ".jpg";
        File file = new File(myDir, fname);

        if (file.exists()) {
            Log.d("SaveImage", "file exists");
            file.delete();
        } else {
            Log.d("SaveImage", "file non exists");
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            Log.d("SaveImage", "file save");
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }
    public void grabImage(ImageView imgView, int imagetype) {

        this.getContentResolver().notifyChange(mImageUri, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap = null;
        Bitmap bm = null;
        Bitmap correctBmp = null;
        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);

            File f = new File(RealPathUtil.getRealPathFromURI_API19(this, mImageUri));
            ExifInterface exif = new ExifInterface(f.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int angle = 0;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);

            Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
            correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);

        } catch (Exception e) {
            Toast.makeText(this, "불러오기에 실패했습니다", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to load", e);
        }
        bm = getResizedBitmap(correctBmp, getResources().getDimensionPixelSize(R.dimen.idcard_pic_height), getResources().getDimensionPixelSize(R.dimen.idcard_pic_width));

        if (imagetype == REQUEST_CAMERA) {
            f = SaveImage(bm);
            imageview.setImageBitmap(bm);
        }
    }
}
