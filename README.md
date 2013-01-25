

maven-gitflow-plugin
====================

The [gitflow branching model](http://nvie.com/posts/a-successful-git-branching-model/) assumes that
when you create a release branch, the new version number will be merged back to the develop branch.
With maven, this is not entirely the case, due to the SNAPSHOT version scheme.

I wrote this maven plugin to avoid inevitable merges when you finish a release branch.

It does not use the maven release plugin. I don't miss it.

Usage
-----

**mvn gitflow:release**

This will:

1. Set project and dependency versions to be release versions on the develop branch, and commit this.
2. Set correct project and dependency versions on develop branch, and commit this.
3. Create the release branch from our first commit, which is set to be a release.

When you do a normal git flow release finish XXX, the release branch will merge to develop without any
complaint, as both branches have a common ancestry.

