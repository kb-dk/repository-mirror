xquery version "3.1" encoding "UTF-8";

import module namespace paths="http://kb.dk/this/paths" at "./get-paths.xqm";
declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";

declare namespace fn="http://www.w3.org/2005/xpath-functions";
declare namespace file="http://exist-db.org/xquery/file";
declare namespace util="http://exist-db.org/xquery/util";
declare namespace t="http://www.tei-c.org/ns/1.0";
declare variable  $coll := '/db/text-retriever/';

declare function local:get-project-name($u as xs:string) as xs:string
{
	let $c := replace($u,"^(.*/)([^/]*).git$","$2")
	return $c
};

let $list:=
for $d in collection($coll)//t:bibl[@copyOf]/@corresp
where contains(util:document-name($d),"collection-")
return <option>{string($d)}</option>

return
<select id="repo" name="repository"> {
for $item in distinct-values($list) 
   for $bibl in collection($coll)//t:bibl[@corresp = $item]
	let $val := $bibl/@copyOf
	let $tit := $bibl/t:title/text()
	return <option value="{local:get-project-name(string($val))}">{$tit}</option>
} </select>

