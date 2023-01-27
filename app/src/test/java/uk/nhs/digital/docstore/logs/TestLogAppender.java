package uk.nhs.digital.docstore.logs;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

public class TestLogAppender extends AppenderBase<ILoggingEvent> {
  ArrayList<ILoggingEvent> loggingEvents = new ArrayList<>();

  public static TestLogAppender addTestLogAppender() {
    TestLogAppender testLogAppender = new TestLogAppender();
    ThresholdFilter filter = new ThresholdFilter();
    filter.setLevel("INFO");
    filter.start();
    testLogAppender.addFilter(filter);
    Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.addAppender(testLogAppender);

    testLogAppender.start();
    return testLogAppender;
  }

  @Override
  protected void append(ILoggingEvent eventObject) {
    loggingEvents.add(eventObject);
  }

  public ILoggingEvent getLastLoggedEvent() {
    if (loggingEvents.isEmpty()) return null;
    return loggingEvents.get(loggingEvents.size() - 1);
  }

  public ILoggingEvent findLoggedEvent(String subString) {
    for (ILoggingEvent event : loggingEvents) {
      System.out.println("logged event message: " + event.getMessage());
      if (event.getMessage().contains(subString)) {
        return event;
      }
    }
    return null;
  }
}
