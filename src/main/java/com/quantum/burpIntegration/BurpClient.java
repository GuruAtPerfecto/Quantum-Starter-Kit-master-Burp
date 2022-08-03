/*
 * Copyright (c) 2016 VMware, Inc. All Rights Reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.quantum.burpIntegration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.quantum.burpIntegration.domain.Config;
import com.quantum.burpIntegration.domain.HttpMessageList;
import com.quantum.burpIntegration.domain.ReportType;
import com.quantum.burpIntegration.domain.ScanIssueList;
import com.quantum.burpIntegration.domain.ScanProgress;
import com.google.common.io.ByteSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BurpClient {
   private static final Logger log = LoggerFactory.getLogger(BurpClient.class);
   private RestTemplate restTemplate = new RestTemplate();
   private String baseUrl;
   public long burpPID;

   public BurpClient(String baseUrl) throws IOException {
      this.baseUrl = baseUrl;
      List<HttpMessageConverter<?>> converters = new ArrayList<>();
      converters.add(new MappingJackson2HttpMessageConverter());
      converters.add(new ByteArrayHttpMessageConverter());
      restTemplate.setMessageConverters(converters);
   }

   public void startBurp(String burpServerAddress, String burpServerPort, String burpConfigFile) throws IOException {

      burpServerAddress = burpServerAddress.replaceFirst("^(http[s]?://)","");
      String[] cmd = new String[0];

      Process process;
      if(System.getProperty("os.name").contains("Windows")) {
         System.out.println("cmd /c burp-rest-api.bat " +  "--server.address=" + burpServerAddress + " --server.port=" + burpServerPort + " --config-file=" + burpConfigFile + " --headless.mode=true" + " --unpause-spider-and-scanner");
         process = Runtime.getRuntime().exec(
                 "cmd /c burp-rest-api.bat " +  "--server.address=" + burpServerAddress + " --server.port=" + burpServerPort + " --config-file=" + burpConfigFile + " --headless.mode=true" + " --unpause-spider-and-scanner" , null, new File(System.getProperty("user.dir") + "\\" + "burp"));
      } else {
         cmd = new String[]{"sh", "burp/burp-rest-api.sh", "--server.address=" + burpServerAddress, "--server.port=" + burpServerPort, "--config-file=" + burpConfigFile, "--headless.mode=true", "--unpause-spider-and-scanner"};
         process = Runtime.getRuntime().exec(cmd);

      }

      printConsoleLog(process);
      this.burpPID = tryGetPid(process);
   }

   public void terminateBurp() throws IOException {
      String port = (String) ConfigurationManager.getBundle().getProperty("burpServerPort");
      Process process = null;
      if(System.getProperty("os.name").contains("Windows"))
         process = Runtime.getRuntime().exec("npx.cmd kill-port " + Integer.parseInt(port));
      else
         process = Runtime.getRuntime().exec("npx kill-port " + Integer.parseInt(port));
      printConsoleLog(process);

   }

   public JsonNode getConfiguration() {
      String uriString = buildUriFromPathSegments("burp", "configuration");
      return restTemplate.getForObject(uriString, JsonNode.class);
   }

   public void updateConfiguration(JsonNode configJson) {
      String uriString = buildUriFromPathSegments("burp", "configuration");
      restTemplate.put(uriString, configJson);
   }

   public Config getConfig() {
      String uriString = buildUriFromPathSegments("burp", "config");
      return restTemplate.getForObject(uriString, Config.class);
   }

   public void setConfig(Config configuration) {
      String uriString = buildUriFromPathSegments("burp", "config");
      restTemplate.postForLocation(uriString, configuration);
   }

   public void updateConfig(Config configuration) {
      String uriString = buildUriFromPathSegments("burp", "config");
      restTemplate.put(uriString, configuration);
   }

   public HttpMessageList getProxyHistory() {
      String uriString = buildUriFromPathSegments("burp", "proxy", "history");
      return restTemplate.getForObject(uriString, HttpMessageList.class);
   }

   public HttpMessageList getSiteMap(String urlPrefix) {
      String uriString = buildUriFromPathSegments("burp", "target", "sitemap");
      if (!StringUtils.isEmpty(urlPrefix)) {
         URI uri = UriComponentsBuilder.fromUriString(uriString).queryParam("urlPrefix", urlPrefix)
               .build().toUri();
         return restTemplate.getForObject(uri, HttpMessageList.class);
      }
      return restTemplate.getForObject(uriString, HttpMessageList.class);
   }

   public boolean isInScope(String url) {
      String uriString = buildUriFromPathSegments("burp", "target", "scope");
      URI uri = UriComponentsBuilder.fromUriString(uriString).queryParam("url", url).build()
            .toUri();
      ObjectNode response = restTemplate.getForObject(uri, ObjectNode.class);
      return response.get("inScope").asBoolean();
   }

   public void includeInScope(String url) {
      String uriString = buildUriFromPathSegments("burp", "target", "scope");
      URI uri = UriComponentsBuilder.fromUriString(uriString).queryParam("url", url).build()
            .toUri();
      restTemplate.put(uri, null);
   }

   public void excludeFromScope(String url) {
      String uriString = buildUriFromPathSegments("burp", "target", "scope");
      URI uri = UriComponentsBuilder.fromUriString(uriString).queryParam("url", url).build()
            .toUri();
      restTemplate.delete(uri);
   }

   public void scan(String baseUrl) {
      String uriString = buildUriFromPathSegments("burp", "scanner", "scans", "active");
      URI uri = UriComponentsBuilder.fromUriString(uriString).queryParam("baseUrl", baseUrl)
            .build().toUri();
      restTemplate.postForLocation(uri, null);
   }

   public void passiveScan(String baseUrl) {
      String uriString = buildUriFromPathSegments("burp", "scanner", "scans", "passive");
      URI uri = UriComponentsBuilder.fromUriString(uriString).queryParam("baseUrl", baseUrl)
              .build().toUri();
      restTemplate.postForLocation(uri, null);
   }

   //TODO: Client method for clearScans. Is this needed?

   public int getScannerStatus() {
      String uriString = buildUriFromPathSegments("burp", "scanner", "status");
      return restTemplate.getForObject(uriString, ScanProgress.class).getTotalScanPercentage();
   }

   public ScanIssueList getScanIssues(String urlPrefix) {
      String uriString = buildUriFromPathSegments("burp", "scanner", "issues");
      URI uri = UriComponentsBuilder.fromUriString(uriString).queryParam("urlPrefix", urlPrefix)
            .build().toUri();
      return restTemplate.getForObject(uri, ScanIssueList.class);
   }

   public ScanIssueList getScanIssues() {
      String uriString = buildUriFromPathSegments("burp", "scanner", "issues");
      return restTemplate.getForObject(uriString, ScanIssueList.class);
   }

   public byte[] getReportData(ReportType reportType, String urlPrefix, String filePath) throws IOException {
      String uriString = buildUriFromPathSegments("burp", "report");
      URI uri = UriComponentsBuilder.fromUriString(uriString).queryParam("reportType", reportType)
            .build().toUri();
      uri = UriComponentsBuilder.fromUriString(uriString).queryParam("urlPrefix", urlPrefix)
              .build().toUri();
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<byte[]> responseContent = restTemplate
            .exchange(uri, HttpMethod.GET, entity, byte[].class);

      if (responseContent.getStatusCode() == HttpStatus.OK) {

         InputStream inputStreamReader = ByteSource.wrap(responseContent.getBody()).openStream();

         String response = IOUtils.toString(inputStreamReader);

         FileWriter myWriter = new FileWriter(filePath);
         myWriter.write(response);
         myWriter.close();

         return responseContent.getBody();
      }
      return null;
   }

   public void spider(String baseUrl) {
      String uriString = buildUriFromPathSegments("burp", "spider");
      URI uri = UriComponentsBuilder.fromUriString(uriString).queryParam("baseUrl", baseUrl).build().toUri();
      restTemplate.postForLocation(uri, null);
   }

   private String buildUriFromPathSegments(String... pathSegments) {
      return UriComponentsBuilder.fromUriString(baseUrl).pathSegment(pathSegments).toUriString();
   }

   private static void printConsoleLog(Process process) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = "";
      while ((line = reader.readLine()) != null) {
         System.out.println(line);
         if ((line.contains("Started BurpApplication in")) || (line.contains("Web server failed to start.")))
            break;
      }
   }

   private static int tryGetPid(Process process) {
      if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
         try {
            Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            return f.getInt(process);
         } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
         }
      }

      return 0;
   }
}
