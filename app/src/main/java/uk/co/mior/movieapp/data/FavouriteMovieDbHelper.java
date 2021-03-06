package uk.co.mior.movieapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static uk.co.mior.movieapp.data.FavouriteMovieContract
        .FavouriteMovieEntry;

/**
 * Helper class to create/alter db/table
 */
class FavouriteMovieDbHelper extends SQLiteOpenHelper {

    // The name of the database
    private static final String DATABASE_NAME = "favouriteMoviesDb.db";

    // If you change the database scheme, you must increment the database
    // version
    private static final int VERSION = 3;

    public FavouriteMovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create favourite movies table
        final String CREATE_TABLE = "CREATE TABLE " + FavouriteMovieEntry
                .TABLE_NAME + " (" +
                FavouriteMovieEntry._ID + " INTEGER PRIMARY KEY, " +
                FavouriteMovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_POSTERPATH + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_VOTEAVERAGE + " REAL NOT NULL, " +
                FavouriteMovieEntry.COLUMN_RELEASEDATE + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                FavouriteMovieEntry.COLUMN_BACKDROPPATH + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }

    /**
     * using recommendation from
     * https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
     *
     * @param db         SQLite database name
     * @param oldVersion old version of db
     * @param newVersion new version of db
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String ALTER_TABLE_1 = "ALTER TABLE "
                + FavouriteMovieEntry.TABLE_NAME + " ADD COLUMN " +
                FavouriteMovieEntry.COLUMN_ID + " INTEGER NOT NULL);";

        final String ALTER_TABLE_2 = "ALTER TABLE "
                + FavouriteMovieEntry.TABLE_NAME + " ADD COLUMN " +
                FavouriteMovieEntry.COLUMN_BACKDROPPATH + " TEXT NOT NULL);";

        if (oldVersion < 2) {
            db.execSQL(ALTER_TABLE_1);
        }

        if (oldVersion < 3) {
            db.execSQL(ALTER_TABLE_2);
        }
    }
}
