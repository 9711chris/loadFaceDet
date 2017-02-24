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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.bitmap;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;
    private Paint mFaceLandmardkPaint2;
    private static final String TAG = "Debug";
    private CameraBridgeViewBase mOpenCvCameraView;
    private FaceDet faceDet = new FaceDet(Constants.getFaceShapeModelPath());

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mLoaderCallback))
        {
            Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
        setContentView(R.layout.activity_main);
        init();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.javaCamera);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        mOpenCvCameraView.setCvCameraViewListener(this);

        getLandmarks();
    }

    private void init(){
        mFaceLandmardkPaint2 = new Paint();
        mFaceLandmardkPaint2.setColor(Color.RED);
        mFaceLandmardkPaint2.setStrokeWidth(2);
        mFaceLandmardkPaint2.setStyle(Paint.Style.STROKE);


    }

    private void getLandmarks(){
        Log.d(TAG, "getLandmarks");

        //Log.d(TAG,"path2 " + Constants.getFaceShapeModelPath());

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.face);
        Bitmap workingBitmap = Bitmap.createBitmap(bm);
        Bitmap mutableBitmap = Bitmap.createScaledBitmap(workingBitmap, (int) workingBitmap.getWidth() / 2, (int) workingBitmap.getHeight() / 2, false);
        //Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        List<VisionDetRet> results = faceDet.detect(mutableBitmap);
        Log.d(TAG, "faces " + results.size());
        for (final VisionDetRet ret : results) {
            Log.d(TAG, "in VisionDet");
            float resizeRatio = 1.0f;
            String label = ret.getLabel(); // If doing face detection, it will be 'Face'
            Log.d(TAG, "label" + label);
            /*Rect bounds = new Rect();
            bounds.left = (int) (ret.getLeft() * resizeRatio);
            bounds.top = (int) (ret.getTop() * resizeRatio);
            bounds.right = (int) (ret.getRight() * resizeRatio);
            bounds.bottom = (int) (ret.getBottom() * resizeRatio);
            canvas.drawRect(bounds, mFaceLandmardkPaint2);*/
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



    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, " in camera frame");
        Mat originalframe = inputFrame.rgba();
        Mat grayFrame = inputFrame.gray();

        Bitmap bmp = null;
        try {
            //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
            bmp = Bitmap.createBitmap(grayFrame.cols(), grayFrame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(grayFrame, bmp);
        }
        catch (CvException e){Log.d("Exception",e.getMessage());}

        //Log.d(TAG,"path " + Constants.getFaceShapeModelPath());

        //Log.d(TAG,"path2 " + Constants.getFaceShapeModelPath());

        //Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.face);
        Bitmap workingBitmap = Bitmap.createBitmap(bmp);
        //Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap mutableBitmap = Bitmap.createScaledBitmap(workingBitmap, (int) workingBitmap.getWidth() / 4, (int) workingBitmap.getHeight() / 4, false);
        Canvas canvas = new Canvas(mutableBitmap);
        List<VisionDetRet> results = faceDet.detect(mutableBitmap);
        Log.d(TAG, "faces " + results.size());
        for (final VisionDetRet ret : results) {
            Log.d(TAG, "in VisionDet");
            float resizeRatio = 4.0f;
            String label = ret.getLabel(); // If doing face detection, it will be 'Face'
            Log.d(TAG, "label" + label);
            /*Rect bounds = new Rect();
            bounds.left = (int) (ret.getLeft() * resizeRatio);
            bounds.top = (int) (ret.getTop() * resizeRatio);
            bounds.right = (int) (ret.getRight() * resizeRatio);
            bounds.bottom = (int) (ret.getBottom() * resizeRatio);
            canvas.drawRect(bounds, mFaceLandmardkPaint2);*/
            ArrayList<Point> landmarks = ret.getFaceLandmarks();
            Log.d(TAG, "list length" + landmarks.size());
            for (Point point : landmarks) {
                int pointX = (int) (point.x * resizeRatio);
                int pointY = (int) (point.y * resizeRatio);
                Log.d(TAG, "x, y " + pointX + " " + pointY);
                // Get the point of the face landmarks
                //canvas.drawCircle(pointX, pointY, 2, mFaceLandmardkPaint2);
                Imgproc.circle(originalframe, new org.opencv.core.Point(pointX, pointY), 5, new Scalar(0, 255, 255));
            }
        }
        //Mat matRet = new Mat(originalframe.rows(), originalframe.cols(), originalframe.type());
        //Utils.bitmapToMat(mutableBitmap, matRet);

        return originalframe;
    }

}
