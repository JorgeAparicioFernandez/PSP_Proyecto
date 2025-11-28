package Clientes;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Pattern;

import Modelos.ModeloCliente;

public class Cliente {

    public static void main(String[] args) {

        final String HOST = "localhost";
        final int PUERTO = 55555;

        ModeloCliente cliente;

        String nombre;
        String apellido;
        String edad;
        String email;
        String usuario;
        String contra;

        try (

                Socket socket = new Socket(HOST, PUERTO);
                ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                Scanner sc = new Scanner(System.in);

        ) {

            int opcion;

            do {
                System.out.println("Bienvenido al sistema de Compra-Venta de entradas:\n"
                        + "1. Registrarse\n"
                        + "2. Iniciar Sesión"
                        + "3. Consultar Billetes\n"
                        + "4. Comprar Billetes\n"
                        + "5. Salir\n");

                try {
                    System.out.println("¿Que operación desea realizar? (1,2,3,4)");
                    opcion = sc.nextInt();
                    sc.nextLine();

                    switch (opcion) {
                        case 1:

                            do {

                                System.out.println("Introduzca su Nombre:");
                                nombre = sc.nextLine();

                                System.out.println("Introduzca su Apellido:");
                                apellido = sc.nextLine();

                                System.out.println("Introduzca su Adad:");
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

                            }while(!validacionRegistro(cliente));

                            break;
                        case 2:

                            break;
                        case 3:

                            break;
                        case 4:

                            break;
                        case 5:
                            System.out.println("Cerrando sistema...");
                        default:
                            System.out.println("Opción no válida, por favor introduzca un número del 1 al 4.\n");
                            break;
                    }

                } catch (InputMismatchException e) {
                    System.out.println("Error: no se admiten caracteres no numéricos.\n");
                    sc.nextLine();
                    opcion = 0;
                }

            } while (opcion != 4);

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

}
