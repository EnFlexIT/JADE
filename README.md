
# JADE
This repository contains the original JADE code from the [SVN](https://jade.tilab.com/developers/source-repository/) of Telecom Italia
## Maven Central
The Code of the Master branch will be published as an JAR packaged as an OSGI-Bundle. You can find it on  [Maven Central](https://central.sonatype.com/artifact/de.enflexit.jade/de.enflexit.jade). If you want to include it via Maven, you can use this dependency:
```
<dependency>
    <groupId>de.enflexit.jade</groupId>
    <artifactId>de.enflexit.jade</artifactId>
    <version>4.6.1</version>
</dependency>
```
The workflow for publishing to Maven Central is fully automated through a Github action. You can find the action [here](https://github.com/EnFlexIT/JADE/blob/master/.github/workflows/release-to-mvn-central.yml). The Github action is triggered when a release is published.
## Branches
### Master
Contains our **changes and extensions** to the code of the [JADE](https://jade.tilab.com/) platform. The master branch will also include future changes made by Telecom Italia. 
### JADE-TILAB
This branch contains the original and **current** JADE code from the [SVN](https://jade.tilab.com/developers/source-repository/) of Telecom Italia. It will be updated frequently.
The update process and its documentation and scripts can be found in the [update folder](https://github.com/EnFlexIT/JADE/tree/master/Update) of this repository.
