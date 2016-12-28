package com.example.dmayc.project2;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.app.ProgressDialog;
import android.widget.Toast;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MainActivity extends AppCompatActivity{
    private ImageButton ib_bottom;
    private int status;
    private ProgressDialog dialog;
    private int[] images = {R.mipmap.btn_close,R.mipmap.btn_open};
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == 0x11){
                ib_bottom.setImageResource(images[status]);
            }
        }
    };
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ib_bottom = (ImageButton) findViewById(R.id.btn_close);
        status = 0;
        MyView.status = 0;
        ib_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setTitle("连接中");
                dialog.setMessage("尝试服务器进行连接");
                dialog.show();
                if(status == 0){
                    try{
                        new socketthread("open").start();
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
                else if(status ==1){
                    try{
                        new socketthread("close").start();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        });
    }
    class drawthread extends Thread{

    }
    class socketthread extends Thread{
        public String text;
        public socketthread(String str){
            text = str;
        }
        public void run(){
            try{
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("120.24.12.197", 8801), 5000);
                PrintStream ps = new PrintStream(socket.getOutputStream(),true,"UTF-8");
                ps.print(text);
//                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
//                out.print(text);
                DataInputStream is = new DataInputStream(socket.getInputStream());
                byte[] b = new byte[1024];
                int length = is.read(b);
                String message = new String(b,0,length,"UTF-8");
                Log.d("HELLO", message);
//                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                String message = in .readLine();
                if (message.equals("okay")){
                    Log.d("here","henhao");
                    status = (status+1)%2;
                    MyView.status = status;
                    dialog.dismiss();
                    Message msg = new Message();
                    msg.what = 0x11;
                    handler.sendMessage(msg);
                }
                else if(message.equals("error")){
                    Log.d("here","henbuhao");
                    dialog.dismiss();
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"插座貌似没有联网 :(", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                else{
                    Log.d("yanzhongdecuowu","error");
                }
                ps.close();
                is.close();
                socket.close();
            }catch (SocketTimeoutException e){
                Log.d("timeout", "timeout");
                dialog.dismiss();
                Looper.prepare();
                Toast.makeText(getApplicationContext(),"连不上服务器，请检查网络设置", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}
