package com.amazon.aws.spinnaker.plugin.lambda.stages;

import com.amazon.aws.spinnaker.plugin.lambda.stages.tasks.LambdaCacheRefreshCustomTask;
import com.netflix.spinnaker.kork.plugins.api.spring.ExposeToApp;
import com.netflix.spinnaker.orca.api.pipeline.graph.StageDefinitionBuilder;
import com.netflix.spinnaker.orca.api.pipeline.graph.TaskNode;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
@ExposeToApp
@StageDefinitionBuilder.Aliases({"Aws.LambdaDeploymentStage"})
public class LambdaDeploymentStage implements StageDefinitionBuilder {
    private static final Logger logger = LoggerFactory.getLogger(LambdaDeploymentStage.class);


    @Override
    public void taskGraph(@NotNull StageExecution stage, @NotNull TaskNode.Builder builder) {
        final String stageType = stage.getType();
        logger.info("LambdaDeploymentCustomStage.taskGraph() stageType: {}", stageType);
        builder.withTask("lambdaCacheRefreshTask", LambdaCacheRefreshCustomTask.class);
    }
}