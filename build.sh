VERSION=$(grep '^version=' gradle.properties | cut -d'=' -f2 | tr -d '[:space:]')
if [ -z "$VERSION" ]; then VERSION="1.0.0"; fi
docker build -t test:$VERSION .