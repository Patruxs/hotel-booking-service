# Docker MySQL and DataGrip Setup Guide

This guide explains how to run MySQL with Docker and connect to it from DataGrip for this Spring Boot project.

## Current Project Database Settings

The project uses this local MySQL connection in `src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hotel_booking_service
    username: root
    password: root123456
```

Use the same values in DataGrip:

```text
Host: localhost
Port: 3306
User: root
Password: root123456
Database: hotel_booking_service
```

## Prerequisites

Install these tools first:

- Docker Desktop
- DataGrip
- Java 21
- Maven, or use the included Maven Wrapper

After installing Docker Desktop, start it before running any Docker command.

Check Docker:

```powershell
docker --version
docker info
```

If `docker info` fails, Docker Desktop is not running.

## Step 1: Check Whether Port 3306 Is Free

MySQL normally uses port `3306`. Before creating the Docker container, check whether another service is already using it:

```powershell
Test-NetConnection localhost -Port 3306
```

If the result is:

```text
TcpTestSucceeded: False
```

then port `3306` is free.

If the result is:

```text
TcpTestSucceeded: True
```

then something is already using port `3306`. Find the process:

```powershell
Get-NetTCPConnection -LocalPort 3306 -State Listen |
  Select-Object LocalAddress,LocalPort,OwningProcess
```

If the process is from WSL/MySQL, stop MySQL inside WSL:

```powershell
wsl -u root -e sh -lc "service mysql stop 2>/dev/null || service mariadb stop 2>/dev/null || true"
```

Check again:

```powershell
Test-NetConnection localhost -Port 3306
```

## Step 2: Remove Old MySQL Containers

If you previously created test MySQL containers, remove them to avoid credential conflicts:

```powershell
docker rm -f hotel-mysql
docker rm -f hotel-mysql-datagrip
```

It is okay if Docker says a container does not exist.

Warning: removing a MySQL container deletes the database stored inside that container unless you configured a Docker volume.

## Step 3: Create the MySQL Docker Container

Create a new MySQL 8 container:

```powershell
docker run --name hotel-mysql `
  -e MYSQL_ALLOW_EMPTY_PASSWORD=yes `
  -e MYSQL_DATABASE=hotel_booking_service `
  -p 127.0.0.1:3306:3306 `
  -d mysql:8.0 `
  --default-authentication-plugin=mysql_native_password
```

This creates:

```text
Container name: hotel-mysql
MySQL version: 8.0
Host port: 3306
Container port: 3306
Database: hotel_booking_service
```

## Step 4: Set Root Password

Wait until MySQL is ready:

```powershell
docker exec hotel-mysql mysqladmin ping -uroot --silent
```

If it prints:

```text
mysqld is alive
```

then MySQL is ready.

Set the root password and grants:

```powershell
docker exec hotel-mysql mysql -uroot -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root123456'; CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root123456'; ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root123456'; GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION; GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION; CREATE DATABASE IF NOT EXISTS hotel_booking_service; FLUSH PRIVILEGES;"
```

Restart the container:

```powershell
docker restart hotel-mysql
```

## Step 5: Verify MySQL Login

Verify root login:

```powershell
docker exec hotel-mysql mysql -uroot --password=root123456 -e "SELECT USER(), CURRENT_USER(); SHOW DATABASES LIKE 'hotel_booking_service';"
```

Expected result:

```text
USER()          CURRENT_USER()
root@localhost root@localhost

Database (hotel_booking_service)
hotel_booking_service
```

Verify the port:

```powershell
Test-NetConnection localhost -Port 3306
```

Expected result:

```text
TcpTestSucceeded: True
```

## Step 6: Configure DataGrip

Open DataGrip and create a new data source:

1. Open the Database tool window.
2. Click `+`.
3. Choose `Data Source`.
4. Choose `MySQL`.
5. If DataGrip asks for driver files, click `Download`.
6. Enter these values:

```text
Host: localhost
Port: 3306
User: root
Password: root123456
Database: hotel_booking_service
```

Click `Test Connection`.

If DataGrip asks for a JDBC URL, use:

```text
jdbc:mysql://localhost:3306/hotel_booking_service?useSSL=false&allowPublicKeyRetrieval=true
```

## Step 7: Configure Spring Boot

Make sure `src/main/resources/application-dev.yml` contains:

```yaml
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:mysql://localhost:3306/hotel_booking_service
    username: root
    password: root123456
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
```

Run the app with the `dev` profile:

```powershell
.\mvnw.cmd spring-boot:run -Pdev
```

## Useful Docker Commands

Show running containers:

```powershell
docker ps
```

Start MySQL:

```powershell
docker start hotel-mysql
```

Stop MySQL:

```powershell
docker stop hotel-mysql
```

View MySQL logs:

```powershell
docker logs hotel-mysql
```

Open MySQL shell:

```powershell
docker exec -it hotel-mysql mysql -uroot --password=root123456 hotel_booking_service
```

Remove the container:

```powershell
docker rm -f hotel-mysql
```

## Troubleshooting

### Error: Connection refused

Example:

```text
Connection refused: connect
```

Cause: MySQL is not running or port `3306` is not mapped.

Fix:

```powershell
docker ps
docker start hotel-mysql
Test-NetConnection localhost -Port 3306
```

### Error: Access denied for user root

Example:

```text
[28000][1045] Access denied for user 'root'@'localhost' (using password: YES)
```

Cause: DataGrip is using the wrong password or cached credentials.

Fix:

1. Delete the old DataGrip data source.
2. Create a new MySQL data source.
3. Type the password manually:

```text
root123456
```

You can also reset root inside the container:

```powershell
docker exec hotel-mysql mysql -uroot --password=root123456 -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root123456'; ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root123456'; FLUSH PRIVILEGES;"
```

### Error: Port 3306 already in use

Cause: another MySQL service is already running on your machine or inside WSL.

Find the process:

```powershell
Get-NetTCPConnection -LocalPort 3306 -State Listen |
  Select-Object LocalAddress,LocalPort,OwningProcess
```

If WSL owns the port, stop WSL MySQL:

```powershell
wsl -u root -e sh -lc "service mysql stop 2>/dev/null || service mariadb stop 2>/dev/null || true"
```

Then recreate the Docker container.

### DataGrip Connects to the Wrong Database

Cause: DataGrip may reuse an old saved data source or password.

Fix:

1. Remove the old DataGrip connection.
2. Create a fresh MySQL data source.
3. Use:

```text
Host: localhost
Port: 3306
User: root
Password: root123456
Database: hotel_booking_service
```

## Clean Rebuild

Use this when you want to reset everything:

```powershell
docker rm -f hotel-mysql
wsl -u root -e sh -lc "service mysql stop 2>/dev/null || service mariadb stop 2>/dev/null || true"
docker run --name hotel-mysql `
  -e MYSQL_ALLOW_EMPTY_PASSWORD=yes `
  -e MYSQL_DATABASE=hotel_booking_service `
  -p 127.0.0.1:3306:3306 `
  -d mysql:8.0 `
  --default-authentication-plugin=mysql_native_password
docker exec hotel-mysql mysql -uroot -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root123456'; CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root123456'; ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root123456'; GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION; GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION; CREATE DATABASE IF NOT EXISTS hotel_booking_service; FLUSH PRIVILEGES;"
docker restart hotel-mysql
```

After rebuilding, connect DataGrip again with:

```text
Host: localhost
Port: 3306
User: root
Password: root123456
Database: hotel_booking_service
```
