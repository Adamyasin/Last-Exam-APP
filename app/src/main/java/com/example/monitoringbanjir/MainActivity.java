package com.example.monitoringbanjir;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView nilaiIndikator, nilaiSirine, nilaiStatus, nilaiSensor, textstatus, datetime;
    private Button kameraButton;
    private Switch switchrelay;
    private ImageButton onSirine, offSirine, warningSirine, disable, history;
    private NotificationManager notificationManager;
    private FirebaseFirestore firestore;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //komponen
        textstatus = findViewById(R.id.textstatus);
        nilaiSensor = findViewById(R.id.nilaiSensor);
        nilaiStatus = findViewById(R.id.nilaiStatus);
        nilaiIndikator = findViewById(R.id.nilaiIndikator);
        nilaiSirine = findViewById(R.id.nilaiSirine);
        kameraButton = findViewById(R.id.kamerabutton);
        onSirine = findViewById(R.id.onsirine);
        disable = findViewById(R.id.disable);
        warningSirine = findViewById(R.id.warningsirine);
        offSirine = findViewById(R.id.offsirine);
        history = findViewById(R.id.history);
        switchrelay = findViewById(R.id.switchrelay);
        datetime = findViewById(R.id.txtwaktu);
        datetime.setVisibility(View.GONE);

        //notif
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        //koneksi database
        firestore = FirebaseFirestore.getInstance();
        handler = new Handler();
        DatabaseReference koneksi = FirebaseDatabase.getInstance().getReference();
        DatabaseReference refRelay = FirebaseDatabase.getInstance().getReference("Relay");
        DatabaseReference refStatus = FirebaseDatabase.getInstance().getReference("Status");

        kameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, KameraActivity.class);
                startActivity(intent);
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, History.class);
                startActivity(intent);
            }
        });

        //baca nilai sensor
        koneksi.child("Sensor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float sensor = snapshot.getValue(Float.class);
                if (sensor != null) {
                    nilaiSensor.setText(sensor + " cm");

                    //nilai untuk status
                    if (sensor.equals("Reading Sensor")) {
                        nilaiStatus.setText("-");
                        nilaiIndikator.setText("-");
                    } else if (sensor >= 0 && sensor <= 13) {
                        nilaiStatus.setText("Bahaya");
                        nilaiIndikator.setText("Tinggi");
                        showNotification("STATUS BAHAYA!", "Ketinggian air dalam status Bahaya!, segera lakukan penindakan!");
                    } else if (sensor >= 13 && sensor <= 16) {
                        nilaiStatus.setText("Siaga");
                        nilaiIndikator.setText("Sedang");
                        showNotification("STATUS SIAGA!", "Ketinggian air dalam status Siaga!");
                    } else {
                        nilaiStatus.setText("Aman");
                        nilaiIndikator.setText("Rendah");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Penanganan kesalahan saat membaca database
                Toast.makeText(MainActivity.this, "Gagal membaca data sensor: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //nilai relay
        refRelay.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long message = dataSnapshot.getValue(Long.class);
                    if (message != null) {
                        if (message == 0) {
                            offSirine.setVisibility(View.GONE);
                            onSirine.setVisibility(View.VISIBLE);
                            warningSirine.setVisibility(View.GONE);
                            disable.setVisibility(View.GONE);
                            nilaiSirine.setText("OFF");
                        } else if (message == 1) {
                            onSirine.setVisibility(View.GONE);
                            offSirine.setVisibility(View.VISIBLE);
                            warningSirine.setVisibility(View.GONE);
                            disable.setVisibility(View.GONE);
                            nilaiSirine.setText("ON");
                        } else if (message == 2) {
                            onSirine.setVisibility(View.GONE);
                            offSirine.setVisibility(View.GONE);
                            disable.setVisibility(View.GONE);
                            warningSirine.setVisibility(View.VISIBLE);
                            nilaiSirine.setText("ON");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Penanganan kesalahan saat membaca database
                Toast.makeText(MainActivity.this, "Gagal membaca data relay: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //pengubahan nilai relay
        onSirine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refRelay.setValue(1);
            }
        });

        offSirine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refRelay.setValue(0);
            }
        });

        warningSirine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refRelay.setValue(0);
            }
        });



//  listener untuk mendapatkan nilai dari Firebase dan memperbarui switch
        refStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Mendapatkan nilai dari Firebase
                int switchValue = dataSnapshot.getValue(Integer.class);

                // Memperbarui status switch berdasarkan nilai dari Firebase
                switchrelay.setChecked(switchValue == 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Penanganan kesalahan jika terjadi
            }
        });

        switchrelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refStatus.setValue(isChecked ? 1 : 0);

                // Mengatur teks status berdasarkan kondisi Switch
                textstatus.setText(isChecked ? "Otomatis" : "Manual");

                onSirine.setEnabled(!isChecked);
                offSirine.setEnabled(!isChecked);
                warningSirine.setEnabled(!isChecked);
            }
        });

   //    kirimNilaiPeriodik();
       tampilkanWaktuRealtime();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //method untuk menampilkan notifikasi
    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.iconstatus)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }

    private void kirimNilaiPeriodik() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Mendapatkan nilai dari TextView
                String nilai = nilaiSensor.getText().toString();
                String status = nilaiStatus.getText().toString();
                String indikator = nilaiIndikator.getText().toString();
                String time = datetime.getText().toString();

                // Membuat objek data yang akan ditambahkan
                Map<String, Object> data = new HashMap<>();
                data.put("nilaisensor", nilai);
                data.put("status", status);
                data.put("indikatorair", indikator);
                data.put("waktu", time);
                // Anda bisa menambahkan lebih banyak field sesuai kebutuhan

                // Menambahkan data ke Firestore
                firestore.collection("history").add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });

                handler.postDelayed(this, 10000);
            }
        }, 1);
    }


    // Pastikan untuk menghentikan handler ketika aktivitas dihancurkan
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // Menghapus semua pesan dan callback dari handler
    }

    private void tampilkanWaktuRealtime() {
        Runnable waktuRealtimeRunnable = new Runnable() {
            @Override
            public void run() {
                // Mendapatkan waktu saat ini
                Calendar calendar = Calendar.getInstance();
                int tahun = calendar.get(Calendar.YEAR);
                int bulan = calendar.get(Calendar.MONTH) + 1; // Penambahan 1 karena nilai bulan dimulai dari 0
                int tanggal = calendar.get(Calendar.DAY_OF_MONTH);
                int jam = calendar.get(Calendar.HOUR_OF_DAY);
                int menit = calendar.get(Calendar.MINUTE);
                int detik = calendar.get(Calendar.SECOND);

                // Format waktu
                String waktu = String.format("%02d-%02d-%d %02d:%02d:%02d", tanggal, bulan, tahun, jam, menit, detik);

                // Menampilkan waktu di TextView atau melakukan tindakan lain yang diinginkan
                datetime.setText(waktu);

                // Mengatur interval waktu untuk menampilkan waktu berikutnya (setiap detik)
                handler.postDelayed(this, 1000); // 1000 milliseconds = 1 detik
            }
        };

        // Memulai tampilan waktu realtime
        handler.post(waktuRealtimeRunnable);
    }
}
