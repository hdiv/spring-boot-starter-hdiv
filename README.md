[![Build Status](https://travis-ci.org/hdiv/spring-boot-starter-hdiv.svg)](https://travis-ci.org/hdiv/spring-boot-starter-hdiv)

spring-boot-starter-hdiv
========================

A spring-boot starter that simplifies the usage of the [Hdiv](https://hdivsecurity.com) library.

You only have to add this library to your Spring Boot project and the Hdiv filter will be registered for you.

### Maven Integration
Add one of the following dependency to your ``pom.xml`` file, depending on the view technology your project uses:

```
<dependency>
	<groupId>org.hdiv</groupId>
	<artifactId>spring-boot-starter-hdiv-thymeleaf</artifactId>
	<version>LATEST_VERSION</version>
	<type>pom</type>
</dependency>
```

```
<dependency>
	<groupId>org.hdiv</groupId>
	<artifactId>spring-boot-starter-hdiv-jsp</artifactId>
	<version>LATEST_VERSION</version>
	<type>pom</type>
</dependency>
```

Have a look at https://github.com/hdiv/spring-boot-sample-hdiv for a sample Spring Boot + HDIV application.
