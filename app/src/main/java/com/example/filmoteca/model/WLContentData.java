package com.example.filmoteca.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WLContentData {
    @SerializedName("contents")
    @Expose
    private List<WlContentDataDetail> contents;

    public class WlContentDataDetail {

        @SerializedName("id_user")
        @Expose
        private int idUser;

        @SerializedName("id_wl")
        @Expose
        private int idWl;

        @SerializedName("id_mv")
        @Expose
        private int idMv;

        public int getIdUser() {
            return idUser;
        }

        public void setIdUser(int idUser) {
            this.idUser = idUser;
        }

        public int getIdWl() {
            return idWl;
        }

        public void setIdWl(int idWl) {
            this.idWl = idWl;
        }

        public int getIdMv() {
            return idMv;
        }

        public void setIdMv(int idMv) {
            this.idMv = idMv;
        }
    }

    public List<WlContentDataDetail> getContents() {
        return contents;
    }

    public void setContents(List<WlContentDataDetail> contents) {
        this.contents = contents;
    }
}
