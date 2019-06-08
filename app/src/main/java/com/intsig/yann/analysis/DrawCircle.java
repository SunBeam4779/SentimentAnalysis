package com.intsig.yann.analysis;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
//import android.view.View;

/*定义一个画矩形框的类*/
public class DrawCircle extends SurfaceView implements SurfaceHolder.Callback {

    private Bitmap bmp;
    protected SurfaceHolder sh;
    private int mWidth;
    private int mHeight;
    private int width;
    private int height;

    public DrawCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        sh = getHolder();
        sh.addCallback(this);
        sh.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int w, int h) {
        // TODO Auto-generated method stub
        mWidth = w;
        mHeight = h;
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        // TODO Auto-generated method stub

    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub

    }

    void clearDraw() {
        Canvas canvas = sh.lockCanvas();
        canvas.drawColor(Color.BLUE);
        sh.unlockCanvasAndPost(canvas);
    }

    public void doDraw() {
        if (bmp != null) {
            Canvas canvas = sh.lockCanvas();
            canvas.drawColor(Color.TRANSPARENT);// 这里是绘制背景
            Paint p = new Paint(); // 笔触
            p.setAntiAlias(true); // 反锯齿
            p.setColor(Color.GREEN);
            p.setStyle(Style.STROKE);
            canvas.drawBitmap(bmp, 0, 0, p);
            canvas.drawLine(width / 2 - 100, 0, width / 2 - 100, height, p);
            canvas.drawLine(width / 2 + 100, 0, width / 2 + 100, height, p);
            // ------------------------ 画边框---------------------
            Rect rec = canvas.getClipBounds();
            rec.bottom--;
            rec.right--;
            p.setColor(Color.GRAY); // 颜色
            p.setStrokeWidth(5);
            canvas.drawRect(rec, p);
            // 提交绘制
            sh.unlockCanvasAndPost(canvas);
        }

    }


    public void drawLine() {
        if (bmp != null) {//必须加上否则会崩
            Canvas canvas = sh.lockCanvas();
            canvas.drawColor(Color.WHITE);
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setColor(Color.WHITE);
            p.setStyle(Style.STROKE);
            p.setStrokeWidth(2.5f);
            //canvas.drawPoint(100.0f, 100.0f, p);
            //canvas.drawBitmap(bmp, 0, 0, p);
            //canvas.drawLine(0, 110, 500, 110, p);
            canvas.drawCircle(80, 80, 100, p);
            sh.unlockCanvasAndPost(canvas);
        }
    }

}
