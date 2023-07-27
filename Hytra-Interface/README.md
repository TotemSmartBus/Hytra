# Hytra-Interface

hytra 存储的应用层

## 使用方法

本项目是提供给 [TransitNet](https://github.com/TotemSmartBus/transitnet) 使用的底层 LSM-tree 索引的依赖项目。需要打包成
jar 文件放置在该项目的 lib 下使用。

1. 打包：

``` bash
mvn package
```

2. 移动到 TransitNet 项目下

``` bash
cp target/Hytra-Exp-1.0-SNAPSHOT.jar ${TRANSITNET_ROOT}/lib/
```

3. 启动 TransitNet 项目

> 参见该项目的 README。

## 相关代码示例

>
摘自 [RealtimeDataIndex.java from TransitNet](https://github.com/TotemSmartBus/transitnet/blob/master/src/main/java/whu/edu/cs/transitnet/service/index/RealtimeDataIndex.java)
。

```java
// 这里使用一个类去管理所有索引相关方法
public class RealtimeDataIndex {

    // 向底层查询时仅返回 PID，对应的信息需要 map 回去查找。这里维护一个 hashmap 映射 PID 和 对应点的完整信息。
    private LinkedList<HashMap<Integer, Vehicle>> pointToVehicle = new LinkedList<>();

    // 底层封装的索引 Engine
    public EngineFactory engineFactory;

    // 默认初始化一些配置信息
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

    // 更新索引
    public void update(List<Vehicle> list) {
        Thread t = new Thread(new IndexUpdater(list));
        t.start();
    }

    // 在底层索引上进行 kNN 查询，传入的参数为坐标点和 k 的值
    public List<Vehicle> search(double lat, double lon, int k) {
        List<Integer> pidList = engineFactory.searchRealtime(lat, lon, k);
        List<Vehicle> result = new ArrayList<>(pidList.size());
        HashMap<Integer, Vehicle> newestMap = pointToVehicle.getLast();
        HashMap<Integer, Vehicle> newestButOneMap = pointToVehicle.get(pointToVehicle.size() - 2);
        // 这里维护最新的 2 个时间分片而不是一个是因为可能在查询的过程中数据已经刷新了，此时则可能在第二最新分片上才能找到数据。
        for (Integer i : pidList) {
            if (newestMap.containsKey(i)) {
                result.add(newestMap.get(i));
            } else if (newestButOneMap.containsKey(i)) {
                result.add(newestButOneMap.get(i));
            } else {
                log.warn("kNN 搜索到的 PID 索引在最新 2 个时间分片上不存在，是否数据已经更新了？");
            }
        }
        return result;
    }

    // 异步更新索引
    class IndexUpdater implements Runnable {
        private List<edu.whu.hytra.entity.Vehicle> newIndex;

        public IndexUpdater(List<Vehicle> newIndex) {
            // 更新索引时还要维护一下 pid 和原始数据的 map
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
            // 只保存最新 10 个原始时间分片防止内存占用过多
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
