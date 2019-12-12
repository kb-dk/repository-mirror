[README](../README.md) | [Architecture](../ARCHITECTURE.md) | [Installation](../INSTALL.md) | [config.xml](CONFIG.md) | [Access control](../htaccess/README.md) | [Collections](./README.md)


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


# Properties

In most editions, such as SKS and GV, stores one document per
directory with auxiliary files with special functions, like comments,
notes, introductions. Since these files have different names and
formats in the different editions we have introduced a special
property file such that there is a such a file normalizing those
functions to a common vocabulary. They are stored in the database
under the name capabilities.xml.

Here's an example Søren Kierkegaard's Notebook 11:

```
<?xml version="1.0" encoding="UTF-8" ?>
<bibl xmlns='http://www.tei-c.org/ns/1.0'>
  <ref type='Hovedtekst' target='txt.xml'/>
  <relatedItem type="ignore" target="capabilities.xml"/>
  <relatedItem type="Kommentar" target="int_2.xml"/>
  <relatedItem type="Tekstkommentarer" target="kom.xml"/>
  <relatedItem type="Indledning" target="int_1.xml"/>
  <relatedItem type="Tekstredegørelse" target="txr.xml"/>
</bibl>
```

There is a ref to the main text and then links to relatedItem's of
various other files.

In the Solr and Snippets projects we have scripts for generating those
capabilities.xml files. One of them is an [XQuery capabilities_generator.xq](https://github.com/Det-Kongelige-Bibliotek/solr-and-snippets/blob/master/exporters/common/capabilities_generator.xq), which is used by repository-mirror to manage the capabilities. 

**For repository-mirror to work, this script has to be executable and SetUID and sticky for database admin, or it won't be able to save the generated documents.**
