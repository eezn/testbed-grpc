package grpc.client.stream;

import com.atto.grpc.User;
import com.atto.grpc.UserIdx;
import com.atto.grpc.UserServiceGrpc;
import grpc.GrpcServerConst;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientSideStreamGrpcClient {
    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(GrpcServerConst.DOMAIN, GrpcServerConst.PORT)
                .usePlaintext()
                .build();

        UserServiceGrpc.UserServiceStub asyncStub = UserServiceGrpc.newStub(channel);


        // Client: Client-side Streaming RPC
        System.out.println(">>> Client-side Streaming RPC");
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<UserIdx> responseObserver = new StreamObserver<>() {

            @Override
            public void onNext(UserIdx userIdx) {
                for (long idx : userIdx.getIdxList()) {
                    System.out.println("Client: " + idx);
                }
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        StreamObserver<User> requestObserver = asyncStub.setUsers(responseObserver);

        try {
            for (int i = 0; i < 5; ++i) {
                requestObserver.onNext(User.newBuilder()
                        .setUsername("NEW USER - " + i)
                        .setEmail("USER" + i + "@test.com")
                        .addRoles("USER")
                        .build());
                Thread.sleep(500);
            }
        } catch (StatusRuntimeException | InterruptedException ignored) {}

        requestObserver.onCompleted();

        try {
            finishLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {}

        channel.shutdown();
    }
}
