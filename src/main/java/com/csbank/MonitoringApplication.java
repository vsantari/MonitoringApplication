package com.csbank;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Date;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.io.FileReader;
import java.util.List;

public class MonitoringApplication {
    private static final String COMPONENT_STATUS_GREEN = "Component Status: GREEN";
    private static final int CONNECTION_TIMEOUT = 3000;
    private static final long RESPONSE_TIME_THRESHOLD = 500;
    private static final Logger LOG = LogManager.getLogger(MonitoringApplication.class);

    public static void main(String[] args) {
        LOG.debug("Start MonitoringApplication");
        String urlStrings[] = null;
        String urlString = System.getProperty("URL_STRINGS");
        String urlStringPath = System.getProperty("URL_STRINGS_PATH");

        if (urlString != null) {
            urlStrings = urlString.split(",");
        } else if (urlStringPath != null) {
            urlStrings = readURLStringFromFile(urlStringPath);

        }

        if (urlStrings != null) {
            ArrayList<LinkedHashMap<String, String>> results = startMonitor(urlStrings);
            writeOutput(results);
        } else {
            LOG.debug("Empty URL Strings");
        }
    }

    private static String[] readURLStringFromFile(String filename) {
        String urlStrings[] = null;
        LOG.debug("Reading {}", filename);
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new ArrayList<String>();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
               line = line.trim();
                if (!line.equals("")) {
                    System.out.println("====" + line + "===");
                    lines.add(line);
                }
            }

            bufferedReader.close();
            urlStrings = lines.toArray(new String[lines.size()]);
        } catch (IOException ie) {
            LOG.error("Failed to read {}. Found {}", filename, ie.toString());
        }

       return urlStrings;
    }

    private static  ArrayList<LinkedHashMap<String, String>> startMonitor(String[] urlStrings)  {
        ArrayList<LinkedHashMap<String, String>> results = new ArrayList<LinkedHashMap<String, String>>();
        for (String urlString: urlStrings) {
            String error = null;
            boolean isGreenStatus = false;
            String timestamp = "";
            Date date = new Date();
            for (int count = 1; count <=3; count++) {
                try {
                    LOG.debug("Checking {} connection", urlString);
                    URL url = new URL(urlString);
                    long start = System.currentTimeMillis();
                    int statusCode = 0;
                    InputStream inputStream = null;
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    http.setConnectTimeout(CONNECTION_TIMEOUT);
                    LOG.debug("Setting connection timeout to {}", CONNECTION_TIMEOUT);
                    try {
                        statusCode = http.getResponseCode();
                        inputStream = http.getInputStream();

                        if (statusCode == 200) {
                            isGreenStatus = getUrlContents(inputStream).contains(COMPONENT_STATUS_GREEN);
                            timestamp = String.valueOf(date.getTime());
                            long end = System.currentTimeMillis();
                            if (isGreenStatus) {
                                LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
                                long responeTime = (end - start);
                                output.put("timestamp", timestamp);
                                output.put("status", "GREEN");
                                output.put("url", urlString);
                                output.put("responseTime", responeTime + " ms " + ((responeTime > RESPONSE_TIME_THRESHOLD) ? "(above threshold)" : "(below threshold)"));
                                results.add(output);
                                LOG.debug("timestamp={}, status={}, url={}, responseTime={} ms", timestamp, "GREEN", urlString, (end - start) );
                            } else {
                                error = "No \"" + COMPONENT_STATUS_GREEN + "\" strings from URL content";
                                LOG.debug(error);
                            }
                        }
                    } finally {
                        http.disconnect();
                        if (isGreenStatus) {
                            break;
                        }

                    }
                } catch (IOException ioException) {
                    error = ioException.toString();
                    timestamp = String.valueOf(date.getTime());
                    LOG.error(ioException);
                    try {
                        Thread.sleep(count * 1000);
                        LOG.error("{} Try connecting... Delaying for {} ms", count, count * 1000);
                    } catch (final InterruptedException ie) {
                    }

                }
            }
            if (!isGreenStatus) {
                LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
                output.put("timestamp", timestamp);
                output.put("status", "RED");
                output.put("url", urlString);
                output.put("error", error);
                results.add(output);
            }
        }

        return results;
    }

    private static String getUrlContents(InputStream inputStream) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    private static void writeOutput(ArrayList<LinkedHashMap<String, String>> results)  {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("output.html"));
            pw.println("<h1> Success Results:</h1>");
            pw.println("<table border=1px; style=\"width:100%\">\n" + "  <tr> \n" + "    <th>Current timestamp</th>\n"
                    + "    <th>Status check</th> \n" + "    <th>URL</th>\n" + "    <th>Response time(threshold="
                    + RESPONSE_TIME_THRESHOLD + " ms)</th>\n" + "  </tr>");
            for(int i=0; i< results.size();i++)  {
                pw.println("<tr>");
                if (!results.get(i).keySet().contains("error")) {
                    for (String key : results.get(i).keySet()) {
                        if (key == "status") {
                            pw.println("<td style=\"background-color: green\">" + results.get(i).get(key) + "</td>");
                        } else {
                            pw.println("<td>" + results.get(i).get(key) + "</td>");
                        }
                    }
                }
                pw.println("</tr>");
            }
            pw.println("</table>");

            pw.println("<h1> Failed Results:</h1>");
            pw.println("<table border=1px; style=\"width:100%\">\n" + "  <tr> \n" + "    <th>Current timestamp</th>\n"
                    + "    <th>Status check</th> \n" + "    <th>URL</th>\n" + "    <th>Error</th>\n" + "  </tr>");
            for(int i=0; i< results.size();i++)  {
                pw.println("<tr>");
                if (results.get(i).keySet().contains("error")) {
                    for (String key : results.get(i).keySet()) {
                        if (key == "status") {
                            pw.println("<td style=\"background-color: red\">" + results.get(i).get(key) + "</td>");
                        } else {
                            pw.println("<td>" + results.get(i).get(key) + "</td>");
                        }
                    }
                }
                pw.println("</tr>");
            }
            pw.println("</table>");
            pw.close();
        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }
}