/*
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazon.aws.spinnaker.plugin.lambda.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.cats.agent.Agent;
import com.netflix.spinnaker.clouddriver.aws.security.AmazonClientProvider;
import com.netflix.spinnaker.clouddriver.aws.security.AmazonCredentials;
import com.netflix.spinnaker.clouddriver.aws.security.NetflixAmazonCredentials;
import com.netflix.spinnaker.clouddriver.core.limits.ServiceLimitConfiguration;
import com.netflix.spinnaker.clouddriver.lambda.provider.agent.LambdaAgentProvider;
import com.netflix.spinnaker.config.LambdaServiceConfig;
import com.netflix.spinnaker.credentials.Credentials;
import com.netflix.spinnaker.kork.plugins.api.spring.ExposeToApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Component
@ExposeToApp
@Primary
public class LambdaCustomAgentProvider extends LambdaAgentProvider {
  private final ObjectMapper objectMapper;
  private final AmazonClientProvider amazonClientProvider;
  private final LambdaServiceConfig lambdaServiceConfig;
  private final ServiceLimitConfiguration serviceLimitConfiguration;


  @Autowired
  public LambdaCustomAgentProvider(
          ObjectMapper objectMapper,
          AmazonClientProvider amazonClientProvider,
          LambdaServiceConfig lambdaServiceConfig,
          ServiceLimitConfiguration serviceLimitConfiguration) {
    super(amazonClientProvider, lambdaServiceConfig, serviceLimitConfiguration);
    this.objectMapper = objectMapper;
    this.amazonClientProvider = amazonClientProvider;
    this.lambdaServiceConfig = lambdaServiceConfig;
    this.serviceLimitConfiguration = serviceLimitConfiguration;
  }

  @Override
  public Collection<Agent> agents(Credentials credentials) {
    List<Agent> agents = new ArrayList<>();
    NetflixAmazonCredentials netflixAmazonCredentials = (NetflixAmazonCredentials) credentials;
    if (netflixAmazonCredentials.getLambdaEnabled()) {
      agents.add(
              new IamRoleCustomCachingAgent(objectMapper, netflixAmazonCredentials, amazonClientProvider));

      for (AmazonCredentials.AWSRegion region : netflixAmazonCredentials.getRegions()) {
        agents.add(
                new LambdaCustomCachingAgent(
                        objectMapper,
                        amazonClientProvider,
                        netflixAmazonCredentials,
                        region.getName(),
                        lambdaServiceConfig,
                        serviceLimitConfiguration));
      }
    }
    return agents;
  }
}
