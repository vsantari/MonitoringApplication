package com.csbank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MonitoringApplication {
    private static final String COMPONENT_STATUS_GREEN = "Component Status: GREEN";
    private static final int CONNECTION_TIMEOUT = 3000;
    private static final long RESPONSE_TIME_THRESHOLD = 500;
    private static final long RETRY_DELAY_MS = 1000;
    private static final String HTML_TAG_END_TD = "</td>";
    private static final String MAP_STATUS_KEY = "status";
    private static final String MAP_TIMESTAMP_KEY = "timestamp";
    private static final String MAP_URL_KEY = "url";
    private static final String MAP_RESPONSE_TIME_KEY = "responsetime";
    private static final String MAP_ERROR_KEY = "error";
    private static final String MAP_RETRY_KEY = "retry";
    private static final String OUTPUT_NAME = "./logs/output-";

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
                Date date = new Date() ;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss") ;
                writeSuccessHtmlReport(OUTPUT_NAME + dateFormat.format(date) + ".html", results);
                writeFailedHtmlReport(OUTPUT_NAME + dateFormat.format(date) + ".html", results);
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
     * Check HTTP URL Connection
     * @param urlString URL String
     * @param retry
     * @return LinkedHashMap
     */
    private static LinkedHashMap<String, String> checkHttpURLConnection(String urlString, int retry) {
        LOG.debug("Start checkHttpURLConnection");
        String error = null;
        boolean isGreenStatus = false;
        int statusCode = 0;
        String timestamp = "";
        Date date = new Date();
        LinkedHashMap<String, String> output = new LinkedHashMap();
        HttpURLConnection http = null;
        try {
            LOG.debug("Checking Http URL Connection for {}", urlString);
            URL url = new URL(urlString);
            long start = System.currentTimeMillis();
            InputStream inputStream = null;
            http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(CONNECTION_TIMEOUT);
            LOG.debug("Setting connection timeout to {} ms.", CONNECTION_TIMEOUT);
            statusCode = http.getResponseCode();
            inputStream = http.getInputStream();
            isGreenStatus = getUrlContents(inputStream).contains(COMPONENT_STATUS_GREEN);
            timestamp = String.valueOf(date.getTime());
            long end = System.currentTimeMillis();
            if (isGreenStatus && statusCode == HttpURLConnection.HTTP_OK) {
                long responeTime = (end - start);
                output.put(MAP_TIMESTAMP_KEY, timestamp);
                output.put(MAP_STATUS_KEY, "GREEN");
                output.put(MAP_URL_KEY, urlString);
                output.put(MAP_RESPONSE_TIME_KEY, responeTime + " ms " + ((responeTime > RESPONSE_TIME_THRESHOLD) ? "(above threshold)" : "(below threshold)"));
                LOG.debug("timestamp={}, status={}, url={}, responseTime={} ms", timestamp, "GREEN", urlString, (end - start) );
             } else {
                error = "No \"" + COMPONENT_STATUS_GREEN + "\" strings from URL content";
                LOG.debug(error);
            }

        } catch (IOException ioException) {
            error = ioException.toString();
            timestamp = String.valueOf(date.getTime());
            LOG.error(ioException);
            long sleepTime = retry * RETRY_DELAY_MS;
            LOG.error("{} Try connecting... Delaying for {} ms", retry, sleepTime);
            try {
                Thread.sleep(sleepTime);
            } catch (final InterruptedException ie) {
                LOG.error("Interrupted! {}", ie.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        finally {
            if (http != null) {
                http.disconnect();
            }
        }

        if (!isGreenStatus) {
            output.put(MAP_TIMESTAMP_KEY, timestamp);
            output.put(MAP_STATUS_KEY, "RED");
            output.put(MAP_URL_KEY, urlString);
            output.put(MAP_ERROR_KEY, error);
            output.put(MAP_RETRY_KEY, String.valueOf(retry));
        }

        return output;
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
            for (int retry = 1; retry <= 3; retry++) {
                LinkedHashMap<String, String> output = checkHttpURLConnection(urlString, retry);
                if (!output.containsKey(MAP_ERROR_KEY)) {
                    results.add(output);
                    break;
                } else {
                    if (retry == 3) {
                        results.add(output);
                    }
                }

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
     * Write success report in html format
     * @param results
     */
    private static void writeSuccessHtmlReport(String filename, ArrayList<LinkedHashMap<String, String>> results)  {
        LOG.debug("Start writeSuccessHtmlReport. Filename={}", filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("<h1> Success Results:</h1>");
            pw.println("<table border=1px; style=\"width:100%\">\n" + "  <tr> \n" + "    <th>Current timestamp</th>\n"
                    + "    <th>Status check</th> \n" + "    <th>URL</th>\n" + "    <th>Response time(threshold="
                    + RESPONSE_TIME_THRESHOLD + " ms)</th>\n" + "  </tr>");
            for(int i=0; i< results.size();i++)  {
                pw.println("<tr>");
                if (!results.get(i).keySet().contains(MAP_ERROR_KEY)) {
                    for (String key : results.get(i).keySet()) {
                        String value = results.get(i).get(key);
                        if (key == MAP_STATUS_KEY) {
                            pw.println("<td style=\"background-color: green\">" + value + HTML_TAG_END_TD);
                        } else if (key == MAP_URL_KEY && isURLValid(value)) {
                            pw.println("<td> <a href=" + value + " target=\"_blank\">" + value + "</a>" + HTML_TAG_END_TD);
                        } else {
                            pw.println("<td>" + value + HTML_TAG_END_TD);
                        }
                    }
                }
                pw.println("</tr>");
            }
            pw.println("</table>");
        }
        catch (IOException ie) {
            LOG.error(ie.getMessage());
        }
    }

    /**
     * Write failed resport in html format
     * @param results
     */
    private static void writeFailedHtmlReport(String filename, ArrayList<LinkedHashMap<String, String>> results)  {
        LOG.debug("Start writeFailedHtmlReport. Filename={}", filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, true))) {
            pw.println("<h1> Failed Results:</h1>");
            pw.println("<table border=1px; style=\"width:100%\">\n" + "  <tr> \n" + "    <th>Current timestamp</th>\n"
                    + "    <th>Status check</th> \n" + "    <th>URL</th>\n" + "    <th>Error</th>\n" +  "    <th>Retry</th>\n" +"  </tr>");
            for(int i=0; i< results.size(); i++)  {
                pw.println("<tr>");
                if (results.get(i).keySet().contains(MAP_ERROR_KEY)) {
                    for (String key : results.get(i).keySet()) {
                        String mapValue = results.get(i).get(key);
                        if (key == MAP_STATUS_KEY) {
                            pw.println("<td style=\"background-color: red\">" + mapValue + HTML_TAG_END_TD);
                        } else if (key == MAP_URL_KEY && isURLValid(mapValue)) {
                            pw.println("<td> <a href=" + mapValue + " target=\"_blank\">" + mapValue + "</a>" + HTML_TAG_END_TD);
                        } else {
                            pw.println("<td>" + mapValue + HTML_TAG_END_TD);
                        }
                    }
                }

                pw.println("</tr>");
            }
            pw.println("</table>");
        }
        catch (IOException ie) {
            LOG.error(ie.getMessage());
        }
    }

    /**
     * Check if the URL string is valid or not
     * @param urlString URL String
     * @return True if valid, otherwise, False
     */
    private static boolean isURLValid(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (java.net.MalformedURLException | java.net.URISyntaxException e) {
           return false;
        }
    }
}