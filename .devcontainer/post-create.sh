#!/usr/bin/env bash
set -euo pipefail

npm install --global --prefix "$HOME/.local" @github/copilot

java -version
mvn --version
node --version
gh --version
copilot --version
