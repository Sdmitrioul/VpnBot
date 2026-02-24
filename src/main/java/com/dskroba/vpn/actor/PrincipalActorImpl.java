package com.dskroba.vpn.actor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

import static com.dskroba.vpn.base.Utils.shutdownExecutorService;

public class PrincipalActorImpl implements PrincipalActor {
    private static final Logger log = LogManager.getLogger(PrincipalActorImpl.class);

    private final ExecutorService virtualExecutor;
    private final Future<?> processingTask;
    private final BlockingQueue<Job> mailbox = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public PrincipalActorImpl(Context context) {
        this.virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.processingTask = virtualExecutor
                .submit(() -> ScopedValue
                        .where(Context.PROVIDER, context)
                        .run(this::processMessages));
    }

    private void processMessages() {
        while (running) {
            try {
                Job job = mailbox.poll(30, TimeUnit.SECONDS);
                if (job != null) {
                    runJob(job);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void runJob(Job job) {
        try {
            job.run();
        } catch (JobException e) {
            log.error("Exception during job update", e);
        }
    }

    @Override
    public boolean submit(Job job) {
        if (!mailbox.offer(job)) {
            log.error("Unable to process the job: {}", job);
            return false;
        }
        return true;
    }

    @Override
    public void close() {
        running = false;
        mailbox.clear();
        processingTask.cancel(true);
        shutdownExecutorService(virtualExecutor);
    }
}
