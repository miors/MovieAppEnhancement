package uk.co.mior.movieapp.utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.mior.movieapp.MovieReturned;

public class MovieJsonUtils {

    public static List<MovieReturned> getMovieObjectsFromJson(String movieJsonStr) throws JSONException {
        List<MovieReturned> parsedMovieData = new ArrayList<>();

        JSONObject movies = new JSONObject(movieJsonStr);
        JSONArray movieArray = movies.getJSONArray("results");

        for (int i = 0; i < movieArray.length(); i++) {

             /* Get the JSON object representing each movie */
            JSONObject eachMovie = movieArray.getJSONObject(i);
            String title = eachMovie.getString("title");
            String posterPath = eachMovie.getString("poster_path");
            String overview = eachMovie.getString("overview");
            double voteAverage = eachMovie.getDouble("vote_average");
            String releaseDate = eachMovie.getString("release_date");

            String BASE_URL = "http://image.tmdb.org/t/p/w185";

            parsedMovieData.add(i, new MovieReturned(title, BASE_URL + posterPath,
                    overview, voteAverage, releaseDate));
        }
        return parsedMovieData;
    }
}
