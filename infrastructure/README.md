# 🏗️ Hotel Booking - Infrastructure

This directory contains all **Infrastructure as Code (IaC)** source files to deploy the Hotel Booking system on **AWS Cloud**.

## 📐 Architecture Overview

```
                    ┌─────────────────────────────────────────────────────────┐
                    │                    AWS VPC (10.0.0.0/16)               │
                    │                                                         │
  Internet ────►    │  ┌─────────────────────────────────────────────┐        │
                    │  │        Application Load Balancer (ALB)       │        │
                    │  │            Port 80 (HTTP)                    │        │
                    │  └──────────────┬──────────────┬────────────────┘        │
                    │                 │              │                         │
                    │    ┌────────────▼──┐   ┌──────▼────────────┐            │
                    │    │  Public       │   │  Public           │            │
                    │    │  Subnet A     │   │  Subnet B         │            │
                    │    │  10.0.1.0/24  │   │  10.0.2.0/24      │            │
                    │    │               │   │                   │            │
                    │    │ ┌───────────┐ │   │ ┌───────────┐     │            │
                    │    │ │ EC2 App-1 │ │   │ │ EC2 App-2 │     │            │
                    │    │ │ Backend   │ │   │ │ Backend   │     │            │
                    │    │ │ Frontend  │ │   │ │ Frontend  │     │            │
                    │    │ │ :8080/:80 │ │   │ │ :8080/:80 │     │            │
                    │    │ └───────────┘ │   │ └───────────┘     │            │
                    │    │               │   │                   │            │
                    │    │ ┌───────────┐ │   │                   │            │
                    │    │ │ EC2       │ │   │                   │            │
                    │    │ │ Monitor   │ │   │                   │            │
                    │    │ │ :9090     │ │   │                   │            │
                    │    │ │ :3000     │ │   │                   │            │
                    │    │ └───────────┘ │   │                   │            │
                    │    └───────────────┘   └───────────────────┘            │
                    │                                                         │
                    │    ┌────────────────────────────────────────┐           │
                    │    │         Private Subnets                 │           │
                    │    │  10.0.3.0/24 (AZ-a) │ 10.0.4.0/24 (AZ-b)│         │
                    │    │         ┌──────────────────┐            │           │
                    │    │         │   RDS MySQL 8.0  │            │           │
                    │    │         │   db.t3.micro    │            │           │
                    │    │         │   :3306          │            │           │
                    │    │         └──────────────────┘            │           │
                    │    └────────────────────────────────────────┘           │
                    └─────────────────────────────────────────────────────────┘
```

## 🛠️ Tech Stack

| Technology | Purpose |
|-----------|---------|
| **Terraform** | AWS infrastructure provisioning (VPC, EC2, RDS, ALB, Security Groups) |
| **Ansible** | Configuration management & application deployment to EC2 |
| **Docker** | Containerization of Backend (Spring Boot) and Frontend |
| **Prometheus** | Metrics collection from Spring Boot Actuator |
| **Grafana** | Monitoring dashboard visualization |
| **AWS ALB** | Load balancing across 2 EC2 instances |
| **AWS RDS** | AWS-managed MySQL Database |

## 📁 Directory Structure

```
infrastructure/
├── README.md                        # This file
├── .gitignore                       # Exclude sensitive files from git
├── INFRASTRUCTURE_ANALYSIS.md       # Detailed infrastructure analysis document
│
├── terraform/                       # Terraform - AWS Infrastructure Provisioning
│   ├── provider.tf                  # AWS Provider configuration (region: ap-southeast-1)
│   ├── network.tf                   # VPC, Subnets, Internet Gateway, Route Tables
│   ├── security.tf                  # Security Groups (ALB, App, Monitor, DB)
│   ├── compute.tf                   # EC2 Instances, ALB, Target Groups, Listeners
│   ├── rds.tf                       # RDS MySQL Instance
│   ├── outputs.tf                   # Output important information after apply
│   └── .terraform.lock.hcl          # Terraform providers lock file
│
└── ansible/                         # Ansible - Deployment & Configuration
    ├── inventory.ini                # Server inventory (IPs + SSH config)
    ├── setup-docker.yml             # Playbook: Install Docker on all EC2 instances
    ├── deploy-app.yml               # Playbook: Deploy Backend + Frontend
    ├── deploy-monitor.yml           # Playbook: Deploy Prometheus + Grafana
    ├── docker-compose.yml           # Docker Compose for App (Backend + Frontend)
    ├── docker-compose-monitor.yml   # Docker Compose for Monitoring stack
    └── prometheus.yml               # Prometheus scrape configuration
```

## 🚀 Getting Started

### Prerequisites

- [Terraform](https://www.terraform.io/downloads) >= 1.0
- [Ansible](https://docs.ansible.com/ansible/latest/installation_guide/) >= 2.9
- [AWS CLI](https://aws.amazon.com/cli/) with configured credentials
- SSH Key file `hotel-key.pem` (not included in repo — must be created separately on AWS)

### Step 1: Provision AWS Infrastructure with Terraform

```bash
cd terraform

# Initialize Terraform (download providers)
terraform init

# Preview changes
terraform plan

# Apply (create actual infrastructure on AWS)
terraform apply
```

After a successful apply, Terraform will output:
- `alb_dns_name` — Application access URL
- `app_1_public_ip` / `app_2_public_ip` — Public IPs of the 2 App EC2 instances
- `monitor_public_ip` — Public IP of the Monitor EC2 instance
- `rds_endpoint` — MySQL RDS endpoint

### Step 2: Install Docker on EC2 Instances

```bash
cd ansible

# Install Docker and Docker Compose on all EC2 instances
ansible-playbook -i inventory.ini setup-docker.yml
```

### Step 3: Deploy the Application

```bash
# Deploy Backend + Frontend to the 2 App EC2 instances
ansible-playbook -i inventory.ini deploy-app.yml \
  -e "backend_image=<DOCKER_HUB_IMAGE>" \
  -e "frontend_image=<DOCKER_HUB_IMAGE>" \
  -e "rds_endpoint=<RDS_ENDPOINT>" \
  -e "cloudinary_name=<NAME>" \
  -e "cloudinary_key=<KEY>" \
  -e "cloudinary_secret=<SECRET>"

# Deploy Prometheus + Grafana to the Monitor EC2 instance
ansible-playbook -i inventory.ini deploy-monitor.yml
```

### Step 4: Access the Services

| Service | URL |
|---------|-----|
| **Application** | `http://<ALB_DNS_NAME>` |
| **Grafana** | `http://<MONITOR_IP>:3000` (default: admin/admin) |
| **Prometheus** | `http://<MONITOR_IP>:9090` |

## 🔒 Security

### Security Groups Strategy

```
Internet ──► ALB SG (Port 80)
                 │
                 ├──► App SG (Port 8080 from ALB, Port 80 from ALB, Port 22 SSH)
                 │        │
                 │        └──► DB SG (Port 3306 from App SG only)
                 │
                 └──► Monitor SG (Port 22, 3000, 9090)
                          │
                          └──► App SG (Port 8080 from Monitor)
```

- **RDS MySQL** is placed in **Private Subnets** with no public IP
- **Security Groups** are designed following the **least privilege** principle
- Database only accepts connections from the **App Security Group**

### ⚠️ Important Notice

> In this project, some credentials (DB password, JWT secret) are hardcoded directly in configuration files for **educational purposes**.
> In a real production environment, you **MUST** use:
> - **Terraform Variables** + `terraform.tfvars` (excluded from git)
> - **AWS Secrets Manager** or **Parameter Store**
> - **Ansible Vault** for encrypting secrets

## 🏷️ Resource Tags

All AWS resources are tagged for easier management and cost tracking:

| Tag | Value |
|-----|-------|
| `Project` | HotelBooking |
| `Environment` | Production |
| `ManagedBy` | Terraform |

## 📊 Monitoring

The monitoring stack uses **Prometheus + Grafana**:

- **Prometheus** scrapes metrics from **Spring Boot Actuator** (`/actuator/prometheus`) on both App EC2 instances every **15 seconds**
- **Grafana** connects to Prometheus to display monitoring dashboards:
  - CPU & Memory usage
  - HTTP request rate & response time
  - JVM metrics
  - Custom business metrics
