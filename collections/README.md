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
