package br.univille.observer;

public class Cliente {
    public static void main(String[] args) {
        //publicador
        var grupoFamilia = new Publisher();

        var zezinho = new ConcreteSubscriber();
        var huguinho = new ConcreteSubscriber();
        var tia = new ConcreteSubscriber();

        //entrou no grupo da familia
        grupoFamilia.subscribe(zezinho);
        grupoFamilia.subscribe(huguinho);
        grupoFamilia.subscribe(tia);

        grupoFamilia.setMainState("BOM DIAAA ❤️❤️❤️");
        //botao enviar
        grupoFamilia.notifySubscribers();

    }
}
