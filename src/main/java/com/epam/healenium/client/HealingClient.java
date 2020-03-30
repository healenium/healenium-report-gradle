package com.epam.healenium.client;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.net.URI;
import java.net.URISyntaxException;

public class HealingClient {

    private final String baseUrl;

    public HealingClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public HealingClient(String host, Integer port) throws URISyntaxException {
        this.baseUrl = new URI("http", null, host, port, null, null, null).normalize().toString();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String initReport(){
        return makePost(baseUrl + "/healenium/report/init");
    }

    public String buildReport(String key){
        return makePost(baseUrl + "/healenium/report/build", key);
    }

    public String makePost(String path){
        return makePost(path, null);
    }

    public String makePost(String path, String headerKey){
        try{
            HttpUrl.Builder urlBuilder = HttpUrl.parse(path).newBuilder();
            String url = urlBuilder.build().toString();

            RequestBody requestBody = RequestBody.create( new byte[0]);
            Request.Builder reqBuilder = new Request.Builder().url(url).post(requestBody);
            if(headerKey != null && !headerKey.isEmpty()){
                reqBuilder.addHeader("sessionKey", headerKey);
            }
            Request request = reqBuilder.build();
            Response response = new OkHttpClient().newCall(request).execute();
            return response.body().string();
        } catch (Exception ex){
            return null;
        }
    }

    public String makeGet(String path){
        try{
            HttpUrl.Builder urlBuilder = HttpUrl.parse(path).newBuilder();
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder().url(url).build();
            Response response = new OkHttpClient().newCall(request).execute();
            return response.body().string();
        } catch (Exception ex){
            return null;
        }
    }

}
