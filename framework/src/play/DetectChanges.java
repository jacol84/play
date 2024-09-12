package play;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicBoolean;
import play.utils.DetectChangeInPath;
import play.vfs.VirtualFile;

public class DetectChanges implements Runnable {
    /**
     * detection changes in directo
     */
    private AtomicBoolean mustRunDetected = new AtomicBoolean(true);

    private static Long index = 0L;
    private boolean state = false;
    private String name;
    private List<VirtualFile> directories = new ArrayList<>();
    private List<DetectChangeInPath> detectChangeInPaths = new ArrayList<>();

    @Override
    public void run() {
        while (true) {
            if (!Thread.currentThread().getName().equals(name)) {
                return;
            }
            isDetection();
            sleep();
        }
    }

    public boolean shouldStartDetection() {
        return mustRunDetected.get();
    }

    public void reStart() {
        mustRunDetected.set(false);
    }

    public boolean isDetection() {
        if (!mustRunDetected.get() && detectChangeInPaths.stream().anyMatch(DetectChangeInPath::detect)) {
            mustRunDetected.set(true);
        }
        return mustRunDetected.get();
    }

    public void start() {
        if (Play.mode == Play.Mode.DEV) {
            init();
            name = "play-detect-" + getNextGen();
            new Thread(this, name).start();
        }
    }

    public void add(VirtualFile app) {
        directories.add(app);
        mustRunDetected.set(true);
    }

    public void clean() {
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
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            Logger.error(e, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private static Long getNextGen() {
        return index++;
    }

}
