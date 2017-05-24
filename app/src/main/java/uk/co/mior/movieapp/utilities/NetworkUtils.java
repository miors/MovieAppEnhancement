package uk.co.mior.movieapp.utilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    private final static String THEMOVIEDB_BASE_URL = "http://api.themoviedb.org/3";
    private final static String ENDPOINT_POPULAR = "/movie/popular";
    private final static String ENDPOINT_TOP_RATED = "/movie/top_rated";
    private final static String API_KEY = "api_key";
    private final static String API_VALUE = "VALUE_OF_API_KEY_HERE";

    public static URL buildUrl(String endpoint){
        if (endpoint.equalsIgnoreCase("popular")){
            endpoint = ENDPOINT_POPULAR;
        } else if (endpoint.equalsIgnoreCase("top_rated")){
            endpoint = ENDPOINT_TOP_RATED;
        }
        Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL + endpoint).buildUpon()
                .appendQueryParameter(API_KEY, API_VALUE)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Attribute: Code from Udacity Android Developer Nanodegree Program
     * Attribute: https://classroom.udacity.com/nanodegrees/nd801
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

                                                   Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        }catch (Exception e){
            return null;
        }
        finally {
            urlConnection.disconnect();
        }
    }
}
