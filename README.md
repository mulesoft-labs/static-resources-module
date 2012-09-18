#Static Resources Module

## Introduction
The idea behind this module is to provide an easy way to host static resources.

## Module docs
http://mulesoft.github.com/static-resources-module/mule/static-resources-config.html

## FAQ
### Why not using HTTP Transport `static-resource-handler`?

  * I want to host resources that are packaged inside jar files. 
  * I don't want to polute a REST API with a docroot and files that will hardcode UI with Backend
  * I want a way to show that certain resources should be static: GAE has a way of doing this as can been here:

  ```yaml
handlers:
- url: /stylesheets
  static_dir: stylesheets
  ```
  in a near future it will be great that CloudHub detects the static files and host them separately (maybe upload the to S3 / CloudFront).

### I have some XYZ requirement different than yours, can I contribute or add something to it?
Yes, please! Add a pull request and we can go over it.

# Usage

Add the following requirement to your pom.xml:

```xml
<dependency>
    <groupId>org.mule.modules</groupId>
    <artifactId>mule-module-static-resources</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Add the following snippet inside your mule-config.xml:

```xml
<static-resources:serve basePackage="com/example/my/resource/path" defaultResource="hello.html" />
```

That will expose the contents of the resources stored in package com.example.my.resource.path. In case the user tries to access the root of the path, or the resource identified with `/` then the resource it should display is `hello.html`.


# Authors

  * Emiliano Lesende (@3miliano) - Rest Router Swagger UI (from where the static resource hosting code was taken)
  * Alberto Pose (@thepose) - Good Artists Borrow, Great Artists Steal
  * (Add your name here!)

# License
Copyright 2012 MuleSoft, Inc.

Licensed under the Common Public Attribution License (CPAL), Version 1.0.

### Happy Hacking!

