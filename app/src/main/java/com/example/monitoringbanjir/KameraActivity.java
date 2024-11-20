package com.example.monitoringbanjir;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class KameraActivity extends AppCompatActivity {
    private ImageButton cameraButton, connectButton;
    private ImageView monitor;
    private HttpURLConnection connection;

    boolean connected = false;
    BufferedInputStream bis = null;
    private String streamUrl;
    private String captureUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kamera);

        connectButton = findViewById(R.id.connect);
        cameraButton = findViewById(R.id.camerabutton);
        monitor = findViewById(R.id.monitor);

        DatabaseReference Stream = FirebaseDatabase.getInstance().getReference("Stream");
        DatabaseReference foto = FirebaseDatabase.getInstance().getReference("foto");

        Stream.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                streamUrl = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        foto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                captureUrl = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });


        // Connect/Disconnect Button
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connected) {
                    // Disconnect
                    connection.disconnect();
                    connectButton.setBackgroundResource(R.drawable.rounded_button);
                    Toast.makeText(KameraActivity.this, "Kamera dimatikan!", Toast.LENGTH_SHORT).show();
                    connected = false;
                } else {
                    // Connect
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(streamUrl);
                                try {
                                    // http parameters
                                    connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.setConnectTimeout(1000 * 5);
                                    connection.setReadTimeout(1000 * 5);
                                    connection.setDoInput(true);
                                    connection.connect();

                                    if (connection.getResponseCode() == 200) {
                                        // If connection successful, handle stream video
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                connectButton.setBackgroundResource(R.drawable.rounded_button_2);;
                                                Toast.makeText(KameraActivity.this, "Kamera dinyalakan!", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        connected = true;
                                        Log.d("TAG", "Connected to server.");
                                        InputStream in = connection.getInputStream();
                                        InputStreamReader isr = new InputStreamReader(in);
                                        BufferedReader br = new BufferedReader(isr);

                                        String data;
                                        int len;
                                        byte[] buffer;

                                        while ((data = br.readLine()) != null) {
                                            if (data.contains("Content-Type:")) {
                                                data = br.readLine();
                                                len = Integer.parseInt(data.split(":")[1].trim());
                                                bis = new BufferedInputStream(in);
                                                buffer = new byte[len];

                                                int t = 0;
                                                while (t < len) {
                                                    t += bis.read(buffer, t, len - t);
                                                }

                                                // Convert buffer to Bitmap
                                                final Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);

                                                // Set the Bitmap to ImageView (monitor)
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        monitor.setImageBitmap(bitmap);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }
        });

        //Capture Gambar
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //to do togle button connect and disconnect

                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        BufferedInputStream bis = null;
                        FileOutputStream fos = null;

                        try {
                            //do get camera
                            URL url = new URL(captureUrl);

                            try {

                                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                                huc.setRequestMethod("GET");
                                huc.setConnectTimeout(1000 * 5);
                                huc.setReadTimeout(1000 * 5);
                                huc.setDoInput(true);
                                huc.connect();
                                if (huc.getResponseCode() == 200) {
                                    InputStream in = huc.getInputStream();
                                    bis = new BufferedInputStream(in);

                                    // You can set the file path and name where you want to save the image
                                    String appName = getResources().getString(R.string.app_name); // Get the name of your app from resources
                                    String folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + appName;

                                    // Create the folder if it doesn't exist
                                    File folder = new File(folderPath);
                                    if (!folder.exists()) {
                                        folder.mkdirs();
                                    }


                                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                                    //String fileName = "sophia2.jpg"; // The desired file name
                                    String fileName = "IMG_" + timeStamp + ".jpg"; // Unique file name with timestamp

                                    String filePath = folderPath + "/" + fileName;

                                    fos = new FileOutputStream(filePath);

                                    byte[] buffer = new byte[1024];
                                    int bytesRead;

                                    while ((bytesRead = bis.read(buffer)) != -1) {
                                        fos.write(buffer, 0, bytesRead);
                                    }

                                    fos.flush();
                                    fos.getFD().sync();

                                    final Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            monitor.setImageBitmap(bitmap);
                                        }
                                    });
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showToast("Nice, gambar berhasil disimpan!");
                                        }
                                    });



                                }
                            } catch (Exception e) {
                                e.printStackTrace();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast("Maaf, periksa kembali koneksi internet anda!");
                                    }
                                });


                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (bis != null) {
                                    bis.close();
                                }
                                if (fos != null) {
                                    fos.close();
                                }


                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
                thread1.start();
            }
        });
    }


    private void Bytes2ImageFile(byte[] bytes, String fileName)
    {
        try
        {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }




    @Override
    protected void onPause() {
        super.onPause();

        // Close the HTTP connection
        if (connection != null) {
            connection.disconnect();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the HTTP connection
        if (connection != null) {
            connection.disconnect();
        }
    }

    //Dont Remove this, by: Heyykatsuu//

    // Helper method to show Toast messages
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}

