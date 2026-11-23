package com.sonichub.app.ui.plaza;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.sonichub.app.R;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.FragmentPlazaBinding;
import com.sonichub.app.ui.player.PlayerActivity;
import com.sonichub.app.ui.search.SearchActivity;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class PlazaFragment extends Fragment implements PlazaContract.View {
    private static final String[] FALLBACK_GENRES = {"流行", "电子", "国风", "Lo-fi", "爵士", "R&B"};

    private FragmentPlazaBinding binding;
    private PlazaPresenter presenter;
    private PlazaAdapter adapter;
    private final List<Filter> filters = new ArrayList<>();
    private final List<TextView> chips = new ArrayList<>();
    private int selected = 0;

    private static class Filter {
        final String label;
        final String sort;
        final String genre;

        Filter(String label, String sort, String genre) {
            this.label = label;
            this.sort = sort;
            this.genre = genre;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlazaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        presenter = new PlazaPresenter(this);
        adapter = new PlazaAdapter(work -> {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_WORK_ID, work.id);
            intent.putExtra(PlayerActivity.EXTRA_TITLE, work.title);
            startActivity(intent);
        });
        binding.plazaGrid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.plazaGrid.setAdapter(adapter);
        binding.searchButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), SearchActivity.class)));

        buildFilters(null);
        load();
        presenter.loadGenres();
    }

    /**
     * 按实际存在的作品风格构建分类。genres 为 null 时先展示兜底分类，
     * 待接口返回后重建并保留当前选中的分类。
     */
    private void buildFilters(@Nullable List<String> genres) {
        Filter current = selected >= 0 && selected < filters.size() ? filters.get(selected) : null;
        binding.filterRow.removeAllViews();
        filters.clear();
        chips.clear();

        addChip("全部", "latest", "");
        addChip("热门", "hot", "");
        if (genres == null) {
            for (String genre : FALLBACK_GENRES) addChip(genre, "latest", genre);
        } else {
            for (String genre : genres) {
                if (genre == null || genre.trim().isEmpty()) continue;
                addChip(genre.trim(), "latest", genre.trim());
            }
        }

        selected = 0;
        if (current != null) {
            for (int i = 0; i < filters.size(); i++) {
                Filter filter = filters.get(i);
                if (filter.sort.equals(current.sort) && filter.genre.equals(current.genre)) {
                    selected = i;
                    break;
                }
            }
        }
        updateChipSelection();
    }

    private void addChip(String label, String sort, String genre) {
        final int index = filters.size();
        filters.add(new Filter(label, sort, genre));

        TextView chip = new TextView(requireContext());
        chip.setText(label);
        chip.setTextSize(11.5f);
        chip.setPadding(Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 7),
                Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 7));
        chip.setBackgroundResource(R.drawable.bg_chip);
        chip.setTextColor(getResources().getColor(R.color.text_2));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = Ui.dp(requireContext(), 8);
        chip.setLayoutParams(lp);
        chip.setOnClickListener(v -> {
            if (selected == index) return;
            selected = index;
            updateChipSelection();
            load();
        });
        chips.add(chip);
        binding.filterRow.addView(chip);
    }

    private void updateChipSelection() {
        for (int i = 0; i < chips.size(); i++) {
            boolean isSelected = i == selected;
            TextView chip = chips.get(i);
            chip.setSelected(isSelected);
            chip.setTextColor(getResources().getColor(isSelected ? R.color.accent : R.color.text_2));
            chip.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void load() {
        if (selected < 0 || selected >= filters.size()) return;
        Filter filter = filters.get(selected);
        if (filter.genre.isEmpty()) {
            binding.sectionTitle.setText("hot".equals(filter.sort) ? "本周热门" : "全部作品");
        } else {
            binding.sectionTitle.setText(filter.genre + "作品");
        }
        presenter.load(filter.sort, filter.genre);
    }

    @Override
    public void showWorks(List<Models.Work> works) {
        adapter.setItems(works);
    }

    @Override
    public void showGenres(List<String> genres) {
        if (binding == null || genres == null || genres.isEmpty()) return;
        buildFilters(genres);
    }

    @Override
    public void showError(String message) {
        Ui.toast(requireContext(), message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
