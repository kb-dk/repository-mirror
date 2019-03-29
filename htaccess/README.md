
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
ProxyPass /import "ajp://localhost:8009/import/"
ProxyPassReverse /import "ajp://localhost:8009/import/"
ProxyPassReverseCookiePath /import /import

<LocationMatch ^/(import)/+.*>

 <Limit>
    AuthType Basic
    AuthName "Text service"
    AuthUserFile /home/xml-store/passwordfile
    AuthGroupFile /home/text-service/groupfile
    Require valid-user 
  </Limit>

</LocationMatch>
```
