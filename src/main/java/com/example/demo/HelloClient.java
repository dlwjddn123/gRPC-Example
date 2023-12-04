package com.example.demo;

import com.example.HelloRequest;
import com.example.HelloResponse;
import com.example.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloClient {

    @GetMapping("/test")
    public void test() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);

        HelloResponse response = stub.sayHello(HelloRequest.newBuilder()
                .setName("world")
                .build());

        System.out.println(response.getMessage());

        channel.shutdown();
    }
}
