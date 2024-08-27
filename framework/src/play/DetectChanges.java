package play;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import play.utils.DetectChangeInPath;
import play.vfs.VirtualFile;

public class DetectChanges implements Runnable {

    private static Long index = 0L;
    private boolean state = false;
    private String name;
    private boolean started = false;
    private List<VirtualFile> directories = new ArrayList<>();
    private List<DetectChangeInPath> detectChangeInPaths = new ArrayList<>();

    @Override
    public void run() {
        while (started) {
            if (Thread.currentThread().getName().equals(name)) {
                return;
            }

            if (isPresent()) {
                Play.mustRunDetected.set(true);
            }
            sleep();
        }
    }

    public boolean isPresent() {
        boolean present = state || detectChangeInPaths.stream().map(DetectChangeInPath::detect).findFirst().isPresent();
        if (present) {
            state = true;
        }
        return present;
    }

    public void start() {
        if (Play.mode == Play.Mode.DEV && !started) {
            init();
            name = "play-detect-" + getNextGen();
            new Thread(this, name).start();
            started = true;
        }
    }

    public void add(VirtualFile app) {
        if (started) {
            throw new IllegalStateException("already started");
        }
        directories.add(app);
    }

    public void clean() {
        started = false;
        detectChangeInPaths.forEach(DetectChangeInPath::close);
        detectChangeInPaths = new ArrayList<>();
        directories = new ArrayList<>();
    }

    private void init() {
        detectChangeInPaths = directories.stream().filter(VirtualFile::isDirectory).map(x -> x.getRealFile().toPath()).map(DetectChangeInPath::new)
                                         .collect(Collectors.toList());
    }

    private void sleep() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            Logger.error(e, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private static Long getNextGen() {
        return index++;
    }

}
