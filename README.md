
gitflow-maven-plugin
====================

gitflow-maven-plugin avoids inevitable pom.xml merges when finishing a git-flow release branch
on a maven project.

A more substantial [plugin](https://bitbucket.org/atlassian/maven-jgitflow-plugin) now exists, based
on the maven-release-plugin.

This plugin does not use the maven release plugin, or try to emulate any of its functionality; it just
provides a replacement for *git flow release start* command that stops the need for pom merging later on, e.g.

    $ mvn gitflow:release-start
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building jgitflow 0.4-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    [INFO] 
    [INFO] --- gitflow-maven-plugin:0.6-SNAPSHOT:release-start (default-cli) @ jgitflow ---
    [INFO] 
    [INFO] Current development version is: 0.4-SNAPSHOT
    [INFO] 
    [INFO] Summary of actions:
    [INFO] - A new branch 'release/0.4' was created, based on 'develop'
    [INFO] - The project version in branch 'release/0.4' is '0.4'
    [INFO] - The project version in branch 'develop' is '0.5-SNAPSHOT'
    [INFO] - You are now on branch 'release/0.4'
    [INFO] 
    [INFO] Follow-up actions:
    [INFO] - Start committing last-minute fixes in preparing your release
    [INFO] 
    [INFO] When done, run:
    [INFO] - git flow release finish 0.4
    [INFO] 
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 2.853s
    [INFO] Finished at: Wed Apr 24 18:25:05 BST 2013
    [INFO] Final Memory: 8M/151M
    [INFO] ------------------------------------------------------------------------

*Fix bugs*

    $ git flow release finish 0.1


Problem
-------

The [gitflow branching model & git extention](http://nvie.com/posts/a-successful-git-branching-model/)
assumes that when you create a release branch, the new version number will cleanly merge back into the develop
branch. This is not the case with maven however, because the release version will always conflict with the new
SNAPSHOT version on the development branch.

Example of the problem
----------------------
A typical use of maven & gitflow would be:

The pom version in develop is **0.1-SNAPSHOT**. To make a 0.1 release you would:

1. **git flow release start 0.1**
2. *Change pom version to 0.1 from 0.1-SNAPSHOT on release/0.1*
3. *Change any dependencies in the pom to non snapshot versions on release/0.1*
4. **git commit -a -m "Remove SNAPSHOT references"**
5. **git checkout develop**
6. *Change pom version to 0.2-SNAPSHOT from 0.1-SNAPSHOT on develop*
7. **git commit -a -m "Bump version number"**

Any bug fixes for the 0.1 release are done, and then to finish the release:
	
8. **git flow release finish 0.1** - *This will result in conflicts in the pom*
9. *Fix conflict in pom on develop*
10. **git commit -a -m "Fixed version conflict"**
11. **git flow release finish 0.1** - *This will result in conflicts in the pom*
12. **git flow release finish 0.1** - *This will now work*

Using the plugin this would be:
-------------------------------
1. **mvn gitflow:release-start** - *Automatically increments the version number on
develop & release branch, and changes any SNAPSHOT references to release versions
on the release branch.*

Any bug fixes for the release are done, and then to finish the release:

2. **git flow release finish 0.1**

Install
-------

jgitflow must first be installed:

    git clone git@github.com:lukepfarrar/jgitflow.git
    cd jgitflow 
    git flow init -d
    mvn clean install
    cd ..

Followed by the plugin itself:

    git clone git@github.com:lukepfarrar/gitflow-maven-plugin.git
    cd gitflow-maven-plugin
    git flow init -d
    mvn clean install

As this is not in central a plugin group needs to be added to ~/.m2/settings.xml:

    <settings>
      <pluginGroups>
        <pluginGroup>uk.co.theboo</pluginGroup>
      </pluginGroups>
    </settings>

Usage
-----

The only useful goal at present is **mvn gitflow:release-start**

This will:

1. Set project and dependency versions to be release versions on the develop branch, and commit this.
2. Set correct project and dependency versions on develop branch, and commit this.
3. Create the release branch from our first commit, which is set to be a release.

When you do a normal git flow release finish XXX, the release branch will merge to develop without any
complaint, as both branches now have a common ancestry.

License
-------

Copyright 2013 Luke Farrar <luke at gmail dot com>

Licensed under the GNU General Public License, version 3. See LICENSE.TXT.

