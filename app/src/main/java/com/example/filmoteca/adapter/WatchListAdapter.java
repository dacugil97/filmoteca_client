package com.example.filmoteca.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmoteca.R;
import com.example.filmoteca.model.TmdbMovie;
import com.example.filmoteca.model.WatchList;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class WatchListAdapter extends RecyclerView.Adapter<WatchListAdapter.WatchListAdapterHolder>  {

    private List<WatchList> wlists;
    private boolean fromSocial;
    private Context context;

    private static final String IMG_URL = "https://image.tmdb.org/t/p/original";

    public WatchListAdapter(List<WatchList> wl, boolean fromSocial, Context context){
        this.wlists = wl;
        this.fromSocial = fromSocial;
        this.context = context;
    }

    @NonNull
    @Override
    public WatchListAdapter.WatchListAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_watchlist, parent, false);

        return new WatchListAdapter.WatchListAdapterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull WatchListAdapterHolder holder, int position) {
        //holder.randomPoster.getLayoutParams().height = 288;
        //holder.randomPoster.getLayoutParams().width = 195;
        WatchList wl = wlists.get(position);
        holder.tvName.setText(wl.getName());

        int total = 0;
        List<TmdbMovie> movies = wl.getMovies();
        if(movies!=null){

            total = movies.size();
        }
        holder.tvTotal.setText(String.valueOf(total));
        if(wlists.get(position).getMovies().size()!=0){
            setRandomPoster(holder, position);
        }else{
            holder.randomPoster.setImageDrawable(null);
        }
        if(fromSocial==false){
            holder.byTv.setVisibility(View.GONE);
            holder.byNickTv.setVisibility(View.GONE);
            if(wl.isPub()){
                holder.publicImg.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_users));
            }
        }else{
            holder.publicImg.setImageDrawable(null);
            holder.byTv.setVisibility(View.VISIBLE);
            holder.byNickTv.setVisibility(View.VISIBLE);
            holder.byNickTv.setText(wl.getAutor());
        }
    }

    @Override
    public int getItemCount() {
        return wlists.size();
    }

    private void setRandomPoster(WatchListAdapterHolder holder, int position){
        Random r = new Random();
        int random = r.nextInt(wlists.get(position).getMovies().size());
        TmdbMovie movie = wlists.get(position).getMovies().get(random);
        if(movie.getOmdbMovie()!=null && !"N/A".equals(movie.getOmdbMovie().getPoster()) &&
                !movie.getOmdbMovie().getPoster().isEmpty()){
            Picasso.get().load(movie.getOmdbMovie().getPoster()).into(holder.randomPoster);
        }else{
            Picasso.get().load(IMG_URL + movie.getPosterPath()).into(holder.randomPoster);
        }
    }

    public void addData(WatchList wl){
        this.wlists.add(wl);
        notifyDataSetChanged();
    }

    public void clearData(){
        this.wlists.clear();
        notifyDataSetChanged();
    }

    public void deleteWatchList(int position){
        this.wlists.remove(position);
        notifyDataSetChanged();
    }

    public WatchList getWatchList(int position){
        return this.wlists.get(position);
    }

    public class WatchListAdapterHolder extends RecyclerView.ViewHolder{
        private TextView tvName;
        private TextView tvTotal;
        private TextView byTv;
        private TextView byNickTv;
        private ImageView randomPoster;
        private ImageView publicImg;

        public WatchListAdapterHolder (@NonNull View itemView){
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            byTv = itemView.findViewById(R.id.byTv);
            byNickTv = itemView.findViewById(R.id.byNickTv);
            randomPoster = itemView.findViewById(R.id.imgRandom);
            publicImg = itemView.findViewById(R.id.imgPublic);
        }
    }

    public List<WatchList> getWlists() {
        return wlists;
    }

    public void setWlists(List<WatchList> wlists) {
        this.wlists = wlists;
        notifyDataSetChanged();
    }
}
