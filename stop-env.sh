#!/bin/bash
echo "Đang tắt 3 máy EC2..."
# Bạn cần thay các ID dưới đây bằng Instance ID thật của bạn (dạng i-0abcd1234...)
aws ec2 stop-instances --instance-ids i-0e0ce92b486a6afc6 i-0dc23df42e76ead65 i-0e17601c7d596d060

echo "Đang tắt Database RDS..."
aws rds stop-db-instance --db-instance-identifier hotel_booking
echo "Đã ra lệnh tắt! Bạn có thể đi ngủ."
