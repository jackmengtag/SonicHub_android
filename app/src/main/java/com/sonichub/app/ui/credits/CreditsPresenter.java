package com.sonichub.app.ui.credits;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.model.Models;

import java.util.ArrayList;

public class CreditsPresenter implements CreditsContract.Presenter {
    private final CreditsContract.View view;
    private final Repository repository;

    public CreditsPresenter(CreditsContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void load() {
        repository.credits(new RepoCallback<Models.CreditAccount>() {
            @Override
            public void onSuccess(Models.CreditAccount data) {
                view.showCredits(data);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
        repository.packages(new RepoCallback<Models.PackagesData>() {
            @Override
            public void onSuccess(Models.PackagesData data) {
                view.showPackages(data.items == null ? new ArrayList<>() : data.items);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
        repository.ledger(new RepoCallback<Models.LedgerData>() {
            @Override
            public void onSuccess(Models.LedgerData data) {
                view.showLedger(data.items == null ? new ArrayList<>() : data.items);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void recharge(String packageId) {
        repository.recharge(packageId, new RepoCallback<Models.CreditAccount>() {
            @Override
            public void onSuccess(Models.CreditAccount data) {
                view.onRecharged(data);
                repository.ledger(new RepoCallback<Models.LedgerData>() {
                    @Override
                    public void onSuccess(Models.LedgerData ledger) {
                        view.showLedger(ledger.items == null ? new ArrayList<>() : ledger.items);
                    }

                    @Override
                    public void onError(String message) {
                    }
                });
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
}
