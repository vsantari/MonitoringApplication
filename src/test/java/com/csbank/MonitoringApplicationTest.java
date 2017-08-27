package com.csbank;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.junit.Test;



public class MonitoringApplicationTest  {
    private final MonitoringApplication mockMonitoringApplication = mock(MonitoringApplication.class);
    @Test
    public void testisstartMonitor_urlStringInputEnvVar_urlStringFileEnvVar()  {
        String urlStringFileEnvVar = "/logs/URL_Strings.txt";
        String urlStringInputEnvVar = "http://google.com,http://facebook.com";
        verify(mockMonitoringApplication, never()).startMonitor(urlStringFileEnvVar, urlStringInputEnvVar);

    }


}
