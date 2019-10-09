package pub_sub_service;

public interface Publisher {

    void publish(Broker broker, Message message);

}
