# security.tf

# 1. ALB Security Group (Allow users to access web from anywhere)
resource "aws_security_group" "alb_sg" {
  name        = "HotelBooking-ALB-SG"
  description = "Security Group for Application Load Balancer"
  vpc_id      = aws_vpc.main_vpc.id

  ingress {
    description = "Allow HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "HotelBooking-ALB-SG" }
}

# 2. Monitoring Security Group (Allow admin to access Grafana and SSH)
resource "aws_security_group" "monitor_sg" {
  name        = "HotelBooking-Monitor-SG"
  description = "Security Group for Prometheus and Grafana"
  vpc_id      = aws_vpc.main_vpc.id

  ingress {
    description = "Allow SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Allow Grafana"
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Allow Prometheus"
    from_port   = 9090
    to_port     = 9090
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "HotelBooking-Monitor-SG" }
}

# 3. App Security Group (Accept traffic from ALB and Monitor)
resource "aws_security_group" "app_sg" {
  name        = "HotelBooking-App-SG"
  description = "Security Group for Spring Boot EC2"
  vpc_id      = aws_vpc.main_vpc.id

  ingress {
    description     = "Allow 8080 from ALB"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  ingress {
    description     = "Allow 8080 from Monitoring"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.monitor_sg.id]
  }

  ingress {
    description = "Allow SSH for Ansible"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description     = "Allow 80 from ALB for Frontend"
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "HotelBooking-App-SG" }
}

# 4. Database Security Group (Last layer of defense, only accepts from App)
resource "aws_security_group" "db_sg" {
  name        = "HotelBooking-DB-SG"
  description = "Security Group for RDS MySQL"
  vpc_id      = aws_vpc.main_vpc.id

  ingress {
    description     = "Allow 3306 from App SG only"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.app_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "HotelBooking-DB-SG" }
}