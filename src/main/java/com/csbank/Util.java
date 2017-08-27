package com.csbank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {
    private static final Logger LOG = LogManager.getLogger(Util.class);

    /**
     * Get URL strings from user input or from file.
     * @param urlStringInput URL string from user input
     * @param urlStringFile URL strings from file.
     * @return URL Strings, otherwise, null
     */
    public String[] getURLStrings(String urlStringInput, String urlStringFile) {
        String[] urlStrings = null;
        if (urlStringInput != null) {
            urlStrings = urlStringFile.split(",");
        } else if (urlStringFile != null) {
            urlStrings = readURLStringFromFile(urlStringFile);
        }
        return urlStrings;
    }

    /**
     * Generate filename using Date now.
     * @return String in date format
     */
    public String generateDateFilename() {
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss") ;
        return dateFormat.format(date);
    }


    /**
     * Read URL strings from the file
     * @param filename
     * @return Array String
     */
    public String[] readURLStringFromFile(String filename) {
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
     * Check if the URL string is valid or not
     * @param urlString URL String
     * @return True if valid, otherwise, False
     */
    public boolean isURLValid(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (java.net.MalformedURLException | java.net.URISyntaxException e) {
            return false;
        }
    }

}
