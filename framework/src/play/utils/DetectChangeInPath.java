package play.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

import play.Logger;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class DetectChangeInPath {
    WatchService watchService;

    public DetectChangeInPath(Path path) {
        try {
            watchService = getWatchService();
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    System.out.println(dir.toAbsolutePath());
                    dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private WatchService getWatchService() throws IOException {
        return FileSystems.getDefault().newWatchService();
    }

    public boolean detect() {
        WatchKey key = watchService.poll();
        if (key == null) {
            return false;
        }
        List<WatchEvent<?>> watchEvents = key.pollEvents();
        logger(watchEvents);
        boolean result = !watchEvents.isEmpty();
        key.reset();
        return result;
    }

    private void logger(List<WatchEvent<?>> watchEvents) {
        if (Logger.isDebugEnabled()) {
            for (WatchEvent<?> event : watchEvents) {
//                System.out.println("detect --> Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
                Logger.debug("detect --> Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
            }
        }
    }

    public void close() {
        try {
            watchService.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}