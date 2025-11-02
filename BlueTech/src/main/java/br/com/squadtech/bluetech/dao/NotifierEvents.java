package br.com.squadtech.bluetech.dao;

import br.com.squadtech.bluetech.notify.AsyncNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central helper to be called by DAOs after mutating events, to trigger async notifications.
 * Keeps controllers lean and makes events easy to invoke from persistence layer.
 */
public final class NotifierEvents {
    private static final Logger log = LoggerFactory.getLogger(NotifierEvents.class);
    private NotifierEvents() {}

    public static void onNewVersionSubmitted(long versaoId) {
        try {
            AsyncNotifier.getInstance().notifySubmissionAsync(versaoId);
        } catch (Exception e) {
            log.warn("Falha ao enfileirar notificação de envio (versaoId={}) - {}", versaoId, e.toString());
        }
    }

    public static void onFeedbackSaved(long feedbackId) {
        try {
            AsyncNotifier.getInstance().notifyFeedbackAsync(feedbackId);
        } catch (Exception e) {
            log.warn("Falha ao enfileirar notificação de feedback (feedbackId={}) - {}", feedbackId, e.toString());
        }
    }
}

