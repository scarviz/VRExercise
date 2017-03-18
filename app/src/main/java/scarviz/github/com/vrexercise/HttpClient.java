package scarviz.github.com.vrexercise;

import android.text.TextUtils;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Request.Builder;

public class HttpClient {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public HttpClient() {
    }

    public HttpClient.Resp get(String url) {
        HttpClient.Resp resp = new HttpClient.Resp();
        resp.code = 400;
        if(TextUtils.isEmpty(url)) {
            return resp;
        } else {
            Request request = (new Builder()).url(url).addHeader("Content-Type", "text/html; charset=utf-8").get().build();
            OkHttpClient client = new OkHttpClient();

            try {
                Response e = client.newCall(request).execute();
                resp.code = e.code();
                ResponseBody body = e.body();
                if(e.isSuccessful()) {
                    resp.body = body.string();
                } else {
                    body.close();
                }
            } catch (IOException var7) {
                var7.printStackTrace();
            }

            return resp;
        }
    }

    public HttpClient.Resp postJson(String url, String json) {
        HttpClient.Resp resp = new HttpClient.Resp();
        resp.code = 400;
        if(TextUtils.isEmpty(url)) {
            return resp;
        } else {
            RequestBody body = RequestBody.create(JSON, json);
            Request request = (new Builder()).url(url).post(body).build();
            OkHttpClient client = new OkHttpClient();

            try {
                Response e = client.newCall(request).execute();
                resp.code = e.code();
                ResponseBody respBody = e.body();
                if(e.isSuccessful()) {
                    resp.body = respBody.string();
                } else {
                    respBody.close();
                }
            } catch (IOException var9) {
                var9.printStackTrace();
            }

            return resp;
        }
    }

    public class Resp {
        public String body;
        public int code;

        public Resp() {
        }
    }
}
