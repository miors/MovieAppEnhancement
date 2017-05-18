package uk.co.mior.movieapp.utilities;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.mior.movieapp.MovieReturned;

/**
 * Created by Mior on 15/05/2017.
 */

public class MovieJsonUtils {

    public static List<MovieReturned> getMovieObjectsFromJson(Context context, String movieJsonStr) throws JSONException {
        List<MovieReturned> parsedMovieData = new ArrayList<>();

        JSONObject movies = new JSONObject(movieJsonStr);
        JSONArray movieArray = movies.getJSONArray("results");

        for (int i = 0; i < movieArray.length(); i++) {

             /* Get the JSON object representing each movie */
            JSONObject eachMovie = movieArray.getJSONObject(i);
            String originalTitle = eachMovie.getString("original_title");
            String posterPath = eachMovie.getString("poster_path");
            String overview = eachMovie.getString("overview");
            String voteAverage = eachMovie.getString("vote_average");
            String releaseDate = eachMovie.getString("release_date");

            String BASE_URL = "http://image.tmdb.org/t/p/w185";

            parsedMovieData.add(i, new MovieReturned());
            parsedMovieData.get(i).setOriginalTitle(originalTitle);
            parsedMovieData.get(i).setPosterPath(BASE_URL + posterPath);
            parsedMovieData.get(i).setOverview(overview);
            parsedMovieData.get(i).setVoteAverage(voteAverage);
            parsedMovieData.get(i).setReleaseDate(releaseDate);
        }
        return parsedMovieData;
    }
}
