package com.example.imageclassification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 123;
    public static final int IMAGE_CAPTURE_CODE = 654;
    private static final int PERMISSION_CODE = 321;
    ImageView innerImage;
    private Uri image_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        innerImage = findViewById(R.id.imageView2);


        innerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });

        innerImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    }
                    else {
                        openCamera();
                    }
                }

                else {
                    openCamera();
                }
                return false;
            }
        });

        // Accurate pose detector on static images, when depending on the pose-detection-accurate sdk





    }

    private void openCamera() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
            image_uri = data.getData();

            doPoseEstimation();
        }

        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK){
            doPoseEstimation();
        }
    }


    //TODO perform image labeling
    public void doPoseEstimation(){
        Bitmap inputImage = uriToBitmap(image_uri);
        Bitmap rotated = rotateBitmap(inputImage);
        innerImage.setImageBitmap(rotated);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //TODO rotate image if image captured on sumsong devices
    //Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
    public Bitmap rotateBitmap(Bitmap input){
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getContentResolver().query(image_uri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
        }
        Log.d("tryOrientation",orientation+"");
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(orientation);
        Bitmap cropped = Bitmap.createBitmap(input,0,0, input.getWidth(), input.getHeight(), rotationMatrix, true);
        return cropped;
    }

    //TODO takes URI of the image and returns bitmap
    private Bitmap uriToBitmap(Uri selectedFileUri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    void drawLine(Canvas canvas, PoseLandmark startLandmark, PoseLandmark endLandmark, Paint paint) {

            PointF start = startLandmark.getPosition();
            PointF end = endLandmark.getPosition();
            canvas.drawLine(
                    start.x, start.y, end.x, end.y, paint);
    }

//    PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
//    PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
//    PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
//    PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
//    PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
//    PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
//    PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
//    PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
//    PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
//    PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
//    PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
//    PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
//
//    PoseLandmark leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY);
//    PoseLandmark rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY);
//    PoseLandmark leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX);
//    PoseLandmark rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX);
//    PoseLandmark leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB);
//    PoseLandmark rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB);
//    PoseLandmark leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL);
//    PoseLandmark rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL);
//    PoseLandmark leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
//    PoseLandmark rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);
//
//    drawLine(canvas, leftShoulder, rightShoulder, p);
//    drawLine(canvas, leftHip, rightHip, p);
//
//    // Left body
//    drawLine(canvas, leftShoulder, leftElbow, p);
//    drawLine(canvas, leftElbow, leftWrist, p);
//    drawLine(canvas, leftShoulder, leftHip, p);
//    drawLine(canvas, leftHip, leftKnee, p);
//    drawLine(canvas, leftKnee, leftAnkle, p);
//    drawLine(canvas, leftWrist, leftThumb, p);
//    drawLine(canvas, leftWrist, leftPinky, p);
//    drawLine(canvas, leftWrist, leftIndex, p);
//    drawLine(canvas, leftIndex, leftPinky, p);
//    drawLine(canvas, leftAnkle, leftHeel, p);
//    drawLine(canvas, leftHeel, leftFootIndex, p);
//
//    // Right body
//    drawLine(canvas, rightShoulder, rightElbow, p);
//    drawLine(canvas, rightElbow, rightWrist, p);
//    drawLine(canvas, rightShoulder, rightHip, p);
//    drawLine(canvas, rightHip, rightKnee, p);
//    drawLine(canvas, rightKnee, rightAnkle, p);
//    drawLine(canvas, rightWrist, rightThumb, p);
//    drawLine(canvas, rightWrist, rightPinky, p);
//    drawLine(canvas, rightWrist, rightIndex, p);
//    drawLine(canvas, rightIndex, rightPinky, p);
//    drawLine(canvas, rightAnkle, rightHeel, p);
//    drawLine(canvas, rightHeel, rightFootIndex, p);

}



