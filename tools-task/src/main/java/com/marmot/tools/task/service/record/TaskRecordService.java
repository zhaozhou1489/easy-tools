package com.marmot.tools.task.service.record;

import com.marmot.tools.task.entity.TaskRecord;

public interface TaskRecordService {

    public boolean taskIsRunning(String taskName);

    public boolean recordStartTask(String taskName);

    public boolean recordEndTask(String taskName);
}
