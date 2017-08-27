package com.csbank;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;

import org.junit.Test;

public class HttpConnectionTest {
    private final HttpConnection mockHttpConnection = mock(HttpConnection.class);

    @Test
    public void testcheckHttpURLConnection_urlString_retry_ReturnLinkedHashMap()  {
        LinkedHashMap<String, String> output = new LinkedHashMap();
        output.put("timestamp", "1233344");
        output.put("status", "GREEN");
        output.put("url", "https://my-json-server.typicode.com/vsantari/MonitoringApplication/posts/2");
        output.put("responsetime", "500 ms");
        when(mockHttpConnection.checkHttpURLConnection("https://my-json-server.typicode.com/vsantari/MonitoringApplication/posts/2",3 )).thenReturn(output);
        assertEquals(mockHttpConnection.checkHttpURLConnection("https://my-json-server.typicode.com/vsantari/MonitoringApplication/posts/2", 3), output);
    }

    @Test
    public void testcheckHttpURLConnection_urlString_retry_ReturnLinkedHashMap2()  {
        LinkedHashMap<String, String> output = new LinkedHashMap();
        output.put("timestamp", "1233344");
        output.put("status", "RED");
        output.put("url", "##https://my-json-server.typicode.com/vsantari/MonitoringApplication/posts/1");
        output.put("error", "java.net.MalformedURLException: no protocol: ##https://my-json-server.typicode.com/vsantari/MonitoringApplication/posts/1");
        when(mockHttpConnection.checkHttpURLConnection("##https://my-json-server.typicode.com/vsantari/MonitoringApplication/posts/1",3 )).thenReturn(output);
        assertEquals(mockHttpConnection.checkHttpURLConnection("##https://my-json-server.typicode.com/vsantari/MonitoringApplication/posts/1", 3), output);
    }

    @Test
    public void testcheckHttpURLConnection_urlString_retry_ReturnNull()  {
        when(mockHttpConnection.checkHttpURLConnection(null,3 )).thenReturn(null);
        assertEquals(mockHttpConnection.checkHttpURLConnection(null, 3), null);
    }
}
