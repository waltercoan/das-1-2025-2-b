package br.univille.singleton;

public class Singleton {
    //parace com uma variável global
    private static Singleton instance;
    private String segredo;

    public String getSegredo() {
        return segredo;
    }
    public void setSegredo(String segredo) {
        this.segredo = segredo;
    }
    private Singleton(){
        System.out.println("Singleton: Construtor");
    }
    public static Singleton getInstance(){
        System.out.println("Singleton: getInstance");
        if (instance == null){
            System.out.println("Singleton: NEW");
            instance = new Singleton();
        }
        return instance;
    }
}
