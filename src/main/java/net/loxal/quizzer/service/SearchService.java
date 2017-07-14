/*
 * Copyright 2017 IntraFind Software AG. All rights reserved.
 */

package net.loxal.quizzer.service;

import net.loxal.quizzer.dto.Hit;
import net.loxal.quizzer.dto.Result;
import net.loxal.quizzer.repository.SearchRepository;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class SearchService {
    private final SearchRepository repository;

    @Autowired
    public SearchService(final SearchRepository repository) {
        this.repository = repository;
    }

    public void delete(String id) {
        repository.delete(id);
    }

    public Hit search(String query) {
        final String qresult;
        switch (query) {
            case "ying":
                qresult = "YING result";
                break;
            case "yang":
                qresult = "YANG result";
                break;
            default:
                qresult = "SOMETHING ELSE";
                break;
        }
        Result result = new Result();
        result.setTitle("Some TITLE with " + qresult + " in it.");
        result.setText("Some TEXT with " + qresult + " in it.");
        Hit hit = new Hit(query, Arrays.asList(result));

//        printElasticSearchInfo();
//        final Hit save = repository.save(hit);
        repository.findAll().forEach(e ->{
            System.out.println("e.getTitle() = " + e.getTitle());
        });
        return hit;
    }

//    @Autowired
//         private ElasticsearchOperations es;
//
//        //useful for debug, print elastic search details
//        private void printElasticSearchInfo() {
//
//            System.out.println("--ElasticSearch--");
//            Client client = es.getClient();
//            Map<String, String> asMap = client.settings().getAsMap();
//
//            asMap.forEach((k, v) -> {
//                System.out.println(k + " = " + v);
//            });
//            System.out.println("--ElasticSearch--");
//
////            try {
//                client.prepareIndex().setIndex("alex").setType("some").setSource("{\n" +
//                        "  \"title\": \"Spring + Spring Data + ElasticSearch\",\n" +
//                        "  \"category\":\"Spring Boot\",\n" +
//                        "  \"published_date\":\"23-MAR-2017\",\n" +
//                        "  \"author\":\"Rambabu Posa\"\n" +
//                        "}").execute().actionGet().getVersion();
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            } catch (ExecutionException e) {
////                e.printStackTrace();
////            }
//
////            SearchRequest sr = new SearchRequest();
////            GetRequest gr = new GetRequest();
////            gr.index("mkyong").type("posts").id("1003");
////            final ActionFuture<GetResponse> getResponseActionFuture = client.get(gr);
////            try {
////                System.out.println("getResponseActionFuture.    get().isExists() = " + getResponseActionFuture.get().isExists());
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            } catch (ExecutionException e) {
////                e.printStackTrace();
////            }
//        }

}