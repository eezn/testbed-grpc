package grpc.client;

import com.atto.grpc.User;
import com.atto.grpc.UserIdx;
import com.atto.grpc.UserServiceGrpc;
import grpc.GrpcServerConst;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GrpcClient {
    public static void main(String[] args) {

        // Channel
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(GrpcServerConst.DOMAIN, GrpcServerConst.PORT)
                .usePlaintext()
                .build();

        // Stub
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
        UserServiceGrpc.UserServiceStub asyncStub = UserServiceGrpc.newStub(channel);
        UserServiceGrpc.UserServiceFutureStub futureStub = UserServiceGrpc.newFutureStub(channel);


        // Client: Unary RPC
        System.out.println("(1) Unary RPC");
        UserIdx setUserResult;
        User getUserResult;

        setUserResult= stub.setUser(User.newBuilder()
                .setUsername("JIN-LEE")
                .setEmail("JIN-LEE@grpc.com")
                .addRoles("USER")
                .addRoles("ADMIN")
                .build());
        System.out.println(setUserResult.getIdx(0));
        getUserResult = stub.getUser(setUserResult);
        System.out.println(getUserResult.toString());

        setUserResult = stub.setUser(User.newBuilder()
                .setUsername("CHULSU")
                .setEmail("CHULSU@grpc.com")
                .addRoles("USER")
                .build());
        System.out.println(setUserResult.getIdx(0));
        getUserResult = stub.getUser(setUserResult);
        System.out.println(getUserResult.toString());


        // Client: Client-side Streaming RPC
        System.out.println("(2) Client-side Streaming RPC");
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


        // Client: Server-side Streaming RPC
        System.out.println("(3) Server-side Streaming RPC");

        try {
            Iterator<User> getUsersResult = stub.getUsers(UserIdx.newBuilder()
                    .addIdx(1)
                    .addIdx(2)
                    .build());
            while (getUsersResult.hasNext()) {
                System.out.println(getUsersResult.next().toString());
            }
        } catch (StatusRuntimeException ignored) { }


        // Client: Bidirectional Streaming RPC
        System.out.println("(4) Bidirectional Streaming RPC");

        final CountDownLatch finishLatch2 = new CountDownLatch(1);
        StreamObserver<User> responseObserver2 = new StreamObserver<User>() {

            @Override
            public void onNext(User user) {
                System.out.println(user.toString());
            }

            @Override
            public void onError(Throwable t) {
                finishLatch2.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch2.countDown();
            }
        };

        StreamObserver<UserIdx> requestObserver2 = asyncStub.getUsersRealtime(responseObserver2);

        try {
            for (int i = 1; i <= 5; ++i) {
                requestObserver2.onNext(UserIdx.newBuilder()
                        .addIdx(i)
                        .build());
                Thread.sleep(1000);
            }

            requestObserver2.onNext(UserIdx.newBuilder()
                    .addIdx(6)
                    .addIdx(7).build());
        } catch (StatusRuntimeException|InterruptedException ignored) { }

        requestObserver2.onCompleted();

        try {
            finishLatch2.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) { }

        channel.shutdown();
    }
}
