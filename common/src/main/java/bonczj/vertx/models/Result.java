package bonczj.vertx.models;

import java.util.UUID;

public class Result
{
    private UUID   id;
    private Status status;
    private String result;

    public Result()
    {
    }

    public Result(UUID id, Status status, String result)
    {
        this.id = id;
        this.status = status;
        this.result = result;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }
}
