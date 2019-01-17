
README | [Architecture](ARCHITECTURE.md)

# Repository mirror

A tool for mirroring data from a GIT repository into a digital library
service. More specifically, a set of XML text documents maintained
using GIT should be possible to mirror into an eXist database and from
there into a SOLR search engine.

## How to build

You need java8 or better and Maven. There are two source trees,

* repository-pull (ActiveMQ consumer, doing the most of the job)
* repository-mirror-web (ActiveMQ producer, allowing users to queue things up)

just do 

```
mvn install
```

in each of those two directories

## Installation

### Data

You will need a some document projects to start with. Like

* adl-text-sources
* other_tei_projects
* [public-adl-text-sources](https://github.com/Det-Kongelige-Bibliotek/public-adl-text-sources)
* SKS_tei
* trykkefrihedsskrifter

Propose that you create a directory /home/text-service/ and checkout whatever texts you need there. We also have the 

* [solr-and-snippets](https://github.com/Det-Kongelige-Bibliotek/solr-and-snippets)

code close at hand

### Collections


