package br.com.squadtech.bluetech.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Singleton for background notification dispatch to avoid blocking UI threads.
 */
public final class AsyncNotifier {
    private static final Logger log = LoggerFactory.getLogger(AsyncNotifier.class);

    private static volatile AsyncNotifier INSTANCE;
    private final ExecutorService pool;
    private final NotifierFacade notifier;

    private AsyncNotifier() {
        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "bluetech-notify");
            t.setDaemon(true);
            return t;
        };
        this.pool = Executors.newFixedThreadPool(2, tf);
        this.notifier = NotifierFacade.getInstance();
    }

    public static AsyncNotifier getInstance() {
        if (INSTANCE == null) {
            synchronized (AsyncNotifier.class) {
                if (INSTANCE == null) INSTANCE = new AsyncNotifier();
            }
        }
        return INSTANCE;
    }

    public void notifySubmissionAsync(long versaoId) {
        pool.submit(() -> {
            try {
                String to = notifier.notifySubmission(versaoId);
                log.info("Notificação de envio enviada para: {} (versaoId={})", to, versaoId);
            } catch (Exception e) {
                log.warn("Falha ao enviar notificação de envio (versaoId={}) - {}", versaoId, e.toString());
            }
        });
    }

    public void notifyFeedbackAsync(long feedbackId) {
        pool.submit(() -> {
            try {
                String to = notifier.notifyFeedback(feedbackId);
                log.info("Notificação de feedback enviada para: {} (feedbackId={})", to, feedbackId);
            } catch (Exception e) {
                log.warn("Falha ao enviar notificação de feedback (feedbackId={}) - {}", feedbackId, e.toString());
            }
        });
    }

    public void shutdown() {
        pool.shutdown();
    }
}

