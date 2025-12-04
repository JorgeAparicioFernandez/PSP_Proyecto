package Clientes;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import Modelos.ModeloCliente;

import javax.crypto.Cipher;

public class Cliente {

    public static void main(String[] args) {

        final String HOST = "localhost";
        final int PUERTO = 55555;

        ModeloCliente cliente;
        Map<String, Object> envio;

        // Variables de Cliente.
        String nombre;
        String apellido;
        String edad;
        String email;
        String usuario;
        String contra;

        // Variables de Compra de billete.
        int identificador;

        try (

                Socket socket = new Socket(HOST, PUERTO);
                ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                Scanner sc = new Scanner(System.in);

        ) {

            PublicKey clavePublica = (PublicKey) entrada.readObject();

            int opcion;

            do {
                System.out.println("Bienvenido al sistema de Compra-Venta de entradas:\n"
                        + "1. Registrarse\n"
                        + "2. Iniciar Sesión\n"
                        + "3. Consultar Billetes\n"
                        + "4. Comprar Billetes\n"
                        + "5. Salir\n");

                try {
                    System.out.println("¿Que operación desea realizar? (1,2,3,4,5)");
                    opcion = sc.nextInt();
                    sc.nextLine();

                    switch (opcion) {
                        case 1:

                            do {

                                cliente = new ModeloCliente();

                                System.out.println("Introduzca su Nombre:");
                                nombre = sc.nextLine();

                                System.out.println("Introduzca su Apellido:");
                                apellido = sc.nextLine();

                                System.out.println("Introduzca su Edad:");
                                edad = sc.nextLine();

                                System.out.println("Introduzca su Email:");
                                email = sc.nextLine();

                                System.out.println("Introduzca su Nombre de Usuario:");
                                usuario = sc.nextLine();

                                System.out.println("Requisitos:\n"
                                        + "Minimo de Caracteres -> 10\n"
                                        + "Maximo de Caracteres -> 20\n"
                                        + "Caracteres validos   -> Alfanumericos / Especiales comunes " +
                                        "[! \" # $ % & ' ( ) * + , - . / : ; < = > ? @ [ \\\\ ] ^ _ { | } ~]\n");
                                System.out.println("Introduzca su Contraseña:");
                                contra = sc.nextLine();

                                cliente = new ModeloCliente(nombre,apellido, edad, email, usuario, contra);

                                byte[] clienteCifrado = cifrar(cliente, clavePublica);

                                // Construimos el paquete para el envio
                                envio = new HashMap<>();
                                envio.put("accion", 1);
                                envio.put("cliente", clienteCifrado);

                                // Enviamos el paquete.
                                salida.writeObject(envio);

                                // Mostramos la devolucion de la accion recibida desde el servidor.
                                System.out.println((String) entrada.readObject());

                            }while(!validacionRegistro(cliente));

                            break;
                        case 2:

                            // Limpiamos el objeto cliente.
                            cliente = new ModeloCliente();

                            // Introducimos el nombre usuario.
                            System.out.println("Introduzca su Nombre de Usuario:");
                            usuario = sc.nextLine();

                            // Introducimos la contraseña.
                            System.out.println("Introduzca su Contraseña:");
                            contra = sc.nextLine();

                            // Metemos los dato en el objeto Cliente.
                            cliente.setUsuario(usuario);
                            cliente.setContra(contra);

                            byte[] clienteCifrado = cifrar(cliente, clavePublica);

                            // Construimos el paquete para el envio.
                            envio = new HashMap<>();
                            envio.put("accion", 2);
                            envio.put("cliente", clienteCifrado);

                            // Enviamos el paquete.
                            salida.writeObject(envio);

                            // Mostramos la devolucion de la accion recibida desde el servidor.
                            System.out.println((String) entrada.readObject());

                            break;
                        case 3:

                            // Construimos el paquete para el envio.
                            envio = new HashMap<>();
                            envio.put("accion", 3);

                            // Enviamos el paquete.
                            salida.writeObject(envio);

                            // Mostramos la devolucion de la accion recibida desde el servidor.
                            System.out.println((String) entrada.readObject());

                            break;
                        case 4:

                            // Introducimos el nombre usuario.
                            System.out.println("Introduce el identificador del billete que deseas comprar:");
                            identificador = sc.nextInt();
                            sc.nextLine();

                            // Construimos el paquete para el envio.
                            envio = new HashMap<>();
                            envio.put("accion", 4);
                            envio.put("identificador", identificador);

                            // Enviamos el paquete.
                            salida.writeObject(envio);

                            Map<String, Object> recibido = (Map<String, Object>) entrada.readObject();

                            String comprado = (String) recibido.get("estado");

                            switch (comprado) {

                                case "Completa":

                                    String codCompra = (String) recibido.get("codCompra");

                                    byte[] firma = (byte[]) recibido.get("firma");

                                    // Generamos el objeto con el que vamos a verificar la firma
                                    Signature verificarRSA = Signature.getInstance("SHA256withRSA");

                                    // Inicializamos el objeto con la clave publica para comprobar la firma
                                    verificarRSA.initVerify(clavePublica);

                                    // Añadimos al objeto el contenido del mesnaje que hemos recibido
                                    verificarRSA.update(codCompra.getBytes());

                                    // Comprobamos la firma recibida con el objeto que hemos creado y mostramos el resultado
                                    if (verificarRSA.verify(firma)){
                                        // La firma ha sido validada
                                        System.out.println("La Firma de la compra de su entrada " +
                                                "ha sido verificada con exito. CodCompra: " + codCompra);
                                    }else{
                                        // La firma ha sido validada
                                        System.out.println("La Firma de la compra de su entrada " +
                                                "no ha pasado la verificación pertinente, " +
                                                "Porfavor contacte con ayuda al cliente. CodCompra: " + codCompra);
                                    }

                                    break;

                                case "Incompleta":

                                    String msg = (String) recibido.get("msg");

                                    System.out.println(msg);

                                    break;
                            }

                            break;
                        case 5:

                            // Construimos el paquete para el envio
                            envio = new HashMap<>();
                            envio.put("accion", 5);

                            // Enviamos el paquete.
                            salida.writeObject(envio);

                            // Mostramos el cierre del sistema.
                            System.out.println("Cerrando sistema...\n");

                            break;
                        default:
                            System.out.println("Opción no válida, por favor introduzca un número del 1 al 4.\n");
                            break;
                    }

                } catch (InputMismatchException e) {
                    System.out.println("Error: no se admiten caracteres no numéricos.\n");
                    sc.nextLine();
                    opcion = 0;
                }

            } while (opcion != 5);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean validacionRegistro(ModeloCliente cliente) {

        try{

            // Patrón de Validación de Nombre y Apellido.
            Pattern patronNombreApellido = Pattern.compile("^[A-Za-z]{1,20}$");

            // Patrón de Validación de la Edad.
            Pattern patronEdad = Pattern.compile("^[0-9]{1,2}$");

            // Patrón de validación del Email.
            Pattern patronEmail = Pattern.compile("^[A-Za-z0-9]+@gmail\\.com$");

            // Patrón de validación del Nombre de Usuario.
            Pattern patronUsuario = Pattern.compile("^[A-Za-z0-9]{1,15}$");

            // Patrón de validación de la contraseña.
            // p{Punct} incluye los caracteres especiales comunes:
            // [! " # $ % & ' ( ) * + , - . / : ; < = > ? @ [ \\ ] ^ _ { | } ~]
            Pattern patronContra = Pattern.compile("^[A-Za-z0-9\\p{Punct}]{10,20}$");

            if (!patronNombreApellido.matcher(cliente.getNombre()).matches()) {
                throw new Exception("Nombre no valido. (Solo se admiten caracteres alfabeticos (20 máx))\n");
            }

            if (!patronNombreApellido.matcher(cliente.getApellido()).matches()) {
                throw new Exception("Apellido no valido. (Solo se admiten caracteres alfabeticos (20 máx))\n");
            }

            if (!patronEdad.matcher(cliente.getEdad()).matches()) {
                throw new Exception("Edad no valida. (Solo se admiten cararcteres númericos (máx 2))\n");
            }

            if (!patronEmail.matcher(cliente.getEmail()).matches()) {
                throw new Exception("Email no valido. (Solo se admite formato 'Gmail.com' -> Ejemplo5@gmail.com)\n");
            }

            if (!patronUsuario.matcher(cliente.getUsuario()).matches()) {
                throw new Exception("Nombre de Usuario no valido. (Solo se admiten caracteres alfanumericos (15 máx))\n");
            }

            if (!patronContra.matcher(cliente.getContra()).matches()) {
                throw new Exception("Contraseña no valida. (No cumple con los requisitos mencionados)\n");
            }

            return true;

        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }

    }

    public static byte[] cifrar(ModeloCliente cliente, PublicKey clave) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, clave);

        byte[] datos = objetoToBytes(cliente);

        return cipher.doFinal(datos);
    }

    public static byte[] objetoToBytes(Object obj) throws Exception {

        // Creamos un contenedor temporal para almaecanr los bytes mientra se construyen.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // Creamos el objeto que sabe convertir el objeto cliente en bytes.
        ObjectOutputStream out = new ObjectOutputStream(bos);

        // Sreializamos al cliente, convirtiendolo en una representacion binaria
        // y escibiendolo en ByteArrayOutputStream.
        out.writeObject(obj);

        // Limpiamos el stream.
        out.flush();

        // Devolvemos en objeto en bytes para el cifrado.
        return bos.toByteArray();
    }

}
