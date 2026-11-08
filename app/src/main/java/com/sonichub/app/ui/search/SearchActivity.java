package com.sonichub.app.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ActivitySearchBinding;
import com.sonichub.app.ui.artist.ArtistActivity;
import com.sonichub.app.ui.discover.ArtistAdapter;
import com.sonichub.app.ui.discover.WorkAdapter;
import com.sonichub.app.ui.player.PlayerActivity;

public class SearchActivity extends AppCompatActivity implements SearchContract.View {
    private ActivitySearchBinding binding;
    private SearchPresenter presenter;
    private WorkAdapter workAdapter;
    private ArtistAdapter artistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        presenter = new SearchPresenter(this);

        workAdapter = new WorkAdapter(new WorkAdapter.Listener() {
            @Override
            public void onWork(Models.Work work) {
                Intent intent = new Intent(SearchActivity.this, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_WORK_ID, work.id);
                intent.putExtra(PlayerActivity.EXTRA_TITLE, work.title);
                startActivity(intent);
            }

            @Override
            public void onLike(Models.Work work) {
                presenter.like(work);
            }
        });
        artistAdapter = new ArtistAdapter(artist -> {
            Intent intent = new Intent(SearchActivity.this, ArtistActivity.class);
            intent.putExtra(ArtistActivity.EXTRA_ARTIST_ID, artist.id);
            startActivity(intent);
        });
        binding.workResults.setLayoutManager(new LinearLayoutManager(this));
        binding.workResults.setAdapter(workAdapter);
        binding.artistResults.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        binding.artistResults.setAdapter(artistAdapter);

        binding.backButton.setOnClickListener(v -> finish());
        binding.searchAction.setOnClickListener(v -> search());
        binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search();
                return true;
            }
            return false;
        });
        binding.emptyText.setVisibility(TextView.VISIBLE);
    }

    private void search() {
        String query = binding.searchInput.getText().toString().trim();
        if (query.isEmpty()) return;
        presenter.search(query);
    }

    @Override
    public void showResults(Models.SearchData data) {
        boolean hasArtists = data.artists != null && !data.artists.isEmpty();
        boolean hasWorks = data.works != null && !data.works.isEmpty();
        binding.artistHeader.setVisibility(hasArtists ? TextView.VISIBLE : TextView.GONE);
        binding.artistResults.setVisibility(hasArtists ? TextView.VISIBLE : TextView.GONE);
        binding.workHeader.setVisibility(hasWorks ? TextView.VISIBLE : TextView.GONE);
        binding.workResults.setVisibility(hasWorks ? TextView.VISIBLE : TextView.GONE);
        binding.emptyText.setVisibility(!hasArtists && !hasWorks ? TextView.VISIBLE : TextView.GONE);
        artistAdapter.setItems(data.artists);
        workAdapter.setItems(data.works);
    }

    @Override
    public void onLikeUpdated(Models.Work work) {
        workAdapter.update(work);
    }

    @Override
    public void showError(String message) {
        com.sonichub.app.util.Ui.toast(this, message);
    }
}
