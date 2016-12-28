package com.example.dmayc.project2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.view.View;

public class MyView extends View{
    private int XPoint = 120;
    private int YPoint = 600;
    private int XScale = 30;  //刻度长度
    private int YScale = 30;
    private int XLength = 600;
    private int YLength = 600;

    public static int status = 0;
    private int MaxDataSize = XLength / XScale;

    private List<Integer> data = new ArrayList<Integer>();

    private String[] YLabel = new String[YLength / YScale];

    public Handler d_handler = new Handler(){
        public void handleMessage(Message msg) {
            if(msg.what == 0x1234){
                MyView.this.invalidate();
            }
        };
    };
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        for(int i=0; i<YLabel.length; i++){
            YLabel[i] = (i*5) + "mA";
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(data.size() >= MaxDataSize){
                        data.remove(0);
                    }
                    if(status ==0){
                        data.add(0);//这里原来是4
                        d_handler.sendEmptyMessage(0x1234);
                    }
                    if(status== 1){
                        int i = new Random().nextInt(10);
                        if(i<8){
                            data.add(new Random().nextInt(5) + 10);//这里原来是4
                            d_handler.sendEmptyMessage(0x1234);
                        }
                        else{
                            data.add(new Random().nextInt(14) + 5);//这里原来是4
                            d_handler.sendEmptyMessage(0x1234);
                        }

                    }


                }
            }
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true); //去锯齿
        paint.setColor(Color.BLUE);

        //画Y轴
        canvas.drawLine(XPoint, YPoint - YLength, XPoint, YPoint, paint);

        //Y轴箭头
        canvas.drawLine(XPoint, YPoint - YLength, XPoint - 3, YPoint-YLength + 6, paint);  //箭头
        canvas.drawLine(XPoint, YPoint - YLength, XPoint + 3, YPoint-YLength + 6 ,paint);

        //添加刻度和文字
        for(int i=0; i * YScale < YLength; i++) {
            canvas.drawLine(XPoint, YPoint - i * YScale, XPoint + 5, YPoint - i * YScale, paint);  //刻度

            canvas.drawText(YLabel[i], XPoint - 50, YPoint - i * YScale, paint);//文字
        }

        //画X轴
        canvas.drawLine(XPoint, YPoint, XPoint + XLength, YPoint, paint);
        System.out.println("Data.size = " + data.size());
        if(data.size() > 1){
            for(int i=1; i<data.size(); i++){
                canvas.drawLine(XPoint + (i-1) * XScale, YPoint - data.get(i-1) * YScale,
                        XPoint + i * XScale, YPoint - data.get(i) * YScale, paint);
            }
        }
    }
}