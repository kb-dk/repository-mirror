
# Tools for the mirroring of repositories 

1. The term **repository** is used as a synonym for a data or software project stored in a revision control system. To begin with we limit ourselves to GIT.
2. The term **user** is referring to a library patron registrered in our user database, and is responsible for the **repository**
3. The term **system** is referring to the repository-mirror system
4. A **release** is a named version or tag which can be manipulated using git tag commands

## Ideas

### 1. Git Large File Storage (LFS)

"Git Large File Storage (LFS) replaces large files such as audio
samples, videos, datasets, and graphics with text pointers inside Git,
while storing the file contents on a remote server like GitHub.com or
GitHub Enterprise" Sounds promising :^)

https://git-lfs.github.com/

Imaging correlated or transcribed text and images, videos or sound. A
system dependent way of maintaining links and redirection of local
ones to a remote server.

### 2. Duplicating/mirroring repository

A true mirror (if we are not to push things to the origin, I believe). Not sure that helps

https://help.github.com/articles/duplicating-a-repository/

### 3. LDAP authentication and authorization

Should be easy to add an external contributor through Active Directory

https://httpd.apache.org/docs/2.4/mod/mod_authnz_ldap.html

That should relieve us from user management

### 4. activemq

http://activemq.apache.org/

## Workflow

### 0. One or more responsible library users register as a content providers

Done using [in our user database: AD/LDAP](#3-ldap-authentication-and-authorization)

### 1. An external repository is registered by the user who must also provide credentials for the remove git repository

We do not need this, unless the repository is private

### 2. The user selects a named release for us to retrieve, and a status (staging or production). 

### 3. The system queues the clone and pulls of data from the repository



### 4. Upon successful cloning (3 above), the system queues loading and indexing of data
### 5. If failure in 3 above, messages should be passed to those who can do something about it
### 6. Upon successful loading in 4. above, the data become available in the environment selected in 2 above.
### 7. If failure, go to 5.

