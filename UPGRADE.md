[README](README.md) | [Architecture](ARCHITECTURE.md) | [Installation](INSTALL.md) | [Upgrading](UPGRADE.md) | [config.xml](CONFIG.md) | [Access control](./htaccess/README.md) | [Collections](./collections/README.md)

# Upgrading

1. pull the new branch or release from github
2. make sure that if new config.xml contain new parameters, then update the config_secret.xml for your service and run add_config.sh
3. stop the text-service-backend (or database_push & repository_pull daemons, if upgrading from earlier prerelease)
4. recompile
5. remove the local git repositories (usually in /home/text-service/) and clone them again (eg., using ./git-clone-corpus.sh) and then  run git-branch-corpus.sh [in the same directory](./INSTALL.md#data) to prepare them for use.
6. copy the web application to the [servlet container](./INSTALL.md#web-ui)
7. start (or restart) the text-service-backend service (the database_push & repository_pull daemons are now obsolete) [See INSTALL.md if going from two daemons to one](INSTALL.md#start-and-stop-services). Restart the application server/the web application, such that it can connect to ActiveMQ with a fresh session.

## Only one service

Recent versions of this package is based on

```
text-service-backend.service
```

The previous database_push & repository_pull services should be disabled and removed.
