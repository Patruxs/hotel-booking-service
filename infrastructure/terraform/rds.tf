# rds.tf

# 1. Create Subnet Group for RDS (Place RDS in 2 Private Subnets)
resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "hotel-booking-db-subnet-group"
  subnet_ids = [aws_subnet.private_subnet_a.id, aws_subnet.private_subnet_b.id]

  tags = { Name = "HotelBooking-DB-Subnet-Group" }
}

# 2. Create RDS MySQL instance
resource "aws_db_instance" "mysql_db" {
  allocated_storage      = 20                   # 20GB storage (Free Tier eligible)
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t3.micro"        # DB server instance type
  db_name                = "hotel_booking"      # Initial database name
  username               = "admin"
  password               = "Admin123456!"       # Password (should not be hardcoded in production)
  parameter_group_name   = "default.mysql8.0"
  skip_final_snapshot    = true                 # Skip backup when deleting DB for faster operations
  publicly_accessible    = false                # Block access from the Internet

  vpc_security_group_ids = [aws_security_group.db_sg.id]
  db_subnet_group_name   = aws_db_subnet_group.db_subnet_group.name

  tags = { Name = "HotelBooking-RDS" }
}