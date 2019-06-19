#!/usr/bin/env bash

set -e

if [ -z "$1" ]
  then
    echo "Argument stage is required"
    exit 1
fi

STAGE=$1

echo "Build maven project..."

mvn clean

mvn compile

mvn package

sam package --template-file template.yaml --s3-bucket deployment-us-east-1-lambdas-1 --output-template-file ${STAGE}-packaged.yaml

sam deploy --template-file ./${STAGE}-packaged.yaml --stack-name ${STAGE}-sengage-webservice --capabilities CAPABILITY_IAM --parameter-overrides DeploymentStage=${STAGE}
