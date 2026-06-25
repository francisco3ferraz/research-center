#!/bin/bash
set -euo pipefail

APP_DIR="/opt/${project_name}"
REPOSITORY_URL="${repository_url}"
REPOSITORY_REF="${repository_ref}"
COMPOSE_VERSION="${docker_compose_version}"
BUILDX_VERSION="${docker_buildx_version}"

dnf update -y
dnf install -y docker git

systemctl enable --now docker
usermod -aG docker ec2-user || true

mkdir -p /usr/local/lib/docker/cli-plugins
curl -fsSL "https://github.com/docker/compose/releases/download/$${COMPOSE_VERSION}/docker-compose-linux-x86_64" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
curl -fsSL "https://github.com/docker/buildx/releases/download/$${BUILDX_VERSION}/buildx-$${BUILDX_VERSION}.linux-amd64" \
  -o /usr/local/lib/docker/cli-plugins/docker-buildx
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose /usr/local/lib/docker/cli-plugins/docker-buildx
ln -sf /usr/local/lib/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose

rm -rf "$${APP_DIR}"
git clone --depth 1 --branch "$${REPOSITORY_REF}" "$${REPOSITORY_URL}" "$${APP_DIR}"
cd "$${APP_DIR}"

cat >.env <<'EOF'
APPLICATION_NAME=${project_name}
DATASOURCE_JNDI=${datasource_jndi}
DATASOURCE_NAME=${datasource_name}
DB_USER=${db_username}
DB_PASS=${db_password}
DB_HOST=${db_host}
DB_PORT=${db_port}
DB_NAME=${db_name}
POSTGRES_DRIVER_VERSION=${postgres_driver_version}
WILDFLY_ADMIN_PASSWORD=${wildfly_admin_password}
EOF
chmod 0600 .env

cat >docker-compose.aws.yaml <<'EOF'
services:
  webserver:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DATASOURCE_JNDI
      - DATASOURCE_NAME
      - DB_USER
      - DB_PASS
      - DB_HOST
      - DB_PORT
      - DB_NAME
      - WILDFLY_ADMIN_PASSWORD
      - POSTGRES_DRIVER_VERSION
    restart: unless-stopped
EOF

docker run --rm \
  -v "$${APP_DIR}:/workspace" \
  -w /workspace \
  eclipse-temurin:17-jdk \
  bash mvnw clean package

docker compose -f docker-compose.aws.yaml up -d --build

until docker compose -f docker-compose.aws.yaml ps webserver --status running >/dev/null 2>&1; do
  sleep 3
done

docker compose -f docker-compose.aws.yaml cp "target/${project_name}.war" "webserver:/opt/jboss/wildfly/standalone/deployments/${project_name}.war"
