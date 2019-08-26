[README](../README.md) | [Architecture](../ARCHITECTURE.md) | [Installation](../INSTALL.md) | [config.xml](../CONFIG.md)| [Access control](../htaccess/README.md) | [Collections](../collections/README.md)


# Access Control

make a password file using htpasswd

```
htpasswd -bc passwordfile first_trusted_user magic_word
htpasswd -b passwordfile second_trusted_user another_magic_word
htpasswd -b passwordfile third_trusted_user yet_another_magic_word
```

Then, create a group file, something like:

```
adl:first_trusted_user second_trusted_user
sks:third_trusted_user
```

There is no hard-coded file-paths like  /home/xml-store/passwordfile and /home/text-service/groupfile, but if you use those two, you can just copy the file text-import.conf to your apache configuration file area.

```
ProxyPassMatch /import/(css/|img/|adl/|sks/|gv/|choose/)?(.*$) "ajp://localhost:8009/import/$2"
ProxyPassReverseCookiePath /import /import

<LocationMatch "^/import/(?<sitename>[^/]+)/.*">

 <Limit GET POST PUT DELETE>
    AuthType Basic
    AuthName "Text service"
    AuthUserFile /home/text-service/passwordfile
    AuthGroupFile /home/text-service/groupfile

    Require group %{env:MATCH_SITENAME}	
  </Limit>

  RewriteEngine On

  RewriteCond %{LA-U:REMOTE_USER} (.+)
  RewriteRule . - [env=RU:%1,NS]
  RequestHeader set X-Forwarded-User %{RU}e

  RewriteCond %{env:MATCH_SITENAME} (.+)
  RewriteRule . - [E=Group:%1,NS]
  RequestHeader set X-Forwarded-Group  %{Group}e

</LocationMatch>
```
