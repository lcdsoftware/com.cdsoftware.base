#!/usr/bin/env sh

set -eu

if [ "$#" -ne 2 ]; then
	echo 'Usage: validate-artifacts.sh ARTIFACT_DIR BUILD_QUALIFIER' >&2
	exit 1
fi

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPOSITORY_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
CONFIG_FILE="$REPOSITORY_DIR/plugin-ci.properties"

property() {
	sed -n "s/^$1=//p" "$CONFIG_FILE" | tail -n 1
}

BUNDLE_ID=$(property 'bundle.id')
TEST_BUNDLE_ID=$(property 'test.bundle.id')
IDEMPIERE_VERSION=$(property 'idempiere.version')

java "$SCRIPT_DIR/BuildArtifactValidator.java" \
	"$1" \
	"$REPOSITORY_DIR/$BUNDLE_ID" \
	"$BUNDLE_ID" \
	"$REPOSITORY_DIR/$TEST_BUNDLE_ID" \
	"$TEST_BUNDLE_ID" \
	"$IDEMPIERE_VERSION.$2"
