package br.ufs.dcomp.ChatRabbitMQ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class HTTPrequestAPI implements Runnable{
    private String URL;
    private String source;
    private String prompt;

    private final String USERNAME = "zkelvinfps";
    private final String PASSWORD = "0";

    public HTTPrequestAPI(String URL, String source){
        this.URL = URL;
        this.source = source;
    }

    public void getJSON(String in) {
        JSONArray jArr = new JSONArray(in);
        JSONObject jObj = jArr.getJSONObject(0);
        String emptyTest = jObj.getString(this.source);
        String msg = emptyTest;

        for (int i = 1; i < jArr.length(); i++) {
            jObj = jArr.getJSONObject(i);
            msg += ", " + jObj.getString(this.source);
        }
        if (emptyTest.isEmpty()){
            msg = msg.substring(2);
        }
        System.out.println("\n" + msg);
        System.out.print(this.prompt);
    }

    public String URLFetch() {
        String inputLine = "";
        try {
            URL url = new URL(this.URL);
            HttpGet httpget = new HttpGet(this.URL);
            HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            CloseableHttpClient httpClient = HttpClients.createDefault();
            final HttpClientContext context = HttpClientContext.create();
            AuthCache authCache = new BasicAuthCache();
            CredentialsProvider credsProvider = new BasicCredentialsProvider();

            credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(USERNAME, PASSWORD));
            authCache.put(targetHost, new BasicScheme());
            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);

            // Get the response
            CloseableHttpResponse response = httpClient.execute(targetHost, httpget, context);

            // Get the data
            HttpEntity entity = response.getEntity();
            BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
            
            inputLine = in.readLine();

            EntityUtils.consume(entity);
            httpClient.close();
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }return inputLine;
    }

    @Override
    public void run() {
        String msg = this.URLFetch();
        this.getJSON(msg);
    }

    /**
     * @param prompt the prompt to set
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}