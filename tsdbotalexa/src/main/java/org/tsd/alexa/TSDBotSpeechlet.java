package org.tsd.alexa;

import com.amazon.speech.speechlet.*;

public class TSDBotSpeechlet implements Speechlet {
    @Override
    public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session) throws SpeechletException {
        System.out.println("onSessionStarted");
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session) throws SpeechletException {
        System.out.println("onLaunch");
        return null;
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest intentRequest, Session session) throws SpeechletException {
        return null;
    }

    @Override
    public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {
        System.out.println("onSessionEnded: "+sessionEndedRequest.getReason().name());
    }
}
