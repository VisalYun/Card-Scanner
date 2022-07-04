package com.example.cardscanner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.util.Rational;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Executor executor = Executors.newSingleThreadExecutor();
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private int REQUEST_CODE_PERMISSIONS = 1001;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private Button captureButton;
    private ImageView imgViewer;
    private CardView cardView;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (hasAllPermission()) {
            previewView = (PreviewView) findViewById(R.id.viewFinder);
            captureButton = (Button) findViewById(R.id.captureBtn);
            imgViewer = (ImageView) findViewById(R.id.imgViewer);
            cardView = (CardView) findViewById(R.id.cardView);

            captureButton.setOnClickListener(v -> {
                captureButton.setVisibility(View.INVISIBLE);
                cardView.setVisibility(View.INVISIBLE);
                previewView.setVisibility(View.VISIBLE);
            });

            captureButton.setVisibility(View.INVISIBLE);
            cardView.setVisibility(View.INVISIBLE);

            cameraProviderFuture = ProcessCameraProvider.getInstance(this);
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e){
                    // this is error handling
                }
            }, ContextCompat.getMainExecutor(this));
        } else {
            requestPermission();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        final ImageCapture imageCapture = new ImageCapture.Builder()
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this,cameraSelector, preview, imageCapture);

        previewView.setOnClickListener(v -> {
            imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback () {
                @Override
                public void onCaptureSuccess(ImageProxy image) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            Bitmap bitmap = changeBitmapContrastBrightness(previewView.getBitmap(), 1, 25);
                            imgViewer.setImageBitmap(bitmap);

                            previewView.setVisibility(View.INVISIBLE);
                            cardView.setVisibility(View.VISIBLE);
                            captureButton.setVisibility(View.VISIBLE);
                        }
                    });
                    super.onCaptureSuccess(image);
                }
                @Override
                public void onError(ImageCaptureException error) {
                    error.printStackTrace();
                }
            });
        });
    }

    Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness)
    {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    boolean hasAllPermission() {
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
        );
    }
}