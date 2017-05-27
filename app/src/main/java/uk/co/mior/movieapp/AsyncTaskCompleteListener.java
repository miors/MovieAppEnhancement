package uk.co.mior.movieapp;

interface AsyncTaskCompleteListener<T> {
    void onTaskComplete(T result);
    void onTaskStart();
}
