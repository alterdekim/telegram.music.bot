package com.alterdekim.telegram.jackett;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class HTTP {
    public static String get( String url ) throws IOException {
        HttpResponse response = HttpClientBuilder.create().build().execute(new HttpGet(url));
        return new BasicResponseHandler().handleResponse(response);
    }
}
