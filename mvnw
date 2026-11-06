#!/bin/sh
if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
fi

if [ -x "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" ]; then
    export JAVA_HOME="/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home"
    exec "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" "$@"
fi

echo "Maven not found. Please ensure Maven or IntelliJ IDEA is installed."
exit 1
