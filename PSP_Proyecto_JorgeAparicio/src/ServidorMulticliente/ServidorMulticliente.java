package ServidorMulticliente;

import Modelos.ModeloBillete;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMulticliente {

    public static final ModeloBillete[] listaBilletes = generarBilletes();

    public static void main(String[] args) {
        final int PUERTO = 55555;

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor multicliente iniciado en el puerto " + PUERTO);

            while (true) {

                Socket cliente = servidor.accept();
                System.out.println("Cliente conectado desde " + cliente.getInetAddress().getHostAddress());

                HiloCliente hilo = new HiloCliente(cliente, listaBilletes);
                hilo.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ModeloBillete[] generarBilletes(){

        ModeloBillete[] listaBilletes = new ModeloBillete[5];

        for (int i = 0; i < 5; i++) {
            ModeloBillete bt = new ModeloBillete();
            bt.setIdentificador(i);
            bt.setNombre("Entrada Trueno " + (i + 1));
            bt.setDisponible(true);

            listaBilletes[i] = bt;
        }

        return listaBilletes;

    }

}
