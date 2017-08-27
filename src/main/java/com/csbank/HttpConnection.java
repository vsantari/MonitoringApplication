package com.csbank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpConnection {
    private static final Logger LOG = LogManager.getLogger(HttpConnection.class);

    /**
     * Check HTTP URL Connection
     * @param urlString URL String
     * @param retry
     * @return LinkedHashMap
     */
    public  LinkedHashMap<String, String> checkHttpURLConnection(String urlString, int retry) {
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
            http.setConnectTimeout(ConstantValues.CONNECTION_TIMEOUT);
            LOG.debug("Setting connection timeout to {} ms.", ConstantValues.CONNECTION_TIMEOUT);
            statusCode = http.getResponseCode();
            inputStream = http.getInputStream();
            isGreenStatus = getUrlContents(inputStream).contains(ConstantValues.COMPONENT_STATUS_GREEN);
            timestamp = String.valueOf(date.getTime());
            long end = System.currentTimeMillis();
            if (isGreenStatus && statusCode == HttpURLConnection.HTTP_OK) {
                long responeTime = (end - start);
                output.put(ConstantValues.MAP_TIMESTAMP_KEY, timestamp);
                output.put(ConstantValues.MAP_STATUS_KEY, "GREEN");
                output.put(ConstantValues.MAP_URL_KEY, urlString);
                output.put(ConstantValues.MAP_RESPONSE_TIME_KEY, responeTime + " ms " + ((responeTime > ConstantValues.RESPONSE_TIME_THRESHOLD) ? "(above threshold)" : "(below threshold)"));
                LOG.debug("timestamp={}, status={}, url={}, responseTime={} ms", timestamp, "GREEN", urlString, (end - start) );
            } else {
                error = "No \"" + ConstantValues.COMPONENT_STATUS_GREEN + "\" strings from URL content";
                LOG.debug(error);
            }

        } catch (IOException ioException) {
            error = ioException.toString();
            timestamp = String.valueOf(date.getTime());
            LOG.error(ioException);
            long sleepTime = retry * ConstantValues.RETRY_DELAY_MS;
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
            output.put(ConstantValues.MAP_TIMESTAMP_KEY, timestamp);
            output.put(ConstantValues.MAP_STATUS_KEY, "RED");
            output.put(ConstantValues.MAP_URL_KEY, urlString);
            output.put(ConstantValues.MAP_ERROR_KEY, error);
            output.put(ConstantValues.MAP_RETRY_KEY, String.valueOf(retry));
        }

        return output;
    }

    /**
     * Read URL content
     * @param inputStream
     * @return String
     */
    private String getUrlContents(InputStream inputStream) {
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
}
