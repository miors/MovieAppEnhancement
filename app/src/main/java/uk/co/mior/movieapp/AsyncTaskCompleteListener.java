package uk.co.mior.movieapp;

public interface AsyncTaskCompleteListener<T> {
    public void onTaskComplete(T result);
    public void onTaskStart();
}
