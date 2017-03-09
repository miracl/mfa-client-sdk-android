package com.miracl.mpinsdk.inappsample;

import com.google.gson.Gson;

import android.os.AsyncTask;

import com.miracl.mpinsdk.inappsample.rest.AccessCodeServiceApi;
import com.miracl.mpinsdk.inappsample.rest.model.AccessCodeInfo;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Just an example of how a validation with auth code can be done with a demo service. Actual implementations can be different and
 * more robust.
 */
public class ValidateLoginTask extends AsyncTask<Void, Void, Boolean> {

    private static final int HTTP_CODE_OK = 200;

    private String mAuthServiceUrl;
    private String mAuthCode;
    private String mUserId;

    public ValidateLoginTask(String authServiceUrl, String authCode, String userId) {
        mAuthServiceUrl = authServiceUrl;
        mAuthCode = authCode;
        mUserId = userId;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Retrofit retrofit;
        try {
            retrofit = new Retrofit.Builder().client(new OkHttpClient())
              .addConverterFactory(GsonConverterFactory.create(new Gson())).baseUrl(mAuthServiceUrl).build();
        } catch (IllegalArgumentException exception) {
            return false;
        }

        AccessCodeServiceApi accessCodeServiceApi = retrofit.create(AccessCodeServiceApi.class);

        try {
            Response<ResponseBody> responseSetAuthToken = accessCodeServiceApi
              .setAuthToken(new AccessCodeInfo(mAuthCode, mUserId)).execute();

            return responseSetAuthToken.code() == HTTP_CODE_OK;
        } catch (IOException e) {
            return false;
        }
    }
}
