package com.example.democlientgrpc.rest;

import com.example.democlientgrpc.grpc.GrpcClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@org.springframework.web.bind.annotation.RestController
public class RestController {

    public static String messageResult;

    @GetMapping("/")
    public String greeting(){
        return "welcome to this grpc client app";
    }

    @GetMapping("/grpc")
    public String triggerGrpc(@RequestParam(name = "service") String service){

        ManagedChannel channel = ManagedChannelBuilder.forAddress("34.123.30.15", 5000)
                .usePlaintext()
                .build();

        if(service.equals("message")){
            return tirggerMessage(channel);
        }

        if(service.equals("messagestream")){
            tirggerMessageStream(channel);
        }

        return "no action result";
    }

    private String tirggerMessage(ManagedChannel channel){
        GrpcClient client = new GrpcClient(channel);
        client.message();
        String result = messageResult;
        channel.shutdown();
        return result;
    }


    private void tirggerMessageStream(ManagedChannel channel){
        GrpcClient client = new GrpcClient(channel);
        client.messageStream();
        channel.shutdown();
    }
}
