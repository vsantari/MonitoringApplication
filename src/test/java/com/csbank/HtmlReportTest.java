package com.csbank;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.Test;

public class HtmlReportTest {
    private final HtmlReport mockHtmlReport= mock(HtmlReport.class);

    @Test
    public void testwriteHTMLReports()  {
        ArrayList<LinkedHashMap<String, String>> results = new ArrayList();
        LinkedHashMap<String, String> output = new LinkedHashMap();
        output.put("timestamp", "1233344");
        output.put("status", "GREEN");
        output.put("url", "https://my-json-server.typicode.com/vsantari/MonitoringApplication/posts/2");
        output.put("responsetime", "500 ms");
        results.add(output);
        verify(mockHtmlReport, never()).writeHTMLReports(results);
    }

    @Test
    public void testwriteSuccessHtmlReport()  {
        String filename = "output-2017-08-27_11-04-58.html";
        ArrayList<LinkedHashMap<String, String>> results = new ArrayList();
        LinkedHashMap<String, String> output = new LinkedHashMap();
        output.put("timestamp", "1233344");
        output.put("status", "GREEN");
        output.put("url", "https://my-json-server.typicode.com/vsantari/MonitoringApplication/posts/2");
        output.put("responsetime", "500 ms");
        results.add(output);
        verify(mockHtmlReport, never()).writeSuccessHtmlReport(filename, results);
    }

    @Test
    public void testwriteFailedHtmlReport()  {
        String filename = "output-2017-08-27_11-04-58.html";
        ArrayList<LinkedHashMap<String, String>> results = new ArrayList();
        LinkedHashMap<String, String> output = new LinkedHashMap();
        output.put("timestamp", "1233344");
        output.put("status", "GREEN");
        output.put("url", "https://my-json-server.typicode.com/vsantari/MonitoringApplication/posts/2");
        output.put("responsetime", "500 ms");
        results.add(output);
        verify(mockHtmlReport, never()).writeFailedHtmlReport(filename, results);

    }
}
