package ServidorMulticliente;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMulticliente {

    public static void main(String[] args) {
        final int PUERTO = 55555;

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor multicliente iniciado en el puerto " + PUERTO);

            while (true) {

                Socket cliente = servidor.accept();
                System.out.println("Cliente conectado desde " + cliente.getInetAddress().getHostAddress());

                HiloCliente hilo = new HiloCliente(cliente);
                hilo.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
