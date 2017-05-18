package uk.co.mior.movieapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {
    private TextView mMovieOriginalTitleTextView;
    private TextView mMovieOverviewTextView;
    private TextView mMovieVoteAverageTextView;
    private TextView mMovieReleaseDateTextView;
    private ImageView mMoviePosterPathImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Intent intent = getIntent();
        MovieReturned movieReturned = (MovieReturned)intent.getSerializableExtra("movieDetailObject");

        mMovieOriginalTitleTextView = (TextView) findViewById(R.id.tv_movie_original_title);
        mMovieOverviewTextView = (TextView) findViewById(R.id.tv_movie_overview);
        mMovieVoteAverageTextView = (TextView) findViewById(R.id.tv_movie_vote_average);
        mMovieReleaseDateTextView = (TextView) findViewById(R.id.tv_movie_release_date);
        mMoviePosterPathImageView = (ImageView) findViewById(R.id.iv_movie_poster_path);

        mMovieOriginalTitleTextView.setText(movieReturned.getOriginalTitle());
        mMovieOverviewTextView.setText(movieReturned.getOverview());
        mMovieVoteAverageTextView.setText("User rating:" + movieReturned.getVoteAverage());
        mMovieReleaseDateTextView.setText("Release date:" + movieReturned.getReleaseDate());
        Picasso.with(this).load(movieReturned.getPosterPath()).into(mMoviePosterPathImageView);
    }
}
