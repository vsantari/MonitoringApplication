package com.csbank;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import org.junit.Test;

public class UtilTest {
    private final Util mockUtil = mock(Util.class);

    @Test
    public void testisURLValid_ReturnFalse()  {
        when(mockUtil.isURLValid("htttttp://google.com")).thenReturn(false);
        assertEquals(mockUtil.isURLValid("htttttp://google.com"), false);
    }

    @Test
    public void testisURLValid_ReturnTrue()  {
        when(mockUtil.isURLValid("http://google.com")).thenReturn(true);
        assertEquals(mockUtil.isURLValid("http://google.com"), true);
    }

    @Test
    public void testgetURLStrings_urlStringInput_null_ReturnArrayStrings()  {
        String urlStringInput = "http://google.com,http://facebook.com";
        String[] urlStrings = {"http://facebook.com", "http://facebook.com"};
        when(mockUtil.getURLStrings(urlStringInput, null)).thenReturn(urlStrings);
        assertEquals(mockUtil.getURLStrings(urlStringInput, null), urlStrings);
    }

    @Test
    public void testgetURLStrings_null_urlStringFile_ReturnArrayStrings()  {
        String urlStringFile = "/logs/URL_Strings.txt";
        String[] urlStrings = {"http://facebook.com", "http://facebook.com"};
        when(mockUtil.getURLStrings(null, urlStringFile)).thenReturn(urlStrings);
        assertEquals(mockUtil.getURLStrings(null, urlStringFile), urlStrings);
    }

    @Test
    public void testgetURLStrings_urlStringInput_urlStringFile_ReturnArrayStrings()  {
        String urlStringFile = "/logs/URL_Strings.txt";
        String urlStringInput = "http://google.com,http://facebook.com";
        String[] urlStrings = {"http://facebook.com", "http://facebook.com"};
        when(mockUtil.getURLStrings(urlStringInput, urlStringFile)).thenReturn(urlStrings);
        assertEquals(mockUtil.getURLStrings(urlStringInput, urlStringFile), urlStrings);
    }

    @Test
    public void testgetURLStrings_null_null_ReturnArrayStrings()  {
        when(mockUtil.getURLStrings(null, null)).thenReturn(null);
        assertEquals(mockUtil.getURLStrings(null, null), null);
    }

    @Test
    public void testgenerateDateFilename_ReturnStringFilename()  {
        when(mockUtil.generateDateFilename()).thenReturn("2017-08-27_11-04-58");
        assertEquals(mockUtil.generateDateFilename(), "2017-08-27_11-04-58");
    }

    @Test
    public void testreadURLStringFromFile_filename_ReturnArrayStrings()  {
        String urlStringFile = "/logs/URL_Strings.txt";
        String[] urlStrings = {"http://facebook.com", "http://facebook.com"};
        when(mockUtil.readURLStringFromFile(urlStringFile)).thenReturn(urlStrings);
        assertEquals(mockUtil.readURLStringFromFile(urlStringFile), urlStrings);
    }

    @Test
    public void testreadURLStringFromFile_filename_ReturnNull()  {
        String urlStringFile = "/logs/URL_Strings.txt";
        when(mockUtil.readURLStringFromFile(urlStringFile)).thenReturn(null);
        assertEquals(mockUtil.readURLStringFromFile(urlStringFile), null);
    }






}
