package br.univille.observer;

public class ConcreteSubscriber
    implements Subscriber {


    public void update(String context) {
        System.out.println(context);
    }

}
