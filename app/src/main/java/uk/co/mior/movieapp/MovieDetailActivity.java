package uk.co.mior.movieapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Intent intent = getIntent();

        if (null != intent && intent.hasExtra("movieDetailObject")){

            // Fetching data from a parcelable object passed from MainActivity
            MovieReturned movieReturned = getIntent().getParcelableExtra("movieDetailObject");

            TextView mMovieOriginalTitleTextView = (TextView) findViewById(R.id.tv_movie_original_title);
            TextView mMovieOverviewTextView = (TextView) findViewById(R.id.tv_movie_overview);
            TextView mMovieVoteAverageTextView = (TextView) findViewById(R.id.tv_movie_vote_average);
            TextView mMovieReleaseDateTextView = (TextView) findViewById(R.id.tv_movie_release_date);
            ImageView mMoviePosterPathImageView = (ImageView) findViewById(R.id.iv_movie_poster_path);

            mMovieOriginalTitleTextView.setText(movieReturned.getTitle());
            mMovieOverviewTextView.setText(movieReturned.getOverview());
            String rating = getResources().getString(R.string.user_rating) + movieReturned.getVoteAverage();
            mMovieVoteAverageTextView.setText(rating);
            String releaseDate = getResources().getString(R.string.release_date) + movieReturned.getReleaseDate();
            mMovieReleaseDateTextView.setText(releaseDate);
            Picasso.with(this).load(movieReturned.getPosterPath()).into(mMoviePosterPathImageView);
        }
    }
}
