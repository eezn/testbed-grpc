package grpc.client.unary;

import com.atto.grpc.User;
import com.atto.grpc.UserIdx;
import com.atto.grpc.UserServiceGrpc;
import grpc.GrpcServerConst;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

// 1:1 (Blocking, Async, Future)
public class UnaryGrpcClient {
    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(GrpcServerConst.DOMAIN, GrpcServerConst.PORT)
                .usePlaintext()
                .build();

        // blocking stub 생성
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);

        // stub.setUser (UserServiceImpl.setUser) 호출
        UserIdx setUserResult = stub.setUser(User.newBuilder()
                .setUsername("JIN-LEE")
                .setEmail("JIN-LEE@grpc.com")
                .addRoles("USER")
                .addRoles("ADMIN")
                .build());

        System.out.println("Client: " + setUserResult.getIdx(0));

        // stub.getUser (UserServiceImpl.getUser) 호출
        User getUserResult = stub.getUser(setUserResult);
        System.out.println(getUserResult.toString());

        channel.shutdown();
    }
}
