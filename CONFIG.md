[README](README.md)|[Architecture](ARCHITECTURE.md)|[Installation](INSTALL.md)|[config.xml](CONFIG.md)|[Access control](./htaccess/README.md)|[Collections](./collections/README.md)

# The config.xml file

```
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">

<properties>
    <comment>Configuration for text service daemon.</comment>

    <entry key="xsl.add_id">/xsl/add-id.xsl</entry>

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
