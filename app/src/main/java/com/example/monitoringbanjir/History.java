package com.example.monitoringbanjir;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CardViewAdapter adapter;
    private List<CardItem> cardItemList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cardItemList = new ArrayList<>();
        adapter = new CardViewAdapter(this, cardItemList);
        recyclerView.setAdapter(adapter);

        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance();

        // Ambil data dari Firestore
        CollectionReference historyRef = db.collection("history");
        historyRef.orderBy("waktu", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("Firestore", "Error getting documents: " + e.getMessage());
                            Toast.makeText(History.this, "Gagal mengambil data dari Firestore", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        cardItemList.clear(); // Hapus data yang lama sebelum menambahkan yang baru

                        // Iterasi melalui setiap dokumen yang diperbarui atau ditambahkan baru
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Mendapatkan ID dokumen
                            String documentId = documentSnapshot.getId();

                            // Mendapatkan nilai dari dokumen
                            String dateTime = documentSnapshot.getString("waktu");
                            String nilaiSensor = documentSnapshot.getString("nilaisensor");
                            String indikatorAir = documentSnapshot.getString("indikatorair");
                            String status = documentSnapshot.getString("status");

                            // Tambahkan data ke dalam cardItemList
                            cardItemList.add(new CardItem(documentId, dateTime, nilaiSensor, indikatorAir, status));
                        }

                        // Perbarui RecyclerView setelah mendapatkan data
                        adapter.notifyDataSetChanged();
                    }
                });

        adapter.setOnItemClickListener(new CardViewAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                KonfirmasiHapusData(position);
            }
        });
    }

    private void KonfirmasiHapusData(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus data ini?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HapusData(position);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.imgalert)
                .show();
    }

    private void HapusData(int position) {
        if (position != RecyclerView.NO_POSITION) {
            String documentId = cardItemList.get(position).getDocumentId();
            // Hapus dokumen dari Firestore
            db.collection("history").document(documentId).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Firestore", "Dokumen berhasil dihapus dari Firestore");
                            // Hapus CardView dari RecyclerView
                            cardItemList.remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Firestore", "Gagal menghapus dokumen dari Firestore: " + e.getMessage());
                        }
                    });
        }
    }
}
