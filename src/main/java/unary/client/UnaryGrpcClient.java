package unary.client;

import com.atto.grpc.User;
import com.atto.grpc.UserIdx;
import com.atto.grpc.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class UnaryGrpcClient {
    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        // blocking stub 생성
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);

        // stub.setUser (UserServiceImpl.setUser) 호출
        UserIdx setUserResult = stub.setUser(User.newBuilder()
                .setUsername("JIN-LEE")
                .setEmail("JIN@LEE.com")
                .addRoles("USER")
                .addRoles("ADMIN")
                .build());

        System.out.println("Client: " + setUserResult.getIdx());

        // stub.getUser (UserServiceImpl.getUser) 호출
        User getUserResult = stub.getUser(setUserResult);
        System.out.println(getUserResult.toString());

        channel.shutdown();
    }
}
