@echo off
cd /d "C:\Users\boufa\OneDrive - Ministere de l'Enseignement Superieur et de la Recherche Scientifique\Bureau\smart recruter\SmartRecruit---server-master\services\identity-service"
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
java -jar target\identity-service-0.0.1-SNAPSHOT.jar
