package uk.co.mior.movieapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.List;

import uk.co.mior.movieapp.utilities.MovieJsonUtils;
import uk.co.mior.movieapp.utilities.NetworkUtils;

public class MainActivity extends AppCompatActivity implements  MovieRecyclerViewAdapter.ListItemClickListener, AdapterView.OnItemSelectedListener{

    private MovieRecyclerViewAdapter mAdapter;
    private List<MovieReturned> mMovieData;
    private RecyclerView mRecyclerView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;
    private static final String TAG = "MainActivity";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.spinner);

        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setOnItemSelectedListener(this);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,R.array.sort_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinner.setAdapter(adapter);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected: method called");
        mMovieData = null;
        if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("Most Popular")){
            Log.d(TAG, "onItemSelected: Fetching most popular movies");
            new FetchMovieTask().execute("popular");
        } else if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("Top Rated")){
            Log.d(TAG, "onItemSelected: Fetching top rated movies");
            new FetchMovieTask().execute("top_rated");
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class FetchMovieTask extends AsyncTask<String, Void, List<MovieReturned>> {
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
    }

    private void showRecyclerView(){
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        Log.d(TAG, "showRecyclerView: is shown");
    }

    private void showErrorMessage(){
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Please check internet connection!",
                Toast.LENGTH_LONG).show();
        Log.d(TAG, "showErrorMessage: is shown");
    }
}
