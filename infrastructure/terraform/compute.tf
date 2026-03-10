# compute.tf

# 1. Find the latest Ubuntu 22.04 AMI (OS image)
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical (Ubuntu) account ID

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
}

# 2. Create 2 EC2 instances for App (Placed in Public Subnet)
resource "aws_instance" "app_1" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = "t2.micro"
  subnet_id                   = aws_subnet.public_subnet_a.id # Using Public Subnet
  associate_public_ip_address = true                          # Enable public IP
  vpc_security_group_ids      = [aws_security_group.app_sg.id]
  key_name                    = "hotel-key"
  tags = { Name = "HotelBooking-App-1" }
}

resource "aws_instance" "app_2" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = "t2.micro"
  subnet_id                   = aws_subnet.public_subnet_b.id # Using Public Subnet
  associate_public_ip_address = true                          # Enable public IP
  vpc_security_group_ids      = [aws_security_group.app_sg.id]
  key_name                    = "hotel-key"
  tags = { Name = "HotelBooking-App-2" }
}

# 3. Create EC2 instance for Monitoring (Placed in Public Subnet)
resource "aws_instance" "monitor" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = "t2.micro"
  subnet_id                   = aws_subnet.public_subnet_a.id
  vpc_security_group_ids      = [aws_security_group.monitor_sg.id]
  key_name                    = "hotel-key"

  tags = { Name = "HotelBooking-Monitor" }
}

# 4. Create Application Load Balancer (ALB)
resource "aws_lb" "app_alb" {
  name               = "hotel-booking-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb_sg.id]
  subnets            = [aws_subnet.public_subnet_a.id, aws_subnet.public_subnet_b.id]

  tags = { Name = "HotelBooking-ALB" }
}

# 5. Create Target Group (Group of App instances for ALB to forward traffic)
resource "aws_lb_target_group" "app_tg" {
  name     = "hotel-booking-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main_vpc.id

  # Health Check configuration for ALB to automatically remove unhealthy instances
  health_check {
    path                = "/actuator/health"
    matcher             = "200"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 2
  }
}

# 6. Attach 2 App instances to Target Group
resource "aws_lb_target_group_attachment" "app_1_attach" {
  target_group_arn = aws_lb_target_group.app_tg.arn
  target_id        = aws_instance.app_1.id
  port             = 8080
}

resource "aws_lb_target_group_attachment" "app_2_attach" {
  target_group_arn = aws_lb_target_group.app_tg.arn
  target_id        = aws_instance.app_2.id
  port             = 8080
}

# 7. Create Listener (Listen on port 80 on ALB and forward to Target Group)
resource "aws_lb_listener" "app_listener" {
  load_balancer_arn = aws_lb.app_alb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend_tg.arn
  }
}


# --- FRONTEND ---

# 1. Create Target Group for Frontend (Port 80)
resource "aws_lb_target_group" "frontend_tg" {
  name     = "hotel-frontend-tg"
  port     = 80
  protocol = "HTTP"
  vpc_id   = aws_vpc.main_vpc.id

  health_check {
    path                = "/"  # Nginx always returns the index.html homepage
    matcher             = "200"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 2
  }
}

# 2. Attach 2 EC2 instances to Frontend Target Group
resource "aws_lb_target_group_attachment" "frontend_1_attach" {
  target_group_arn = aws_lb_target_group.frontend_tg.arn
  target_id        = aws_instance.app_1.id
  port             = 80
}

resource "aws_lb_target_group_attachment" "frontend_2_attach" {
  target_group_arn = aws_lb_target_group.frontend_tg.arn
  target_id        = aws_instance.app_2.id
  port             = 80
}

# 3. Create routing rule: If path matches /api/* then forward to Backend (8080)
resource "aws_lb_listener_rule" "backend_api_rule" {
  listener_arn = aws_lb_listener.app_listener.arn
  priority     = 100 # Higher priority than default rule

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app_tg.arn # App_tg is backend port 8080
  }

  condition {
    path_pattern {
      values = ["/api/*"] # All Spring Boot APIs start with /api
    }
  }
}