package io.github.zhaozhou1489.tools_task.enums;

public enum TaskStatusEnum {
    RUNNING(0,"进行中"),
    FINISHED(1,"已完成"),
    FAILED(2,"已失败")
    ;

    private int status;

    private String  name;

    public int getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    TaskStatusEnum(int status, String name) {
        this.status = status;
        this.name = name;
    }
}
