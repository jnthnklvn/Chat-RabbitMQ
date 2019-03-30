package br.ufs.dcomp.ChatRabbitMQ;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
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

/**
 * Classe para conexões e requisições de dados de grupos e usuários
 * através do protocolo HTTP e do Json para o tratamento do retorno.
 * @version 1.0
 * @since Finalização da Etapa 5
 */
public class HTTPrequestAPI{
    private String URL;
    private String keyJson;

    private final String USERNAME = "zkelvinfps";
    private final String PASSWORD = "0";

    /**
     * Método construtor responsável por inicializar as váriaveis de
     * URL e chave de busca para objetos Json.
     * @param URL - String com endereço pra requisição.
     * @param keyJson - String com campo que deve ser retornaod do Json.
     */
    public HTTPrequestAPI(String URL, String keyJson){
        this.URL = URL;
        this.keyJson = keyJson;
    }

    /**
     * Faz uma requisição HTTP, atribui a um Stream, escreve conteúdo do Stream numa String.
     * @return inputLine - String com conteúdo da requisição.
     */
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

            credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(this.USERNAME, this.PASSWORD));
            authCache.put(targetHost, new BasicScheme());
            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);

            CloseableHttpResponse response = httpClient.execute(targetHost, httpget, context);

            HttpEntity entity = response.getEntity();
            BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
            
            inputLine = in.readLine();

            EntityUtils.consume(entity);
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }return inputLine;
    }

    /**
     * Chama um método para fazer a requisição HTTP, atribui o retorno a uma String,
     * cria um Array Json a partir da dessa String e, por fim, varre o Array adicionando
     * o campo keyJson de cada objeto para compor uma mensagem a ser enviada ao usuário.
     * @return msg - String com conteúdo da operação solicitada.
     */
    public String getJsonMsg() {
        String in = this.URLFetch();
        JSONArray jArr = new JSONArray(in);
        JSONObject jObj = jArr.getJSONObject(0);
        String emptyTest = jObj.getString(this.keyJson);
        String msg = emptyTest;

        for (int i = 1; i < jArr.length(); i++) {
            jObj = jArr.getJSONObject(i);
            msg += ", " + jObj.getString(this.keyJson);
        }
        
        if (emptyTest.isEmpty()){
            msg = msg.substring(2);
        }
        return msg;
    }
}