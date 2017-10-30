package edu.purdue.qingdong.imagetest;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    File file;
    Uri uri;
    Intent CamIntent,GalIntent,CropIntent;
    final int RequestPermissionCode=1;
    DisplayMetrics displayMetrics;

    Button btnCamera;
    ImageView imageView;  // show the image took from camera
    TextView rgbTextView; // textview for rgb values
    TextView labColorView; // textview for oac testing
    TextView rgbColorView; // textview for oac testing
    TextView hsvColorView; // textview for oac testing

    //store the rgb values from the scanned images
    int[][] rgbs = new int[909][3];
    //store the lab values that converted from rgb values
    double[][] labs = new double[909][3];
    //store the hsv/hsb values that converted from rgb values
    float[][] hsvs = new float[909][3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCamera = (Button) findViewById(R.id.btnCamera);

        rgbColorView = (TextView) findViewById(R.id.rgbColorView);
        labColorView = (TextView) findViewById(R.id.labColorView);
        hsvColorView = (TextView) findViewById(R.id.hsvColorView);
        //read the rgb values from csv file
        readData(rgbs);
        //convert rgb values into labs and hsvs
        convertRgbToLab(labs);
        convertRgbToHsv(hsvs);



        imageView = (ImageView)findViewById(R.id.imageView);
        rgbTextView = (TextView) findViewById(R.id.rgbValue);

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
            RequestRuntimePermission();

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CamIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                file = new File(Environment.getExternalStorageDirectory(),
                        "file"+String.valueOf(System.currentTimeMillis())+".jpg");
                uri = Uri.fromFile(file);
                CamIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                CamIntent.putExtra("return-data",true);
                startActivityForResult(CamIntent,0);
            }
        });

    }


    public void saveTestValue(String value){
        // for test purpose, write the string into the test.csv file
        try
        {
            File root = Environment.getExternalStorageDirectory();
            File gpxfile = new File(root, "test.csv");
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append(value);
            //generate whatever data you want
            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//        imageView.setImageBitmap(bitmap);
//    }


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

    private int colorMatchRGB(int[][]RGBcolors, int[] croppedValues) {
        int ans = 1;
        int red = croppedValues[0];
        int green = croppedValues[1];
        int blue = croppedValues[2];
        int min = (red - RGBcolors[0][0]) * (red - RGBcolors[0][0]) + (green - RGBcolors[0][1]) * (green - RGBcolors[0][1]) +(blue - RGBcolors[0][2]) * (blue - RGBcolors[0][2]);
        for (int i = 1; i < RGBcolors.length; i++) {
            int ri = RGBcolors[i][0];
            int gi = RGBcolors[i][1];
            int bi = RGBcolors[i][2];
            int diff = (red - ri) * (red - ri) + (green - gi) * (green - gi) +(blue - bi) * (blue - bi) ;
            if (diff < min) {
                min = diff;
                ans = i + 1;
            }
        }
        return ans;
    }

    private int colorMatchHSV(float[][] hsvColors, float[] hsv) {
        int ans = 1;
        double min = distanceOf(hsvColors[0], hsv);
        for (int i = 1; i < hsvColors.length; i++) {
            double diff = distanceOf(hsvColors[i], hsv);
            if (diff < min) {
                min = diff;
                ans = i + 1;
            }
        }

        return ans;
    }
    private int colorMatchLab(double[][]labColors, double[] croppedValues) {
        int ans = 1;
        double l = croppedValues[0];
        double a = croppedValues[1];
        double b = croppedValues[2];
        double min = (l - labColors[0][0]) * (l - labColors[0][0]) + (a - labColors[0][1]) * (a - labColors[0][1]) +(b - labColors[0][2]) * (b - labColors[0][2]);
        for (int i = 1; i < labColors.length; i++) {
            double li = labColors[i][0] ;
            double ai = labColors[i][1];
            double bi = labColors[i][2];
            double diff = (l - li) * (l - li) + (a - ai) * (a - ai) +(b - bi) * (b - bi) ;
            if (diff < min) {
                min = diff;
                ans = i + 1;
            }
        }
        return ans;
    }

    private void RequestRuntimePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA))
            Toast.makeText(this,"CAMERA permission allows us to access CAMERA app",Toast.LENGTH_SHORT).show();
        else
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},RequestPermissionCode);
        }
    }

    private void colorDiffTestRGB(int[] rgb) {
        //show the most similar oac chunk according the rgb values
        int oac = colorMatchRGB(rgbs, rgb);
        String oacStr = "RGB_oac" + oac;
        //string to write to testfile
        String test =  "RGB_oac," + oac + "\n";
        saveTestValue(test);
        rgbColorView.setText(oacStr);
    }

    private void colorDiffTestLab(double[] lab) {
        //show the most similar oac chunk according the lab values
        int oac = colorMatchLab(labs, lab);
        String oacStr = "Lab_oac" + oac;
        String test = "Lab_oac," + oac + ",";
        saveTestValue(test); //save the test result to test.csv file
        labColorView.setText(oacStr);
    }

    private void colorDiffTestHSV(float[] hsv) {
        //show the most similar oac chunk according the rgb values
        int oac = colorMatchHSV(hsvs, hsv);
        String oacStr = "HSV_oac" + oac;
        String test = "HSV_oac," + oac + ",";
        saveTestValue(test);
        hsvColorView.setText(oacStr);
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main,menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if(item.getItemId() == R.id.btnCamera)
//            CameraOpen();
//        else if(item.getItemId() == R.id.btn_gallery)
//            GalleryOpen();
//        return true;
//    }

//    private void GalleryOpen() {
//        GalIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(Intent.createChooser(GalIntent,"Select Image from Gallery"),2);
//    }

    private void CameraOpen() {
        CamIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(),
                "file"+String.valueOf(System.currentTimeMillis())+".jpg");
        uri = Uri.fromFile(file);
        CamIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        CamIntent.putExtra("return-data",true);
        startActivityForResult(CamIntent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0) {
            CropImage();
        }
        else if (requestCode == 1 && resultCode == RESULT_OK) {
            if(data != null) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = bundle.getParcelable("data");
                SaveImage(bitmap);
                int[] rgb = new int[3];
                getRGB(bitmap, rgb);

                showRGB(rgb);

                //colorDiffTest(rgb);

                //test the lab color
                float[] croppedHsv = new float[3];
                Color.RGBToHSV(rgb[0],rgb[1],rgb[2],croppedHsv);
                double[] croppedLab = rgbToLab(rgb[0],rgb[1],rgb[2]);
                colorDiffTestLab(croppedLab);
                colorDiffTestHSV(croppedHsv);
                colorDiffTestRGB(rgb);
                imageView.setImageBitmap(bitmap);
            }
        }
//        else if(requestCode == 2)
//        {
//            if(data != null)
//            {
//                uri = data.getData();
//                CropImage();
//            }
//        }
//        else if (requestCode == 1)
//        {
//            if(data != null)
//            {
//                Bundle bundle = data.getExtras();
//                Bitmap bitmap = bundle.getParcelable("data");
//                imageView.setImageBitmap(bitmap);
//            }
//        }
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
//        for (int i = 0; i < w; i++) {
//            for (int j = 0; j < h; j++) {
//                int pix = b.getPixel(i, j);
//                rSum += (pix >> 16) & 0xff;     //bitwise shifting
//                gSum += (pix >> 8) & 0xff;
//                bSum += pix & 0xff;
//            }
//        }
        rgb[0] = rSum / pixSum;
        rgb[1] = gSum / pixSum;
        rgb[2] = bSum / pixSum;
    }

    private void showRGB(int[] rgb) {
        String rgbText = "Red: " + rgb[0] + " Green: " + rgb[1] + " Blue: " + rgb[2];
        rgbTextView.setText(rgbText);
    }

    private void CropImage() {

        try{
            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setDataAndType(uri,"image/*");
            CropIntent.putExtra("crop","true");
            CropIntent.putExtra("outputX",180);
            CropIntent.putExtra("outputY",180);
            CropIntent.putExtra("aspectX",3);
            CropIntent.putExtra("aspectY",4);
            CropIntent.putExtra("scaleUpIfNeeded",true);
            CropIntent.putExtra("return-data",true);
            startActivityForResult(CropIntent,1);
        }
        catch (ActivityNotFoundException ex)
        {

        }

    }


    public double[] rgbToLab(int R, int G, int B) {
        //convert rgb to lab
        //return a double array

        double r, g, b, X, Y, Z, xr, yr, zr;
        // D65/2°
        double Xr = 95.047;
        double Yr = 100.0;
        double Zr = 108.883;

        // RGB to XYZ //

        r = R/255.0;
        g = G/255.0;
        b = B/255.0;

        if (r > 0.04045)
            r = Math.pow((r+0.055)/1.055,2.4);
        else
            r = r/12.92;

        if (g > 0.04045)
            g = Math.pow((g+0.055)/1.055,2.4);
        else
            g = g/12.92;

        if (b > 0.04045)
            b = Math.pow((b+0.055)/1.055,2.4);
        else
            b = b/12.92 ;

        r*=100;
        g*=100;
        b*=100;

        X =  0.4124*r + 0.3576*g + 0.1805*b;
        Y =  0.2126*r + 0.7152*g + 0.0722*b;
        Z =  0.0193*r + 0.1192*g + 0.9505*b;

        //  XYZ to Lab //

        xr = X/Xr;
        yr = Y/Yr;
        zr = Z/Zr;

        if ( xr > 0.008856 )
            xr =  (float) Math.pow(xr, 1/3.);
        else
            xr = (float) ((7.787 * xr) + 16 / 116.0);

        if ( yr > 0.008856 )
            yr =  (float) Math.pow(yr, 1/3.);
        else
            yr = (float) ((7.787 * yr) + 16 / 116.0);

        if ( zr > 0.008856 )
            zr =  (float) Math.pow(zr, 1/3.);
        else
            zr = (float) ((7.787 * zr) + 16 / 116.0);


        double[] lab = new double[3];

        lab[0] = (116*yr)-16;
        lab[1] = 500*(xr-yr);
        lab[2] = 200*(yr-zr);

        return lab;

    }

    private void convertRgbToLab(double[][] labs) {
        //convert rgb to lab value
        for (int i = 0; i < rgbs.length; i++) {
            double[] temp = rgbToLab(rgbs[i][0],rgbs[i][1],rgbs[i][2]);
            labs[i][0] = temp[0];
            labs[i][1] = temp[1];
            labs[i][2] = temp[2];
        }
    }

    public void convertRgbToHsv(float [][] hsvs){
        for (int i = 0; i < hsvs.length; i++) {
            int red = rgbs[i][0];
            int green = rgbs[i][1];
            int blue = rgbs[i][2];
            Color.RGBToHSV(red, green, blue,hsvs[i]);
        }
    }



    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double distanceOf(float[] hsv1, float[] hsv2) {
        float h1 = hsv1[0];
        float s1 = hsv1[1];
        float v1 = hsv1[2];

        float h2 = hsv2[0];
        float s2 = hsv2[1];
        float v2 = hsv2[2];
        //self-defined
        final double R = 100;
        final double angle = 30;
        final double h = R * Math.cos(angle / 180 * Math.PI);
        final double r = R * Math.sin(angle / 180 * Math.PI);

        double x1 = r * v1 * s1 * Math.cos(h1 / 180 * Math.PI);
        double y1 = r * v1 * s1 * Math.sin(h1 / 180 * Math.PI);
        double z1 = h * (1 - v1);
        double x2 = r * v2 * s2 * Math.cos(h2 / 180 * Math.PI);
        double y2 = r * v2 * s2 * Math.sin(h2 / 180 * Math.PI);
        double z2 = h * (1 - v2);
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public void writeHsv() {

        try
        {
            File root = Environment.getExternalStorageDirectory();
            File gpxfile = new File(root, "hsv.csv");
            FileWriter writer = new FileWriter(gpxfile, true);
            String hsvStr = "";

            for (int i = 0; i < hsvs.length; i++) {
                hsvStr = hsvStr + hsvs[i][0] + "," + hsvs[i][1] + "," + hsvs[i][2] + "\n";
            }

            writer.append(hsvStr);
            //generate whatever data you want
            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode)
//        {
//            case RequestPermissionCode:
//            {
//                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
//                else
//                    Toast.makeText(this,"Permission Canceled",Toast.LENGTH_SHORT).show();
//            }
//            break;
//        }
//    }
}
