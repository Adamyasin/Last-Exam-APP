package com.example.monitoringbanjir;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> {

    private List<CardItem> cardItemList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CardViewAdapter(Context context, List<CardItem> cardItemList) {
        this.context = context;
        this.cardItemList = cardItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardItem item = cardItemList.get(position);
        holder.textDateTime.setText("Hari/Tanggal: " + item.getDateTime());
        holder.textNilaiSensor.setText("Nilai Sensor: " + item.getNilaiSensor());
        holder.textIndikatorAir.setText("Indikator Air: " + item.getIndikatorAir());
        holder.textStatus.setText("Status: " + item.getStatus());

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardItemList.size();
    }

    public void addCardItem(CardItem newItem) {
        cardItemList.add(newItem);
        notifyItemInserted(cardItemList.size() - 1);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textDateTime, textNilaiSensor, textIndikatorAir, textStatus;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDateTime = itemView.findViewById(R.id.textdatetime);
            textNilaiSensor = itemView.findViewById(R.id.textnilaisensor);
            textIndikatorAir = itemView.findViewById(R.id.textindikatorair);
            textStatus = itemView.findViewById(R.id.textstatus);
            btnDelete = itemView.findViewById(R.id.btn_hapus);
        }
    }
}
