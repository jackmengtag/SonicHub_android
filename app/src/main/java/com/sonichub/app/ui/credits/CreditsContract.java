package com.sonichub.app.ui.credits;

import com.sonichub.app.data.model.Models;

import java.util.List;

public interface CreditsContract {
    interface View {
        void showCredits(Models.CreditAccount account);

        void showPackages(List<Models.PackageItem> items);

        void showLedger(List<Models.LedgerEntry> items);

        void onRecharged(Models.CreditAccount account);

        void showError(String message);
    }

    interface Presenter {
        void load();

        void recharge(String packageId);
    }
}
