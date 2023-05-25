package net.bramp.ffmpeg.job;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.progress.ProgressListener;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/** @author net.bramp */
public abstract class FFmpegJob implements Runnable {

  public enum State {
    WAITING   (false),
    RUNNING   (false),
    FINISHED  (true),
    FAILED    (true);

    private final boolean terminal;

    State(boolean terminal) {
      this.terminal = terminal;
    }

    public boolean isTerminal() {
      return terminal;
    }
  }

  final FFmpeg ffmpeg;
  final ProgressListener listener;

  State state = State.WAITING;

  public FFmpegJob(FFmpeg ffmpeg) {
    this(ffmpeg, null);
  }

  public FFmpegJob(FFmpeg ffmpeg, @Nullable ProgressListener listener) {
    this.ffmpeg = checkNotNull(ffmpeg);
    this.listener = listener;
  }

  public State getState() {
    return state;
  }

  public void stop() {
    if (this.ffmpeg != null) {
      ffmpeg.stop();
    }
  }
}
