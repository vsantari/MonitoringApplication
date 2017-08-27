package com.csbank;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HtmlReport {
    private static final Logger LOG = LogManager.getLogger(HtmlReport.class);
    private static Util util = new Util();
    private static ConstantValues constantValues = new ConstantValues();

    /**
     * Write success and failed results in HTML format
     * @param results ArrayList
     */
    public void writeHTMLReports(ArrayList<LinkedHashMap<String, String>> results) {
        if (!results.isEmpty()) {
            String filename = util.generateDateFilename();
            writeSuccessHtmlReport(constantValues.OUTPUT_NAME + filename+ ".html", results);
            writeFailedHtmlReport(constantValues.OUTPUT_NAME + filename + ".html", results);
        } else {
            LOG.debug("Result is empty");
        }
    }

    /**
     * Write success report in html format
     * @param filename Filename
     * @param results ArrayList
     */
    public void writeSuccessHtmlReport(String filename, ArrayList<LinkedHashMap<String, String>> results)  {
        LOG.debug("Start writeSuccessHtmlReport. Filename={}", filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("<h1> Success Results:</h1>");
            pw.println("<table border=1px; style=\"width:100%\">\n" + "  <tr> \n" + "    <th>Current timestamp</th>\n"
                    + "    <th>Status check</th> \n" + "    <th>URL</th>\n" + "    <th>Response time(threshold="
                    + constantValues.RESPONSE_TIME_THRESHOLD + " ms)</th>\n" + "  </tr>");
            for(int i=0; i< results.size();i++)  {
                pw.println("<tr>");
                if (!results.get(i).keySet().contains(constantValues.MAP_ERROR_KEY)) {
                    for (String key : results.get(i).keySet()) {
                        String value = results.get(i).get(key);
                        if (key == constantValues.MAP_STATUS_KEY) {
                            pw.println("<td style=\"background-color: green\">" + value + constantValues.HTML_TAG_END_TD);
                        } else if (key == constantValues.MAP_URL_KEY && util.isURLValid(value)) {
                            pw.println("<td> <a href=" + value + " target=\"_blank\">" + value + "</a>" + constantValues.HTML_TAG_END_TD);
                        } else {
                            pw.println("<td>" + value +constantValues. HTML_TAG_END_TD);
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
     * Write failed report in html format
     * @param filename Filename
     * @param results ArrayList
     */
    public void writeFailedHtmlReport(String filename, ArrayList<LinkedHashMap<String, String>> results)  {
        LOG.debug("Start writeFailedHtmlReport. Filename={}", filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, true))) {
            pw.println("<h1> Failed Results:</h1>");
            pw.println("<table border=1px; style=\"width:100%\">\n" + "  <tr> \n" + "    <th>Current timestamp</th>\n"
                    + "    <th>Status check</th> \n" + "    <th>URL</th>\n" + "    <th>Error</th>\n" +  "    <th>Retry</th>\n" +"  </tr>");
            for(int i=0; i< results.size(); i++)  {
                pw.println("<tr>");
                if (results.get(i).keySet().contains(constantValues.MAP_ERROR_KEY)) {
                    for (String key : results.get(i).keySet()) {
                        String mapValue = results.get(i).get(key);
                        if (key == constantValues.MAP_STATUS_KEY) {
                            pw.println("<td style=\"background-color: red\">" + mapValue + constantValues.HTML_TAG_END_TD);
                        } else if (key == constantValues.MAP_URL_KEY && util.isURLValid(mapValue)) {
                            pw.println("<td> <a href=" + mapValue + " target=\"_blank\">" + mapValue + "</a>" + constantValues.HTML_TAG_END_TD);
                        } else {
                            pw.println("<td>" + mapValue + constantValues.HTML_TAG_END_TD);
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
}
