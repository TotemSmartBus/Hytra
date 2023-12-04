# Hytra

A Lightweight and Hybrid Framework for Conducting Fast Real-time and Historical Queries over Trajectory Data.

## Repository Overview

- Hytra-Interface: the global encoding engine of Hytra.
- Hytra-LSM: the hybrid storage engine of Hytra.
- transitnet: the back-end of the real-time bus trajectory data visualization platform and also the unified search engine of Hytra, where we implement the real-time range query, historical range query, real-time kNN query, and historical kNN query.
- transitnet-vue: the front-end of the real-time bus trajectory data visualization platform.

## Relationships Between Projects

![image-20230802181152370](https://github.com/TotemSmartBus/Hytra/blob/main/relationships.png)

- Hytra-Interface is a dependency project for the underlying LSM-tree index used by transitnet. It needs to be packaged into a jar file and placed under the lib folder of transitnet.
- Hytra-Interface and Hytra-LSM use socket to communicate.
- Transitnet provides underlying services to transitnet-vue, and transitnet-vue renders the interface to realize real-time bus trajectory data visualization and support interactive queries for users.

<!--Each project has its own .md file that describes their specific content and use.-->

## Features of Hytra

* Trajectory storage based on an LSM-tree-extended architecture called *Adjacency-based Compaction Tree* (ACT).
  * Key-value store.
  * Adjacency-based compaction policy.
  * Optimized storage architecture.
* Fast trajectory queries with
  * Enriched posting lists.
  * A search operator *SweepLine*.
  * A unified similarity measure *LOGC*.
  * A *shape-trip-schedule* three-level index for bus trajectory data.
* Bus trajectory visualization in New York City

## Supported Queries

Hytra is able to efficiently answer four typical types of queries over trajectory data now:

* Real-time range query
* Historical range query
* Real-time kNN query
* Historical kNN query

## Bus Trajectory Visualization

Link of the bus trajectory visualization platform: http://sheng.whu.edu.cn/bus/

[![transitnet](https://github.com/TotemSmartBus/Hytra/blob/main/platform.png)](http://sheng.whu.edu.cn/bus/)

## Getting Started

### 0. Download the NYC Dataset

Our trajectory dataset collected at New York City and Sydney is available. Please download it at https://drive.google.com/drive/folders/1lWYpBT27IudvCVryDTGZPHn2XUIXjWBl?usp=drive_link and put it in the root directory of the Hytra-interface project. 

### 1. Dependencies

We manage the dependent libraries with Maven. You can easily install those required softwares in pom.xml file.

### 2. Running the sample program

We provide a use case for Real-time Range Query. The `main()` method is in the `Engine` class in the Hytra-Interface project.

```java
//1. Set the parameters.     
Params.put("city","nyc");     
Params.put("spatialDomain", new double[]{40.502873,-74.252339,40.93372,-73.701241});        
Params.put("resolution",6);        
Params.put("separator", "@");        
Params.put("epsilon", 30);        
Params.put("dataSize",(int) 1.2e7);

//2. Initialize the encoder and the generator with parameters.
 Encoder.setup(Params);
Generator.setup(Params);

//3. Execute query.
buildTrajDB((String) Params.get("city"), "jun");
RealtimeRange.setup(trajDataBase,Params,3000);
RealtimeRange.hytra(PostingList.GT,PostingList.TlP);
```

Hytra-Interface provides simple APIs for query processing.

* `buildTrajDB()` loads/builds the index structure of a dataset, encapsulated by `PostingList` class.
* `RealtimeRange.setup()` initializes the query paraemeters, including the length of the spatial range (3000).
* `RealtimeRange.hytra()` invokes API of a real-time range query. APIs for other supported query types are described in the next section.

### Query Types

#### 1) Real-time range query

```java
HistoricalRange.generateQr(params, s_length, t_length);
HistoricalRange.hytra(planes);
```

The historical range query is used to retrive trajectories passing through a rectangular area and a period of time.

#### 2) Historical range query

```java
HistoricalRange.generateQr(params, s_length, t_length);
HistoricalRange.hytra(planes);
```

The historical range query is used to retrive trajectories passing through a rectangular area in a period of time.

#### 3) Real-time kNN query

```java
int k = 20;
realtimeKNNExpService.getTopKTrips(k);
```

The real-time kNN query is used to retrieve the k highest ranked trajectories based on the similarity to the query trajectory. 

#### 4) Historical kNN query

```java
int k = 10;
historicalKNNExpService.getTopKTrips(k);
```

The historical kNN query is used to retrive the k highest ranked trajectories based on the similarity to the query trajectory. 

## Future Directions

1. Support other advanced trajectory queries.
2. Develop a distributed version of Hytra for extremely large volumes of trajectory data.

