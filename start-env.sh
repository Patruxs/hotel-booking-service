#!/bin/bash
echo "Đang bật 3 máy EC2..."
aws ec2 start-instances --instance-ids i-0e0ce92b486a6afc6 i-0dc23df42e76ead65 i-0e17601c7d596d060

echo "Đang bật Database RDS..."
aws rds start-db-instance --db-instance-identifier hotel_booking
echo "Đang khởi động! Vui lòng cập nhật lại file inventory.ini với IP mới."