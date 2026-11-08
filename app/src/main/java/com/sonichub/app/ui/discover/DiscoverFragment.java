package com.sonichub.app.ui.discover;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.FragmentDiscoverBinding;
import com.sonichub.app.ui.artist.ArtistActivity;
import com.sonichub.app.ui.create.CreateActivity;
import com.sonichub.app.ui.player.PlayerActivity;
import com.sonichub.app.ui.search.SearchActivity;
import com.sonichub.app.util.Ui;

import java.util.List;

public class DiscoverFragment extends Fragment implements DiscoverContract.View {
    private FragmentDiscoverBinding binding;
    private DiscoverPresenter presenter;
    private WorkAdapter workAdapter;
    private ArtistAdapter artistAdapter;
    private boolean followingTab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        presenter = new DiscoverPresenter(this);

        String name = Session.userName();
        String greeting = name == null || name.isEmpty() ? "晚上好，先听听看" : "晚上好，" + name;
        binding.greetingText.setText(greeting);

        workAdapter = new WorkAdapter(new WorkAdapter.Listener() {
            @Override
            public void onWork(Models.Work work) {
                Intent intent = new Intent(requireContext(), PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_WORK_ID, work.id);
                intent.putExtra(PlayerActivity.EXTRA_TITLE, work.title);
                startActivity(intent);
            }

            @Override
            public void onLike(Models.Work work) {
                presenter.like(work);
            }
        });
        binding.feedList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.feedList.setAdapter(workAdapter);

        artistAdapter = new ArtistAdapter(artist -> {
            Intent intent = new Intent(requireContext(), ArtistActivity.class);
            intent.putExtra(ArtistActivity.EXTRA_ARTIST_ID, artist.id);
            startActivity(intent);
        });
        binding.artistRail.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        binding.artistRail.setAdapter(artistAdapter);

        binding.searchButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), SearchActivity.class)));
        binding.searchBar.setOnClickListener(v -> startActivity(new Intent(requireContext(), SearchActivity.class)));
        binding.createButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), CreateActivity.class)));
        binding.refreshButton.setOnClickListener(v -> presenter.load(followingTab));
        binding.allArtists.setOnClickListener(v -> Ui.toast(requireContext(), "在广场或搜索中发现更多音乐人"));

        binding.tabRecommend.setOnClickListener(v -> selectTab(false));
        binding.tabFollowing.setOnClickListener(v -> selectTab(true));

        setupHeroWave();
        selectTab(false);
    }

    private void selectTab(boolean following) {
        followingTab = following;
        if (following) {
            binding.tabRecommend.setBackground(null);
            binding.tabFollowing.setBackgroundResource(com.sonichub.app.R.drawable.bg_segment_selected);
        } else {
            binding.tabRecommend.setBackgroundResource(com.sonichub.app.R.drawable.bg_segment_selected);
            binding.tabFollowing.setBackground(null);
        }
        binding.tabRecommend.setTextColor(getResources().getColor(following
                ? com.sonichub.app.R.color.text_3 : com.sonichub.app.R.color.text));
        binding.tabFollowing.setTextColor(getResources().getColor(following
                ? com.sonichub.app.R.color.text : com.sonichub.app.R.color.text_3));
        presenter.load(following);
    }

    private void setupHeroWave() {
        binding.heroWave.removeAllViews();
        int[] heights = {18, 40, 28, 56, 44, 70, 34, 52, 26, 64, 38, 30, 50, 22, 58, 36};
        for (int h : heights) {
            View bar = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(requireContext(), h));
            lp.weight = 1;
            lp.rightMargin = Ui.dp(requireContext(), 3);
            bar.setLayoutParams(lp);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(Ui.dp(requireContext(), 2));
            drawable.setColor(0xAAFF7A45);
            bar.setBackground(drawable);
            binding.heroWave.addView(bar);
        }
    }

    @Override
    public void showWorks(List<Models.Work> works) {
        workAdapter.setItems(works);
    }

    @Override
    public void showArtists(List<Models.UserCard> artists) {
        artistAdapter.setItems(artists);
    }

    @Override
    public void showError(String message) {
        Ui.toast(requireContext(), message);
    }

    @Override
    public void onLikeUpdated(Models.Work work) {
        workAdapter.update(work);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
