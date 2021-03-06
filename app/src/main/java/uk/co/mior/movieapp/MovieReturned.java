package uk.co.mior.movieapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents the class of returned movie data from server
 */
public class MovieReturned implements Parcelable {

    private final String title;
    private final String posterPath;
    private final String overview;
    private final double voteAverage;
    private final String releaseDate;
    private final int id;
    private final String backdropPath;

    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public int getId() {
        return id;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeDouble(voteAverage);
        dest.writeString(releaseDate);
        dest.writeInt(id);
        dest.writeString(backdropPath);
    }

    public MovieReturned(String title, String posterPath, String overview,
                         double voteAverage, String releaseDate, int id,
                         String backdropPath) {
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.id = id;
        this.backdropPath = backdropPath;
    }

    private MovieReturned(Parcel in) {
        this.title = in.readString();
        this.posterPath = in.readString();
        this.overview = in.readString();
        this.voteAverage = in.readDouble();
        this.releaseDate = in.readString();
        this.id = in.readInt();
        this.backdropPath = in.readString();
    }

    public static final Creator<MovieReturned> CREATOR = new
            Creator<MovieReturned>() {
                @Override
                public MovieReturned createFromParcel(Parcel in) {
                    return new MovieReturned(in);
                }

                @Override
                public MovieReturned[] newArray(int size) {
                    return new MovieReturned[size];
                }
            };
}
