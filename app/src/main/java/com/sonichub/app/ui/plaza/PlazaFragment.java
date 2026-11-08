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
    private FragmentPlazaBinding binding;
    private PlazaPresenter presenter;
    private PlazaAdapter adapter;
    private String sort = "latest";
    private String genre = "";
    private final List<TextView> chips = new ArrayList<>();

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

        buildFilters();
        load();
    }

    private void buildFilters() {
        binding.filterRow.removeAllViews();
        chips.clear();
        addChip("最新", "latest", "");
        addChip("热门", "hot", "");
        addChip("电子", "latest", "电子");
        addChip("Lo-fi", "latest", "Lo-fi");
        addChip("国风", "latest", "国风");
        addChip("摇滚", "latest", "摇滚");
        addChip("影视配乐", "latest", "影视配乐");
        updateChipSelection();
    }

    private void addChip(String label, String newSort, String newGenre) {
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
            sort = newSort;
            genre = newGenre;
            updateChipSelection();
            load();
        });
        chips.add(chip);
        binding.filterRow.addView(chip);
    }

    private void updateChipSelection() {
        for (TextView chip : chips) {
            boolean selected = chip.getText().toString().equals(
                    "latest".equals(sort) && genre.isEmpty() ? "最新"
                            : "hot".equals(sort) ? "热门" : genre);
            chip.setSelected(selected);
            chip.setTextColor(getResources().getColor(selected ? R.color.accent : R.color.text_2));
            chip.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void load() {
        binding.sectionTitle.setText(genre.isEmpty()
                ? ("hot".equals(sort) ? "本周热门" : "最新发布")
                : genre + "作品");
        presenter.load(sort, genre);
    }

    @Override
    public void showWorks(List<Models.Work> works) {
        adapter.setItems(works);
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
