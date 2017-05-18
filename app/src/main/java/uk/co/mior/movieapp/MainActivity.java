package uk.co.mior.movieapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.List;

import uk.co.mior.movieapp.utilities.MovieJsonUtils;
import uk.co.mior.movieapp.utilities.NetworkUtils;

public class MainActivity extends AppCompatActivity implements  MovieRecyclerViewAdapter.ListItemClickListener{

    private MovieRecyclerViewAdapter mAdapter;
    private List<MovieReturned> mMovieData;
    private RecyclerView mRecyclerView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemThatWasSelected = item.getItemId();
        if (menuItemThatWasSelected == R.id.action_sort_by_most_popular){
            new FetchMovieTask().execute("popular");
        } else if (menuItemThatWasSelected == R.id.action_sort_by_top_rated){
            new FetchMovieTask().execute("top_rated");
        }
        return true;
    }

    public class FetchMovieTask extends AsyncTask<String, Void, List<MovieReturned>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<MovieReturned> movieData) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mMovieData = movieData;
            if (movieData != null) {
                mAdapter.setData(movieData);
                int numberOfColumns = 2;
                mRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, numberOfColumns));
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setAdapter(mAdapter);
                showRecyclerView();
            } else {
                showErrorMessage();
            }
        }

        @Override
        protected List<MovieReturned> doInBackground(String... params) {
            URL movieRequestUrl = null;

            try {
                movieRequestUrl = NetworkUtils.buildUrl(params[0]);
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpUrl( movieRequestUrl);

                List<MovieReturned> movieData = MovieJsonUtils
                        .getMovieObjectsFromJson(MainActivity.this, jsonMovieResponse);

                return movieData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Context context = MainActivity.this;
        Class destinationActivity = MovieDetailActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra("movieDetailObject", mMovieData.get(clickedItemIndex));
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_progress);

        mAdapter = new MovieRecyclerViewAdapter(this, null, this);
        new FetchMovieTask().execute("popular");
    }

    private void showRecyclerView(){
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage(){
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }
}
