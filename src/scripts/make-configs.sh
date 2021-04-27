#! /usr/bin/env bash

usage() {
  cat <<EOF

$0 [options]

Generate configurations for local development.

Options
     --debug               Enable debugging
 -h, --help                Print this help and exit.
     --secrets-conf <file> The configuration file with secrets!

$1
EOF
  exit 1
}
main() {
  REPO=$(cd $(dirname $0)/../.. && pwd)
  SECRETS="$REPO/secrets.conf"
  PROFILE=dev
  MARKER=$(date +%s)
  ARGS=$(getopt -n $(basename ${0}) \
      -l "debug,help,secrets-conf:" \
    -o "h" -- "$@")
  [ $? != 0 ] && usage
  eval set -- "$ARGS"
  while true
  do
    case "$1" in
      --debug) set -x ;;
      -h|--help) usage "halp! what this do?" ;;
      --secrets-conf) SECRETS="$2" ;;
      --) shift;break ;;
    esac
    shift;
  done

  # Use SECRETS if secret values are added.

  doMakeConfigs
}

# ==================================================

checkForUnsetValues() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  echo "checking $target"
  grep -E '(.*= *unset)' "$target"
  [ $? == 0 ] && echo "Failed to populate all unset values" && exit 1
  diff -q $target $target.$MARKER
  [ $? == 0 ] && rm -v $target.$MARKER
}

configValue() {
  local project="$1"
  local profile="$2"
  local key="$3"
  local value="$4"
  local target="$REPO/$project/config/application-${profile}.properties"
  local escapedValue=$(echo $value | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')
  sed -i "s/^$key=.*/$key=$escapedValue/" $target
}

doMakeConfigs() {
  makeConfig mock-mpi $PROFILE
  configValue mock-mpi $PROFILE patient.vista-site.details "config\/$PROFILE-vista-sites.json"

  checkForUnsetValues mock-mpi $PROFILE

  makeVistaSitesJson
}

makeConfig() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  [ -f "$target" ] && mv -v $target $target.$MARKER
  grep -E '(.*= *unset)' "$REPO/$project/src/main/resources/application.properties" \
    > "$target"
}

makeVistaSitesJson() {
  local target="$REPO/mock-mpi/config/$PROFILE-vista-sites.json"
  [ -f "$target" ] && mv -v $target $target.$MARKER
  cat > ${target} <<EOF
{
  "vistaSiteDetails": {
    "default": {
      "sites": [
        "673"
      ]
    },
    "1011537977V693883": {
      "sites": [ "673" ]
    },
    "1010101010V666666": {
      "sites": []
    }
  }
}
EOF
}

# ==================================================

main $@
