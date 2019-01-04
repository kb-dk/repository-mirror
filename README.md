
# Tools for the mirroring of repositories 

## Components

### 1. Git Large File Storage (LFS): Sounds promising

"Git Large File Storage (LFS) replaces large files such as audio
samples, videos, datasets, and graphics with text pointers inside Git,
while storing the file contents on a remote server like GitHub.com or
GitHub Enterprise."

https://git-lfs.github.com/

Imaging correlated or transcribed text and images, videos or sound. A
system dependent way of maintaining links and redirection of local
ones to a remote server.

### 2. Duplicating/mirroring repository

A true mirror (if we are not to push things to the origin, I believe). Not sure that helps

https://help.github.com/articles/duplicating-a-repository/

### 3. LDAD authentication and authorization

Should be easy to add an external contributor through Active Directory

https://httpd.apache.org/docs/2.4/mod/mod_authnz_ldap.html

That should relieve us from user management

### 4. 