package android.wangridev.ble.osezbeacon;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Administrator on 7/25/2015.
 */
public class MyHTTPRequest {
    DefaultHttpClient httpClient;
    public MyHTTPRequest(){
        httpClient = new DefaultHttpClient();
    }

    public HttpResponse responseHTTP(String sUrl){
        HttpGet httpRequest = new HttpGet(sUrl);
        httpRequest.setHeader("Accept", "application/text");

        HttpResponse response = null;
        try {
            response = (HttpResponse) httpClient.execute(httpRequest);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return response;
    }

    public String toString(HttpResponse response){
        if (response == null)return "No Network!";
        HttpEntity entity = response.getEntity();
        if (entity == null)return null;

        InputStream instream = null;
        try {
            instream = entity.getContent();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (instream == null)return "No Such Server!";

        String sResult= convertStreamToString(instream);

        try {
            if (sResult.contains("File Not Found"))return "No Such Server!";
            sResult = URLDecoder.decode(sResult, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            instream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (sResult == null)return "Unknown Error!";
        return sResult;
    }

    public String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 *
		 * (c) public domain: http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/11/a-simple-restful-client-at-android/
		 */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
