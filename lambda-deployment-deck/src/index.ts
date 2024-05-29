// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import { IDeckPlugin } from '@spinnaker/core';

import { lambdaDeleteStage } from './deleteLambda';
import { initialize, lambdaDeploymentStage } from './deployLambda';
import { lambdaInvokeStage } from './invokeLambda';
import { lambdaRouteStage } from './routeLambda';
import { lambdaUpdateCodeStage } from './updateCodeLambda';

export const plugin: IDeckPlugin = {
  initialize,
  stages: [lambdaDeploymentStage],
};
