//package com.aws.sync;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.Base64;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//public class MyApiClient {
//    private static final String API_URL = "https://api.fireflies.ai/graphql/";
//    private static final String AUTH_TOKEN = "559da0e8-21aa-4bec-a530-b65844392d72";
//
//    public static void main(String[] args) {
//        try {
//            URL url = new URL(API_URL);
//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//            con.setRequestMethod("POST");
//            con.setRequestProperty("Content-Type", "application/json");
//            con.setRequestProperty("Authorization", "Bearer " + AUTH_TOKEN);
//            con.setDoOutput(true);
//
//            String jsonInputString = "{ \"query\": \"{ user { name integrations } }\" }";
//            byte[] postData = jsonInputString.getBytes("UTF-8");
//            con.getOutputStream().write(postData);
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            // 解析JSON响应
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode jsonNode = mapper.readTree(response.toString());
//            JsonNode userNode = jsonNode.get("data").get("user");
//            String name = userNode.get("name").asText();
//            JsonNode integrationsNode = userNode.get("integrations");
//            // 处理集成数据
//
//            System.out.println("User name: " + name);
//            System.out.println("Integrations: " + integrationsNode);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
