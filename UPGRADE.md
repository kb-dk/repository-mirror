[README](README.md) | [Architecture](ARCHITECTURE.md) | [Installation](INSTALL.md) | [Upgrading](UPGRADE.md) | [config.xml](CONFIG.md) | [Access control](./htaccess/README.md) | [Collections](./collections/README.md)

# Upgrading

1. pull the new branch or release from github
2. make sure that if new config.xml contain new parameters, then update the config_secret.xml for your service and run add_config.sh
3. stop the database_push & repository_pull daemons
4. recompile
5. copy the web application to the [servlet container](./INSTALL.md#web-ui)
6. restart the  database-push & repository-pull daemons



