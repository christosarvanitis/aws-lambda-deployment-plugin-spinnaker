/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.amazon.aws.spinnaker.plugin.lambda;

import com.netflix.spinnaker.kork.plugins.api.spring.SpringLoader;
import com.netflix.spinnaker.kork.plugins.api.spring.SpringLoaderPlugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.List;

public class LambdaSpringLoaderPlugin extends SpringLoaderPlugin {
    private static Logger logger = LoggerFactory.getLogger(LambdaSpringLoaderPlugin.class);
    public LambdaSpringLoaderPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static final String SPRING_LOADER_BEAN_NAME = String.format("Aws.LambdaDeploymentPlugin.%s", SpringLoader.class.getName());

    private static final List<String> ORCA_BEANS_DEPENDING_ON_PLUGIN = List.of(
            "dynamicStageResolver"
    );

    @Override
    public List<String> getPackagesToScan() {
        return List.of("com.amazon.aws.spinnaker.plugin.lambda");
    }

    public void start() {
        logger.info("Aws.LambdaDeploymentPlugin.start()");
    }

    public void stop() {
        logger.info("Aws.LambdaDeploymentPlugin.stop()");
    }

    @Override
    public void registerBeanDefinitions(BeanDefinitionRegistry registry) {
        super.registerBeanDefinitions(registry);

        ORCA_BEANS_DEPENDING_ON_PLUGIN.forEach(bean -> {
            if (registry.containsBeanDefinition(bean)) {
                registry
                        .getBeanDefinition(bean)
                        .setDependsOn(SPRING_LOADER_BEAN_NAME);
            }
        });
    }

}
