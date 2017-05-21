package uk.co.mior.movieapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.MovieViewHolder>{

    private List<MovieReturned> mData = new ArrayList<>();
    final private LayoutInflater mInflater;
    final private ListItemClickListener mOnClickListener;

    public void setData(List<MovieReturned> data) {
        mData = data;
    }

    public MovieRecyclerViewAdapter(Context context, @SuppressWarnings("SameParameterValue") List<MovieReturned> mData, ListItemClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = mData;
        this.mOnClickListener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        MovieReturned movieData = mData.get(position);
        Context context = holder.mMovieItem.getContext();
        Picasso.with(context).load(movieData.getPosterPath()).into(holder.mMovieItem);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ImageView mMovieItem;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mMovieItem = (ImageView) itemView.findViewById(R.id.iv_movie_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }
}