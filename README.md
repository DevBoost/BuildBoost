# ![BuildBoost](BuildBoost.png) *BuildBoost*

BuildBoost is a build framework for Continuous Integration (CI) and Continuous Delivery (CD) that is highly extensible and focused on building OSGi bundles and Eclipse plug-ins.

## Your 3-step Build Setup\*

### (1) Get the BuildBoost master [build.xml](https://raw.githubusercontent.com/devboost/BuildBoost/master/Universal/de.devboost.buildboost.universal.build/boost/build.xml)

Just **[click](https://raw.githubusercontent.com/devboost/BuildBoost/master/Universal/de.devboost.buildboost.universal.build/boost/build.xml) and download**

or

<sup>`curl -LJO raw.githubusercontent.com/devboost/BuildBoost/master/Universal/de.devboost.buildboost.universal.build/boost/build.xml`</sup>

or

<sup>`wget -N raw.githubusercontent.com/devboost/BuildBoost/master/Universal/de.devboost.buildboost.universal.build/boost/build.xml`</sup>

### (2) Run it against your root repository\*\*

`ant -DrootRepository=git:git@github.com:DevBoost/EclipseSlideshow-Build.git`

### (3) Enjoy your ready built software

You are done!

## Where is the magic?

\* BuildBoost requires [Apache ANT](http://ant.apache.org) to be
installed. Based on which kinds of repositories you need to acces, you
might require a [GIT](http://git-scm.com) and/or
[SVN](http://subversion.apache.org) or installation.

\*\* BuildBoost fetches all stuff it needs from a set of repositories.
The **root repository** is passed as argument to the master build
script. The root repository itself can contain a **.repositories** file
which is a simple list of further repositories. Repositories contain
**Artifacts** (stuff which you want to process in the build: e.g. source
files, test cases, models, etc.) and **Build Stages** (things that do
something with the Artifacts: e.g. compilers, test runners, code
generators etc.).

## BuildBoost on GitHub

[https://github.com/DevBoost/BuildBoost](https://github.com/DevBoost/BuildBoost)