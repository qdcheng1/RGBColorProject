package edu.purdue.qingdong.imagetest;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class CropActivity extends AppCompatActivity {

    private static String photoPath;
    Button btnCam;
    Button btnCropInterest;
    Button btnCropReference;
    ImageView imageView;

    int[][] rgbs = new int[909][3];
    double[][] lnRGB = new double[909][3];

    int[] rgb_ref = new int[3];
    int[] rgb_int = new int[3];

    double[] lnRGB_ref = new double[3];
    double[] lnRGB_int = new double[3];

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CROP_INTEREST = 2;
    private static final int REQUEST_CROP_REFERENCE = 3;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        context = this;

        imageView = (ImageView) findViewById(R.id.imageViewCrop);
        btnCropInterest = (Button) findViewById(R.id.button3);
        btnCropReference = (Button) findViewById(R.id.button4);
        btnCam = (Button) findViewById(R.id.btnCamera);

        readData(rgbs); //read data from csv file
        lnRGB(rgbs, lnRGB); //calculate the logarithm of the rgb values
        String lnstr1 = "" + lnRGB[0][0] + " " + lnRGB[0][1] + " " + lnRGB[0][2];
        String lnstr909 = "" + lnRGB[908][0] + " " + lnRGB[908][1] + " " + lnRGB[908][2];


        Log.i("lnoac1",lnstr1);
        Log.i("lnoac907",lnstr909);

        initialLn(lnRGB_ref);
        initialLn(lnRGB_int);
    }


    /**
     * initial the ln array to -1
     */
    private void initialLn(double[] arr) {
        for(int i = 0; i < arr.length; i++) {
            arr[i] = -1;
        }
    }

    private void lnRGB(int[][] colors, double[][] lnRGBs){
        for (int i = 0; i < colors.length; i++) {
            int red = colors[i][0];
            int green = colors[i][1];
            int blue = colors[i][2];

            double lnRed = Math.log(red);
            double lnGreen = Math.log(green);
            double lnBlue = Math.log(blue);

            lnRGBs[i][0] = lnRed;
            lnRGBs[i][1] = lnGreen;
            lnRGBs[i][2] = lnBlue;
        }
    }

    private void readData(int[][]rgbs){
        InputStream is = getResources().openRawResource(R.raw.rgbvalues);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = "";
        int index = 0;
        try {
            while ((line = reader.readLine()) != null) {
                //set splitter
                String[] tokens = line.split(",");
                //read the data
                rgbs[index][0] = Integer.parseInt(tokens[1]);
                rgbs[index][1] = Integer.parseInt(tokens[2]);
                rgbs[index][2] = Integer.parseInt(tokens[3]);
                index++;
            }
        } catch (IOException e1) {
            Log.e("MainActivity", "Error" + line, e1);
            e1.printStackTrace();
        }
    }

    public void onTakePhoto(View v) {
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

    public void onCropInterest(View v) {
        try {

            if (photoPath == null) {
                Toast.makeText(this, "Please take photo first!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent("com.android.camera.action.CROP");
            //intent.setClassName("com.android.camera", "com.android.camera.CropImage");
            File file = new File(photoPath);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 96);
            intent.putExtra("outputY", 96);
            intent.putExtra("noFaceDetection", true);
            intent.putExtra("return-data", true);
            intent.putExtra("scale", true);
            startActivityForResult(intent, REQUEST_CROP_INTEREST);

        } catch (ActivityNotFoundException e) {

        }
    }

    public void onCropReference(View v) {
        try {

            if (photoPath == null) {
                Toast.makeText(this, "Please take photo first!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent("com.android.camera.action.CROP");
            //intent.setClassName("com.android.camera", "com.android.camera.CropImage");
            File file = new File(photoPath);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 96);
            intent.putExtra("outputY", 96);
            intent.putExtra("scaleUpIfNeeded",true);
            //intent.putExtra("noFaceDetection", true);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, REQUEST_CROP_REFERENCE);
        } catch (ActivityNotFoundException e) {

        }
    }

    private File createImageFile() throws IOException {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_.jpg";
        File file = new File (myDir, imageFileName);
        // Save a file: path for use with ACTION_VIEW intents
        photoPath = file.getAbsolutePath();
        return file;
    }

    private void getRGB(Bitmap b, int[] rgb) {
        //get the average rgb value from an image

        int w = b.getWidth();
        int h = b.getHeight();
        int pixSum = w * h;
        int rSum = 0, gSum = 0, bSum = 0;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = b.getPixel(i,j);
                rSum += Color.red(color);
                gSum += Color.green(color);
                bSum += Color.blue(color);
            }
        }
        rgb[0] = rSum / pixSum;
        rgb[1] = gSum / pixSum;
        rgb[2] = bSum / pixSum;
    }

    public void calculateLn(int[] rgb, double[] lnrgb) {
        lnrgb[0] = Math.log(rgb[0]);
        lnrgb[1] = Math.log(rgb[1]);
        lnrgb[2] = Math.log(rgb[2]);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;      // 1/4 of original image
                Bitmap b = BitmapFactory.decodeFile(photoPath, options);
                imageView.setImageBitmap(b);
            } else if (requestCode == REQUEST_CROP_INTEREST) {
                Bundle extras = data.getExtras();
                if(extras != null ) {
                    Bitmap photo = extras.getParcelable("data");
                    getRGB(photo,rgb_int);
                    calculateLn(rgb_int, lnRGB_int);
                    imageView.setImageBitmap(photo);
                }

            } else if (requestCode == REQUEST_CROP_REFERENCE) {
                Bundle extras = data.getExtras();
                if(extras != null ) {
                    Bitmap photo = extras.getParcelable("data");
                    getRGB(photo, rgb_ref);
                    calculateLn(rgb_ref, lnRGB_ref);
                    imageView.setImageBitmap(photo);
                    //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    //photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                    // The stream to write to a file or directly using the photo
                }

            }
        }

    }

    public int getOac(double r, double g, double b, double[][] colors){
        double min = Double.MAX_VALUE;
        int result = 1;
        for (int i = 0; i < colors.length; i++) {
            double lnRed = colors[i][0];
            double lnGreen = colors[i][1];
            double lnBlue = colors[i][2];

            double sum = (lnRed - r) * (lnRed - r) + (lnGreen - g) * (lnGreen - g) + (lnBlue - b) * (lnBlue - b);

            if (sum < min) {
                min = sum;
                result = i + 1;
            }
        }

        return result;
    }

    public void onResult(View view) {
        if (lnRGB_ref[0] == -1 || lnRGB_int[0] == -1) {
            return;
        }

        //oac 41
        double realRefRed = Math.log(159);
        double realRefGreen = Math.log(167);
        double realRefBlue = Math.log(101);

        double realInterestRed = realRefRed / lnRGB_ref[0] * lnRGB_int[0];
        double realInterestGreen = realRefGreen / lnRGB_ref[1] * lnRGB_int[1];
        double realInterestBlue = realRefBlue / lnRGB_ref[2] * lnRGB_int[2];

        int oac = getOac(realInterestRed, realInterestGreen, realInterestBlue,lnRGB);
        TextView oacView = (TextView) findViewById(R.id.oacView);
        oacView.setText("oac:" + oac);
    }
}
