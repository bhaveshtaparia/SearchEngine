# My Understanding 


## Spring Boot / Java Concepts Used in the Project

1. @springbootapplication --> this annotation is the combination of @configuration, @enableAutoConfigurtation and @componentScan. (Used in RagBackendApplication.java)
    1. @configuration --> You can define bean definitions using this annotation. (refer LuceneConfig.java)
    2. @enableAutoConfigurtation --> annotation auto configures the dependencies in classpath. (like it will configure the tomcat server if you have the dependency in classpath)
    3. @componentScan --> it will scan the components in the package and register then bean definition in the spring context. (like it will scan the controller, service and repository and register the bean definition in the spring context)
2. Rest endpoint -->
    1. @RestController --> it is used to create RESTful web services using Spring MVC. (Refer controller package). its combination of @Controller and @ResponseBody.
    2. @RequestMapping --> it is used to map the HTTP requests to the handler methods of MVC and REST controllers. (Refer controller package)
    3. @GetMapping, @postMapping --> it is used to handle the requests. (Refer controller package)
3. ConfigurationProperties --> it is used to bind the properties from the application.properties file to the Java class. (Refer LuceneProperties.java) 
4. @Validated , @NotBlank, @NotNull --> it is used to validate the properties. (Refer LuceneProperties.java)

---

## Lucene Concepts Used in the Project



---


## Architecture of the Project






