# provider.tf
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "ap-southeast-1" # Singapore region

  # Add default tags for easier cost management
  default_tags {
    tags = {
      Project     = "HotelBooking"
      Environment = "Production"
      ManagedBy   = "Terraform"
    }
  }
}