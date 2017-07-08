package uk.co.mior.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.mior.movieapp.data.FavouriteMovieContract;
import uk.co.mior.movieapp.utilities.MovieJsonUtils;
import uk.co.mior.movieapp.utilities.NetworkUtils;


public class MainActivity extends AppCompatActivity implements MovieRecyclerViewAdapter.ListItemClickListener,
        AdapterView.OnItemSelectedListener {

    private static final String QUERY_STRING = "";
    private static final String SCROLL_POSITION = "scroll position";
    private MovieRecyclerViewAdapter mAdapter;
    private List<MovieReturned> mMovieData;
    private Cursor mFavouriteMovieCursorData;
    //private RecyclerView mRecyclerView;
    @BindView(R.id.rv_movies) RecyclerView mRecyclerView;
    //private TextView mErrorMessageDisplay;
    @BindView(R.id.tv_error_message_display) TextView mErrorMessageDisplay;
    //private ProgressBar mProgressBar;
    @BindView(R.id.pb_progress) ProgressBar mProgressBar;
    private static final String TAG = "MainActivity";
    private static final int MOVIE_QUERY_LOADER = 22;
    private static final int MOVIE_CURSOR_QUERY_LOADER = 33;
    private List<MovieReturned> mMovieReturneds;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private Spinner spinner;
    private LoaderManager loaderManager;
    private Loader<List<MovieReturned>> movieReturnedLoader;
    private Loader<Cursor> mCursorLoader;
    private int mRestoredScrollPosition;
    private boolean refresh;

    private final LoaderManager.LoaderCallbacks<Cursor> movieCursorQueryLoaderListener = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<Cursor>(MainActivity.this) {

                @Override
                protected void onStartLoading() {
                    Log.d(TAG, "onStartLoading: cursor");
                    if (args == null) {
                        return;
                    }

                    mProgressBar.setVisibility(View.VISIBLE);
                    if (mFavouriteMovieCursorData != null) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        deliverResult(mFavouriteMovieCursorData);
                    } else {
                        forceLoad();
                    }
                }

                @Override
                public Cursor loadInBackground() {
                    try {
                        return getContentResolver().query(FavouriteMovieContract.FavouriteMovieEntry.CONTENT_URI,
                                null,
                                null,
                                null,
                                null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public void deliverResult(Cursor data) {
                    super.deliverResult(data);
                    mFavouriteMovieCursorData = data;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mySwipeRefreshLayout.setRefreshing(false);
            mFavouriteMovieCursorData = data;
            if (data.moveToFirst()) {

                // data row exists
                int i = 0;
                List<MovieReturned> temp = new ArrayList<>();
                do {
                    String title = data.getString(data.getColumnIndex("title"));
                    String posterPath = data.getString(data.getColumnIndex("posterPath"));
                    String overview = data.getString(data.getColumnIndex("overview"));
                    String releaseDate = data.getString(data.getColumnIndex("releaseDate"));
                    double voteAverage = data.getDouble(data.getColumnIndex("voteAverage"));
                    int id = data.getInt(data.getColumnIndex("id"));

                    // create object for each data row
                    temp.add(i, new MovieReturned(title, posterPath, overview, voteAverage, releaseDate, id));
                    i++;
                } while (data.moveToNext());

                mMovieData = temp;
                mAdapter.setData(mMovieData);
                setStaggered();

                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setAdapter(mAdapter);

                try {
                    //go to the was position if not refresh
                    if (!refresh) {
                        if (mRestoredScrollPosition > 0) {
                            mRecyclerView.scrollToPosition(mRestoredScrollPosition);
                        } else {
                            // go to fist position
                            mRecyclerView.scrollToPosition(0);
                            //reset refresh to false
                            refresh = false;
                        }
                    }
                } catch (Exception e) {
                    showErrorMessage();
                }

                showRecyclerView();

            } else {
                // show error
                showFavouriteMovieErrorMessage();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }


    };

    private final LoaderManager.LoaderCallbacks<List<MovieReturned>> movieQueryLoaderListener = new LoaderManager.LoaderCallbacks<List<MovieReturned>>() {

        @Override
        public Loader<List<MovieReturned>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<List<MovieReturned>>(MainActivity.this) {

                @Override
                protected void onStartLoading() {
                    Log.d(TAG, "onStartLoading: MovieReturned");
                    super.onStartLoading();
                    if (args == null) {
                        return;
                    }
                    mProgressBar.setVisibility(View.VISIBLE);
                    if (mMovieReturneds != null) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        deliverResult(mMovieData);
                    } else {
                        forceLoad();
                    }
                }

                @Override
                public List<MovieReturned> loadInBackground() {
                    String searchQueryString = args.getString(QUERY_STRING);
                    if (searchQueryString == null || TextUtils.isEmpty(searchQueryString)) {
                        return null;
                    }
                    try {
                        URL movieRequestUrl = NetworkUtils.buildUrl(searchQueryString);
                        String jsonMovieResponse = NetworkUtils
                                .getResponseFromHttpUrl(movieRequestUrl);

                        return MovieJsonUtils
                                .getMovieObjectsFromJson(jsonMovieResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public void deliverResult(List<MovieReturned> movieReturneds) {
                    super.deliverResult(movieReturneds);
                    mMovieReturneds = movieReturneds;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<List<MovieReturned>> loader, List<MovieReturned> result) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mySwipeRefreshLayout.setRefreshing(false);
            mMovieData = result;
            if (result != null) {
                mAdapter.setData(result);
                setStaggered();

                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setAdapter(mAdapter);

                try {
                    //go to the was position if not refresh
                    if (!refresh) {
                        if (mRestoredScrollPosition > 0) {
                            mRecyclerView.scrollToPosition(mRestoredScrollPosition);
                        } else {
                            // go to fist position
                            mRecyclerView.scrollToPosition(0);
                            //reset refresh to false
                            refresh = false;
                        }
                    }

                } catch (Exception e) {
                    showErrorMessage();
                }

                showRecyclerView();
            } else {
                showErrorMessage();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<MovieReturned>> loader) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:

                String selectedItem = spinner.getSelectedItem().toString();

                Bundle queryBundle = prepareBundleQueryString(selectedItem);

                mySwipeRefreshLayout.setRefreshing(true);
                LoaderManager loaderManager = getSupportLoaderManager();

                if (selectedItem.equalsIgnoreCase("My Favourite")) {
                    loaderManager.initLoader(MOVIE_CURSOR_QUERY_LOADER, queryBundle, movieCursorQueryLoaderListener);
                } else {
                    loaderManager.initLoader(MOVIE_QUERY_LOADER, queryBundle, movieQueryLoaderListener);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: method called");
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.spinner);

        spinner = (Spinner) item.getActionView();
        spinner.setOnItemSelectedListener(this);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.sort_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinner.setAdapter(adapter);

        SharedPreferences sharedPref = getSharedPreferences("SharedPrefFileName", MODE_PRIVATE);
        int spinnerValue = sharedPref.getInt("userSelection", -1);
        if (spinnerValue != -1) {
            // set the selected value of the spinner
            spinner.setSelection(spinnerValue);
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected: method called");

        int userChoice = parent.getSelectedItemPosition();
        SharedPreferences sharedPref = getSharedPreferences("SharedPrefFileName", 0);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putInt("userSelection", userChoice);
        prefEditor.apply();

        mMovieData = null;
        mMovieReturneds = null;
        Bundle queryBundle = prepareBundleQueryString(parent.getItemAtPosition(position).toString());

        loaderManager = getSupportLoaderManager();
        movieReturnedLoader = loaderManager.getLoader(MOVIE_QUERY_LOADER);
        mCursorLoader = loaderManager.getLoader(MOVIE_CURSOR_QUERY_LOADER);

        if (parent.getItemAtPosition(position).toString().equalsIgnoreCase(getString(R.string.most_popular)) || parent.getItemAtPosition(position).toString().equalsIgnoreCase(getString(R.string.top_rated))) {
            if (movieReturnedLoader == null) {
                loaderManager.initLoader(MOVIE_QUERY_LOADER, queryBundle, movieQueryLoaderListener);
            } else {
                loaderManager.restartLoader(MOVIE_QUERY_LOADER, queryBundle, movieQueryLoaderListener);
            }
        } else if (parent.getItemAtPosition(position).toString().equalsIgnoreCase(getString(R.string.my_favourite))) {
            if (mCursorLoader == null) {
                loaderManager.initLoader(MOVIE_CURSOR_QUERY_LOADER, queryBundle, movieCursorQueryLoaderListener);
            } else {
                loaderManager.restartLoader(MOVIE_CURSOR_QUERY_LOADER, queryBundle, movieCursorQueryLoaderListener);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {
            //get the index of first visible item
            StaggeredGridLayoutManager layoutManager = ((StaggeredGridLayoutManager) mRecyclerView.getLayoutManager());
            int[] firstVisibleItems = layoutManager.findFirstVisibleItemPositions(null);

            outState.putInt(SCROLL_POSITION, firstVisibleItems[0]);

            Log.d(TAG, "onSaveInstanceState: method called: first item index " + firstVisibleItems[0]);
        } catch (Exception e) {
            showErrorMessage();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: method called");

        try {
            mRestoredScrollPosition = savedInstanceState.getInt(SCROLL_POSITION);
        } catch (Exception e) {
            showErrorMessage();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (mCursorLoader != null) {
            loaderManager.destroyLoader(MOVIE_CURSOR_QUERY_LOADER);
        }


        if (movieReturnedLoader != null) {
            loaderManager.destroyLoader(MOVIE_QUERY_LOADER);
        }
        Context context = MainActivity.this;
        Class destinationActivity = MovieDetailActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra("movieDetailObject", mMovieData.get(clickedItemIndex));
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: method called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAdapter = new MovieRecyclerViewAdapter(this, null, this);
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        mySwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                String selectedItem = spinner.getSelectedItem().toString();

                Bundle queryBundle = prepareBundleQueryString(selectedItem);

                mySwipeRefreshLayout.setRefreshing(true);
                LoaderManager loaderManager = getSupportLoaderManager();

                if (selectedItem.equalsIgnoreCase("My Favourite")) {
                    loaderManager.initLoader(MOVIE_CURSOR_QUERY_LOADER, queryBundle, movieCursorQueryLoaderListener);
                } else {
                    loaderManager.initLoader(MOVIE_QUERY_LOADER, queryBundle, movieQueryLoaderListener);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: method called");
    }

    private void showRecyclerView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        Log.d(TAG, "showRecyclerView: is shown");
    }

    private void showErrorMessage() {
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mErrorMessageDisplay.setText(getResources().getString(R.string.internet_error));
        mRecyclerView.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Please check internet connection!",
                Toast.LENGTH_LONG).show();
        Log.d(TAG, "showErrorMessage: is shown");
    }

    private void showFavouriteMovieErrorMessage() {
        mErrorMessageDisplay.setText(getResources().getString(R.string.no_result));
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        Log.d(TAG, "showErrorMessage: is shown");
    }

    private void setStaggered() {
        int numberOfColumnsPortrait = 2;
        int numberOfColumnsLandscape = 3;

        if (MainActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(numberOfColumnsPortrait, 1));
        } else {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(numberOfColumnsLandscape, 1));
        }
    }

    private Bundle prepareBundleQueryString(String selectedItem) {
        Bundle queryBundle = new Bundle();
        String placeholder = "";
        if (selectedItem.equalsIgnoreCase(getString(R.string.most_popular))) {
            placeholder = "popular";
        } else if (selectedItem.equalsIgnoreCase(getString(R.string.top_rated))) {
            placeholder = "top_rated";
        } else if (selectedItem.equalsIgnoreCase(getString(R.string.my_favourite))) {
            placeholder = "my favourite";
        } else {
            showErrorMessage();
        }
        queryBundle.putString(QUERY_STRING, placeholder);
        return queryBundle;
    }
}
