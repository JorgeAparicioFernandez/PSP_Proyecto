package ServidorMulticliente;

import Modelos.ModeloBillete;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMulticliente {

    // Generamos la lista de billetes gracias a su función específica.
    public static final ModeloBillete[] listaBilletes = generarBilletes();

    public static void main(String[] args) {

        // Variable para asignar el puerto de nuestro servidor.
        final int PUERTO = 55555;

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {

            // Mostramos un mensaje informativo.
            System.out.println("Servidor multicliente iniciado en el puerto " + PUERTO);

            while (true) {

                // Esperamos a que un cliente se conecte al servidor creando un objeto
                // Socket para representar dicha conexión.
                Socket cliente = servidor.accept();

                // Mostramos un mensaje informativo.
                System.out.println("Cliente conectado desde " + cliente.getInetAddress().getHostAddress());

                // Creamos un hilo con el objeto Socket y la lista de billetes.
                HiloCliente hilo = new HiloCliente(cliente, listaBilletes);

                // Lanzamos el hilo.
                hilo.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ModeloBillete[] generarBilletes(){

        // Generamos la lista de billetes.
        ModeloBillete[] listaBilletes = new ModeloBillete[5];

        // Con un for vamos llenando la lista las entradas.
        for (int i = 0; i < 5; i++) {

            // Creamos el el objeto ModeloBillete
            ModeloBillete bt = new ModeloBillete();

            // Le introducimos el identificador, el nombre y la marcamos como disponible.
            bt.setIdentificador(i);
            bt.setNombre("Entrada Trueno " + (i + 1));
            bt.setDisponible(true);

            // Agregamos la entrada a la lista
            listaBilletes[i] = bt;
        }

        // devolvemos la lista.
        return listaBilletes;

    }

}
