package ServidorMulticliente;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class HiloCliente extends Thread{

    private Socket cliente;
    public HiloCliente(Socket cliente) {
        this.cliente = cliente;
    }

    @Override
    public void run() {

        try(
                ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
                ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
        ){

            // Generamos la clave publica y privada.
            KeyPair claves = generarClaves();

            // Enviamos la clave publica al cliente para que pueda cifrar los mensajes.
            salida.writeObject(claves.getPublic());
            salida.flush();

        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    public static KeyPair generarClaves() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

}
