package client;

public class CustomerMain {
    public static void main(String[] args) throws Exception {
        System.out.println(">>> CustomerMain 실행됨");
        CustomerClient client = new CustomerClient();
        client.start();
        System.out.println("CustomerClient.start() 실행됨");
    }
}
