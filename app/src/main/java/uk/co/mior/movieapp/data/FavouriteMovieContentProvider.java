package uk.co.mior.movieapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static uk.co.mior.movieapp.data.FavouriteMovieContract.FavouriteMovieEntry.TABLE_NAME;

public class FavouriteMovieContentProvider extends ContentProvider {

    public static final int FAVOURITE_MOVIES = 100;
    public static final int FAVOURITE_MOVIE_WITH_ID = 101;
    private FavouriteMovieDbHelper mFavouriteMovieDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();


    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // directory
        uriMatcher.addURI(FavouriteMovieContract.AUTHORITY, FavouriteMovieContract.PATH_FAVOURITE_MOVIES, FAVOURITE_MOVIES);
        // single item
        uriMatcher.addURI(FavouriteMovieContract.AUTHORITY, FavouriteMovieContract.PATH_FAVOURITE_MOVIES + "/#", FAVOURITE_MOVIE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mFavouriteMovieDbHelper = new FavouriteMovieDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mFavouriteMovieDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch(match) {
            case FAVOURITE_MOVIES:
                long id = db.insert(TABLE_NAME, null, values);
                if ( id > 0 ) {
                    //success
                    returnUri = ContentUris.withAppendedId(FavouriteMovieContract.FavouriteMovieEntry.CONTENT_URI, id);

                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}