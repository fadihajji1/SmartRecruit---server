@echo off
set POSTGRES_PASSWORD=postgres
set JWT_SECRET=replace_with_a_generated_random_secret
set JWT_ISSUER=smartrecruit-identity
set ADMIN_EMAIL=admin@admin.com
set ADMIN_PASSWORD=admin
set MINIO_ROOT_USER=smartrecruitMinio
set MINIO_ROOT_PASSWORD=SmartRecruitLocalMinio_2026
set MINIO_BUCKET=smartrecruit-private
set MINIO_CV_BUCKET=smartrecruit-cvs
set DOMAIN_EVENTS_TOPIC=smartrecruit.domain-events
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1
set PATH=%JAVA_HOME%\bin;%PATH%

cd services\identity-service
start /B java -jar target\identity-service-0.0.1-SNAPSHOT.jar
cd ..\candidate-ai-service
start /B java -jar target\candidate-ai-service-0.0.1-SNAPSHOT.jar
cd ..\subscription-service
start /B java -jar target\subscription-service-0.0.1-SNAPSHOT.jar
cd ..\notification-service
start /B java -jar target\notification-service-0.0.1-SNAPSHOT.jar
cd ..\analytics-service
start /B java -jar target\analytics-service-0.0.1-SNAPSHOT.jar
cd ..\recruitment-service
start /B java -jar target\recruitment-service-0.0.1-SNAPSHOT.jar
cd ..\interview-service
start /B java -jar target\interview-service-0.0.1-SNAPSHOT.jar
cd ..\api-gateway
start /B java -jar target\api-gateway-0.0.1-SNAPSHOT.jar
