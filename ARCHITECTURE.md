[README](README.md) | [Architecture](ARCHITECTURE.md) | [Installation](INSTALL.md) | [config.xml](CONFIG.md) | [Access control](./htaccess/README.md) | [Collections](./collections/README.md)

# Tools for the mirroring of repositories 

1. The term **repository** is used as a synonym for a data or software project stored in a revision control system. To begin with we limit ourselves to GIT.
2. The term **user** is referring to a library patron registrered in our user database. The **user**(s) is(are) the editor(s) responsible for the **repository**
3. The term **system** is referring to the repository-mirror system
4. A **release** is a named version (a tag) which can be manipulated using git tag commands and in particular be retrieved in a predictable condition. A branch is dynamic and change until frozen and merged with its master branch.
5. Here we refer to **commit** is including all revisions up to and including the identified commit in the given branch.

## Workflow

![Workflow](architecture/architecture.svg)

### 1. An external repository is registered in the database

Technically the registration is done using the procedure described in a [Collection description](./collections/README.md)

The software need read access to repository and must have credentials if it is private.

### 2. The user selects a destination

Depending on the destination status the user needs to enter slightly different data

1. **preview**     - The user may choose any branch for testing in a special preview server
2. **publication** - The user chooses a branch or named release for publication in a public server

The two statuses store data in separate databases and only the latter
is public. The staging service is a test site for the editorial users.

### 3. The user selects branch and induces pull operations

Running asynchronously using [ActiveMQ](#4-activemq) Multiple jobs per
repository should not be permitted. The system should ensure this is
impossible.

### 4. Upon successful pull (3 above), the system queues loading and indexing of data

1. Mirrors the data in local git repository
2. Store them in database (eXist)
3. Request indexing service for index documents
4. Store the index documents in index (Solr)

#### Process

We have a local copy of the git repository with all its branches. In
addition to these we have two branches locally that are never pushed to
the remote repository. They are named 

* installed_corpus
* previewed_corpus

They reflect the statuses in the publication and preview servers,
respectively.

If a user is about to preview or publish a branch X, we use a git diff
to calculate the set of files that differ between the two. Then we do
a git pull into the appropriate local branch and store the
corresponding files into the database from where they are then indexed in SOLR.

### 5. If failure in 3-4 above, messages should be passed to those who can do something about it

Task should be stored somewhere for re-execution

### 6. Upon successful loading in 4. above, the data become directly available in the environment selected in 2 above.

### 7. If failure, go to 4.
