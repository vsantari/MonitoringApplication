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

public class MonitoringApplication {

    private static final String COMPONENT_STATUS_GREEN = "Component Status: GREEN";
    private static final int CONNECTION_TIMEOUT = 3000;
    private static final Logger LOG = LogManager.getLogger(MonitoringApplication.class);

    public static void main(String[] args) {
        LOG.debug("Start MonitoringApplication");
        String urlStrings[] = {"http://localhost:9788/mve/", "http://my-json-server.typicode.com/vsantari/cs-devops/posts/2","http://my-json-server.typicode.com/vsantari/cs-devops/posts/1", "http://my-json-server.typicode.com/vsantari/cs-devops/posts/100"};
        ArrayList<LinkedHashMap<String, String>> results = startMonitor(urlStrings);
        writeOutput(results);


    }

    private static  ArrayList<LinkedHashMap<String, String>> startMonitor(String[] urlStrings)  {


        ArrayList<LinkedHashMap<String, String>> results = new ArrayList<LinkedHashMap<String, String>>();

        for (String urlString: urlStrings) {
            for (int count = 1; count <=3; count++) {
                try {
                    LOG.debug("Checking {} connection", urlString);
                    URL url = new URL(urlString);
                    long start = System.currentTimeMillis();
                    int statusCode = 0;
                    boolean isGreenStatus = false;
                    InputStream inputStream = null;
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    http.setConnectTimeout(CONNECTION_TIMEOUT);
                    LOG.debug("Setting connection timeout to {}", CONNECTION_TIMEOUT);
                    try {
                        statusCode = http.getResponseCode();
                        inputStream = http.getInputStream();

                        if (statusCode == 200) {
                            isGreenStatus = getUrlContents(inputStream).contains(COMPONENT_STATUS_GREEN);
                            Date date = new Date();
                            String timestamp = String.valueOf(date.getTime());

                            long end = System.currentTimeMillis();
                            if (isGreenStatus) {
                                LinkedHashMap<String, String> output = new LinkedHashMap<String, String>();
                                output.put("timestamp", timestamp);
                                output.put("status", "GREEN");
                                output.put("url", urlString);
                                output.put("responseTime", (end - start) + " ms");
                                results.add(output);
                                LOG.debug("timestamp={}, status={}, url={}, responseTime={} ms", timestamp, "GREEN", urlString, (end - start) );
                            }

                        }
                    } finally {
                        http.disconnect();
                        if (isGreenStatus) {
                            break;
                        }

                    }
                } catch (IOException ioe) {
                    LOG.error(ioe);
                    try {
                        Thread.sleep(count * 1000);
                        LOG.error("{} Try connecting... Delaying for {} ms", count, count * 1000);
                    } catch (final InterruptedException ie) {
                    }

                }
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
                    + "    <th>Status check</th> \n" + "    <th>URL</th>\n" + "    <th>Response time</th>\n" + "  </tr>");
            for(int i=0; i< results.size();i++)  {
                pw.println("<tr>");
                for(String key:results.get(i).keySet()){
                    if (key == "status") {
                        pw.println("<td style=\"background-color: green\">" +results.get(i).get(key)+  "</td>");
                    } else {
                        pw.println("<td>" +results.get(i).get(key)+  "</td>");
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