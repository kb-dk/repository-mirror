
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

