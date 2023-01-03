package grpc.server;

import grpc.GrpcServerConst;
import grpc.service.UserServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    public static void main(String[] args) {

        Server server = ServerBuilder
                .forPort(GrpcServerConst.PORT)
                .addService(new UserServiceImpl())
                .build();

        try {
            System.out.println("Listening port " + GrpcServerConst.PORT);
            server.start();
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
