package com.example.christantia.switchback_dlib;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.bitmap;

public class MainActivity extends AppCompatActivity {
    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;
    private Paint mFaceLandmardkPaint2;
    private static final String TAG = "Debug";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFaceLandmardkPaint2 = new Paint();
        mFaceLandmardkPaint2.setColor(Color.RED);
        mFaceLandmardkPaint2.setStrokeWidth(2);
        mFaceLandmardkPaint2.setStyle(Paint.Style.STROKE);
        getLandmarks();
    }

    private void getLandmarks(){
        Log.d(TAG, "getLandmarks");
        FaceDet faceDet = new FaceDet(Constants.getFaceShapeModelPath());
        Log.d(TAG,"path " + Constants.getFaceShapeModelPath());

        if (!new File(Constants.getFaceShapeModelPath()).exists()) {
            copyFileFromRawToOthers(this, R.raw.shape_predictor_68_face_landmarks, Constants.getFaceShapeModelPath());
        }
        Log.d(TAG,"path2 " + Constants.getFaceShapeModelPath());
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.face);
        Bitmap workingBitmap = Bitmap.createBitmap(bm);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        List<VisionDetRet> results = faceDet.detect(mutableBitmap);
        Log.d(TAG, "faces " + results.size());
        for (final VisionDetRet ret : results) {
            Log.d(TAG, "in VisionDet");
            float resizeRatio = 1.0f;
            String label = ret.getLabel(); // If doing face detection, it will be 'Face'
            Log.d(TAG, "label" + label);
            Rect bounds = new Rect();
            bounds.left = (int) (ret.getLeft() * resizeRatio);
            bounds.top = (int) (ret.getTop() * resizeRatio);
            bounds.right = (int) (ret.getRight() * resizeRatio);
            bounds.bottom = (int) (ret.getBottom() * resizeRatio);
            canvas.drawRect(bounds, mFaceLandmardkPaint2);
            ArrayList<Point> landmarks = ret.getFaceLandmarks();
            Log.d(TAG, "list length" + landmarks.size());
            for (Point point : landmarks) {
                int pointX = (int) (point.x * resizeRatio);
                int pointY = (int) (point.y * resizeRatio);
                Log.d(TAG, "x, y " + pointX + " " + pointY);
                // Get the point of the face landmarks
                canvas.drawCircle(pointX, pointY, 2, mFaceLandmardkPaint2);
            }
        }
        ImageView imageView = (ImageView)findViewById(R.id.imgView);
        imageView.setImageBitmap(mutableBitmap);
    }

    @NonNull
    public static final void copyFileFromRawToOthers(@NonNull final Context context, @RawRes int id, @NonNull final String targetPath) {
        InputStream in = context.getResources().openRawResource(id);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(targetPath);
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
