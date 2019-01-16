
README | [Architecture](ARCHITECTURE.md)

# How to build

You need java8 or better and Maven. There are two source trees,

* repository-pull (ActiveMQ consumer, doing the most of the job)
* repository-mirror-web (ActiveMQ producer, allowing users to queue things up)

just do 

```
mvn install

```