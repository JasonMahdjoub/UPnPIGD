UPnP IGD
========

[![CodeQL](https://github.com/JasonMahdjoub/UPnPIGD/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/JasonMahdjoub/UPnPIGD/actions/workflows/codeql-analysis.yml)

This is a fork of UPnPIGD, the UPnP stack for Java and Android
------------------------------------------------------------

This project is a fork of [Cling](https://github.com/4thline/cling). It fixes some security issues, and upgrade used libraries. Efforts were only made into UPNP IGD part. The project's goals are strict specification compliance, complete, clean and extensive APIs, as well as rich SPIs for easy customization.

UPnPIGD is Free Software, distributed under the terms of the [GNU Lesser General Public License, version 2.1](https://www.gnu.org/licenses/lgpl-2.1.html).

How to use it ?
---------------
### With Gradle :

Adapt into your build.gradle file, the next code :

 - When using UPnPIGD into desktop environment, please add this dependency (minimum Java version is 11) :
    ```
	    ...
	    dependencies {
		    ...
		    implementation(group:'com.distrimind.upnp_igd.desktop', name: 'UPnPIGD-Desktop', version: '1.4.2-STABLE')
		    //optional :
		    implementation(group:'org.slf4j', name: 'slf4j-jdk14', version: '2.0.16')
		    ...
	    }
	    ...
    ```

 - When using UPnPIGD into android environment, please add this dependency (Android API version is 26) :

    ```
	    ...
	    dependencies {
		    ...
		    implementation(group:'com.distrimind.upnp_igd.android', name: 'UPnPIGD-Android', version: '1.4.2-STABLE')
		    ...
	    }
	    ...
    ```

 - Libraries are available on Maven Central. You can check signatures of dependencies with this [public GPG key](key-2023-10-09.pub). You can also use the next repository : 
    ```
        ...
        repositories {
            ...
            maven {
                    url "https://artifactory.distri-mind.fr/ui/native/gradle-release/"
            }
            ...
        }
        ...
    ```

To know what is the last uploaded version, please refer to versions available here : [this repository](https://artifactory.distri-mind.fr/ui/native/DistriMind-Public/com/distrimind/upnp_igd/UPnPIGD-Core/)
### With Maven :
Adapt into your pom.xml file, the next code :
 - When using UPnPIGD into desktop environment, please add this dependency (minimum Java version is 11) :
    ```
        ...
        <project>
            ...
            <dependencies>
                ...
                <dependency>
                    <groupId>com.distrimind.upnp_igd.desktop</groupId>
                    <artifactId>UPnPIGD-Desktop</artifactId>
                    <version>1.4.2-STABLE</version>
                </dependency>
                <-- optional -->
                <dependency>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-jdk14</artifactId>
                    <version>2.0.16</version>
                </dependency>   
                ...
            </dependencies>
            ...
        </project>
        ...
    ```
   
 - When using UPnPIGD into android environment, please add this dependency (minimum Android API version is 26) :
    ```
        ...
        <dependency>
            <groupId>com.distrimind.upnp_igd.android</groupId>
            <artifactId>UPnPIGD-Android</artifactId>
            <version>1.4.2-STABLE</version>
        </dependency>
        ...
    ```
   
 - Libraries are available on Maven Central. You can check signatures of dependencies with this [public GPG key](key-2023-10-09.pub). You can also use the next repository : 
    ```
        ...
        <repositories>
            ...
            <repository>
                <id>DistriMind-Public</id>
                <url>https://artifactory.distri-mind.fr/ui/native/gradle-release/</url>
            </repository>
            ...
        </repositories>
        ...		
    ```
To know what last version has been uploaded, please refer to versions available into [this repository](https://artifactory.distri-mind.fr/ui/native/DistriMind-Public/com/distrimind/upnp_igd/UPnPIGD-Core/)


