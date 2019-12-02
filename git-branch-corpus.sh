#!/bin/bash

REPOS="public-adl-text-sources adl-text-sources other_tei_projects SKS_tei trykkefrihedsskrifter"

for repo in $REPOS ; do (

	cd $repo
	brnch=`git branch | grep installed_corpus|wc -l`
	if [ $brnch -gt 0 ]; then
	    echo $repo
	else
	    echo "branching $repo"
	    git branch installed_corpus
	fi
	
) done

