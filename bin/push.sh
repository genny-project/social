#!/bin/bash

if [ -z "${1}" ]; then
   version="latest"
else
   version="${1}"
fi


docker push gennyproject/social:"${version}"
docker tag  gennyproject/social:"${version}"  gennyproject/social:latest
docker push gennyproject/social:latest

