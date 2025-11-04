#!/bin/bash

# Terraform Validation Script
# Run this before deploying to ensure configuration is valid

set -e

echo "=========================================="
echo "Terraform Configuration Validation"
echo "=========================================="
echo ""

# Check Terraform is installed
if ! command -v terraform &> /dev/null; then
    echo "❌ ERROR: Terraform is not installed"
    echo "Install: https://developer.hashicorp.com/terraform/install"
    exit 1
fi

# Check version
TERRAFORM_VERSION=$(terraform version -json | jq -r '.terraform_version')
echo "✅ Terraform version: $TERRAFORM_VERSION"

# Check AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo "❌ ERROR: AWS CLI is not installed"
    echo "Install: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html"
    exit 1
fi

AWS_VERSION=$(aws --version)
echo "✅ AWS CLI: $AWS_VERSION"

# Check AWS credentials
if ! aws sts get-caller-identity &> /dev/null; then
    echo "❌ ERROR: AWS credentials not configured"
    echo "Run: aws configure"
    exit 1
fi

ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "✅ AWS Account: $ACCOUNT_ID"

echo ""
echo "=========================================="
echo "Validating Terraform Configuration"
echo "=========================================="
echo ""

# Format check
echo "🔍 Checking Terraform formatting..."
if terraform fmt -check -recursive; then
    echo "✅ Code is properly formatted"
else
    echo "⚠️  Code needs formatting. Run: terraform fmt -recursive"
fi

# Initialize (if needed)
if [ ! -d ".terraform" ]; then
    echo ""
    echo "🔧 Initializing Terraform..."
    terraform init
fi

# Validate
echo ""
echo "🔍 Validating configuration..."
if terraform validate; then
    echo "✅ Configuration is valid"
else
    echo "❌ Configuration validation failed"
    exit 1
fi

# Check for terraform.tfvars
if [ ! -f "terraform.tfvars" ]; then
    echo ""
    echo "⚠️  WARNING: terraform.tfvars not found"
    echo "Copy terraform.tfvars.example to terraform.tfvars and customize"
    echo "Using default values from variables.tf"
fi

echo ""
echo "=========================================="
echo "Pre-flight Checks Complete"
echo "=========================================="
echo ""
echo "Next steps:"
echo "  1. Review configuration: terraform plan"
echo "  2. Deploy infrastructure: terraform apply"
echo "  3. Check estimated costs in the plan output"
echo ""
