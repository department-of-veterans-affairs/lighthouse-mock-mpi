#!/usr/bin/env bash

ENDPOINT_DOMAIN_NAME="$K8S_LOAD_BALANCER"
ENVIRONMENT="$K8S_ENVIRONMENT"
BASE_PATH="$BASE_PATH"
SSN="$SSN"

#Put Health endpoints here if you got them, all that's here is a WAG
PATHS=("/actuator/health" "/actuator/info")

SUCCESS=0

FAILURE=0

# New phone who this?
usage() {
cat <<EOF
Commands
  smoke-test [--endpoint-domain-name|-d <endpoint>] [--environment|-e <env>] [--base-path|-b <base_path>] [--ssn|-s <ssn>]
  regression-test [--endpoint-domain-name|-d <endpoint>] [--environment|-e <env>] [--base-path|-b <base_path>] [--ssn|-s <ssn>]

Example
  smoke-test
    --endpoint-domain-name=localhost
    --environment=qa
    --base-path=mock-mpi/v0
    --ssn=796130115

$1
EOF
exit 1
}

# Keeps track of successes and failures
trackStatus () {
  if [ "$status_code" == 200 -o "$status_code" == 201 ]
    then
      SUCCESS=$((SUCCESS + 1))
      echo "$request_url: $status_code - Success"
    else
      FAILURE=$((FAILURE + 1))
      echo "$request_url: $status_code - Fail"
  fi
}

# Make 1305 request.
httpListenerTests () {

  if [[ ! "$ENDPOINT_DOMAIN_NAME" == http* ]]; then
      ENDPOINT_DOMAIN_NAME="https://$ENDPOINT_DOMAIN_NAME"
  fi

  for path in "${PATHS[@]}"
    do
      request_url="$ENDPOINT_DOMAIN_NAME$BASE_PATH$path"
      status_code=$(curl -k --write-out %{http_code} --silent --output /dev/null "$request_url")
      trackStatus
    done

  path="/psim_webservice/IdMWebService/"
  request_url="$ENDPOINT_DOMAIN_NAME$BASE_PATH$path"
  status_code=$(curl -X POST -k --write-out %{http_code} --silent --output /dev/null "$request_url" -H 'Content-Type: text/xml' -d '
  <env:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:env="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <env:Header/>
    <env:Body>
      <idm:PRPA_IN201305UV02 xmlns:idm="http://vaww.oed.oit.va.gov" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:hl7-org:v3" xsi:schemaLocation="urn:hl7-org:v3 ../../schema/HL7V3/NE2008/multicacheschemas/PRPA_IN201305UV02.xsd" ITSVersion="XML_1.0">
        <id root="1.2.840.114350.1.13.0.1.7.1.1" extension="200VGOV-24ec50c5-1927-43f2-958b-1b7805b5e61c"/>
        <creationTime value="20201209201923"/>
        <versionCode code="4.1"/>
        <interactionId root="2.16.840.1.113883.1.6" extension="PRPA_IN201305UV02"/>
        <processingCode code="T"/>
        <processingModeCode code="T"/>
        <acceptAckCode code="AL"/>
        <receiver typeCode="RCV">
          <device classCode="DEV" determinerCode="INSTANCE">
            <id root="1.2.840.114350.1.13.999.234" extension="200M"/>
          </device>
        </receiver>
        <sender typeCode="SND">
          <device classCode="DEV" determinerCode="INSTANCE">
            <id root="2.16.840.1.113883.4.349" extension="200VGOV"/>
          </device>
        </sender>
        <controlActProcess classCode="CACT" moodCode="EVN">
          <code code="PRPA_TE201305UV02" codeSystem="2.16.840.1.113883.1.6"/>
          <dataEnterer typeCode="ENT" contextControlCode="AP">
            <assignedPerson classCode="ASSIGNED">
              <id extension="796130115" root="2.16.840.1.113883.777.999"/>
              <assignedPerson classCode="PSN" determinerCode="INSTANCE">
                <name>
                  <given>Tamara</given>
                  <given>E</given>
                  <family>Ellis</family>
                </name>
              </assignedPerson>
            </assignedPerson>
          </dataEnterer>
          <queryByParameter>
            <queryId root="1.2.840.114350.1.13.28.1.18.5.999" extension="18204"/>
            <statusCode code="new"/>
            <modifyCode code="MVI.COMP2"/>
            <initialQuantity value="1"/>
            <parameterList>
              <livingSubjectAdministrativeGender>
                <value code="F"/>
                <semanticsText>Gender</semanticsText>
              </livingSubjectAdministrativeGender>
              <livingSubjectBirthTime>
                <value value="19670619"/>
                <semanticsText>Date of Birth</semanticsText>
              </livingSubjectBirthTime>
              <livingSubjectId>
                <value root="2.16.840.1.113883.4.1" extension="'$SSN'"/>
                <semanticsText>SSN</semanticsText>
              </livingSubjectId>
              <livingSubjectName>
                <value use="L">
                  <given>Tamara</given>
                  <given>E</given>
                  <family>Ellis</family>
                </value>
                <semanticsText>Legal Name</semanticsText>
              </livingSubjectName>
            </parameterList>
          </queryByParameter>
        </controlActProcess>
      </idm:PRPA_IN201305UV02>
    </env:Body>
  </env:Envelope>')
  trackStatus
}

printResults () {
  TOTAL=$((SUCCESS + FAILURE))

  echo "=== TOTAL: $TOTAL | SUCCESS: $SUCCESS | FAILURE: $FAILURE ==="

  if [[ "$FAILURE" -gt 0 ]]; then
  exit 1
  fi
}

# Send some smoke signals
smokeTest () {
  httpListenerTests
  printResults
}

# Regress
regressionTest () {
  httpListenerTests
  printResults
}

# Let's get down to business
ARGS=$(getopt -n $(basename ${0}) \
    -l "endpoint-domain-name:,environment:,base-path:,ssn:,help" \
    -o "d:e:b:u:s:h" -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    -d|--endpoint-domain-name) ENDPOINT_DOMAIN_NAME=$2;;
    -e|--environment) ENVIRONMENT=$2;;
    -b|--base-path) BASE_PATH=$2;;
    -s|--ssn) SSN=$2;;
    -h|--help) usage "I need a hero! I'm holding out for a hero...";;
    --) shift;break;;
  esac
  shift;
done

if [ -z "$ENDPOINT_DOMAIN_NAME" ]; then
  usage "Missing variable K8S_LOAD_BALANCER or option --endpoint-domain-name|-d."
fi

if [ -z "$ENVIRONMENT" ]; then
  usage "Missing variable K8S_ENVIRONMENT or option --environment|-e."
fi

if [ -z "$SSN" ]; then
  usage "Missing variable SSN or option --ssn|-s."
fi

[ $# == 0 ] && usage "No command specified"
COMMAND=$1
shift

case "$COMMAND" in
  s|smoke-test) smokeTest;;
  r|regression-test) regressionTest;;
  *) usage "Unknown command: $COMMAND";;
esac

exit 0
