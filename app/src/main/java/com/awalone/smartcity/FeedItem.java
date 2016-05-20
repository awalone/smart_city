package com.awalone.smartcity;


public class FeedItem {
    private int id;
    private String nama, komentar, gambar, waktu;

    public FeedItem() {
    }

    public FeedItem(int id, String nama, String gambar, String komentar,
                    String waktu) {
        super();
        this.id = id;
        this.nama = nama;
        this.gambar = gambar;
        this.komentar = komentar;
        this.waktu = waktu;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
    }

    public String getKomentar() {
        return komentar;
    }

    public void setKomentar(String komentar) {
        this.komentar = komentar;
    }


    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

}