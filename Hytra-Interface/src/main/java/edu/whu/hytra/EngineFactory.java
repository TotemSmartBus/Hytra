package edu.whu.hytra;

import edu.whu.hyk.Engine;
import edu.whu.hyk.encoding.Encoder;
import edu.whu.hyk.exp.RealtimekNN;
import edu.whu.hyk.merge.Generator;
import edu.whu.hyk.model.Point;
import edu.whu.hyk.model.PostingList;
import edu.whu.hytra.entity.Vehicle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class EngineFactory {
    public EngineParam params;

    private Engine engine;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public EngineFactory(EngineParam params) {
        this.params = params;
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("city", params.getCity());
        paramsMap.put("spatialDomain", params.getSpatialDomain());
        paramsMap.put("resolution", params.getResolution());
        paramsMap.put("separator", params.getSeperator());
        paramsMap.put("epsilon", params.getEpsilon());
        paramsMap.put("dataSize", params.getDataSize());
        Engine.Params = paramsMap;
        Encoder.setup(paramsMap);
        Generator.setup(paramsMap);

    }

    /**
     * 去引擎更新索引
     *
     * @param data 应用端传入的数据
     */
    public void updateIndex(List<Vehicle> data) {
        List<Point> parsedList = data.stream().map(p -> new Point(p.getPID(), p.getLat(), p.getLon(), formatter.format(p.getRecordedTime() * 1000), p.getTID())
        ).collect(Collectors.toList());
        Engine.buildIndex(parsedList);
    }

    /**
     * 去引擎更新索引
     *
     * @param data 应用端传入的数据
     */
    public void updateCTIndex(HashMap<String, ArrayList<String>> data) {
        Engine.buildCTIndex(data);
    }

    public void clearIndex() {
        Engine.clear();
    }
    /**
     * 对实时数据进行搜索，仅搜索 grid 上的数据,搜索到的是 PointID
     *
     * @param lat
     * @param lon
     * @param k   最近的 k 个单位
     * @return PointID List
     */
    public List<Integer> searchRealtime(double lat, double lon, int k) {
        int gid = Encoder.encodeGrid(lat, lon);
        Point query = new Point(lat, lon);
        RealtimekNN.setup(Engine.trajDataBase, Engine.Params, query, k);
        // 使用 hytra 方法去搜索
        return RealtimekNN.hytra(PostingList.GT);
    }

}
