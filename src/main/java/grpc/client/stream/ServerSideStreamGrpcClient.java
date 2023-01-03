package grpc.client.stream;

import com.atto.grpc.User;
import com.atto.grpc.UserIdx;
import com.atto.grpc.UserServiceGrpc;
import grpc.GrpcServerConst;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.Iterator;

public class ServerSideStreamGrpcClient {
    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(GrpcServerConst.DOMAIN, GrpcServerConst.PORT)
                .usePlaintext()
                .build();

        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);


        // Client: Server-side Streaming RPC
        System.out.println(">>> Server-side Streaming RPC");

        try {
            Iterator<User> getUsersResult = stub.getUsers(UserIdx.newBuilder()
                    .addIdx(1)
                    .addIdx(2)
                    .build());
            while (getUsersResult.hasNext()) {
                System.out.println(getUsersResult.next().toString());
            }
        } catch (StatusRuntimeException ignored) {}

        channel.shutdown();
    }
}
