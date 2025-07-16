#!/bin/bash

echo "Running tests sequentially..."

TEST_CLASSES=(
    "AuthControllerValidationTest"
    "CategoryControllerTest"
    "MemberControllerTest"
    "ProfileControllerTest"
    "StatisticsControllerTest"
    "TagControllerTest"
    "TodoControllerTest"
)

FAILED_TESTS=()
PASSED_TESTS=()

for test in "${TEST_CLASSES[@]}"
do
    echo "Running $test..."
    if ./gradlew test --tests "$test" --no-daemon -q; then
        PASSED_TESTS+=("$test")
        echo "✓ $test passed"
    else
        FAILED_TESTS+=("$test")
        echo "✗ $test failed"
    fi
done

echo ""
echo "Test Results Summary:"
echo "===================="
echo "Passed: ${#PASSED_TESTS[@]}"
echo "Failed: ${#FAILED_TESTS[@]}"

if [ ${#FAILED_TESTS[@]} -gt 0 ]; then
    echo ""
    echo "Failed tests:"
    for test in "${FAILED_TESTS[@]}"
    do
        echo "  - $test"
    done
fi