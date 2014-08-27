package io.shenanigans.server;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.db.jpa.BasicLogEventEntity;

public class SubmissionLogRecord extends BasicLogEventEntity{

	public SubmissionLogRecord() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SubmissionLogRecord(LogEvent wrappedEvent) {
		super(wrappedEvent);
		// TODO Auto-generated constructor stub
	}

	
}
