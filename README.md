# Research Center

Research Center is a full-stack publication management platform for academic research groups. It combines a Jakarta EE backend, a Nuxt frontend, local Docker orchestration, and Terraform-managed AWS infrastructure.

The project demonstrates a production-style deployment footprint: private database subnets, managed PostgreSQL, an EC2-hosted WildFly backend, and a static frontend distributed through S3 and CloudFront.

## Contents

- [Architecture](#architecture)
- [Repository Layout](#repository-layout)
- [Prerequisites](#prerequisites)
- [Local Development](#local-development)
- [AWS Deployment](#aws-deployment)
- [Frontend Deployment](#frontend-deployment)
- [Verification](#verification)
- [Operational Notes](#operational-notes)
- [Troubleshooting](#troubleshooting)

## Architecture

![AWS deployment architecture](docs/aws-architecture.svg)

### Backend

- Java 17 Jakarta EE application packaged as a WAR.
- WildFly application server.
- JAX-RS API mounted at `/research-center/api`.
- JPA/Hibernate persistence.
- PostgreSQL datasource bound at `java:/ResearchCenterDS`.
- JWT bearer authentication with role-based authorization.
- Seed data loaded at startup through `ConfigBean`.

### Frontend

- Nuxt 4 SPA in `research-center-client`.
- Tailwind CSS.
- Local API fallback: `http://localhost:8080/research-center/api`.
- Production API base: `/research-center/api`, routed through CloudFront.
- Static production hosting through private S3 and CloudFront.

### AWS Infrastructure

Terraform in `terraform/` provisions:

- VPC with public subnets and private database subnets.
- EC2 instance in a public subnet for the WildFly backend.
- RDS PostgreSQL in private subnets.
- Security groups that restrict PostgreSQL `5432` to the backend application server.
- Private S3 bucket for generated Nuxt frontend files.
- CloudFront distribution for frontend delivery and API path routing.
- Optional frontend ALB resources for instance-based web targets.

Request flow:

```text
Browser
  -> CloudFront
  -> S3 static frontend

Browser API request
  -> CloudFront /research-center/api/*
  -> EC2 WildFly backend on :8080
  -> RDS PostgreSQL on :5432
```

## Repository Layout

```text
.
├── src/main/java/...              # Jakarta EE backend source
├── src/main/resources/META-INF    # JPA/CDI configuration
├── research-center-client         # Nuxt frontend
├── terraform                      # AWS infrastructure as code
├── scripts                        # Deployment and helper scripts
├── docs                           # Architecture diagram and project documentation assets
├── Dockerfile                     # WildFly backend image
├── docker-compose.yaml            # Local service stack
├── Makefile                       # Local build/deploy commands
└── api-endpoint-spec_final.md     # REST API specification
```

## Prerequisites

Local development:

- Docker and Docker Compose.
- Java 17.
- Maven, or the included Maven wrapper through `bash mvnw`.
- Node.js and npm.

AWS deployment:

- Terraform 1.6 or newer.
- AWS credentials configured outside the repository.
- Permission to manage VPC, EC2, IAM, ALB, RDS, S3, and CloudFront resources.
- Secure values supplied through environment variables or a secrets workflow.

## Local Development

Create a local environment file:

```bash
cp .env.example .env
```

Review `.env` before starting services. Do not commit `.env`.

Start the local stack:

```bash
make up
```

Docker Compose starts:

- `webserver`: WildFly backend container.
- `db`: PostgreSQL.
- `smtp`: fake SMTP server for password recovery email flows.
- `ollama`: local LLM service used by AI summary features.

Build and deploy the backend WAR into the running WildFly container:

```bash
make deploy
```

Start the frontend:

```bash
cd research-center-client
npm install
npm run dev
```

Local URLs:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080/research-center/api
```

For non-CloudFront frontend environments, set the API base explicitly before build/start:

```bash
export NUXT_PUBLIC_API_BASE='http://BACKEND_PUBLIC_DNS:8080/research-center/api'
```

## AWS Deployment

Terraform lives in `terraform/`.

Required sensitive variables:

```bash
export TF_VAR_db_password='change-me'
export TF_VAR_wildfly_admin_password='change-me'
```

Recommended workflow:

```bash
terraform -chdir=terraform init
terraform fmt -check -diff terraform
terraform -chdir=terraform validate
terraform -chdir=terraform plan -out=tfplan
terraform -chdir=terraform apply tfplan
```

Quick apply:

```bash
terraform -chdir=terraform init && terraform -chdir=terraform apply
```

The EC2 backend bootstrap is handled through Terraform `user_data`. On first boot, the instance installs Docker, Docker Buildx, and Docker Compose; clones the configured repository; writes an `.env` file from Terraform variables; builds the WAR; starts an AWS-specific backend Compose service; and deploys the WAR into WildFly.

The AWS bootstrap points the backend at RDS and intentionally avoids the local development PostgreSQL, fake SMTP, and Ollama services.

Backend health check:

```bash
BACKEND_DNS=$(terraform -chdir=terraform output -raw backend_public_dns)
curl -i "http://${BACKEND_DNS}:8080/research-center/api/publications"
```

## Frontend Deployment

Terraform creates a private S3 bucket and CloudFront distribution for the generated Nuxt frontend. CloudFront serves static files from S3 and forwards `/research-center/api/*` to the backend EC2 instance, allowing the SPA to call the API over the same HTTPS origin.

After Terraform has applied successfully and the backend API is healthy, deploy frontend files:

```bash
scripts/deploy-frontend.sh
```

The script:

- Reads Terraform outputs.
- Sets `NUXT_PUBLIC_API_BASE` to `/research-center/api` by default.
- Runs `npm ci`.
- Generates the static Nuxt site.
- Syncs `.output/public` to S3.
- Creates a CloudFront invalidation.

Override the API base when needed:

```bash
NUXT_PUBLIC_API_BASE='https://api.example.com/research-center/api' scripts/deploy-frontend.sh
```

Print the frontend URL:

```bash
terraform -chdir=terraform output -raw frontend_static_url
```

## Verification

Backend compile/test lifecycle:

```bash
bash mvnw test
```

Frontend production build:

```bash
NUXT_PUBLIC_API_BASE='/research-center/api' npm run build --prefix research-center-client
```

Terraform formatting and validation:

```bash
terraform fmt -check -diff terraform
terraform -chdir=terraform validate
```

Deployment script syntax:

```bash
bash -n scripts/deploy-frontend.sh
```

Current project note: there is no `src/test` tree yet, so Maven reports that no tests are available.

## Operational Notes

- Local Docker uses PostgreSQL in a container; AWS uses managed RDS PostgreSQL.
- Production database subnets are private and do not receive public IPs.
- The database security group allows `5432` only from the backend application security group.
- RDS deletion protection is enabled by default.
- The backend EC2 instance currently exposes WildFly on `8080` according to `backend_allowed_cidrs`.
- The Nuxt frontend is hosted from private S3 through CloudFront.
- CloudFront custom frontend domains require an ACM certificate in `us-east-1` via `frontend_cloudfront_certificate_arn`.
- The optional frontend ALB remains available for instance-based frontend targets but is not required for the S3/CloudFront deployment path.
- `persistence.xml` currently uses `drop-and-create`, which recreates schema on startup. Change this before using persistent production data.

## Troubleshooting

Check local container status:

```bash
make ps
```

Follow backend logs:

```bash
make logs
```

Open a shell in the local WildFly container:

```bash
make bash
```

Access local PostgreSQL:

```bash
make sql
```

Inspect fake SMTP mail storage:

```bash
make mails
```

Tear down local containers, local images, and volumes:

```bash
make down
```

Remove all Compose resources, including non-local images:

```bash
make down-all
```
