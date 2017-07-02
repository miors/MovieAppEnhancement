package uk.co.mior.movieapp.utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.mior.movieapp.MovieReturned;
import uk.co.mior.movieapp.Reviews;

public class MovieJsonUtils {

    public static String getYoutubeTrailer(String trailerJsonStr) throws JSONException {
        JSONObject trailers = new JSONObject(trailerJsonStr);
        JSONArray trailerArray = trailers.getJSONArray("results");
        for (int i = 0; i < trailerArray.length(); i++) {
            /* Get the JSON object representing each index */
            JSONObject eachMovie = trailerArray.getJSONObject(i);

            String type = eachMovie.getString("type");
            if (type.equalsIgnoreCase("Trailer")){
                return eachMovie.getString("key");
            }
        }
        return null;
    }

    public static List<Reviews> getReviewsList(String reviewJsonStr) throws JSONException {
        List<Reviews> parsedReviewData = new ArrayList<>();
        JSONObject reviews = new JSONObject(reviewJsonStr);
        JSONArray reviewArray = reviews.getJSONArray("results");
        for (int i = 0; i < reviewArray.length(); i++) {
            JSONObject eachReview = reviewArray.getJSONObject(i);
            String author = eachReview.getString("author");
            String content = eachReview.getString("content");

            parsedReviewData.add(i, new Reviews(author, content));
        }
        return parsedReviewData;
    }

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
            int id = eachMovie.getInt("id");

            //String BASE_URL = "http://image.tmdb.org/t/p/w185";

            parsedMovieData.add(i, new MovieReturned(title, posterPath,
                    overview, voteAverage, releaseDate, id));
        }
        return parsedMovieData;
    }
}
