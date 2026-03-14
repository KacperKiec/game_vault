package io.kk.gameservice.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class APICallParams {
    HashMap<String, List<String>> paramsMap;

    public APICallParams() {
        paramsMap = new HashMap<>();
    }

    public void addParam(String filter, String value) {
        List<String> values = paramsMap.get(filter);
        if (values == null)
            values = new ArrayList<>();

        values.add(value);

        paramsMap.put(filter, values);
    }

    public void addParams(String filter, String[] values) {
        List<String> list = paramsMap.get(filter);
        if (list == null)
            list = new ArrayList<>();

        list.addAll(Arrays.stream(values).toList());

        paramsMap.put(filter, list);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        paramsMap.forEach((filter, values) -> {
            sb.append("&").append(filter).append("=").append(String.join(",", values));
        });

        return sb.toString();
    }
}
