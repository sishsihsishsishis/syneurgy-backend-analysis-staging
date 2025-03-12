package com.aws.sync.utils;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.Heatmap;
import com.aws.sync.vo.csv.AllUser;
import com.aws.sync.vo.csv.Score;
import com.aws.sync.entity.*;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class CsvUtil {

    public static void createFile(File file){
        if (file.exists()) {
//            System.out.println("File exists");
        } else {
//            System.out.println("File not exists, create it ...");
            //getParentFile() 获取上级目录(包含文件名时无法直接创建目录的)
            if (!file.getParentFile().exists()) {
//                System.out.println("not exists");
                //创建上级目录
                file.getParentFile().mkdirs();
            }
            try {
                //在上级目录里创建文件
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static Map<String,Object> handleResult(List<AllUser> user,int count){
        List<List<Object>> user00 = new ArrayList<>();
        List<List<Object>> user01 = new ArrayList<>();
        List<List<Object>> user02 = new ArrayList<>();
        List<List<Object>> user03 = new ArrayList<>();
        List<List<Object>> user04 = new ArrayList<>();
        List<List<Object>> user05 = new ArrayList<>();
        List<List<Object>> user06 = new ArrayList<>();
        List<List<Object>> user07 = new ArrayList<>();
        List<List<Object>> user08 = new ArrayList<>();
        List<List<Object>> user09 = new ArrayList<>();
        List<List<Object>> user10 = new ArrayList<>();
        List<List<Object>> user11 = new ArrayList<>();
        List<List<Object>> user12 = new ArrayList<>();
        List<List<Object>> user13 = new ArrayList<>();
        List<List<Object>> user14 = new ArrayList<>();
        List<List<Object>> user15 = new ArrayList<>();

        for (AllUser a : user) {
            switch (count){
                case 16:
                    user15.add(Arrays.asList(a.getTime_ms(),a.getUser15()));
                case 15:
                    user14.add(Arrays.asList(a.getTime_ms(),a.getUser14()));
                case 14:
                    user13.add(Arrays.asList(a.getTime_ms(),a.getUser13()));
                case 13:
                    user12.add(Arrays.asList(a.getTime_ms(),a.getUser12()));
                case 12:
                    user11.add(Arrays.asList(a.getTime_ms(),a.getUser11()));
                case 11:
                    user10.add(Arrays.asList(a.getTime_ms(),a.getUser10()));
                case 10:
                    user09.add(Arrays.asList(a.getTime_ms(),a.getUser09()));
                case 9:
                    user08.add(Arrays.asList(a.getTime_ms(),a.getUser08()));
                case 8:
                    user07.add(Arrays.asList(a.getTime_ms(),a.getUser07()));
                case 7:
                    user06.add(Arrays.asList(a.getTime_ms(),a.getUser06()));
                case 6:
                    user05.add(Arrays.asList(a.getTime_ms(),a.getUser05()));
                case 5:
                    user04.add(Arrays.asList(a.getTime_ms(),a.getUser04()));
                case 4:
                    user03.add(Arrays.asList(a.getTime_ms(),a.getUser03()));
                case 3:
                    user02.add(Arrays.asList(a.getTime_ms(),a.getUser02()));
                case 2:
                    user01.add(Arrays.asList(a.getTime_ms(),a.getUser01()));
                case 1:
                    user00.add(Arrays.asList(a.getTime_ms(),a.getUser00()));
            }
        }
        Map<String, Object> map = new HashMap<>();
        switch (count){
            case 16:
                map.put("user15",user15);
            case 15:
                map.put("user14",user14);
            case 14:
                map.put("user13",user13);
            case 13:
                map.put("user12",user12);
            case 12:
                map.put("user11",user11);
            case 11:
                map.put("user10",user10);
            case 10:
                map.put("user09",user09);
            case 9:
                map.put("user08",user08);
            case 8:
                map.put("user07",user07);
            case 7:
                map.put("user06",user06);
            case 6:
                map.put("user05",user05);
            case 5:
                map.put("user04",user04);
            case 4:
                map.put("user03",user03);
            case 3:
                map.put("user02",user02);
            case 2:
                map.put("user01",user01);
            case 1:
                map.put("user00",user00);
        }
        return map;
    }

    public Map<String, Object> process(List<AllUser> user, int count) throws NoSuchFieldException, IllegalAccessException {
        Map<String, List<List<Object>>> resultMap = new HashMap<>();

        for (int i = 0; i < count; i++) {
            List<List<Object>> userList = new ArrayList<>();
            for (AllUser a : user) {
                List<Object> data = new ArrayList<>();
                Field field = AllUser.class.getDeclaredField("user" + i);
                field.setAccessible(true);
                data.add(a.getTime_ms());
                data.add(field.get(a));
                userList.add(data);
            }
            resultMap.put("user" + i, userList);
        }

        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, List<List<Object>>> entry : resultMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }

    public static List<AResult> read_a(Long meetingID, List<String[]> data,List<String> userList) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<AResult> res = new ArrayList<>();
        String[] users = data.get(0);
        final int count = users.length - 3;
//        for (int i = 3; i < users.length; i++) {
//            userList.add(users[i]);
//        }
        for(int i = 0; i < count; i++){
            if(i < 10){
                userList.add("user0" + i);
            }else {
                userList.add("user" + i);
            }
        }
        for (int i = 0; i < data.size(); i++) {
            String[] rowData = data.get(i);
            AResult a = new AResult();
            a.setMeeting_id(meetingID);

            try {
                a.setTime_ms("".equals(rowData[0].trim()) ? null : Long.parseLong(rowData[0]));
                a.setA_mean("".equals(rowData[1].trim()) ? null : Double.parseDouble(rowData[1]));
                a.setA_std("".equals(rowData[2].trim()) ? null : Double.parseDouble(rowData[2]));

                // 从 User00 到 User14 或更多
                for (int userIndex = 3; userIndex <= 3 + count - 1; userIndex++) {
                    if (userIndex < rowData.length) {  // 确保不会超出数组界限
                        double userValue = "".equals(rowData[userIndex].trim()) ? null : Double.parseDouble(rowData[userIndex]);
                        Method method = AResult.class.getMethod("setUser" + String.format("%02d", userIndex - 3), Double.class);
                        method.invoke(a, userValue);
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("数字格式错误：" + e.getMessage());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                System.err.println("反射调用错误：" + e.getMessage());
            }
            res.add(a);
        }

//        for(int i = 0; i < data.size(); i++){
//            String[] rowData = data.get(i);
//            AResult a = new AResult();
//            a.setMeeting_id(meetingID);
//            a.setTime_ms("".equals(rowData[0].trim()) ? null : Long.parseLong(rowData[0]));
//            a.setA_mean("".equals(rowData[1].trim()) ? null : Double.parseDouble(rowData[1]));
//            a.setA_std("".equals(rowData[2].trim()) ? null :Double.parseDouble(rowData[2]));
//            switch (count){
//                case 15:
//                    a.setUser14("".equals(rowData[17].trim()) ? null : Double.parseDouble(rowData[17]));
//                case 14:
//                    a.setUser13("".equals(rowData[16].trim()) ? null : Double.parseDouble(rowData[16]));
//                case 13:
//                    a.setUser12("".equals(rowData[15].trim()) ? null : Double.parseDouble(rowData[15]));
//                case 12:
//                    a.setUser11("".equals(rowData[14].trim()) ? null : Double.parseDouble(rowData[14]));
//                case 11:
//                    a.setUser10("".equals(rowData[13].trim()) ? null : Double.parseDouble(rowData[13]));
//                case 10:
//                    a.setUser09("".equals(rowData[12].trim()) ? null : Double.parseDouble(rowData[12]));
//                case 9:
//                    a.setUser08("".equals(rowData[11].trim()) ? null : Double.parseDouble(rowData[11]));
//                case 8:
//                    a.setUser07("".equals(rowData[10].trim()) ? null : Double.parseDouble(rowData[10]));
//                case 7:
//                    a.setUser06("".equals(rowData[9].trim()) ? null : Double.parseDouble(rowData[9]));
//                case 6:
//                    a.setUser05("".equals(rowData[8].trim()) ? null : Double.parseDouble(rowData[8]));
//                case 5:
//                    a.setUser04("".equals(rowData[7].trim()) ? null : Double.parseDouble(rowData[7]));
//                case 4:
//                    a.setUser03("".equals(rowData[6].trim()) ? null : Double.parseDouble(rowData[6]));
//                case 3:
//                    a.setUser02("".equals(rowData[5].trim()) ? null : Double.parseDouble(rowData[5]));
//                case 2:
//                    a.setUser01("".equals(rowData[4].trim()) ? null : Double.parseDouble(rowData[4]));
//                case 1:
//                    a.setUser00("".equals(rowData[3].trim()) ? null : Double.parseDouble(rowData[3]));
//            }
//            res.add(a);
//
//        }
        return res;
    }

    public static List<VResult> read_v(Long meetingID,List<String[]> data) {
        List<VResult> res = new ArrayList<>();
        int count = data.get(0).length - 3;
        for (int i = 0; i < data.size(); i++) {
            String[] rowData = data.get(i);
            VResult v = new VResult();
            v.setMeeting_id(meetingID);

            try {
                v.setTime_ms("".equals(rowData[0].trim()) ? null : Long.parseLong(rowData[0]));
                v.setV_mean("".equals(rowData[1].trim()) ? null : Double.parseDouble(rowData[1]));
                v.setV_std("".equals(rowData[2].trim()) ? null : Double.parseDouble(rowData[2]));

                // 从 User00 到 User14 或更多
                for (int userIndex = 3; userIndex <= 3 + count - 1; userIndex++) {
                    if (userIndex < rowData.length) {  // 确保不会超出数组界限
                        double userValue = "".equals(rowData[userIndex].trim()) ? null : Double.parseDouble(rowData[userIndex]);
                        Method method = VResult.class.getMethod("setUser" + String.format("%02d", userIndex - 3), Double.class);
                        method.invoke(v, userValue);
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("数字格式错误：" + e.getMessage());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                System.err.println("反射调用错误：" + e.getMessage());
            }
            res.add(v);
        }

        return res;
    }

    public static List<RResult> read_r(Long meetingID,List<String[]> data) {
        List<RResult> res = new ArrayList<>();
        int count = data.get(0).length - 3;
        for (int i = 0; i < data.size(); i++) {
            String[] rowData = data.get(i);
            RResult r = new RResult();
            r.setMeeting_id(meetingID);

            try {
                r.setTime_ms("".equals(rowData[0].trim()) ? null : Long.parseLong(rowData[0]));
                r.setR_mean("".equals(rowData[1].trim()) ? null : Double.parseDouble(rowData[1]));
                r.setR_std("".equals(rowData[2].trim()) ? null : Double.parseDouble(rowData[2]));

                // 从 User00 到 User14 或更多
                for (int userIndex = 3; userIndex <= 3 + count - 1; userIndex++) {
                    if (userIndex < rowData.length) {  // 确保不会超出数组界限
                        double userValue = "".equals(rowData[userIndex].trim()) ? null : Double.parseDouble(rowData[userIndex]);
                        Method method = RResult.class.getMethod("setUser" + String.format("%02d", userIndex - 3), Double.class);
                        method.invoke(r, userValue);
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("数字格式错误：" + e.getMessage());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                System.err.println("反射调用错误：" + e.getMessage());
            }
            res.add(r);
        }
//        for(int i = 0; i <data.size(); i++){
//            Long time = Long.parseLong(data.get(i)[0]);
//            if((time - 40) % 20000 == 0){
//                String[] rowData = data.get(i);
//                RResult r = new RResult();
//                r.setMeeting_id(meetingID);
////            v.setV_result(Long.parseLong(String.valueOf(i)));
//                r.setTime_ms("".equals(rowData[0].trim()) ? null : Long.parseLong(rowData[0]));
//                r.setR_mean("".equals(rowData[1].trim()) ? null : Double.parseDouble(rowData[1]));
//                r.setR_std("".equals(rowData[2].trim()) ? null :Double.parseDouble(rowData[2]));
//                switch (count){
//                    case 15:
//                        r.setUser14("".equals(rowData[17].trim()) ? null : Double.parseDouble(rowData[17]));
//                    case 14:
//                        r.setUser13("".equals(rowData[16].trim()) ? null : Double.parseDouble(rowData[16]));
//                    case 13:
//                        r.setUser12("".equals(rowData[15].trim()) ? null : Double.parseDouble(rowData[15]));
//                    case 12:
//                        r.setUser11("".equals(rowData[14].trim()) ? null : Double.parseDouble(rowData[14]));
//                    case 11:
//                        r.setUser10("".equals(rowData[13].trim()) ? null : Double.parseDouble(rowData[13]));
//                    case 10:
//                        r.setUser09("".equals(rowData[12].trim()) ? null : Double.parseDouble(rowData[12]));
//                    case 9:
//                        r.setUser08("".equals(rowData[11].trim()) ? null : Double.parseDouble(rowData[11]));
//                    case 8:
//                        r.setUser07("".equals(rowData[10].trim()) ? null : Double.parseDouble(rowData[10]));
//                    case 7:
//                        r.setUser06("".equals(rowData[9].trim()) ? null : Double.parseDouble(rowData[9]));
//                    case 6:
//                        r.setUser05("".equals(rowData[8].trim()) ? null : Double.parseDouble(rowData[8]));
//                    case 5:
//                        r.setUser04("".equals(rowData[7].trim()) ? null : Double.parseDouble(rowData[7]));
//                    case 4:
//                        r.setUser03("".equals(rowData[6].trim()) ? null : Double.parseDouble(rowData[6]));
//                    case 3:
//                        r.setUser02("".equals(rowData[5].trim()) ? null : Double.parseDouble(rowData[5]));
//                    case 2:
//                        r.setUser01("".equals(rowData[4].trim()) ? null : Double.parseDouble(rowData[4]));
//                    case 1:
//                        r.setUser00("".equals(rowData[3].trim()) ? null : Double.parseDouble(rowData[3]));
//                }
//                res.add(r);
//            }
//
//
//        }
        return res;
    }

    // sync_name = "rppg" | "v" | "a"
    public static List<Async> get_and_save_sync_a(int window_length_s, String sync_name,List<String[]> data,
                                                  List<String []> sync_a,Long meetingID,List<IndividualSyncA> isa) throws IOException {
        List<Async> res = new ArrayList<>();
        String prefix = "syne" + meetingID;
        String write_file = prefix + "/" + sync_name + "_sync.csv";
        createFile(new File(write_file));
        int dataCols = data.get(0).length;
        List<Long> time_ms = new ArrayList<>(); // time in second
        for (int i = 0; i < data.size(); i++) {  // 从第一行开始
            time_ms.add(Long.parseLong(data.get(i)[0]));
        }
        int len_time_ms = time_ms.size(); // number of total samples
        List<List<Double>> data_user = new ArrayList<>(); // data from all users
        for (int i = 0; i < data.size(); i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 3; j < dataCols; j++) {
                row.add("".equals(data.get(i)[j]) ? Double.NaN : Double.parseDouble(data.get(i)[j]));
            }
            data_user.add(row);
        }
        Long interval = time_ms.get(1) - time_ms.get(0); // interval between samples (should be even,so only use the first)
        int num_window_length_points = (int) (1.0 * window_length_s / interval); // number of points in window length
        int num_total_syc = (int) (1.0 * time_ms.size() / num_window_length_points); // number of total sync scores
        if (len_time_ms > (num_total_syc * num_window_length_points)) { // 因为向下取整，可能少算最后一部分
            num_total_syc++;
        }
        CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter(write_file))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build();
        writer.writeNext(new String[]{"start_time", sync_name + "_sync"});
        for (int n = 0; n < num_total_syc; n++) {
            int start_point_ind = n * num_window_length_points;
            int end_point_ind = start_point_ind + num_window_length_points;
            if (end_point_ind > len_time_ms) {
                end_point_ind = len_time_ms; // 不能超过最后一个点
            }
            List<List<Double>> window_matrix_all_users = new ArrayList<>();
            for (int i = start_point_ind; i < end_point_ind; i++) {
                window_matrix_all_users.add(data_user.get(i)); // window 内所有 user 组成的 matrix
            }
            double corr_sum = 0; // 两两组合correlation的和
            double corr_count = 0; // 两两组合correlation的个数
            int num_users = window_matrix_all_users.get(0).size();

            for (int ind1 = 0; ind1 < num_users-1; ind1++) {
                for (int ind2 = ind1 + 1; ind2 < num_users; ind2++) {
                    List<Double> s1 = new ArrayList<>(); // user1的所有数据
                    List<Double> s2 = new ArrayList<>(); // user2的所有数据
                    for (List<Double> window_matrix_all_user : window_matrix_all_users) {
                        s1.add(window_matrix_all_user.get(ind1));
                        s2.add(window_matrix_all_user.get(ind2));
                    }
                    boolean allNaNs1 = true;
                    boolean allNaNs2 = true;
                    for (double d : s1) {
                        if (!Double.isNaN(d)) {
                            allNaNs1 = false;
                            break;
                        }
                    }
                    for (double d : s2) {
                        if (!Double.isNaN(d)) {
                            allNaNs2 = false;
                            break;
                        }
                    }
                    if (allNaNs1 || allNaNs2) // s1全为空 或s2全为空
                        break;
                    double s1_sum = 0; // 不能算所有值的mean,需要算两组都不是nan的位置的mean
                    double s2_sum = 0;
                    double sumss = 0;  // 分子为对应位置上的(值-mean)相乘再相加的和（即点乘）
                    double sum_s1s1 = 0; // 分母开平方前第一项
                    double sum_s2s2 = 0; // 分母开平方前第二项
                    int numss = 0; // 有多少对应位置两组都不为nan
                    for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                        double ss1 = s1.get(ind_s1); // user1的中的每一个数据
                        double ss2 = s2.get(ind_s1); // user2中对应位置的数据
                        if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                            s1_sum += ss1;
                            s2_sum += ss2;
                            numss++;
                        }
                    }
                    if (numss == 0)
                        break;
                    double s1_mean = s1_sum / numss;
                    double s2_mean = s2_sum / numss;
                    for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                        double ss1 = s1.get(ind_s1);
                        double ss2 = s2.get(ind_s1);
                        if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                            double ss1d = ss1 - s1_mean;
                            double ss2d = ss2 - s2_mean;
                            sumss += ss1d * ss2d;
                            sum_s1s1 += ss1d * ss1d;
                            sum_s2s2 += ss2d * ss2d;
                        }
                    }
                    if (sum_s1s1 != 0 && sum_s2s2 != 0) {
                        corr_sum += (sumss / Math.sqrt(sum_s1s1 * sum_s2s2));
                        corr_count += 1;
                    }
                }
            }
            if (corr_count != 0) {
                writer.writeNext(new String[]{
                        Long.toString(time_ms.get(start_point_ind)),
                        Double.toString(corr_sum / corr_count)});
                sync_a.add(new String[]{Long.toString(time_ms.get(start_point_ind)),
                                        Double.toString(corr_sum/corr_count)});
                Async async = new Async();
                async.setMeeting_id(meetingID);
                async.setStart_time(time_ms.get(start_point_ind));
                async.setA_sync(corr_sum / corr_count);
                res.add(async);
            } else {
                writer.writeNext(new String[]{Long.toString(time_ms.get(start_point_ind)), ""});
                sync_a.add(new String[]{Long.toString(time_ms.get(start_point_ind)),
                            ""});
                Async async = new Async();
                async.setMeeting_id(meetingID);
                async.setStart_time(time_ms.get(start_point_ind));
                async.setA_sync(null);
                res.add(async);
            }
        }

        List<Double> time = new ArrayList<>();
        List<List<Double>> individual_sync = new ArrayList<>();
        for (int n = 0; n < num_total_syc; n++) {
            int start_point_ind = n * num_window_length_points;
            int end_point_ind = start_point_ind + num_window_length_points;
            if (end_point_ind > len_time_ms) {
                end_point_ind = len_time_ms; // 不能超过最后一个点
            }
            List<List<Double>> window_matrix_all_users = new ArrayList<>();
            for (int i = start_point_ind; i < end_point_ind; i++) {
                window_matrix_all_users.add(data_user.get(i)); // window 内所有 user 组成的 matrix
            }
            int num_users = window_matrix_all_users.get(0).size();
            List<Double> tmp = new ArrayList<>();
            List<Double> rate = new ArrayList<>();
            List<Double> softmaxScore = new ArrayList<>();
            Double sum = 0.0;
            Double softmaxSum = 0.0;
            int count = 0;
            time.add(Double.valueOf(time_ms.get(start_point_ind)));
            for (int ind1 = 0; ind1 < num_users ; ind1++) {
                double corr_sum = 0; // 两两组合correlation的和
                double corr_count = 0; // 两两组合correlation的个数
                for (int ind2 = 0; ind2 < num_users; ind2++)
                    if(ind2 != ind1){
                        List<Double> s1 = new ArrayList<>(); // user1的所有数据
                        List<Double> s2 = new ArrayList<>(); // user2的所有数据
                        for (List<Double> window_matrix_all_user : window_matrix_all_users) {
                            s1.add(window_matrix_all_user.get(ind1));
                            s2.add(window_matrix_all_user.get(ind2));
                        }
                        boolean allNaNs1 = true;
                        boolean allNaNs2 = true;
                        for (double d : s1) {
                            if (!Double.isNaN(d)) {
                                allNaNs1 = false;
                                break;
                            }
                        }
                        for (double d : s2) {
                            if (!Double.isNaN(d)) {
                                allNaNs2 = false;
                                break;
                            }
                        }
                        if (allNaNs1 || allNaNs2) // s1全为空 或s2全为空
                            break;
                        double s1_sum = 0; // 不能算所有值的mean,需要算两组都不是nan的位置的mean
                        double s2_sum = 0;
                        double sumss = 0;  // 分子为对应位置上的(值-mean)相乘再相加的和（即点乘）
                        double sum_s1s1 = 0; // 分母开平方前第一项
                        double sum_s2s2 = 0; // 分母开平方前第二项
                        int numss = 0; // 有多少对应位置两组都不为nan
                        for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                            double ss1 = s1.get(ind_s1); // user1的中的每一个数据
                            double ss2 = s2.get(ind_s1); // user2中对应位置的数据
                            if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                                s1_sum += ss1;
                                s2_sum += ss2;
                                numss++;
                            }
                        }
                        if (numss == 0)
                            break;
                        double s1_mean = s1_sum / numss;
                        double s2_mean = s2_sum / numss;
                        for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                            double ss1 = s1.get(ind_s1);
                            double ss2 = s2.get(ind_s1);
                            if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                                double ss1d = ss1 - s1_mean;
                                double ss2d = ss2 - s2_mean;
                                sumss += ss1d * ss2d;
                                sum_s1s1 += ss1d * ss1d;
                                sum_s2s2 += ss2d * ss2d;
                            }
                        }
                        if (sum_s1s1 != 0 && sum_s2s2 != 0) {
                            corr_sum += (sumss / Math.sqrt(sum_s1s1 * sum_s2s2));
                            corr_count += 1;
                        }
                    }
                if (corr_count != 0) {

                    Double r = Math.pow(Math.E,-corr_sum / corr_count);
                    tmp.add((corr_sum / corr_count));
                    tmp.add(r);
                    rate.add(1.0/r);
                    sum += 1.0/r;

                    //individual score
                    Double sigmoid = 1.0 / (1 + Math.pow(Math.E,-corr_sum / corr_count));
                    tmp.add(sigmoid);
//                    Double softmax = Math.pow(Math.E,corr_sum / corr_count);
//                    softmaxSum += softmax;
//                    softmaxScore.add(softmax);

                    if(count == num_users - 1 ){
                        for (Double aDouble : rate) {
                            if(sum.isNaN()) {
                                tmp.add(Double.NaN);
                            }else {
                                tmp.add(aDouble / sum);

                            }
                        }

//                        for (Double sf : softmaxScore) {
//                            if(softmaxSum.isNaN()){
//                                tmp.add(Double.NaN);
//                            }else {
//                                tmp.add(sf / softmaxSum);
//                            }
//                        }
                    }
                    count++;
                } else {
                    tmp.add(Double.NaN);
                    tmp.add(Double.NaN);
                    tmp.add(Double.NaN);
                    rate.add(Double.NaN);
                    sum += Double.NaN;
                    if(count == num_users - 1 ){
                        for (Double aDouble : rate) {
                            tmp.add(Double.NaN);
                        }
//                        for (Double aDouble : softmaxScore) {
//                            tmp.add(Double.NaN);
//                        }
                    }
                    count++;
                }
            }
            individual_sync.add(tmp);
        }

        System.out.println("debug");
        SyncUtil.handleIndividualSyncA(time, individual_sync, isa, meetingID);
        System.out.println("debug");

        writer.close();
        return res;
    }

    public static List<Vsync> get_and_save_sync_v(int window_length_s, String sync_name,List<String[]> data,
                                                  List<String[]> sync_v,Long meetingID,List<IndividualSyncV> isv) throws IOException {
        List<Vsync> res = new ArrayList<>();
        String prefix = "syne" + String.valueOf(meetingID);
        String write_file = prefix + "/" + sync_name + "_sync.csv";
        createFile(new File(write_file));
        int dataCols = data.get(0).length;
        List<Long> time_ms = new ArrayList<>(); // time in second
        for (int i = 0; i < data.size(); i++) {  // 从第一行开始，跳过标题行
            time_ms.add(Long.parseLong(data.get(i)[0]));
        }
        int len_time_ms = time_ms.size(); // number of total samples
        List<List<Double>> data_user = new ArrayList<>(); // data from all users
        for (int i = 0; i < data.size(); i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 3; j < dataCols; j++) {
                row.add("".equals(data.get(i)[j]) ? Double.NaN : Double.parseDouble(data.get(i)[j]));
            }
            data_user.add(row);
        }
        Long interval = time_ms.get(1) - time_ms.get(0); // interval between samples (should be even,so only use the first)
        int num_window_length_points = (int) (1.0 * window_length_s / interval); // number of points in window length
        int num_total_syc = (int) (1.0 * time_ms.size() / num_window_length_points); // number of total sync scores
        if (len_time_ms > (num_total_syc * num_window_length_points)) { // 因为向下取整，可能少算最后一部分
            num_total_syc++;
        }
        CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter(write_file))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build();
        writer.writeNext(new String[]{"start_time", sync_name + "_sync"});
        for (int n = 0; n < num_total_syc; n++) {
            int start_point_ind = n * num_window_length_points;
            int end_point_ind = start_point_ind + num_window_length_points;
            if (end_point_ind > len_time_ms) {
                end_point_ind = len_time_ms; // 不能超过最后一个点
            }
            List<List<Double>> window_matrix_all_users = new ArrayList<>();
            for (int i = start_point_ind; i < end_point_ind; i++) {
                window_matrix_all_users.add(data_user.get(i)); // window 内所有 user 组成的 matrix
            }
            double corr_sum = 0; // 两两组合correlation的和
            double corr_count = 0; // 两两组合correlation的个数
            int num_users = window_matrix_all_users.get(0).size();

            for (int ind1 = 0; ind1 < num_users-1; ind1++) {
                for (int ind2 = ind1+1; ind2 < num_users; ind2++) {
                    List<Double> s1 = new ArrayList<>(); // user1的所有数据
                    List<Double> s2 = new ArrayList<>(); // user2的所有数据
                    for (List<Double> window_matrix_all_user : window_matrix_all_users) {
                        s1.add(window_matrix_all_user.get(ind1));
                        s2.add(window_matrix_all_user.get(ind2));
                    }
                    boolean allNaNs1 = true;
                    boolean allNaNs2 = true;
                    for (double d : s1) {
                        if (!Double.isNaN(d)) {
                            allNaNs1 = false;
                            break;
                        }
                    }
                    for (double d : s2) {
                        if (!Double.isNaN(d)) {
                            allNaNs2 = false;
                            break;
                        }
                    }
                    if (allNaNs1 || allNaNs2) // s1全为空 或s2全为空
                        break;
                    double s1_sum = 0; // 不能算所有值的mean,需要算两组都不是nan的位置的mean
                    double s2_sum = 0;
                    double sumss = 0;  // 分子为对应位置上的(值-mean)相乘再相加的和（即点乘）
                    double sum_s1s1 = 0; // 分母开平方前第一项
                    double sum_s2s2 = 0; // 分母开平方前第二项
                    int numss = 0; // 有多少对应位置两组都不为nan
                    for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                        double ss1 = s1.get(ind_s1); // user1的中的每一个数据
                        double ss2 = s2.get(ind_s1); // user2中对应位置的数据
                        if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                            s1_sum += ss1;
                            s2_sum += ss2;
                            numss++;
                        }
                    }
                    if (numss == 0)
                        break;
                    double s1_mean = s1_sum / numss;
                    double s2_mean = s2_sum / numss;
                    for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                        double ss1 = s1.get(ind_s1);
                        double ss2 = s2.get(ind_s1);
                        if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                            double ss1d = ss1 - s1_mean;
                            double ss2d = ss2 - s2_mean;
                            sumss += ss1d * ss2d;
                            sum_s1s1 += ss1d * ss1d;
                            sum_s2s2 += ss2d * ss2d;
                        }
                    }
                    if (sum_s1s1 != 0 && sum_s2s2 != 0) {
                        corr_sum += (sumss / Math.sqrt(sum_s1s1 * sum_s2s2));
                        corr_count += 1;
                    }
                }
            }
            if (corr_count != 0) {
                writer.writeNext(new String[]{
                        Long.toString(time_ms.get(start_point_ind)),
                        Double.toString(corr_sum / corr_count)});
                sync_v.add(new String[]{Long.toString(time_ms.get(start_point_ind)),
                        Double.toString(corr_sum/corr_count)});
                Vsync vsync = new Vsync();
                vsync.setMeeting_id(meetingID);
                vsync.setStart_time(time_ms.get(start_point_ind));
                vsync.setV_sync(corr_sum / corr_count);
                res.add(vsync);
            } else {
                writer.writeNext(new String[]{Long.toString(time_ms.get(start_point_ind)), ""});
                sync_v.add(new String[]{Long.toString(time_ms.get(start_point_ind)),
                        ""});
                Vsync vsync = new Vsync();
                vsync.setMeeting_id(meetingID);
                vsync.setStart_time(time_ms.get(start_point_ind));
                vsync.setV_sync(null);
                res.add(vsync);
            }
        }

        List<Double> time = new ArrayList<>();
        List<List<Double>> individual_sync = new ArrayList<>();
        for (int n = 0; n < num_total_syc; n++) {
            int start_point_ind = n * num_window_length_points;
            int end_point_ind = start_point_ind + num_window_length_points;
            if (end_point_ind > len_time_ms) {
                end_point_ind = len_time_ms; // 不能超过最后一个点
            }
            List<List<Double>> window_matrix_all_users = new ArrayList<>();
            for (int i = start_point_ind; i < end_point_ind; i++) {
                window_matrix_all_users.add(data_user.get(i)); // window 内所有 user 组成的 matrix
            }
            int num_users = window_matrix_all_users.get(0).size();
            List<Double> tmp = new ArrayList<>();
            List<Double> rate = new ArrayList<>();
            Double sum = 0.0;
            int count = 0;
            time.add(Double.valueOf(time_ms.get(start_point_ind)));
            for (int ind1 = 0; ind1 < num_users ; ind1++) {
                double corr_sum = 0; // 两两组合correlation的和
                double corr_count = 0; // 两两组合correlation的个数
                for (int ind2 = 0; ind2 < num_users; ind2++)
                    if(ind2 != ind1){
                        List<Double> s1 = new ArrayList<>(); // user1的所有数据
                        List<Double> s2 = new ArrayList<>(); // user2的所有数据
                        for (List<Double> window_matrix_all_user : window_matrix_all_users) {
                            s1.add(window_matrix_all_user.get(ind1));
                            s2.add(window_matrix_all_user.get(ind2));
                        }
                        boolean allNaNs1 = true;
                        boolean allNaNs2 = true;
                        for (double d : s1) {
                            if (!Double.isNaN(d)) {
                                allNaNs1 = false;
                                break;
                            }
                        }
                        for (double d : s2) {
                            if (!Double.isNaN(d)) {
                                allNaNs2 = false;
                                break;
                            }
                        }
                        if (allNaNs1 || allNaNs2) // s1全为空 或s2全为空
                            break;
                        double s1_sum = 0; // 不能算所有值的mean,需要算两组都不是nan的位置的mean
                        double s2_sum = 0;
                        double sumss = 0;  // 分子为对应位置上的(值-mean)相乘再相加的和（即点乘）
                        double sum_s1s1 = 0; // 分母开平方前第一项
                        double sum_s2s2 = 0; // 分母开平方前第二项
                        int numss = 0; // 有多少对应位置两组都不为nan
                        for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                            double ss1 = s1.get(ind_s1); // user1的中的每一个数据
                            double ss2 = s2.get(ind_s1); // user2中对应位置的数据
                            if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                                s1_sum += ss1;
                                s2_sum += ss2;
                                numss++;
                            }
                        }
                        if (numss == 0)
                            break;
                        double s1_mean = s1_sum / numss;
                        double s2_mean = s2_sum / numss;
                        for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                            double ss1 = s1.get(ind_s1);
                            double ss2 = s2.get(ind_s1);
                            if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                                double ss1d = ss1 - s1_mean;
                                double ss2d = ss2 - s2_mean;
                                sumss += ss1d * ss2d;
                                sum_s1s1 += ss1d * ss1d;
                                sum_s2s2 += ss2d * ss2d;
                            }
                        }
                        if (sum_s1s1 != 0 && sum_s2s2 != 0) {
                            corr_sum += (sumss / Math.sqrt(sum_s1s1 * sum_s2s2));
                            corr_count += 1;
                        }
                    }
                if (corr_count != 0) {
                    Double r = Math.pow(Math.E,-corr_sum / corr_count);
                    tmp.add((corr_sum / corr_count));
                    tmp.add(r);

                    //individual score
                    Double sigmoid = 1.0 / (1 + Math.pow(Math.E,-corr_sum / corr_count));
                    tmp.add(sigmoid);
                    rate.add(1.0/r);
                    sum += 1.0/r;
                    if(count == num_users -1 ){
                        for (Double aDouble : rate) {
                            if(sum.isNaN()) {
                                tmp.add(Double.NaN);
                            }else {
                                tmp.add(aDouble / sum);
                            }
                        }
                    }
                    count++;
                } else {
                    tmp.add(Double.NaN);
                    tmp.add(Double.NaN);
                    tmp.add(Double.NaN);
                    rate.add(Double.NaN);
                    sum += Double.NaN;
                    if(count == num_users -1 ){
                        for (Double aDouble : rate) {
                            tmp.add(Double.NaN);
                        }
                    }
                    count++;
                }
            }
            individual_sync.add(tmp);
        }

        SyncUtil.handleIndividualSyncV(time, individual_sync,isv,meetingID);
        writer.close();
        return res;
    }

    public static List<Rsync> get_and_save_sync_r(int window_length_s, String sync_name, List<String[]> data,
                                                  List<String[]> sync_r, Long meetingID, List<IndividualSyncR> isr) throws IOException {
        List<Rsync> res = new ArrayList<>();
        String prefix = "syne" + String.valueOf(meetingID);
        String write_file = prefix + "/" + sync_name + "_sync.csv";
        createFile(new File(write_file));
        int dataCols = data.get(0).length;
        List<Long> time_ms = new ArrayList<>(); // time in second
        for (int i = 0; i < data.size(); i++) {  // 从第一行开始，跳过标题行
            time_ms.add(Long.parseLong(data.get(i)[0]));
        }
        int len_time_ms = time_ms.size(); // number of total samples
        List<List<Double>> data_user = new ArrayList<>(); // data from all users
        for (int i = 0; i < data.size(); i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 3; j < dataCols; j++) {
                row.add("".equals(data.get(i)[j]) ? Double.NaN : Double.parseDouble(data.get(i)[j]));
            }
            data_user.add(row);
        }
        Long interval = time_ms.get(1) - time_ms.get(0); // interval between samples (should be even,so only use the first)
        int num_window_length_points = (int) (1.0 * window_length_s / interval); // number of points in window length
        int num_total_syc = (int) (1.0 * time_ms.size() / num_window_length_points); // number of total sync scores
        if (len_time_ms > (num_total_syc * num_window_length_points)) { // 因为向下取整，可能少算最后一部分
            num_total_syc++;
        }
        CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter(write_file))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build();
        writer.writeNext(new String[]{"start_time", sync_name + "_sync"});
        for (int n = 0; n < num_total_syc; n++) {
            int start_point_ind = n * num_window_length_points;
            int end_point_ind = start_point_ind + num_window_length_points;
            if (end_point_ind > len_time_ms) {
                end_point_ind = len_time_ms; // 不能超过最后一个点
            }
            List<List<Double>> window_matrix_all_users = new ArrayList<>();
            for (int i = start_point_ind; i < end_point_ind; i++) {
                window_matrix_all_users.add(data_user.get(i)); // window 内所有 user 组成的 matrix
            }
            double corr_sum = 0; // 两两组合correlation的和
            double corr_count = 0; // 两两组合correlation的个数
            int num_users = window_matrix_all_users.get(0).size();

            for (int ind1 = 0; ind1 < num_users-1; ind1++) {
                for (int ind2 = ind1+1; ind2 < num_users; ind2++) {
                    List<Double> s1 = new ArrayList<>(); // user1的所有数据
                    List<Double> s2 = new ArrayList<>(); // user2的所有数据
                    for (List<Double> window_matrix_all_user : window_matrix_all_users) {
                        s1.add(window_matrix_all_user.get(ind1));
                        s2.add(window_matrix_all_user.get(ind2));
                    }
                    boolean allNaNs1 = true;
                    boolean allNaNs2 = true;
                    for (double d : s1) {
                        if (!Double.isNaN(d)) {
                            allNaNs1 = false;
                            break;
                        }
                    }
                    for (double d : s2) {
                        if (!Double.isNaN(d)) {
                            allNaNs2 = false;
                            break;
                        }
                    }
                    if (allNaNs1 || allNaNs2) // s1全为空 或s2全为空
                        break;
                    double s1_sum = 0; // 不能算所有值的mean,需要算两组都不是nan的位置的mean
                    double s2_sum = 0;
                    double sumss = 0;  // 分子为对应位置上的(值-mean)相乘再相加的和（即点乘）
                    double sum_s1s1 = 0; // 分母开平方前第一项
                    double sum_s2s2 = 0; // 分母开平方前第二项
                    int numss = 0; // 有多少对应位置两组都不为nan
                    for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                        double ss1 = s1.get(ind_s1); // user1的中的每一个数据
                        double ss2 = s2.get(ind_s1); // user2中对应位置的数据
                        if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                            s1_sum += ss1;
                            s2_sum += ss2;
                            numss++;
                        }
                    }
                    if (numss == 0)
                        break;
                    double s1_mean = s1_sum / numss;
                    double s2_mean = s2_sum / numss;
                    for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                        double ss1 = s1.get(ind_s1);
                        double ss2 = s2.get(ind_s1);
                        if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                            double ss1d = ss1 - s1_mean;
                            double ss2d = ss2 - s2_mean;
                            sumss += ss1d * ss2d;
                            sum_s1s1 += ss1d * ss1d;
                            sum_s2s2 += ss2d * ss2d;
                        }
                    }
                    if (sum_s1s1 != 0 && sum_s2s2 != 0) {
                        corr_sum += (sumss / Math.sqrt(sum_s1s1 * sum_s2s2));
                        corr_count += 1;
                    }
                }
            }
            if (corr_count != 0) {
                writer.writeNext(new String[]{
                        Long.toString(time_ms.get(start_point_ind)),
                        Double.toString(corr_sum / corr_count)});
                sync_r.add(new String[]{Long.toString(time_ms.get(start_point_ind)),
                        Double.toString(corr_sum/corr_count)});
                Rsync rsync = new Rsync();
                rsync.setMeeting_id(meetingID);
                rsync.setStart_time(time_ms.get(start_point_ind));
                rsync.setR_sync(corr_sum / corr_count);
                res.add(rsync);
            } else {
                writer.writeNext(new String[]{Long.toString(time_ms.get(start_point_ind)), ""});
                sync_r.add(new String[]{Long.toString(time_ms.get(start_point_ind)),
                        ""});
                Rsync rsync = new Rsync();
                rsync.setMeeting_id(meetingID);
                rsync.setStart_time(time_ms.get(start_point_ind));
                rsync.setR_sync(null);
                res.add(rsync);
            }
        }
        writer.close();

        List<Double> time = new ArrayList<>();
        List<List<Double>> individual_sync = new ArrayList<>();
        for (int n = 0; n < num_total_syc; n++) {
            int start_point_ind = n * num_window_length_points;
            int end_point_ind = start_point_ind + num_window_length_points;
            if (end_point_ind > len_time_ms) {
                end_point_ind = len_time_ms; // 不能超过最后一个点
            }
            List<List<Double>> window_matrix_all_users = new ArrayList<>();
            for (int i = start_point_ind; i < end_point_ind; i++) {
                window_matrix_all_users.add(data_user.get(i)); // window 内所有 user 组成的 matrix
            }
            int num_users = window_matrix_all_users.get(0).size();
            List<Double> tmp = new ArrayList<>();
            List<Double> rate = new ArrayList<>();
            Double sum = 0.0;
            int count = 0;
            time.add(Double.valueOf(time_ms.get(start_point_ind)));
            for (int ind1 = 0; ind1 < num_users ; ind1++) {
                double corr_sum = 0; // 两两组合correlation的和
                double corr_count = 0; // 两两组合correlation的个数
                for (int ind2 = 0; ind2 < num_users; ind2++)
                    if(ind2 != ind1){
                        List<Double> s1 = new ArrayList<>(); // user1的所有数据
                        List<Double> s2 = new ArrayList<>(); // user2的所有数据
                        for (List<Double> window_matrix_all_user : window_matrix_all_users) {
                            s1.add(window_matrix_all_user.get(ind1));
                            s2.add(window_matrix_all_user.get(ind2));
                        }
                        boolean allNaNs1 = true;
                        boolean allNaNs2 = true;
                        for (double d : s1) {
                            if (!Double.isNaN(d)) {
                                allNaNs1 = false;
                                break;
                            }
                        }
                        for (double d : s2) {
                            if (!Double.isNaN(d)) {
                                allNaNs2 = false;
                                break;
                            }
                        }
                        if (allNaNs1 || allNaNs2) // s1全为空 或s2全为空
                            break;
                        double s1_sum = 0; // 不能算所有值的mean,需要算两组都不是nan的位置的mean
                        double s2_sum = 0;
                        double sumss = 0;  // 分子为对应位置上的(值-mean)相乘再相加的和（即点乘）
                        double sum_s1s1 = 0; // 分母开平方前第一项
                        double sum_s2s2 = 0; // 分母开平方前第二项
                        int numss = 0; // 有多少对应位置两组都不为nan
                        for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                            double ss1 = s1.get(ind_s1); // user1的中的每一个数据
                            double ss2 = s2.get(ind_s1); // user2中对应位置的数据
                            if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                                s1_sum += ss1;
                                s2_sum += ss2;
                                numss++;
                            }
                        }
                        if (numss == 0)
                            break;
                        double s1_mean = s1_sum / numss;
                        double s2_mean = s2_sum / numss;
                        for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                            double ss1 = s1.get(ind_s1);
                            double ss2 = s2.get(ind_s1);
                            if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                                double ss1d = ss1 - s1_mean;
                                double ss2d = ss2 - s2_mean;
                                sumss += ss1d * ss2d;
                                sum_s1s1 += ss1d * ss1d;
                                sum_s2s2 += ss2d * ss2d;
                            }
                        }
                        if (sum_s1s1 != 0 && sum_s2s2 != 0) {
                            corr_sum += (sumss / Math.sqrt(sum_s1s1 * sum_s2s2));
                            corr_count += 1;
                        }
                    }
                if (corr_count != 0) {
                    Double r = Math.pow(Math.E,-corr_sum / corr_count);
                    tmp.add((corr_sum / corr_count));
                    tmp.add(r);
                    //individual score
                    Double sigmoid = 1.0 / (1 + Math.pow(Math.E,-corr_sum / corr_count));
                    tmp.add(sigmoid);
                    rate.add(1.0/r);
                    sum += 1.0/r;
                    if(count == num_users -1 ){
                        for (Double aDouble : rate) {
                            if(sum.isNaN()) {
                                tmp.add(Double.NaN);
                            }else {
                                tmp.add(aDouble / sum);

                            }
                        }
                    }
                    count++;
                } else {
                    tmp.add(Double.NaN);
                    tmp.add(Double.NaN);
                    tmp.add(Double.NaN);
                    rate.add(Double.NaN);
                    sum += Double.NaN;
                    if(count == num_users -1 ){
                        for (Double aDouble : rate) {
                            tmp.add(Double.NaN);
                        }
                    }
                    count++;
                }
            }
            individual_sync.add(tmp);
        }
        SyncUtil.handleIndividualSyncR(time, individual_sync, isr, meetingID);
        return res;
    }

    public static List<String[]> get_sync_brain(int window_length_s, List<String[]> data) {
        List<String[]> sync_r = new ArrayList<>();
        int dataCols = data.get(0).length;
        List<Long> time_ms = new ArrayList<>(); // time in second
        for (int i = 0; i < data.size(); i++) {
            time_ms.add(Long.parseLong(data.get(i)[0]));
        }
        int len_time_ms = time_ms.size(); // number of total samples
        List<List<Double>> data_user = new ArrayList<>(); // data from all users
        for (int i = 0; i < data.size(); i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 3; j < dataCols; j++) {
                row.add("".equals(data.get(i)[j]) ? Double.NaN : Double.parseDouble(data.get(i)[j]));
            }
            data_user.add(row);
        }
        Long interval = time_ms.get(1) - time_ms.get(0); // interval between samples (should be even,so only use the first)
        int num_window_length_points = (int) (1.0 * window_length_s / interval); // number of points in window length
        int num_total_syc = (int) (1.0 * time_ms.size() / num_window_length_points); // number of total sync scores
        if (len_time_ms > (num_total_syc * num_window_length_points)) { // 因为向下取整，可能少算最后一部分
            num_total_syc++;
        }

        for (int n = 0; n < num_total_syc; n++) {
            int start_point_ind = n * num_window_length_points;
            int end_point_ind = start_point_ind + num_window_length_points;
            if (end_point_ind > len_time_ms) {
                end_point_ind = len_time_ms; // 不能超过最后一个点
            }
            List<List<Double>> window_matrix_all_users = new ArrayList<>();
            for (int i = start_point_ind; i < end_point_ind; i++) {
                window_matrix_all_users.add(data_user.get(i)); // window 内所有 user 组成的 matrix
            }
            double corr_sum = 0; // 两两组合correlation的和
            double corr_count = 0; // 两两组合correlation的个数
            int num_users = window_matrix_all_users.get(0).size();

            for (int ind1 = 0; ind1 < num_users - 1; ind1++) {
                for (int ind2 = ind1 + 1; ind2 < num_users; ind2++) {
                    List<Double> s1 = new ArrayList<>(); // user1的所有数据
                    List<Double> s2 = new ArrayList<>(); // user2的所有数据
                    for (List<Double> window_matrix_all_user : window_matrix_all_users) {
                        s1.add(window_matrix_all_user.get(ind1));
                        s2.add(window_matrix_all_user.get(ind2));
                    }
                    boolean allNaNs1 = true;
                    boolean allNaNs2 = true;
                    for (double d : s1) {
                        if (!Double.isNaN(d)) {
                            allNaNs1 = false;
                            break;
                        }
                    }
                    for (double d : s2) {
                        if (!Double.isNaN(d)) {
                            allNaNs2 = false;
                            break;
                        }
                    }
                    if (allNaNs1 || allNaNs2) // s1全为空 或s2全为空
                        break;
                    double s1_sum = 0; // 不能算所有值的mean,需要算两组都不是nan的位置的mean
                    double s2_sum = 0;
                    double sumss = 0;  // 分子为对应位置上的(值-mean)相乘再相加的和（即点乘）
                    double sum_s1s1 = 0; // 分母开平方前第一项
                    double sum_s2s2 = 0; // 分母开平方前第二项
                    int numss = 0; // 有多少对应位置两组都不为nan
                    for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                        double ss1 = s1.get(ind_s1); // user1的中的每一个数据
                        double ss2 = s2.get(ind_s1); // user2中对应位置的数据
                        if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                            s1_sum += ss1;
                            s2_sum += ss2;
                            numss++;
                        }
                    }
                    if (numss == 0)
                        break;
                    double s1_mean = s1_sum / numss;
                    double s2_mean = s2_sum / numss;
                    for (int ind_s1 = 0; ind_s1 < s1.size(); ind_s1++) {
                        double ss1 = s1.get(ind_s1);
                        double ss2 = s2.get(ind_s1);
                        if (!Double.isNaN(ss1) && !Double.isNaN(ss2)) {
                            double ss1d = ss1 - s1_mean;
                            double ss2d = ss2 - s2_mean;
                            sumss += ss1d * ss2d;
                            sum_s1s1 += ss1d * ss1d;
                            sum_s2s2 += ss2d * ss2d;
                        }
                    }
                    if (sum_s1s1 != 0 && sum_s2s2 != 0) {
                        corr_sum += (sumss / Math.sqrt(sum_s1s1 * sum_s2s2));
                        corr_count += 1;
                    }
                }
            }
            if (corr_count != 0) {
                sync_r.add(new String[]{Long.toString(time_ms.get(start_point_ind)),
                        Double.toString(corr_sum / corr_count)});
            } else {
                sync_r.add(new String[]{Long.toString(time_ms.get(start_point_ind)),
                        ""});
            }
        }

        return sync_r;
    }
    public static void get_team_top3(int windowLengthS, List<String[]> rppg_sync_data,
                                     List<String[]> v_sync_data,List<String[]> a_sync_data,
                                     List<Long> time,List<Integer> label) {

        // 统一长度
        int minLen = Math.min(rppg_sync_data.size(), Math.min(v_sync_data.size(), a_sync_data.size()));

        String[] rppg_sync_array = new String[minLen];
        String[] v_sync_array = new String[minLen];
        String[] a_sync_array = new String[minLen];
        Long[] time_va_sync = new Long[minLen];

        int j = 0;
        for(int i = 0; i < minLen; i++){
            if((!"".equals(rppg_sync_data.get(i)[1]))&&(!"".equals(v_sync_data.get(i)[1]))&&(!"".equals(a_sync_data.get(i)[1]))){
                rppg_sync_array[j] = rppg_sync_data.get(i)[1];
                time_va_sync[j] = Long.parseLong(v_sync_data.get(i)[0]);
                v_sync_array[j] = v_sync_data.get(i)[1];
                a_sync_array[j] = a_sync_data.get(i)[1];
                j++;
            }
        }

        Double[] all_sync = new Double[j];
        for(int i = 0; i < j; i++){
            all_sync[i] = Double.parseDouble(rppg_sync_array[i]) + Double.parseDouble(v_sync_array[i]) + Double.parseDouble(a_sync_array[i]);
        }



        //top3
        MininumHeap maximumHeap = new MininumHeap(3);
        for(int i = 0; i < all_sync.length; i++){
            maximumHeap.addElement(new TreeNode(i, all_sync[i]));
        }

        TreeNode[] array = maximumHeap.getArray();
        Long[] team_top3_start_time = new Long[3];
        Integer[] ids = new Integer[3];
        for(int i = 0; i < 3; i++){
            if(array[i] != null){
                team_top3_start_time[i] = time_va_sync[array[i].id];
                ids[i] = array[i].id;
            }
        }
        time.add(team_top3_start_time[0]);
        time.add(team_top3_start_time[1]);
        time.add(team_top3_start_time[2]);
        // 打印结果
        // System.out.println("team top 3 sections start time: " + team_top3_start_time[0]+","+team_top3_start_time[1]+","+team_top3_start_time[2]);
        //TODO: compute team label
        //team label
        Double[] a = new Double[j];
        Double[] v = new Double[j];
        Double[] rppg = new Double[j];
        for(int i = 0; i < j; i++){
            a[i] = Double.parseDouble(a_sync_array[i]);
            v[i] = Double.parseDouble(v_sync_array[i]);
            rppg[i] = Double.parseDouble(rppg_sync_array[i]);
        }

        Double[][]  topSync = new Double[3][3];
        for(int i = 0; i < 3; i++){
            if(ids[i] != null && a[ids[i]] != null){
                topSync[i][0] = a[ids[i]];
                topSync[i][1] = v[ids[i]];
                topSync[i][2] = rppg[ids[i]];
            }

        }
        //求 75%
        Arrays.sort(a);
        Arrays.sort(v);
        Arrays.sort(rppg);

        Double ha = a[j * 3 / 4];
        Double hv = v[j * 3 / 4];
        Double hr = rppg[j * 3 / 4];

//        List<Integer> label = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            Double[] sync = topSync[i];
            System.out.println(sync[0]);
            System.out.println(sync[1]);
            System.out.println(sync[2]);
            //TODO: 处理空指针问题
            if (sync[0] != null && sync[1] != null && sync[2] != null) {
                label.add((sync[0] >= ha ? 0 : 1) + (sync[1] >= hv ? 0 : 2) + (sync[2] >= hr ? 0 : 4));
            }
        }

    }

    public static List<Integer> get_top3_ind(List<Double> total_diff_abs){
        if (total_diff_abs.size() < 3) return new ArrayList<Integer>();
        List<Integer> res = new ArrayList<>();
        MininumHeap maximumHeap = new MininumHeap(3);
        for(int i = 0; i < total_diff_abs.size(); i++){
            maximumHeap.addElement(new TreeNode(i, total_diff_abs.get(i)));
        }
        TreeNode[] array = maximumHeap.getArray();
        for (int i = 0; i < 3 && i < total_diff_abs.size(); i++) {
            res.add(array[i].id + 1);
        }
//        res.add(array[0].id + 1);
//        res.add(array[1].id + 1);
//        res.add(array[2].id + 1);
        return res;
    }

    public static List<List<Double>> norm_min_max(List<List<Double>> matrix) {
        int num_user = matrix.size();
        List<List<Double>> norm_data = new ArrayList<>();

        for (int ind = 0; ind < num_user; ind++) {
            List<Double> user_data = matrix.get(ind);
            double user_data_min = 0.0d;
            double user_data_max = 0.0d;
            for (Double user_datum : user_data) {
                if(!Double.isNaN(user_datum)){
                    user_data_min = user_datum;
                    user_data_max = user_datum;
                    break;
                }
            }
            for (double d : user_data) {
                if (d != Double.NaN && d < user_data_min) {
                    user_data_min = d;
                }
            }
            for (double d : user_data) {
                if (d != Double.NaN && d > user_data_max) {
                    user_data_max = d;
                }
            }
            List<Double> norm_datum = new ArrayList<>();
            double range = user_data_max - user_data_min;
            for (double d : user_data) {
                norm_datum.add((d - user_data_min) / range);
            }
            norm_data.add(norm_datum);
        }
        return norm_data;
    }

    private static double std(List<Integer> list) {
        double mean = 0.0;
        for (int i : list) {
            mean += i;
        }
        mean /= list.size();
        double variance = 0.0;
        for (int i : list) {
            variance += (Math.pow((1.0 * i - mean), 2));
        }
        return Math.sqrt(variance / list.size());
    }

    public static List<List<Double>> abs2DMatrix(List<List<Double>> matrix) {
        List<List<Double>> a = new ArrayList<>();
        for (int i = 0; i < matrix.size(); i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 0; j < matrix.get(0).size(); j++) {
                if (Double.isNaN(matrix.get(i).get(j))) {
                    row.add(Double.NaN);
                } else {
                    row.add(matrix.get(i).get(j) < 0 ? -matrix.get(i).get(j) : matrix.get(i).get(j));
                }
            }
            a.add(row);
        }
        return a;
    }
    // 计算二维矩阵中每一列的平均值，忽视NaN
    private static List<Double> nanmeanAxis0(List<List<Double>> matrix) {
        int cnt;
        double sum;
        List<Double> means = new ArrayList<>();
        for (int col = 0; col < matrix.get(0).size(); col++) {
            cnt = 0;
            sum = 0.0;
            for (int row = 0; row < matrix.size(); row++) {
                if (!Double.isNaN(matrix.get(row).get(col))) {
                    cnt++;
                    sum += matrix.get(row).get(col);
                }
            }
            means.add(cnt == 0 ? Double.NaN : sum / cnt);
        }
        return means;
    }

    //TODO: 检查len_time_ms > end 是否需要改动
    public static List<List<Double>> get_hrv_diff(int window_length_s,List<String[]> rppg_data) throws IOException {
        int dataCols = rppg_data.get(0).length;
        List<Double> time_ms = new ArrayList<>();
        for (int i = 0; i < rppg_data.size(); i++) {
            time_ms.add(Double.parseDouble(rppg_data.get(i)[0]));
        }
        int len_time_ms = time_ms.size();
        double interval = time_ms.get(1) - time_ms.get(0);
        int num_window_length_points = (int) (1.0 * window_length_s / interval);
        int num_hrv = (int) (1.0 * len_time_ms / num_window_length_points);
        if (len_time_ms > (num_hrv * num_window_length_points)) {
            num_hrv++;
        }
        List<List<Double>> rppg_array_users = new ArrayList<>();
        for (int i = 0; i < rppg_data.size(); i++) {
            List<Double> rppg_array_user = new ArrayList<>();
            for (int j = 3; j < dataCols; j++) {
                rppg_array_user.add("".equals(rppg_data.get(i)[j].trim()) ? Double.NaN : Double.parseDouble(rppg_data.get(i)[j]));
            }
            rppg_array_users.add(rppg_array_user);
        }
        int num_user = rppg_array_users.get(0).size();
        List<List<Double>> hrv_matrix_all_user = new ArrayList<>();

        for (int user_id = 0; user_id < num_user; user_id++) {
            List<Double> rppg_user = new ArrayList<>();
            for (int r = 0; r < rppg_array_users.size(); r++) {
                rppg_user.add(rppg_array_users.get(r).get(user_id));
            }
            List<Integer> peaks_inds = new ArrayList<>();
            for (int i = 1; i <= rppg_user.size()-2; i++) {
                if (rppg_user.get(i) > rppg_user.get(i-1) && rppg_user.get(i) > rppg_user.get(i+1)) {
                    peaks_inds.add(i);
                }
            }
            List<Integer> rr_intervals = new ArrayList<>();
            List<Integer> rr_start_inds = new ArrayList<>();
            for (int i = 1; i < peaks_inds.size(); i++) {
                rr_intervals.add(peaks_inds.get(i) - peaks_inds.get(i-1));
                rr_start_inds.add(peaks_inds.get(i));
            }
            double rr_median;
            if(rr_intervals.size() < 2) {
                continue;
            }
            if (rr_intervals.size() % 2 == 1) {
                rr_median = rr_intervals.get(rr_intervals.size() / 2);
            } else {
                rr_median = 1.0 * ((rr_intervals.get(rr_intervals.size() / 2) +
                        rr_intervals.get(rr_intervals.size() / 2 - 1))) / 2;
            }
            List<Integer> rr_intervals_no_outlier = new ArrayList<>();
            List<Integer> rr_start_inds_no_outlier = new ArrayList<>();
            for (int ii = 0; ii < rr_intervals.size(); ii++) {
                int rr_interval = rr_intervals.get(ii);
                if (rr_interval > 0.1 * rr_median && rr_interval < 5 * rr_median) {
                    rr_intervals_no_outlier.add(rr_interval);
                    rr_start_inds_no_outlier.add(rr_start_inds.get(ii));
                }
            }
            List<Double> user_hrv = new ArrayList<>();
            for (int n = 0; n < num_hrv; n++) {
                int start_point_ind = n * num_window_length_points;
                int end_point_ind = start_point_ind + num_window_length_points;
                if (end_point_ind > len_time_ms) {
                    end_point_ind = len_time_ms;
                }
                List<Integer> rr_in_window = new ArrayList<>();
                for (int iii = 0; iii < rr_start_inds_no_outlier.size(); iii++) {
                    int rr_ind = rr_start_inds_no_outlier.get(iii);
                    if (rr_ind >= start_point_ind && rr_ind < end_point_ind) {
                        rr_in_window.add(rr_intervals_no_outlier.get(iii));
                    }
                }
                double hrv = Double.NaN;
                if (rr_in_window.size() != 0) {
                    hrv = std(rr_in_window);
                }
                user_hrv.add(hrv);
            }
            hrv_matrix_all_user.add(user_hrv);
        }

        List<Double> hrv_mean = nanmeanAxis0(hrv_matrix_all_user);
        List<List<Double>> hrv_diff = new ArrayList<>();
        for (int row = 0; row < hrv_matrix_all_user.size(); row++) {
            List<Double> hrv_diff_row = new ArrayList<>();
            for (int col = 0; col < hrv_matrix_all_user.get(0).size(); col++) {
                hrv_diff_row.add(hrv_matrix_all_user.get(row).get(col) - hrv_mean.get(col));
            }
            hrv_diff.add(hrv_diff_row);
        }
        return  hrv_diff;
    }

    public static List<Double> get_hrv(int window_length_s, List<String[]> rppg_data, Double start, Double end) throws IOException {
        int dataCols = rppg_data.get(0).length;
        List<Double> time_ms = new ArrayList<>();
        for (int i = 0; i < rppg_data.size(); i++) {
            time_ms.add(Double.parseDouble(rppg_data.get(i)[0]));
        }
        int len_time_ms = time_ms.size();
        double interval = time_ms.get(1) - time_ms.get(0);
        int num_window_length_points = (int) (1.0 * window_length_s / interval);
        int num_hrv = (int) (1.0 * len_time_ms / num_window_length_points);
        if (len_time_ms > (num_hrv * num_window_length_points)) {
            num_hrv++;
        }
        List<List<Double>> rppg_array_users = new ArrayList<>();
        for (int i = 0; i < rppg_data.size(); i++) {
            List<Double> rppg_array_user = new ArrayList<>();
            for (int j = 3; j < dataCols; j++) {
                rppg_array_user.add("".equals(rppg_data.get(i)[j].trim()) ? Double.NaN : Double.parseDouble(rppg_data.get(i)[j]));
            }
            rppg_array_users.add(rppg_array_user);
        }
        int num_user = rppg_array_users.get(0).size();
        List<List<Double>> hrv_matrix_all_user = new ArrayList<>();
        List<Double> user_hrv = new ArrayList<>();

        for (int user_id = 0; user_id < num_user; user_id++) {
            List<Double> rppg_user = new ArrayList<>();
            for (int r = 0; r < rppg_array_users.size(); r++) {
                rppg_user.add(rppg_array_users.get(r).get(user_id));
            }
            List<Integer> peaks_inds = new ArrayList<>();
            for (int i = 1; i <= rppg_user.size()-2; i++) {
                if (rppg_user.get(i) > rppg_user.get(i-1) && rppg_user.get(i) > rppg_user.get(i+1)) {
                    peaks_inds.add(i);
                }
            }
            List<Integer> rr_intervals = new ArrayList<>();
            List<Integer> rr_start_inds = new ArrayList<>();
            for (int i = 1; i < peaks_inds.size(); i++) {
                rr_intervals.add(peaks_inds.get(i) - peaks_inds.get(i-1));
                rr_start_inds.add(peaks_inds.get(i));
            }
            double rr_median;
            if(rr_intervals.size() < 2) {
                continue;
            }
            if (rr_intervals.size() % 2 == 1) {
                rr_median = rr_intervals.get(rr_intervals.size() / 2);
            } else {
                rr_median = 1.0 * ((rr_intervals.get(rr_intervals.size() / 2) +
                        rr_intervals.get(rr_intervals.size() / 2 - 1))) / 2;
            }
            List<Integer> rr_intervals_no_outlier = new ArrayList<>();
            List<Integer> rr_start_inds_no_outlier = new ArrayList<>();
            for (int ii = 0; ii < rr_intervals.size(); ii++) {
                int rr_interval = rr_intervals.get(ii);
                if (rr_interval > 0.1 * rr_median && rr_interval < 5 * rr_median) {
                    rr_intervals_no_outlier.add(rr_interval);
                    rr_start_inds_no_outlier.add(rr_start_inds.get(ii));
                }
            }


            int start_point_ind = start.intValue() / 1000;
            int end_point_ind = end.intValue() / 1000;
            if (end_point_ind > len_time_ms) {
                end_point_ind = len_time_ms;
            }
            List<Integer> rr_in_window = new ArrayList<>();
            for (int iii = 0; iii < rr_start_inds_no_outlier.size(); iii++) {
                int rr_ind = rr_start_inds_no_outlier.get(iii);
                if (rr_ind >= start_point_ind && rr_ind < end_point_ind) {
                    rr_in_window.add(rr_intervals_no_outlier.get(iii));
                }
            }
            double hrv = Double.NaN;
            if (rr_in_window.size() != 0) {
                hrv = std(rr_in_window);
            }
            user_hrv.add(hrv);
        }

        return  user_hrv;
    }

    public static List<List<List<Double>>> get_va_diff(int window_length_s, List<String[]> v_data, List<String[]> a_data) throws Exception {
        int dataRows = v_data.size();
        int dataCols = v_data.get(0).length;
        List<Double> time_ms = new ArrayList<>();
        for (int i = 0; i < v_data.size(); i++) {  // 从第一行开始，跳过标题行
            time_ms.add(Double.parseDouble(v_data.get(i)[0]));
        }
        int len_time_ms = time_ms.size();
        double interval = time_ms.get(1) - time_ms.get(0);
        int num_window_length_points = (int) (1.0 * window_length_s / interval);
        int num_window = (int) (time_ms.size()/num_window_length_points);
        if (len_time_ms > num_window * num_window_length_points){
            num_window ++;
        }
        List<List<Double>> v_diff_users = new ArrayList<>();
        for (int i = 0; i < v_data.size(); i++) {
            List<Double> v_array_user = new ArrayList<>();
            for (int j = 3; j < dataCols; j++) {
                Double t1 = "".equals(v_data.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(v_data.get(i)[1]);
                Double t2 = "".equals(v_data.get(i)[j].trim()) ? Double.NaN : Double.parseDouble(v_data.get(i)[j]);
                v_array_user.add(t2-t1);
            }
            v_diff_users.add(v_array_user);
        }
        List<List<Double>> a_diff_users = new ArrayList<>();
        for (int i = 0; i < a_data.size(); i++) {
            List<Double> a_array_user = new ArrayList<>();
            for (int j = 3; j < dataCols; j++) {
                Double t1 = "".equals(a_data.get(i)[1].trim()) ? Double.NaN : Double.parseDouble(a_data.get(i)[1]);
                Double t2 = "".equals(a_data.get(i)[j].trim()) ? Double.NaN : Double.parseDouble(a_data.get(i)[j]);
                a_array_user.add(t2 - t1);
            }
            a_diff_users.add(a_array_user);
        }
        List<List<Double>> v_diff_sum_matrix = new ArrayList<>();
        List<List<Double>> a_diff_sum_matrix = new ArrayList<>();
        for(int n = 0; n < num_window; n++ ){
            int start_point_ind = n * num_window_length_points;
            int end_point_ind = start_point_ind + num_window_length_points;
            if(end_point_ind > len_time_ms){
//                不能超过最后一个点
                end_point_ind = len_time_ms;
            }
            List<List<Double>> v_diff_window = new ArrayList<>();
            for (int i = start_point_ind; i < end_point_ind; i++) {
                v_diff_window.add(v_diff_users.get(i));
            }
            List<Double> v_diff_window_sum = nanmeanAxis0(v_diff_window);
            v_diff_sum_matrix.add(v_diff_window_sum);

            List<List<Double>> a_diff_window = new ArrayList<>();
            for (int i = start_point_ind; i < end_point_ind; i++) {
                a_diff_window.add(a_diff_users.get(i));
            }
            List<Double> a_diff_window_sum = nanmeanAxis0(a_diff_window);
            a_diff_sum_matrix.add(a_diff_window_sum);
        }
        List<List<List<Double>>> result = new ArrayList<>();
        List<List<Double>>  transpose_v = new ArrayList<>();
        List<List<Double>>  transpose_a = new ArrayList<>();
        int v_rows = v_diff_sum_matrix.size();
        int v_cols = v_diff_sum_matrix.get(0).size();
        for(int i = 0; i < v_cols; i++){
            List<Double> transpose = new ArrayList<>();
            for(int j = 0; j < v_rows; j++){
                transpose.add(v_diff_sum_matrix.get(j).get(i));
            }
            transpose_v.add(transpose);
        }

        int a_rows = a_diff_sum_matrix.size();
        int a_cols = a_diff_sum_matrix.get(0).size();
        for(int i = 0; i < a_cols; i++){
            List<Double> transpose = new ArrayList<>();
            for(int j = 0; j < a_rows; j++){
                transpose.add(a_diff_sum_matrix.get(j).get(i));
            }
            transpose_a.add(transpose);
        }
        result.add(transpose_v);
        result.add(transpose_a);
        return result;

    }

    public static void sections(List<List<Double>> hrv_diff, List<List<Double>> hrv_diff_abs_norm,
                                List<List<Double>> v_diff, List<List<Double>> a_diff,
                                List<List<Double>> v_diff_abs_norm,
                                List<List<Double>> a_diff_abs_norm,
                                List<Section> sections,List<String> user){
        for (int user_id = 0; user_id < hrv_diff.size(); user_id++){
            List<Double> total_diff_abs = new ArrayList<>();
            List<Double> t1 = nan_to_num(hrv_diff_abs_norm.get(user_id));
            List<Double> t2 = nan_to_num(v_diff_abs_norm.get(user_id));
            List<Double> t3 = nan_to_num(a_diff_abs_norm.get(user_id));
            for(int i = 1; i < t1.size() - 1; i++){
                total_diff_abs.add(t1.get(i) + t2.get(i) + t3.get(i));
            }
            List<Integer> ind_sort = get_top3_ind(total_diff_abs);
            List<Double> start_times_ms = new ArrayList<>();
            List<Double> end_times_ms= new ArrayList<>();
            for(long i : ind_sort){
                start_times_ms.add(i * 30000.0);
                end_times_ms.add(i * 30000.0 + 30000);
            }
//            System.out.println("individual sections");
            for(int ii = 0; ii < ind_sort.size(); ii++){
                int hrv = 0;
                int v = 0;
                int a =0;
                if(hrv_diff.get(user_id).get(ind_sort.get(ii)) > 0.0)
                    hrv = 1;
                if(v_diff.get(user_id).get(ind_sort.get(ii)) > 0.0)
                    v = 1;
                if(a_diff.get(user_id).get(ind_sort.get(ii)) > 0.0)
                    a = 1;
                Section section = new Section();
                section.setUsers(user.get(user_id));
                section.setStarts(start_times_ms.get(ii));
                section.setEnds(end_times_ms.get(ii));
                section.setLabel(a + v * 2 + hrv * 4);
                sections.add(section);
            }
        }
    }

    public static List<Double> nan_to_num(List<Double> nan_list){
        List<Double> res = new ArrayList<>();
        for(Double t : nan_list){
            if(t.equals(Double.NaN))
                res.add(0d);
            else
                res.add(t);
        }
        return res;
    }
    public static double softmax_weights_output(List<Double> arr) {
        List<Double> e_weights = new ArrayList<>();
        for (double d : arr) {
            e_weights.add(Math.exp(d));
        }
        double dotProduct = 0.0;
        double sum = 0.0;
        for (int i = 0; i < e_weights.size(); i++) {
            dotProduct += (e_weights.get(i) * arr.get(i));
            sum += e_weights.get(i);
        }
        return dotProduct / sum;
    }

    public static Score get_scores(List<String[]> rppg_sync_data, List<String[]> v_sync_data, List<String[]> a_sync_data) {
        List<Double> rppg_sync_array = new ArrayList<>();
        for (int r = 0; r < rppg_sync_data.size(); r++) {
            rppg_sync_array.add("".equals(rppg_sync_data.get(r)[1].trim()) ? Double.NaN : Double.parseDouble(rppg_sync_data.get(r)[1]));
        }

        int body_score_cnt = 0;
        double body_score = 0.0;
//        for (double d : rppg_sync_array) {
//            if (!Double.isNaN(d)) {
//                body_score_cnt++;
////                body_score += (1 / (1 + Math.exp(d * -100)));
//                body_score += (d / 2 + 0.5);
//            }
//        }
        for (int i = 0; i < rppg_sync_array.size(); i++) {
            Double d = rppg_sync_array.get(i);
//            if (i % 80 == 0 && !Double.isNaN(d)) {
            if (!Double.isNaN(d)) {
                body_score_cnt++;
                body_score += (d / 2) + 0.5;
//                d = (d / 2 + 0.5);
//                body_score += (1 / (1 + Math.exp(-100 * (d - 0.5))));
//                body_score += (1 / (1 + Math.exp(d * -200)));
            }
        }
        body_score /= body_score_cnt;
        body_score = (1 / (1 + Math.exp(-10 * (body_score - 0.5))));
        body_score = body_score * 100;
//        body_score = body_score * 100;
//        System.out.println("body_score: " + body_score);

/*        body_score = rppg_sync_array.stream()
                .filter(x -> !Double.isNaN(x))
                .map(x -> (1 / (1 + Math.exp(-5 * x))))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.5) * 100;*/
        System.out.println("body_score:" + body_score);
        List<Double> radar_chart_array = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            radar_chart_array.add(0.0);
        }
        //TODO:利用到radar_score
        double radar_score = softmax_weights_output(radar_chart_array);

        List<Double> v_sync_array = new ArrayList<>();
        for (int r = 0; r < v_sync_data.size(); r++) {
            v_sync_array.add("".equals(v_sync_data.get(r)[1].trim()) ? Double.NaN : Double.parseDouble(v_sync_data.get(r)[1]));
        }
        int v_score_cnt = 0;
        double v_score = 0.0;
        for (double d : v_sync_array) {
            if (!Double.isNaN(d)) {
                v_score_cnt++;
//                v_score += (1 / (1 + Math.exp(d * -10)));
                v_score += (d / 2 + 0.5);
            }
        }
        v_score /= v_score_cnt;

/*        v_score = v_sync_array.stream()
                .filter(x -> !Double.isNaN(x))
                .map(x -> (1 / (1 + Math.exp(-5 * x))))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.5);
        System.out.println("v_score:" + body_score);*/
        List<Double> a_sync_array = new ArrayList<>();
        for (int r = 0; r < a_sync_data.size(); r++) {
            a_sync_array.add("".equals(a_sync_data.get(r)[1].trim()) ? Double.NaN : Double.parseDouble(a_sync_data.get(r)[1]));
        }
        int a_score_cnt = 0;
        double a_score = 0.0;
        for (double d : a_sync_array) {
            if (!Double.isNaN(d)) {
                a_score_cnt++;
//                a_score += (1 / (1 + Math.exp(d * -10)));
                a_score += (d / 2 + 0.5);
            }
        }
        a_score /= a_score_cnt;
/*        a_score = a_sync_array.stream()
                .filter(x -> !Double.isNaN(x))
                .map(x -> (1 / (1 + Math.exp(-5 * x))))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.5);
        System.out.println("a_score:" + body_score);
        System.out.println();*/
        List<Double> scores = new ArrayList<>();
//        scores.add(radar_score);
        scores.add(v_score);
        scores.add(a_score);
        double behavior_score = computeAveScore(scores);
        behavior_score = (1 / (1 + Math.exp(-10 * (behavior_score - 0.5))));
        behavior_score = behavior_score * 100;
//        double behavior_score = Math.round(softmax_weights_output(scores) * 100);
        System.out.println("behavior score: " + behavior_score);

        scores.clear();
        scores.add(body_score);
        scores.add(behavior_score);
        double total_score = computeAveScore(scores) / 100;
        total_score = (1 / (1 + Math.exp(-10 * (total_score - 0.5))));
        total_score = total_score * 100;

        body_score = (1 / (1 + Math.exp(-10 * (body_score/100 - 0.5))));
        body_score = body_score * 100;

        behavior_score = (1 / (1 + Math.exp(-10 * (behavior_score/100 - 0.5))));
        behavior_score = behavior_score * 100;
//        double total_score = Math.round(softmax_weights_output(scores));
        System.out.println("total_score: " + total_score);
        body_score = Math.round(body_score);
        behavior_score = Math.round(behavior_score);
        total_score = Math.round(processScore(total_score));
        Score score = new Score();
        score.setBody_score(body_score);
        score.setBehavior_score(behavior_score);
        score.setTotal_score(total_score);
        return score;
    }

    private static double processScore(double score) {
        if (score <= 40) {
            score = 40;
        } else if (score >= 66) {
            score = 66;
        }
        score = (score - 40) / 26 * 100;
        return score;
    }

    public static Double getBrainScores(List<String[]> sync_data) {

        List<Double> sync_array = new ArrayList<>();
        for (int r = 0; r < sync_data.size(); r++) {
            sync_array.add("".equals(sync_data.get(r)[1].trim()) ? Double.NaN : Double.parseDouble(sync_data.get(r)[1]));
        }

    /*    int brain_score_cnt = 0;
        double brain_score = 0.0;

        for (int i = 0; i < sync_array.size(); i++) {
            Double d = sync_array.get(i);
            if (!Double.isNaN(d)) {
                brain_score_cnt++;
                brain_score += (d / 2) + 0.5;
            }
        }
        brain_score /= brain_score_cnt;
        brain_score = (1 / (1 + Math.exp(-10 * (brain_score - 0.5))));
        brain_score = brain_score * 100;
*/

        double brain_score = sync_array.stream()
                .filter(x -> !Double.isNaN(x))
                .map(x -> (1 / (1 + Math.exp(-10 * x))))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.5) * 100;
        System.out.println("brain_score: " + brain_score);
        return brain_score;
    }

    public static Score get_score(List<String[]> rppg_sync_data,List<String[]> v_sync_data,List<String[]> a_sync_data
                                    ,double coefficientBody,double coefficientBehaviour, double coefficientBrain, double coefficientTotal,
                                    double weightBody, double weightBehaviour, double weightBrain, double weightNlp, double weightParticipation,
                                  Double nlp_time, Double equal_participation) {
        List<Double> rppg_sync_array = new ArrayList<>();
        Integer cvTotalTime = Integer.valueOf(a_sync_data.get(a_sync_data.size() - 1)[0]);
        Double nlpRate = nlp_time / cvTotalTime;
        for (int r = 0; r < rppg_sync_data.size(); r++) {
            rppg_sync_array.add("".equals(rppg_sync_data.get(r)[1].trim()) ? Double.NaN : Double.parseDouble(rppg_sync_data.get(r)[1]));
        }
        int body_score_cnt = 0;
        double body_score = 0.0;
        for (int i = 0; i < rppg_sync_array.size(); i++) {
            Double d = rppg_sync_array.get(i);
            if (i % 80 == 0 && !Double.isNaN(d)) {
                body_score_cnt++;
                body_score += (d / 2) + 0.5;
            }
        }
        body_score /= body_score_cnt;
//        body_score = (1 / (1 + Math.exp(-coefficientBody * (body_score - 0.5))));
        //body_score sigmod非线性转换
        body_score = nonLinear(coefficientBody, body_score);
//        body_score = body_score * 100;

        System.out.println("body_score: " + body_score);

        List<Double> radar_chart_array = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            radar_chart_array.add(0.0);
        }
        double radar_score = softmax_weights_output(radar_chart_array);

        List<Double> v_sync_array = new ArrayList<>();
        for (int r = 0; r < v_sync_data.size(); r++) {
            v_sync_array.add("".equals(v_sync_data.get(r)[1].trim()) ? Double.NaN : Double.parseDouble(v_sync_data.get(r)[1]));
        }
        int v_score_cnt = 0;
        double v_score = 0.0;
        for (double d : v_sync_array) {
            if (!Double.isNaN(d)) {
                v_score_cnt++;
                v_score += (d / 2 + 0.5);
            }
        }
        v_score /= v_score_cnt;

        List<Double> a_sync_array = new ArrayList<>();
        for (int r = 0; r < a_sync_data.size(); r++) {
            a_sync_array.add("".equals(a_sync_data.get(r)[1].trim()) ? Double.NaN : Double.parseDouble(a_sync_data.get(r)[1]));
        }
        int a_score_cnt = 0;
        double a_score = 0.0;
        for (double d : a_sync_array) {
            if (!Double.isNaN(d)) {
                a_score_cnt++;
                a_score += (d / 2 + 0.5);
            }
        }
        a_score /= a_score_cnt;

        List<Double> scores = new ArrayList<>();
//        scores.add(radar_score);
        scores.add(v_score);
        scores.add(a_score);
        double behavior_score = computeAveScore(scores);
//        behavior_score = (1 / (1 + Math.exp(-coefficientBehaviour * (behavior_score - 0.5))));
        behavior_score = nonLinear(coefficientBehaviour, behavior_score);
//        behavior_score = behavior_score * 100;
        System.out.println("behavior score: " + behavior_score);
        //TODO: 添加brain_score 相关操作
        Double total_score = computeScoreWithWeight(0.0, body_score, behavior_score, nlpRate * 100, equal_participation * 100,
                weightBrain, weightBody, weightBehaviour, weightNlp, weightParticipation);
//        scores.clear();
//        scores.add(body_score);
//        scores.add(behavior_score);
//        double total_score = computeAveScore(scores) / 100;
//        total_score = (1 / (1 + Math.exp(-coefficientTotal * (total_score - 0.5))));
        total_score = nonLinear(coefficientTotal, total_score / 100);
//        total_score = total_score * 100;

//        body_score = (1 / (1 + Math.exp(-coefficientTotal * (body_score/100 - 0.5))));
//        body_score = body_score * 100;
        body_score = nonLinear(coefficientTotal, body_score / 100);
        behavior_score = nonLinear(coefficientTotal, behavior_score / 100);
//
//        behavior_score = (1 / (1 + Math.exp(-coefficientTotal * (behavior_score/100 - 0.5))));
//        behavior_score = behavior_score * 100;
        System.out.println("total_score: " + total_score);
        body_score = Math.round(body_score);
        behavior_score = Math.round(behavior_score);
        //线性回归
        total_score = 44.302 + 0.4652 * total_score;
        total_score = Double.valueOf(Math.round(total_score));
        Score score = new Score();
        score.setBody_score(body_score);
        score.setBehavior_score(behavior_score);
        score.setTotal_score(total_score);
        score.setNlp_speaker_time((double) Math.round(nlpRate * 100));
        score.setNlp_equal_participation((double) Math.round(equal_participation * 100));
        return score;
    }

    private static Double computeScoreWithWeight(Double brain, Double body, Double behaviour, Double nlp, Double participation,
                                               Double weightBrain, Double weightBody, Double weightBehaviour, Double weightNlp, Double weightParticipation) {
        return (body * weightBody + behaviour * weightBehaviour + nlp * weightNlp + participation * weightParticipation) / (weightBody + weightBehaviour + weightNlp + weightParticipation);
    }

    private static double nonLinear(double coefficient, double score) {
        //转成0 - 100
        return (1 / (1 + Math.exp(-coefficient * (score - 0.5)))) * 100;
    }

    private static Double computeAveScore(List<Double> scores) {
        int cnt = 0;
        Double totalScore = 0.0d;
        for (Double score : scores) {
            if (score != null && !Double.isNaN(score) && !Double.isInfinite(score) && score != 0) {
                cnt++;
                totalScore += score;
            }
        }
        if (cnt != 0) {
//            return Double.valueOf(Math.round(totalScore * 100 / cnt) );
            return totalScore / cnt;
        }
        return null;
    }

    private static final int num_pixel = 300;

    public static double[] ind_to_va(int row_ind, int col_ind) {
        return new double[] {
                2.0 * col_ind / (num_pixel - 1) - 1.0, // v
                1.0 - 2.0 * row_ind / (num_pixel - 1), // a
        };
    }

//    public static void get_pie_and_bar(List<String[]> data) throws IOException{
//        double[] speakers_time = {0.0,0.0,0.0};
//        double[] emotions_time = {0.0,0.0,0.0};
//        double[] acts_time = {0.0,0.0,0.0,0.0,0.0,0.0,0.0};
//        double[][] speakers_time_sep_by_emotions = {{0.0,0.0,0.0},{0.0,0.0,0.0},{0.0,0.0,0.0}};
//        double[][] emotions_time_sep_by_speakers = {{0.0,0.0,0.0},{0.0,0.0,0.0},{0.0,0.0,0.0}};
//        List<String> sentences_array = new ArrayList<String>();
//        HashMap<String,Integer> speakers_ind = new HashMap<>();
//        speakers_ind.put("user00",0);
//        speakers_ind.put("user01",1);
//        speakers_ind.put("user10",2);
//        HashMap<String,Integer> emotions_ind = new HashMap<>();
//        emotions_ind.put("Negative",0);
//        emotions_ind.put("Neutral",1);
//        emotions_ind.put("Positive",2);
//        HashMap<String,Integer> acts_ind = new HashMap<>();
//        acts_ind.put("Statement-non-opinion",0);
//        acts_ind.put("Statement-opinion",1);
//        acts_ind.put("Collaborative Completion",2);
//        acts_ind.put("Abandoned or Turn-Exit",3);
//        acts_ind.put("Uninterpretable",4);
//        acts_ind.put("Yes-No-Question",5);
//        acts_ind.put("Others",6);
//        for (int i = 0; i < data.size(); i++) {  // 从第一行开始，跳过标题行
//            String speaker = data.get(i)[0];
//            String emotion = data.get(i)[4];
//            String act = data.get(i)[5];
//            String start_time = data.get(i)[1];
//            String end_time = data.get(i)[2];
//            String sentence = data.get(i)[3];
//            boolean isExist = false;
//            Set<String> keySet = acts_ind.keySet();
////            nlpUtil中
//            for (String key:keySet){
//                if (act == key || act.equals(key)){
//                    isExist = true;
//                    break;
//                }
//            }
//            if (!isExist) act = "Others";
//            double delta_time = Double.parseDouble(end_time) - Double.parseDouble(start_time);
//
//            double speakers_time_pre = speakers_time[speakers_ind.get(speaker)];
//            speakers_time[speakers_ind.get(speaker)] = speakers_time_pre + delta_time;
//
//            double emotions_time_pre = emotions_time[emotions_ind.get(emotion)];
//            emotions_time[emotions_ind.get(emotion)] = emotions_time_pre + delta_time;
//
//            double acts_time_pre = acts_time[acts_ind.get(act)];
//            acts_time[acts_ind.get(act)] = acts_time_pre + delta_time;
//
//            double speakers_time_sep_by_emotions_pre = speakers_time_sep_by_emotions[speakers_ind.get(speaker)][emotions_ind.get(emotion)];
//            speakers_time_sep_by_emotions[speakers_ind.get(speaker)][emotions_ind.get(emotion)] = speakers_time_sep_by_emotions_pre + delta_time;
//
//            double emotions_time_sep_by_speakers_pre = emotions_time_sep_by_speakers[emotions_ind.get(emotion)][speakers_ind.get(speaker)];
//            emotions_time_sep_by_speakers[emotions_ind.get(emotion)][speakers_ind.get(speaker)] = emotions_time_sep_by_speakers_pre + delta_time;
//
//            sentences_array.add(sentence);
//        }
//        Double total_time = speakers_time[0]+speakers_time[1]+speakers_time[2];
//        BigDecimal s0 = new BigDecimal(speakers_time[0]);
//        BigDecimal s1 = new BigDecimal(speakers_time[1]);
//        BigDecimal s2 = new BigDecimal(speakers_time[2]);
//        double v1 = s0.setScale(3, RoundingMode.HALF_UP).doubleValue();
////        double v2 = s1.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
//        double v2 = s1.setScale(3, RoundingMode.HALF_UP).doubleValue();
//        double v3 = s2.setScale(3, RoundingMode.HALF_UP).doubleValue();
//        System.out.println(v1);
//        System.out.println(v2);
//        System.out.println(v3);
//        List<Double> rate = new ArrayList<>();
//        BigDecimal t0 = new BigDecimal(speakers_time[0]/total_time);
//        BigDecimal t1 = new BigDecimal(speakers_time[1]/total_time);
//        BigDecimal t2 = new BigDecimal(speakers_time[2]/total_time);
//        rate.add(t0.setScale(3, RoundingMode.HALF_UP).doubleValue());
//        rate.add(t1.setScale(3, RoundingMode.HALF_UP).doubleValue());
//        rate.add(t2.setScale(3, RoundingMode.HALF_UP).doubleValue());
//        Set<String> strings = speakers_ind.keySet();
//        System.out.println(strings.toString());
//    }

    public static List<Heatmap> va_heatmap(Long meetingID,List<String[]> v_data_string,List<String[]> a_data_string) {
        int[][] img = new int[num_pixel][num_pixel];

        List<List<Double>> v_data = new ArrayList<>();
        for (int i = 0; i < v_data_string.size(); i++) {
            String[] row = v_data_string.get(i);
            List<Double> v_data_row = new ArrayList<>();
            for (String s : row)
                v_data_row.add("".equals(s.trim()) ? Double.NaN : Double.parseDouble(s));
            v_data.add(v_data_row);
        }
        List<List<Double>> a_data = new ArrayList<>();
        for (int i = 0; i < a_data_string.size(); i++) {
            String[] row = a_data_string.get(i);
            List<Double> a_data_row = new ArrayList<>();
            for (String s : row)
                a_data_row.add("".equals(s.trim()) ? Double.NaN : Double.parseDouble(s));
            a_data.add(a_data_row);
        }

        final int v_mean_index = 1;
        final int a_mean_index = 1;
        final int v_std_index  = 2;
        final int a_std_index  = 2;

        for (int row_id = 0; row_id < v_data.size(); row_id++) {
            double v_mean = v_data.get(row_id).get(v_mean_index);
            double a_mean = a_data.get(row_id).get(a_mean_index);
            double v_std  = v_data.get(row_id).get(v_std_index);
            double a_std  = a_data.get(row_id).get(a_std_index);

            double sq_v_std = Double.isNaN(v_std) ? Double.NaN : v_std * v_std;
            double sq_a_std = Double.isNaN(a_std) ? Double.NaN : a_std * a_std;

            for (int i = 0; i < num_pixel; i++) {
                for (int j = 0; j < num_pixel; j++) {
                    double v = ind_to_va(i, j)[0];
                    double a = ind_to_va(i, j)[1];
                    double c = (Double.isNaN(v_mean) || Double.isNaN(sq_v_std) ||
                            Double.isNaN(a_mean) || Double.isNaN(sq_a_std)) ?
                            Double.NaN :
                            ((v-v_mean)*(v-v_mean)/sq_v_std + (a-a_mean)*(a-a_mean)/sq_a_std);
                    if (!Double.isNaN(c) && c <= 1) {
                        img[i][j]++;
                    }
                }
            }
        }

        List<Heatmap> json_array = new ArrayList<>();
        for (int i = 0; i < num_pixel; i++)
            for (int j = 0; j < num_pixel; j++)
                if (img[i][j] > 0) {
                    Heatmap heatmap = new Heatmap();
                    heatmap.setMeeting_id(meetingID);
                    heatmap.setX(i);
                    heatmap.setY(j);
                    heatmap.setImg(img[i][j]);
                    json_array.add(heatmap);
                }
//                    json_array.add(String.format("[%d, %d, %d]", i, j, img[i][j]));
//        System.out.println(json_array);
        return json_array;
    }


    public static List<IndividualScore> get_individual_score(Long meetingID,List<IndividualSyncA> isa, List<IndividualSyncV> isv,
                                                             List<IndividualSyncR> isr, List<IndividualSyncR> isb) {
        //求body_score
        HashMap<String, Double> bodyScore = new HashMap<>();
        HashMap<String, Integer> bodyScoreCount = new HashMap<>();
        HashSet<String> userList = new HashSet<>();
        for (IndividualSyncR individualSyncR : isr) {
            String users = individualSyncR.getUsers();
            Double individual_rate = individualSyncR.getIndividual_rate();
            userList.add(users);
            if(!Double.isNaN(individual_rate)){
                bodyScore.put(users, bodyScore.getOrDefault(users, 0.0d) + individual_rate);
                bodyScoreCount.put(users, bodyScoreCount.getOrDefault(users,0) + 1);
            }
        }

        for (Map.Entry<String, Double> entry : bodyScore.entrySet()) {
            String user = entry.getKey();
            Double individualTotalScore = entry.getValue();
            Integer count = bodyScoreCount.getOrDefault(user, 0);
            if(count != 0){
                entry.setValue(BigDecimal.valueOf(individualTotalScore / count).setScale(2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue());
            }
        }

        //求brain_score
        HashMap<String,Double> brainScore = new HashMap<>();
        HashMap<String,Integer> brainScoreCount = new HashMap<>();
        //TODO: blink user 与rppg user 可能不一致
        HashSet<String> brainUserList = new HashSet<>();
        for (IndividualSyncR individualSyncB : isb) {
            String users = individualSyncB.getUsers();
            Double individual_rate = individualSyncB.getIndividual_rate();
            brainUserList.add(users);
            if(!Double.isNaN(individual_rate)){
                brainScore.put(users, brainScore.getOrDefault(users, 0.0d) + individual_rate);
                brainScoreCount.put(users, brainScoreCount.getOrDefault(users,0) + 1);
            }
        }
        for (Map.Entry<String, Double> entry : brainScore.entrySet()) {
            String user = entry.getKey();
            Double individualTotalScore = entry.getValue();
            Integer count = brainScoreCount.getOrDefault(user, 0);
            if(count != 0){
                entry.setValue(BigDecimal.valueOf(individualTotalScore / count).setScale(2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue());
            }
        }

        //求behavior_score
        HashMap<String,Double> behaviorAScore = new HashMap<>();
        HashMap<String,Double> behaviorVScore = new HashMap<>();
        HashMap<String,Integer> behaviorACount = new HashMap<>();
        HashMap<String,Integer> behaviorVCount = new HashMap<>();
        for (IndividualSyncA individualSyncA : isa) {
            String users = individualSyncA.getUsers();
            Double individual_score = individualSyncA.getIndividual_rate();
            if(!Double.isNaN(individual_score)){
                behaviorAScore.put(users,behaviorAScore.getOrDefault(users,0.0d) + individual_score);
                behaviorACount.put(users,behaviorACount.getOrDefault(users,0) + 1);
            }
        }

        for (IndividualSyncV individualSyncV : isv) {
            String users = individualSyncV.getUsers();
            Double individual_score = individualSyncV.getIndividual_score();
            if(!Double.isNaN(individual_score)){
                behaviorVScore.put(users,behaviorVScore.getOrDefault(users,0.0d) + individual_score);
                behaviorVCount.put(users,behaviorVCount.getOrDefault(users,0) + 1);
            }
        }

        List<IndividualScore> individualScores = new ArrayList<>();
        for (String u : userList) {
            IndividualScore individualScore = new IndividualScore();
            individualScore.setMeeting_id(meetingID);
            individualScore.setUsers(u);
            if (bodyScore.containsKey(u)){
                individualScore.setBody_score(bodyScore.get(u));
            }
            if (brainScore.containsKey(u)) {
                individualScore.setBrain_score(brainScore.get(u));
            }
            if(behaviorAScore.containsKey(u) && behaviorVScore.containsKey(u)){
                Double scoreA = behaviorAScore.get(u);
                Integer countA = behaviorACount.get(u);
                Double scoreV = behaviorVScore.get(u);
                Integer countV = behaviorVCount.get(u);
                if(countA != 0 && countV != 0){
                    Double s = (scoreA / countA + scoreV / countV) / 2;
                    individualScore.setBehavior_score(BigDecimal.valueOf(s).setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue());
                }
            }
            individualScores.add(individualScore);
        }

        return individualScores;
    }

    public static List<EmojiTable> handleIndividualEmoji(Long meetingID, List<AResult> listA, List<VResult> listV, int userNum,List<Long> timeline) {
        List<List<List<Double>>> data = new ArrayList<>();
        for (int i = 0; i < userNum; i++) {
            List<List<Double>> userAV = new ArrayList<>();
            List<Double> v = new ArrayList<>();
            List<Double> a = new ArrayList<>();
            for (VResult vResult : listV) {
                String userFiled = "user" + (i < 10 ? "0" : "") + i;
                try {
                    Field field = VResult.class.getDeclaredField(userFiled);
                    field.setAccessible(true);
                    Double value = (Double) field.get(vResult);
                    v.add(value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            for (AResult aResult : listA) {
                String userField = "user" + (i < 10 ? "0" : "") + i;
                try {
                    Field field = AResult.class.getDeclaredField(userField);
                    field.setAccessible(true);
                    Double value = (Double) field.get(aResult);
                    a.add(value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            userAV.add(v);
            userAV.add(a);
            data.add(userAV);
        }
        List<EmojiTable> emojiTables = computeEmoji(data, timeline, meetingID);
        return emojiTables;
    }

    public static List<GroupEmoji> handleUniverseGroupEmoji(Long meetingID, List<AResult> listA, List<VResult> listV, int userNum,List<Long> timeline) {
        int len = Math.min(listA.size(), listV.size());
        List<GroupEmoji> groupEmojiList = new ArrayList<GroupEmoji>(len);
        Double preX = 0.0d;
        Double preY = 0.0d;
        for (int i = 0; i < len; i++) {
            AResult aResult = listA.get(i);
            VResult vResult = listV.get(i);
            Double x = 0.0d;
            Double y = 0.0d;
            List<Double> v = new ArrayList<>();
            List<Double> a = new ArrayList<>();
            for (int j = 0; j < userNum; j++) {
                String userFiled = "user" + (j < 10 ? "0" : "") + j;
                try {
                    Field field = VResult.class.getDeclaredField(userFiled);
                    field.setAccessible(true);
                    Double value = (Double) field.get(vResult);
                    v.add(value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                try {
                    Field field = AResult.class.getDeclaredField(userFiled);
                    field.setAccessible(true);
                    Double value = (Double) field.get(aResult);
                    a.add(value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
            x = v.stream()
                    .filter(d -> d != null && !Double.isNaN(d))
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .getAsDouble();
            y = v.stream()
                    .filter(d -> d != null && !Double.isNaN(d))
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .getAsDouble();
            int closestPoint = findClosestPoint(x, y);
            if (i == 0) {
                groupEmojiList.add(new GroupEmoji(meetingID, closestPoint, timeline.get(i), closestPoint));
            } else {
                int closestAccPoint = findClosestPoint((preX + x) / 2, (preY + y) / 2);
                groupEmojiList.add(new GroupEmoji(meetingID, closestPoint, timeline.get(i), closestAccPoint));
            }
            preX = x;
            preY = y;
        }
        return groupEmojiList;
    }

    public static List<PosNegRate> getPositiveAndNegative(List<String[]> listA, List<String[]> listV, List<String> userList, Long meetingID,
                                                          List<Number> total_rate) {
        List<PosNegRate> posNegRates = new ArrayList<>();
        int total_pos_cnt_a = 0;
        int total_pos_cnt_v = 0;
        int total_cnt_a = 0;
        int total_cnt_v = 0;
        int start = 3;
        for (String user : userList) {
            Integer i = Integer.valueOf(user.substring(user.length() - 2));
            int total_count_a = 0;
            int positive_count_a = 0;
            int total_count_v = 0;
            int positive_count_v = 0;
            for (String[] s : listA) {
                if(!"".equals(s[start + i].trim())){
                    total_count_a++;

                    total_cnt_a++;

                    if(Double.valueOf(s[start + i].trim()) >= 0.0d){
                        positive_count_a++;
                        total_pos_cnt_a++;
                    }
                }
            }

            for (String[] s : listV) {
                if(!"".equals(s[start + i].trim())){
                    total_count_v++;
                    total_cnt_v++;
                    if(Double.valueOf(s[start + i].trim()) >= 0.0d){
                        positive_count_v++;
                        total_pos_cnt_v++;
                    }
                }
            }
            Double positive_rate_a;
            if(total_count_a != 0){
                positive_rate_a = 1.0 * positive_count_a / total_count_a;
            }else {
                positive_rate_a = Double.NaN;
            }

            Double positive_rate_v;
            if(total_count_v != 0){
                positive_rate_v = 1.0 * positive_count_v / total_count_v;
            }else {
                positive_rate_v = Double.NaN;
            }
            posNegRates.add(
                    new PosNegRate(meetingID, user, positive_rate_a,1.0 - positive_rate_a, positive_rate_v, 1.0 - positive_rate_v));
            total_rate.add(1.0 * total_pos_cnt_a / total_cnt_a);
            total_rate.add(1 - 1.0 * total_pos_cnt_a / total_cnt_a);
            total_rate.add(1.0 * total_pos_cnt_v / total_cnt_v);
            total_rate.add(1 - 1.0 * total_pos_cnt_v / total_cnt_v);
        }
        return posNegRates;
    }
    public static List<EmojiTable> computeEmoji(List<List<List<Double>>> data, List<Long> timeline, Long meetingID){
        //用户数一致
        int userCount = data.size();
        List<EmojiTable> emojiTableList = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            String userId = "user" + (i < 10 ? "0" : "") + i;
            List<Double> dataV = data.get(i).get(0);
            List<Double> dataA = data.get(i).get(1);
            int len = Math.min(dataA.size(),dataV.size());
            for (int j = 0; j < len; j++) {
                //meetingID user emoji time
                int emoji = findClosestPoint(dataV.get(j), dataA.get(j));
                emojiTableList.add(new EmojiTable(meetingID,userId,emoji,timeline.get(j)));
            }
        }
        return emojiTableList;
    }

    public static Double get_team_sync(List<Async> listAsync, List<Vsync> listVsync, List<Rsync> listRsync) {
        long time = 0;
        int count = 0;
        double total = 0.0d;
//        double team_sync = 0.0d;
        for (Async async : listAsync) {
            if(async.getA_sync() != null && !Double.isNaN(async.getA_sync())){
                count++;
                time += async.getStart_time();
                total += async.getA_sync();
            }
        }

        for (Vsync vsync : listVsync) {
            if(vsync.getV_sync()!= null && !Double.isNaN(vsync.getV_sync())){
                count ++;
                time += vsync.getStart_time();
                total += vsync.getV_sync();
            }
        }

        for (Rsync rsync : listRsync) {
            if(rsync.getR_sync() != null && !Double.isNaN(rsync.getR_sync())){
                count ++;
                time += rsync.getStart_time();
                total += rsync.getR_sync();
            }
        }
        Double team_sync;
//        Long ave_time;
        if(count != 0){
            //[-1,1]
            team_sync = total / count;
            //[0,1]
            team_sync = (team_sync + 1) / 2;
            team_sync = 1 - team_sync;

//            team_sync *= team_sync;
//            ave_time = (time / count);
            team_sync = 1 / (1 + Math.pow(Math.E, -24 * (team_sync - 0.5)));
        }else {
            team_sync = Double.NaN;
//            ave_time = 0l;
        }

//        ArrayList<Object> objects = new ArrayList<>();
//        objects.add(ave_time);
//        objects.add(team_sync);
//        HashMap<String,Number> ans = new HashMap<>();
//        ans.put("ave_time",ave_time);
//        ans.put("team_sync",team_sync);
        return team_sync;

    }



//    public static List<CircleSync> get_user_sync(List<IndividualSync> individualSyncs, Long meetingID, int distance) {
//        List<CircleSync> circleSyncs = new ArrayList<>();
//        for (IndividualSync individualSync : individualSyncs) {
//            Double individual_sync = individualSync.getIndividual_sync();
//            int label = 0;
//            if(!Double.isNaN(individual_sync)){
//                individual_sync = 1 - (individual_sync + 1) / 2;
//                label = individual_sync <  distance ? 1 : 0;
//            }
//            circleSyncs.add(new CircleSync(meetingID,individualSync.getTime_ms().longValue(),individual_sync,individualSync.getUsers(),label));
//        }
//        return circleSyncs;
////        HashMap<String,Integer> user_count = new HashMap<>();
////        HashMap<String,Double> user_total = new HashMap<>();
//////        HashMap<String,Double> user_sync = new HashMap<>();
////        long time = 0;
////        int time_count = 0;
////
////        for (IndividualSync individualSync : individualSyncs) {
////            if(!Double.isNaN(individualSync.getIndividual_sync())){
////                String user = individualSync.getUsers();
////                time += individualSync.getTime_ms();
////                time_count++;
////                if(user_count.containsKey(user)){
////                    user_count.put(user,user_count.get(user) + 1);
////                    user_total.put(user, user_total.get(user) + individualSync.getIndividual_sync());
////                }else {
////                    user_count.put(user,1);
////                    user_total.put(user,individualSync.getIndividual_sync());
////                }
////            }
////        }
////
////        for (Map.Entry<String, Integer> entry : user_count.entrySet()) {
////            String key = entry.getKey();
////            Integer count = user_count.get(key);
////            Double total = user_total.get(key);
////
////            if(count != 0)
////                user_sync.put(key,1 - (total / count + 1) / 2);
////        }
////
////        return time / time_count;
//////        return user_sync;
//    }

    static class Point {
        double x, y;
        int id;

        public Point(int id, double x, double y) {
            this.x = x;
            this.y = y;
            this.id = id;
        }

        public double distance(double x, double y) {
            return Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2);
        }
    }

    public static int findClosestPoint(double x, double y) {
        List<Point> points = Arrays.asList(
                new Point(1, 0.125, -0.0625),
                new Point(2, 0.5, 0.5),
                new Point(3, -0.125, 0.5),
                new Point(4, -0.25, -0.25),
                new Point(5, -0.0625, 0.25),
                new Point(6, -0.375, -0.375),
                new Point(7, -0.5, 0.5),
                new Point(8, -0.625, 0),
                new Point(9, -0.5, -0.5),
                new Point(10, -0.625, 0.25),
                new Point(11, -0.125, -0.0625)
        );

        int emojiId = 0;
        double closestDist = Double.MAX_VALUE;

        for (Point p : points) {
            double dist = p.distance(x, y);
            if (dist <= closestDist) {
                emojiId = p.id;
                closestDist = dist;
            }
        }

        return emojiId;
    }


}