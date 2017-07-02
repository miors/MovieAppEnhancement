package uk.co.mior.movieapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

import uk.co.mior.movieapp.data.FavouriteMovieContract;
import uk.co.mior.movieapp.utilities.MovieJsonUtils;
import uk.co.mior.movieapp.utilities.NetworkUtils;

public class MovieDetailActivity extends AppCompatActivity{

    private final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
    private static final int YOUTUBE_TRAILER_QUERY_LOADER = 33;
    private static final int REVIEWS_QUERY_LOADER = 44;
    private static final int FAVOURITE_MOVIE_LOADER_ID = 0;
    private MovieReturned movieReturned;
    private final String TRAILER = "trailer";
    private final String REVIEWS = "reviews";
    private Button mFavouriteButton;
    private Boolean mMovieInFavourite = false;
    private Boolean mReviewSectionShown = false;

    private final LoaderManager.LoaderCallbacks<Cursor> favouriteMovieLoaderListener = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<Cursor>(MovieDetailActivity.this) {
                //Cursor mFavouriteMovieData = null;

                @Override
                protected void onStartLoading() {
                    forceLoad();
                }

                @Override
                public Cursor loadInBackground() {

                            Uri movieIdUri =FavouriteMovieContract.FavouriteMovieEntry.CONTENT_URI.buildUpon().appendPath(Integer.toString(movieReturned.getId())).build();
                    try {
                        return getContentResolver().query(movieIdUri,
                                null,
                                null,
                                null,
                                null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;

                    }
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if ( data.moveToFirst() ) {
                //movie id exists in db
                mMovieInFavourite = true;
                mFavouriteButton.setText(getResources().getString(R.string.remove_from_favourite));
            } else {
                //movie id not in db
                mMovieInFavourite = false;
                mFavouriteButton.setText(getResources().getString(R.string.add_to_favourite));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    private final View.OnClickListener reviewClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (!mReviewSectionShown){
                mReviewSectionShown = !mReviewSectionShown;

                Bundle queryBundle = new Bundle();

                LoaderManager loaderManager = getSupportLoaderManager();
                Loader<String> reviewLoader = loaderManager.getLoader(REVIEWS_QUERY_LOADER);

                if (reviewLoader == null){
                    loaderManager.initLoader(REVIEWS_QUERY_LOADER, queryBundle, reviewLoaderListener);
                } else {
                    loaderManager.restartLoader(REVIEWS_QUERY_LOADER, queryBundle, reviewLoaderListener);
                }
            }
        }
    };

    private final LoaderManager.LoaderCallbacks<List<Reviews>> reviewLoaderListener = new LoaderManager.LoaderCallbacks<List<Reviews>>() {
        @Override
        public Loader<List<Reviews>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<List<Reviews>>(MovieDetailActivity.this) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if (args == null){
                        return;
                    }
                    forceLoad();
                }
                @Override
                public List<Reviews> loadInBackground() {
                    List<Reviews> reviews;

                    try {
                        URL reviewsRequestUrl = NetworkUtils.buildTrailerOrReviewUrl(movieReturned.getId(), REVIEWS);
                        String jsonReviewsResponse = NetworkUtils
                                .getResponseFromHttpUrl(reviewsRequestUrl);

                        reviews = MovieJsonUtils
                                .getReviewsList(jsonReviewsResponse);

                        return reviews;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<List<Reviews>> loader, List<Reviews> data) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.tv_movie_detail_linear_layout);

            if (data == null){
                Toast.makeText(MovieDetailActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                return;
            }
            if (data.size() == 0){
                TextView textViewError = new TextView(MovieDetailActivity.this);
                textViewError.setText(getResources().getString(R.string.no_review_available));
                linearLayout.addView(textViewError);
                return;
            }

            for( int i = 0; i < data.size(); i++ )
            {
                TextView textViewName = new TextView(MovieDetailActivity.this);
                textViewName.setText(data.get(i).getAuthor());
                textViewName.setTextColor(Color.BLUE);
                textViewName.setTypeface(null, Typeface.BOLD);
                textViewName.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(20, 0, 20 ,10);
                textViewName.setLayoutParams(params);


                TextView textViewContent = new TextView(MovieDetailActivity.this);
                textViewContent.setText(data.get(i).getContent());
                textViewContent.setLayoutParams(params);

                linearLayout.addView(textViewName);
                linearLayout.addView(textViewContent);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Reviews>> loader) {

        }
    };

    private final View.OnClickListener youTubeTrailerClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Bundle queryBundle = new Bundle();

            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<String> youtubeTrailerLoader = loaderManager.getLoader(YOUTUBE_TRAILER_QUERY_LOADER);

            if (youtubeTrailerLoader == null){
                loaderManager.initLoader(YOUTUBE_TRAILER_QUERY_LOADER, queryBundle, youTubeLoaderListener);
            } else {
                loaderManager.restartLoader(YOUTUBE_TRAILER_QUERY_LOADER, queryBundle, youTubeLoaderListener);
            }
        }
    };

    private final LoaderManager.LoaderCallbacks<Uri> youTubeLoaderListener = new LoaderManager.LoaderCallbacks<Uri>() {

        @Override
        public Loader<Uri> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<Uri>(MovieDetailActivity.this) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    super.onStartLoading();
                    if (args == null){
                        return;
                    }
                    forceLoad();
                }

                @Override
                public Uri loadInBackground() {
                    Uri webpage;

                    try {
                        URL trailerRequestUrl = NetworkUtils.buildTrailerOrReviewUrl(movieReturned.getId(), TRAILER);
                        String jsonTrailerResponse = NetworkUtils
                                .getResponseFromHttpUrl(trailerRequestUrl);

                        String youTubeKey = MovieJsonUtils
                                .getYoutubeTrailer(jsonTrailerResponse);

                        webpage = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                                .appendQueryParameter("v", youTubeKey)
                                .build();

                        return webpage;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Uri> loader, Uri webpage) {
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(intent);
        }

        @Override
        public void onLoaderReset(Loader<Uri> loader) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Intent intent = getIntent();

        if (null != intent && intent.hasExtra("movieDetailObject")){

            // Fetching data from a parcelable object passed from MainActivity
            movieReturned = getIntent().getParcelableExtra("movieDetailObject");

            TextView mMovieOriginalTitleTextView = (TextView) findViewById(R.id.tv_movie_original_title);
            TextView mMovieOverviewTextView = (TextView) findViewById(R.id.tv_movie_overview);
            TextView mMovieVoteAverageTextView = (TextView) findViewById(R.id.tv_movie_vote_average);
            TextView mMovieReleaseDateTextView = (TextView) findViewById(R.id.tv_movie_release_date);
            TextView mViewTrailer = (TextView) findViewById(R.id.tv_movie_trailer);
            TextView mReviews = (TextView) findViewById(R.id.tv_movie_reviews);
            ImageView mMoviePosterPathImageView = (ImageView) findViewById(R.id.iv_movie_poster_path);
            mFavouriteButton = (Button) findViewById(R.id.favouriteButton);

            mMovieOriginalTitleTextView.setText(movieReturned.getTitle());
            mMovieOverviewTextView.setText(movieReturned.getOverview());
            String rating = getResources().getString(R.string.user_rating) + movieReturned.getVoteAverage();
            mMovieVoteAverageTextView.setText(rating);
            String releaseDate = getResources().getString(R.string.release_date) + movieReturned.getReleaseDate();
            mMovieReleaseDateTextView.setText(releaseDate);
            String BASE_URL = "http://image.tmdb.org/t/p/w185";
            Picasso.with(this).load(BASE_URL + movieReturned.getPosterPath()).into(mMoviePosterPathImageView);
            mViewTrailer.setOnClickListener(youTubeTrailerClickListener);
            mReviews.setOnClickListener(reviewClickListener);

        }
        //set actionbar title
        setTitle(movieReturned.getTitle());
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> reviewLoader = loaderManager.getLoader(FAVOURITE_MOVIE_LOADER_ID);

        if (reviewLoader == null){
            loaderManager.initLoader(FAVOURITE_MOVIE_LOADER_ID, null, favouriteMovieLoaderListener);
        } else {
            loaderManager.restartLoader(FAVOURITE_MOVIE_LOADER_ID, null, favouriteMovieLoaderListener);
        }
    }

    public void onClickAddFavouriteMovie(@SuppressWarnings("UnusedParameters") View view) {
        //add if not already in favourite

        if (!mMovieInFavourite) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(FavouriteMovieContract.FavouriteMovieEntry.COLUMN_TITLE, movieReturned.getTitle());
            contentValues.put(FavouriteMovieContract.FavouriteMovieEntry.COLUMN_POSTERPATH, movieReturned.getPosterPath());
            contentValues.put(FavouriteMovieContract.FavouriteMovieEntry.COLUMN_OVERVIEW, movieReturned.getOverview());
            contentValues.put(FavouriteMovieContract.FavouriteMovieEntry.COLUMN_VOTEAVERAGE, movieReturned.getVoteAverage());
            contentValues.put(FavouriteMovieContract.FavouriteMovieEntry.COLUMN_RELEASEDATE, movieReturned.getReleaseDate());
            contentValues.put(FavouriteMovieContract.FavouriteMovieEntry.COLUMN_ID, movieReturned.getId());

            //insert favourite movie
            Uri uri = getContentResolver().insert(FavouriteMovieContract.FavouriteMovieEntry.CONTENT_URI, contentValues);

            if (uri != null){
                //Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
                Toast.makeText(getBaseContext(), "Added to favourite", Toast.LENGTH_SHORT).show();
                getSupportLoaderManager().restartLoader(FAVOURITE_MOVIE_LOADER_ID, null, favouriteMovieLoaderListener);
            }

        } else {
            //lets remove from favourite DB
            Uri movieIdUri =FavouriteMovieContract.FavouriteMovieEntry.CONTENT_URI.buildUpon().appendPath(Integer.toString(movieReturned.getId())).build();
            getContentResolver().delete(movieIdUri, null, null);
            Toast.makeText(getBaseContext(), "Removed from favourite", Toast.LENGTH_SHORT).show();
            getSupportLoaderManager().restartLoader(FAVOURITE_MOVIE_LOADER_ID, null, favouriteMovieLoaderListener);



        }

    }
}
