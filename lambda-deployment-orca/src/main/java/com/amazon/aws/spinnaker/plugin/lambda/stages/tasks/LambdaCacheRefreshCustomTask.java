package com.amazon.aws.spinnaker.plugin.lambda.stages.tasks;

import com.netflix.spinnaker.kork.plugins.api.spring.ExposeToApp;
import com.netflix.spinnaker.orca.api.pipeline.Task;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ExposeToApp
public class LambdaCacheRefreshCustomTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(LambdaCacheRefreshCustomTask.class);



    @NotNull
    @Override
    public TaskResult execute(@NotNull StageExecution stageExecution) {
        logger.info("----------------------------------------------------------");
        logger.info("LambdaCacheRefreshCustomTask.execute() stageType: {}", stageExecution.getType());
        logger.info("----------------------------------------------------------");
        return TaskResult.builder(ExecutionStatus.SUCCEEDED).build();
    }
}
