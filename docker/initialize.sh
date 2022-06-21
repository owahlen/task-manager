#!/bin/bash -e

# Variable that can be set with their defaults
KEYCLOAK_HOST=${KEYCLOAK_HOST:-"localhost"}
KEYCLOAK_PORT=${KEYCLOAK_PORT:-"8180"}
TASKMANAGER_DB_NAME=${TASKMANAGER_DB_NAME:-"taskmanager"}
TASKMANAGER_DB_HOST=${TASKMANAGER_DB_HOST:-"localhost"}
TASKMANAGER_DB_PORT=${TASKMANAGER_DB_PORT:-"5432"}
TASKMANAGER_DB_USERNAME=${TASKMANAGER_DB_USERNAME:-"postgres"}
TASKMANAGER_DB_PASSWORD=${TASKMANAGER_DB_PASSWORD:-"password"}

SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]:-$0}"; )" &> /dev/null && pwd 2> /dev/null; )";

# Initialize Keycloak and put the UUID of the admin user in the admin_user_id variable
echo "Initializing Keycloak"
source <(docker run --net="host" --rm -e "realmName=TaskManager" -e "KEYCLOAK_HOST=${KEYCLOAK_HOST}" -e "KEYCLOAK_PORT=${KEYCLOAK_PORT}" \
  -v ${SCRIPT_DIR}/keycloak:/opt/jboss/initialize --entrypoint /bin/bash jboss/keycloak -c /opt/jboss/initialize/initialize-keycloak.sh - \
  | tee /dev/tty)

# Create database schema of task-service
echo "Creating database schema for task-service"
docker run --net="host" --rm -v ${SCRIPT_DIR}/../task-service/src/main/resources:/liquibase/changelog liquibase/liquibase \
  --url="jdbc:postgresql://${TASKMANAGER_DB_HOST}:${TASKMANAGER_DB_PORT}/${TASKMANAGER_DB_NAME}?sslmode=disable" \
  --changeLogFile=classpath:/db/changelog/db.changelog-master.yaml --username=${TASKMANAGER_DB_USERNAME} --password=${TASKMANAGER_DB_PASSWORD} update

# Create admin user in task-service
echo "Creating admin user for task-service"
docker run --net="host" --rm -e PGPASSWORD=${TASKMANAGER_DB_PASSWORD} postgres psql \
  "postgresql://${TASKMANAGER_DB_USERNAME}@${TASKMANAGER_DB_HOST}:${TASKMANAGER_DB_PORT}/${TASKMANAGER_DB_NAME}" \
   -c "INSERT INTO users (uuid, email, first_name, last_name) VALUES ('${admin_user_id}', 'admin', '', '');"
