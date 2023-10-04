# The Anatomy of a Search Engine

Yaaay search engine bitches

The repository contains a gradle applications project template for completing Assignment number 2.

### Project Structure

```bash
src
└── main
│    ├── java
          ├── com.example.searchengine
│    │              ├── Crawler.java # Abstract class for all crawlers
│    │              └── IndexFlipper.java # map object class
│    │              └── MultithreadCrawler.java #Instantiation of a multithreaded crawler
│    │              └── SearchEngine.java #Implementation of a search engine
                    └── SearchEngineApplication.java #Main Spring class, this class should need to be modified.
                    └── SearchEngineProperties.java #Spring class to define the properties, this class should not need to be modified.
│    │              └── SimpleCrawler.java #Instantiation of a simple crawler (with only one thread)
│    ├──  resources
          ├── static
                └── index.html  # The main page of the search engine (TO COMPLETE)
│         └── index.csv  # CSV file to store the index generated by the crawler.
│         └── index_flipped.csv  # CSV file to store the flipped index generated by the index flipper.
│         └── search_engine_api.yaml  # OpenAPI description of the Search Engine (TO COMPLETE)
└── test
      ├── TestBase.java
      └── CrawlerTest.java
      └── SimpleCrawlerTest.java
      └── MultithreadCrawlerTest.java
      └── IndexFlipperTest.java
      └── SearcherTest.java

```

## Task 1

### Run the tests for the class SimpleCrawlerTest. You should have a sucessful build to pass the test.

On Linux:

```bash
./gradlew test --tests "SimpleCrawlerTest"

```

On MacOS:

```bash
.\gradlew test --tests "SimpleCrawlerTest"

```

On Windows:

```bash
.\gradlew test --tests "SimpleCrawlerTest"
```



## Task 2

### Run the tests for the class IndexFlipperTest, and then SearcherTest. You should have a sucessful build to pass the test.

On Linux:

```bash
./gradlew test --tests "IndexFlipperTest"
./gradlew test --tests "SearcherTestTest"
```

On MacOS:

```bash
.\gradlew test --tests "SimpleCrawlerTest"

```


On Windows:

```bash
.\gradlew test --tests "SimpleCrawlerTest"
```




## Task 3

### Run the search engine and test it from a browser. 

You should first configure the file application.properties in src/main/resources:

- server.port defines the port.
- crawler indicates the crawler to be used (simple for the SimpleCrawler and multithread for the MultithreadCrawler).
- crawl is a boolean indicating whether the search engine should crawl the environment when starting.


To access the search engine, go to the URL: "http://localhost:{PORT}/", where PORT is the specified PORT.
If the port is 80, you can just use: "http://localhost/".

On Linux:

```bash
./gradlew bootRun

```

On MacOS:

```bash
.\gradlew bootRun

```


On Windows:

```bash
.\gradlew bootRun

```





## Task 4

### Run the tests for the class MultithreadCrawlerTest. You should have a sucessful build to pass the test.

On Linux:

```bash
./gradlew test --tests "MultithreadCrawlerTest"

```

On MacOS:

```bash
.\gradlew test --tests "MultithreadCrawlerTest"

```


On Windows:

```bash
.\gradlew test --tests "MultithreadCrawlerTest"
```

