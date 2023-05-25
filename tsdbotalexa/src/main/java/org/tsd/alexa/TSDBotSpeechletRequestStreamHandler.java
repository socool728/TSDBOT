package org.tsd.alexa;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

public class TSDBotSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    public TSDBotSpeechletRequestStreamHandler() {
        this(new TSDBotSpeechlet(), new HashSet<>());
    }

    public TSDBotSpeechletRequestStreamHandler(SpeechletV2 speechlet, Set<String> supportedApplicationIds) {
        super(speechlet, supportedApplicationIds);
    }

    public TSDBotSpeechletRequestStreamHandler(Speechlet speechlet, Set<String> supportedApplicationIds) {
        super(speechlet, supportedApplicationIds);
    }
}
