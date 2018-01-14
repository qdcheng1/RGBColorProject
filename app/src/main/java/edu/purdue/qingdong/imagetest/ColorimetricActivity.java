package edu.purdue.qingdong.imagetest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ColorimetricActivity extends AppCompatActivity {

    Button btnProcess;
    Button btnCamera;
    private String mCurrentPhotoPath;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorimetric);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnProcess = (Button) findViewById(R.id.process);
        imageView = (ImageView) findViewById(R.id.imageViewColor);
    }

    public void onReferenceCrop(View v) {

    }

    public void onInterestCrop(View v) {

    }

    public void onProcess(View v) {

    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_.jpg";
        File image = new File(Environment.getExternalStorageDirectory(),
                imageFileName);
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public void onCamera(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;      // 1/4 of original image
            Bitmap b = BitmapFactory.decodeFile(mCurrentPhotoPath,options);
            imageView.setImageBitmap(b);
        }

    }

}
