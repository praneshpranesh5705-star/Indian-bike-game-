package com.pranesh.bikeai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView chat; private EditText input;
    @Override public void onCreate(Bundle b) { super.onCreate(b); buildChat(); requestOverlayThenStart(); }
    @Override protected void onResume(){ super.onResume(); if(Settings.canDrawOverlays(this)) startService(new Intent(this, OverlayService.class)); }
    private void requestOverlayThenStart() {
        if (!Settings.canDrawOverlays(this)) startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        else startService(new Intent(this, OverlayService.class));
    }
    private void buildChat() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,24,24,24);
        TextView title=new TextView(this); title.setText("🤖 Bike AI Assistant"); title.setTextSize(24); title.setTextColor(Color.BLACK); root.addView(title);
        Button config=new Button(this); config.setText("⚙ Set AI Server URL"); root.addView(config);
        ScrollView sv=new ScrollView(this); chat=new TextView(this); chat.setText("Ask me about bikes, game strategy, missions or fictional driving challenges.\n\n"); chat.setTextSize(17); chat.setTextColor(Color.DKGRAY); sv.addView(chat); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout row=new LinearLayout(this); input=new EditText(this); input.setHint("Ask Bike AI..."); row.addView(input,new LinearLayout.LayoutParams(0,-2,1)); Button send=new Button(this); send.setText("Send"); row.addView(send); root.addView(row);
        Button close=new Button(this); close.setText("Back to game"); root.addView(close);
        config.setOnClickListener(v->setApiUrl()); send.setOnClickListener(v->ask()); close.setOnClickListener(v->finish()); setContentView(root);
    }
    private void setApiUrl(){
        EditText e=new EditText(this); e.setSingleLine(true); e.setHint("https://your-domain/api/ai"); e.setText(getPreferences(0).getString("api",""));
        new AlertDialog.Builder(this).setTitle("AI server URL").setMessage("Enter your deployed /api/ai endpoint. Keep your OpenAI API key on the server, never in this app.").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->getPreferences(0).edit().putString("api",e.getText().toString().trim()).apply()).show();
    }
    private void ask(){
        final String q=input.getText().toString().trim(); if(q.isEmpty()) return; chat.append("\nYou: "+q+"\n"); input.setText("");
        executor.execute(()->{ String reply; try{
            String api=getPreferences(0).getString("api",""); if(api.isEmpty()) throw new Exception("Set the AI server URL first.");
            HttpURLConnection c=(HttpURLConnection)new URL(api).openConnection(); c.setRequestMethod("POST"); c.setConnectTimeout(15000); c.setReadTimeout(30000); c.setDoOutput(true); c.setRequestProperty("Content-Type","application/json");
            JSONObject body=new JSONObject(); body.put("message",q); try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes(StandardCharsets.UTF_8));}
            InputStream is=c.getResponseCode()<400?c.getInputStream():c.getErrorStream(); BufferedReader br=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8)); StringBuilder sb=new StringBuilder(); String line; while((line=br.readLine())!=null)sb.append(line); JSONObject out=new JSONObject(sb.toString()); reply=out.optString("reply",out.optString("error","No response"));
        }catch(Exception e){reply="AI connection error: "+e.getMessage();} final String r=reply; runOnUiThread(()->chat.append("AI: "+r+"\n")); });
    }
}
