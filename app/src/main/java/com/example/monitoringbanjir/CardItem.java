package com.example.monitoringbanjir;

public class CardItem {
    private String documentId; // tambahkan variabel documentId
    private String dateTime;
    private String nilaiSensor;
    private String indikatorAir;
    private String status;

    // Perbarui konstruktor
    public CardItem(String documentId, String dateTime, String nilaiSensor, String indikatorAir, String status) {
        this.documentId = documentId;
        this.dateTime = dateTime; 
        this.nilaiSensor = nilaiSensor;
        this.indikatorAir = indikatorAir;
        this.status = status;
    }

    // Tambahkan getter untuk documentId
    public String getDocumentId() {
        return documentId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getNilaiSensor() {
        return nilaiSensor;
    }

    public String getIndikatorAir() {
        return indikatorAir;
    }

    public String getStatus() {
        return status;
    }
}
