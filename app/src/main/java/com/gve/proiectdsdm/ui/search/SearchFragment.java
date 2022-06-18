package com.gve.proiectdsdm.ui.search;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gve.proiectdsdm.SearchAdapter;
import com.gve.proiectdsdm.SearchItem;
import com.gve.proiectdsdm.databinding.FragmentSearchBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchAdapter searchAdapter;
    private List<SearchItem> searchList;

    public interface SearchComm {
        void passSearchAdapter(SearchAdapter searchAdapter);
    }

    SearchComm searchComm;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SearchViewModel searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        searchList = new ArrayList<>();
        File gallery = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        if (gallery.exists()) {
            File[] photos = gallery.listFiles((dir, name) -> (name.startsWith("DSDM")));
            if (photos != null) {
                for (File photo : photos) {
                    searchList.add(new SearchItem(Uri.fromFile(photo), photo.getName().substring(0, photo.getName().length() - 4), photo.getName().substring(5, 7) + "/" + photo.getName().substring(7, 9) + "/" + photo.getName().substring(9, 13)));
                }
            }
        }

        final RecyclerView searchRV = binding.searchRv;
        searchRV.setHasFixedSize(true);
        searchRV.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager searchLM = new LinearLayoutManager(super.requireContext());
        searchAdapter = new SearchAdapter(super.requireContext(), searchList);
        searchRV.setLayoutManager(searchLM);
        searchRV.setAdapter(searchAdapter);
        searchComm.passSearchAdapter(searchAdapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        searchComm = (SearchComm) context;
    }
}