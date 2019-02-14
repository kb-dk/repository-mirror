
README | [Architecture](ARCHITECTURE.md)

# Repository mirror

A tool for mirroring data from a GIT repository into a digital library
service. More specifically, a set of XML text documents maintained
using GIT should be possible to mirror into an eXist database and from
there into a SOLR search engine.

## Configuration

Copy the file

```config.xml```

to the name

```config_secret.xml```

and edit it under that name that name. Make sure that you keep that
for yourself, since it is supposed to contain credentials for your git
repositories! The repository user is supposed to have read only access
only, but nevertheless.

Before building you can run 

```add_config.sh```

which copies the config.xml to the source trees. After use run 

```del_config.sh```

## How to build

You need java8 or better and Maven. There are two source trees,

* repository-pull (ActiveMQ consumer, doing the most of the job)
* repository-mirror-web (ActiveMQ producer, allowing users to queue things up)

Doing

```
mvn install
```

in project root builds both trees

## Installation

The products from building are in the directories

```
./repository-pull/target
./database-push/target
./repository-mirror-web/target

```

The last one contains a web archive

```repository-mirror-web.war```

which is installed by copying it to your apache tomcat servlet
container. We are assuming that your tomcat is run by a user with the
same name __tomcat__

The other two directories contain the jars

```repository-pull-1.0.one-jar.jar
database-push-1.0.one-jar.jar```

### Data

You will need a some document projects to start with. Like

* adl-text-sources
* other_tei_projects
* [public-adl-text-sources](https://github.com/Det-Kongelige-Bibliotek/public-adl-text-sources)
* SKS_tei
* trykkefrihedsskrifter

Propose that you create a directory /home/text-service/ and checkout whatever texts you need there.
The directory were the texts are living should be read- and writable for the user running the tomcat.

We also have the 

* [solr-and-snippets](https://github.com/Det-Kongelige-Bibliotek/solr-and-snippets)

code close at hand

### Collections


