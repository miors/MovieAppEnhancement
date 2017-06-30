package uk.co.mior.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
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


public class MainActivity extends AppCompatActivity implements  MovieRecyclerViewAdapter.ListItemClickListener,
        AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<List<MovieReturned>>{

    private static final String QUERY_STRING = "";
    private MovieRecyclerViewAdapter mAdapter;
    private List<MovieReturned> mMovieData;
    private RecyclerView mRecyclerView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mProgressBar;
    private static final String TAG = "MainActivity";
    private static final int MOVIE_QUERY_LOADER = 22;

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
        Bundle queryBundle = new Bundle();
        if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("Most Popular")){
            Log.d(TAG, "onItemSelected: Fetching most popular movies");
            queryBundle.putString(QUERY_STRING, "popular");
        } else if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("Top Rated")){
            Log.d(TAG, "onItemSelected: Fetching top rated movies");
            queryBundle.putString(QUERY_STRING, "top_rated");
        } else {
            showErrorMessage();
        }
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<List<MovieReturned>> movieReturnedLoader = loaderManager.getLoader(MOVIE_QUERY_LOADER);

        if (movieReturnedLoader == null){
            loaderManager.initLoader(MOVIE_QUERY_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(MOVIE_QUERY_LOADER, queryBundle, this);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public Loader<List<MovieReturned>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<MovieReturned>>(this){

            private List<MovieReturned> mMovieReturneds;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null){
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                if (mMovieReturneds != null){
                    mProgressBar.setVisibility(View.INVISIBLE);
                    deliverResult(mMovieData);
                }else{
                    forceLoad();
                }
            }

            @Override
            public List<MovieReturned> loadInBackground() {
                String searchQueryString = args.getString(QUERY_STRING);
                if (searchQueryString == null || TextUtils.isEmpty(searchQueryString)){
                    return null;
                }
                try {
                    URL movieRequestUrl = NetworkUtils.buildUrl(searchQueryString);
                    String jsonMovieResponse = NetworkUtils
                            .getResponseFromHttpUrl( movieRequestUrl);

                    return MovieJsonUtils
                            .getMovieObjectsFromJson(jsonMovieResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(List<MovieReturned> movieReturneds) {
                mMovieReturneds = movieReturneds;
                super.deliverResult(movieReturneds);
            }
        };


    }

    @Override
    public void onLoadFinished(Loader<List<MovieReturned>> loader, List<MovieReturned> result) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mMovieData = result;
        if (result != null) {
            mAdapter.setData(result);
            int numberOfColumnsPortrait = 2;
            int numberOfColumnsLandscape = 3;

            if(MainActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(numberOfColumnsPortrait, 1));
            }
            else{
                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(numberOfColumnsLandscape, 1));
            }

            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(mAdapter);
            showRecyclerView();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<MovieReturned>> loader) {

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
