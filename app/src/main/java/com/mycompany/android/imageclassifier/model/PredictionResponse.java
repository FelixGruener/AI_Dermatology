package com.mycompany.android.imageclassifier.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PredictionResponse {

    @SerializedName("predicted class")
    @Expose
    private String predictedClass;
    @SerializedName("prediction stats")
    @Expose
    private PredictionStats predictionStats;

    /**
     * No args constructor for use in serialization
     *
     */
    public PredictionResponse() {
    }

    /**
     *
     * @param predictedClass
     * @param predictionStats
     */
    public PredictionResponse(String predictedClass, PredictionStats predictionStats) {
        super();
        this.predictedClass = predictedClass;
        this.predictionStats = predictionStats;
    }

    public String getPredictedClass() {
        return predictedClass;
    }

    public void setPredictedClass(String predictedClass) {
        this.predictedClass = predictedClass;
    }

    public PredictionStats getPredictionStats() {
        return predictionStats;
    }

    public void setPredictionStats(PredictionStats predictionStats) {
        this.predictionStats = predictionStats;
    }

}
