package pub_sub_service;

/**
 * Interface for a Publisher
 */
public interface Publisher {

    /**
     * Method call to publish a Message
     * @param broker the message broker to publish to
     * @param message the message to publish
     */
    void publish(Broker broker, Message message);

}
