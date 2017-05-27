package uk.co.mior.movieapp;

import android.content.Context;
import android.os.AsyncTask;

import java.net.URL;
import java.util.List;

import uk.co.mior.movieapp.utilities.MovieJsonUtils;
import uk.co.mior.movieapp.utilities.NetworkUtils;

class FetchMovieTask extends AsyncTask<String, Void, List<MovieReturned>> {

    private final Context context;
    private final AsyncTaskCompleteListener<List<MovieReturned>> listener;

    public FetchMovieTask(Context ctx, AsyncTaskCompleteListener<List<MovieReturned>> listener)
    {
        this.context = ctx;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onTaskStart();
    }

    @Override
    protected void onPostExecute(List<MovieReturned> movieData) {
        listener.onTaskComplete(movieData);
    }

    @Override
    protected List<MovieReturned> doInBackground(String... params) {

        try {
            URL movieRequestUrl = NetworkUtils.buildUrl(params[0]);
            String jsonMovieResponse = NetworkUtils
                    .getResponseFromHttpUrl( movieRequestUrl);

            return MovieJsonUtils
                    .getMovieObjectsFromJson(jsonMovieResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}