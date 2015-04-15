package org.opendaylight.alto.manager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

@Command(scope = "alto", name = "load", description = "Alto Manager")
public class AltoManager extends OsgiCommandSupport {

  private static final Logger log = LoggerFactory.getLogger(AltoManager.class);
  private JsonFactory jsonF = new JsonFactory();
  private HttpClient httpClient;

  public AltoManager () {
    httpClient = initiateHttpClient();
    log.info(this.getClass().getName() + " Initiated");
  }

  private HttpClient initiateHttpClient() {
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials("admin:admin"));
    return HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
  }

  @Argument(index = 0, name = "map type", description = "Map Type", required = true, multiValued = false)
  String type = null;

  @Argument(index = 1, name = "map path", description = "Map Path", required = true, multiValued = false)
  String path = null;

  @Override
  protected Object doExecute() throws Exception {
    if (AltoManagerConstants.SERVICE_TYPE.NETWORK.toString().toLowerCase().equals(type)) {
      putNetworkMap();
    } else if (AltoManagerConstants.SERVICE_TYPE.RESOURCE.toString().toLowerCase().equals(type)) {
      putResources();
    }
    return null;
  }

  private void putResources() throws IOException {
    log.info("Loading Resources From " + path);
    String data = readFromFile(path);
    httpPut(AltoManagerConstants.HOST, data);
  }

  private void putNetworkMap() throws IOException {
    log.info("Loading Network Map From " + this.path);
    String data = readFromFile(path);
    String resourceId = resourceIdFromNetworkMap(data);
    if (resourceId == null) {
      log.info("Cannot parse resourceId, abort loading");
      return;
    }
    String url = AltoManagerConstants.NETWORK_MAP_HOST + resourceId;
    log.info("Url: " + url);
    httpPut(url, data);
  }

  private void httpPut(String url, String data) throws IOException {
    HttpPut httpput = new HttpPut(url);
    httpput.setHeader(HTTP.CONTENT_TYPE, AltoManagerConstants.JSON_CONTENT_TYPE);
    httpput.setEntity(new StringEntity(data));
    HttpResponse response = httpClient.execute(httpput);
    handleResponse(response);
  }

  private String readFromFile(String path) throws IOException {
    return new String(Files.readAllBytes(Paths.get(path)),
        StandardCharsets.UTF_8);
  }

  private void handleResponse(HttpResponse response) throws ParseException, IOException {
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      String body = EntityUtils.toString(entity);
      if (response.getStatusLine().getStatusCode()== 200) {
        log.info("Loading Succesfully.");
      } else {
        log.error("Failed to load file \n" + body);
      }
    }
  }

  private String resourceIdFromNetworkMap(String jsonString) throws JsonParseException, IOException {
    JsonParser jParser = jsonF.createParser(jsonString);
    while (jParser.nextToken() != JsonToken.END_OBJECT) {
      String fieldname = jParser.getCurrentName();
      if (AltoManagerConstants.RESOURCE_ID_LABEL.equals(fieldname)) {
        jParser.nextToken();
        String resourceId = jParser.getText();
        return resourceId;
      }
    }
    return null;
  }

  private void postResources(String path) throws IOException {
    log.info("Loading Resources From " + path);
    String content = readFromFile(path);

    HttpPost httppost = new HttpPost(AltoManagerConstants.HOST);
    httppost.setHeader(HTTP.CONTENT_TYPE, AltoManagerConstants.JSON_CONTENT_TYPE);
    httppost.setEntity(new StringEntity(content));
    HttpResponse response = httpClient.execute(httppost);
    handleResponse(response);
  }
}
