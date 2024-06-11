// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  ExecutionDetailsTasks,
  HelpContentsRegistry,
  IStageTypeConfig,
  overrideRegistrationQueue,
} from '@spinnaker/core';
import { overridesComponent } from '@spinnaker/core/src/overrideRegistry/Overrides';

import { LambdaDeploymentConfig, validate } from './LambdaDeploymentStageConfig';
import { LambdaDeploymentExecutionDetails } from './LambdaDeploymentStageExecutionDetails';

export const initialize = () => {
  HelpContentsRegistry.register('aws.lambdaDeploymentStage.lambda', 'Lambda Name');
};

export const lambdaDeploymentStage: IStageTypeConfig = {
  key: 'lambdaDeployment', //Stage Type
  alias: 'Aws.LambdaDeploymentStage', //The previous stage type for backwards compatibility
  label: `AWS Lambda Deployment`,
  description: 'Create a Single AWS Lambda Function',
  component: LambdaDeploymentConfig, // stage config
  executionDetailsSections: [LambdaDeploymentExecutionDetails, ExecutionDetailsTasks],
  validateFn: validate,
};
