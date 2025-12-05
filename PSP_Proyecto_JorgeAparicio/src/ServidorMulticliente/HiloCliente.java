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

    // Variables transmitidas desde ServidorMulticliente.
    private Socket cliente;
    private ModeloBillete[] listaBilletes;

    // Constructor de la clase HiloCliente.
    public HiloCliente(Socket cliente, ModeloBillete[] listaBilletes) {
        this.cliente = cliente;
        this.listaBilletes = listaBilletes;
    }

    @Override
    public void run() {

        try(
                ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
                ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream())
        ){

            // Generamos la clave pública y privada.
            KeyPair claves = generarClaves();

            // Enviamos la clave pública al cliente para que pueda cifrar los mensajes.
            salida.writeObject(claves.getPublic());
            salida.flush();

            // Variable para el manejo del Switch.
            int accion;

            // Variables de lógica.
            ModeloCliente clienteRegistrado = null;
            byte[] clienteCifrado;
            boolean login = false;
            Map<String, Object> envio;

            do{

                // Recibimos el paquete del servidor con la respuesta a la compra.
                Map<String, Object> recibido = (Map<String, Object>) entrada.readObject();

                // Leemos la acción requerida del paquete de datos.
                accion = (Integer) recibido.get("accion");

                switch (accion) {
                    case 1:

                        // Mostramos un mensaje informativo.
                        System.out.println("Iniciando proceso de registro... \n");

                        // Leemos el objeto clienteCifrado que nos envia el cliente
                        clienteCifrado = (byte[]) recibido.get("cliente");

                        if (clienteRegistrado == null){

                            // Desciframos el objeto ModeloCliente proporcionado por el cliente.
                            ModeloCliente mc = descifrar(clienteCifrado, claves.getPrivate());

                            // Hasheamos la contraseña con su función específica.
                            String contraHasheada = hashSHA256(mc.getContra());

                            // Guardamos los datos del cliente registrado con la contraseña hasheada.
                            clienteRegistrado = new ModeloCliente(mc.getNombre(), mc.getApellido(),
                                    mc.getEdad(), mc.getEmail(), mc.getUsuario(), contraHasheada);

                            // Mostramos un mensaje informativo.
                            String msg = "Registro de " + clienteRegistrado.getUsuario() + " completado con exito.\n";

                            System.out.println("Proceso de registro completado " +
                                    "para la sesión de "
                                    + cliente.getInetAddress().getHostAddress() + "\n");

                            salida.writeObject(msg);

                        }else{

                            // Mostramos un mensaje informativo.
                            System.out.println("Proceso de registro rechazado, " +
                                    "ya hay un cliente registrado en la sesión de "
                                    + cliente.getInetAddress().getHostAddress() + "\n");

                            String msg = "Ya hay un cliente registrado para esta sesión: "
                                       + clienteRegistrado.getUsuario() + "\n";

                            salida.writeObject(msg);

                        }

                        break;
                    case 2:

                        // Mostramos un mensaje informativo.
                        System.out.println("Iniciando proceso de inicio de sesión... \n");

                        // Leemos el objeto clienteCifrado que nos envia el cliente
                        clienteCifrado = (byte[]) recibido.get("cliente");

                        if (clienteRegistrado != null){

                            if (!login){

                                // Desciframos el objeto ModeloCliente proporcionado por el cliente.
                                ModeloCliente mc = descifrar(clienteCifrado, claves.getPrivate());

                                // Hasheamos la contraseña con su función específica.
                                String contraHasheada = hashSHA256(mc.getContra());

                                if (clienteRegistrado.getUsuario().equals(mc.getUsuario())
                                        && clienteRegistrado.getContra().equals(contraHasheada)){

                                    // Marcamos que el cliente ha iniciado sesión con éxito.
                                    login = true;

                                    // Mostramos un mensaje informativo.
                                    System.out.println("Proceso de inicio de sesión completado " +
                                            "para la sesión de "
                                            + cliente.getInetAddress().getHostAddress() + "\n");

                                    String msg = "Inicio de sesión completado con éxito, bienvenido "
                                            + mc.getUsuario() +"\n";

                                    System.out.println("Proceso de inicio de sesión completado " +
                                            "para la sesión de "
                                            + cliente.getInetAddress().getHostAddress() + "\n");

                                    salida.writeObject(msg);

                                }else{

                                    // Mostramos un mensaje informativo.
                                    System.out.println("Proceso de inicio de sesión rechazado, " +
                                            "los datos son incorrectos para la sesión de "
                                            + cliente.getInetAddress().getHostAddress() + "\n");

                                    String msg = "Inicio de sesión fallido, los datos son incorrectos.\n";

                                    salida.writeObject(msg);

                                }

                            }else{

                                // Mostramos un mensaje informativo.
                                System.out.println("Proceso de inicio de sesión rechazado, " +
                                        "ya hay un cliente dentro en la sesión de "
                                        + cliente.getInetAddress().getHostAddress() + "\n");

                                String msg = "Ya esta la sesión iniciada para este usuario: "
                                        + clienteRegistrado.getUsuario() + "\n";

                                salida.writeObject(msg);

                            }

                        }else{

                            // Mostramos un mensaje informativo.
                            System.out.println("Proceso de inicio de sesión rechazado, " +
                                    "no hay ningun cliente registrado dentro en la sesión de "
                                    + cliente.getInetAddress().getHostAddress() + "\n");

                            String msg = "No hay ningun cliente registrado para esta sesión.\n";

                            salida.writeObject(msg);
                        }

                        break;
                    case 3:

                        // Mostramos un mensaje informativo.
                        System.out.println("Iniciando proceso de mostrado de entradas...\n");

                        if(login){

                            // Creamos el objeto para poder montar el mensaje de las entradas.
                            StringBuilder msg = new StringBuilder();

                            msg.append("Lista de billetes:\n");

                            // Recorremos en un for la lista de entradas y montamos el
                            // mensaje para enviarlo al cliente.
                            for (ModeloBillete billete : listaBilletes){

                                msg.append( "Identificador: " + billete.getIdentificador()
                                        +". Nombre: " + billete.getNombre()
                                        + ". Disponible: " + billete.isDisponible() + "\n");

                            }

                            // Mostramos un mensaje informativo.
                            System.out.println("Proceso de mostrado de entradas completado " +
                                    "para la sesión de "
                                    + cliente.getInetAddress().getHostAddress() + "\n");

                            // Enviamos el mensaje.
                            salida.writeObject(msg.toString());

                        }else{

                            // Mostramos un mensaje informativo.
                            System.out.println("Proceso de mostrado de entradas rechazado, " +
                                    "no hay ningún cliente dentro en la sesión de "
                                    + cliente.getInetAddress().getHostAddress() + "\n");

                            String msg = "Debes iniciar sesión primero para acceder a esta funcionalidad.\n";

                            salida.writeObject(msg);

                        }

                        break;
                    case 4:

                        // Mostramos un mensaje informativo.
                        System.out.println("Iniciando proceso de venta de entradas...\n");

                        if (login) { // Verificamos que el cliente haya iniciado sesión

                            try {
                                // Leemos el identificador de la entrada solicitado por el cliente.
                                int identificador = (int) recibido.get("identificador");

                                // Declaramos el paquete de envío
                                envio = new HashMap<>();

                                boolean ventaExitosa = false;

                                // Sincronizamos únicamente el billete que deseamos vender para
                                // realizar el bloque de venta.
                                synchronized (listaBilletes[identificador]) {
                                    if (listaBilletes[identificador].isDisponible()) {

                                        // Marcamos el billete como vendido
                                        listaBilletes[identificador].setDisponible(false);

                                        // Generamos el codCompra
                                        Random rand = new Random();
                                        int aleatorio = rand.nextInt(11) + 10;
                                        listaBilletes[identificador].setCodCompra(
                                                clienteRegistrado.getEdad() + "/*" + clienteRegistrado.getApellido()
                                                        + (identificador + aleatorio)
                                        );

                                        // Creamos la firma
                                        Signature rsa = Signature.getInstance("SHA256withRSA");
                                        rsa.initSign(claves.getPrivate());
                                        rsa.update(listaBilletes[identificador].getCodCompra().getBytes());
                                        byte[] firma = rsa.sign();
                                        listaBilletes[identificador].setFirma(firma);

                                        // Preparamos el paquete de envío
                                        envio.put("estado", "Completa");
                                        envio.put("codCompra", listaBilletes[identificador].getCodCompra());
                                        envio.put("firma", listaBilletes[identificador].getFirma());

                                        ventaExitosa = true; // Marcamos venta exitosa
                                    }
                                }

                                // Fuera del bloque synchronized: enviamos respuesta y logs
                                if (ventaExitosa) {
                                    System.out.println("Proceso de venta de entradas completado para la sesión de "
                                            + cliente.getInetAddress().getHostAddress() + "\n");
                                } else {
                                    String msg = "La entrada " + listaBilletes[identificador].getNombre()
                                            + " con el identificador " + identificador
                                            + " ya ha sido vendida y no está disponible.\n";
                                    envio.put("estado", "Incompleta");
                                    envio.put("msg", msg);

                                    System.out.println("Proceso de venta de entradas rechazado, la entrada con el identificador proporcionado desde la sesión de "
                                            + cliente.getInetAddress().getHostAddress() + " ya ha sido vendida\n");
                                }

                                // Enviamos el paquete al cliente
                                salida.writeObject(envio);

                            } catch (ClassCastException | EOFException | ArrayIndexOutOfBoundsException e) {

                                // Mostramos un mensaje informativo
                                System.out.println("Proceso de venta de entradas rechazado, no hay ninguna entrada con el identificador proporcionado desde la sesión de "
                                        + cliente.getInetAddress().getHostAddress() + "\n");

                                String msg = "El identificador no coincide con ninguna entrada\n";
                                envio = new HashMap<>();
                                envio.put("estado", "Incompleta");
                                envio.put("msg", msg);

                                salida.writeObject(envio);

                            } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                                // Manejo de excepciones de firma
                                System.out.println("Error interno en la generación de la firma: " + e.getMessage() + "\n");
                                String msg = "Error interno en la generación de la firma de la compra.\n";
                                envio = new HashMap<>();
                                envio.put("estado", "Incompleta");
                                envio.put("msg", msg);
                                salida.writeObject(envio);
                            }

                        } else {
                            // Cliente no ha iniciado sesión
                            System.out.println("Proceso de venta de entradas rechazado, no hay ningún cliente dentro en la sesión de "
                                    + cliente.getInetAddress().getHostAddress() + "\n");

                            String msg = "Debes iniciar sesión primero para acceder a esta funcionalidad.\n";
                            envio = new HashMap<>();
                            envio.put("estado", "Incompleta");
                            envio.put("msg", msg);

                            salida.writeObject(envio);
                        }

                        break;

                    case 5:

                        // Mostramos un mensaje informativo.
                        System.out.println("Iniciando proceso de cerrar sesión...\n");
                        System.out.println("Proceso de cerrado de sesión completado para "
                                + cliente.getInetAddress().getHostAddress() + "\n");

                        break;
                }

            }while(accion != 5);

        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    public static KeyPair generarClaves() throws Exception {
        // Creamos el generador de claves con el algoritmo RSA.
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");

        // Inicializamos el generador para producir claves RSA de 2048 bits.
        gen.initialize(2048);

        // Genera el par de claves (Privada y Pública) y la devuelve.
        return gen.generateKeyPair();
    }

    public static ModeloCliente descifrar(byte[] msg, PrivateKey clave) throws Exception {

        // Creamos el objeto Cipher utilizando el algoritmo RSA.
        Cipher cipher = Cipher.getInstance("RSA");

        // Inicializamos el objeto en modo descrifrado usando la clave pública.
        cipher.init(Cipher.DECRYPT_MODE, clave);

        // Desciframos los datos recibidos.
        byte[] datosDescifrados = cipher.doFinal(msg);

        // Convertimos de vuelta los bytes descifrados a un objeto.
        return (ModeloCliente) bytesAObjeto(datosDescifrados);
    }

    public static Object bytesAObjeto(byte[] datos) throws Exception {

        // Creamos un ByteArrayInputStream a partir de los bytes recibidos, y posteriormente creamos un
        // ObjectInputStream para reconstruir el objeto desde los bytes serializados.
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(datos));

        // Leemos el objeto del flujo y lo devolvemos.
        return in.readObject();
    }

    public static String hashSHA256(String input) {
        try {

            // Creamos una instancia MessageDigest configurada con el algoritmo SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Convertimos el texto recibido en bytes.
            byte[] hashBytes = md.digest(input.getBytes());

            // Codifica los bytes del hash en formato Base64 y devulve el hash codificado como una cadena de texto.
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // Mostramos un mensaje informativo.
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }

}
