[README](README.md) | [Architecture](ARCHITECTURE.md) | [Installation](INSTALL.md)

# Installation

## Prerequisites

You need 

* tomcat 8
* java 8 (or better)
* the Maven build tool
* Active MQ. I use version 5.11.1

## Configuration

Copy the file

```
config.xml
```

to the name

```
config_secret.xml
```

and edit it under that name that name. As of writing this, the file looks like this

```
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">

<properties>
    <comment>Configuration for text service daemon.</comment>
    <entry key="queue.uri">tcp://[Active MQ hostport]</entry>
    <entry key="queue.name">text-git-pull</entry>
    <entry key="queue.load.name">text-db-load</entry>
    <entry key="queue.logfile">text-logger.log</entry>
    <entry key="queue.loglevel">info</entry>
    <entry key="data.home">/home/text-service/</entry>

    <entry key="published.branch">installed_corpus</entry>

    <!-- These are RFC 6570 URI templates -->
    
    <entry key="indexing.template">http://{+solr_hostport}/solr/text-retriever-core/update</entry>
    <entry key="commit.template">http://{+solr_hostport}/solr/text-retriever-core/update?commit=true</entry>

    <entry key="collection.template">http://{+exist_hostport}/exist/rest/db/text-retriever/collection.xq{?repository}</entry>
    <entry key="solrizr.template">http://{+exist_hostport}/exist/rest/db/text-retriever/present.xq{?op,doc,c}</entry>
    <entry key="file.template">http://{+exist_hostport}/exist/rest/db/text-retriever/{collection}/{+file}</entry>

    <!-- Snippet server -->

    <entry key="staging">staging hostport</entry>
    <entry key="production">production hostport</entry>

    <!-- Two fields in one: note the semicolon! -->
    <entry key="staging.credentials">[user];[password]</entry>
    <entry key="production.credentials">[user];[password]</entry>

    <!-- The index servers -->
    <entry key="staging.index_hostport">host:port</entry>
    <entry key="production.index_hostport">host:port</entry>

    <!-- Eventually we'll need more than one of these: -->
    <entry key="git.user">the_git_user_name</entry>
    <entry key="git.password">very_secret_git_password</entry>
</properties>

```

For some reason, I'm completely unable to remember the standard port number of the
Active MQ. It is __61616__, hence the queue.uri will be

```
tcp://localhost:61616
```

if you run Active MQ on the same server as the rest of the software.

Make sure that you keep that file for yourself, since it is supposed to
contain credentials for your git repositories! The repository user is
supposed to have read only access only, but nevertheless. Obviously
the same is true for the compiled software where those who knows how
could extract the secret data from jar and war files.

Before building you can run 

```add_config.sh```

which copies the config.xml to the source trees. After use run 

```del_config.sh```

## How to build

There are three source trees,

* database-push (ActiveMQ consumer, doing the most of the job related to the database and the indexing)
* repository-pull (ActiveMQ consumer and producer, doing the most of the GIT related jobs)
* repository-mirror-web (ActiveMQ producer, allowing users to queue things up)

Doing

```
mvn install
```

in project root builds all three trees

## Installation

The products from building are in the directories

```
./repository-pull/target
./database-push/target
./repository-mirror-web/target
```

The last one contains a web archive

```
import.war
```

which is installed by copying it to your apache tomcat servlet
container. We are assuming that your tomcat is run by a user with the
same name __tomcat__

The other two directories contain the jars

```
repository-pull-1.0.one-jar.jar
database-push-1.0.one-jar.jar
```

### Data

You will need a some document projects to start with. Like

* adl-text-sources
* other_tei_projects
* [public-adl-text-sources](https://github.com/Det-Kongelige-Bibliotek/public-adl-text-sources)
* SKS_tei
* trykkefrihedsskrifter

Propose that you create a directory /home/text-service/ and checkout
whatever texts you need there.  The directory where the texts are
living should be read- and writable for the user running the tomcat,
who is named __tomcat__.

We also have the 

* [solr-and-snippets](https://github.com/Det-Kongelige-Bibliotek/solr-and-snippets)

code close at hand
