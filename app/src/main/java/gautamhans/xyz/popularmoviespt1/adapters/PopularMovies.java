package gautamhans.xyz.popularmoviespt1.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import gautamhans.xyz.popularmoviespt1.R;
import gautamhans.xyz.popularmoviespt1.models.Results;

/**
 * Created by Gautam on 24-Jun-17.
 */

public class PopularMovies extends RecyclerView.Adapter<PopularMovies.ViewHolder>{

    private Context context;
    private static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    private List<Results> mResults;
    private MovieClickListener movieClickListener;

    public interface MovieClickListener{
        void onMovieClick(String id);
    }

    public PopularMovies(Context context, List<Results> mResults, MovieClickListener movieClickListener) {
        this.context = context;
        this.mResults = mResults;
        this.movieClickListener = movieClickListener;
    }

    @Override
    public PopularMovies.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_popular_movies, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PopularMovies.ViewHolder holder, int position) {
        Picasso.with(context).load(POSTER_BASE_URL + mResults.get(position).getPoster_path())
                .placeholder(R.drawable.noposter)
                .error(R.drawable.noposter)
                .into(holder.moviePoster);
        holder.movieTitle.setText(mResults.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView moviePoster;
        TextView movieTitle;
        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            moviePoster = (ImageView) itemView.findViewById(R.id.movie_poster);
            movieTitle = (TextView) itemView.findViewById(R.id.movie_title);
            cardView = (CardView) itemView.findViewById(R.id.card_view_popular);
            cardView.setOnClickListener(clickListener);
            movieTitle.setOnClickListener(clickListener);
            moviePoster.setOnClickListener(clickListener);
        }

        private View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = getAdapterPosition();
                String id = mResults.get(clickedPosition).getId();
                Log.d(String.valueOf(this), "Movie ID: " +id);
                movieClickListener.onMovieClick(id);
            }
        };
    }
}
