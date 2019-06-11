package com.intsig.yann.analysis;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;

public class Crop extends AppCompatActivity {
    private static final int START_X = 500;
    private static final int START_Y = 1100;

    private Bitmap bitmap;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        finish();
    }

    private void init() {

        Intent intent = getIntent();
        String URI = intent.getStringExtra("uri");
        Log.d("URI is", "" + URI);

        try {
            FileInputStream fis = new FileInputStream(URI);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /*height of selection box*/
        int coverHeight = 100;
        /*width of selection box*/
        int coverWidth = 200;
        Bitmap result = Bitmap.createBitmap(bitmap, START_X, START_Y, coverWidth, coverHeight);
        File file = new File(AnalysisHolderActivity.TempCropFile);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            result.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent1 = new Intent();
        //intent1.putExtra("bitmap", result);
        setResult(Activity.RESULT_OK, intent1);
    }

}
