package com.intsig.yann.analysis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Camera_test extends AppCompatActivity implements SurfaceHolder.Callback, SensorEventListener {

    private Camera mCamera1;
    private Button button1;

    private ImageView mImageView1;
    private TextView mTextView1;
    private String TAG = "HIPPO";
    private SurfaceView mSurfaceView1;
    private DrawCircle mCircle;
    private SurfaceHolder mSurfaceHolder1;

    private boolean bIfPreview = true;

    private int numberOfCameras;
    private CameraInfo cameraInfo;
    private int defaultCameraId; // 后置
    public int facingCameraId; // 前置
    public int cameraCurrentlyLocked;
    private int intScreenRotating;
    private Size mPreviewSize;
    private List<Size> mSupportedPreviewSizes;
    private String URI;

    @SuppressLint("SdCardPath")
    private String strCaptureFilePath = Util.ORIGINAL_IMG;
    private int mSensorRotation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        hideNavigationBar();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_camera);

        Intent intent = getIntent();
        URI = intent.getStringExtra("uri");

        getCameraInfo();

        if (!checkSDCard()) {
            mMakeTextToast(getResources().getText(R.string.need_sdcard_permission).toString(), true);
        }

        DisplayMetrics dm = new DisplayMetrics();// 取得屏幕解析像素
        Log.d("", dm.toString());
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        DrawOnTop mDraw = new DrawOnTop(this);
        addContentView(mDraw, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        //mCircle = (DrawCircle)findViewById(R.id.mCircle);
        mTextView1 = findViewById(R.id.myTextView1);

        //以SurfaceView作为相机preview之用
        mSurfaceView1 = findViewById(R.id.mSurfaceView1);

        //绑定SurfaceView，取得SurfaceHolder对象
        mSurfaceHolder1 = mSurfaceView1.getHolder();
        mSurfaceHolder1.setFormat(PixelFormat.TRANSPARENT);

        //Activity必须实现SurfaceHolder.Callback
        mSurfaceHolder1.addCallback(this);

        /*
         * 以SRUFACE_TYPE_PUSH_BUFFERS(3)
         * 作为SurfaceHolder显示类型
         * */
        //mSurfaceHolder1.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        button1 = findViewById(R.id.mybutton1);

        //打开相机及preview

        initCamera();

        //拍照
        button1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (checkSDCard()) {
                    takePicture();
                    //exitActivity(2);
                } else {
                    mTextView1.setText(getResources().getText(R.string.need_sdcard_permission).toString());
                }
            }
        });
    }

    class DrawOnTop extends View {
        public DrawOnTop(Context context) {
            super(context);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = new Paint();
            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.GREEN);
            canvas.drawColor(Color.TRANSPARENT);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(7f);
            paint.setColor(Color.RED);
            //canvas.drawText("调整手机，将眼睛放入圆圈", 300, 500, p);
            canvas.drawCircle(300, 700, 100, paint);
            canvas.drawCircle(780, 700, 100, paint);
            super.onDraw(canvas);
        }
    }

    private void getCameraInfo() {
        numberOfCameras = Camera.getNumberOfCameras();
        cameraInfo = new CameraInfo();
        /*
         * 这里在判断前置还是后置的时候，前置与后置反过来了，暂时还不知道为什么
         * 即：defaultCameraID是前置，facingCameraID是后置。。。
         * */
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            //判断如果是后置
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
            } else if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                facingCameraId = i;
            }
        }
    }

    private void initCamera() {
        if (bIfPreview) {
            try {
                cameraCurrentlyLocked = defaultCameraId;//默认用了前置相机
                //根据相机数量，指定开启相机ID
                mCamera1 = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras);
                //已开启的相机ID
                cameraCurrentlyLocked = (cameraCurrentlyLocked + 1) % numberOfCameras;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        if (mCamera1 != null && bIfPreview) {
            Log.i(TAG, "inside the camera");

            //取得相机支持的像素
            mSupportedPreviewSizes = mCamera1.getParameters().getSupportedPreviewSizes();
            WindowManager wm = (WindowManager) Camera_test.this.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            int height = width * 4 / 3;

            if (mSupportedPreviewSizes != null) {
                //指定相机预览的像素分辨率
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }

            //取得相机的设置参数
            Camera.Parameters parameters = mCamera1.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

            //设置拍照后的相片图像格式
            parameters.setPictureFormat(PixelFormat.JPEG);

            //重新设置相机参数
            mCamera1.setParameters(parameters);
            //setCameraDisplayOrientation(mCamera1);
            try {
                mCamera1.setPreviewDisplay(mSurfaceHolder1);
                int degree = setCameraDisplayOrientation(mCamera1);
                mCamera1.setDisplayOrientation(90);
                //setCameraDisplayOrientation(mCamera1);
                //立即执行preview
                mCamera1.startPreview();
                bIfPreview = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    //拍照捕捉影像
    private void takePicture() {
        if (mCamera1 != null && bIfPreview) {
            //调用takePicture方法拍照，传入三个返回值
            mCamera1.takePicture(shutterCallback, rawCallback, jpegCallback);
        }
    }

    //相机重置
    private void resetCamera() {
        if (mCamera1 != null && bIfPreview) {
            mCamera1.stopPreview();

            mCamera1.release();
            mCamera1 = null;

            Log.i(TAG, "stopPreview");
            bIfPreview = false;
        }
    }

    //设置相机显示方向
    private int setCameraDisplayOrientation(Camera camera) {
        intScreenRotating = Camera_test.this.getWindowManager().getDefaultDisplay().getRotation();

        int degrees = 0;
        int result;
        //根据当前的intScreenRotation, 旋转Surface
        switch (intScreenRotating) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        cameraInfo = new CameraInfo();
        int orientation = 0;
        for (int i = 0; i < numberOfCameras; i++) {
            //若是前置
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = (cameraInfo.orientation + degrees) % 360;
                result = (360 - result) % 360;
            } else {
                //后置
                result = (cameraInfo.orientation - degrees + 360) % 360;
            }
            //camera.setDisplayOrientation(result);
            orientation = result;
        }
        return orientation;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //手机移动一段时间后静止，然后静止一段时间后进行对焦
        // 读取加速度传感器数值，values数组0,1,2分别对应x,y,z轴的加速度
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];

        }

        mSensorRotation = calculateSensorRotation(event.values[0], event.values[1]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int calculateSensorRotation(float x, float y) {
        //x是values[0]的值，X轴方向加速度，从左侧向右侧移动，values[0]为负值；从右向左移动，values[0]为正值
        //y是values[1]的值，Y轴方向加速度，从上到下移动，values[1]为负值；从下往上移动，values[1]为正值
        //不考虑Z轴上的数据，
        if (Math.abs(x) > 6 && Math.abs(y) < 4) {
            if (x > 6) {
                return 270;
            } else {
                return 90;
            }
        } else if (Math.abs(y) > 6 && Math.abs(x) < 4) {
            if (y > 6) {
                return 0;
            } else {
                return 180;
            }
        }

        return -1;
    }


    //取得最佳分辨率
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {

        //允许缩放的宽高比
        final double ASPECT_TOLERANCE = 0.1;
        //以宽为主，计算缩放比
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;

        //尝试找出最佳宽高比与宽高指
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        //无法找到核时的宽高比，以绝对值取得等比缩放值
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private ShutterCallback shutterCallback = new ShutterCallback() {
        @Override
        public void onShutter() {
            //快门关闭的callback()
        }
    };

    private PictureCallback rawCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //TODO Handle RAW image data
        }
    };

    private PictureCallback jpegCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //TODO Handle RAW image data
            //传入的第一个参数即为相片的byte
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            Log.d("", "hello");

            //创建新文件
            File myCaptureFile = new File(strCaptureFilePath);
            if (!myCaptureFile.exists()) {
                myCaptureFile.mkdir();
            }

            File Picturefile = new File(URI);
            if (Picturefile.exists()) {
                Picturefile.delete();
            }
            final Bitmap result;
            //Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            int rotation = (setCameraDisplayOrientation(mCamera1) + mSensorRotation) % 360;
            if (cameraCurrentlyLocked == Camera.CameraInfo.CAMERA_FACING_BACK) {
                matrix.setRotate(rotation);
            } else {
                rotation = (270 - rotation) % 360;
                matrix.setRotate(rotation);
                matrix.postScale(-1, 1);
            }
            result = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

            try {
                FileOutputStream fos = new FileOutputStream(Picturefile);
                //fos.write(data);
                result.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                fos.flush();
                fos.close();
                //再次重启相机继续预览
                initCamera();

                Intent intent = new Intent();
                intent.putExtra("uri", Picturefile);
                setResult(Activity.RESULT_OK, intent);
                finish();

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                Log.e(TAG, e.toString());
            }
        }
    };

    //自定义删除文件函数
    private void delFile(String strFileName) {
        try {
            File myFile = new File(strFileName);
            if (myFile.exists()) {
                myFile.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public void mMakeTextToast(String str, boolean isLong) {
        if (isLong) {
            Toast.makeText(Camera_test.this, str, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(Camera_test.this, str, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceholder) {
        try {
            if (mCamera1 != null) {
                mCamera1.setPreviewDisplay(surfaceholder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "setPreviewDisplay()", exception);
        }
        Log.i(TAG, "Surface Changed2");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceCreated(SurfaceHolder surfaceholder, int format, int w, int h) {
        //TODO Auto-generated method stub
        Log.i(TAG, "Surface Changed1");
    }

    public void exitActivity(int exitMethod) {
        try {
            switch (exitMethod) {
                case 0:
                    System.exit(0);
                    break;
                case 1:
                    android.os.Process.killProcess(android.os.Process.myPid());
                    break;
                case 2:
                    finish();
                    break;
            }
        } catch (Exception e) {
            finish();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        try {
            //delFile(strCaptureFilePath);
            mCircle.clearDraw();
            mCamera1.stopPreview();
            mCamera1.release();
            mCamera1 = null;
            Log.i(TAG, "Surface Destroyed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideStatusBar() {
        // 隐藏标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        // 获得窗口对象
        Window curWindow = this.getWindow();
        // 设置Flag标示
        curWindow.setFlags(flag, flag);
    }

    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void showNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    protected void onPause() {
        try {
            resetCamera();
            mCamera1.release();
            exitActivity(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, Camera_test.class);
        activity.startActivity(intent);
    }

}