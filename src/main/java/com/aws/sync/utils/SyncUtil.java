package com.aws.sync.utils;

import com.aws.sync.entity.*;

import java.util.*;

public class SyncUtil {
    public static void handleIndividualSyncA(List<Double> time, List<List<Double>> individualSync, List<IndividualSyncA> idsResult, Long id) {
        HashMap<Integer, String> userList = new HashMap<>();
        int userCount = individualSync.size() > 0 ? individualSync.get(0).size() / 4 : 25;
        for (int i = 0; i < userCount; i++) {
            if (i < 10) {
                userList.put(i, "user0" + i);
            } else {
                userList.put(i, "user" + i);
            }
        }

        int len = Math.min(time.size(), individualSync.size());
        for (int i = 0; i < len; i++) {
            int index = 0;
            List<Double> sync = individualSync.get(i);
            int size = individualSync.get(i).size();
            int countUser = size / 4;
            int rateStart = size - countUser;
//            int rateStart = size * 2 / 3 + 1;
            for (int j = 0; j < size - countUser; j = j + 3) {
                idsResult.add(new IndividualSyncA(id, time.get(i), userList.get(index), sync.get(j), sync.get(j + 1), sync.get(j + 2), sync.get(rateStart++)));
                index++;
            }
        }
        System.out.println("debug");

    }

    public static void handleIndividualSyncV(List<Double> time, List<List<Double>> individualSync, List<IndividualSyncV> idsResult, Long id) {
        HashMap<Integer, String> userList = new HashMap<>();
        int userCount = individualSync.size() > 0 ? individualSync.get(0).size() / 4 : 25;
        for (int i = 0; i < userCount; i++) {
            if (i < 10) {
                userList.put(i, "user0" + i);
            } else {
                userList.put(i, "user" + i);
            }
        }

        int len = Math.min(time.size(), individualSync.size());
        for (int i = 0; i < len; i++) {
            int index = 0;
            List<Double> sync = individualSync.get(i);
            int size = individualSync.get(i).size();
            int countUser = size / 4;
            int rateStart = size - countUser;
//            int rateStart = size * 2 / 3 + 1;
            for (int j = 0; j < size - countUser; j = j + 3) {
                idsResult.add(new IndividualSyncV(id, time.get(i), userList.get(index), sync.get(j), sync.get(j + 1), sync.get(j + 2), sync.get(rateStart++)));
                index++;
            }
        }
    }

    public static void handleIndividualSyncR(List<Double> time, List<List<Double>> individualSync, List<IndividualSyncR> idsResult, Long id) {
        HashMap<Integer, String> userList = new HashMap<>();
        int userCount = individualSync.size() > 0 ? individualSync.get(0).size() / 4 : 25;
        for (int i = 0; i < userCount; i++) {
            if (i < 10) {
                userList.put(i, "user0" + i);
            } else {
                userList.put(i, "user" + i);
            }
        }
        int len = Math.min(time.size(), individualSync.size());
        for (int i = 0; i < len; i++) {
            int index = 0;
            List<Double> sync = individualSync.get(i);
            int size = individualSync.get(i).size();
            int countUser = size / 4;
            int rateStart = size - countUser;
//            int rateStart = size * 2 / 3 + 1;
            for (int j = 0; j < size - countUser; j = j + 3) {
                idsResult.add(new IndividualSyncR(id, time.get(i), userList.get(index), sync.get(j), sync.get(j + 1), sync.get(j + 2), sync.get(rateStart++)));
                index++;
            }
        }
        System.out.println("debug");
    }

    public static void handleIndividualSyncAll(List<IndividualSyncA> idsA,
                                               List<IndividualSyncV> idsV,
                                               List<IndividualSyncR> idsR,
                                               List<IndividualSync> ids
                                               ) {
        int len = Math.min(Math.min(idsA.size(), idsV.size()), idsR.size());

        for (int i = 0; i < len; i++) {
            IndividualSyncA individualSyncA = idsA.get(i);
            IndividualSyncV individualSyncV = idsV.get(i);
            IndividualSyncR individualSyncR = idsR.get(i);
            individualSyncA.getTime_ms();
            Double value = 0.0d;
            Double distance = 0.0d;
            int countV = 0;
            int countD = 0;
            if (!Double.isNaN(individualSyncA.getIndividual_sync())) {
                value += individualSyncA.getIndividual_sync();
                countV++;
            }
            if (!Double.isNaN(individualSyncV.getIndividual_sync())) {
                value += individualSyncV.getIndividual_sync();
                countV++;
            }
            if (!Double.isNaN(individualSyncR.getIndividual_sync())) {
                value += individualSyncR.getIndividual_sync();
                countV++;
            }

            if (!Double.isNaN(individualSyncA.getIndividual_distance())) {
                distance += individualSyncA.getIndividual_distance();
                countD++;
            }
            if (!Double.isNaN(individualSyncV.getIndividual_distance())) {
                distance += individualSyncV.getIndividual_distance();
                countD++;
            }
            if (!Double.isNaN(individualSyncR.getIndividual_distance())) {
                distance += individualSyncR.getIndividual_distance();
                countD++;
            }
            if (countV != 0 && countD != 0) {
                value /= countV;
                distance /= countD;
            } else {
                value = Double.NaN;
                distance = Double.NaN;
            }
            Double i_distance;
            if (!Double.isNaN(value)) {
                i_distance = 1 - (value + 1) / 2;
                i_distance = 1 / (1 + Math.pow(Math.E, -24 * (i_distance - 0.5)));
            } else {
                i_distance = Double.NaN;
            }
            ids.add(new IndividualSync(
                    individualSyncA.getMeeting_id(), individualSyncA.getUsers(), individualSyncA.getTime_ms(), value, distance, i_distance
            ));

        }

        System.out.println("debug");
    }

    public static List<UserDistance> handleIndividualDistance(List<IndividualSync> individualSyncAll,Long meetingID,Double team_distance) {
        HashMap<Integer, List<Double>> user_total = new HashMap<>();
        for (IndividualSync individualSync : individualSyncAll){
            user_total.computeIfAbsent(individualSync.getTime_ms().intValue(), k -> new ArrayList<>()).add(individualSync.getDistance());
        }
        List<UserDistance> userDistanceList = new ArrayList<>();
        for (Map.Entry<Integer, List<Double>> entry : user_total.entrySet()) {
            Integer time = entry.getKey();
            List<Double> value = entry.getValue();
            double distance = 0;
            int count = 0;
            for (Double v : value) {
                if(v != null && !Double.isNaN(v)){
                    distance += v;
                    count++;
                }
            }
            int label = 0;
            if(count != 0){
                distance /= count;
                distance = 1 / (1 + Math.pow(Math.E, -24 * (distance - 0.5)));
                label = distance < team_distance ? 1: 0;
            }else {
                distance = Double.NaN;
            }
            userDistanceList.add(new UserDistance(meetingID, distance, label, time));
        }
        return userDistanceList;
    }

    public static List<UserContribution> handleMyContribution(List<IndividualSync> individualSyncAll, Long meetingID) {

        Map<String, List<Double>> distanceMap = new TreeMap<>();
        for (IndividualSync score : individualSyncAll) {
            if(score.getDistance()!= null && !Double.isNaN(score.getDistance())){
                distanceMap.computeIfAbsent(score.getUsers(), k -> new ArrayList<>()).add(score.getDistance());
            }
        }

        Map<String, Double> averageDistanceMap = new TreeMap<>();
        for (Map.Entry<String, List<Double>> entry : distanceMap.entrySet()) {
            double average = computeAverage(entry.getValue());
            averageDistanceMap.put(entry.getKey(), average);
        }

        averageDistanceMap = averageDistanceMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);

        double factor = 1;
        Map<String,Double> amplifiedMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : averageDistanceMap.entrySet()) {
            amplifiedMap.put(entry.getKey(), entry.getValue() * factor);
            factor += 1;
        }

        Map<String, Double> softmaxMap = computeSoftmax(amplifiedMap);

        List<UserContribution> userContributionList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : softmaxMap.entrySet()) {
            userContributionList.add(new UserContribution(meetingID,entry.getKey(), entry.getValue()));
//            System.out.println("User: " + entry.getKey() + ", Proportion: " + entry.getValue());
        }
        return userContributionList;
    }

    public static double computeAverage(List<Double> list) {
        return list.stream().mapToDouble(d -> d).average().orElse(0.0);
    }

    public static Map<String, Double> computeSoftmax(Map<String, Double> input) {
        Map<String, Double> softmaxMap = new HashMap<>();
        double sum = 0.0;

        for (Map.Entry<String, Double> entry : input.entrySet()) {
            sum += Math.exp(entry.getValue());
        }

        for (Map.Entry<String, Double> entry : input.entrySet()) {
            softmaxMap.put(entry.getKey(), Math.exp(entry.getValue()) / sum);
        }

        return softmaxMap;
    }
}
