## Implementing Custom Logic via a plugin for core functionality in Spinnaker
AWS Lambda plugin functionality has moved to Spinnaker OSS starting 1.32.x release. See https://github.com/spinnaker/orca/pull/4449

This fork demonstrates how to implement custom logic via a plugin for core functionality in Spinnaker ie Lambda functionality.

### Clouddriver (version 5.85.2 onwards)
For the clouddriver part of the plugin we are extending LambdaAgentProvider which is the Primary Bean of the Lambda caching agents. 
Any custom logic or modifications can be done in the Custom classes as done in [LambdaCustomService](./lambda-deployment-clouddriver/src/main/java/com/amazon/aws/spinnaker/plugin/lambda/LambdaCustomService.java#L57:L60).

### Orca (version 8.36.2 onwards)
For Orca stages is important that any change in the Stages wont break the existing stages defined in the end-users pipelines.
To achieve that we will rely on [dynamic-stage-resolver](https://github.com/spinnaker/orca/blob/master/orca-core/src/main/java/com/netflix/spinnaker/orca/DynamicStageResolver.kt) 
which makes migrating stages originally written directly into Orca to a plugin model easier.

Lets take the example that we want to migrate the existing LambdaDeploymentStage to a new stage defined in this plugin.

1. The stage type and aliases must be identical in both the existing stage and the plugin stage
2. Implementing the Orca part of the plugin we need to define the bean dependency in order to make sure that the `dynamicStageResolver` Bean 
is dependent on the plugin loaded stages. This is done [here](./lambda-deployment-orca/src/main/java/com/amazon/aws/spinnaker/plugin/lambda/LambdaSpringLoaderPlugin.java#L55:L64)
3. We implement the new stage definition in the plugin.
4. In order to override the stageDefinition from the plugin we add in the `orca-profile.yml` the following configuration:
```yaml
dynamic-stage-resolver:
  enabled: true
  Aws.LambdaDeploymentStage: com.amazon.aws.spinnaker.plugin.lambda.stages.LambdaDeploymentStage #Stage alias
  lambdaDeployment: com.amazon.aws.spinnaker.plugin.lambda.stages.LambdaDeploymentStage #Stage Type
```
5. We enable the plugin in Orca profile as normally done for the plugin framework.


### Deck - Migrating OSS Lambda stages to Plugin Components
In order to migrate the existing Lambda stages to the implementation of the plugin while keeping a backward compatibility:

1. Implement the new Deck stage in the plugin with the type matching the Orca definition. For example for the Orca code:
```java
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
```
- The corresponding Type is `lambdaDeployment` (matching the class name - starting with lowercase & excluding the Stage postfix)
- The corresponding Alias is `Aws.LambdaDeploymentStage` (As defined in the `@StageDefinitionBuilder.Aliases` annotation)

For Deck we will define the new stage in the plugin as follows:
```javascript
export const lambdaDeploymentStage: IStageTypeConfig = {
    key: 'lambdaDeployment', //Stage Type
    alias: 'Aws.LambdaDeploymentStage', //The previous stage type or alias to maintain backward compatibility
    label: `AWS Lambda Deployment`,
    description: 'Create a Single AWS Lambda Function',
    component: LambdaDeploymentConfig, // stage config
    executionDetailsSections: [LambdaDeploymentExecutionDetails, ExecutionDetailsTasks],
    validateFn: validate,
};
```

2. In order to hide from the end user the previous implementation of the OSS for the Stage we need to add the following configuration in the `settings-local.js` of the Deck service:
```yaml
settings-local.js: |
  ...
  window.spinnakerSettings.hiddenStages= ["Aws.LambdaDeploymentStage"];
  ...
```
The above configuration will hide any stages defined as `type="Aws.LambdaDeploymentStage"` from the UI. However since our new stage 
definition in the plugin has the alias `Aws.LambdaDeploymentStage` the stage will be visible in the UI with the new implementation of the plugin.

Any new pipeline created after the migration to the new implementation using the plugin will use the new stage type=`lambdaDeployment` and the old pipelines will still use the old stage `type="lambdaDeployment"`.

---

*Original README.md content below*

---

# Bellow section is deprecated since Lambda plugin has moved to OSS


## Spinnaker Plugin for AWS Lambda Deployment 

This plugin provides support for AWS Lambda Deployment via Pipelines in Spinnaker.  This repository is in transition
from its current distribution as a plugin into to the core Spinnaker project. Updates will be less frequent until
the migration is complete and feature parity is achieved in future Spinnaker releases.

### Version Compatibility
| Plugin  | Spinnaker Platform |
|:------------- |:-------------------|
| 1.0.11 >= | 1.29.x             |
| 1.2.0 >= | 1.30.x             |
| 1.2.0 <= | 1.32.x             |

This plugin is currently only compatible with Spinnaker platform 1.28.x and up.

## Major changes

- 11/02/2022 - Release 1.1.0 - removes older versions of the runtime engine from the UI.  This means editing older pipelines will no longer let you use the unsupported lambda runtimes.  Please see https://docs.aws.amazon.com/lambda/latest/dg/lambda-runtimes.html for questions/information.
- 09/21/2023 - Release 1.2.0 - Adds the ability to override clouddriver native functionality and supports clouddriver plugin.  Removed references to unsupported spinnaker versions.

### Requirements
1. This plugin requires Java 11
2. AWS Lambda functions must be enabled in your spinnaker environment and for all required AWS accounts. Find more information [here](https://aws.amazon.com/blogs/opensource/how-to-integrate-aws-lambda-with-spinnaker/) or within [samples](samples/README.md).
 
### Plugin Deployment Guide

1. Add the following to the Halyard config (typically found at `~/.hal/config`) to load the Orca backend
```yaml
  spinnaker:
    extensibility:
      plugins:
        Aws.LambdaDeploymentPlugin:
          enabled: true
          version: <<VERSION_NUMBER>> 
          extensions:
            Aws.LambdaDeploymentStage:
              enabled: true
      repositories:
        awsLambdaDeploymentPluginRepo:
          id: awsLambdaDeploymentPluginRepo
          url: https://raw.githubusercontent.com/spinnaker-plugins/aws-lambda-deployment-plugin-spinnaker/master/plugins.json

  # you can also optionally configure cache refresh retries and timeouts.  Several settings are for 
  # overall service communication timeouts should be set globally.   
  # https://github.com/spinnaker/kork/blob/master/kork-web/src/main/groovy/com/netflix/spinnaker/okhttp/OkHttpClientConfigurationProperties.groovy#L29-L32
  lambdaPluginConfig:
    cacheRefreshRetryWaitTime: 15 # defaults to 15 sec
    cacheOnDemandRetryWaitTime: 15 # defaults to 15 sec
    cloudDriverPostRequestRetries: 5 # defaults to 5.  Disable if you don't want duplicates.
    cloudDriverRetrieveNewPublishedLambdaWaitSeconds: 40 # defaults to 40 sec
    cloudDriverRetrieveMaxValidateWeightsTimeSeconds: 240 # defaults to 240 sec
```
2. Add the following to `gate-local.yml` in the necessary [profile](https://spinnaker.io/reference/halyard/custom/#custom-profiles) to load the Deck frontend
```yaml
spinnaker:
 extensibility:
    deck-proxy:
      enabled: true
      plugins:
        Aws.LambdaDeploymentPlugin:
          enabled: true
          version: <<VERSION NUMBER>>
    repositories:
      awsLambdaDeploymentPluginRepo:
        url: https://raw.githubusercontent.com/spinnaker-plugins/aws-lambda-deployment-plugin-spinnaker/master/plugins.json
```
3. Execute `hal deploy apply` to deploy the changes.
4. You should now be able to see 3 new stages provided by this plugin in the Deck UI when adding a new stage to your pipeline.

### Plugin User Guide

See the plugin user guide [here](UserGuide.md)

### Plugin TroubleShooting Guide

See the plugin user guide for troubleshooting instructions [here](UserGuide.md)

### Plugin Developer Guide

See the plugin developers guide [here](DeveloperGuide.md)

### Releasing New Versions

* Releases are done from the master branch
* Releases uses github actions. Scripts required for this are checked into the .github directory
* First update the version in gradle.properties and get that change merged to master.
* Then, to create a release, we tag the master branch commit with a release number (e.g. release-1.2.3) and push this tag

```
git tag 1.2.3
git push --tag
```

* The scripts in the .github directory trigger a build when this tag is pushed
* Once the build is successful, A new branch is created (called release-1.2.3) off this tag.
* A new commit is added to this branch that updates the plugin.json with artifacts produced by this build
* A PR is created for merging this commit to master. Merge this PR to master. 
* Navigate to the releases page [Releases](https://github.com/spinnaker-plugins/aws-lambda-deployment-plugin-spinnaker/releases) to make sure the new release shows up.
* Use the updated plugin.json in any new spinnaker deploys.

### Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

### License

This project is licensed under the Apache-2.0 License.




