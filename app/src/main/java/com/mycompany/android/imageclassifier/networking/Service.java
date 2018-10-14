package com.mycompany.android.imageclassifier.networking;


import com.mycompany.android.imageclassifier.model.PredictionResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by delaroy on 5/18/17.
 */
public interface Service {

    @Multipart
    @POST("classify")
    Call<PredictionResponse> uploadImage(@Part MultipartBody.Part image);

}
