package uk.co.mior.movieapp;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieReturned implements Parcelable {

    private final String title;
    private final String posterPath;
    private final String overview;
    private final double voteAverage;
    private final String releaseDate;

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
    }

    public MovieReturned(String title, String posterPath, String overview, double voteAverage, String releaseDate) {
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
    }

    private MovieReturned(Parcel in) {
        this.title = in.readString();
        this.posterPath = in.readString();
        this.overview = in.readString();
        this.voteAverage = in.readDouble();
        this.releaseDate = in.readString();
    }

    public static final Creator<MovieReturned> CREATOR = new Creator<MovieReturned>() {
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
