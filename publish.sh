#!/bin/sh

if [[ $# -eq 0 ]] ; then
    echo 'Must pass version: ./publish.sh 0.0.1'
    exit 0
fi

./gradlew updateVersion -PnewVersion=$1 && \
./gradlew publishMavenJavaPublicationToMavenRepository && \
  git commit -a -m "Publish $1" && \
  git tag "v$1" && \
  git push && \
  git push --tags
