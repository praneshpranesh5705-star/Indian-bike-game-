package com.pranesh.bikeai;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager wm; private View bubble;
    @Override public void onCreate(){ super.onCreate(); if(!Settings.canDrawOverlays(this)) return; wm=(WindowManager)getSystemService(WINDOW_SERVICE); showBubble(); }
    private void showBubble(){
        TextView v=new TextView(this); v.setText("🤖"); v.setTextSize(24); v.setTextColor(Color.WHITE); v.setGravity(Gravity.CENTER);
        GradientDrawable bg=new GradientDrawable(); bg.setColor(Color.rgb(255,109,0)); bg.setShape(GradientDrawable.OVAL); v.setBackground(bg); v.setElevation(12);
        final int type=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; final WindowManager.LayoutParams p=new WindowManager.LayoutParams(64,64,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT); p.gravity=Gravity.CENTER_VERTICAL|Gravity.RIGHT; p.x=18; p.y=0;
        v.setOnClickListener(x->{ Intent i=new Intent(this,MainActivity.class); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); });
        v.setOnTouchListener(new View.OnTouchListener(){ float downX,downY; int startX,startY; boolean moved; public boolean onTouch(View view,MotionEvent e){ if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getRawX();downY=e.getRawY();startX=p.x;startY=p.y;moved=false;return true;} if(e.getAction()==MotionEvent.ACTION_MOVE){float dx=e.getRawX()-downX,dy=e.getRawY()-downY;if(Math.abs(dx)>8||Math.abs(dy)>8)moved=true;p.x=startX-(int)dx;p.y=startY+(int)dy;wm.updateViewLayout(view,p);return true;} if(e.getAction()==MotionEvent.ACTION_UP&&!moved){view.performClick();} return true;}});
        bubble=v; wm.addView(v,p);
    }
    @Override public void onDestroy(){if(wm!=null&&bubble!=null)wm.removeView(bubble);super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
