
[README](README.md) | [Architecture](ARCHITECTURE.md) | [Installation](INSTALL.md) | [Collections](./collections/README.md)

# Installation

## 1. Prerequisites

You need for building

* the Maven build tool

For running it you need

* tomcat 8 & java 8 (or better. it is tested with version 8)
* Active MQ. I use version 5.11.1
* A modern Apache HTTPD supporting [Apache Expressions](https://httpd.apache.org/docs/2.4/expr.html) (httpd 2.4 or better). See [Access control README](htaccess/README.md)

## 2. Configuration

Copy the file

```
config.xml
```

to the name

```
config_secret.xml
```

and edit it under that name that name. As of writing this, the file looks like this

Before building you can run 

```add_config.sh```

which copies the config.xml to the source trees. After use run 

```del_config.sh```

## 3. How to build

There are three source trees,

* database-push (ActiveMQ consumer, doing the most of the job related to the database and the indexing)
* repository-pull (ActiveMQ consumer and producer, doing the most of the GIT related jobs)
* repository-mirror-web (ActiveMQ producer, allowing users to queue things up)

Doing

```
mvn clean ; mvn install
```

in project root removes old stuff and builds new fresh ones in all three source trees

## 4. Installation

### ActiveMQ

Make sure that the ActiveMQ daemon is configured and running. 

The products from building are in the directories

```
./repository-pull/target
./database-push/target
./repository-mirror-web/target
```

### Web UI

The last one contains a web archive

```
import.war
```

which is installed by copying it to your apache tomcat servlet
container. We are assuming that your tomcat is run by a user with the
same name __tomcat__

The web interface starts at 

```
http(s)://<your_host_port>/import/
```

### Daemons

The other two directories contain the jars

```
repository-pull-1.0.one-jar.jar
database-push-1.0.one-jar.jar
```

I start them (as root) using

```
cd repository-pull ; sudo ./run-command.sh
cd database-push ; sudo ./run-command.sh 
```

in each of those directories there should be a ```run_directory```
owned by __tomcat__.

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

### Data

You will need a some document projects to start with. Like

* adl-text-sources
* other_tei_projects
* [public-adl-text-sources](https://github.com/Det-Kongelige-Bibliotek/public-adl-text-sources)
* SKS_tei
* trykkefrihedsskrifter

Propose that you create a directory

```
/home/text-service/
```

and checkout whatever texts you need there. Note the script

```
./clone.sh
```

You'll need an account on github with access to the texts. The
directory where the texts are living should be read- and writable for
the user running the tomcat, who is named __tomcat__. As a matter of
fact, it is best to clone them as tomcat user.

We also have the 

* [solr-and-snippets](https://github.com/Det-Kongelige-Bibliotek/solr-and-snippets)

code close at hand
