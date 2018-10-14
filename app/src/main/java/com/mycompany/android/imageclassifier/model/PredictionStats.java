package com.mycompany.android.imageclassifier.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PredictionStats {

    @SerializedName("melanoma")
    @Expose
    private Double melanoma;
    @SerializedName("nevus")
    @Expose
    private Double nevus;
    @SerializedName("seborrheic_keratosis")
    @Expose
    private Double seborrheicKeratosis;

    /**
     * No args constructor for use in serialization
     *
     */
    public PredictionStats() {
    }

    /**
     *
     * @param nevus
     * @param seborrheicKeratosis
     * @param melanoma
     */
    public PredictionStats(Double melanoma, Double nevus, Double seborrheicKeratosis) {
        super();
        this.melanoma = melanoma;
        this.nevus = nevus;
        this.seborrheicKeratosis = seborrheicKeratosis;
    }

    public Double getMelanoma() {
        return melanoma;
    }

    public void setMelanoma(Double melanoma) {
        this.melanoma = melanoma;
    }

    public Double getNevus() {
        return nevus;
    }

    public void setNevus(Double nevus) {
        this.nevus = nevus;
    }

    public Double getSeborrheicKeratosis() {
        return seborrheicKeratosis;
    }

    public void setSeborrheicKeratosis(Double seborrheicKeratosis) {
        this.seborrheicKeratosis = seborrheicKeratosis;
    }

}
