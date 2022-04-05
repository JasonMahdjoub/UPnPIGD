[![CodeQL](https://github.com/JasonMahdjoub/UpnpIGD/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/JasonMahdjoub/UpnpIGD/actions/workflows/codeql-analysis.yml)

This is a fork of Cling, the UPnP stack for Java and Android
------------------------------------------------------------

The fork fix some security issues, and upgrade used libraries. Efforts was only made into UPNP IGD part. The project's goals are strict specification compliance, complete, clean and extensive APIs, as well as rich SPIs for easy customization.

Cling is Free Software, distributed under the terms of the <a href="http://www.gnu.org/licenses/lgpl-2.1.html">GNU Lesser General Public License</a> <b>or at your option</b> the <a href="http://opensource.org/licenses/CDDL-1.0">Common Development and Distribution License</a>.

This repository is mainly implemented for MaDKitLanEdition

How to use it ?
---------------
### With Gradle :

Adapt into your build.gradle file, the next code :

```
	...
	dependencies {
		...
		api(group:'com.distrimind.upnp_igd', name: 'UPnPIGD', version: '1.0.0-STABLE')
		...
	}
	...
```	
When using UpnpIGD into Android, if you want to use UpnpIGD functions, please use this additionnal dependencies (minimum Java version is 11) :
```
	...
	dependencies {
		...
		var jettyVersion='11.+'
    		implementation 'org.eclipse.jetty:jetty-server:'+jettyVersion
		implementation 'org.eclipse.jetty:jetty-servlet:'+jettyVersion
		implementation 'org.eclipse.jetty:jetty-client:'+jettyVersion
		...
	}
	...
```	
Librairies are available on Maven Central. You can check signatures of dependencies with this [public GPG key](./gpg_key.asc). You can also use the next repository : 
```
	...
	repositories {
		...
		maven {
	       		url "https://artifactory.distri-mind.fr:443/artifactory/gradle-release/"
	   	}
		...
	}
	...
```
To know what is the last updaloed version, please refer to versions availables here : [this repository](https://artifactory.distri-mind.fr/artifactory/DistriMind-Public/com/distrimind/upnp_igd/UPnPIGD/)
### With Maven :
Adapt into your pom.xml file, the next code :
```
	...
	<project>
		...
		<dependencies>
			...
			<dependency>
				<groupId>com.distrimind.upnp_igd</groupId>
				<artifactId>UPnPIGD</artifactId>
				<version>1.0.0-STABLE</version>
			</dependency>
			...
		</dependencies>
		...
	</project>
	...
```
When using UpnpIGD into Android, if you want to use UpnpIGD functions, please use this additionnal dependencies (minimum Java version is 11) :
```
	...
	<dependency>
		<groupId>org.eclipse.jetty</groupId>
		<artifactId>jetty-server</artifactId>
		<version>11.0.8</version>
	</dependency>
	<dependency>
		<groupId>org.eclipse.jetty</groupId>
		<artifactId>jetty-servlet</artifactId>
		<version>11.0.8</version>
	</dependency>
	<dependency>
		<groupId>org.eclipse.jetty</groupId>
		<artifactId>jetty-client</artifactId>
		<version>11.0.8</version>
	</dependency>	
	...
```
Librairies are available on Maven Central. You can check signatures of dependencies with this [public GPG key](./gpg_key.asc). You can also use the next repository : 
```
	...
	<repositories>
		...
		<repository>
			<id>DistriMind-Public</id>
			<url>https://artifactory.distri-mind.fr:443/artifactory/gradle-release/</url>
		</repository>
		...
	</repositories>
	...		
```
To know what last version has been uploaded, please refer to versions availables into [this repository](https://artifactory.distri-mind.fr/artifactory/DistriMind-Public/com/distrimind/upnp_igd/UPnPIGD/)


