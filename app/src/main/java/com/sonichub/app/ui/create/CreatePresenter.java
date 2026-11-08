package com.sonichub.app.ui.create;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.model.Models;

import java.util.List;

public class CreatePresenter implements CreateContract.Presenter {
    private final CreateContract.View view;
    private final Repository repository;

    public CreatePresenter(CreateContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void quote(List<String> styles, int duration, String quality, boolean stems) {
        repository.quote(styles, duration, quality, stems, new RepoCallback<Models.Quote>() {
            @Override
            public void onSuccess(Models.Quote data) {
                repository.credits(new RepoCallback<Models.CreditAccount>() {
                    @Override
                    public void onSuccess(Models.CreditAccount account) {
                        view.showQuote(data, account.available);
                    }

                    @Override
                    public void onError(String message) {
                        view.showQuote(data, -1);
                    }
                });
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void generate(String quoteId, String prompt, String lyrics, List<String> styles, int duration,
                         String vocal, String quality, boolean stems, String visibility) {
        repository.generate(quoteId, prompt, lyrics, styles, duration, vocal, quality, stems, "", visibility,
                new RepoCallback<Models.Generation>() {
                    @Override
                    public void onSuccess(Models.Generation data) {
                        view.showGenerating(data);
                    }

                    @Override
                    public void onError(String message) {
                        view.showError(message);
                    }
                });
    }

    @Override
    public void poll(String generationId) {
        repository.generation(generationId, new RepoCallback<Models.Generation>() {
            @Override
            public void onSuccess(Models.Generation data) {
                if ("succeeded".equals(data.status) && data.workId != null && !data.workId.isEmpty()) {
                    view.showResult(data.workId);
                } else if ("failed".equals(data.status) || "cancelled".equals(data.status)) {
                    view.showError(data.error == null || data.error.isEmpty() ? "生成失败" : data.error);
                } else {
                    view.updateGeneration(data);
                }
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void publish(String workId, String title, String visibility, List<String> tags) {
        repository.publish(workId, title, "", visibility, tags, new RepoCallback<Models.Work>() {
            @Override
            public void onSuccess(Models.Work data) {
                view.published(data);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void cancel(String generationId) {
        repository.cancelGeneration(generationId, new RepoCallback<Models.Generation>() {
            @Override
            public void onSuccess(Models.Generation data) {
                view.cancelled();
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
}
