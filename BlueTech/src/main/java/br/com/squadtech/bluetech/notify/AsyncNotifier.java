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

    public void notifyVersionApprovedAsync(long versaoId) {
        pool.submit(() -> {
            try {
                String to = notifier.notifyVersionApproved(versaoId);
                log.info("Versão aprovada — notificação enviada para: {} (versaoId={})", to, versaoId);
            } catch (Exception e) {
                log.warn("Falha ao notificar versão aprovada (versaoId={}) - {}", versaoId, e.toString());
            }
        });
    }

    public void notifySectionApprovedAsync(long secaoId) {
        pool.submit(() -> {
            try {
                String to = notifier.notifySectionApproved(secaoId);
                log.info("Seção aprovada — notificação enviada para: {} (secaoId={})", to, secaoId);
            } catch (Exception e) {
                log.warn("Falha ao notificar seção aprovada (secaoId={}) - {}", secaoId, e.toString());
            }
        });
    }

    public void notifyPortfolioCompletedAsync(long portifolioId) {
        pool.submit(() -> {
            try {
                String to = notifier.notifyPortfolioCompleted(portifolioId);
                log.info("Portfólio concluído — notificação enviada para: {} (portifolioId={})", to, portifolioId);
            } catch (Exception e) {
                log.warn("Falha ao notificar conclusão de portfólio (portifolioId={}) - {}", portifolioId, e.toString());
            }
        });
    }

    public void notifyCoordinatorPortfolioCompletedAsync(long portifolioId) {
        pool.submit(() -> {
            try {
                String to = notifier.notifyCoordinatorOnPortfolioCompleted(portifolioId);
                log.info("Coordenador notificado sobre portfólio concluído: {} (portifolioId={})", to, portifolioId);
            } catch (Exception e) {
                log.warn("Falha ao notificar coordenador (portifolioId={}) - {}", portifolioId, e.toString());
            }
        });
    }

    public void notifyOrientationRequestAsync(long alunoId, long professorId) {
        pool.submit(() -> {
            try {
                String to = notifier.notifyOrientationRequest(alunoId, professorId);
                log.info("Solicitação de orientação enviada para: {} (alunoId={}, professorId={})", to, alunoId, professorId);
            } catch (Exception e) {
                log.warn("Falha ao notificar solicitação de orientação (alunoId={}, professorId={}) - {}", alunoId, professorId, e.toString());
            }
        });
    }

    public void notifyOrientationResponseAsync(long alunoId, long professorId, boolean aceita) {
        pool.submit(() -> {
            try {
                String to = notifier.notifyOrientationResponse(alunoId, professorId, aceita);
                log.info("Resposta de orientação enviada para: {} (alunoId={}, professorId={})", to, alunoId, professorId);
            } catch (Exception e) {
                log.warn("Falha ao enviar resposta de orientação (alunoId={}, professorId={}) - {}", alunoId, professorId, e.toString());
            }
        });
    }

    public void notifyDefenseScheduledAsync(long defesaId) {
        pool.submit(() -> {
            try {
                notifier.notifyDefenseScheduled(defesaId);
                log.info("Notificação de defesa agendada enviada (defesaId={})", defesaId);
            } catch (Exception e) {
                log.warn("Falha ao enviar notificação de defesa agendada (defesaId={}) - {}", defesaId, e.toString());
            }
        });
    }


    public void shutdown() {
        pool.shutdown();
    }
}

