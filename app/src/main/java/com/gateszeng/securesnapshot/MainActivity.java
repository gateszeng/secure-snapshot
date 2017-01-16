package com.gateszeng.securesnapshot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.FileOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 1;
    private static final int MAX_RECORD_TIME = 5000;
    private static final int RECORD_INTERVALS = 500;
    private static final int START_DELAY= 1000;
    private static final int MAX_IMAGE_COUNT = 10;
    private static String[] PERMISSIONS = {Manifest.permission.CAMERA};
    private static final String MODE = "DES";
    private static final String IMAGE_FILE = "IMAGES_FILE";
    private static final String USER_PASSWORD = "randomPassword";

    private static int imageCount = 0;
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private byte[][] faceImages = new byte[MAX_IMAGE_COUNT][];
    Button captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureButton = (Button) findViewById(R.id.captureButton);
        verifyPermissions(this);
    }

    public void recordFace(View view) {
        captureButton.setVisibility(Button.INVISIBLE);
        if (safeCameraOpen()) {
            captureImages();
        }
    }

    private void captureImages() {
        imageCount = 0;
        new CountDownTimer(MAX_RECORD_TIME + START_DELAY, RECORD_INTERVALS) {
            @Override
            public void onTick(long millisUntilFinished) {
                mCamera.takePicture(null, null, mPicture);
            }

            @Override
            public void onFinish() {
                captureButton.setVisibility(Button.VISIBLE);
                safeCameraClose();
                try {
                    storeImages();
                } catch (Exception e) {
                    Log.e("MainActivity", "storeImages failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void storeImages() throws Exception {
        SecretKey key = generateKey(USER_PASSWORD);
        FileOutputStream fileOutputStream = openFileOutput(IMAGE_FILE, Context.MODE_PRIVATE);

        for (int i = 0; i < faceImages.length; i++) {
            ;
            fileOutputStream.write(encrypt(key, faceImages[i]));
        }
        fileOutputStream.close();
        Log.d("storeImages", "Images encrypted and stored");
    }

    private static SecretKey generateKey(String password) throws Exception{
        DESKeySpec keySpec = new DESKeySpec(password.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(MODE);
        SecretKey key = keyFactory.generateSecret(keySpec);
        return key;
    }

    private static byte[] encrypt(SecretKey key, byte[] clear) throws Exception {
        Cipher cipher = Cipher.getInstance(MODE);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            faceImages[imageCount++] = data;
            Log.d("imageCount", ""+imageCount);
        }
    };

    private Camera getFrontFacingCamera() {
        int cameraCount = 0;
        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIndex = 0; camIndex < cameraCount; camIndex++) {
            Camera.getCameraInfo(camIndex, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    camera = Camera.open(camIndex);
                } catch (Exception e) {
                    Log.e("MainActivity ERROR", "safeCameraOpen - failed to get camera: " + e.getMessage());
                }
            }
        }
        return camera;
    }

    private boolean safeCameraOpen() {
        mCamera = getFrontFacingCamera();

        if (mCamera != null) {
            mCameraView = new CameraView(this, mCamera);
            FrameLayout camera_view = (FrameLayout) findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);
            return true;
        }

        return false;
    }

    private void safeCameraClose() {
        if (mCamera != null) {
            FrameLayout camera_view = (FrameLayout) findViewById(R.id.camera_view);
            camera_view.removeView(mCameraView);
        }
    }

    private static void verifyPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST_CAMERA);
        }
    }

}
