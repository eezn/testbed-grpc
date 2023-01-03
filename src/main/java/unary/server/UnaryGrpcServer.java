package unary.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import unary.server.UserServiceImpl;

import java.io.IOException;

public class UnaryGrpcServer {

    private static final int PORT = 8080;

    public static void main(String[] args) {

        Server server = ServerBuilder
                .forPort(PORT)
                .addService(new UserServiceImpl())
                .build();

        try {
            server.start();
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
