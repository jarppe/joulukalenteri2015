#!/usr/bin/env bash

env=$1

case "$1" in
  "" | "vag" | "vagrant")
    host=Vagrant
    i=vagrant
    ;;
  "prod")
    host=PROD
    i=prod
    ;;
  *)
    echo "rtfm!"
    exit 1
esac

echo "Build and deploy to $host..."
boot package && ( cd ansible && ansible-playbook deploy.yml -i "inventory/$i" )
