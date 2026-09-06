#!/bin/sh
set -u
# Clear generated evidence only, so previous runs cannot appear in the report.
rm -rf /app/build/allure-results /app/build/reports /app/build/test-results
sh /app/gradlew test --rerun-tasks --no-daemon --project-cache-dir /tmp/project-cache \
  "-Dremote_url=http://chrome:4444/wd/hub" \
  "-Dheadless=true" "-Dbrowser_size=${BROWSER_SIZE:-1440x1000}"
test_status=$?
# Produce diagnostic reports even when a test fails; preserve its exit status.
sh /app/gradlew allureReport --no-daemon --project-cache-dir /tmp/project-cache
report_status=$?
if [ "$test_status" -ne 0 ]; then exit "$test_status"; fi
exit "$report_status"
