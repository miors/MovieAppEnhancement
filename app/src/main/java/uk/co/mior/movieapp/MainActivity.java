package uk.co.mior.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.mior.movieapp.data.FavouriteMovieContract;
import uk.co.mior.movieapp.utilities.MovieJsonUtils;
import uk.co.mior.movieapp.utilities.NetworkUtils;

/**
 * MainActivity is the entrance point of the app
 */
public class MainActivity extends AppCompatActivity implements
        MovieRecyclerViewAdapter.ListItemClickListener, AdapterView
        .OnItemSelectedListener {

    private static final String QUERY_STRING = "";
    private static final String SCROLL_POSITION = "scroll position";
    private MovieRecyclerViewAdapter mAdapter;
    private List<MovieReturned> mMovieData;
    private Cursor mFavouriteMovieCursorData;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.rv_movies)
    RecyclerView mRecyclerView;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.tv_error_message_display)
    TextView mErrorMessageDisplay;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.pb_progress)
    ProgressBar mProgressBar;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout mCoordinatorLayout;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mySwipeRefreshLayout;
    private static final String TAG = "MainActivity";
    private static final int MOVIE_QUERY_LOADER = 22;
    private static final int MOVIE_CURSOR_QUERY_LOADER = 33;
    private List<MovieReturned> mMovieReturneds;
    private Spinner spinner;
    private int mRestoredScrollPosition;
    private boolean refresh;

    private final LoaderManager.LoaderCallbacks<Cursor>
            movieCursorQueryLoaderListener = new LoaderManager
            .LoaderCallbacks<Cursor>() {

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
                    Log.d(TAG, "loadInBackground: method called for cursor");
                    try {
                        return getContentResolver().query
                                (FavouriteMovieContract.FavouriteMovieEntry
                                                .CONTENT_URI,
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
                    Log.d(TAG, "deliverResult: method called for cursor");
                    super.deliverResult(data);
                    mFavouriteMovieCursorData = data;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.d(TAG, "onLoadFinished: method called for cursor");
            mProgressBar.setVisibility(View.INVISIBLE);
            mySwipeRefreshLayout.setRefreshing(false);
            if (data.moveToFirst()) {

                // data row exists
                int i = 0;
                List<MovieReturned> temp = new ArrayList<>();
                do {
                    String title = data.getString(data.getColumnIndex("title"));
                    String posterPath = data.getString(data.getColumnIndex
                            ("posterPath"));
                    String overview = data.getString(data.getColumnIndex
                            ("overview"));
                    String releaseDate = data.getString(data.getColumnIndex
                            ("releaseDate"));
                    double voteAverage = data.getDouble(data.getColumnIndex
                            ("voteAverage"));
                    int id = data.getInt(data.getColumnIndex("id"));

                    // create object for each data row
                    temp.add(i, new MovieReturned(title, posterPath,
                            overview, voteAverage, releaseDate, id));
                    i++;
                } while (data.moveToNext());

                mMovieData = temp;
                mAdapter.setData(mMovieData);
                setStaggered();

                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setAdapter(mAdapter);
                scrollToOriginalPosition();

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

    private final LoaderManager.LoaderCallbacks<List<MovieReturned>>
            movieQueryLoaderListener = new LoaderManager
            .LoaderCallbacks<List<MovieReturned>>() {

        @Override
        public Loader<List<MovieReturned>> onCreateLoader(int id, final
        Bundle args) {
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
                    if (searchQueryString == null || TextUtils.isEmpty
                            (searchQueryString)) {
                        return null;
                    }
                    try {
                        URL movieRequestUrl = NetworkUtils.buildUrl
                                (searchQueryString);
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
        public void onLoadFinished(Loader<List<MovieReturned>> loader,
                                   List<MovieReturned> result) {
            Log.d(TAG, "onLoadFinished: method called for MovieReturned");
            mProgressBar.setVisibility(View.INVISIBLE);
            mySwipeRefreshLayout.setRefreshing(false);
            mMovieData = result;
            if (result != null) {
                mAdapter.setData(result);
                setStaggered();

                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setAdapter(mAdapter);

                scrollToOriginalPosition();

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
        Log.d(TAG, "onOptionsItemSelected: method called");
        switch (item.getItemId()) {
            case R.id.menu_refresh:

                String selectionName = spinner.getSelectedItem().toString();

                Bundle queryBundle = prepareBundleQueryString(selectionName);

                mySwipeRefreshLayout.setRefreshing(true);
                loadLoader(selectionName, queryBundle);
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

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array
                .sort_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinner.setAdapter(adapter);

        SharedPreferences sharedPref = getSharedPreferences
                ("SharedPrefFileName", MODE_PRIVATE);
        int spinnerValue = sharedPref.getInt("userSelection", -1);
        if (spinnerValue != -1) {
            // set the selected value of the spinner
            spinner.setSelection(spinnerValue);
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int
            position, long id) {
        Log.d(TAG, "onItemSelected: method called");

        mMovieData = null;
        mMovieReturneds = null;
        mFavouriteMovieCursorData = null;
        String selectionName = parent.getItemAtPosition(position).toString();
        Bundle queryBundle = prepareBundleQueryString(selectionName);

        loadLoader(selectionName, queryBundle);

    }

    private void loadLoader(String selectionName, Bundle queryBundle) {
        Loader<List<MovieReturned>> movieReturnedLoader =
                getSupportLoaderManager().getLoader(MOVIE_QUERY_LOADER);
        Loader<Cursor> cursorLoader = getSupportLoaderManager().getLoader
                (MOVIE_CURSOR_QUERY_LOADER);

        if (selectionName.equalsIgnoreCase(getString(R.string.my_favourite))) {
            if (movieReturnedLoader != null) {
                getSupportLoaderManager().destroyLoader(MOVIE_QUERY_LOADER);
            }
            if (cursorLoader == null) {
                getSupportLoaderManager().initLoader
                        (MOVIE_CURSOR_QUERY_LOADER, queryBundle,
                                movieCursorQueryLoaderListener);
            } else {
                getSupportLoaderManager().restartLoader
                        (MOVIE_CURSOR_QUERY_LOADER, queryBundle,
                                movieCursorQueryLoaderListener);
            }
        } else {
            if (cursorLoader != null) {
                getSupportLoaderManager().destroyLoader
                        (MOVIE_CURSOR_QUERY_LOADER);
            }
            if (movieReturnedLoader == null) {
                getSupportLoaderManager().initLoader(MOVIE_QUERY_LOADER,
                        queryBundle, movieQueryLoaderListener);
            } else {
                getSupportLoaderManager().restartLoader(MOVIE_QUERY_LOADER,
                        queryBundle, movieQueryLoaderListener);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int userChoice = spinner.getSelectedItemPosition();
        SharedPreferences sharedPref = getSharedPreferences
                ("SharedPrefFileName", 0);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putInt("userSelection", userChoice);
        prefEditor.apply();

        try {
            //get the index of first visible item
            StaggeredGridLayoutManager layoutManager = (
                    (StaggeredGridLayoutManager) mRecyclerView
                            .getLayoutManager());
            int[] firstVisibleItems = layoutManager
                    .findFirstVisibleItemPositions(null);

            outState.putInt(SCROLL_POSITION, firstVisibleItems[0]);

            Log.d(TAG, "onSaveInstanceState: method called: first item index " +
                    "" + firstVisibleItems[0]);
        } catch (Exception e) {
            showErrorMessage();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: method called");

        try {
            mRestoredScrollPosition = savedInstanceState.getInt
                    (SCROLL_POSITION);
        } catch (Exception e) {
            showErrorMessage();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onListItemClick(int clickedItemIndex) {
        // want to reload cursor when it goes back main activity
        mFavouriteMovieCursorData = null;
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

        mySwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout
                .OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh = true;
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                String selectionName = spinner.getSelectedItem().toString();

                Bundle queryBundle = prepareBundleQueryString(selectionName);

                mySwipeRefreshLayout.setRefreshing(true);
                loadLoader(selectionName, queryBundle);
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
        mErrorMessageDisplay.setText(getResources().getString(R.string
                .internet_error));
        mRecyclerView.setVisibility(View.INVISIBLE);
        Snackbar.make(mCoordinatorLayout, "Please check internet connection",
                Snackbar.LENGTH_LONG).show();
        Log.d(TAG, "showErrorMessage: is shown");
    }

    private void showFavouriteMovieErrorMessage() {
        mErrorMessageDisplay.setText(getResources().getString(R.string
                .no_result));
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        Log.d(TAG, "showErrorMessage: is shown");
    }

    private void setStaggered() {
        int numberOfColumnsPortrait = 2;
        int numberOfColumnsLandscape = 3;

        if (MainActivity.this.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager
                    (numberOfColumnsPortrait, 1));
        } else {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager
                    (numberOfColumnsLandscape, 1));
        }
    }

    private Bundle prepareBundleQueryString(String selectedItem) {
        Bundle queryBundle = new Bundle();
        String placeholder = "";
        if (selectedItem.equalsIgnoreCase(getString(R.string.most_popular))) {
            placeholder = "popular";
        } else if (selectedItem.equalsIgnoreCase(getString(R.string
                .top_rated))) {
            placeholder = "top_rated";
        } else if (selectedItem.equalsIgnoreCase(getString(R.string
                .my_favourite))) {
            placeholder = "my favourite";
        } else {
            showErrorMessage();
        }
        queryBundle.putString(QUERY_STRING, placeholder);
        return queryBundle;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: method called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: method called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: method called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: method called");
    }

    private void scrollToOriginalPosition() {
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
    }
}
