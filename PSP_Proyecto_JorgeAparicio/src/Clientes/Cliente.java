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

        // Variables de Conexión.
        final String HOST = "localhost";
        final int PUERTO = 55555;

        // Variables de lógica.
        ModeloCliente cliente;
        byte[] clienteCifrado;
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
                Scanner sc = new Scanner(System.in)

        ) {

            // Recibimos la clave pública desde el servidor.
            PublicKey clavePublica = (PublicKey) entrada.readObject();

            // Variable para el manejo del Switch.
            int opcion;

            do {
                // Texto con las opciones del programa.
                System.out.println("Bienvenido al sistema de Compra-Venta de entradas:\n"
                        + "1. Registrarse\n"
                        + "2. Iniciar Sesión\n"
                        + "3. Consultar Billetes\n"
                        + "4. Comprar Billetes\n"
                        + "5. Salir\n");

                try {
                    // Leemos la orden del cliente.
                    System.out.println("¿Que operación desea realizar? (1,2,3,4,5)");
                    opcion = sc.nextInt();
                    sc.nextLine();

                    switch (opcion) {
                        case 1:

                            do {

                                // Leemos todos los datos del cliente y los mandamos a verificar.
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
                                        + "Mínimo de Caracteres -> 10\n"
                                        + "Máximo de Caracteres -> 20\n"
                                        + "Caracteres validos   -> Alfanuméricos / Especiales comunes " +
                                        "[! \" # $ % & ' ( ) * + , - . / : ; < = > ? @ [ \\\\ ] ^ _ { | } ~]\n");
                                System.out.println("Introduzca su Contraseña:");
                                contra = sc.nextLine();

                                cliente = new ModeloCliente(nombre,apellido, edad, email, usuario, contra);

                            }while(!validacionRegistro(cliente));

                            // Ciframos el cliente una vez validados los datos.
                            clienteCifrado = cifrar(cliente, clavePublica);

                            // Construimos el paquete para el envío.
                            envio = new HashMap<>();
                            envio.put("accion", 1);
                            envio.put("cliente", clienteCifrado);

                            // Enviamos el paquete.
                            salida.writeObject(envio);

                            // Mostramos la devolución de la acción recibida desde el servidor.
                            System.out.println((String) entrada.readObject());

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

                            clienteCifrado = cifrar(cliente, clavePublica);

                            // Construimos el paquete para el envío.
                            envio = new HashMap<>();
                            envio.put("accion", 2);
                            envio.put("cliente", clienteCifrado);

                            // Enviamos el paquete.
                            salida.writeObject(envio);

                            // Mostramos la devolución de la acción recibida desde el servidor.
                            System.out.println((String) entrada.readObject());

                            break;
                        case 3:

                            // Construimos el paquete para el envío.
                            envio = new HashMap<>();
                            envio.put("accion", 3);

                            // Enviamos el paquete.
                            salida.writeObject(envio);

                            // Mostramos la devolución de la acción recibida desde el servidor.
                            System.out.println((String) entrada.readObject());

                            break;
                        case 4:

                            // Introducimos el identificador del billete que deseamos comprar.
                            System.out.println("Introduce el identificador del billete que deseas comprar:");
                            identificador = sc.nextInt();
                            sc.nextLine();

                            // Construimos el paquete para el envío.
                            envio = new HashMap<>();
                            envio.put("acción", 4);
                            envio.put("identificador", identificador);

                            // Enviamos el paquete.
                            salida.writeObject(envio);

                            // Recibimos el paquete del servidor con la respuesta a la compra.
                            Map<String, Object> recibido = (Map<String, Object>) entrada.readObject();

                            // Variable para saber si la compra ha sido completada o rechazada.
                            String comprado = (String) recibido.get("estado");

                            switch (comprado) {

                                case "Completa":

                                    // Variables para almacenar el código de compra y la firma
                                    String codCompra = (String) recibido.get("codCompra");
                                    byte[] firma = (byte[]) recibido.get("firma");

                                    // Generamos el objeto con el que vamos a verificar la firma
                                    Signature verificarRSA = Signature.getInstance("SHA256withRSA");

                                    // Inicializamos el objeto con la clave pública para comprobar la firma
                                    verificarRSA.initVerify(clavePublica);

                                    // Añadimos al objeto el contenido del mensaje que hemos recibido
                                    verificarRSA.update(codCompra.getBytes());

                                    // Comprobamos el código de compra con la firma recibida, para demostrar que no ha
                                    // habido cambios durante el envio del paquete.
                                    if (verificarRSA.verify(firma)){
                                        // La firma ha sido validada
                                        System.out.println("La Firma de la compra de su entrada " +
                                                "ha sido verificada con éxito. CodCompra: " + codCompra + "\n");
                                    }else{
                                        // La firma no ha sido validada
                                        System.out.println("La Firma de la compra de su entrada " +
                                                "no ha pasado la verificación pertinente, " +
                                                "Por favor contacte con ayuda al cliente. CodCompra: " + codCompra + "\n");
                                    }

                                    break;

                                case "Incompleta":

                                    // Mostramos el mensaje con el motivo por el que la compra no se haya realizado.
                                    String msg = (String) recibido.get("msg");
                                    System.out.println(msg);

                                    break;
                            }

                            break;
                        case 5:

                            // Construimos el paquete para el envío.
                            envio = new HashMap<>();
                            envio.put("accion", 5);

                            // Enviamos el paquete.
                            salida.writeObject(envio);

                            // Mostramos el cierre del sistema.
                            System.out.println("Cerrando sistema...\n");

                            break;
                        default:

                            // Mostramos un mensaje para los casos en los que no recibamos un número entre el 1 y el 5.
                            System.out.println("Opción no válida, por favor introduzca un número del 1 al 5.\n");
                            break;
                    }

                } catch (InputMismatchException e) {

                    // Mostramos un mensaje para los casos en los que no recibamos caracteres numéricos.
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

            // Probamos los datos con sus respectivos patrones, y en caso de no pasarlos, lanzamos una excepción con el
            // mensaje oportuno para cada caso.
            if (!patronNombreApellido.matcher(cliente.getNombre()).matches()) {
                throw new Exception("Nombre no valido. (Solo se admiten caracteres alfabéticos (20 máx))\n");
            }

            if (!patronNombreApellido.matcher(cliente.getApellido()).matches()) {
                throw new Exception("Apellido no valido. (Solo se admiten caracteres alfabéticos (20 máx))\n");
            }

            if (!patronEdad.matcher(cliente.getEdad()).matches()) {
                throw new Exception("Edad no valida. (Solo se admiten caracteres numéricos (máx 2))\n");
            }

            if (!patronEmail.matcher(cliente.getEmail()).matches()) {
                throw new Exception("Email no valido. (Solo se admite formato 'Gmail.com' -> Ejemplo5@gmail.com)\n");
            }

            if (!patronUsuario.matcher(cliente.getUsuario()).matches()) {
                throw new Exception("Nombre de Usuario no valido. (Solo se admiten caracteres alfanuméricos (15 máx))\n");
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

        // Creamos el objeto Cipher utilizando el algoritmo RSA.
        Cipher cipher = Cipher.getInstance("RSA");

        // Inicializamos el objeto en modo cifrado usando la clave pública.
        cipher.init(Cipher.ENCRYPT_MODE, clave);

        // Convertimos el objeto en bytes con la función específica para ello, y los almacenamos en la variable Datos.
        byte[] datos = objetoToBytes(cliente);

        // Aplicamos el cifrado RSA y lo devolvemos.
        return cipher.doFinal(datos);
    }

    public static byte[] objetoToBytes(Object obj) throws Exception {

        // Creamos un contenedor temporal para almacenar los bytes mientras se construyen.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // Creamos el objeto que sabe convertir el objeto cliente en bytes.
        ObjectOutputStream out = new ObjectOutputStream(bos);

        // Serializamos al cliente, convirtiéndolo en una representación binaria
        // y escribiéndolo en ByteArrayOutputStream.
        out.writeObject(obj);

        // Limpiamos el stream.
        out.flush();

        // Devolvemos en objeto en bytes para el cifrado.
        return bos.toByteArray();
    }

}
