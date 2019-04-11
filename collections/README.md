[README](../README.md) | [Architecture](../ARCHITECTURE.md) | [Installation](../INSTALL.md) | [Collections](./README.md)


# Collections

Each collection in the text portal should have a description as the ones here

| Collection description | goes to eXist collection |
|:-----------------------|:----------|
| [collection-adl.xml](collection-adl.xml) | /exist/rest/db/text-retriever/adl/ |
| [collection-pmm.xml](collection-pmm.xml) | /exist/rest/db/text-retriever/pmm/ |
| [collection-grundtvig.xml](collection-grundtvig.xml) | /exist/rest/db/text-retriever/grundtvig/ |
| [collection-sks.xml](collection-sks.xml) | /exist/rest/db/text-retriever/sks/ |
| [collection-holberg.xml](collection-holberg.xml) | /exist/rest/db/text-retriever/holberg/ |
| [collection-tfs.xml](collection-tfs.xml) |  /exist/rest/db/text-retriever/tfs/ |

These are syntactically TEI bibliographic records. The RNG/RNC schemas
used are in the source tree ([tei_all.rng](tei_all.rng) and
[tei_all.rnc](tei_all.rnc))

The xquery script

```
collection.xq
```

goes to

```
/exist/rest/db/text-retriever/
```

The collection-sks.xml looks like

```
<?xml version="1.0" encoding="UTF-8" ?>
<bibl xmlns="http://www.tei-c.org/ns/1.0" 
      corresp="sks" 
      copyOf="https://github.com/Det-Kongelige-Bibliotek/SKS_tei.git">
  <title>Søren Kierkegaards Skrifter</title>
  <ref corresp="data/v1.9/">/</ref>
</bibl>
```

The file name information we get from git contains name and path like

```
SKS_tei/data/v1.9/ee1/txt.xml
```

1. Take edition acronym (/bibl/@corresp - in this case sks)
2. replace everything in file name to the left of and including the string in /bibl/ref/@corresp. I.e., in this case the regex
```
^.*data\/v1.9\/
```
with the string in /bibl/ref. Concatenate this to the acronym in 1.

3. The result will be sks/ee1/txt.xml 
4. The dokument URI will be if we concatenate this with the base URI of the text-retriever, e.g., http://xstorage-test-01.kb.dk:8080/exist/rest/db/text-retriever/. I.e.,  http://xstorage-test-01.kb.dk:8080/exist/rest/db/text-retriever/sks/ee1/txt.xml


