package com.example.democlientgrpc.grpc;

import com.example.democlientgrpc.rest.RestController;
import com.google.protobuf.Message;
import com.example.grpc.HeartBeatRequest;
import com.example.grpc.HeartBeatResponse;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.example.grpc.MessagingServiceGrpc;
import com.example.grpc.MessagingServiceGrpc.MessagingServiceStub;
import com.example.grpc.MessagingServiceGrpc.MessagingServiceBlockingStub;
import io.grpc.stub.StreamObserver;
import com.example.grpc.MessagingRequest;
import com.example.grpc.MessagingResponse;
import com.example.grpc.MessagingServiceGrpc;

import java.util.Iterator;

public class GrpcClient {

    private final MessagingServiceStub asyncStub;
    private final MessagingServiceBlockingStub stub;

    public void message(){
        MessagingRequest request = MessagingRequest.newBuilder().setMessage("testing message service from Will's local").build();
        MessagingResponse response = stub.message(request);
        System.out.println(response.getMessage());
        RestController.messageResult = response.getMessage();
    }

    public void messageStream(){
        MessagingRequest request = MessagingRequest.newBuilder().setMessage("testing message stream service  from Will's local").build();
        Iterator<MessagingResponse> responses;
        responses = stub.messageStream(request);
        for (int i = 1; responses.hasNext(); i++) {
            MessagingResponse response = responses.next();
            System.out.println(response.getMessage());
        }
    }

    public GrpcClient(Channel channel){
        stub = MessagingServiceGrpc.newBlockingStub(channel);
        asyncStub = MessagingServiceGrpc.newStub(channel);
    }

    public void recordStream(){
        final int heartBeat = 12;
        StreamObserver<HeartBeatResponse> responseObserver = new StreamObserver<HeartBeatResponse>(){
            int hearBeatCount;

            @Override
            public void onNext(HeartBeatResponse response) {
                hearBeatCount = response.getCount();
                System.out.println("Server says that it received " + hearBeatCount + "count of heartBeat");
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("grpc client failed: " + t.getMessage());

            }

            @Override
            public void onCompleted() {
                String result = hearBeatCount == heartBeat? "correct": "wrong";
                System.out.println("you've got the "+result+"result");
            }
        };
        StreamObserver<HeartBeatRequest> requestObserver = asyncStub.recordStream(responseObserver);
        for(int i = 0; i < heartBeat; i++){
            HeartBeatRequest request = HeartBeatRequest.newBuilder().setCount(1).build();
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();
    }

    public static void main(String[] args){

        String target = "34.123.30.15:5000";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        GrpcClient client = new GrpcClient(channel);
        client.message();
        channel.shutdown();
    }
}

