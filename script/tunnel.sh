#!/usr/bin/env bash

env=$1

case "$1" in
  "prod")
    host=millan-joulukalenteri.fi
    desc=PROD
    ;;
  *)
    echo "rtfm!"
    exit 1
esac

echo "Tunnels to $desc ($host) are open"
ssh -nNT $host \
  -L 3001:localhost:3000 \
  -L 6001:localhost:6000
