# Hytra-Interface

The global encoding engine for Hytra.

## Usage

This project is a dependency project that provides the underlying LSM-tree index used by TransitNet. It needs to be packaged into a jar file and placed under the lib of the TransitNet.

1. Pack the project

``` bash
mvn package
```

2. Move the package to TransitNet

``` bash
cp target/Hytra-Exp-1.0-SNAPSHOT.jar ${TRANSITNET_ROOT}/lib/
```

3. Start the TransitNet project

> See the TransitNet's README.md

## Code Examples

>
Excerpt from [RealtimeDataIndex.java from TransitNet](https://github.com/TotemSmartBus/transitnet/blob/master/src/main/java/whu/edu/cs/transitnet/service/index/RealtimeDataIndex.java)

```java
// A class is used here to manage all index related methods
public class RealtimeDataIndex {
    
    private LinkedList<HashMap<Integer, Vehicle>> pointToVehicle = new LinkedList<>();

    // Index Engine encapsulated by the underlying layer
    public EngineFactory engineFactory;

    // Initialize some configuration information by default
    public RealtimeDataIndex() {
        EngineParam params = new EngineParam(
                "nyc",
                new double[]{40.502873, -74.252339, 40.93372, -73.701241},
                6,
                "@",
                30,
                (int) 1.2e7
        );
        engineFactory = new EngineFactory(params);
    }

    // Update index
    public void update(List<Vehicle> list) {
        Thread t = new Thread(new IndexUpdater(list));
        t.start();
    }

    // Perform a kNN query on the underlying index
    public List<Vehicle> search(double lat, double lon, int k) {
        List<Integer> pidList = engineFactory.searchRealtime(lat, lon, k);
        List<Vehicle> result = new ArrayList<>(pidList.size());
        HashMap<Integer, Vehicle> newestMap = pointToVehicle.getLast();
        HashMap<Integer, Vehicle> newestButOneMap = pointToVehicle.get(pointToVehicle.size() - 2);
        // Maintain the latest 2 time slices
        for (Integer i : pidList) {
            if (newestMap.containsKey(i)) {
                result.add(newestMap.get(i));
            } else if (newestButOneMap.containsKey(i)) {
                result.add(newestButOneMap.get(i));
            } else {
                log.warn("The PID index searched by kNN does not exist in the latest two time slices. Has the data been updated?");
            }
        }
        return result;
    }

    // Update index asynchronously
    class IndexUpdater implements Runnable {
        private List<edu.whu.hytra.entity.Vehicle> newIndex;

        public IndexUpdater(List<Vehicle> newIndex) {
            // Maintain the map of PID and original data
            HashMap<Integer, Vehicle> pointMap = new HashMap<>();
            this.newIndex = newIndex.stream().map(i -> {
                edu.whu.hytra.entity.Vehicle j = new edu.whu.hytra.entity.Vehicle();
                j.setId(i.getId());
                j.setRecordedTime(i.getRecordedTime());
                j.setLat(i.getLat());
                j.setLon(i.getLon());
                j.setTripID(i.getTripID());
                pointMap.put(j.getPID(), i);
                return j;
            }).collect(Collectors.toList());
            pointToVehicle.add(pointMap);
            // Only save the latest 10 original time slices 
            // to prevent excessive memory usage
            if (pointToVehicle.size() > 10) {
                pointToVehicle.poll();
            }
        }

        @Override
        public void run() {
            engineFactory.updateIndex(newIndex);
        }
    }
}

```
