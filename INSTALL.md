[README](README.md) | [Architecture](ARCHITECTURE.md) | [Installation](INSTALL.md) | [Upgrading](UPGRADE.md) | [config.xml](CONFIG.md) | [Access control](./htaccess/README.md) | [Collections](./collections/README.md)

# Installation


## 1. Prerequisites

You need for building

* the Maven build tool, version 3 or better (something like ```yum install maven35``` whould do it)

For running it you need

* apache-tomcat version 9.0.2* & and a recent java 8 (it is only tested with version 8). Add ```<welcome-file>index.jspx</welcome-file>``` to the welcome-file-list in its web.xml
* Active MQ. I use version 5.11.1
* A modern Apache HTTPD supporting [Apache Expressions](https://httpd.apache.org/docs/2.4/expr.html) (tested on httpd 2.4.29 and better).

Start by cloning this repository, or if applicable, download one of the releases.

**Please note that there are some prerequisites for the database.**

* [Collections](./collections/README.md) README contain info on collection level records required by repository-mirror. 
* There is also one special script installed with the [solr-and-snippets](https://github.com/Det-Kongelige-Bibliotek/solr-and-snippets) package which requires some extra attention, namely [capabilities_generator.xq](https://github.com/Det-Kongelige-Bibliotek/solr-and-snippets/blob/master/exporters/common/capabilities_generator.xq). It requires some enhanced permissions, SETUID and SETGID, or it cannot create the capabilities file.

See also the text on [correlations and identifications](https://github.com/Det-Kongelige-Bibliotek/solr-and-snippets/blob/master/correlations-and-identifications.md#correlations).



## 2. Configuration

Copy the file

```
config.xml
```

to the name

```
config_secret.xml
```

and edit it under that name that name (if your software developers
don't provide one for you). As of writing this, the file [looks like
this](CONFIG.md). The system accesses other services as

* a git user
* an eXist DB admin user

Before building run 

```
add_config.sh
```

which copies the config.xml to the source trees. After use you can run 

```
del_config.sh
```

to ensure that there is no configuration files everywhere.

### HTTPD and Access Control

See [Access control README](htaccess/README.md)

## 3. How to build

There are three source trees,

* database-push (ActiveMQ consumer, doing the most of the job related to the database and the indexing)
* repository-pull (ActiveMQ consumer and producer, doing the most of the GIT related jobs)
* repository-mirror-web (ActiveMQ producer, allowing users to queue things up)

Doing

```
mvn clean ; mvn install
```

in project root removes old stuff and builds new fresh ones in all
three source trees. Occasionally we see that installation fails
because of a broken unit test, then do

```
mvn install -Dmaven.test.skip=true
```

## 4. Installation

### Data

You will need a some document projects to start with. Like

* adl-text-sources
* other_tei_projects
* [public-adl-text-sources](https://github.com/Det-Kongelige-Bibliotek/public-adl-text-sources)
* SKS_tei
* trykkefrihedsskrifter

Create a directory called

```
/home/text-service/
```

which should be owned by and read and writable to tomcat. Clone
whatever texts you need there. [Note the script](./git-clone-corpus.sh)

```
./git-clone-corpus.sh
```

Each of those should have a branch called ```installed_corpus```. There is a script doing that.

```
./git-branch-corpus.sh
```

Run both as tomcat, or the versioning will not work!

You'll need an account on github with access to the texts. The
directory where the texts are living should be read- and writable for
the user running the tomcat, who is named __tomcat__. As a matter of
fact, it is best to clone them as tomcat user.

### ActiveMQ

Make sure that the ActiveMQ daemon is configured and running. 

The products from building are in the directories

```
./repository-pull/target
./database-push/target
./repository-mirror-web/target
```

### Web UI

The last directory mentioned above a web archive

```
./repository-mirror-web/target/import.war
```

which is **installed by copying it to your apache tomcat servlet**
container. We are assuming that your tomcat is run by a user with the
same name __tomcat__

The web interface starts at 

```
http(s)://<your_host_port>/import/
```

For instance ```localhost:8080``` if you run it on your workstation.

### Daemons

In each of the directories

```
./repository-pull/
./database-push/
```

there should be a ```run_directory``` owned by __tomcat__.

Note that the daemons will not run, unless the paths to executables
and data are read, executable and (when applicable) writable to tomcat
user. Run these

```
cd repository-mirror
chown -r tomcat repository-pull/run_directory
chown -r tomcat database-push/run_directory
chown -r tomcat repository-pull/target
chown -r tomcat database-push/target
```

containing the the jars

```
repository-pull-1.0.one-jar.jar
database-push-1.0.one-jar.jar
```
#### Repository Pull Service

You start repository-pull using systemd

```
 sudo vi /etc/systemd/system/repository_pull.service

```

paste this into the editor and save:

```
[Unit]
Description=Repository Pull Java Daemon

[Service]
WorkingDirectory=/home/text-service/repository-mirror/repository-pull/run_directory
ExecStart=/bin/java -jar ../target/repository-pull-1.0.one-jar.jar
User=tomcat
Type=simple
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

__NB:__ Note the /home/text-service/repository-mirror/ path in the script. Changed that if necessary.

#### Database Push Service

```
 sudo vi /etc/systemd/system/database_push.service
```

Paste the following into the editor

```
[Unit]
Description=Database Push Java Daemon

[Service]
WorkingDirectory=/home/text-service/repository-mirror/database-push/run_directory
ExecStart=/bin/java -jar ../target/database-push-1.0.one-jar.jar
User=tomcat
Type=simple
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```
__NB:__ Note the /home/text-service/repository-mirror/ path in the script. Changed that if necessary.

### While developing

While developing, I start them (as root) using

```
cd repository-pull ; sudo ./run-command.sh
cd database-push ; sudo ./run-command.sh 
```

in each of those directories there should be a ```run_directory``` owned by __tomcat__.

Note that they won't run, unless the paths to 

```
repository-mirror/repository-pull/run_directory
repository-mirror/database-push/run_directory
```
are read, executable and writable to tomcat user

```
repository-mirror/repository-pull/target
repository-mirror/database-push/target
```
are read and executable to tomcat user

The programs are creating far too much log info. There is a script for cleaning:

```
./clean_logs.sh
```

Run it as root, you won't be allowed to delete tomcat's log files.


## 6. See also

You might need to have the 

* [solr-and-snippets](https://github.com/Det-Kongelige-Bibliotek/solr-and-snippets)

code close at hand
