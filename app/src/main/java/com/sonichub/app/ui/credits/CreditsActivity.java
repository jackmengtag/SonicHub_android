package com.sonichub.app.ui.credits;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ActivityCreditsBinding;
import com.sonichub.app.util.Ui;

import java.util.List;

public class CreditsActivity extends AppCompatActivity implements CreditsContract.View {
    private ActivityCreditsBinding binding;
    private CreditsPresenter presenter;
    private PackageAdapter packageAdapter;
    private LedgerAdapter ledgerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreditsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        presenter = new CreditsPresenter(this);
        binding.backButton.setOnClickListener(v -> finish());

        packageAdapter = new PackageAdapter(item -> updateRechargeText());
        binding.packageList.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        binding.packageList.setAdapter(packageAdapter);

        ledgerAdapter = new LedgerAdapter();
        binding.ledgerList.setLayoutManager(new LinearLayoutManager(this));
        binding.ledgerList.setAdapter(ledgerAdapter);

        binding.rechargeButton.setOnClickListener(v -> {
            Models.PackageItem item = packageAdapter.selected();
            if (item != null) {
                binding.rechargeButton.setEnabled(false);
                presenter.recharge(item.id);
            }
        });
        presenter.load();
    }

    private void updateRechargeText() {
        Models.PackageItem item = packageAdapter.selected();
        binding.rechargeButton.setText(item == null ? "充值"
                : "充值 ¥" + (item.priceCents / 100) + " · 到账 " + item.credits + " 积分");
    }

    @Override
    public void showCredits(Models.CreditAccount account) {
        binding.availableText.setText(String.valueOf(account.available));
        binding.reservedText.setText("冻结中 " + account.reserved + " 积分");
    }

    @Override
    public void showPackages(List<Models.PackageItem> items) {
        packageAdapter.setItems(items);
        updateRechargeText();
    }

    @Override
    public void showLedger(List<Models.LedgerEntry> items) {
        ledgerAdapter.setItems(items);
    }

    @Override
    public void onRecharged(Models.CreditAccount account) {
        binding.rechargeButton.setEnabled(true);
        showCredits(account);
        Ui.toast(this, "充值成功（演示支付）");
    }

    @Override
    public void showError(String message) {
        binding.rechargeButton.setEnabled(true);
        Ui.toast(this, message);
    }
}
