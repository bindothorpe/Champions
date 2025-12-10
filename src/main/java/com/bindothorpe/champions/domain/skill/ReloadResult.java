package com.bindothorpe.champions.domain.skill;

import java.util.HashMap;
import java.util.Map;

public class ReloadResult {

    private final Map<ResultState, Integer> resultCount = new HashMap<>();

    public void addResult(ResultState state) {
        resultCount.computeIfAbsent(state, s -> 0);
        resultCount.put(state, resultCount.get(state) + 1);
    }

    public int getResultCount(ResultState state) {
        resultCount.computeIfAbsent(state, s -> 0);
        return resultCount.get(state);
    }




    public enum ResultState {
        SUCCESS("Success"),
        NOT_RELOADABLE("Not reloadable"),
        NOT_FOUND("Not found"),
        FAILED("Failed");

        private final String label;

        ResultState(String label) {
            this.label = label;
        }


        public String getLabel() {
            return label;
        }
    }
}
