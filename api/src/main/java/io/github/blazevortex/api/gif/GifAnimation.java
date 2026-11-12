package io.github.blazevortex.api.gif;

import java.nio.file.Path;
import java.util.List;

public interface GifAnimation {

    String name();

    Path file();

    int width();

    int height();

    int frameCount();

    List<Integer> frameDelaysMs();

    int loopCount();

    boolean hasTransparency();

    double defaultFps();
}
