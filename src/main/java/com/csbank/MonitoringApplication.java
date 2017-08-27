package com.csbank;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MonitoringApplication {
    private static HttpConnection httpConnection = new HttpConnection();
    private static Util util = new Util();
    private static HtmlReport htmlReport = new HtmlReport();
    private static final Logger LOG = LogManager.getLogger(MonitoringApplication.class);

    public static void main(String[] args) {
        MonitoringApplication monitoringApplication = new MonitoringApplication();
        monitoringApplication.startMonitor(System.getProperty(ConstantValues.URL_STRINGS), System.getProperty(ConstantValues.URL_STRINGS_PATH));
    }

    /**
     * Start checking URL connection and parsing it's content
     * @param urlStringInputEnvVar URL Strings Input
     * @param urlStringFileEnvVar URL Strings in file
     */
    public void startMonitor(String urlStringInputEnvVar, String urlStringFileEnvVar)  {
        LOG.debug("Start startMonitor");
        String[] urlStrings = util.getURLStrings(urlStringInputEnvVar, urlStringFileEnvVar);

        if (urlStrings == null) {
            LOG.debug("Empty URL Strings");
            return;
        }

        ArrayList<LinkedHashMap<String, String>> results = new ArrayList();
        for (String urlString: urlStrings) {
            for (int retry = 1; retry <= 3; retry++) {
                LinkedHashMap<String, String> output = httpConnection.checkHttpURLConnection(urlString, retry);
                if (!output.containsKey(ConstantValues.MAP_ERROR_KEY)) {
                    results.add(output);
                    break;
                }
                if (output.containsKey(ConstantValues.MAP_ERROR_KEY) && retry == 3) {
                    results.add(output);
                }
            }
        }

        htmlReport.writeHTMLReports(results);

    }
}