set -e
cd ..
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t aneki-registry.registry.twcstorage.ru/prod/aneki-backend:latest \
  --push \
  .

cd ../aneki_frontend
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t aneki-registry.registry.twcstorage.ru/prod/aneki-frontend:latest \
  --push \
  .