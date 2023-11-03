package io.github.zhaozhou1489.tools_task.service.record;

public interface TaskRecordService {

    public boolean taskIsRunning(String taskName);

    public boolean recordStartTask(String taskName);

    public boolean recordEndTask(String taskName);
}
