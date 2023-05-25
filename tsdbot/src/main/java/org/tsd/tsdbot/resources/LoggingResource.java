package org.tsd.tsdbot.resources;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.logging.FileAppenderFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.tsdbot.app.config.TSDBotConfiguration;
import org.tsd.tsdbot.auth.User;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Optional;

@Path("/logs")
public class LoggingResource {

    private static final Logger log = LoggerFactory.getLogger(LoggingResource.class);

    private final TSDBotConfiguration configuration;

    @Inject
    public LoggingResource(TSDBotConfiguration configuration) {
        this.configuration = configuration;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({"staff"})
    public String getLogs(@Auth User user) {
        log.debug("User accessed logs: {}", user.getUsername());
        try {
            log.info("LoggingFactory: {}", configuration.getLoggingFactory());
            if (configuration.getLoggingFactory() instanceof DefaultLoggingFactory) {
                DefaultLoggingFactory defaultLoggingFactory = (DefaultLoggingFactory) configuration.getLoggingFactory();

                Optional<AppenderFactory<ILoggingEvent>> fileAppender = defaultLoggingFactory.getAppenders()
                        .stream()
                        .peek(appender -> log.info("Evaluating logging appender: {}", appender))
                        .filter(appender -> appender instanceof FileAppenderFactory)
                        .findAny();

                if (fileAppender.isPresent()) {
                    FileAppenderFactory fileAppenderFactory = (FileAppenderFactory) fileAppender.get();
                    String logFilePath = fileAppenderFactory.getCurrentLogFilename();
                    log.info("Found file appender, file: {}", logFilePath);
                    File logFile = new File(logFilePath);

                    if (!logFile.exists()) {
                        throw new FileNotFoundException("Could not find log file: "+logFile);
                    }

                    if (!logFile.canRead()) {
                        throw new FileNotFoundException("Cannot read log file: "+logFile);
                    }

                    return FileUtils.readFileToString(logFile, Charset.forName("UTF-8"));
                } else {
                    log.warn("No file logging appenders in configuration");
                }
            }
        } catch (Exception e) {
            log.error("Error reading log file", e);
        }

        return "Failed to get logs";
    }

}
