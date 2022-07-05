#!/bin/bash -e

KEYCLOAK_HOST=${KEYCLOAK_HOST:-"localhost"}
KEYCLOAK_PORT=${KEYCLOAK_PORT:-"8080"}
realmName=${realmName:-"TaskManager"}
task_browser_client_name=${task_browser_client_name:-"task-browser"}
task_service_client_name=${task_service_client_name:-"task-service"}
task_service_client_secret=${task_service_client_secret:-"2H1eJsF78MQL0akC9OE2lB7buGLIcIof"}

# PATH to kcadm.sh
PATH=$PATH:/opt/jboss/keycloak/bin

# login
echo -n "# "
kcadm.sh config credentials --server http://${KEYCLOAK_HOST}:${KEYCLOAK_PORT}/auth --realm master --user admin --password password

echo "# Creating realm ${realmName}"
echo -n "export realm_id="
realm_id=$(kcadm.sh create realms -i -s realm=${realmName} -s enabled=true -s editUsernameAllowed=true)
echo ${realm_id}

echo "# Creating clients"
echo -n "export task_browser_client_id="
task_browser_client_id=$(kcadm.sh create clients -i -r ${realmName} -s clientId=${task_browser_client_name} -s 'redirectUris=["http://localhost:8080/*"]' -s directAccessGrantsEnabled=true -s publicClient=true)
echo ${task_browser_client_id}
echo -n "export task_service_client_id="
task_service_client_id=$(kcadm.sh create clients -i -r ${realmName} -s clientId=${task_service_client_name} -s enabled=true -s clientAuthenticatorType=client-secret -s serviceAccountsEnabled=true -s secret=${task_service_client_secret})
echo ${task_browser_client_id}

echo "# Assigning realm-management/manage-users role to task_service service account"
kcadm.sh add-roles -r ${realmName} --uusername "service-account-${task_service_client_name}" --cclientid "realm-management" --rolename "manage-users"

echo "# Creating roles"
echo -n "export role_user_id="
role_user_id=$(kcadm.sh create roles -i -r ${realmName} -s name=ROLE_USER -s 'description=Regular user with limited set of permissions')
echo ${role_user_id}
echo -n "export role_admin_id="
role_admin_id=$(kcadm.sh create roles -i -r ${realmName} -s name=ROLE_ADMIN -s 'description=Admin user with unlimited access')
echo ${role_admin_id}

echo "# Creating groups"
echo -n "export task_manager_users_group_id="
task_manager_users_group_id=$(kcadm.sh create groups -i -r ${realmName} -s name=task-manager-users)
echo ${task_manager_users_group_id}
echo -n "export task_manager_admins_group_id="
task_manager_admins_group_id=$(kcadm.sh create groups -i -r ${realmName} -s name=task-manager-admins)
echo ${task_manager_admins_group_id}

echo "# Assigning roles to groups"
kcadm.sh add-roles -r ${realmName} --gid ${task_manager_users_group_id} --rolename ROLE_USER
kcadm.sh add-roles -r ${realmName} --gid ${task_manager_admins_group_id} --rolename ROLE_ADMIN

echo "# Creating user admin"
echo -n "export admin_user_id="
admin_user_id=$(kcadm.sh create users -i -r ${realmName} -s username=admin -s email=admin -s enabled=true)
echo ${admin_user_id}
kcadm.sh set-password -r ${realmName} --userid ${admin_user_id} --new-password password
kcadm.sh update users/${admin_user_id}/groups/${task_manager_users_group_id} -r ${realmName} -s realm=${realmName} -s userId=${admin_user_id} -s groupId=${task_manager_users_group_id} -n
kcadm.sh update users/${admin_user_id}/groups/${task_manager_admins_group_id} -r ${realmName} -s realm=${realmName} -s userId=${admin_user_id} -s groupId=${task_manager_admins_group_id} -n

echo "# Adding kafka event listener"
kcadm.sh update events/config -r ${realmName} -s 'eventsListeners=["jboss-logging","kafka"]'
