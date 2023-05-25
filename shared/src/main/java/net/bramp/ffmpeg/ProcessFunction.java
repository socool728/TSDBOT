package net.bramp.ffmpeg;

import java.io.IOException;
import java.util.List;

/**
 * Runs a process returning a Reader to its stdout
 *
 * @author net.bramp
 */
public interface ProcessFunction {
  Process run(List<String> args) throws IOException;
}
