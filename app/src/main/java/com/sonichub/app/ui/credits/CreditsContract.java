package com.sonichub.app.ui.credits;

import com.sonichub.app.data.model.Models;

import java.util.List;

public interface CreditsContract {
    interface View {
        void showCredits(Models.CreditAccount account);

        void showLedger(List<Models.LedgerEntry> items);

        void showError(String message);
    }

    interface Presenter {
        void load();
    }
}
