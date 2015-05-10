package org.opendaylight.alto.manager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AltoManager extends OsgiCommandSupport {
  private static final Logger log = LoggerFactory.getLogger(AltoManager.class);
  
  protected HttpClient httpClient;

  public AltoManager () {
    httpClient = initiateHttpClient();
    log.info(this.getClass().getName() + " Initiated");
  }

  protected HttpClient initiateHttpClient() {
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials("admin:admin"));
    return HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
  }
  
  @Override
  protected Object doExecute() throws Exception {
    return null;
  }

  protected String readFromFile(String path) throws IOException {
    return new String(Files.readAllBytes(Paths.get(path)),
        StandardCharsets.UTF_8);
  }
  
  protected boolean isDefaultNetworkMap(String resourceId) throws IOException {
    String defaultNetworkResourceId = getDefaultNetworMapResourceId();
    return (defaultNetworkResourceId != null) && defaultNetworkResourceId.equals(resourceId);
  }
  
  protected String getDefaultNetworMapResourceId() throws IOException {
    HttpResponse response = httpGet(AltoManagerConstants.IRD_DEFAULT_NETWORK_MAP_URL);
    Pattern pattern = Pattern.compile(AltoManagerConstants.DEFAULT_NETWORK_MAP_REGEX);
    Matcher matcher = pattern.matcher(EntityUtils.toString(response.getEntity()));
    return matcher.find() ? matcher.group(1) : null;
  }
  
  protected HttpResponse httpGet(String url) throws IOException {
    HttpGet httpGet = new HttpGet(url);
    logHttpRequest("HTTP GET:", url, "");
    httpGet.setHeader(HTTP.CONTENT_TYPE, AltoManagerConstants.JSON_CONTENT_TYPE);
    return httpClient.execute(httpGet);
  }
  
  protected boolean httpPut(String url, String data) throws IOException {
    HttpPut httpPut = new HttpPut(url);
    httpPut.setHeader(HTTP.CONTENT_TYPE, AltoManagerConstants.JSON_CONTENT_TYPE);
    httpPut.setEntity(new StringEntity(data));
    logHttpRequest("HTTP PUT:", url, data);
    HttpResponse response = httpClient.execute(httpPut);
    return handleResponse(response);
  }
  
  protected boolean httpDelete(String url) throws IOException {
    HttpDelete httpDelete = new HttpDelete(url);
    httpDelete.setHeader(HTTP.CONTENT_TYPE, AltoManagerConstants.JSON_CONTENT_TYPE);
    logHttpRequest("HTTP DELETE:", url, "");
    HttpResponse response = httpClient.execute(httpDelete);
    return handleResponse(response);
  }
  
  private void logHttpRequest(String prefix, String url, String data) {
    log.debug(prefix + 
        "\nUrl: " + url + 
        "\nHeader: " + HTTP.CONTENT_TYPE + ": " + AltoManagerConstants.JSON_CONTENT_TYPE +
        "\nData: " + data);
  }
  
  protected boolean handleResponse(HttpResponse response) throws ParseException, IOException {
    int statusCode = response.getStatusLine().getStatusCode();
    logResponse(response);
    if (statusCode == 200) {
      log.info("Operation Succesfully");
      return true;
    } else {
      log.error("Operation Failed");
      return false;
    }
  }
  
  protected void logResponse(HttpResponse response) throws IOException {
    HttpEntity entity = response.getEntity();
    int statusCode = response.getStatusLine().getStatusCode();
    String body = entity != null ? EntityUtils.toString(entity) : "";
    log.info("Response: "
      + "\nStatus Code: " + statusCode
      + "\nBody: " + body);
  }
  
  protected String networkMapType() {
    return AltoManagerConstants.SERVICE_TYPE.NETWORK_MAP.toString()
        .toLowerCase().replace("_", "-");
  }
  
  protected String costMapType() {
    return AltoManagerConstants.SERVICE_TYPE.COST_MAP.toString()
        .toLowerCase().replace("_", "-");
  }
  
  protected String endpointPropertyMapType() {
    return AltoManagerConstants.SERVICE_TYPE.ENDPOINT_PROPERTY_MAP.toString()
        .toLowerCase().replace("_", "-");
  }
}
