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
    private static final String MAP_STATUS_KEY = "status";
    private static final String MAP_TIMESTAMP_KEY = "timestamp";
    private static final String MAP_URL_KEY = "url";
    private static final String MAP_RESPONSE_TIME_KEY = "responsetime";
    private static final String MAP_ERROR_KEY = "error";

    private static final String HTML_TAG_END_TD = "</td>";

    private static final Logger LOG = LogManager.getLogger(MonitoringApplication.class);

    public static void main(String[] args) {
        LOG.debug("Start MonitoringApplication");
        String[] urlStrings = null;
        String urlString = System.getProperty("URL_STRINGS");
        String urlStringPath = System.getProperty("URL_STRINGS_PATH");

        if (urlString != null) {
            urlStrings = urlString.split(",");
        } else if (urlStringPath != null) {
            urlStrings = readURLStringFromFile(urlStringPath);
        }

        if (urlStrings != null) {
            ArrayList<LinkedHashMap<String, String>> results = startMonitor(urlStrings);
            if (!results.isEmpty()) {
                writeHtmlReport(results);
            } else {
                LOG.debug("Result is empty");
            }
        } else {
            LOG.debug("Empty URL Strings");
        }
    }

    /**
     * Read URL strings from the file
     * @param filename
     * @return Array String
     */
    private static String[] readURLStringFromFile(String filename) {
        LOG.debug("Start readURLStringFromFile.. filename={}", filename);
        String[] urlStrings =null;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            List<String> lines = new ArrayList();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            urlStrings = lines.toArray(new String[lines.size()]);
        } catch (IOException ie) {
            LOG.error(ie.getMessage());
        }
        return urlStrings;
    }

    /**
     * Start checking URL connection and parsing it's content
     * @param urlStrings URL
     * @return ArrayList
     */
    private static  ArrayList<LinkedHashMap<String, String>> startMonitor(String[] urlStrings)  {
        LOG.debug("Start startMonitor");
        ArrayList<LinkedHashMap<String, String>> results = new ArrayList();

        for (String urlString: urlStrings) {
            String error = null;
            boolean isGreenStatus = false;
            int statusCode = 0;
            String timestamp = "";
            Date date = new Date();
            for (int count = 1; count <=3; count++) {
                try {
                    LOG.debug("Checking {} connection", urlString);
                    URL url = new URL(urlString);
                    long start = System.currentTimeMillis();
                    InputStream inputStream = null;
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    http.setConnectTimeout(CONNECTION_TIMEOUT);
                    LOG.debug("Setting connection timeout to {} ms.", CONNECTION_TIMEOUT);
                    try {
                        statusCode = http.getResponseCode();
                        inputStream = http.getInputStream();

                        if (statusCode == HttpURLConnection.HTTP_OK) {
                            isGreenStatus = getUrlContents(inputStream).contains(COMPONENT_STATUS_GREEN);
                            timestamp = String.valueOf(date.getTime());
                            long end = System.currentTimeMillis();
                            if (isGreenStatus) {
                                LinkedHashMap<String, String> output = new LinkedHashMap();
                                long responeTime = (end - start);
                                output.put(MAP_TIMESTAMP_KEY, timestamp);
                                output.put(MAP_STATUS_KEY, "GREEN");
                                output.put(MAP_URL_KEY, urlString);
                                output.put(MAP_RESPONSE_TIME_KEY, responeTime + " ms " + ((responeTime > RESPONSE_TIME_THRESHOLD) ? "(above threshold)" : "(below threshold)"));
                                LOG.debug("timestamp={}, status={}, url={}, responseTime={} ms", timestamp, "GREEN", urlString, (end - start) );
                                results.add(output);
                            } else {
                                error = "No \"" + COMPONENT_STATUS_GREEN + "\" strings from URL content";
                                LOG.debug(error);
                            }
                        }
                    } finally {
                        http.disconnect();
                    }
                } catch (IOException ioException) {
                    error = ioException.toString();
                    timestamp = String.valueOf(date.getTime());
                    LOG.error(ioException);
                    try {
                        Thread.sleep(count * 1000);
                        LOG.error("{} Try connecting... Delaying for {} ms", count, count * 1000);
                    } catch (final InterruptedException ie) {
                        LOG.error("Interrupted! {}", ie.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    break;
                }
            }
            if (!isGreenStatus) {
                LinkedHashMap<String, String> output = new LinkedHashMap();
                output.put(MAP_TIMESTAMP_KEY, timestamp);
                output.put(MAP_STATUS_KEY, "RED");
                output.put(MAP_URL_KEY, urlString);
                output.put(MAP_ERROR_KEY, error);
                results.add(output);
            }
        }

        return results;
    }

    /**
     * Read URL content
     * @param inputStream
     * @return String
     */
    private static String getUrlContents(InputStream inputStream) {
        StringBuilder content = new StringBuilder();
        try {
            LOG.debug("Start getUrlContents");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e) {
            LOG.debug(e.getMessage());
        }
        return content.toString();
    }

    /**
     * Write output in html format
     * @param results
     */
    private static void writeHtmlReport(ArrayList<LinkedHashMap<String, String>> results)  {
        PrintWriter pw = null;
        try {
            LOG.debug("Start writeHtmlReport");
            pw = new PrintWriter(new FileWriter("output.html"));
            pw.println("<h1> Success Results:</h1>");
            pw.println("<table border=1px; style=\"width:100%\">\n" + "  <tr> \n" + "    <th>Current timestamp</th>\n"
                    + "    <th>Status check</th> \n" + "    <th>URL</th>\n" + "    <th>Response time(threshold="
                    + RESPONSE_TIME_THRESHOLD + " ms)</th>\n" + "  </tr>");
            for(int i=0; i< results.size();i++)  {
                pw.println("<tr>");
                if (!results.get(i).keySet().contains(MAP_ERROR_KEY)) {
                    for (String key : results.get(i).keySet()) {
                        if (key == MAP_STATUS_KEY) {
                            pw.println("<td style=\"background-color: green\">" + results.get(i).get(key) + HTML_TAG_END_TD);
                        } else {
                            pw.println("<td>" + results.get(i).get(key) + HTML_TAG_END_TD);
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
                if (results.get(i).keySet().contains(MAP_ERROR_KEY)) {
                    for (String key : results.get(i).keySet()) {
                        if (key == MAP_STATUS_KEY) {
                            pw.println("<td style=\"background-color: red\">" + results.get(i).get(key) + HTML_TAG_END_TD);
                        } else {
                            pw.println("<td>" + results.get(i).get(key) + HTML_TAG_END_TD);
                        }
                    }
                }
                pw.println("</tr>");
            }
            pw.println("</table>");

        } catch (Exception ie) {
            LOG.debug(ie.getMessage());
        } finally {
            pw.close();
        }
    }
}