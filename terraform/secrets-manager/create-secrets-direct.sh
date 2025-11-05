#!/bin/bash
# ============================================================================
# Script Alternativo - Criar Secrets Diretamente via AWS CLI
# ============================================================================
# Este script cria os secrets no AWS Secrets Manager SEM usar Terraform
# Útil quando Terraform não está instalado
# ============================================================================

set -e  # Exit on error

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}AWS Secrets Manager - Setup Direto${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Ler valores do terraform.tfvars
echo -e "${YELLOW}📋 Lendo configurações do terraform.tfvars...${NC}"

AWS_REGION="sa-east-1"
ENVIRONMENT="prod"
DB_HOST="cliente-core-prod.cneocl9ggplh.sa-east-1.rds.amazonaws.com"
DB_PORT="5432"
DB_NAME="vanessa_mudanca_clientes"
DB_USERNAME="dbadmin"
DB_PASSWORD="9QvSp8Itk54gu6EZV8FPXCRvufGpu+kT"
JWT_KEY="+P2AtTd0DIKt6cEvUmvo6hNAXR5g00mswWeDs1sePdY="

echo -e "${GREEN}✓ Configurações carregadas${NC}"
echo "  Region: $AWS_REGION"
echo "  Environment: $ENVIRONMENT"
echo "  DB Host: $DB_HOST"
echo ""

# ============================================================================
# 1. Criar Secret: Database Credentials
# ============================================================================

SECRET_NAME_DB="cliente-core/${ENVIRONMENT}/database"
echo -e "${YELLOW}🔐 Criando secret: $SECRET_NAME_DB${NC}"

# Montar JSON do secret
DB_SECRET_JSON=$(cat <<EOF
{
  "host": "$DB_HOST",
  "port": $DB_PORT,
  "database": "$DB_NAME",
  "username": "$DB_USERNAME",
  "password": "$DB_PASSWORD",
  "url": "jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
}
EOF
)

# Verificar se secret já existe
if aws secretsmanager describe-secret --secret-id "$SECRET_NAME_DB" --region "$AWS_REGION" &>/dev/null; then
    echo -e "${YELLOW}⚠️  Secret já existe. Atualizando...${NC}"
    aws secretsmanager put-secret-value \
        --secret-id "$SECRET_NAME_DB" \
        --secret-string "$DB_SECRET_JSON" \
        --region "$AWS_REGION" \
        --no-cli-pager > /dev/null
    echo -e "${GREEN}✓ Secret atualizado: $SECRET_NAME_DB${NC}"
else
    echo -e "${BLUE}➜ Criando novo secret...${NC}"
    aws secretsmanager create-secret \
        --name "$SECRET_NAME_DB" \
        --description "PostgreSQL database credentials for cliente-core ($ENVIRONMENT)" \
        --secret-string "$DB_SECRET_JSON" \
        --region "$AWS_REGION" \
        --tags Key=Project,Value="Va Nessa Mudança" Key=Service,Value=cliente-core Key=Environment,Value="$ENVIRONMENT" Key=ManagedBy,Value=CLI \
        --no-cli-pager > /dev/null
    echo -e "${GREEN}✓ Secret criado: $SECRET_NAME_DB${NC}"
fi

# Pegar ARN do secret
DB_SECRET_ARN=$(aws secretsmanager describe-secret --secret-id "$SECRET_NAME_DB" --region "$AWS_REGION" --query 'ARN' --output text)
echo -e "${GREEN}  ARN: $DB_SECRET_ARN${NC}"
echo ""

# ============================================================================
# 2. Criar Secret: JWT Signing Key
# ============================================================================

SECRET_NAME_JWT="cliente-core/${ENVIRONMENT}/jwt-key"
echo -e "${YELLOW}🔑 Criando secret: $SECRET_NAME_JWT${NC}"

# Montar JSON do secret
JWT_SECRET_JSON=$(cat <<EOF
{
  "signing_key": "$JWT_KEY",
  "algorithm": "HS256",
  "expiration": "86400"
}
EOF
)

# Verificar se secret já existe
if aws secretsmanager describe-secret --secret-id "$SECRET_NAME_JWT" --region "$AWS_REGION" &>/dev/null; then
    echo -e "${YELLOW}⚠️  Secret já existe. Atualizando...${NC}"
    aws secretsmanager put-secret-value \
        --secret-id "$SECRET_NAME_JWT" \
        --secret-string "$JWT_SECRET_JSON" \
        --region "$AWS_REGION" \
        --no-cli-pager > /dev/null
    echo -e "${GREEN}✓ Secret atualizado: $SECRET_NAME_JWT${NC}"
else
    echo -e "${BLUE}➜ Criando novo secret...${NC}"
    aws secretsmanager create-secret \
        --name "$SECRET_NAME_JWT" \
        --description "JWT signing key for OAuth2 authentication ($ENVIRONMENT)" \
        --secret-string "$JWT_SECRET_JSON" \
        --region "$AWS_REGION" \
        --tags Key=Project,Value="Va Nessa Mudança" Key=Service,Value=cliente-core Key=Environment,Value="$ENVIRONMENT" Key=ManagedBy,Value=CLI \
        --no-cli-pager > /dev/null
    echo -e "${GREEN}✓ Secret criado: $SECRET_NAME_JWT${NC}"
fi

# Pegar ARN do secret
JWT_SECRET_ARN=$(aws secretsmanager describe-secret --secret-id "$SECRET_NAME_JWT" --region "$AWS_REGION" --query 'ARN' --output text)
echo -e "${GREEN}  ARN: $JWT_SECRET_ARN${NC}"
echo ""

# ============================================================================
# 3. Criar IAM Role para ECS Tasks
# ============================================================================

ROLE_NAME="cliente-core-secrets-role-${ENVIRONMENT}"
echo -e "${YELLOW}👤 Criando IAM Role: $ROLE_NAME${NC}"

# Trust policy (permite ECS tasks assumirem a role)
TRUST_POLICY=$(cat <<'EOF'
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": [
          "ecs-tasks.amazonaws.com",
          "ec2.amazonaws.com"
        ]
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
)

# Verificar se role já existe
if aws iam get-role --role-name "$ROLE_NAME" &>/dev/null; then
    echo -e "${YELLOW}⚠️  Role já existe. Pulando criação...${NC}"
else
    echo -e "${BLUE}➜ Criando IAM role...${NC}"
    aws iam create-role \
        --role-name "$ROLE_NAME" \
        --assume-role-policy-document "$TRUST_POLICY" \
        --description "IAM role for cliente-core to read secrets from AWS Secrets Manager" \
        --tags Key=Project,Value="Va Nessa Mudança" Key=Service,Value=cliente-core Key=Environment,Value="$ENVIRONMENT" \
        --no-cli-pager > /dev/null
    echo -e "${GREEN}✓ Role criada: $ROLE_NAME${NC}"
fi

ROLE_ARN=$(aws iam get-role --role-name "$ROLE_NAME" --query 'Role.Arn' --output text)
echo -e "${GREEN}  ARN: $ROLE_ARN${NC}"
echo ""

# ============================================================================
# 4. Criar IAM Policy para ler secrets
# ============================================================================

POLICY_NAME="SecretsManagerReadPolicy"
echo -e "${YELLOW}📜 Criando IAM Policy: $POLICY_NAME${NC}"

# Policy document
POLICY_DOCUMENT=$(cat <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue",
        "secretsmanager:DescribeSecret"
      ],
      "Resource": [
        "$DB_SECRET_ARN",
        "$JWT_SECRET_ARN"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "kms:Decrypt",
        "kms:DescribeKey"
      ],
      "Resource": "*",
      "Condition": {
        "StringEquals": {
          "kms:ViaService": "secretsmanager.${AWS_REGION}.amazonaws.com"
        }
      }
    }
  ]
}
EOF
)

# Aplicar policy inline na role
echo -e "${BLUE}➜ Aplicando policy na role...${NC}"
aws iam put-role-policy \
    --role-name "$ROLE_NAME" \
    --policy-name "$POLICY_NAME" \
    --policy-document "$POLICY_DOCUMENT" \
    --no-cli-pager

echo -e "${GREEN}✓ Policy aplicada com sucesso${NC}"
echo ""

# ============================================================================
# RESUMO FINAL
# ============================================================================

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ Setup Concluído com Sucesso!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}📋 Recursos Criados:${NC}"
echo ""
echo "1. Secret (Database):"
echo "   Nome: $SECRET_NAME_DB"
echo "   ARN:  $DB_SECRET_ARN"
echo ""
echo "2. Secret (JWT):"
echo "   Nome: $SECRET_NAME_JWT"
echo "   ARN:  $JWT_SECRET_ARN"
echo ""
echo "3. IAM Role:"
echo "   Nome: $ROLE_NAME"
echo "   ARN:  $ROLE_ARN"
echo ""
echo -e "${YELLOW}📝 Próximos Passos:${NC}"
echo ""
echo "1. Copie o ROLE ARN acima"
echo ""
echo "2. Configure sua ECS Task Definition com:"
echo "   {" echo "     \"taskRoleArn\": \"$ROLE_ARN\","
echo "     \"containerDefinitions\": [{"
echo "       \"environment\": ["
echo "         {\"name\": \"AWS_REGION\", \"value\": \"$AWS_REGION\"},"
echo "         {\"name\": \"AWS_SECRETS_NAME\", \"value\": \"$SECRET_NAME_DB\"},"
echo "         {\"name\": \"SPRING_PROFILES_ACTIVE\", \"value\": \"prod\"}"
echo "       ]"
echo "     }]"
echo "   }"
echo ""
echo "3. Verifique os secrets criados:"
echo "   aws secretsmanager get-secret-value --secret-id $SECRET_NAME_DB --region $AWS_REGION"
echo ""
echo -e "${GREEN}Sucesso! 🎉${NC}"
