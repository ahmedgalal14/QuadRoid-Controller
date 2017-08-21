package com.example.salsabeel.client;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class controlActivity extends AppCompatActivity {
    private static final String TAG = "PPC";
    ImageView imview;
    int len;
    Socket toServer;
    public DataInputStream dis;
    byte[] buffer;

    Socket socket;
    OutputStream outputStream;
    DataOutputStream dataOutputStream;

    SeekBar aiel,eval,throttle,rudd;
    int x=500,y=500,z=500,speed=0;

    boolean isConnected = false;
    String message= "0";
    String ip = "";

    Button disconnectBtn;
    Intent mainActivity;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        Intent intent = getIntent();
        ip = intent.getStringExtra("ServerIP");

        disconnectBtn = (Button) findViewById(R.id.DisconectBtn);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConnected = false;
                try
                {
                    socket.close();
                    toServer.close();
                    v.setEnabled(false);
                    mainActivity = new Intent(controlActivity.this,MainActivity.class);
                    controlActivity.this.startActivity(mainActivity);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isCon = setIsConnected();
                while(true)
                {
                    try{
                        toServer=new Socket(ip,8000);
                        isConnected = true;
                        break;
                    }
                    catch(Exception e){
                        Log.i("preview","couldn't connect to server");
                    }
                }
                try {
                    dis = new DataInputStream(toServer.getInputStream());//create an input stream for the socket, image frames will come through this
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    outputStream = toServer.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dataOutputStream = new DataOutputStream(outputStream);

                isCon = setIsConnected();
                while (isCon) {
                    try {
                        message = "*" + String.valueOf(x) +"," + String.valueOf(y) + "," + String.valueOf(z) +","+ String.valueOf(speed) + "$";
                        Log.i("client: ", "sending message " + message);
                        try {
                            dataOutputStream.writeUTF(message);
                        } catch (IOException e) {
                            Log.i("Server: ","data stream not set");
                        }

                        len = Integer.parseInt("" + dis.readInt()); // get the size of the incoming image frame i.e. size of the byte array
                        buffer = new byte[len];// create the byte array where we will save the image data
                        dis.readFully(buffer, 0, len);//read the image data from the socket inputstream
                        Log.i("loop", "read successfully");

                        //new tofile().execute(buffer);
                        new frame2video().execute(buffer);

                    } catch (EOFException e) {
                        Log.i("receive task", "EOF");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try
                    {
                        Thread.sleep(20);
                    }
                    catch (Exception e){}
                    isCon = setIsConnected();
                }
            }
        }).start();

        //new sendMessageTask().execute();
        for (int i = 0 ; i < 10000000 ; i++);
        if (!isConnected)
        {
            Toast.makeText(getApplicationContext(),"Make sure you are Connected to the Quadcopter HOTSPOT",Toast.LENGTH_LONG).show();
            isConnected = false;
        }
        //Toast.makeText(getApplicationContext(),getIpAddress(),Toast.LENGTH_LONG).show();

        aiel = (SeekBar) findViewById(R.id.aiel);
        aiel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                x = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                x = 500;
                seekBar.setProgress(x);
            }
        });

        eval = (SeekBar) findViewById(R.id.eval);
        eval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                y = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                y = 500;
                seekBar.setProgress(y);
            }
        });


        rudd = (SeekBar) findViewById(R.id.rudd);
        rudd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                z = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                z = 500;
                seekBar.setProgress(z);
            }
        });

        throttle = (SeekBar) findViewById(R.id.throttle);
        throttle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public class frame2video extends AsyncTask<byte[],Void,Bitmap> {// class that deals with taking byte array and displaying it onto an imageview


        protected void onPreExecute() {

            imview = (ImageView) findViewById(R.id.imview);// initialize the image view component
        }


        protected Bitmap doInBackground(byte[]... buffer) {

            Log.i("to video","started");
            try {
                Bitmap image = BitmapFactory.decodeByteArray(buffer[0], 0, len); // decode the byte array received to a bitmap format
                return image; // return the image object

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(Bitmap b) {
            imview.setImageBitmap(b); //set the image view to the image object returned by doInBackground(byte[]...buffer) method
            imview.invalidate();// attempt to refresh the canvas in imview
        }
    }

    class sendMessageTask extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                while(true) {
                    try {
                        socket = new Socket(ip, 8080);
                        Log.i("server: ", "connected");
                        isConnected = true;
                        break;
                    } catch (Exception e) {
                        Log.i("quad", "couldn't connect to server 8080");
                    }
                }
                outputStream = socket.getOutputStream();
                dataOutputStream = new DataOutputStream(outputStream);

                boolean isCon = setIsConnected();

                // sending to server
                while (isCon) {
                    message = "*" + String.valueOf(x) +"," + String.valueOf(y) + "," + String.valueOf(z) +","+ String.valueOf(speed) + "$";
                    Log.i("client: ", "sending message " + message);
                    try {
                        dataOutputStream.writeUTF(message);
                    } catch (IOException e) {
                        Log.i("Server: ","data stream not set");
                    }
                    try {
                        Thread.sleep(50);
                    } catch (Exception ex) {
                    }
                    isCon = setIsConnected();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            /*
            // receiving from server
            dataInputStream = new DataInputStream(inputStream);

            try {
                message= dataInputStream.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            t1=System.currentTimeMillis();
            Log.i("client: ","meesage "+message+ " received from server after "+t1+" \n");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"meesage "+message+ " received from server after "+t1+" \n",Toast.LENGTH_SHORT).show();
                }
            });
            */
            return null;
        }
    }


    public boolean setIsConnected()
    {
        return isConnected;
    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Connect Controller to ip at: "
                                + inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    public class tofile extends AsyncTask<byte[],Void,Void>
    {
        @Override
        protected Void doInBackground(byte[]... params) {
            Log.i("f2f","task");
            try{

                Log.i("f2f","task");
                DataOutputStream dos=new DataOutputStream(new FileOutputStream("/sdcard/"+System.currentTimeMillis()+".jpg"));//create the file output stream to write the data onto the sdcard
                Log.i("f2f","dos created");
                dos.write(params[0]);//write the byte array to the file
                Log.i("f2f","buffer written");
                dos.flush();

                try{
                    dos.close();//close the file stream object returned from doInBackground(byte[]...buffer)
                }

                catch(Exception e){e.printStackTrace();}

            }

            catch(Exception e){
                Log.i("f2f","error");
                e.printStackTrace();
            }


            return null;

        }
    }
}
