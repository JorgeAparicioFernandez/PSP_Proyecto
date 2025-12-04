package ServidorMulticliente;

import Modelos.ModeloBillete;
import Modelos.ModeloCliente;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HiloCliente extends Thread{

    private Socket cliente;
    private ModeloBillete[] listaBilletes;
    public HiloCliente(Socket cliente, ModeloBillete[] listaBilletes) {
        this.cliente = cliente;
        this.listaBilletes = listaBilletes;
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

            int accion = 0;

            ModeloCliente clienteRegistrado = null;
            byte[] clienteCifrado = null;
            boolean login = false;
            Map<String, Object> envio;

            do{

                Map<String, Object> recibido = (Map<String, Object>) entrada.readObject();

                System.out.println("Envio de datos recibido");

                accion = (Integer) recibido.get("accion");

                switch (accion) {
                    case 1:

                        // Leemos el objeto clienteCifrado que nos envia el cliente
                        clienteCifrado = (byte[]) recibido.get("cliente");

                        if (clienteRegistrado == null){

                            ModeloCliente cliente = descifrar(clienteCifrado, claves.getPrivate());

                            String contraHasheada = hashSHA256(cliente.getContra());

                            clienteRegistrado = new ModeloCliente(cliente.getNombre(), cliente.getApellido(),
                                    cliente.getEdad(), cliente.getEmail(), cliente.getUsuario(), contraHasheada);

                            String msg = "Registro de " + clienteRegistrado.getUsuario() + " completado con exito.\n";

                            System.out.println(msg);

                            salida.writeObject(msg);

                        }else{

                            String msg = "Ya hay un cliente registrado para esta sesión: "
                                       + clienteRegistrado.getUsuario() + "\n";

                            salida.writeObject(msg);

                        }

                        break;
                    case 2:

                        // Leemos el objeto clienteCifrado que nos envia el cliente
                        clienteCifrado = (byte[]) recibido.get("cliente");

                        if (clienteRegistrado != null){

                            if (!login){

                                ModeloCliente cliente = descifrar(clienteCifrado, claves.getPrivate());

                                String contraHasheada = hashSHA256(cliente.getContra());

                                if (clienteRegistrado.getUsuario().equals(cliente.getUsuario())
                                        && clienteRegistrado.getContra().equals(contraHasheada)){

                                    login = true;

                                    String msg = "Inicio de sesión completado con exito, bienvenido "
                                            + cliente.getUsuario() +"\n";

                                    System.out.println(msg);

                                    salida.writeObject(msg);

                                }else{

                                    String msg = "Inicio de sesión fallido, los datos son incorrectos.\n";

                                    salida.writeObject(msg);

                                }

                            }else{

                                String msg = "Ya esta la sesión iniciada para este usuario: "
                                        + clienteRegistrado.getUsuario() + "\n";

                                salida.writeObject(msg);

                            }

                        }else{

                            String msg = "No hay ningun cliente registrado para esta sesión.\n";

                            salida.writeObject(msg);
                        }

                        break;
                    case 3:

                        if(login){

                            synchronized (listaBilletes){

                                StringBuilder msg = new StringBuilder();

                                msg.append("Lista de billetes:\n");

                                for (ModeloBillete billete : listaBilletes){

                                    msg.append( "Identificador: " + billete.getIdentificador()
                                                +". Nombre: " + billete.getNombre()
                                                + ". Disponible: " + billete.isDisponible() + "\n");

                                }

                                salida.writeObject(msg.toString());

                            }

                        }else{

                            String msg = "Debes iniciar sesión primero para acceder a esta funcionalidad.\n";

                            salida.writeObject(msg);

                        }

                        break;
                    case 4:

                        synchronized (listaBilletes){

                            if(login){

                                try{

                                    int identificador = (int) recibido.get("identificador");

                                    if(listaBilletes[identificador].isDisponible()){

                                        listaBilletes[identificador].setDisponible(false);

                                        // Creamos el objeto Signature para realizar la firma.
                                        Signature rsa = Signature.getInstance("SHA256withRSA");

                                        // Inicializamos el objeto con la clave privada para realizar la firma.
                                        rsa.initSign(claves.getPrivate());

                                        // Inicializamos el objeto Random.
                                        Random rand = new Random();

                                        // Creamos un número aleatorio entre el 10 y el 20 para el codCompra.
                                        int aleatorio = rand.nextInt(11) + 10;
                                        listaBilletes[identificador].setCodCompra(
                                                clienteRegistrado.getEdad() + "/*" + clienteRegistrado.getApellido()
                                                        + (identificador + aleatorio)
                                        );

                                        // Actualizamos el objeto introduciendo los datos del mensaje
                                        rsa.update(listaBilletes[identificador].getCodCompra().getBytes());

                                        // Generamos la firma
                                        byte[] firma = rsa.sign();

                                        // Guardamos la firma en el servidor
                                        listaBilletes[identificador].setFirma(firma);

                                        envio = new HashMap<>();
                                        envio.put("estado", "Completa");
                                        envio.put("codCompra", listaBilletes[identificador].getCodCompra());
                                        envio.put("firma", listaBilletes[identificador].getFirma());

                                        salida.writeObject(envio);

                                    }else{

                                        String msg = "La entrada " + listaBilletes[identificador].getNombre()
                                                + " con el identificador " + identificador
                                                + " ya ha sido vendida y no esta disponible.\n";

                                        envio = new HashMap<>();
                                        envio.put("estado", "Incompleta");
                                        envio.put("msg", msg);

                                        salida.writeObject(envio);

                                    }

                                }catch(ClassCastException | EOFException | ArrayIndexOutOfBoundsException e){

                                    String msg = "El identificador no coincide con ninguna entrada\n";

                                    envio = new HashMap<>();
                                    envio.put("estado", "Incompleta");
                                    envio.put("msg", msg);

                                    salida.writeObject(envio);

                                }

                            }else{

                                String msg = "Debes iniciar sesión primero para acceder a esta funcionalidad.\n";

                                envio = new HashMap<>();
                                envio.put("estado", "Incompleta");
                                envio.put("msg", msg);

                                salida.writeObject(envio);

                            }
                        }

                        break;
                    case 5:

                        System.out.println("El cliente cierra la sesión. \n");

                        break;
                }

            }while(accion != 5);

        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    public static KeyPair generarClaves() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    public static ModeloCliente descifrar(byte[] msg, PrivateKey clave) throws Exception {

        // Descifrar los bytes con RSA
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, clave);
        byte[] datosDescifrados = cipher.doFinal(msg);

        // Deserializar bytes a objeto usando la función separada
        return (ModeloCliente) bytesAObjeto(datosDescifrados);
    }

    public static Object bytesAObjeto(byte[] datos) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(datos));
        return in.readObject();
    }

    public static String hashSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }

}
