package com.pranesh.bikeai;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
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
    private TextView chat;
    private EditText input;
    private static final String DEFAULT_API = "";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        requestOverlayThenStart();
        buildChat();
    }

    private void requestOverlayThenStart() {
        if (!Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } else {
            startService(new Intent(this, OverlayService.class));
        }
    }

    private void buildChat() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,24,24,24);
        TextView title = new TextView(this); title.setText("🤖 Bike AI Assistant"); title.setTextSize(24); title.setTextColor(Color.BLACK); title.setPadding(0,0,0,18); root.addView(title);
        ScrollView sv = new ScrollView(this); chat = new TextView(this); chat.setText("Ask me about bikes, game strategy, missions or fictional driving challenges.\n\n"); chat.setTextSize(17); chat.setTextColor(Color.DKGRAY); sv.addView(chat); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
        input = new EditText(this); input.setHint("Ask Bike AI..."); row.addView(input,new LinearLayout.LayoutParams(0,-2,1));
        Button send = new Button(this); send.setText("Send"); row.addView(send); root.addView(row);
        Button close = new Button(this); close.setText("Back to game"); root.addView(close);
        send.setOnClickListener(v -> ask()); close.setOnClickListener(v -> finish());
        setContentView(root);
    }

    private void ask() {
        final String q = input.getText().toString().trim(); if(q.isEmpty()) return;
        chat.append("\nYou: " + q + "\n"); input.setText("");
        executor.execute(() -> {
            String reply;
            try {
                String api = getPreferences(0).getString("api", DEFAULT_API);
                if(api.isEmpty()) throw new Exception("AI server URL is not configured yet.");
                HttpURLConnection c=(HttpURLConnection)new URL(api).openConnection(); c.setRequestMethod("POST"); c.setConnectTimeout(15000); c.setReadTimeout(30000); c.setDoOutput(true); c.setRequestProperty("Content-Type","application/json");
                JSONObject body=new JSONObject(); body.put("message",q); try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes(StandardCharsets.UTF_8));}
                InputStream is=c.getResponseCode()<400?c.getInputStream():c.getErrorStream(); BufferedReader br=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8)); StringBuilder sb=new StringBuilder(); String line; while((line=br.readLine())!=null) sb.append(line);
                JSONObject out=new JSONObject(sb.toString()); reply=out.optString("reply",out.optString("error","No response"));
            } catch(Exception e){ reply="AI is not connected yet: " + e.getMessage(); }
            final String r=reply; runOnUiThread(() -> chat.append("AI: " + r + "\n"));
        });
    }
}
