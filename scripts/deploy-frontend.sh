#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLIENT_DIR="$ROOT_DIR/research-center-client"

FRONTEND_BUCKET="$(terraform -chdir="$ROOT_DIR/terraform" output -raw frontend_static_bucket_name)"
CLOUDFRONT_DISTRIBUTION_ID="$(terraform -chdir="$ROOT_DIR/terraform" output -raw frontend_cloudfront_distribution_id)"
CLOUDFRONT_DOMAIN="$(terraform -chdir="$ROOT_DIR/terraform" output -raw frontend_cloudfront_domain_name)"

export NUXT_PUBLIC_API_BASE="${NUXT_PUBLIC_API_BASE:-/research-center/api}"

cd "$CLIENT_DIR"
npm ci
npm run generate

aws s3 sync .output/public "s3://${FRONTEND_BUCKET}/" \
  --delete \
  --cache-control "public,max-age=300"

aws cloudfront create-invalidation \
  --distribution-id "$CLOUDFRONT_DISTRIBUTION_ID" \
  --paths "/*" >/dev/null

printf 'Frontend deployed: https://%s\n' "$CLOUDFRONT_DOMAIN"
printf 'API base: %s\n' "$NUXT_PUBLIC_API_BASE"
