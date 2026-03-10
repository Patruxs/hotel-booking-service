# outputs.tf

output "alb_dns_name" {
  description = "URL to access the application"
  value       = aws_lb.app_alb.dns_name
}

output "rds_endpoint" {
  description = "Database endpoint for Spring Boot to connect"
  value       = aws_db_instance.mysql_db.endpoint
}

output "monitor_public_ip" {
  description = "Public IP of Monitoring server (for SSH and Grafana)"
  value       = aws_instance.monitor.public_ip
}

output "app_1_public_ip" {
  value = aws_instance.app_1.public_ip
}

output "app_2_public_ip" {
  value = aws_instance.app_2.public_ip
}