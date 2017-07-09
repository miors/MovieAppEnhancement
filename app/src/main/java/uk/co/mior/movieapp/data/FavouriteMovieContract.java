package uk.co.mior.movieapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines DB contract
 */
public class FavouriteMovieContract {

    public static final String AUTHORITY = "uk.co.mior.movieapp";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" +
            AUTHORITY);
    public static final String PATH_FAVOURITE_MOVIES = "favourite_movies";

    public static final class FavouriteMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath
                        (PATH_FAVOURITE_MOVIES).build();

        //Favourite movie table and column names
        public static final String TABLE_NAME = "favourite_movies";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTERPATH = "posterPath";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTEAVERAGE = "voteAverage";
        public static final String COLUMN_RELEASEDATE = "releaseDate";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_BACKDROPPATH = "backdropPath";
    }

}
