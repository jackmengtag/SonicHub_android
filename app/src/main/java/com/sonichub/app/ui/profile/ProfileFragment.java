package com.sonichub.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sonichub.app.data.Navigator;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.FragmentProfileBinding;
import com.sonichub.app.ui.credits.CreditsActivity;
import com.sonichub.app.ui.login.LoginActivity;
import com.sonichub.app.ui.verify.VerifyActivity;
import com.sonichub.app.ui.works.WorksActivity;
import com.sonichub.app.util.Ui;

public class ProfileFragment extends Fragment implements ProfileContract.View {
    private FragmentProfileBinding binding;
    private ProfilePresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        presenter = new ProfilePresenter(this);
        binding.rowWorks.setOnClickListener(v -> requireLoginOr(WorksActivity.class));
        binding.rowCredits.setOnClickListener(v -> requireLoginOr(CreditsActivity.class));
        binding.creditButton.setOnClickListener(v -> requireLoginOr(CreditsActivity.class));
        binding.rowVerify.setOnClickListener(v -> requireLoginOr(VerifyActivity.class));
        binding.profileBlock.setOnClickListener(v -> {
            if (!Session.isLoggedIn()) startActivity(new Intent(requireContext(), LoginActivity.class));
        });
        binding.rowLogout.setOnClickListener(v -> {
            if (!Session.isLoggedIn()) {
                startActivity(new Intent(requireContext(), LoginActivity.class));
                return;
            }
            Session.clear();
            Navigator.toLogin(requireActivity());
        });
        binding.headerLogout.setOnClickListener(v -> {
            if (Session.isLoggedIn()) {
                Session.clear();
                Navigator.toLogin(requireActivity());
            } else {
                startActivity(new Intent(requireContext(), LoginActivity.class));
            }
        });
    }

    private void requireLoginOr(Class<?> target) {
        if (!Session.isLoggedIn()) {
            Ui.toast(requireContext(), "请先登录");
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return;
        }
        startActivity(new Intent(requireContext(), target));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) presenter.load();
    }

    @Override
    public void showMe(Models.MeData data) {
        if (data.user != null) {
            binding.name.setText(data.user.displayName);
            binding.bio.setText(data.user.bio == null || data.user.bio.isEmpty()
                    ? "写歌给失眠的人听" : data.user.bio);
            binding.avatar.setCover(data.user.avatar);
            binding.avatar.setCircle(true);
        }
        binding.creditValue.setText(data.credits == null ? "0" : String.valueOf(data.credits.available));
        if (data.counts != null) {
            binding.stats.setText(data.counts.works + " 作品 · " + data.counts.followers
                    + " 粉丝 · " + data.counts.friends + " 朋友");
        }
        binding.verifyState.setText(data.musician == null ? "" :
                "pending".equals(data.musician.status) ? "审核中" :
                        "verified".equals(data.musician.status) ? "已认证" :
                                "rejected".equals(data.musician.status) ? "未通过" : "");
        binding.rowLogout.setText("退出登录");
    }

    @Override
    public void showGuest() {
        binding.name.setText("未登录");
        binding.bio.setText("登录后开始写歌、关注音乐人并查看消息");
        binding.stats.setText("0 作品 · 0 粉丝 · 0 朋友");
        binding.creditValue.setText("0");
        binding.verifyState.setText("");
        binding.rowLogout.setText("登录 / 注册");
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
