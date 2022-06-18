package com.gve.proiectdsdm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> implements Filterable {

    private Context context;
    private List<SearchItem> searchList;
    private List<SearchItem> searchListFull;

    class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView dateView;
        ImageButton shareButton;

        SearchViewHolder(View searchView) {
            super(searchView);
            imageView = searchView.findViewById(R.id.search_item_image);
            nameView = searchView.findViewById(R.id.search_item_name);
            dateView = searchView.findViewById(R.id.search_item_date);
            shareButton = searchView.findViewById(R.id.share_btn);
        }

    }

    public SearchAdapter(Context context, List<SearchItem> searchList) {
        this.context = context;
        this.searchList = searchList;
        this.searchListFull = new ArrayList<>(searchList);
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchItem currentItem = searchList.get(position);

        try {
            holder.imageView.setImageBitmap(Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(context.getContentResolver(), currentItem.getPhoto()), 50, 50, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.imageView.setTag(currentItem.getPhoto().toString().substring(7));
        holder.nameView.setText(currentItem.getName());
        holder.dateView.setText(currentItem.getDate());

        holder.shareButton.setOnClickListener(view -> {
            File sharedFile = new File(holder.imageView.getTag().toString());
            Uri sharedURI = FileProvider.getUriForFile(context, "com.gve.proiectdsdm.provider", sharedFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Look at this photo!");
            shareIntent.putExtra(Intent.EXTRA_STREAM, sharedURI);
            Intent shareChooser = Intent.createChooser(shareIntent, "Share Photo");
            List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(shareChooser, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resolveInfoList) {
                context.grantUriPermission(resolveInfo.activityInfo.packageName, sharedURI, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            context.startActivity(shareChooser);
        });
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<SearchItem> searchListFiltered = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                searchListFiltered.addAll(searchListFull);
            } else {
                String searchFilterPattern = constraint.toString().toLowerCase().trim();
                for (SearchItem item : searchListFull) {
                    if (item.getName().toLowerCase().contains(searchFilterPattern)) {
                        searchListFiltered.add(item);
                    }
                }
            }

            FilterResults searchResults = new FilterResults();
            searchResults.values = searchListFiltered;
            return searchResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            searchList.clear();
            searchList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

}
