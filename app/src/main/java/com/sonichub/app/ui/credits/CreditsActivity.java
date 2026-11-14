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
    private LedgerAdapter ledgerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreditsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        presenter = new CreditsPresenter(this);
        binding.backButton.setOnClickListener(v -> finish());

        ledgerAdapter = new LedgerAdapter();
        binding.ledgerList.setLayoutManager(new LinearLayoutManager(this));
        binding.ledgerList.setAdapter(ledgerAdapter);

        presenter.load();
    }

    @Override
    public void showCredits(Models.CreditAccount account) {
        binding.availableText.setText(String.valueOf(account.available));
        binding.reservedText.setText("冻结中 " + account.reserved + " 积分");
    }

    @Override
    public void showLedger(List<Models.LedgerEntry> items) {
        ledgerAdapter.setItems(items);
    }

    @Override
    public void showError(String message) {
        Ui.toast(this, message);
    }
}
