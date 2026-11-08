package com.sonichub.app.ui.create;

import com.sonichub.app.data.model.Models;

import java.util.List;

public interface CreateContract {
    interface View {
        void showQuote(Models.Quote quote, long balance);

        void showGenerating(Models.Generation generation);

        void updateGeneration(Models.Generation generation);

        void showResult(String workId);

        void published(Models.Work work);

        void cancelled();

        void showError(String message);
    }

    interface Presenter {
        void quote(List<String> styles, int duration, String quality, boolean stems);

        void generate(String quoteId, String prompt, String lyrics, List<String> styles, int duration,
                      String vocal, String quality, boolean stems, String visibility);

        void poll(String generationId);

        void publish(String workId, String title, String visibility, List<String> tags);

        void cancel(String generationId);
    }
}
