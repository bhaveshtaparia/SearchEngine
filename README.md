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

Let’s understand some concepts → you can connect the things easily.

   1. Analyzer → Just understand data in simple term → books have para, para have sentence , sentence have words. So analyzer splits the sentence in to words (tokenization), make it lowercase (case insensitive search work), remove stop word (like the, is). So analyzer is required to simply that data. → so lucene have multiple analyzer –and we are going to use Standard analyzer in this project.
   2. FSDirectory → So lucene store index data in file format (generally in disk) → so there is 2 implementation MMapDirectory , NIOFDirectory → used to help on reading fast, Internally use FSIndexOutput for writing .
   3. Codec → How to encode and decode file during writing or reading from disk. Define index storage format.
   4. Lucene Document Model → so in lucene we have document → which contain Fields. (later lucene if required convert field value in terms). So Fields is an important concept → there are different types of fields supported , if you need an analyzer in string use TextField, if you want to sort the number , then docsValuesexists. So in simple words lucene store the document.
   5. Imp concept → lucene create immutable segment from provided docs, for keyword search lucene use inverted index, posting list and many more files , which help to design the effective search algo. (lucene is read heavy storage engine – means give priority to read – based on that its design its algo)
   6. IndexWriter → Manage segments, apply segments merge , analyzer, codec....


---


## Architecture of the Project



```
┌─────────────────────────────────────────────────────────┐
│                    HTTP Layer                           │
│   IndexingController  ←  POST /api/v1/index            │
│   TestController      ←  GET  /test  (dev only)        │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                  Service Layer                          │
│   IndexingService                                       │
│   • orchestrates: build → getWriter → upsert → commit  │
└──────────┬─────────────────────────┬────────────────────┘
           │                         │
┌──────────▼──────────┐   ┌──────────▼──────────────────┐
│   Document Layer    │   │   Index Writer Layer        │
│   DocumentBuilder   │   │   IndexWriterManager        │
│   LuceneFieldMapper │   │   • ConcurrentHashMap cache │
│                     │   │   • one writer per index    │
│  ┌──────────────┐   │   │   • @PreDestroy shutdown    │
│  │FieldHandler  │   │   └─────────────────────────────┘
│  │  (Strategy)  │   │
│  ├──────────────┤   │
│  │ StringField  │   │
│  │ Handler      │   │
│  ├──────────────┤   │
│  │ NumericField │   │
│  │ Handler      │   │
│  └──────────────┘   │
└─────────────────────┘
           │
┌──────────▼──────────────────────────────────────────────┐
│                 Apache Lucene Core                      │
│   FSDirectory → IndexWriter → Document → Segments       │
└─────────────────────────────────────────────────────────┘
           │
┌──────────▼──────────────────────────────────────────────┐
│                    Filesystem                          │
│   indexes/{folderId}/{indexId}/   (Lucene segments)    │
└─────────────────────────────────────────────────────────┘
```




