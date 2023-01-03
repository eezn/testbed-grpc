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

// N:N (Async Only)
public class BidirectionalStreamGrpcClient {
    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(GrpcServerConst.DOMAIN, GrpcServerConst.PORT)
                .usePlaintext()
                .build();

        UserServiceGrpc.UserServiceStub asyncStub = UserServiceGrpc.newStub(channel);


        // Client: Bidirectional Streaming RPC
        System.out.println(">>> Bidirectional Streaming RPC");
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<User> responseObserver = new StreamObserver<>() {

            @Override
            public void onNext(User user) {
                System.out.println(user.toString());
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

        StreamObserver<UserIdx> requestObserver = asyncStub.getUsersRealtime(responseObserver);

        try {
            for (int i = 1; i <= 5; ++i) {
                requestObserver.onNext(UserIdx.newBuilder()
                        .addIdx(i)
                        .build());
                Thread.sleep(1000);
            }
            requestObserver.onNext(UserIdx.newBuilder()
                    .addIdx(6)
                    .addIdx(7)
                    .build());
        } catch (StatusRuntimeException | InterruptedException ignored) {}
        requestObserver.onCompleted();

        try {
            finishLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {}

        channel.shutdown();
    }
}
