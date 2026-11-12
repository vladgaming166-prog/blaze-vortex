package io.github.blazevortex.content;

import io.github.blazevortex.platform.BvxScheduler;
import io.github.blazevortex.platform.ScheduledHandle;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Debounced directory watcher. Callbacks run on the async scheduler, never on the watch thread
 * doing blocking image work without going through the engine.
 */
public final class ContentWatcher implements AutoCloseable {

    private final Path root;
    private final Logger logger;
    private final BvxScheduler scheduler;
    private final Consumer<Path> onChange;
    private final AtomicBoolean running = new AtomicBoolean();
    private final Set<Path> pending = ConcurrentHashMap.newKeySet();
    private WatchService watchService;
    private Thread thread;
    private ScheduledHandle debounce;

    public ContentWatcher(Path root, Logger logger, BvxScheduler scheduler, Consumer<Path> onChange) {
        this.root = root;
        this.logger = logger;
        this.scheduler = scheduler;
        this.onChange = onChange;
    }

    public void start() throws IOException {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        watchService = FileSystems.getDefault().newWatchService();
        registerAll(root);
        thread = new Thread(this::loop, "bvx-content-watcher");
        thread.setDaemon(true);
        thread.start();
    }

    private void registerAll(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void loop() {
        while (running.get()) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                if (running.get()) {
                    logger.log(Level.WARNING, "Content watcher stopped: " + ex.getMessage(), ex);
                }
                return;
            }
            Path dir = (Path) key.watchable();
            for (WatchEvent<?> event : key.pollEvents()) {
                Path relative = (Path) event.context();
                Path resolved = dir.resolve(relative);
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(resolved)) {
                    try {
                        registerAll(resolved);
                    } catch (IOException ex) {
                        logger.warning("Could not watch new directory " + resolved + ": " + ex.getMessage());
                    }
                }
                pending.add(resolved);
            }
            key.reset();
            scheduleFlush();
        }
    }

    private synchronized void scheduleFlush() {
        if (debounce != null) {
            debounce.cancel();
        }
        debounce = scheduler.runAsyncLater(() -> {
            Set<Path> batch = Set.copyOf(pending);
            pending.clear();
            debounce = null;
            for (Path path : batch) {
                try {
                    onChange.accept(path);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "Failed to handle content change " + path + ": " + ex.getMessage(), ex);
                }
            }
        }, 10);
    }

    @Override
    public void close() {
        running.set(false);
        if (debounce != null) {
            debounce.cancel();
        }
        if (thread != null) {
            thread.interrupt();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
            }
        }
    }
}
